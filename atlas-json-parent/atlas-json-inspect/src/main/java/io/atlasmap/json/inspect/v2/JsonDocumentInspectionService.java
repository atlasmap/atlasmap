package io.atlasmap.json.inspect.v2;

import io.atlasmap.json.v2.JsonDocument;

public class JsonDocumentInspectionService {


    public JsonDocument inspectJsonDocument(String sourceDocument) throws JsonInspectionException {
        if (sourceDocument == null || sourceDocument.isEmpty() || (sourceDocument.trim().length() == 0)) {
            throw new IllegalArgumentException("Source document cannot be null, empty or contain only whitespace.");
        }

        if (sourceDocument.startsWith("{") || sourceDocument.startsWith("[")) {
            InstanceInspector instanceInspector = new InstanceInspector();
            instanceInspector.inspect(sourceDocument);
            return instanceInspector.getJsonDocument();
        } else {
            throw new JsonInspectionException("JSON '" + sourceDocument + "'is invalid because it does not start with either '{' or '['");
        }
    }
}
