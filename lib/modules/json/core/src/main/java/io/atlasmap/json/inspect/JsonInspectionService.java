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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import io.atlasmap.json.v2.JsonDocument;

/**
 * The JSON inspection service.
 */
public class JsonInspectionService {

    /**
     * Inspects the JSON instance.
     * @param sourceDocument JSON instance
     * @return inspected
     * @throws JsonInspectionException invalid JSON data
     */
    public JsonDocument inspectJsonDocument(String sourceDocument) throws JsonInspectionException {
        if (sourceDocument == null || sourceDocument.isEmpty() || (sourceDocument.trim().length() == 0)) {
            throw new IllegalArgumentException("Source document cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(sourceDocument);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            return inspectJsonDocument(new ByteArrayInputStream(cleanDocument.getBytes()));
        }
        throw new JsonInspectionException("JSON data must begin with either '{' or '['");
    }

    /**
     * Inspects the JSON instance.
     * @param sourceDocument JSON instance InputStream
     * @return inspected
     * @throws JsonInspectionException invalid JSON data
     */
    public JsonDocument inspectJsonDocument(InputStream sourceDocument) throws JsonInspectionException {
        return JsonInstanceInspector.instance().inspect(sourceDocument);
    }

    /**
     * Inspects the JSON schema.
     * @param jsonSchema JSON schema
     * @return inspected
     * @throws JsonInspectionException invalid JSON schema
     */
    public JsonDocument inspectJsonSchema(String jsonSchema) throws JsonInspectionException {
        if (jsonSchema == null || jsonSchema.isEmpty() || (jsonSchema.trim().length() == 0)) {
            throw new IllegalArgumentException("Schema cannot be null, empty or contain only whitespace.");
        }
        String cleanDocument = cleanJsonDocument(jsonSchema);

        if (cleanDocument.startsWith("{") || cleanDocument.startsWith("[")) {
            return inspectJsonSchema(new ByteArrayInputStream(cleanDocument.getBytes()));
        }
        throw new JsonInspectionException("JSON schema must begin with either '{' or '['");
    }

    public JsonDocument inspectJsonSchema(InputStream sourceDocument) throws JsonInspectionException {
        return JsonSchemaInspector.instance().inspect(sourceDocument);
    }

    /**
     * Trims the whitespaces at left end and right end.
     * @param sourceDocument string
     * @return trimmed
     */
    protected String cleanJsonDocument(String sourceDocument) {
        return sourceDocument.trim();
    }

}
