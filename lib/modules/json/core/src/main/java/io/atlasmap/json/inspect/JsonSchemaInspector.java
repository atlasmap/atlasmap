package io.atlasmap.json.inspect;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.json.core.JsonComplexTypeFactory;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

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
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(schema);

            Map<String, JsonNode> definitionMap = new HashMap<>();
            populateDefinitions(rootNode, definitionMap);
            JsonField rootNodeType = getJsonFieldBuilder("", rootNode, null, definitionMap, false).build();

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

    private List<JsonField> loadProperties(JsonNode node, String parentPath, Map<String, JsonNode> definitionMap) throws JsonInspectionException {
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
            JsonField type = getJsonFieldBuilder(entry.getKey(), entry.getValue(), parentPath, definitionMap, false).build();
            answer.add(type);
        }
        return answer;
    }

    private JsonFieldBuilder getJsonFieldBuilder(String name, JsonNode value, String parentPath, Map<String, JsonNode> definitionMap, boolean isArray) throws JsonInspectionException {
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
        nodeValue = resolveReference(nodeValue, definitionMap);

        JsonNode fieldType = nodeValue.get("type");
        if (fieldType == null || fieldType.asText() == null) {
            LOG.warn("'type' is not defined for node '{}', assuming as an object", name);
            builder.type = FieldType.COMPLEX;
            builder.subFields.getJsonField().addAll(loadProperties(nodeValue, builder.path, definitionMap));
        } else if ("array".equals(fieldType.asText())) {
            JsonNode arrayItems = nodeValue.get("items");
            if (arrayItems == null || !arrayItems.fields().hasNext()) {
                LOG.warn("'{}' is an array node, but no 'items' found in it. It will be ignored", name);
                builder.status = FieldStatus.UNSUPPORTED;
            } else {
                builder = getJsonFieldBuilder(name, arrayItems, parentPath, definitionMap, true);
            }
        } else if ("boolean".equals(fieldType.asText())) {
            builder.type = FieldType.BOOLEAN;
        } else if ("integer".equals(fieldType.asText())) {
            builder.type = FieldType.INTEGER;
        } else if ("null".equals(fieldType.asText())) {
            builder.type = FieldType.NONE;
        } else if ("number".equals(fieldType.asText())) {
            builder.type = FieldType.NUMBER;
        } else if ("string".equals(fieldType.asText())) {
            builder.type = FieldType.STRING;
        } else {
            if (!"object".equals(fieldType.asText())) {
                LOG.warn("Unsupported field type '{}' found, assuming as an object", fieldType.asText());
            }
            builder.type = FieldType.COMPLEX;
            builder.subFields.getJsonField().addAll(loadProperties(nodeValue, builder.path, definitionMap));
        }

        return builder;
    }

    private class JsonFieldBuilder {
        private String name;
        private String path;
        private FieldType type;
        private CollectionType collectionType;
        private FieldStatus status;
        private JsonFields subFields = new JsonFields();

        public JsonField build() {
            JsonField answer;
            if (type == FieldType.COMPLEX) {
                JsonComplexType complex = JsonComplexTypeFactory.createJsonComlexField();
                complex.setJsonFields(subFields);
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

    private JsonNode resolveReference(JsonNode node, Map<String, JsonNode> definitionMap) {
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
            return def;
        }

        // then try external resource
        try {
            JsonNode external = new ObjectMapper().readTree(new URI(uri).toURL().openStream());
            LOG.trace("Successfully fetched external JSON schema '{}'    ", uri);
            return external;
        } catch (Exception e) {
            LOG.debug("", e);
            LOG.warn("The referenced schema '{}' is not found. Ignoring", node.get("$ref"));
            return node;
        }
    }

}
