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

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.kafkaconnect.v2.KafkaConnectConstants;
import io.atlasmap.kafkaconnect.v2.KafkaConnectDocument;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionRequest;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionResponse;
import io.atlasmap.kafkaconnect.v2.KafkaConnectSchemaType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Json;

public class KafkaConnectServiceTest {

    private KafkaConnectService kafkaConnectService = null;

    @BeforeEach
    public void setUp() {
        kafkaConnectService = new KafkaConnectService();
    }

    @AfterEach
    public void tearDown() {
        kafkaConnectService = null;
    }

    @Test
    public void testAvroSchema() throws Exception {
        KafkaConnectInspectionRequest request = new KafkaConnectInspectionRequest();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("avro-complex.json");
        request.setSchemaData(new String(is.readAllBytes()));
        request.getOptions().put(KafkaConnectConstants.OPTIONS_SCHEMA_TYPE, "AVRO");
        request.getOptions().put(KafkaConnectConstants.OPTIONS_IS_KEY, "true");
        Response res = kafkaConnectService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        KafkaConnectInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, KafkaConnectInspectionResponse.class);
        assertNull(inspectionResponse.getErrorMessage());
        KafkaConnectDocument doc = inspectionResponse.getKafkaConnectDocument();
        assertNotNull(doc);
        assertEquals(org.apache.kafka.connect.data.Schema.Type.STRUCT, doc.getRootSchemaType());
    }


}
