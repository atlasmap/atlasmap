/**
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

import io.atlasmap.csv.v2.CsvComplexType;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.csv.v2.CsvInspectionResponse;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Json;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CsvServiceTest {

    private CsvService csvService = null;

    @Before
    public void setUp() {
        csvService = new CsvService();
    }

    @After
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

        Response res = csvService.inspect(inputStream, null, ",", true, null,
            null, null, null, null, null, null,
            null, null, null);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        CsvInspectionResponse csvInspectionResponse = Json.mapper().readValue((byte[])entity, CsvInspectionResponse.class);
        CsvComplexType complexType = (CsvComplexType) csvInspectionResponse.getCsvDocument().getFields().getField().get(0);
        List<CsvField> fields = complexType.getCsvFields().getCsvField();
        assertThat(fields.get(0).getName(), is("header1"));
        assertThat(fields.get(1).getName(), is("header2"));
        assertThat(fields.get(2).getName(), is("header3"));
    }

}
