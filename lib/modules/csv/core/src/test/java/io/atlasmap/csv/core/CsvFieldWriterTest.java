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
package io.atlasmap.csv.core;

import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Document;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        csvConfig.setFirstRecordAsHeader(true);
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
        sourceSubField1.setPath("/<0>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertThat(csv, is("givenName\r\nBob\r\nAndrew\r\n"));
    }

    @Test
    public void testWithSimpleDocumentWithoutHeader() throws Exception {
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
        sourceSubField1.setPath("/<0>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertThat(csv, is("Bob\r\nAndrew\r\n"));
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
        sourceSubField1.setPath("/<0>/name");
        sourceSubField1.setValue("Andrew");
        sourceField.getField().add(sourceSubField1);

        CsvField targetField = new CsvField();
        targetField.setName("givenName");
        targetField.setPath("/<>/givenName");

        write(writer, sourceField, targetField);
        String csv = writer.toCsv();
        assertThat(csv, is("givenName\r\nBob\r\nAndrew\r\n"));
    }

    @Test
    public void testWithSimpleDocumentWithHeaderAndDelimiterSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setDelimiter(';');
        csvConfig.setHeaders("givenName;familyName");
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
        assertThat(csv, is("givenName;familyName\r\nBob;Smith\r\nAndrew;Johnson\r\n"));
    }
}
