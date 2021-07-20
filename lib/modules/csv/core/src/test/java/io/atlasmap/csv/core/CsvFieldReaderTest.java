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

import java.io.ByteArrayInputStream;

import io.atlasmap.csv.v2.CsvComplexType;
import io.atlasmap.v2.Document;
import org.junit.jupiter.api.Test;

import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.FieldGroup;

public class CsvFieldReaderTest {

    @Test
    public void testWithNullDocument() throws Exception {
        CsvFieldReader csvFieldReader = new CsvFieldReader(new CsvConfig());
        csvFieldReader.setDocument(null);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        when(session.head().getSourceField()).thenReturn(new CsvField());
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        csvFieldReader.read(session);
        assertEquals(1, audits.getAudit().size());
        assertEquals(AuditStatus.ERROR, audits.getAudit().get(0).getStatus());
    }

    @Test
    public void testWithSimpleDocumentWithHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setFirstRecordAsHeader(true);
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("givenName,familyName\nBob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        CsvField csvField = new CsvField();
        csvField.setName("givenName");
        csvField.setPath("/<>/givenName");
        when(session.head().getSourceField()).thenReturn(csvField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup field = (FieldGroup) csvFieldReader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("Bob", field.getField().get(0).getValue());
        assertEquals("Andrew", field.getField().get(1).getValue());
    }

    @Test
    public void testWithSimpleDocumentWithoutHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("Bob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        CsvField csvField = new CsvField();
        csvField.setName("1");
        csvField.setColumn(1);
        csvField.setPath("/<>/1");
        when(session.head().getSourceField()).thenReturn(csvField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup field = (FieldGroup) csvFieldReader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("Smith", field.getField().get(0).getValue());
        assertEquals("Johnson", field.getField().get(1).getValue());
    }

    @Test
    public void testWithSimpleDocumentWithHeaderSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setHeaders("givenName,familyName");
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("Bob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        CsvField csvField = new CsvField();
        csvField.setName("familyName");
        csvField.setPath("/<>/familyName");
        when(session.head().getSourceField()).thenReturn(csvField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup field = (FieldGroup) csvFieldReader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("Smith", field.getField().get(0).getValue());
        assertEquals("Johnson", field.getField().get(1).getValue());
    }

    @Test
    public void testWithSimpleDocumentWithHeaderAndDelimiterSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setDelimiter(';');
        csvConfig.setHeaders("givenName;familyName");
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("Bob;Smith\nAndrew;Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        CsvField csvField = new CsvField();
        csvField.setName("familyName");
        csvField.setPath("/<>/familyName");
        when(session.head().getSourceField()).thenReturn(csvField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup field = (FieldGroup) csvFieldReader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("Smith", field.getField().get(0).getValue());
        assertEquals("/<0>/familyName", field.getField().get(0).getPath());
        assertEquals("Johnson", field.getField().get(1).getValue());
        assertEquals("/<1>/familyName", field.getField().get(1).getPath());
    }

    @Test
    public void testIgnoreHeaderCase() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setIgnoreHeaderCase(true);
        csvConfig.setHeaders("givenName,familyName");
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("Bob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        CsvField csvField = new CsvField();
        csvField.setName("FAMILYNAME");
        csvField.setPath("/<>/FAMILYNAME");
        when(session.head().getSourceField()).thenReturn(csvField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        FieldGroup field = (FieldGroup) csvFieldReader.read(session);
        assertEquals(0, audits.getAudit().size());
        assertEquals("Smith", field.getField().get(0).getValue());
        assertEquals("/<0>/FAMILYNAME", field.getField().get(0).getPath());
        assertEquals("Johnson", field.getField().get(1).getValue());
        assertEquals("/<1>/FAMILYNAME", field.getField().get(1).getPath());
    }

    @Test
    public void testReadSchemaWithHeaderSpecified() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setHeaders("givenName,familyName");
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("Bob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        Document document = csvFieldReader.readSchema();
        CsvComplexType list = (CsvComplexType) document.getFields().getField().get(0);
        assertEquals("givenName", list.getCsvFields().getCsvField().get(0).getName());
        assertEquals("familyName", list.getCsvFields().getCsvField().get(1).getName());
    }

    @Test
    public void testReadSchemaWithFirstRecordAsHeader() throws Exception {
        CsvConfig csvConfig = new CsvConfig();
        csvConfig.setFirstRecordAsHeader(true);
        CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
        csvFieldReader.setDocument(new ByteArrayInputStream("givenName,familyName\nBob,Smith\nAndrew,Johnson".getBytes()));
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(AtlasInternalSession.Head.class));
        Document document = csvFieldReader.readSchema();
        CsvComplexType list = (CsvComplexType) document.getFields().getField().get(0);
        assertEquals("givenName", list.getCsvFields().getCsvField().get(0).getName());
        assertEquals("familyName", list.getCsvFields().getCsvField().get(1).getName());
    }
}
