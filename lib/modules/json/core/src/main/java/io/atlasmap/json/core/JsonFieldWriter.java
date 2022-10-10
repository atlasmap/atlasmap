/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.json.core;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

/**
 */
public class JsonFieldWriter implements AtlasFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JsonFieldWriter.class);

    private ObjectMapper objectMapper = null;
    private ContainerNode<?> rootNode = null;

    public JsonFieldWriter() {
        this.objectMapper = new ObjectMapper()
            .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    }

    public JsonFieldWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ContainerNode<?> getRootNode() {
        return rootNode;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void write(AtlasInternalSession session) throws AtlasException {
        Field targetField = session.head().getTargetField();
        if (targetField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Field: " + AtlasModelFactory.toString(targetField));
            LOG.debug("Field type=" + targetField.getFieldType() + " path=" + targetField.getPath() + " v="
                    + targetField.getValue());
        }
        AtlasPath path = new AtlasPath(targetField.getPath());
        SegmentContext lastSegment = path.getLastSegment();

        if (this.rootNode == null) {
            if (path.hasCollectionRoot()) {
                this.rootNode = objectMapper.createArrayNode();
            } else {
                this.rootNode = objectMapper.createObjectNode();
            }
        }
        ContainerNode<?> parentNode = this.rootNode;

        SegmentContext parentSegment = null;
        for (SegmentContext segment : path.getSegments(true)) {
            if (!segment.equals(lastSegment)) { // this is a parent node.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing parent segment: " + segment);
                }
                JsonNode childNode;
                if (segment.isRoot()) {
                    if (parentNode instanceof ArrayNode) {
                        // taking care of topmost collection
                        childNode = parentNode;
                    } else {
                        parentSegment = segment;
                        continue;
                    }
                } else {
                    childNode = getChildNode(parentNode, parentSegment, segment);
                }
                if (childNode == null) {
                    childNode = createParentNode(parentNode, parentSegment, segment, targetField);
                } else if (childNode instanceof ArrayNode) {
                    Integer index = segment.getCollectionIndex();
                    if (index == null) {
                        return;
                    }

                    ArrayNode arrayChild = (ArrayNode) childNode;
                    if (arrayChild.size() < (index + 1)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Object Array is too small, resizing to accomodate index: " + index
                                    + ", current array: " + arrayChild);
                        }
                        // if our array doesn't have index + 1 items in it, add nulls until we have the
                        // index available
                        while (arrayChild.size() < (index + 1)) {
                            arrayChild.addObject();
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Object Array after resizing: " + arrayChild);
                        }
                    }
                    childNode = arrayChild.get(index);
                }
                if (childNode == null) {
                    return;
                }
                parentNode = (ObjectNode) childNode;
                parentSegment = segment;
            } else { // this is the last segment of the path, write the value
                if (targetField.getFieldType() == FieldType.COMPLEX) {
                    createParentNode(parentNode, parentSegment, segment, targetField);
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing field value segment: " + segment);
                }
                writeValue(parentNode, parentSegment, segment, targetField);
            }
        }
    }

    private void writeValue(ContainerNode<?> parentNode, SegmentContext parentSegment, SegmentContext segment, Field field)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing field value '" + segment + "' in parent node '" + parentSegment + "', parentNode: "
                    + parentNode);
        }
        JsonNode valueNode = createValueNode(field);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Value to write: " + valueNode);
        }
        String cleanedSegment = segment.getName();
        if (segment.getCollectionType() != CollectionType.NONE) {
            // if this field is a collection, we need to place our value in an array

            // get or construct the array the value will be placed in
            if (LOG.isDebugEnabled()) {
                LOG.debug("Field type is collection. Fetching array '" + segment + "' from parent '" + parentSegment
                        + "': " + parentNode);
            }

            ArrayNode arrayChild;
            if (parentSegment == null && cleanedSegment.isEmpty() && this.rootNode instanceof ArrayNode) {
                // taking care of topmost collection
                arrayChild = (ArrayNode)parentNode;
            } else {
                arrayChild = (ArrayNode) getChildNode(parentNode, parentSegment, segment);
            }
            if (arrayChild == null) {
                if (parentNode instanceof ObjectNode) {
                    arrayChild = ((ObjectNode)parentNode).putArray(cleanedSegment);
                } else if (parentNode instanceof ArrayNode) {
                    arrayChild = ((ArrayNode)parentNode).addArray();
                } else {
                    throw new AtlasException(String.format("Unknown JsonNode type '%s' for segment '%s'",
                            parentNode.getClass(), segment));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not find array to place value in, created it in parent: " + parentNode);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Array before placing value: " + arrayChild);
            }

            // determine where in the array our value will go
            Integer index = segment.getCollectionIndex();
            if (index == null) {
                // No index is specified in the collection path
                return;
            }

            if (arrayChild.size() < (index + 1)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Value Array is too small, resizing to accomodate index: " + index + ", current array: "
                            + arrayChild);
                }
                // if our array doesn't have index + 1 items in it, add nulls until we have the
                // index available
                while (arrayChild.size() < (index + 1)) {
                    arrayChild.addNull();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Value Array after resizing: " + arrayChild);
                }
            }

            // set the value in the array
            arrayChild.set(index, valueNode);
        } else if (field.getStatus() != FieldStatus.NOT_FOUND) {
            if (parentNode instanceof ArrayNode) {
                ((ArrayNode)parentNode).add(valueNode);
            } else if (parentNode instanceof ObjectNode) {
                ((ObjectNode)parentNode).replace(cleanedSegment, valueNode);
            } else {
                throw new AtlasException(String.format("Unknown JsonNode type '%s' for segment '%s'",
                        parentNode.getClass(), segment));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent node after value written: " + parentNode);
        }
    }

    private ObjectNode createParentNode(ContainerNode<?> parentNode, SegmentContext parentSegment, SegmentContext segment, Field targetField)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating parent node '" + segment + "' under previous parent '" + parentSegment + "' ("
                    + parentNode.getClass().getName() + ")");
        }
        ObjectNode childNode = null;
        String cleanedSegment = segment.getName();
        if (segment.getCollectionType() != CollectionType.NONE) {
            ArrayNode arrayChild;
            if (parentNode instanceof ObjectNode) {
                arrayChild = ((ObjectNode)parentNode).putArray(cleanedSegment);
            } else if (parentNode instanceof ArrayNode) {
                arrayChild = ((ArrayNode)parentNode).addArray();
            } else {
                throw new AtlasException(String.format("Unknown JsonNode type '%s' for segment '%s'",
                        parentNode.getClass(), segment));
            }
            Integer index = segment.getCollectionIndex();

            if (index == null) {
                // No index is specified in the collection path - create only an array node and leave it empty
                return null;
            } else if (arrayChild.size() < (index + 1)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Object Array is too small, resizing to accomodate index: " + index + ", current array: "
                            + arrayChild);
                }
                // if our array doesn't have index + 1 items in it, add objects until we have
                // the index available
                while (arrayChild.size() < (index + 1)) {
                    arrayChild.addObject();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Object Array after resizing: " + arrayChild);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created wrapper parent array node '" + segment + "': " + arrayChild);
            }
            childNode = (ObjectNode) arrayChild.get(index);
        } else {
            if (parentNode instanceof ObjectNode) {
                if (targetField.getStatus() != FieldStatus.NOT_FOUND) {
                    childNode = ((ObjectNode) parentNode).putObject(cleanedSegment);
                }
            } else if (parentNode instanceof ArrayNode) {
                childNode = ((ArrayNode) parentNode).addObject();
            } else {
                throw new AtlasException(String.format("Unknown JsonNode type '%s' for segment '%s'",
                        parentNode.getClass(), segment));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent Node '" + parentSegment + "' after adding child parent node '" + segment + "':"
                    + parentNode);
        }
        return childNode;
    }

    private JsonNode createValueNode(Field jsonField) {
        FieldType type = jsonField.getFieldType();
        Object value = jsonField.getValue();
        JsonNode valueNode = null;
        if (value == null) {
            valueNode = rootNode.nullNode();
        } else if (FieldType.STRING.equals(type)) {
            valueNode = rootNode.textNode(String.valueOf(value));
        } else if (FieldType.CHAR.equals(type)) {
            valueNode = rootNode.textNode(Character.toString((char) value));
        } else if (FieldType.BOOLEAN.equals(type)) {
            valueNode = rootNode.booleanNode((Boolean) value);
        } else if (FieldType.INTEGER.equals(type)) {
            valueNode = rootNode.numberNode((Integer) value);
        } else if (FieldType.DOUBLE.equals(type) || FieldType.FLOAT.equals(type) || FieldType.NUMBER.equals(type)) {
            valueNode = rootNode.numberNode(new BigDecimal(String.valueOf(value)));
        } else if (FieldType.SHORT.equals(type)) {
            valueNode = rootNode.numberNode(Short.valueOf(String.valueOf(value)));
        } else if (FieldType.LONG.equals(type)) {
            valueNode = rootNode.numberNode(Long.valueOf(String.valueOf(value)));
        } else if (FieldType.BYTE.equals(type)) {
            valueNode = rootNode.numberNode(Byte.valueOf(String.valueOf(value)));
        } else if (FieldType.BIG_INTEGER.equals(type)) {
            valueNode = rootNode.numberNode(new BigInteger(String.valueOf(value)));
        } else {
            valueNode = rootNode.textNode(String.valueOf(value));
        }
        if (LOG.isDebugEnabled()) {
            String valueClass = value == null ? "null" : value.getClass().getName();
            LOG.debug("Converted JsonField value to ValueNode. Type: " + type + ", value: " + value + "(" + valueClass
                    + "), node class: " + valueNode.getClass().getName() + ", node: " + valueNode);
        }
        return valueNode;
    }

    private JsonNode getChildNode(ContainerNode<?> parentNode, SegmentContext parentSegment, SegmentContext segment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for child node '" + segment + "' in parent '" + parentSegment + "': " + parentNode);
        }
        String cleanedSegment = segment.getName();
        JsonNode childNode = parentNode.path(cleanedSegment);
        if (JsonNodeType.MISSING.equals(childNode.getNodeType())) {
            childNode = null;
        }
        if (LOG.isDebugEnabled()) {
            if (childNode == null) {
                LOG.debug("Could not find child node '" + segment + "' in parent '" + parentSegment + "'.");
            } else {
                LOG.debug("Found child node '" + segment + "' in parent '" + parentSegment + "', class: "
                        + childNode.getClass().getName() + ", node: " + childNode);
            }
        }
        return childNode;
    }

}
