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
package io.atlasmap.json.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.InspectionType;
import io.atlasmap.v2.Json;

public class JsonServiceTest {

    private JsonService jsonService = null;
    private DocumentService documentService;

    @BeforeEach
    public void setUp() throws AtlasException {
        AtlasService atlas = new AtlasService();
        documentService = new DocumentService(atlas);
        jsonService = new JsonService(atlas, documentService);
    }

    @AfterEach
    public void tearDown() {
        jsonService = null;
    }

    @Test
    public void testValidJsonData() {
        assertTrue(jsonService.validJsonData("{ \"foo\":\"bar\" }"));
        assertTrue(jsonService.validJsonData("[ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));

        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("  { \"foo\":\"bar\" }")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("{ \"foo\":\"bar\" }   ")));
        assertTrue(jsonService
                .validJsonData(jsonService.cleanJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("\b\t\n\f\r   { \"foo\":\"bar\" }")));
    }

    @Test
    public void testImportJsonDocument() throws Exception {
        JsonInspectionRequest request = new JsonInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setDocumentType(DocumentType.JSON);
        request.setDocumentId("test");
        request.setJsonData(new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/BaseContact.jsd").readAllBytes()));
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = jsonService.importJsonDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        JsonInspectionResponse jir = Json.mapper().readValue((byte[])res.getEntity(), JsonInspectionResponse.class);
        assertEquals(4, jir.getJsonDocument().getFields().getField().size());
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        JsonDocument inspected = Json.mapper().readValue((File)res.getEntity(), JsonDocument.class);
        assertEquals(4, inspected.getFields().getField().size());
    }
}
