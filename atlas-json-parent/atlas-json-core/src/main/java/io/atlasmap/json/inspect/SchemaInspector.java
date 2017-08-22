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
public class SchemaInspector {

    private JsonDocument jsonDocument = AtlasJsonModelFactory.createJsonDocument();
    private static final Logger logger = LoggerFactory.getLogger(SchemaInspector.class);

    public SchemaInspector() {
    }

    public JsonDocument getJsonDocument() {
        return jsonDocument;
    }

    public void inspect(String schema) throws JsonInspectionException {
        if (schema == null || schema.isEmpty()) {
            throw new IllegalArgumentException("JSON schema cannot be null");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(schema);
            
            JsonNode header = rootNode.get("$schema");
            if (header == null || !header.asText().startsWith("http://json-schema.org/")) {
                throw new JsonInspectionException(String.format("The $schema property not found or invalid: '%s'", header.asText()));
            }
            JsonNode type = rootNode.get("type");
            if (type == null || !type.asText().equals("object")) {
                throw new JsonInspectionException(String.format("The property type '%s' is not supported for root node", type));
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
        } catch (Exception e) {
            throw new JsonInspectionException(e);
        }
    }

    private JsonComplexType createJsonComplexType(String name, JsonNode value, JsonComplexType parent) throws JsonInspectionException {
        if (value.get("$ref") != null) {
            logger.warn("'$ref' is not yet supported for JSON Schema, will be ignored: '{}", value);
        }
        JsonNode fieldType = value.get("type");
        if (fieldType == null || fieldType.asText() == null) {
            throw new JsonInspectionException(String.format("The type is not defined for node '%s'", name));
        }
        
        JsonComplexType answer = new JsonComplexType();
        answer.setJsonFields(new JsonFields());
        answer.setName(name);
        answer.setStatus(FieldStatus.SUPPORTED);
        answer.setPath((parent != null ? parent.getPath() : "").concat("/").concat(name));
        
        switch (fieldType.asText()) {
        case "array":
            answer = createJsonComplexType(name, value.get("items"), parent);
            answer.setCollectionType(CollectionType.ARRAY);
            break;
        case "object":
            Iterator<Entry<String, JsonNode>> subFields = value.get("properties").fields();
            while (subFields.hasNext()) {
                Entry<String, JsonNode> entry = subFields.next();
                if (!entry.getValue().isObject()) {
                    logger.warn("Ignoring non-object field '{}'", entry);
                    continue;
                }
                logger.trace("--> Adding a field '{}' on a node '{}' with value '{}'", entry.getKey(), answer.getName(), entry.getValue());
                createJsonComplexType(entry.getKey(), entry.getValue(), answer);
            }
            answer.setFieldType(FieldType.COMPLEX);
            break;
        case "boolean":
            answer.setFieldType(FieldType.BOOLEAN);
            break;
        case "integer":
            answer.setFieldType(FieldType.INTEGER);
            break;
        case "null":
            answer.setFieldType(FieldType.NONE);
            break;
        case "number":
            answer.setFieldType(FieldType.NUMBER);
            break;
        case "string":
            answer.setFieldType(FieldType.STRING);
            break;
        default:
            throw new JsonInspectionException(String.format("Unsupported field type '%s'", fieldType.asText()));
        }
        
        if (parent != null) {
            parent.getJsonFields().getJsonField().add(answer);
        }
        return answer;
    }

}
