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
package io.atlasmap.java.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.Json;

@ExtendWith(MockitoExtension.class)
public class JavaServiceTest {

    private AtlasService atlasService;
    private JavaService javaService;
    private DocumentService documentService;

    @BeforeEach
    public void before() throws AtlasException {
        this.atlasService = new AtlasService();
        this.documentService = new DocumentService(atlasService);
        this.javaService = new JavaService(atlasService, documentService);
    }

    @Test
    public void testImportJavaDocument() throws Exception {
        ClassInspectionRequest request = new ClassInspectionRequest();
        request.setDocumentId("test");
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setClassName(ClassInspectionRequest.class.getName());
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = javaService.importJavaDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        ClassInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, ClassInspectionResponse.class);
        String error = inspectionResponse.getErrorMessage();
        assertTrue(error == null || error.isEmpty(), error);
        assertEquals(ClassInspectionRequest.class.getName(), inspectionResponse.getJavaClass().getClassName());
        request.setClassName(AtlasSession.class.getName());
        bytes = Json.mapper().writeValueAsBytes(request);
        res = javaService.importJavaDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        inspectionResponse = Json.mapper().readValue((byte[]) entity, ClassInspectionResponse.class);
        error = inspectionResponse.getErrorMessage();
        assertTrue(error == null || error.isEmpty(), error);
        assertEquals(AtlasSession.class.getName(), inspectionResponse.getJavaClass().getClassName());
        inspectionResponse.getJavaClass().getJavaFields().getJavaField()
                .stream().filter(f -> "properties".equals(f.getName()))
                .forEach(f -> assertEquals("/properties", f.getPath(), "Invalid path: " + f.getPath()));
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        JavaClass inspected = Json.mapper().readValue((File)res.getEntity(), JavaClass.class);
        assertEquals(AtlasSession.class.getName(), inspected.getClassName());
        AtlasMappingHandler handler = atlasService.getADMArchiveHandler(0).getAtlasMappingHandler();
        DataSource ds = handler.getDataSource(new DocumentKey(DataSourceType.SOURCE, "test"));
        assertEquals(AtlasSession.class.getName(), AtlasUtil.getUriParameters(ds.getUri()).get("className"));
        assertEquals("java", AtlasUtil.getUriModule(ds.getUri()));
        assertEquals("test", AtlasUtil.getUriDataType(ds.getUri()));
    }
}
