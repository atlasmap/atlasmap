package io.atlasmap.json.inspect;

import io.atlasmap.json.v2.JsonDocument;

public class JsonDocumentInspectionService {

    public JsonDocument inspectJsonDocument(String sourceDocument) throws JsonInspectionException {
        if (sourceDocument == null || sourceDocument.isEmpty() || (sourceDocument.trim().length() == 0)) {
            throw new IllegalArgumentException("Source document cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(sourceDocument);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            InstanceInspector instanceInspector = new InstanceInspector();
            instanceInspector.inspect(cleanDocument);
            return instanceInspector.getJsonDocument();
        } else {
            throw new JsonInspectionException("JSON data must begin with either '{' or '['");
        }
    }

    public JsonDocument inspectJsonSchema(String jsonSchema) throws JsonInspectionException {
        if (jsonSchema == null || jsonSchema.isEmpty() || (jsonSchema.trim().length() == 0)) {
            throw new IllegalArgumentException("Schema cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(jsonSchema);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            SchemaInspector schemaInspector = new SchemaInspector();
            schemaInspector.inspect(cleanDocument);
            return schemaInspector.getJsonDocument();
        } else {
            throw new JsonInspectionException("JSON schema must begin with either '{' or '['");
        }
    }

    protected String cleanJsonDocument(String sourceDocument) {
        return sourceDocument.trim();
    }

}
