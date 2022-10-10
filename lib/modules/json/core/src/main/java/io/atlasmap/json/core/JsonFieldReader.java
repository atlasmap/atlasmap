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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonEnumField;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

public class JsonFieldReader implements AtlasFieldReader {

    private static final Logger LOG = LoggerFactory.getLogger(JsonFieldReader.class);

    private AtlasConversionService conversionService;
    private JsonNode rootNode;

    @SuppressWarnings("unused")
    private JsonFieldReader() {
    }

    public JsonFieldReader(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Field read(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getSourceField();
        if (rootNode == null) {
            AtlasUtil.addAudit(session, field,
                String.format("Cannot read a field '%s' of JSON document '%s', document is null",
                    field.getPath(), field.getDocId()),
                AuditStatus.ERROR, null);
            return field;
        }

        AtlasPath path = new AtlasPath(field.getPath());

        List<Field> fields = getJsonFieldsForPath(session, rootNode, field, path, 0);
        if (path.hasCollection() && !path.isIndexedCollection()) {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
            fieldGroup.getField().addAll(fields);
            if (fields.size() == 0) {
                fieldGroup.setStatus(FieldStatus.NOT_FOUND);
            }
            session.head().setSourceField(fieldGroup);
            return fieldGroup;
        } else if (fields.size() == 1) {
            field.setValue(fields.get(0).getValue());
            return field;
        } else {
            if (fields.size() == 0) {
                field.setStatus(FieldStatus.NOT_FOUND);
            }
            return field;
        }
    }

    private List<Field> getJsonFieldsForPath(AtlasInternalSession session, JsonNode node, Field field, AtlasPath path, int depth) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        List<SegmentContext> segments = path.getSegments(true);
        if (segments.size() < depth) {
            throw new AtlasException(String.format("depth '%s' exceeds segment size '%s'", depth, segments.size()));
        }
        if (segments.size() == depth) {
            //if traversed the entire path and found value
            if (field.getFieldType() == FieldType.COMPLEX && !node.isValueNode()) {
                FieldGroup group = (FieldGroup) field;
                populateChildFields(session, node, group, path);
                fields.add(group);
            } else {
                JsonField jsonField = new JsonField();
                AtlasModelFactory.copyField(field, jsonField, true);
                if (field instanceof JsonEnumField && field.getFieldType() == FieldType.COMPLEX) {
                    jsonField.setFieldType(FieldType.STRING); // enum has COMPLEX by default
                }
                Object value = handleValueNode(session, node, jsonField);
                jsonField.setValue(value);
                jsonField.setIndex(null); //reset index for subfields
                fields.add(jsonField);
            }
            return fields;
        }

        // segments.size() > depth
        SegmentContext segmentContext;
        JsonNode child;
        if (depth == 0 && path.hasCollectionRoot()) {
            child = node; //if root is a collection
            segmentContext = segments.get(depth);
        } else {
            if (depth == 0) {
                if (node.size() == 1 && !path.getSegments(false).get(0).getExpression().startsWith(rootNode.fieldNames().next())) {
                    //peel off a rooted object, i.e. mapping /orderId works for document { source: { orderId: 123 } }
                    node = node.elements().next();
                }

                if (segments.size() > 1) {
                    depth = 1; //skip the root, if not a collection
                }
            }
            segmentContext = segments.get(depth);
            String fieldName = segmentContext.getName();
            child = node.get(fieldName);
        }
        if (child == null) {
            return fields;
        }

        if (segmentContext.getCollectionType() == CollectionType.NONE) {
            List<Field> childFields = getJsonFieldsForPath(session, child, field, path, depth + 1);
            fields.addAll(childFields);
            return fields;
        }

        // collection
        if (segmentContext.getCollectionIndex() != null) {
            if (child.size() <= segmentContext.getCollectionIndex()) {
                //index out of range
                return fields;
            }
            List<Field> arrayFields = getJsonFieldsForPath(session, child.get(segmentContext.getCollectionIndex()), field, path, depth + 1);
            fields.addAll(arrayFields);
        } else {
            //if index not included, iterate over all
            for (int i=0; i<child.size(); i++) {
                Field itemField;
                if (field instanceof FieldGroup) {
                    itemField = AtlasJsonModelFactory.cloneFieldGroup((FieldGroup)field);
                    AtlasPath.setCollectionIndexRecursively((FieldGroup)itemField, depth, i);
                 } else {
                    itemField = AtlasJsonModelFactory.cloneField((JsonField)field, false);
                    AtlasPath itemPath = new AtlasPath(field.getPath());
                    itemPath.setCollectionIndex(depth, i);
                    itemField.setPath(itemPath.toString());
                 }
                List<Field> arrayFields = getJsonFieldsForPath(
                    session, child.get(i), itemField, new AtlasPath(itemField.getPath()), depth + 1);
                fields.addAll(arrayFields);
            }
        }
        return fields;
    }

