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
package io.atlasmap.kafkaconnect.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.storage.StringConverterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.kafkaconnect.v2.KafkaConnectComplexType;
import io.atlasmap.kafkaconnect.v2.KafkaConnectDocument;
import io.atlasmap.kafkaconnect.v2.KafkaConnectEnumField;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

public class KafkaConnectInspectionServiceTest {

    private KafkaConnectInspectionService service = new KafkaConnectInspectionService(getClass().getClassLoader());
    private HashMap<String, Object> options = new HashMap<>();

    @BeforeEach
    public void before() {
        options.put(JsonConverterConfig.SCHEMAS_CACHE_SIZE_CONFIG, 0);
        options.put(StringConverterConfig.TYPE_CONFIG, "key");
    }

    @Test
    public void testJsonPrimitive() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-primitive.json");
        KafkaConnectDocument doc = service.inspectJson(new String(is.readAllBytes()), options);
        assertNotNull(doc);
        assertEquals("primitive", doc.getName());
        assertEquals("/", doc.getPath());
        assertEquals(FieldType.STRING, doc.getFieldType());
    }

    @Test
    public void testJsonComplex() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("json-complex.json");
        KafkaConnectDocument doc = service.inspectJson(new String(is.readAllBytes()), options);
        assertNotNull(doc);
        assertEquals("struct", doc.getName());
        assertEquals("/", doc.getPath());
        assertEquals(FieldType.COMPLEX, doc.getFieldType());
        List<Field> fields = doc.getFields().getField();
        assertEquals(2, fields.size());
        Field field1 = fields.get(0);
        assertEquals("field1", field1.getName());
        assertEquals("/field1", field1.getPath());
        assertEquals(FieldType.BOOLEAN, field1.getFieldType());
        Field field2 = fields.get(1);
        assertEquals("field2", field2.getName());
        assertEquals("/field2", field2.getPath());
        assertEquals(FieldType.STRING, field2.getFieldType());

    }

    @Test
    public void testAvroPrimitive() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("avro-primitive.json");
        KafkaConnectDocument doc = service.inspectAvro(new String(is.readAllBytes()), options);
        assertNotNull(doc);
        assertNull(doc.getName());
        assertEquals("/", doc.getPath());
        assertEquals(FieldType.STRING, doc.getFieldType());
    }

    @Test
    public void testAvroComplex() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("avro-complex.json");
        KafkaConnectDocument doc = service.inspectAvro(new String(is.readAllBytes()), options);
        assertNotNull(doc);
        assertEquals("root", doc.getName());
        assertEquals("/", doc.getPath());
        assertEquals(FieldType.COMPLEX, doc.getFieldType());
        List<Field> fields = doc.getFields().getField();
        assertEquals(9, fields.size());
        Field f1 = fields.get(0);
        assertEquals("f1", f1.getName());
        assertEquals("/f1", f1.getPath());
        assertEquals(FieldType.BOOLEAN, f1.getFieldType());
        Field f2 = fields.get(1);
        assertEquals("f2", f2.getName());
        assertEquals("/f2", f2.getPath());
        assertEquals(FieldType.STRING, f2.getFieldType());

        Field record = fields.get(2);
        assertEquals("record", record.getName());
        assertEquals("/record", record.getPath());
        assertEquals(FieldType.COMPLEX, record.getFieldType());
        assertTrue(record instanceof KafkaConnectComplexType);
        KafkaConnectComplexType recordComplex = (KafkaConnectComplexType)record;
        assertEquals(2, recordComplex.getKafkaConnectFields().getKafkaConnectField().size());
        Field recordf1 = recordComplex.getKafkaConnectFields().getKafkaConnectField().get(0);
        assertEquals("recordf1", recordf1.getName());
        assertEquals("/record/recordf1", recordf1.getPath());
        assertEquals(FieldType.LONG, recordf1.getFieldType());
        Field recordf2 = recordComplex.getKafkaConnectFields().getKafkaConnectField().get(1);
        assertEquals("recordf2", recordf2.getName());
        assertEquals("/record/recordf2", recordf2.getPath());
        assertEquals(FieldType.DOUBLE, recordf2.getFieldType());

        Field enumf = fields.get(3);
        assertEquals("enum", enumf.getName());
        assertEquals("/enum", enumf.getPath());
        assertEquals(FieldType.COMPLEX, enumf.getFieldType());
        assertTrue(record instanceof KafkaConnectComplexType);
        KafkaConnectComplexType enumfComplex = (KafkaConnectComplexType)enumf;
        List<KafkaConnectEnumField> entries = enumfComplex.getKafkaConnectEnumFields().getKafkaConnectEnumField();
        assertEquals(3, entries.size());
        assertEquals("ONE", entries.get(0).getName());
        assertEquals("TWO", entries.get(1).getName());
        assertEquals("THREE", entries.get(2).getName());

        Field sarray = fields.get(4);
        assertEquals("sarray", sarray.getName());
        assertEquals("/sarray<>", sarray.getPath());
        assertEquals(CollectionType.LIST, sarray.getCollectionType());
        assertEquals(FieldType.STRING, sarray.getFieldType());

        Field rarray = fields.get(5);
        assertEquals("rarray", rarray.getName());
        assertEquals("/rarray<>", rarray.getPath());
        assertEquals(CollectionType.LIST, rarray.getCollectionType());
        assertEquals(FieldType.COMPLEX, rarray.getFieldType());
        assertTrue(rarray instanceof KafkaConnectComplexType);
        KafkaConnectComplexType rarrayComplex = (KafkaConnectComplexType)rarray;
        List<KafkaConnectField> rarrayEntries = rarrayComplex.getKafkaConnectFields().getKafkaConnectField();
        assertEquals(2, rarrayEntries.size());
        Field rarrayf3= rarrayEntries.get(0);
        assertEquals("recordf3", rarrayf3.getName());
        assertEquals("/rarray<>/recordf3", rarrayf3.getPath());
        assertEquals(FieldType.INTEGER, rarrayf3.getFieldType());
        Field rarrayf4 = rarrayEntries.get(1);
        assertEquals("recordf4", rarrayf4.getName());
        assertEquals("/rarray<>/recordf4", rarrayf4.getPath());
        assertEquals(FieldType.FLOAT, rarrayf4.getFieldType());

        Field map = fields.get(6);
        assertEquals("map", map.getName());
        assertEquals("/map{}", map.getPath());
        assertEquals(CollectionType.MAP, map.getCollectionType());
        assertEquals(FieldType.STRING, map.getFieldType());

        Field union = fields.get(7);
        assertEquals("union", union.getName());
        assertEquals("/union", union.getPath());
        assertEquals(FieldType.COMPLEX, union.getFieldType());
        assertTrue(union instanceof KafkaConnectComplexType);
        KafkaConnectComplexType unionComplex = (KafkaConnectComplexType)union;
        assertEquals(FieldStatus.UNSUPPORTED, unionComplex.getStatus());

        Field fixed = fields.get(8);
        assertEquals("fixed", fixed.getName());
        assertEquals("/fixed", fixed.getPath());
        assertEquals(FieldType.BYTE_ARRAY, fixed.getFieldType());

   }

    @Test
    public void testAvroTopmostArray() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("avro-topmost-array.json");
        KafkaConnectDocument doc = service.inspectAvro(new String(is.readAllBytes()), options);
        assertNotNull(doc);
        assertNull(doc.getName());
        assertEquals("/<>", doc.getPath());
        assertEquals(CollectionType.LIST, doc.getCollectionType());
        assertEquals(FieldType.STRING, doc.getFieldType());
    }
}