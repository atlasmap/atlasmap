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
package io.atlasmap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentCatalog;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Json;

public class DocumentServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceTest.class);
    private static final String TEMP_DIR = "target/DocumentServiceTest";

    private AtlasService atlasService;
    private DocumentService documentService;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() throws Exception {
        File workspaceFolderWithSpace = new File(TEMP_DIR);
        System.setProperty(AtlasService.ATLASMAP_WORKSPACE, workspaceFolderWithSpace.getAbsolutePath());
        this.atlasService = new AtlasService();
        this.documentService = new DocumentService(atlasService);
        mapper = Json.mapper();
    }

    @AfterEach
    public void tearDown() {
        mapper = null;
    }

    @Test
    public void testGetDocumentCatalogRequest() throws Exception {
        atlasService.importADMArchiveRequest(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"),
            0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project"));
        Response res = documentService.getDocumentCatalogRequest(0);
        assertEquals(200, res.getStatus());
        DocumentCatalog catalog = mapper.readValue((byte[])res.getEntity(), DocumentCatalog.class);
        assertEquals(5, catalog.getSources().size());
        assertEquals(5, catalog.getTargets().size());
    }

    @Test
    public void testGetDocumentInspectionResultRequest() throws Exception {
        documentService.registerModuleService(DocumentType.JSON, new DummyJsonService(atlasService, documentService));
        atlasService.importADMArchiveRequest(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"),
            0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project"));
        Response res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "JSONInstanceSource");
        assertEquals(200, res.getStatus());
        JsonNode inspected = new ObjectMapper().readTree((File)res.getEntity());
        assertEquals("dummy", inspected.get("String").asText());
    }

    @Test
    public void testSetDocumentNameRequest() throws Exception {
        atlasService.importADMArchiveRequest(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"),
            0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project"));
        Response res = documentService.getDocumentCatalogRequest(0);
        assertEquals(200, res.getStatus());
        DocumentCatalog catalog = mapper.readValue((byte[])res.getEntity(), DocumentCatalog.class);
        Optional<DocumentMetadata> opt = catalog.getSources().stream().filter(meta -> meta.getId().equals("JSONInstanceSource")).findAny();
        assertEquals("JSONInstanceSource", opt.get().getName());
        res = documentService.setDocumentNameRequest(0, DataSourceType.SOURCE, "JSONInstanceSource", "JSONInstanceSource modified");
        assertEquals(200, res.getStatus());
        res = documentService.getDocumentCatalogRequest(0);
        assertEquals(200, res.getStatus());
        catalog = mapper.readValue((byte[])res.getEntity(), DocumentCatalog.class);
        opt = catalog.getSources().stream().filter(meta -> meta.getId().equals("JSONInstanceSource")).findAny();
        assertEquals("JSONInstanceSource modified", opt.get().getName());
    }

    @Test
    public void testDeleteDocumentRequest() throws Exception {
        atlasService.importADMArchiveRequest(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"),
            0,
            Util.generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/project"));
            Response res = documentService.getDocumentCatalogRequest(0);
            assertEquals(200, res.getStatus());
            DocumentCatalog catalog = mapper.readValue((byte[])res.getEntity(), DocumentCatalog.class);
            Optional<DocumentMetadata> opt = catalog.getSources().stream().filter(meta -> meta.getId().equals("JSONInstanceSource")).findAny();
            assertTrue(opt.isPresent());
            documentService.deleteDocumentRequest(0, DataSourceType.SOURCE, "JSONInstanceSource");
            res = documentService.getDocumentCatalogRequest(0);
            assertEquals(200, res.getStatus());
            catalog = mapper.readValue((byte[])res.getEntity(), DocumentCatalog.class);
            opt = catalog.getSources().stream().filter(meta -> meta.getId().equals("JSONInstanceSource")).findAny();
            assertFalse(opt.isPresent());
    }

    class DummyJsonService extends ModuleService {
        public DummyJsonService(AtlasService atlasService, DocumentService documentService) {
            super(atlasService, documentService);
        }

        @Override
        public Field getField(String path, boolean recursive) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Field> searchFields(String keywords) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec)
                throws AtlasException {
            storeInspectionResult(mappingDefinitionId, meta.getDataSourceType(), meta.getId(), "dummy");
            
        }

        @Override
        protected Logger getLogger() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