    private void populateChildFields(AtlasInternalSession session, JsonNode node, FieldGroup fieldGroup, AtlasPath path)
     throws AtlasException {
        List<Field> newChildren = new ArrayList<>();
        for (Field child : fieldGroup.getField()) {
            AtlasPath childPath = new AtlasPath(child.getPath());
            JsonNode childNode = node.get(childPath.getLastSegment().getName());
            if (childPath.getLastSegment().getCollectionType() != CollectionType.NONE) {
                FieldGroup childGroup = populateCollectionItems(session, (ArrayNode)childNode, child);
                newChildren.add(childGroup);
            } else {
                if (child instanceof FieldGroup) {
                    populateChildFields(session, childNode, (FieldGroup)child, childPath);
                } else {
                    Object value = handleValueNode(session, childNode, (JsonField)child);
                    child.setValue(value);
                }
                newChildren.add(child);
            }
        }
        fieldGroup.getField().clear();
        fieldGroup.getField().addAll(newChildren);
    }

    private FieldGroup populateCollectionItems(AtlasInternalSession session, JsonNode node, Field field)
     throws AtlasException {
        if (!node.isArray()) {
            throw new AtlasException(String.format("Couldn't find JSON array for field %s:%s",
                field.getDocId(), field.getPath()));
        }
        FieldGroup group = field instanceof FieldGroup ?
         (FieldGroup)field : AtlasModelFactory.createFieldGroupFrom(field, true);
        ArrayNode arrayNode = (ArrayNode)node;
        for (int i=0; i<arrayNode.size(); i++) {
            AtlasPath itemPath = new AtlasPath(group.getPath());
            List<SegmentContext> segments = itemPath.getSegments(true);
            itemPath.setCollectionIndex(segments.size() - 1, i);
            if (field instanceof FieldGroup) {
                FieldGroup itemGroup = AtlasJsonModelFactory.cloneFieldGroup((FieldGroup)field);
                AtlasPath.setCollectionIndexRecursively(itemGroup, segments.size(), i);
                populateChildFields(session, arrayNode.get(i), itemGroup, itemPath);
                group.getField().add(itemGroup);
            } else {
                JsonField itemField = AtlasJsonModelFactory.cloneField((JsonField)field, false);
                itemField.setPath(itemPath.toString());
                Object value = handleValueNode(session, arrayNode.get(i), itemField);
                itemField.setValue(value);
                group.getField().add(itemField);
            }
        }
        return group;
    }

    private Object handleValueNode(AtlasInternalSession session, JsonNode valueNode, JsonField jsonField) throws AtlasException {
        if (valueNode.isNull()) {
            return null;
            // we can't detect field type if it's null node
        }
        if (valueNode.isObject()) {
            jsonField.setFieldType(FieldType.COMPLEX);
            return null;
        }
        if (valueNode.isArray()) {
            AtlasUtil.addAudit(session, jsonField,
                    String.format("Unexpected array node is detected: '%s'", valueNode.asText()),
                    AuditStatus.ERROR, valueNode.asText());
            return null;
        }

        if (jsonField.getFieldType() != null) { // mapping is overriding the fieldType
            try {
                return conversionService.convertType(valueNode.asText(), jsonField.getFormat(),
                        jsonField.getFieldType(), null);
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, jsonField,
                        String.format("Failed to convert field value '%s' into type '%s'", valueNode.asText(),
                                jsonField.getFieldType()),
                        AuditStatus.ERROR, valueNode.asText());
                return null;
            }
        }

