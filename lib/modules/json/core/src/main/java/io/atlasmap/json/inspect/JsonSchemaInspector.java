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
package io.atlasmap.json.inspect;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.json.core.JsonComplexTypeFactory;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonEnumField;
import io.atlasmap.json.v2.JsonEnumFields;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Json;

/**
 */
public class JsonSchemaInspector implements JsonInspector {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaInspector.class);
    private static JsonSchemaInspector myself = new JsonSchemaInspector();

    private JsonSchemaInspector() {
    }

    public static JsonSchemaInspector instance() {
        return myself;
    }

    public JsonDocument inspect(String schema) throws JsonInspectionException {
        if (schema == null || schema.isEmpty()) {
            throw new IllegalArgumentException("JSON schema cannot be null");
        }

        try {
            JsonDocument jsonDocument = AtlasJsonModelFactory.createJsonDocument();
            ObjectMapper objectMapper = new ObjectMapper()
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES);
            JsonNode rootNode = objectMapper.readTree(schema);

            Map<String, JsonNode> definitionMap = new HashMap<>();
            populateDefinitions(rootNode, definitionMap);
            JsonField rootNodeType = getJsonFieldBuilder("", rootNode, null, definitionMap, new HashSet<>(), false).build();

            if (rootNodeType instanceof JsonComplexType
                    && ((JsonComplexType)rootNodeType).getJsonFields().getJsonField().size() != 0) {
                if (rootNodeType.getCollectionType() == null || rootNodeType.getCollectionType() == CollectionType.NONE) {
                    jsonDocument.getFields().getField().addAll(((JsonComplexType)rootNodeType).getJsonFields().getJsonField());
                } else {
                    // taking care of topmost collection
                    jsonDocument.getFields().getField().add(rootNodeType);
                }
            } else if (rootNodeType.getFieldType() == FieldType.COMPLEX) {
                LOG.warn("No simple type nor property is defined for the root node. It's going to be empty");
            } else {
                jsonDocument.getFields().getField().add(rootNodeType);
            }

            return jsonDocument;
        } catch (Exception e) {
            throw new JsonInspectionException(e);
        }
    }

    /**
     * Store the JsonNode rather than pre-built JsonComplexType as path needs to be filled by their own.
     */
    private void populateDefinitions(JsonNode node, Map<String, JsonNode> definitionMap) {
        JsonNode definitions = node.get("definitions");
        if (definitions == null) {
            return;
        }

        definitions.fields().forEachRemaining((entry) -> {
            String name = entry.getKey();
            JsonNode def = entry.getValue();
            JsonNode id = def.get("$id");
            if (id != null && !id.asText().isEmpty()) {
                definitionMap.put(id.asText(), def);
            }
            definitionMap.put("#/definitions/" + name, def);
        });
    }

    private List<JsonField> loadProperties(JsonNode node, String parentPath, Map<String, JsonNode> definitionMap, Set<String> definitionTrace) throws JsonInspectionException {
        List<JsonField> answer = new ArrayList<>();
        JsonNode properties = node.get("properties");
        if (properties == null || !properties.fields().hasNext()) {
            LOG.warn("An object node without 'properties', it will be ignored: {}", node);
            return answer;
        }

        Iterator<Entry<String, JsonNode>> topFields = properties.fields();
        while (topFields.hasNext()) {
            Entry<String, JsonNode> entry = topFields.next();
            if (!entry.getValue().isObject()) {
                LOG.warn("Ignoring non-object field '{}'", entry);
                continue;
            }
            JsonField type = getJsonFieldBuilder(entry.getKey(), entry.getValue(), parentPath, definitionMap, definitionTrace, false).build();
            answer.add(type);
        }
        return answer;
    }

    private JsonFieldBuilder getJsonFieldBuilder(String name, JsonNode value, String parentPath,
            Map<String, JsonNode> definitionMap, Set<String> definitionTrace, boolean isArray) throws JsonInspectionException {
        LOG.trace("--> Field:[name=[{}], value=[{}], parentPath=[{}]", name, value, parentPath);
        JsonFieldBuilder builder = new JsonFieldBuilder();
        if (name != null) {
            builder.name = name;
            builder.path = (parentPath != null && !parentPath.equals("/")
                    ? parentPath.concat("/") : "/").concat(name);
        }
        if (isArray) {
            builder.path += "<>";
            builder.collectionType = CollectionType.LIST;
        }
        builder.status = FieldStatus.SUPPORTED;

        JsonNode nodeValue = value;
        populateDefinitions(nodeValue, definitionMap);
        if (isRecursive(nodeValue, definitionTrace)) {
            builder.type = FieldType.COMPLEX;
            builder.status = FieldStatus.CACHED;
            return builder;
        } else {
            definitionTrace = new HashSet<>(definitionTrace);
            nodeValue = resolveReference(nodeValue, definitionMap, definitionTrace);
        }

        JsonNode fieldEnum = nodeValue.get("enum");
        if (fieldEnum != null) {
            builder.type = FieldType.COMPLEX;
            if (fieldEnum.isArray()) {
                final JsonFieldBuilder finalBuilder = builder;
                ((ArrayNode)fieldEnum).forEach(item -> {
                    JsonEnumField itemField = new JsonEnumField();
                    itemField.setName(item.isNull() ? null : item.asText());
                    finalBuilder.enumFields.getJsonEnumField().add(itemField);
                });
            } else if (!fieldEnum.isEmpty()) {
                JsonEnumField itemField = new JsonEnumField();
                itemField.setName(fieldEnum.isNull() ? null : fieldEnum.asText());
                builder.enumFields.getJsonEnumField().add(itemField);
            }
            return builder;
        }

        JsonNode fieldType = nodeValue.get("type");
        if (fieldType == null || fieldType.asText() == null) {
            LOG.warn("'type' is not defined for node '{}', assuming as an object", name);
            builder.type = FieldType.COMPLEX;
            builder.subFields.getJsonField().addAll(loadProperties(nodeValue, builder.path, definitionMap, definitionTrace));
            return builder;
        } else if ("array".equals(fieldType.asText())) {
            JsonNode arrayItems = nodeValue.get("items");
            if (arrayItems == null || !arrayItems.fields().hasNext()) {
                LOG.warn("'{}' is an array node, but no 'items' found in it. It will be ignored", name);
                builder.status = FieldStatus.UNSUPPORTED;
            } else {
                builder = getJsonFieldBuilder(name, arrayItems, parentPath, definitionMap, definitionTrace,true);
            }
            return builder;
        }
        List<String> jsonTypes = new LinkedList<>();
        if (fieldType instanceof ArrayNode) {
            ((ArrayNode)fieldType).spliterator().forEachRemaining(node -> jsonTypes.add(node.asText()));
        } else {
            jsonTypes.add(fieldType.asText());
        }
        processFieldType(builder, jsonTypes, nodeValue, definitionMap, definitionTrace);
        
        return builder;
    }

    private void processFieldType(JsonFieldBuilder builder, List<String> jsonTypes,
            JsonNode nodeValue, Map<String, JsonNode> definitionMap, Set<String> definitionTrace) throws JsonInspectionException {
        String jsonType = jsonTypes.get(0);
        if (jsonTypes.size() > 1) {
            if (jsonTypes.contains("object")) {
                jsonType = "object";
            } else if (jsonTypes.contains("string")) {
                jsonType = "string";
            } else if (jsonTypes.contains("number")) {
                jsonType = "number";
            } else if (jsonTypes.contains("integer")) {
                jsonType = "integer";
            } else if (jsonTypes.contains("boolean")) {
                jsonType = "boolean";
            } else {
                jsonType = "null";
            }
        }
        if ("boolean".equals(jsonType)) {
            builder.type = FieldType.BOOLEAN;
        } else if ("integer".equals(jsonType)) {
            builder.type = FieldType.BIG_INTEGER;
        } else if ("null".equals(jsonType)) {
            builder.type = FieldType.NONE;
        } else if ("number".equals(jsonType)) {
            builder.type = FieldType.NUMBER;
        } else if ("string".equals(jsonType)) {
            builder.type = FieldType.STRING;
        } else {
            if (!"object".equals(jsonType)) {
                LOG.warn("Unsupported field type '{}' found, assuming as an object", jsonType);
            }
            builder.type = FieldType.COMPLEX;
            builder.subFields.getJsonField().addAll(loadProperties(nodeValue, builder.path, definitionMap, definitionTrace));
        }
    }

    private class JsonFieldBuilder {
        private String name;
        private String path;
        private FieldType type;
        private CollectionType collectionType;
        private FieldStatus status;
        private JsonFields subFields = new JsonFields();
        private JsonEnumFields enumFields = new JsonEnumFields();

        public JsonField build() {
            JsonField answer;
            if (type == FieldType.COMPLEX) {
                JsonComplexType complex = JsonComplexTypeFactory.createJsonComlexField();
                complex.setJsonFields(subFields);
                complex.setJsonEnumFields(enumFields);
                if (!enumFields.getJsonEnumField().isEmpty()) {
                    complex.setEnumeration(true);
                }
                answer = complex;
            } else {
                answer = new JsonField();
                answer.setFieldType(type);
            }
            answer.setName(name);
            answer.setPath(path);
            answer.setCollectionType(collectionType);
            answer.setStatus(status);
            return answer;
        }
    }

    private boolean isRecursive(JsonNode node, Set<String> definitionTrace) {
        if (node.get("$ref") == null) {
            return false;
        }
        String uri = node.get("$ref").asText();
        if (uri == null || uri.isEmpty()) {
            return false;
        }

        return definitionTrace.contains(uri);
    }

    private JsonNode resolveReference(JsonNode node, Map<String, JsonNode> definitionMap, Set<String> definitionTrace) {
        if (node.get("$ref") == null) {
            return node;
        }
        String uri = node.get("$ref").asText();
        if (uri == null || uri.isEmpty()) {
            return node;
        }

        LOG.trace("Resolving JSON schema reference '{}'", uri);
        // internal reference precedes even if it's full URL
        JsonNode def = definitionMap.get(uri);
        if (def != null) {
            definitionTrace.add(uri);
            return def;
        }

        // then try external resource
        try {
            JsonNode external = Json.mapper().readTree(new URI(uri).toURL().openStream());
            LOG.trace("Successfully fetched external JSON schema '{}'    ", uri);
            definitionMap.put(uri, external);
            return external;
        } catch (Exception e) {
            LOG.debug("", e);
            LOG.warn("The referenced schema '{}' is not found. Ignoring", node.get("$ref"));
            return node;
        }
    }

}
