/**
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
import java.util.LinkedList;
import java.util.List;

import io.atlasmap.v2.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
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
            AtlasUtil.addAudit(session, field.getDocId(),
                String.format("Cannot read a field '%s' of JSON document '%s', document is null",
                    field.getPath(), field.getDocId()),
                field.getPath(), AuditStatus.ERROR, null);
            return field;
        }

        AtlasPath path = new AtlasPath(field.getPath());

        List<JsonField> fields = getJsonFieldsForPath(session, rootNode, field, path, 0);
        if (path.hasCollection() && !path.isIndexedCollection()) {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
            fieldGroup.getField().addAll(fields);
            session.head().setSourceField(fieldGroup);
            return fieldGroup;
        } else if (fields.size() == 1) {
            field.setValue(fields.get(0).getValue());
            return field;
        } else {
            return field;
        }
    }

    private List<JsonField> getJsonFieldsForPath(AtlasInternalSession session, JsonNode node, Field field, AtlasPath path, int depth) throws AtlasException {
        List<JsonField> fields = new ArrayList<>();
        List<SegmentContext> segments = path.getSegments(true);

        if (node.isValueNode() && segments.size() == depth) {
            //if traversed the entire path and found value
            JsonField jsonField = new JsonField();
            AtlasModelFactory.copyField(field, jsonField, true);
            Object value = handleValueNode(session, node, jsonField);
            jsonField.setValue(value);
            fields.add(jsonField);
        } else if (segments.size() > depth) {
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

            if (child != null) {
                if (child.isArray()) {
                    if (segmentContext.getCollectionIndex() != null) {
                        if (child.size() <= segmentContext.getCollectionIndex()) {
                            //index out of range
                            return fields;
                        }
                        List<JsonField> arrayFields = getJsonFieldsForPath(session, child.get(segmentContext.getCollectionIndex()), field, path, depth + 1);
                        fields.addAll(arrayFields);
                    } else {
                        //if index not included, iterate over all
                        int arrayIndex = 0;
                        for (JsonNode arrayItem : child) {
                            List<JsonField> arrayFields = getJsonFieldsForPath(session, arrayItem, field, path, depth + 1);
                            for (JsonField arrayField : arrayFields) {
                                AtlasPath subPath = new AtlasPath(arrayField.getPath());
                                //include the array index within the path
                                subPath.setCollectionIndex(depth, arrayIndex);
                                arrayField.setPath(subPath.toString());
                            }
                            fields.addAll(arrayFields);
                            arrayIndex++;
                        }
                    }
                } else {
                    List<JsonField> childFields = getJsonFieldsForPath(session, child, field, path, depth + 1);
                    fields.addAll(childFields);
                }
            }
        }
        return fields;
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
            AtlasUtil.addAudit(session, jsonField.getDocId(),
                    String.format("Unexpected array node is detected: '%s'", valueNode.asText()),
                    jsonField.getPath(), AuditStatus.ERROR, valueNode.asText());
            return null;
        }

        if (jsonField.getFieldType() != null) { // mapping is overriding the fieldType
            try {
                return conversionService.convertType(valueNode.asText(), jsonField.getFormat(),
                        jsonField.getFieldType(), null);
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, jsonField.getDocId(),
                        String.format("Failed to convert field value '%s' into type '%s'", valueNode.asText(),
                                jsonField.getFieldType()),
                        jsonField.getPath(), AuditStatus.ERROR, valueNode.asText());
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
            ObjectMapper mapper = new ObjectMapper();
            JsonParser parser = factory.createParser(document);
            this.rootNode = mapper.readTree(parser);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

}
