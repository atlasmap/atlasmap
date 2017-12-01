package io.atlasmap.json.core;

import java.math.BigDecimal;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

/**
 */
public class JsonFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JsonFieldWriter.class);

    private ObjectMapper objectMapper = null;
    private ObjectNode rootNode = null;

    public JsonFieldWriter() {
        this.objectMapper = new ObjectMapper();
        objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        rootNode = objectMapper.createObjectNode();
    }

    public JsonFieldWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.rootNode = objectMapper.createObjectNode();
    }

    public ObjectNode getRootNode() {
        return rootNode;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void write(Field field) throws AtlasException {
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Field: " + AtlasModelFactory.toString(field));
            LOG.debug("Field type=" + field.getFieldType() + " path=" + field.getPath() + " v=" + field.getValue());
        }
        PathUtil path = new PathUtil(field.getPath());
        String lastSegment = path.getLastSegment();
        ObjectNode parentNode = this.rootNode;
        String parentSegment = null;
        for (String segment : path.getSegments()) {
            if (!segment.equals(lastSegment)) { // this is a parent node.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing parent segment: " + segment);
                }
                JsonNode childNode = getChildNode(parentNode, parentSegment, segment);
                if (childNode == null) {
                    childNode = createParentNode(parentNode, parentSegment, segment);
                } else if (childNode instanceof ArrayNode) {
                    int index = PathUtil.indexOfSegment(segment);
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
                parentNode = (ObjectNode) childNode;
                parentSegment = segment;
            } else { // this is the last segment of the path, write the value
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing field value segment: " + segment);
                }
                writeValue(parentNode, parentSegment, segment, field);
            }
        }
    }

    public void writeValue(ObjectNode parentNode, String parentSegment, String segment, Field field)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing field value '" + segment + "' in parent node '" + parentSegment + "', parentNode: "
                    + parentNode);
        }
        JsonNode valueNode = createValueNode(field);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Value to write: " + valueNode);
        }
        String cleanedSegment = PathUtil.cleanPathSegment(segment);
        if (PathUtil.isCollectionSegment(segment)) {
            // if this field is a collection, we need to place our value in an array

            // get or construct the array the value will be placed in
            if (LOG.isDebugEnabled()) {
                LOG.debug("Field type is collection. Fetching array '" + segment + "' from parent '" + parentSegment
                        + "': " + parentNode);
            }

            ArrayNode arrayChild = (ArrayNode) getChildNode(parentNode, parentSegment, segment);
            if (arrayChild == null) {
                arrayChild = parentNode.putArray(cleanedSegment);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not find array to place value in, created it in parent: " + parentNode);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Array before placing value: " + arrayChild);
            }

            // determine where in the array our value will go
            int index = PathUtil.indexOfSegment(segment);

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
        } else {
            // on a regular primitive value, just set it in the object node parent
            parentNode.replace(cleanedSegment, valueNode);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent node after value written: " + parentNode);
        }
    }

    public static JsonNode getChildNode(ObjectNode parentNode, String parentSegment, String segment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for child node '" + segment + "' in parent '" + parentSegment + "': " + parentNode);
        }
        String cleanedSegment = PathUtil.cleanPathSegment(segment);
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

    public ObjectNode createParentNode(ObjectNode parentNode, String parentSegment, String segment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating parent node '" + segment + "' under previous parent '" + parentSegment + "' ("
                    + parentNode.getClass().getName() + ")");
        }
        ObjectNode childNode = null;
        String cleanedSegment = PathUtil.cleanPathSegment(segment);
        if (PathUtil.isCollectionSegment(segment)) {
            ArrayNode arrayChild = parentNode.putArray(cleanedSegment);
            int index = PathUtil.indexOfSegment(segment);

            if (arrayChild.size() < (index + 1)) {
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
            childNode = parentNode.putObject(cleanedSegment);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent Node '" + parentSegment + "' after adding child parent node '" + segment + "':"
                    + parentNode);
        }
        return childNode;
    }

    public JsonNode createValueNode(Field jsonField) throws AtlasException {
        FieldType type = jsonField.getFieldType();
        Object value = jsonField.getValue();
        JsonNode valueNode = null;
        if (FieldType.STRING.equals(type)) {
            valueNode = rootNode.textNode(String.valueOf(value));
        } else if (FieldType.CHAR.equals(type)) {
            valueNode = rootNode.textNode(Character.toString((char) value));
        } else if (FieldType.BOOLEAN.equals(type)) {
            valueNode = rootNode.booleanNode((Boolean) value);
        } else if (FieldType.INTEGER.equals(type)) {
            valueNode = rootNode.numberNode((Integer) value);
        } else if (FieldType.DOUBLE.equals(type) || FieldType.FLOAT.equals(type)) {
            valueNode = rootNode.numberNode(new BigDecimal(String.valueOf(value)));
        } else if (FieldType.SHORT.equals(type)) {
            valueNode = rootNode.numberNode(Short.valueOf(String.valueOf(value)));
        } else if (FieldType.LONG.equals(type)) {
            valueNode = rootNode.numberNode(Long.valueOf(String.valueOf(value)));
        } else if (FieldType.BYTE.equals(type)) {
            valueNode = rootNode.numberNode(Byte.valueOf(String.valueOf(value)));
        } else {
            throw new AtlasException(
                    "Cannot set value for " + jsonField.getPath() + " --> " + value + " for field type " + type);
        }
        if (LOG.isDebugEnabled()) {
            String valueClass = value == null ? "null" : value.getClass().getName();
            LOG.debug("Converted JsonField value to ValueNode. Type: " + type + ", value: " + value + "(" + valueClass
                    + "), node class: " + valueNode.getClass().getName() + ", node: " + valueNode);
        }
        return valueNode;
    }
}
