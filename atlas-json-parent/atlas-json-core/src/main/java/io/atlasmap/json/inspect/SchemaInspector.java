package io.atlasmap.json.inspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
            
            JsonNode header = rootNode.get("$schema");
            if (header == null || !header.asText().startsWith("http://json-schema.org/")) {
                throw new JsonInspectionException(String.format("The $schema property not found or invalid: '%s'", header.asText()));
            }
            JsonNode type = rootNode.get("type");
            if (type == null || !"object".equals(type.asText())) {
                throw new JsonInspectionException(String.format("The property type '%s' is not supported for root node", type));
            }

            JsonNode properties = rootNode.get("properties");
            if (properties == null || !properties.fields().hasNext()) {
                logger.warn("No properties could be found for the JSON schema: '{}': JsonDocument will be empty", schema);
                return jsonDocument;
            }

            Iterator<Entry<String, JsonNode>> topFields = rootNode.get("properties").fields();
            while (topFields.hasNext()) {
                Entry<String, JsonNode> entry = topFields.next();
                if (!entry.getValue().isObject()) {
                    logger.warn("Ignoring non-object field '{}'", entry);
                    continue;
                }
                logger.trace("--> Adding a field '{}' on a root node with value '{}'", entry.getKey(), entry.getValue());
                JsonComplexType target = createJsonComplexType(entry.getKey(), entry.getValue(), null);
                jsonDocument.getFields().getField().add(target);
            }
            return jsonDocument;
        } catch (Exception e) {
            throw new JsonInspectionException(e);
        }
    }

    private JsonComplexType createJsonComplexType(String name, JsonNode value, JsonComplexType parent) throws JsonInspectionException {
        JsonComplexType answer = new JsonComplexType();
        answer.setJsonFields(new JsonFields());
        answer.setName(name);
        answer.setPath((parent != null ? parent.getPath() : "").concat("/").concat(name));
        answer.setStatus(FieldStatus.SUPPORTED);
        
        JsonNode fieldType = value.get("type");
        if (value.get("$ref") != null) {
            logger.warn("'$ref' is not yet supported for JSON Schema, node '{}' will be ignored: '{}", name);
            answer.setStatus(FieldStatus.UNSUPPORTED);
        } else if (fieldType == null || fieldType.asText() == null) {
            logger.warn("'type' is not defined for node '{}': this node will be ignored", name);
            answer.setStatus(FieldStatus.UNSUPPORTED);
        } else if ("array".equals(fieldType.asText())) {
            JsonNode arrayItems = value.get("items");
            if (arrayItems == null || !arrayItems.fields().hasNext()) {
                logger.warn("'{}' is an array node, but no 'items' found in it. It will be ignored");
                answer.setCollectionType(CollectionType.ARRAY);
                answer.setStatus(FieldStatus.UNSUPPORTED);
            } else {
                answer = createJsonComplexType(name, value.get("items"), parent);
                answer.setCollectionType(CollectionType.ARRAY);
            }
        } else if ("object".equals(fieldType.asText())) {
            answer.setFieldType(FieldType.COMPLEX);
            JsonNode properties = value.get("properties");
            if (properties == null || !properties.fields().hasNext()) {
                logger.warn("'{}' is an object node, but no 'properties' found in it. It will be ignored");
                answer.setStatus(FieldStatus.UNSUPPORTED);
            } else {
                Iterator<Entry<String, JsonNode>> subFields = properties.fields();
                while (subFields.hasNext()) {
                    Entry<String, JsonNode> entry = subFields.next();
                    if (!entry.getValue().isObject()) {
                        logger.warn("Ignoring non-object field '{}'", entry);
                        continue;
                    }
                    logger.trace("--> Adding a field '{}' on a node '{}' with value '{}'", entry.getKey(), answer.getName(), entry.getValue());
                    createJsonComplexType(entry.getKey(), entry.getValue(), answer);
                }
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
            logger.warn("Unsupported field type '{}' found, will be ignored", fieldType.asText());
            answer.setStatus(FieldStatus.UNSUPPORTED);
        }
        
        if (parent != null) {
            parent.getJsonFields().getJsonField().add(answer);
        }
        return answer;
    }

}
