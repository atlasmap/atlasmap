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
package io.atlasmap.kafkaconnect.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Test;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.kafkaconnect.v2.AtlasKafkaConnectModelFactory;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

public class KafkaConnectFieldReaderTest {

    private static KafkaConnectFieldReader reader = new KafkaConnectFieldReader(DefaultAtlasConversionService.getInstance());

    @Test
    public void testWithNullDocument() throws Exception {
        reader.setDocument(null);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(AtlasKafkaConnectModelFactory.createKafkaConnectField());
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        assertEquals(1, audits.getAudit().size());
        assertEquals(AuditStatus.ERROR, audits.getAudit().get(0).getStatus());
    }

    @Test
    public void testReadPrimitive() throws Exception {
        reader.setDocument("foo");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        KafkaConnectField field = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        field.setPath("/");
        field.setFieldType(FieldType.STRING);
        when(session.head().getSourceField()).thenReturn(field);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        Field answer = reader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("foo", answer.getValue());
    }

    @Test
    public void testReadPrimitiveArray() throws Exception {
        reader.setDocument(Arrays.asList(new String[] {"foo", "bar", "val"}));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        KafkaConnectField field = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        field.setPath("/<>");
        field.setFieldType(FieldType.STRING);
        when(session.head().getSourceField()).thenReturn(field);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        Field answer = reader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertTrue(answer instanceof FieldGroup);
        FieldGroup group = (FieldGroup) answer;
        assertEquals("/<>", group.getPath());
        Field child = group.getField().get(0);
        assertEquals(FieldType.STRING, child.getFieldType());
        assertEquals("foo", child.getValue());
        assertEquals("/<0>", child.getPath());
        child = group.getField().get(1);
        assertEquals(FieldType.STRING, child.getFieldType());
        assertEquals("bar", child.getValue());
        assertEquals("/<1>", child.getPath());
        child = group.getField().get(2);
        assertEquals(FieldType.STRING, child.getFieldType());
        assertEquals("val", child.getValue());
        assertEquals("/<2>", child.getPath());
        field.setPath("/<1>");
        answer = reader.read(session);
        assertEquals("bar", answer.getValue());
    }

    @Test
    public void testReadRootComplex() throws Exception {
        reader.setDocument(createTestDoc(1).get(0));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        FieldGroup field = createRootField();
        when(session.head().getSourceField()).thenReturn(field);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup answer = (FieldGroup) reader.read(session);
        assertEquals(4, answer.getField().size());
        assertRoot(answer, "", 0);
        Field f0 = createF0Field(new AtlasPath("/"));
        when(session.head().getSourceField()).thenReturn(f0);
        assertf0(reader.read(session), "");
        Field fl0 = createFl0Field(new AtlasPath("/"));
        when(session.head().getSourceField()).thenReturn(fl0);
        assertfl0((FieldGroup)reader.read(session), "", 0);
        Field fc0 = createFc0Field(new AtlasPath("/"));
        when(session.head().getSourceField()).thenReturn(fc0);
        assertfc0((FieldGroup)reader.read(session), "");
        Field fcl0 = createFcl0Field(new AtlasPath("/"));
        when(session.head().getSourceField()).thenReturn(fcl0);
        assertfcl0((FieldGroup)reader.read(session), "", 0);
    }

    private void assertRoot(FieldGroup root, String rootPrefix, int rootIndex) throws Exception {
        assertf0(root.getField().get(0), rootPrefix);
        assertfl0((FieldGroup) root.getField().get(1), rootPrefix, rootIndex);
        assertfc0((FieldGroup) root.getField().get(2), rootPrefix);
        assertfcl0((FieldGroup) root.getField().get(3), rootPrefix, rootIndex);
    }

    private void assertf0(Field f0, String rootPrefix) {
        assertEquals(rootPrefix + "/f0", f0.getPath());
        assertEquals(FieldType.STRING, f0.getFieldType());
        assertEquals("foo", f0.getValue());
    }

    private void assertfl0(FieldGroup fl0, String rootPrefix, int rootIndex) {
        assertEquals(rootPrefix + "/fl0<>", fl0.getPath());
        assertEquals(FieldType.STRING, fl0.getFieldType());
        assertEquals(CollectionType.LIST, fl0.getCollectionType());
        assertfl0(fl0.getField(), rootPrefix, rootIndex);
    }

    private void assertfl0(List<Field> fields, String rootPrefix, int rootIndex) {
        assertEquals(3, fields.size());
        for (int i=0; i<3; i++) {
            Field fl0Item = fields.get(i);
            assertEquals(rootPrefix + "/fl0<" + i + ">", fl0Item.getPath());
            assertEquals(FieldType.STRING, fl0Item.getFieldType());
            assertEquals("fl0val" + rootIndex + "-" + i, fl0Item.getValue());
        }
    }

