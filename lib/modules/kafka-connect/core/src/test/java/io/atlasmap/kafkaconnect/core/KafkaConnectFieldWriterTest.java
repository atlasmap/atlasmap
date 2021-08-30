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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.kafkaconnect.v2.AtlasKafkaConnectModelFactory;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Field;

public class KafkaConnectFieldWriterTest {
    private static KafkaConnectFieldWriter writer;

    private static Schema rootSchema;
    static {
        Schema f0Schema = SchemaBuilder.struct()
            .field("f0", Schema.STRING_SCHEMA)
            .build();
        rootSchema = SchemaBuilder.struct()
            .field("f0", Schema.STRING_SCHEMA)
            .field("fl0", SchemaBuilder.array(Schema.STRING_SCHEMA).build())
            .field("fc0", f0Schema)
            .field("fcl0", SchemaBuilder.array(f0Schema).build())
            .build();
    }

    @BeforeEach
    public void before() throws Exception {
        writer = new KafkaConnectFieldWriter(DefaultAtlasConversionService.getInstance());
    }

    @Test
    public void testWriteNullField() throws Exception {
        assertThrows(AtlasException.class, () -> {
            write(null);
        });
    }

    @Test
    public void testWriteEmptyField() throws Exception {
        KafkaConnectField f = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        assertThrows(AtlasException.class, () -> {
            write(f);
        });
    }

    @Test
    public void testWriteRoot() throws Exception {
        writer.setSchema(rootSchema);
        writeRoot("", 0);
        Struct root = (Struct) writer.getDocument();
        assertRoot(root, "", 0);
    }

    private void writeRoot(String rootPrefix, int rootIndex) throws Exception {
        KafkaConnectField f0 = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        f0.setPath(rootPrefix + "/f0");
        f0.setValue("f0val-" + rootIndex);
        write(f0);
        for (int i=0; i<3; i++) {
            KafkaConnectField fl0 = AtlasKafkaConnectModelFactory.createKafkaConnectField();
            fl0.setPath(rootPrefix + "/fl0<" + i + ">");
            fl0.setValue("fl0val-" + rootIndex + "-" + i);
            write(fl0);
        }
        KafkaConnectField fc0f0 = AtlasKafkaConnectModelFactory.createKafkaConnectField();
        fc0f0.setPath(rootPrefix + "/fc0/f0");
        fc0f0.setValue("fc0f0val-" + rootIndex);
        write(fc0f0);
        for (int i=0; i<3; i++) {
            KafkaConnectField fcl0f0 = AtlasKafkaConnectModelFactory.createKafkaConnectField();
            fcl0f0.setPath(rootPrefix + "/fcl0<" + i + ">/f0");
            fcl0f0.setValue("fcl0f0val-" + rootIndex + "-" + i);
            write(fcl0f0);
        }
    }

    private void assertRoot(Struct root, String rootPrefix, int rootIndex) {
        assertEquals("f0val-" + rootIndex, root.get("f0"));
        List<Object> fl0 = root.getArray("fl0");
        assertEquals(3, fl0.size());
        for (int i=0; i<3; i++) {
            assertEquals("fl0val-" + rootIndex + "-" + i, fl0.get(i));
        }
        Struct fc0 = root.getStruct("fc0");
        assertEquals("fc0f0val-" + rootIndex, fc0.get("f0"));
        List<Object> fcl0 = root.getArray("fcl0");
        assertEquals(3, fcl0.size());
        for (int i=0; i<3; i++) {
            Struct item = (Struct) fcl0.get(i);
            assertEquals("fcl0f0val-" + rootIndex + "-" + i, item.get("f0"));
        }
    }

    @Test
    public void testWriteCollectionRoot() throws Exception {
        writer.setSchema(SchemaBuilder.array(rootSchema).build());
        writeRoot("/<0>", 0);
        writeRoot("/<1>", 1);
        writeRoot("/<2>", 2);
        List<Object> root = (List<Object>) writer.getDocument();
        assertEquals(3, root.size());
        assertRoot((Struct)root.get(0), "/<0>", 0);
        assertRoot((Struct)root.get(1), "/<1>", 1);
        assertRoot((Struct)root.get(2), "/<2>", 2);
    }

    private void write(Field field) throws Exception {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(mock(Field.class));
        when(session.head().getTargetField()).thenReturn(field);
        writer.write(session);
    }

}
