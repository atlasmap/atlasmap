package io.atlasmap.json.inspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 */
public class SchemaInspector implements JsonInspector {

    private static final Logger logger = LoggerFactory.getLogger(SchemaInspector.class);
    private static SchemaInspector myself = new SchemaInspector();

    private SchemaInspector() {
    }

    public static SchemaInspector instance() {
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
            JsonComplexType rootNodeType = createJsonComplexType(null, rootNode, null, definitionMap);
            
            if (rootNodeType.getCollectionType() == CollectionType.LIST) {
                logger.warn("Topmost array is not supported");
                rootNodeType.getJsonFields().getJsonField().clear();
                rootNodeType.setStatus(FieldStatus.UNSUPPORTED);
                jsonDocument.getFields().getField().add(rootNodeType);
            } else if (rootNodeType.getJsonFields().getJsonField().size() != 0) {
                jsonDocument.getFields().getField().addAll(rootNodeType.getJsonFields().getJsonField());
            } else if (rootNodeType.getFieldType() == FieldType.COMPLEX) {
                logger.warn("No simple type nor property is defined for the root node. It's going to be empty");
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
     * 
     * TODO do we need to honor pointer reference vs. full URI? as long as the pointer is always from root document,
     * the pointer works as a unique key, therefore not necessary to resolve to full URI.
     */
    private void populateDefinitions(JsonNode node, Map<String, JsonNode> definitionMap) {
        JsonNode definitions = node.get("definitions");
        if (definitions == null) {
            return;
        }

        definitions.forEach(entry -> {
            JsonNode id = entry.get("$id");
            if (id == null || id.asText().isEmpty()) {
                logger.warn("$id must be specified for the definition '{}', ignoring", entry);
            } else {
                definitionMap.put(id.asText(), entry);
            }
        });
    }

    private List<JsonField> loadProperties(JsonNode node, String parentPath, Map<String, JsonNode> definitionMap) throws JsonInspectionException {
        List<JsonField> answer = new ArrayList<>();
        JsonNode properties = node.get("properties");
        if (properties == null || !properties.fields().hasNext()) {
            logger.warn("An object node without 'properties', it will be ignored: {}", node);
            return answer;
        }

        Iterator<Entry<String, JsonNode>> topFields = properties.fields();
        while (topFields.hasNext()) {
            Entry<String, JsonNode> entry = topFields.next();
            if (!entry.getValue().isObject()) {
                logger.warn("Ignoring non-object field '{}'", entry);
                continue;
            }
            JsonComplexType type = createJsonComplexType(entry.getKey(), entry.getValue(), parentPath, definitionMap);
            answer.add(type);
        }
        return answer;
    }

    private JsonComplexType createJsonComplexType(String name, JsonNode value, String parentPath, Map<String, JsonNode> definitionMap) throws JsonInspectionException {
        logger.trace("--> Field:[name=[{}], value=[{}], parentPath=[{}]", name, value, parentPath);
        JsonComplexType answer = new JsonComplexType();
        answer.setJsonFields(new JsonFields());
        if (name != null) {
            answer.setName(name);
            answer.setPath((parentPath != null ? parentPath.concat("/") : "/").concat(name));
        }
        answer.setStatus(FieldStatus.SUPPORTED);
        
        populateDefinitions(value, definitionMap);
        value = resolveReference(value, definitionMap);

        JsonNode fieldType = value.get("type");
        if (fieldType == null || fieldType.asText() == null) {
            logger.warn("'type' is not defined for node '{}', assuming as an object", name);
            answer.setFieldType(FieldType.COMPLEX);
            answer.getJsonFields().getJsonField().addAll(loadProperties(value, answer.getPath(), definitionMap));
        } else if ("array".equals(fieldType.asText())) {
            JsonNode arrayItems = value.get("items");
            if (arrayItems == null || !arrayItems.fields().hasNext()) {
                logger.warn("'{}' is an array node, but no 'items' found in it. It will be ignored", name);
                answer.setCollectionType(CollectionType.LIST);
                answer.setStatus(FieldStatus.UNSUPPORTED);
            } else {
                answer = createJsonComplexType(name, value.get("items"), parentPath, definitionMap);
                answer.setCollectionType(CollectionType.LIST);
            }
        } else if ("boolean".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.BOOLEAN);
        } else if ("integer".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.INTEGER);
        } else if ("null".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.NONE);
        } else if ("number".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.NUMBER);
        } else if ("string".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.STRING);
        } else {
            if (!"object".equals(fieldType.asText())) {
                logger.warn("Unsupported field type '{}' found, assuming as an object", fieldType.asText());
            }
            answer.setFieldType(FieldType.COMPLEX);
            answer.getJsonFields().getJsonField().addAll(loadProperties(value, answer.getPath(), definitionMap));
        }

        return answer;
    }

    private JsonNode resolveReference(JsonNode node, Map<String, JsonNode> definitionMap) {
        if (node.get("$ref") == null) {
            return node;
        }
        String uri = node.get("$ref").asText();
        if (uri == null || uri.isEmpty()) {
            return node;
        }
        
        logger.trace("Resolving JSON schema reference '{}'", uri);
        // internal reference precedes even if it's full URL
        JsonNode def = definitionMap.get(uri);
        if (def != null) {
            return def;
        }
        
        // then try external resource
        try {
            JsonNode external = new ObjectMapper().readTree(new URI(uri).toURL().openStream());
            logger.trace("Successfully fetched external JSON schema '{}'    ", uri);
            return external;
        } catch (Exception e) {
            logger.debug("", e);
            logger.warn("The referenced schema '{}' is not found. Ignoring", node.get("$ref"));
            return node;
        }
    }

}
