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
package io.atlasmap.kafkaconnect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasMappingHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.kafkaconnect.v2.KafkaConnectConstants;
import io.atlasmap.kafkaconnect.v2.KafkaConnectDocument;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionRequest;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.Json;

public class KafkaConnectServiceTest {

    private AtlasService atlasService;
    private KafkaConnectService kafkaConnectService = null;
    private DocumentService documentService;

    @BeforeEach
    public void setUp() throws AtlasException {
        atlasService = new AtlasService();
        documentService = new DocumentService(atlasService);
        kafkaConnectService = new KafkaConnectService(atlasService, documentService);
    }

    @AfterEach
    public void tearDown() {
        kafkaConnectService = null;
    }

    @Test
    public void testAvroSchema() throws Exception {
        KafkaConnectInspectionRequest request = new KafkaConnectInspectionRequest();
        request.setDocumentId("test");
        request.setDataSourceType(DataSourceType.SOURCE);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("avro-complex.json");
        request.setSchemaData(new String(is.readAllBytes()));
        request.getOptions().put(KafkaConnectConstants.OPTIONS_SCHEMA_TYPE, "AVRO");
        request.getOptions().put(KafkaConnectConstants.OPTIONS_IS_KEY, "true");
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = kafkaConnectService.importKafkaConnectDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        KafkaConnectInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, KafkaConnectInspectionResponse.class);
        assertNull(inspectionResponse.getErrorMessage());
        KafkaConnectDocument doc = inspectionResponse.getKafkaConnectDocument();
        assertNotNull(doc);
        assertEquals(org.apache.kafka.connect.data.Schema.Type.STRUCT, doc.getRootSchemaType());
        assertEquals(9, doc.getFields().getField().size());
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        KafkaConnectDocument inspected = Json.mapper().readValue((File)res.getEntity(), KafkaConnectDocument.class);
        assertEquals(9, inspected.getFields().getField().size());
        AtlasMappingHandler handler = atlasService.getADMArchiveHandler(0).getAtlasMappingHandler();
        DataSource ds = (DataSource) handler.getDataSource(new DocumentKey(DataSourceType.SOURCE, "test"));
        assertEquals("kafkaconnect", AtlasUtil.getUriModule(ds.getUri()));
        assertEquals("test", AtlasUtil.getUriDataType(ds.getUri()));
    }


}
