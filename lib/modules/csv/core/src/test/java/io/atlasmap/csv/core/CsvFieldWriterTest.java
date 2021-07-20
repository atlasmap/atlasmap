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
package io.atlasmap.csv.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class CsvFieldWriterTest {

    private void write(CsvFieldWriter writer, Field sourceField, Field targetField) throws Exception {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        when(session.head().getSourceField()).thenReturn(sourceField);
        when(session.head().getTargetField()).thenReturn(targetField);
        writer.write(session);
    }

    @Test
    public void testWithSimpleDocumentWithHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertEquals("givenName\r\nBob\r\nAndrew\r\n", csv);
    }

    @Test
    public void testWithSimpleDocumentWithoutHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setSkipHeaderRecord(true);
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertEquals("Bob\r\nAndrew\r\n", csv);
    }

    @Test
    public void testWithSimpleDocumentWithHeaderSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setHeaders("givenName");
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertEquals("givenName\r\nBob\r\nAndrew\r\n", csv);
    }

    @Test
    public void testWithNoMatchingHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setHeaders("FAMILYNAME,GIVENNAME");
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);

        String csv = writer.toCsv();
        assertEquals("FAMILYNAME,GIVENNAME\r\n,\r\n,\r\n", csv);
    }

    @Test
    public void testWithHeaderAndIgnoreHeaderCase() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setHeaders("FAMILYNAME,GIVENNAME");
        csvConfig.setIgnoreHeaderCase(true);
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);

        String csv = writer.toCsv();
        assertEquals("FAMILYNAME,GIVENNAME\r\n,Bob\r\n,Andrew\r\n", csv);
    }

    @Test
    public void testWithSimpleDocumentWithHeaderAndDelimiterSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setDelimiter(';');
        csvConfig.setHeaders("familyName;givenName");
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);

        sourceField = new FieldGroup();
        sourceField.setName("family");
        sourceField.setPath("/<>/family");
        sourceSubField0 = new CsvField();
        sourceSubField0.setName("family");
        sourceSubField0.setPath("/<0>/family");
        sourceSubField0.setValue("Smith");
        sourceField.getField().add(sourceSubField0);
        sourceSubField1 = new CsvField();
        sourceSubField1.setName("family");
        sourceSubField1.setPath("/<1>/family");
        sourceSubField1.setValue("Johnson");
        sourceField.getField().add(sourceSubField1);

        targetField = new CsvField();
        targetField.setName("familyName");
        targetField.setPath("/<>/familyName");

        write(writer, sourceField, targetField);

        String csv = writer.toCsv();
        assertEquals("familyName;givenName\r\nSmith;Bob\r\nJohnson;Andrew\r\n", csv);
    }

    @Test
    public void testWithSimpleDocumentWithTargetColumnsDefined() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        FieldGroup sourceField = new FieldGroup();
        sourceField.setName("name");
        sourceField.setPath("/<>/name");
        CsvField sourceSubField0 = new CsvField();
        sourceSubField0.setName("name");
        sourceSubField0.setPath("/<0>/name");
        sourceSubField0.setValue("Bob");
        sourceField.getField().add(sourceSubField0);
        CsvField sourceSubField1 = new CsvField();
        sourceSubField1.setName("name");
        sourceSubField1.setPath("/<1>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");
        targetField.setColumn(1);

        write(writer, sourceField, targetField);

        FieldGroup secondSourceField = new FieldGroup();
        CsvField secondSourceSubField = new CsvField();
        secondSourceSubField.setName("familyName");
        secondSourceSubField.setPath("/<0>/familyName");
        secondSourceSubField.setValue("Smith");
        secondSourceField.getField().add(secondSourceSubField);
        CsvField secondSourceSubField1 = new CsvField();
        secondSourceSubField1.setName("familyName");
        secondSourceSubField1.setPath("/<1>/familyName");
        secondSourceSubField1.setValue("Johnson");
        secondSourceField.getField().add(secondSourceSubField1);

        CsvField secondTargetField = new CsvField();
        secondTargetField.setName("familyName");
        secondTargetField.setPath("/<>/familyName");
        secondTargetField.setColumn(0);

        write(writer, secondSourceField, secondTargetField);

        String csv = writer.toCsv();
        assertEquals("familyName,givenName\r\nSmith,Bob\r\nJohnson,Andrew\r\n", csv);
    }
}
