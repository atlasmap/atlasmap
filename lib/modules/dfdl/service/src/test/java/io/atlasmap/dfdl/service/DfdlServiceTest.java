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
package io.atlasmap.dfdl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.dfdl.core.schema.CsvDfdlSchemaGenerator;
import io.atlasmap.dfdl.v2.DfdlInspectionRequest;
import io.atlasmap.dfdl.v2.DfdlInspectionResponse;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Json;
import io.atlasmap.xml.v2.InspectionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;

public class DfdlServiceTest {

    private DfdlService dfdlService = null;

    @BeforeEach
    public void setUp() {
        dfdlService = new DfdlService();
    }

    @AfterEach
    public void tearDown() {
        dfdlService = null;
    }

    @Test
    public void testSchema() throws Exception {
        final String source =
            "header1,header2,header3\n"
            + "l1r1,l1r2,l1r3\n"
            + "l2r1,l2r2,l2r3\n"
            + "l3r1,l3r2,l3r3\n";

        DfdlInspectionRequest request = new DfdlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setDfdlSchemaName("csv");
        request.getOptions().put(CsvDfdlSchemaGenerator.Options.HEADER.value(), source);
        request.getOptions().put(CsvDfdlSchemaGenerator.Options.DELIMITER.value(), ",");
        Response res = dfdlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        DfdlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, DfdlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType file = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals("tns:file", file.getName());
        assertEquals(2, file.getXmlFields().getXmlField().size());
        XmlComplexType record = (XmlComplexType) file.getXmlFields().getXmlField().get(1);
        assertEquals("record", record.getName());
        assertEquals(3, record.getXmlFields().getXmlField().size());
        XmlField header1 = (XmlField) record.getXmlFields().getXmlField().get(0);
        assertEquals("header1", header1.getName());
        assertEquals(FieldType.STRING, header1.getFieldType());
        XmlField header2 = (XmlField) record.getXmlFields().getXmlField().get(1);
        assertEquals("header2", header2.getName());
        assertEquals(FieldType.STRING, header2.getFieldType());
        XmlField header3 = (XmlField) record.getXmlFields().getXmlField().get(2);
        assertEquals("header3", header3.getName());
        assertEquals(FieldType.STRING, header3.getFieldType());
    }

    @Test
    public void testInstance() throws Exception {
        final String source =
            "header1,header2,header3\n"
            + "l1r1,l1r2,l1r3\n"
            + "l2r1,l2r2,l2r3\n"
            + "l3r1,l3r2,l3r3\n";

        DfdlInspectionRequest request = new DfdlInspectionRequest();
        request.setType(InspectionType.INSTANCE);
        request.setDfdlSchemaName("csv");
        request.getOptions().put(CsvDfdlSchemaGenerator.Options.EXAMPLE.value(), source);
        request.getOptions().put(CsvDfdlSchemaGenerator.Options.DELIMITER.value(), ",");
        Response res = dfdlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        DfdlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, DfdlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType file = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals("atlas:file", file.getName());
        assertEquals(2, file.getXmlFields().getXmlField().size());
        XmlComplexType record = (XmlComplexType) file.getXmlFields().getXmlField().get(1);
        assertEquals("record", record.getName());
        assertEquals(3, record.getXmlFields().getXmlField().size());
        XmlField header1 = (XmlField) record.getXmlFields().getXmlField().get(0);
        assertEquals("header1", header1.getName());
        assertEquals(FieldType.STRING, header1.getFieldType());
        XmlField header2 = (XmlField) record.getXmlFields().getXmlField().get(1);
        assertEquals("header2", header2.getName());
        assertEquals(FieldType.STRING, header2.getFieldType());
        XmlField header3 = (XmlField) record.getXmlFields().getXmlField().get(2);
        assertEquals("header3", header3.getName());
        assertEquals(FieldType.STRING, header3.getFieldType());
    }

}
