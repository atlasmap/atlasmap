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
package io.atlasmap.csv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.csv.v2.CsvComplexType;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.csv.v2.CsvInspectionResponse;
import io.atlasmap.v2.Json;

public class CsvServiceTest {

    private CsvService csvService = null;

    @BeforeEach
    public void setUp() {
        csvService = new CsvService();
    }

    @AfterEach
    public void tearDown() {
        csvService = null;
    }

    @Test
    public void testSchema() throws Exception {
        final String source =
            "header1,header2,header3\n"
            + "l1r1,l1r2,l1r3\n"
            + "l2r1,l2r2,l2r3\n"
            + "l3r1,l3r2,l3r3\n";

        InputStream inputStream = new ByteArrayInputStream(source.getBytes());

        Response res = csvService.inspect(inputStream, null, ",", true, null, null,
            null, null, null, null, null, null,
            null, null, null);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        CsvInspectionResponse csvInspectionResponse = Json.mapper().readValue((byte[])entity, CsvInspectionResponse.class);
        CsvComplexType complexType = (CsvComplexType) csvInspectionResponse.getCsvDocument().getFields().getField().get(0);
        List<CsvField> fields = complexType.getCsvFields().getCsvField();
        assertEquals("header1", fields.get(0).getName());
        assertEquals("header2", fields.get(1).getName());
        assertEquals("header3", fields.get(2).getName());
    }

    @Test
    public void testSchemaNoParametersSpecified() throws Exception {
        final String source =
            "l1r1,l1r2,l1r3\n"
                + "l2r1,l2r2,l2r3\n"
                + "l3r1,l3r2,l3r3\n";

        InputStream inputStream = new ByteArrayInputStream(source.getBytes());

        Response res = csvService.inspect(inputStream, null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        CsvInspectionResponse csvInspectionResponse = Json.mapper().readValue((byte[])entity, CsvInspectionResponse.class);
        CsvComplexType complexType = (CsvComplexType) csvInspectionResponse.getCsvDocument().getFields().getField().get(0);
        List<CsvField> fields = complexType.getCsvFields().getCsvField();
        assertEquals("0", fields.get(0).getName());
        assertEquals("1", fields.get(1).getName());
        assertEquals("2", fields.get(2).getName());
    }

    @Test
    public void testSchemaFile() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.csv");

        Response res = csvService.inspect(inputStream, null, ",", true, null, null,
            null, null, null, null, null, null,
            null, null, null);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        CsvInspectionResponse csvInspectionResponse = Json.mapper().readValue((byte[])entity, CsvInspectionResponse.class);
        CsvComplexType complexType = (CsvComplexType) csvInspectionResponse.getCsvDocument().getFields().getField().get(0);
        List<CsvField> fields = complexType.getCsvFields().getCsvField();
        assertEquals(5, fields.size());
        assertEquals("sourceCsvString", fields.get(0).getName());
        assertEquals("sourceCsvNumber", fields.get(1).getName());
        assertEquals("sourceCsvDecimal", fields.get(2).getName());
        assertEquals("sourceCsvDate", fields.get(3).getName());
        assertEquals("sourceCsvBoolean", fields.get(4).getName());
    }
}