        if (valueNode.isTextual()) {
            return handleTextualNode(valueNode, jsonField);
        } else if (valueNode.isNumber()) {
            return handleNumberNode(valueNode, jsonField);
        } else if (valueNode.isBoolean()) {
            return handleBooleanNode(valueNode, jsonField);
        } else if (valueNode.isContainerNode()) {
            return handleContainerNode(valueNode, jsonField);
        } else if (valueNode.isNull()) {
            return null;

        } else {
            LOG.warn(String.format("Detected unsupported json type for field p=%s docId=%s",
                    jsonField.getPath(), jsonField.getDocId()));
            jsonField.setFieldType(FieldType.UNSUPPORTED);
            return valueNode.toString();
        }
    }

    private Object handleTextualNode(JsonNode valueNode, JsonField jsonField) {
        if (jsonField.getFieldType() == null || FieldType.STRING.equals(jsonField.getFieldType())) {
            jsonField.setFieldType(FieldType.STRING);
            return valueNode.textValue();
        }
        if (FieldType.CHAR.equals(jsonField.getFieldType())) {
            return valueNode.textValue().charAt(0);
        }
        LOG.warn(String.format("Unsupported FieldType for text data t=%s p=%s docId=%s",
                jsonField.getFieldType().value(), jsonField.getPath(), jsonField.getDocId()));
        return valueNode.textValue();
    }

    private Object handleNumberNode(JsonNode valueNode, JsonField jsonField) {
        if (valueNode.isInt()) {
            jsonField.setFieldType(FieldType.INTEGER);
            return valueNode.intValue();
        } else if (valueNode.isDouble()) {
            jsonField.setFieldType(FieldType.DOUBLE);
            return valueNode.doubleValue();
        } else if (valueNode.isBigDecimal()) {
            jsonField.setFieldType(FieldType.DECIMAL);
            return valueNode.decimalValue();
        } else if (valueNode.isFloat()) {
            jsonField.setFieldType(FieldType.DOUBLE);
            return valueNode.floatValue();
        } else if (valueNode.isLong()) {
            jsonField.setFieldType(FieldType.LONG);
            return valueNode.longValue();
        } else if (valueNode.isShort()) {
            jsonField.setFieldType(FieldType.SHORT);
            return valueNode.shortValue();
        } else if (valueNode.isBigInteger()) {
            jsonField.setFieldType(FieldType.BIG_INTEGER);
            return valueNode.bigIntegerValue();
        } else {
            jsonField.setFieldType(FieldType.NUMBER);
            return valueNode.numberValue();
        }
    }

    private Object handleBooleanNode(JsonNode valueNode, JsonField jsonField) {
        jsonField.setFieldType(FieldType.BOOLEAN);
        return valueNode.booleanValue();
    }

    private Object handleContainerNode(JsonNode valueNode, JsonField jsonField) throws AtlasException {
        if (valueNode.isArray()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        String.format("Detected json array p=%s docId=%s", jsonField.getPath(), jsonField.getDocId()));
            }
            jsonField.setFieldType(FieldType.COMPLEX);
            jsonField.setCollectionType(CollectionType.ARRAY);
            return valueNode.toString();
        }
        if (valueNode.isObject()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Detected json complex object p=%s docId=%s", jsonField.getPath(),
                        jsonField.getDocId()));
            }
            jsonField.setFieldType(FieldType.COMPLEX);
            return valueNode.toString();
        }
        throw new AtlasException("Unknown error: detected a container JSON node which is not ARRAY nor OBJECT");
    }

    public void setDocument(String document) throws AtlasException {
        if (document == null || document.isEmpty()) {
            this.rootNode = null;
            return;
        }

        try {
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper()
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES);
            JsonParser parser = factory.createParser(document);
            this.rootNode = mapper.readTree(parser);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

}
