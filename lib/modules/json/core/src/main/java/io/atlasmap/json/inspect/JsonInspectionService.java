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

import io.atlasmap.json.v2.JsonDocument;

public class JsonInspectionService {

    public JsonDocument inspectJsonDocument(String sourceDocument) throws JsonInspectionException {
        if (sourceDocument == null || sourceDocument.isEmpty() || (sourceDocument.trim().length() == 0)) {
            throw new IllegalArgumentException("Source document cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(sourceDocument);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            return JsonInstanceInspector.instance().inspect(cleanDocument);
        }
        throw new JsonInspectionException("JSON data must begin with either '{' or '['");
    }

    public JsonDocument inspectJsonSchema(String jsonSchema) throws JsonInspectionException {
        if (jsonSchema == null || jsonSchema.isEmpty() || (jsonSchema.trim().length() == 0)) {
            throw new IllegalArgumentException("Schema cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(jsonSchema);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            return JsonSchemaInspector.instance().inspect(cleanDocument);
        }
        throw new JsonInspectionException("JSON schema must begin with either '{' or '['");
    }

    protected String cleanJsonDocument(String sourceDocument) {
        return sourceDocument.trim();
    }

}
