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

import java.util.LinkedList;
import java.util.List;

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
        if (rootNode == null) {
            throw new AtlasException("document is not set");
        }

        Field field = session.head().getSourceField();
        AtlasPath path = new AtlasPath(field.getPath());
        FieldGroup fieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
            session.head().setSourceField(fieldGroup);
        }

        List<JsonNode> valueNodes = new LinkedList<>();
        if (path.getSegments().size() >= 1) {
            JsonNode valueNode = null;
            if (rootNode.size() == 1 && !path.getSegments().get(0).startsWith(rootNode.fieldNames().next())) {
                // peel off a rooted object
                valueNode = rootNode.elements().next();
            } else {
                valueNode = rootNode;
            }

            if (valueNode !=null) {
                valueNodes.add(valueNode);
            }
            // need to walk the path....
            for (String nodeName : path.getSegments()) {
                if (valueNodes.size() == 0) {
                    break;
                }
                valueNodes = getValueNode(session, field, valueNodes, nodeName);
            }
        }
        if (fieldGroup != null) {
            for (int i=0; i<valueNodes.size(); i++) {
                JsonNode terminalNode = valueNodes.get(i);
                JsonField jsonField = new JsonField();
                AtlasModelFactory.copyField(field, jsonField, false);
                AtlasPath subPath = new AtlasPath(jsonField.getPath());
                subPath.setVacantCollectionIndex(i);
                jsonField.setPath(subPath.toString());
                Object v = handleValueNode(session, terminalNode, jsonField);
                jsonField.setValue(v);
                fieldGroup.getField().add(jsonField);
            }
            return fieldGroup;
        } else if (valueNodes.size() > 0){
            JsonField jsonField = (JsonField)field;
            Object v = handleValueNode(session, valueNodes.get(0), jsonField);
            jsonField.setValue(v);
        }
        return field;
    }

    private List<JsonNode> getValueNode(AtlasInternalSession session, Field field, List<JsonNode> parents, String nodeName) {
        boolean isCollection = false;
        String strippedNodeName = nodeName;
        Integer index = null;
        if (AtlasPath.isCollection(nodeName)) {
            isCollection = true;
            index = AtlasPath.indexOfSegment(nodeName);
            strippedNodeName = AtlasPath.cleanPathSegment(nodeName);
        }
        List<JsonNode> answer = new LinkedList<>();
        for (JsonNode parent : parents) {
            if (parent == null) {
                continue;
            }
            JsonNode node = parent.get(strippedNodeName);
            if (node == null) {
                continue;
            }
            if (!isCollection) {
                if (node.isArray()) {
                    answer.add(node.get(0));
                } else {
                    answer.add(node);
                }
                continue;
            } else if (!node.isArray()) {
                answer.add(node);
                continue;
            }

            // isCollection && node.isArray()
            if (index == null) {
                for (int i=0; i<node.size(); i++) {
                    answer.add(node.get(i));
                }
            } else if (index >= 0 && index < node.size()) {
                answer.add(node.get(index));
            } else {
                AtlasUtil.addAudit(session, field.getDocId(),
                        String.format("Detected out of range index for field p=%s, ignoring...", nodeName),
                        field.getPath(), AuditStatus.WARN, node.asText());
                LOG.warn(String.format("", nodeName));
            }
        }
        return answer;
    }

    private Object handleValueNode(AtlasInternalSession session, JsonNode valueNode, JsonField jsonField) throws AtlasException {
        if (valueNode.isNull()) {
            return null;
            // we can't detect field type if it's null node
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
            throw new AtlasException(new IllegalArgumentException("document cannot be null nor empty"));
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
