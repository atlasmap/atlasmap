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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlFieldReaderTest {
    private XmlFieldReader reader = new XmlFieldReader(XmlFieldReader.class.getClassLoader(), DefaultAtlasConversionService.getInstance());

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

    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplex() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example_multiple_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order[0]/id[0]/@y:custId");
        String docId = "docId";
        xmlField.setDocId(docId);
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");
        reader.setNamespaces(namespaces);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        mockDataSources(docId, session);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        reader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setDocId(docId);
        xmlField2.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField2.getValue());
        when(session.head().getSourceField()).thenReturn(xmlField2);
        reader.read(session);
        assertNotNull(xmlField2.getValue());
        assertThat(xmlField2.getValue(), is("aa"));

        XmlField xmlField3 = AtlasXmlModelFactory.createXmlField();
        xmlField3.setDocId(docId);
        xmlField3.setPath("/orders/q:order[1]/id/@y:custId");
        assertNull(xmlField3.getValue());
        when(session.head().getSourceField()).thenReturn(xmlField3);
        reader.read(session);
        assertNotNull(xmlField3.getValue());
        assertThat(xmlField3.getValue(), is("ea"));
    }

    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplexConstructorArg() throws Exception {
        String doc = getDocumentString("src/test/resources/complex_example_multiple_ns.xml");
        reader.setDocument(doc, true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/@y:custId");
        String docId = "docId";
        xmlField.setDocId(docId);
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");

        XmlFieldReader multipleNamespacesReader = new XmlFieldReader(XmlFieldReader.class.getClassLoader(), DefaultAtlasConversionService.getInstance(), namespaces);
        multipleNamespacesReader.setDocument(doc, true);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        mockDataSources(docId, session);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(xmlField);
        multipleNamespacesReader.read(session);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));
        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField2.getValue());
        when(session.head().getSourceField()).thenReturn(xmlField2);
        multipleNamespacesReader.read(session);
        assertNotNull(xmlField2.getValue());
        assertThat(xmlField2.getValue(), is("aa"));
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
    public void testXmlFieldDoubleMax() throws Exception {
        validateBoundaryValue(FieldType.DOUBLE, "test-read-field-double-max.xml", Double.MAX_VALUE);
    }

    @Test
    public void testXmlFieldDoubleMin() throws Exception {
        validateBoundaryValue(FieldType.DOUBLE, "test-read-field-double-min.xml", Double.MIN_VALUE);
    }

    @Test
    public void testXmlFieldFloatMax() throws Exception {
        validateBoundaryValue(FieldType.FLOAT, "test-read-field-float-max.xml", Float.MAX_VALUE);
    }

    @Test
    public void testXmlFieldFloatMin() throws Exception {
        validateBoundaryValue(FieldType.FLOAT, "test-read-field-float-min.xml", Float.MIN_VALUE);
    }

    @Test
    public void testXmlFieldLongMax() throws Exception {
        validateBoundaryValue(FieldType.LONG, "test-read-field-long-max.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldLongMin() throws Exception {
        validateBoundaryValue(FieldType.LONG, "test-read-field-long-min.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldIntegerMax() throws Exception {
        validateBoundaryValue(FieldType.INTEGER, "test-read-field-integer-max.xml", Integer.MAX_VALUE);
    }

    @Test
    public void testXmlFieldIntegerMin() throws Exception {
        validateBoundaryValue(FieldType.INTEGER, "test-read-field-integer-min.xml", Integer.MIN_VALUE);
    }

    @Test
    public void testXmlFieldShortMax() throws Exception {
        validateBoundaryValue(FieldType.SHORT, "test-read-field-short-max.xml", Short.MAX_VALUE);
    }

    @Test
    public void testXmlFieldShortMin() throws Exception {
        validateBoundaryValue(FieldType.SHORT, "test-read-field-short-min.xml", Short.MIN_VALUE);
    }

    @Test
    public void testXmlFieldChar() throws Exception {
        validateBoundaryValue(FieldType.CHAR, "test-read-field-char.xml", '\u0021');
    }

    @Test
    public void testXmlFieldByteMax() throws Exception {
        validateBoundaryValue(FieldType.BYTE, "test-read-field-byte-max.xml", Byte.MAX_VALUE);
    }

    @Test
    public void testXmlFieldByteMin() throws Exception {
        validateBoundaryValue(FieldType.BYTE, "test-read-field-byte-min.xml", Byte.MIN_VALUE);
    }

    @Test
    public void testXmlFieldBooleanTrue() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-true.xml", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanFalse() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-false.xml", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanNumber1() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-one.xml", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanNumber0() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-zero.xml", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanLetterT() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-letter-T.xml", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanLetterF() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-letter-F.xml", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldDoubleMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.DOUBLE, "test-read-field-double-max-range-out.xml", "1.7976931348623157E309");
    }

    @Test
    public void testXmlFieldDoubleMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.DOUBLE;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "xmlFields" + File.separator + "test-read-field-double-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(0.0, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testXmlFieldFloatMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.FLOAT, "test-read-field-float-max-range-out.xml", "3.4028235E39");
    }

    @Test
    public void testXmlFieldFloatMinRangeOut() throws Exception {
        String fieldPath = "/primitive/value";
        FieldType fieldType = FieldType.FLOAT;
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "xmlFields" + File.separator + "test-read-field-float-min-range-out.xml");

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);

        assertEquals(0.0f, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testXmlFieldLongMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-read-field-long-max-range-out.xml", "9223372036854775808");
    }

    @Test
    public void testXmlFieldLongMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-read-field-long-min-range-out.xml", "-9223372036854775809");
    }

    @Test
    public void testXmlFieldIntegerMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-read-field-integer-max-range-out.xml", "9223372036854775807");
    }

    @Test
    public void testXmlFieldIntegerMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-read-field-integer-min-range-out.xml", "-9223372036854775808");
    }

    @Test
    public void testXmlFieldShortMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-read-field-short-max-range-out.xml", "9223372036854775807");
    }

    @Test
    public void testXmlFieldShortMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-read-field-short-min-range-out.xml", "-9223372036854775808");
    }

    @Test
    public void testXmlFieldCharMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-read-field-char-max-range-out.xml", "9223372036854775807");
    }

    @Test
    public void testXmlFieldCharMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-read-field-char-min-range-out.xml", "-9223372036854775808");
    }

    @Test
    public void testXmlFieldByteMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-read-field-byte-max-range-out.xml", "9223372036854775807");
    }

    @Test
    public void testXmlFieldByteMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-read-field-byte-min-range-out.xml", "-9223372036854775808");
    }

    @Test
    public void testXmlFieldBooleanRangeOut() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-range-out.xml", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanDecimal() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-read-field-boolean-decimal.xml", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldLongDecimal() throws Exception {
        validateDecimalValue(FieldType.LONG, "test-read-field-long-decimal.xml", 126L);
    }

    @Test
    public void testXmlFieldIntegerDecimal() throws Exception {
        validateDecimalValue(FieldType.INTEGER, "test-read-field-integer-decimal.xml", 126);
    }

    @Test
    public void testXmlFieldShortDecimal() throws Exception {
        validateDecimalValue(FieldType.SHORT, "test-read-field-short-decimal.xml", (short)126);
    }

    @Test
    public void testXmlFieldCharDecimal() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-read-field-char-decimal.xml", "126.1234");
    }

    @Test
    public void testXmlFieldByteDecimal() throws Exception {
        validateDecimalValue(FieldType.BYTE, "test-read-field-byte-decimal.xml", (byte)126);
    }

    @Test
    public void testXmlFieldDoubleString() throws Exception {
        validateRangeOutValue(FieldType.DOUBLE, "test-read-field-double-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldFloatString() throws Exception {
        validateRangeOutValue(FieldType.FLOAT, "test-read-field-float-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldLongString() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-read-field-long-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldIntegerString() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-read-field-integer-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldShortString() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-read-field-short-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldCharString() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-read-field-char-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldByteString() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-read-field-byte-string.xml", "abcd");
    }

    private String getDocumentString(String uri) throws IOException {
        File f = new File(uri);
        FileInputStream fis = new FileInputStream(f);
        byte[] buf = new byte[(int) f.length()];
        fis.read(buf);
        fis.close();
        return new String(buf);
    }

    private void validateBoundaryValue(FieldType fieldType, String fileName, Object testObject) throws Exception {
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "xmlFields" + File.separator + fileName);

        AtlasInternalSession session = readFromFile("/primitive/value", fieldType, path);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    private AtlasInternalSession readFromFile(String fieldPath, FieldType fieldType, Path path) throws Exception {
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

    private void validateRangeOutValue(FieldType fieldType, String fileName, String inputValue) throws Exception {
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "xmlFields" + File.separator + fileName);

        AtlasInternalSession session = readFromFile("/primitive/value", fieldType, path);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '" + inputValue + "' into type '" + fieldType.value().toUpperCase()
                + "'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals(inputValue, session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    private void validateDecimalValue(FieldType fieldType, String fileName, Object expectedValue) throws Exception {
        Path path = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "xmlFields" + File.separator + fileName);

        AtlasInternalSession session = readFromFile("/primitive/value", fieldType, path);

        assertEquals(expectedValue, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }


    private void mockDataSources(String docId, AtlasInternalSession session) {
        AtlasMapping atlasMapping = mock(AtlasMapping.class);
        List<DataSource> dataSources = new ArrayList<>();
        XmlDataSource xmlDataSource = new XmlDataSource();
        xmlDataSource.setId(docId);
        xmlDataSource.setDataSourceType(DataSourceType.SOURCE);
        XmlNamespaces atlasNamespaces = new XmlNamespaces();

        XmlNamespace xmlNamespaceQ = new XmlNamespace();
        xmlNamespaceQ.setAlias("q");
        xmlNamespaceQ.setUri("http://www.example.com/q/");

        XmlNamespace xmlNamespaceX = new XmlNamespace();
        xmlNamespaceX.setAlias("");
        xmlNamespaceX.setUri("http://www.example.com/x/");

        XmlNamespace xmlNamespaceY = new XmlNamespace();
        xmlNamespaceY.setAlias("y");
        xmlNamespaceY.setUri("http://www.example.com/y/");

        atlasNamespaces.getXmlNamespace().add(xmlNamespaceQ);
        atlasNamespaces.getXmlNamespace().add(xmlNamespaceX);
        atlasNamespaces.getXmlNamespace().add(xmlNamespaceY);
        xmlDataSource.setXmlNamespaces(atlasNamespaces);
        dataSources.add(xmlDataSource);
        when(atlasMapping.getDataSource()).thenReturn(dataSources);
        when(session.getMapping()).thenReturn(atlasMapping);
    }

}
