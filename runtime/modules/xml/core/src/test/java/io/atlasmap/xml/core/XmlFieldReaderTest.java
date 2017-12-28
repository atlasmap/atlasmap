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
package io.atlasmap.xml.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldReaderTest {
    private XmlFieldReader reader = new XmlFieldReader(DefaultAtlasConversionService.getInstance());

    @Test
    public void testReadDocumentSetElementValueAsString() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");

        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentSetValueFromAttrAsString() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/@totalCost");

        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12525.00"));
    }

    @Test
    public void testReadDocumentSetElementValueComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[3]/id[1]");

        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("54554555"));
    }

    @Test
    public void testReadDocumentSetAttributeValueAsString() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@custId");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("a"));
    }

    @Test
    public void testReadDocumentSetAttributeValueComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[1]/@custId");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("b"));
    }

    @Test
    public void testReadDocumentWithSingleNamespaceSetElementValueAsString() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example_single_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentWithMultipleNamespaceSetElementValueAsString() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example_multiple_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id/@custId");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("a"));
    }

    @Test
    public void testReadDocumentWithMultipleNamespaceComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[1]/@y:custId");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("b"));
    }

    @Ignore("https://github.com/atlasmap/atlasmap/issues/141")
    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example_multiple_ns.xml");
        reader.setDocument(doc, true);
        // NB: the index is namespace aware, that is, if there are multiple namespaces,
        // each namespace has an index starting at zero regardless of the element's
        // indexed position in the document...
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/@y:custId");
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");
        reader.setNamespaces(namespaces);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("aa"));

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order[1]/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("ea"));
    }

    @Ignore("https://github.com/atlasmap/atlasmap/issues/141")
    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplexConstructorArg() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example_multiple_ns.xml");
        reader.setDocument(doc, true);
        // NB: the index is namespace aware, that is, if there are multiple namespaces,
        // each namespace has an index starting at zero regardless of the element's
        // indexed position in the document...
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/@y:custId");
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");

        reader = new XmlFieldReader(DefaultAtlasConversionService.getInstance(), namespaces);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));
        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("aa"));
    }

    @Test
    public void testReadDocumentMultipleFieldsSetElementValuesComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example.xml");
        reader.setDocument(doc, false);
        LinkedList<XmlField> fieldList = new LinkedList<>();

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[3]/id[1]");
        fieldList.addLast(xmlField);

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/order[1]/id");
        fieldList.addLast(xmlField2);

        XmlField xmlField3 = AtlasXmlModelFactory.createXmlField();
        xmlField3.setPath("/orders/order[2]/id[2]");
        fieldList.addLast(xmlField3);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        for (XmlField field : fieldList) {
            assertNull(field.getValue());
            when(session.head().getSourceField()).thenReturn(field);
            reader.read(session);
            assertNotNull(field.getValue());
        }
        assertThat(fieldList.getFirst().getValue(), is("54554555"));
        assertThat(fieldList.get(1).getValue(), is("12312"));
        assertThat(fieldList.getLast().getValue(), is("54554555"));
    }

    @Test
    public void testReadDocumentMisMatchedFieldNameAT416() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        // correct field name should be id or id[1]
        xmlField.setPath("/orders/order/id1");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNull(xmlField.getValue());
    }

    @Test
    public void testReadDocumentMixedNamespacesNoNSDocument() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        // there is no namespace on the document but there is this field....
        xmlField.setPath("/ns:orders/order/id");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentMixedNamespacesNoNSOnPaths() throws Exception {
        String doc = getDocumentString("src/test/resources/simple_example_single_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        // there is a namespace on the document but there is not on the paths....
        xmlField.setPath("/orders/order/id");
        assertNull(xmlField.getValue());
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullDocument() throws Exception {
        reader.setDocument(null, false);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(new XmlField());
        reader.read(session);
    }

    public void testThrowExceptionOnNullAmlFeilds() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example.xml");
        reader.setDocument(doc, false);
        XmlField fieldList = null;
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(fieldList);
        reader.read(session);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlField() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example.xml");
        reader.setDocument(doc, false);
        XmlField xmlField = null;
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
    }

    @Test
    public void testFieldTypeValueOf() {
        System.out.println(Boolean.valueOf("Foo"));
    }

    private String getDocumentString(String uri) throws IOException {
        File f = new File(uri);
        FileInputStream fis = new FileInputStream(f);
        byte[] buf = new byte[(int) f.length()];
        fis.read(buf);
        fis.close();
        return new String(buf);
    }

    @Test
    public void testXmlFieldDoubleMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-double-max.xml");
        Object testObject = Double.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldDoubleMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-double-min.xml");
        Object testObject = Double.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldFloatMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-float-max.xml");
        Object testObject = Float.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldFloatMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-float-min.xml");
        Object testObject = Float.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldLongMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-max.xml");
        Object testObject = Long.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldLongMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-min.xml");
        Object testObject = Long.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldIntegerMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-max.xml");
        Object testObject = Integer.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldIntegerMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-min.xml");
        Object testObject = Integer.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldShortMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-max.xml");
        Object testObject = Short.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldShortMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-min.xml");
        Object testObject = Short.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldChar() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.CHAR;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-char.xml");
        Object testObject = '\u0021';

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldByteMax() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-max.xml");
        Object testObject = Byte.MAX_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldByteMin() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-min.xml");
        Object testObject = Byte.MIN_VALUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanTrue() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-true.xml");
        Object testObject = Boolean.TRUE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanFalse() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-false.xml");
        Object testObject = Boolean.FALSE;

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanNumber1() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-one.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(true, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanNumber0() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-zero.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(false, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanLetterT() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-letter-T.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(true, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanLetterF() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-letter-F.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(false, session.head().getSourceField().getValue());
    }

    private AtlasInternalSession readFromFile(String fieldPath, FieldType fieldType, Path path) throws IOException, AtlasException {
        String input = new String(Files.readAllBytes(path));
        reader.setDocument(input, false);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath(fieldPath);
        xmlField.setPrimitive(Boolean.TRUE);
        xmlField.setFieldType(fieldType);
        assertNull(xmlField.getValue());

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        return session;
    }

    @Test
    public void testXmlFieldDoubleMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-double-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '1.7976931348623157E309' into type 'DOUBLE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("1.7976931348623157E309", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldDoubleMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-double-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(0.0, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testXmlFieldFloatMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-float-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '3.4028235E39' into type 'FLOAT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("3.4028235E39", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldFloatMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-float-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(0.0f, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testXmlFieldLongMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775808' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldLongMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775809' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775809", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldIntegerMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775807' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775807", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldIntegerMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775808' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldShortMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775807' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775807", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldShortMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775808' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldCharMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.CHAR;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-char-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775807' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775807", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldCharMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.CHAR;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-char-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775808' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldByteMaxRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-max-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775807' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775807", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldByteMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775808' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldBooleanRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(true, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldBooleanDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BOOLEAN;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-boolean-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(true, session.head().getSourceField().getValue());
    }

    @Test
    public void testXmlFieldLongDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '126.1234' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("126.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldIntegerDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '126.1234' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("126.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldShortDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '126.1234' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("126.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldCharDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.CHAR;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-char-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '126.1234' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("126.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldByteDecimal() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-decimal.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '126.1234' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("126.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldDoubleString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-double-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'DOUBLE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldFloatString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-float-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'FLOAT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldLongString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.LONG;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-long-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldIntegerString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.INTEGER;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-integer-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldShortString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.SHORT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-short-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldCharString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.CHAR;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-char-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testXmlFieldByteString() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.BYTE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "xmlFields" + File.separator + "test-read-field-byte-string.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }
}