    private void assertfc0(FieldGroup fc0, String rootPrefix) {
        assertEquals(rootPrefix + "/fc0", fc0.getPath());
        assertEquals(FieldType.COMPLEX, fc0.getFieldType());
        assertEquals(1, fc0.getField().size());
        Field fc0f0 = fc0.getField().get(0);
        assertEquals(rootPrefix + "/fc0/f0", fc0f0.getPath());
        assertEquals(FieldType.STRING, fc0f0.getFieldType());
        assertEquals("bar", fc0f0.getValue());
    }

    private void assertfcl0(List<Field> fields, String rootPrefix, int rootIndex) {
        assertEquals(3, fields.size());
        for (int i=0; i<3; i++) {
            FieldGroup fcl0Item = (FieldGroup)fields.get(i);
            assertEquals(rootPrefix + "/fcl0<" + i + ">", fcl0Item.getPath());
            assertEquals(FieldType.COMPLEX, fcl0Item.getFieldType());
            assertEquals(1, fcl0Item.getField().size());
            Field fcl0Itemf0 = fcl0Item.getField().get(0);
            assertEquals(rootPrefix + "/fcl0<" + i + ">/f0", fcl0Itemf0.getPath());
            assertEquals(FieldType.STRING, fcl0Itemf0.getFieldType());
            assertEquals("fcl0f0val" + rootIndex + "-" + i, fcl0Itemf0.getValue());
        }
    }

    private void assertfcl0(FieldGroup fcl0, String rootPrefix, int rootIndex) {
        assertEquals(rootPrefix + "/fcl0<>", fcl0.getPath());
        assertEquals(FieldType.COMPLEX, fcl0.getFieldType());
        assertEquals(CollectionType.LIST, fcl0.getCollectionType());
        assertfcl0(fcl0.getField(), rootPrefix, rootIndex);
    }

    @Test
    public void testReadCollectionRootComplex() throws Exception {
        reader.setDocument(createTestDoc(3));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        FieldGroup field = createCollectionRootField();
        when(session.head().getSourceField()).thenReturn(field);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup answer = (FieldGroup) reader.read(session);
        assertEquals(3, answer.getField().size());
        for (int i=0; i<answer.getField().size(); i++) {
            assertRoot((FieldGroup)answer.getField().get(i), "/<" + i + ">", i);
        }
        Field f0 = createF0Field(new AtlasPath("/<>"));
        when(session.head().getSourceField()).thenReturn(f0);
        FieldGroup f0answer = (FieldGroup) reader.read(session);
        assertEquals(3, f0answer.getField().size());
        for (int i=0; i<f0answer.getField().size(); i++) {
            Field f = f0answer.getField().get(i);
            assertf0(f, "/<" + i + ">");
        }
        Field fl0 = createFl0Field(new AtlasPath("/<>"));
        when(session.head().getSourceField()).thenReturn(fl0);
        FieldGroup fl0answer = (FieldGroup) reader.read(session);
        assertEquals(9, fl0answer.getField().size());
        assertfl0(Arrays.asList(fl0answer.getField().get(0), fl0answer.getField().get(1), fl0answer.getField().get(2)), "/<0>", 0);
        assertfl0(Arrays.asList(fl0answer.getField().get(3), fl0answer.getField().get(4), fl0answer.getField().get(5)), "/<1>", 1);
        assertfl0(Arrays.asList(fl0answer.getField().get(6), fl0answer.getField().get(7), fl0answer.getField().get(8)), "/<2>", 2);
        Field fc0 = createFc0Field(new AtlasPath("/<>"));
        when(session.head().getSourceField()).thenReturn(fc0);
        FieldGroup fc0answer = (FieldGroup)reader.read(session);
        assertEquals(3, fc0answer.getField().size());
        for (int i=0; i<fc0answer.getField().size(); i++) {
            FieldGroup f = (FieldGroup) fc0answer.getField().get(i);
            assertfc0(f, "/<" + i + ">");
        }
        Field fcl0 = createFcl0Field(new AtlasPath("/<>"));
        when(session.head().getSourceField()).thenReturn(fcl0);
        FieldGroup fcl0answer = (FieldGroup) reader.read(session);
        assertEquals(9, fcl0answer.getField().size());
        assertfcl0(Arrays.asList(fcl0answer.getField().get(0), fcl0answer.getField().get(1), fcl0answer.getField().get(2)), "/<0>", 0);
        assertfcl0(Arrays.asList(fcl0answer.getField().get(3), fcl0answer.getField().get(4), fcl0answer.getField().get(5)), "/<1>", 1);
        assertfcl0(Arrays.asList(fcl0answer.getField().get(6), fcl0answer.getField().get(7), fcl0answer.getField().get(8)), "/<2>", 2);
    }

    @Test
    public void testReadIndexedCollectionRootComplex() throws Exception {
        reader.setDocument(createTestDoc(3));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        FieldGroup field = createIndexedCollectionRootField(1);
        when(session.head().getSourceField()).thenReturn(field);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup answer = (FieldGroup) reader.read(session);
        assertEquals(4, answer.getField().size());
        assertRoot(answer, "/<" + 1 + ">", 1);
        Field f0 = createF0Field(new AtlasPath("/<1>"));
        when(session.head().getSourceField()).thenReturn(f0);
        assertf0(reader.read(session), "/<1>");
        Field fl0 = createFl0Field(new AtlasPath("/<1>"));
        when(session.head().getSourceField()).thenReturn(fl0);
        assertfl0((FieldGroup)reader.read(session), "/<1>", 1);
        Field fc0 = createFc0Field(new AtlasPath("/<1>"));
        when(session.head().getSourceField()).thenReturn(fc0);
        assertfc0((FieldGroup)reader.read(session), "/<1>");
        Field fcl0 = createFcl0Field(new AtlasPath("/<1>"));
        when(session.head().getSourceField()).thenReturn(fcl0);
        assertfcl0((FieldGroup)reader.read(session), "/<1>", 1);
    }

    private List<Struct> createTestDoc(int size) {
        Schema f0Schema = SchemaBuilder.struct()
            .field("f0", Schema.STRING_SCHEMA)
            .build();
        Schema docSchema = SchemaBuilder.struct()
            .field("f0", Schema.STRING_SCHEMA)
            .field("fl0", SchemaBuilder.array(Schema.STRING_SCHEMA).build())
            .field("fc0", f0Schema)
            .field("fcl0", SchemaBuilder.array(f0Schema).build())
            .build();
        List<Struct> answer = new LinkedList<>();
        for (int j = 0; j < size; j++) {
            Struct doc = new Struct(docSchema);
            doc.put("f0", "foo");
            List<Object> fl0 = new LinkedList<>();
            for (int i = 0; i < 3; i++) {
                fl0.add("fl0val" + j + "-" + i);
            }
            doc.put("fl0", fl0);
            Struct fc0 = new Struct(f0Schema);
            fc0.put("f0", "bar");
            doc.put("fc0", fc0);
            List<Object> fcl0 = new LinkedList<>();
            for (int i = 0; i < 3; i++) {
                Struct fcl0s = new Struct(f0Schema);
                fcl0s.put("f0", "fcl0f0val" + j + "-" + i);
                fcl0.add(fcl0s);
            }
            doc.put("fcl0", fcl0);
            answer.add(doc);
    }
        return answer;
    }

    private FieldGroup doCreateRootField(String rootPath) {
        AtlasPath path = new AtlasPath(rootPath);
        FieldGroup field = new FieldGroup();
        field.setPath(path.toString());
        field.setFieldType(FieldType.COMPLEX);
        if (rootPath.contains("<")) {
            field.setCollectionType(CollectionType.LIST);
        }
        field.getField().add(createF0Field(path));
        field.getField().add(createFl0Field(path));
        field.getField().add(createFc0Field(path));
        field.getField().add(createFcl0Field(path));
        return field;
    }

    private FieldGroup createRootField() {
        return doCreateRootField("/");
    }

    private FieldGroup createCollectionRootField() {
        return doCreateRootField("/<>");
    }

    private FieldGroup createIndexedCollectionRootField(int index) {
        return doCreateRootField("/<" + index + ">");
    }

    private KafkaConnectField createF0Field(AtlasPath parentPath) {
        KafkaConnectField f0Field = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        f0Field.setPath(parentPath.clone().appendField("f0").toString());
        f0Field.setFieldType(FieldType.STRING);
        return f0Field;
    }

    private KafkaConnectField createFl0Field(AtlasPath parentPath) {
        KafkaConnectField fl0Field = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        fl0Field.setPath(parentPath.clone().appendField("fl0<>").toString());
        fl0Field.setFieldType(FieldType.STRING);
        fl0Field.setCollectionType(CollectionType.LIST);
        return fl0Field;
    }

    private FieldGroup createFc0Field(AtlasPath parentPath) {
        FieldGroup fc0Field = new FieldGroup();
        AtlasPath path = parentPath.clone().appendField("fc0");
        fc0Field.setPath(path.toString());
        fc0Field.setFieldType(FieldType.COMPLEX);
        fc0Field.getField().add(createF0Field(path));
        return fc0Field;
    }

    private FieldGroup createFcl0Field(AtlasPath parentPath) {
        FieldGroup fcl0Field = new FieldGroup();
        AtlasPath path = parentPath.clone().appendField("fcl0<>");
        fcl0Field.setPath(path.toString());
        fcl0Field.setFieldType(FieldType.COMPLEX);
        fcl0Field.setCollectionType(CollectionType.LIST);
        fcl0Field.getField().add(createF0Field(path));
        return fcl0Field;
    }

}
