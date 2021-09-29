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
package io.atlasmap.xml.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldWriterTest {

    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";

    private XmlFieldWriter writer = null;

    private Document document = null;
    private String seedDocument = null;
    private Map<String, String> namespaces = new HashMap<>();
    private XmlFieldReader reader = new XmlFieldReader(XmlFieldReader.class.getClassLoader(), DefaultAtlasConversionService.getInstance());
    private XmlIOHelper xmlHelper = new XmlIOHelper(XmlIOHelper.class.getClassLoader());

    @BeforeEach
    public void setup() {
        this.writer = null;
        this.document = null;
        this.seedDocument = null;
        this.namespaces = new HashMap<>();
    }

    public void createWriter() throws Exception {
        createWriter(null);
    }

    public void createWriter(Schema schema) throws Exception {
        writer = new XmlFieldWriter(XmlFieldWriter.class.getClassLoader(), namespaces, seedDocument);
        this.document = writer.getDocument();
        assertNotNull(document);
    }

    public void writeValue(String path, Object value) throws Exception {
        if (writer == null) {
            createWriter();
        }
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath(path);
        xmlField.setValue(value);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(mock(Field.class));
        when(session.head().getTargetField()).thenReturn(xmlField);
        writer.write(session);
    }

    @Test
    public void testWriteValueToDefaultDocument() throws Exception {
        writeValue("/orders/order/id", "3333333354");
        final String expected = "<orders><order><id>3333333354</id></order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueToAttributeWithDefaultDocument() throws Exception {
        writeValue("/orders/order/id/@custId", "b");
        final String expected = "<orders><order><id custId=\"b\"/></order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueWithSeedDocument() throws Exception {
        seedDocument = "<orders/>";

        writeValue("/orders/order/id/@custId", "b");
        writeValue("/orders/order/id", "3333333354");

        final String expected = "<orders><order><id custId=\"b\">3333333354</id></order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueWithSeedDocumentWithNamespaces() throws Exception {
        seedDocument = "<orders xmlns:x=\"http://www.example.com/x/\"/>";

        writeValue("/orders/order/x:id/@custId", "b");
        writeValue("/orders/order/x:id", "3333333354");

        final String expected = "<orders xmlns:x=\"http://www.example.com/x/\"><order><x:id custId=\"b\">3333333354</x:id></order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueWithSeedDocumentWithDefaultNamespace() throws Exception {
        seedDocument = "<orders xmlns=\"http://www.example.com/x/\"/>";

        writeValue("/orders/order/id/@custId", "b");
        writeValue("/orders/order/id", "3333333354");

        final String expected = "<orders xmlns=\"http://www.example.com/x/\"><order><id custId=\"b\">3333333354</id></order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueWithSeedDocumentWithNamespacesAddNamespace() throws Exception {
        seedDocument = "<orders xmlns:x=\"http://www.example.com/x/\"><x:order foo=\"bar\">preexisting</x:order></orders>";
        namespaces.put("y", "http://www.example.com/y/");

        writeValue("/orders/y:order/x:id/@custId", "b");
        writeValue("/orders/y:order/x:id", "3333333354");

        final String expected = "<orders xmlns:x=\"http://www.example.com/x/\" xmlns:y=\"http://www.example.com/y/\"><x:order foo=\"bar\">preexisting</x:order><y:order><x:id custId=\"b\">3333333354</x:id></y:order></orders>";
        checkResult(expected);
    }

    @Test
    public void testWriteValueToDefaultDocumentComplex() throws Exception {
        this.seedDocument = new String(Files.readAllBytes(Paths.get("src/test/resources/complex_example_write.xml")));

        writeValue("/orders/order[2]/id[2]", "54554555");

        checkResultFromFile("complex_example.xml");
    }

    @Test
    public void testWriteNewNodeWithAttrToDocumentComplex() throws Exception {
        this.seedDocument = new String(
                Files.readAllBytes(Paths.get("src/test/resources/complex_example_write_attr.xml")));

        writeValue("/orders/order[2]/id[2]", "54554555");
        writeValue("/orders/order[2]/id[2]/@custId", "c");

        checkResultFromFile("complex_example.xml");
    }

    @Test
    public void testBuildSimpleExampleDocument() throws Exception {
        writeValue("/orders/@totalCost", "12525.00");
        writeValue("/orders/order/id/@custId", "a");
        writeValue("/orders/order/id", "12312");
        writeValue("/orders/order/id[1]/@custId", "b");
        writeValue("/orders/order/id[1]", "4423423");

        checkResultFromFile("simple_example.xml");
    }

    @Test
    public void testBuildSimpleExampleDocumentFromSeedWithNamespace() throws Exception {
        namespaces.put("x", "http://www.example.com/x/");

        writeValue("/x:orders/@totalCost", "12525.00");
        writeValue("/x:orders/order/id/@custId", "a");
        writeValue("/x:orders/order/id", "12312");
        writeValue("/x:orders/order/id[1]/@custId", "b");
        writeValue("/x:orders/order/id[1]", "4423423");

        checkResultFromFile("simple_example_single_ns.xml");
    }

    @Test
    public void testBuildSimpleExampleDocumentWithMultipleNamespaces() throws Exception {
        namespaces.put("x", "http://www.example.com/x/");
        namespaces.put("y", "http://www.example.com/y/");

        writeValue("/x:orders/@totalCost", "12525.00");
        writeValue("/x:orders/order/y:id/@custId", "a");
        writeValue("/x:orders/order/y:id", "12312");
        writeValue("/x:orders/order/y:id[1]/@custId", "b");
        writeValue("/x:orders/order/y:id[1]", "4423423");

        checkResultFromFile("simple_example_multiple_ns.xml");
    }

    @Test
    public void testBuildSimpleExampleDocumentWithMultipleNamespacesConstructor() throws Exception {
        namespaces.put("x", "http://www.example.com/x/");
        namespaces.put("y", "http://www.example.com/y/");

        writeValue("/x:orders/@totalCost", "12525.00");
        writeValue("/x:orders/order/y:id/@custId", "a");
        writeValue("/x:orders/order/y:id", "12312");
        writeValue("/x:orders/order/y:id[1]/@custId", "b");
        writeValue("/x:orders/order/y:id[1]", "4423423");

        checkResultFromFile("simple_example_multiple_ns.xml");
    }

    @Test
    public void testBuildSimpleExampleDocumentWithNamespaceSingleFieldAndNS() throws Exception {
        namespaces.put("x", "http://www.example.com/x/");

        writeValue("/x:orders/@totalCost", "12525.00");

        final String expected = "<x:orders xmlns:x=\"http://www.example.com/x/\" totalCost=\"12525.00\"/>";
        checkResult(expected);
    }

    @Test
    public void testBuildDocumentWithMixedParentAttributeNamespaces() throws Exception {
        namespaces.put("", "http://www.example.com/x/");
        namespaces.put("y", "http://www.example.com/y/");

        writeValue("/orders/order/@y:totalCost", "12525.00");

        checkResultFromFile("simple_example_mixed_ns.xml");
    }

    @Test
    public void testBuildComplexNamespaceDuplicateElements() throws Exception {
        namespaces.put("", "http://www.example.com/x/");
        namespaces.put("y", "http://www.example.com/y/");
        namespaces.put("q", "http://www.example.com/q/");

        writeValue("/orders/@totalCost", "12525.00");
        writeValue("/orders/order/id", "a12312");
        writeValue("/orders/order/id/@y:custId", "aa");
        writeValue("/orders/order/id[1]", "b4423423");
        writeValue("/orders/order/id[1]/@y:custId", "bb");

        writeValue("/orders/q:order/id", "c12312");
        writeValue("/orders/q:order/id/@y:custId", "cx");

        writeValue("/orders/order[1]/id", "d54554555");
        writeValue("/orders/order[1]/id/@y:custId", "dc");
        writeValue("/orders/q:order[1]/id", "e12312");
        writeValue("/orders/q:order[1]/id/@y:custId", "ea");

        checkResultFromFile("complex_example_multiple_ns.xml");
    }

    @Test
    public void testWriteCollectionWithNamespace() throws Exception {
        namespaces.put("tns", "http://www.example.com/tns");

        writeValue("/tns:orders/tns:order[1]/tns:id", "1111");
        writeValue("/tns:orders/tns:order[4]/tns:id", "4444");

        String expected = "<tns:orders xmlns:tns=\"http://www.example.com/tns\">"
                + "<tns:order/>"
                + "<tns:order><tns:id>1111</tns:id></tns:order>"
                + "<tns:order/><tns:order/>"
                + "<tns:order><tns:id>4444</tns:id></tns:order>"
                + "</tns:orders>";
        checkResult(expected);
    }

    @Test
    public void testThrowExceptionOnNullXmlField() throws Exception {
        createWriter();
        XmlField field = null;
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(mock(Field.class));
        when(session.head().getTargetField()).thenReturn(field);
        assertThrows(AtlasException.class, () -> {
            writer.write(session);
        });
    }

    @Test
    public void testXmlFieldDoubleMax() throws Exception {
        validateBoundaryValue(FieldType.DOUBLE, "test-write-field-double-max.xml", Double.MAX_VALUE);
    }

    @Test
    public void testXmlFieldDoubleMin() throws Exception {
        validateBoundaryValue(FieldType.DOUBLE, "test-write-field-double-min.xml", Double.MIN_VALUE);
    }

    @Test
    public void testXmlFieldFloatMax() throws Exception {
        validateBoundaryValue(FieldType.FLOAT, "test-write-field-float-max.xml", Float.MAX_VALUE);
    }

    @Test
    public void testXmlFieldFloatMin() throws Exception {
        validateBoundaryValue(FieldType.FLOAT, "test-write-field-float-min.xml", Float.MIN_VALUE);
    }

    @Test
    public void testXmlFieldLongMax() throws Exception {
        validateBoundaryValue(FieldType.LONG, "test-write-field-long-max.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldLongMin() throws Exception {
        validateBoundaryValue(FieldType.LONG, "test-write-field-long-min.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldIntegerMax() throws Exception {
        validateBoundaryValue(FieldType.INTEGER, "test-write-field-integer-max.xml", Integer.MAX_VALUE);
    }

    @Test
    public void testXmlFieldIntegerMin() throws Exception {
        validateBoundaryValue(FieldType.INTEGER, "test-write-field-integer-min.xml", Integer.MIN_VALUE);
    }

    @Test
    public void testXmlFieldShortMax() throws Exception {
        validateBoundaryValue(FieldType.SHORT, "test-write-field-short-max.xml", Short.MAX_VALUE);
    }

    @Test
    public void testXmlFieldShortMin() throws Exception {
        validateBoundaryValue(FieldType.SHORT, "test-write-field-short-min.xml", Short.MIN_VALUE);
    }

    @Test
    public void testXmlFieldChar() throws Exception {
        validateBoundaryValue(FieldType.CHAR, "test-write-field-char.xml", '\u0021');
    }

    @Test
    public void testXmlFieldByteMax() throws Exception {
        validateBoundaryValue(FieldType.BYTE, "test-write-field-byte-max.xml", Byte.MAX_VALUE);
    }

    @Test
    public void testXmlFieldByteMin() throws Exception {
        validateBoundaryValue(FieldType.BYTE, "test-write-field-byte-min.xml", Byte.MIN_VALUE);
    }

    @Test
    public void testXmlFieldBooleanTrue() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-write-field-boolean-true.xml", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanFalse() throws Exception {
        validateBoundaryValue(FieldType.BOOLEAN, "test-write-field-boolean-false.xml", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanNumber1() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-one.xml", 1, Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanNumber0() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-zero.xml", 0, Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanLetterT() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-letter-T.xml", "T", Boolean.TRUE);
    }

    @Test
    public void testXmlFieldBooleanLetterF() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-letter-F.xml", "F", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldDoubleMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.DOUBLE, "test-write-field-double-max-range-out.xml", "1.7976931348623157E309");
    }

    @Test
    public void testXmlFieldDoubleMinRangeOut() throws Exception {
        validateRangeOutMinValue(FieldType.DOUBLE, "test-write-field-double-min-range-out.xml", "4.9E-325", 0.0);
    }

    @Test
    public void testXmlFieldFloatMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.FLOAT, "test-write-field-float-max-range-out.xml", "3.4028235E39");
    }

    @Test
    public void testXmlFieldFloatMinRangeOut() throws Exception {
        validateRangeOutMinValue(FieldType.FLOAT, "test-write-field-float-min-range-out.xml", "1.4E-46", 0.0f);
    }

    @Test
    public void testXmlFieldLongMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-write-field-long-max-range-out.xml", "9223372036854775808");
    }

    @Test
    public void testXmlFieldLongMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-write-field-long-min-range-out.xml", "-9223372036854775809");
    }

    @Test
    public void testXmlFieldIntegerMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-write-field-integer-max-range-out.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldIntegerMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-write-field-integer-min-range-out.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldShortMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-write-field-short-max-range-out.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldShortMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-write-field-short-min-range-out.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldCharMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-write-field-char-max-range-out.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldCharMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-write-field-char-min-range-out.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldByteMaxRangeOut() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-write-field-byte-max-range-out.xml", Long.MAX_VALUE);
    }

    @Test
    public void testXmlFieldByteMinRangeOut() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-write-field-byte-min-range-out.xml", Long.MIN_VALUE);
    }

    @Test
    public void testXmlFieldBooleanRangeOut() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-range-out.xml", "abcd", Boolean.FALSE);
    }

    @Test
    public void testXmlFieldBooleanDecimal() throws Exception {
        validatePrimitiveValue(FieldType.BOOLEAN, "test-write-field-boolean-decimal.xml", Double.valueOf("126.1234"),
                Boolean.TRUE);
    }

    @Test
    public void testXmlFieldLongDecimal() throws Exception {
        validateDecimalValue(FieldType.LONG, "test-write-field-long-decimal.xml", Double.valueOf("126.1234"), 126L);
    }

    @Test
    public void testXmlFieldIntegerDecimal() throws Exception {
        validateDecimalValue(FieldType.INTEGER, "test-write-field-integer-decimal.xml", Double.valueOf("126.1234"), 126);
    }

    @Test
    public void testXmlFieldShortDecimal() throws Exception {
        validateDecimalValue(FieldType.SHORT, "test-write-field-short-decimal.xml", Double.valueOf("126.1234"), (short)126);
    }

    @Test
    public void testXmlFieldCharDecimal() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-write-field-char-decimal.xml", Double.valueOf("126.1234"));
    }

    @Test
    public void testXmlFieldByteDecimal() throws Exception {
        validateDecimalValue(FieldType.BYTE, "test-write-field-byte-decimal.xml", Double.valueOf("126.1234"), (byte)126);
    }

    @Test
    public void testXmlFieldDoubleString() throws Exception {
        validateRangeOutValue(FieldType.DOUBLE, "test-write-field-double-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldFloatString() throws Exception {
        validateRangeOutValue(FieldType.FLOAT, "test-write-field-float-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldLongString() throws Exception {
        validateRangeOutValue(FieldType.LONG, "test-write-field-long-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldIntegerString() throws Exception {
        validateRangeOutValue(FieldType.INTEGER, "test-write-field-integer-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldShortString() throws Exception {
        validateRangeOutValue(FieldType.SHORT, "test-write-field-short-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldCharString() throws Exception {
        validateRangeOutValue(FieldType.CHAR, "test-write-field-char-string.xml", "abcd");
    }

    @Test
    public void testXmlFieldByteString() throws Exception {
        validateRangeOutValue(FieldType.BYTE, "test-write-field-byte-string.xml", "abcd");
    }

    private void validateBoundaryValue(FieldType fieldType, String fileName, Object testObject) throws Exception {
        validatePrimitiveValue(fieldType, fileName, testObject, testObject);
    }

    private void validatePrimitiveValue(FieldType fieldType, String fileName, Object testObject, Object expectedObject)
            throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        AtlasInternalSession session = readSession(fieldType, path, testObject);

        assertNotNull(session.head().getSourceField().getValue());
        assertEquals(expectedObject, session.head().getSourceField().getValue());
    }

    private void writeToFile(String fieldPath, Path path, Object testObject) throws Exception {
        writeValue(fieldPath, testObject.toString());
        String output = xmlHelper.writeDocumentToString(true, writer.getDocument());
        Files.write(path, output.getBytes());
    }

    private AtlasInternalSession readFromFile(String fieldPath, FieldType fieldType, Path path) throws Exception {
        reader.setDocument(getDocumentFromPath(path, false));
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

    private Document getDocumentFromPath(Path path, boolean namespaced) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaced);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(path.toFile());
    }

    private void validateRangeOutMinValue(FieldType fieldType, String fileName, Object testObject,
            Object expectedObject) throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        AtlasInternalSession session = readSession(fieldType, path, testObject);

        assertEquals(expectedObject, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    private AtlasInternalSession readSession(FieldType fieldType, Path path, Object testObject) throws Exception {
        String fieldPath = "/primitive/value";
        writeToFile(fieldPath, path, testObject);

        AtlasInternalSession session = readFromFile(fieldPath, fieldType, path);
        return session;
    }

    private void validateRangeOutValue(FieldType fieldType, String fileName, Object testObject) throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        AtlasInternalSession session = readSession(fieldType, path, testObject);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '" + testObject.toString() + "' into type '"
                + fieldType.value().toUpperCase() + "'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals(testObject.toString(), session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    private void validateDecimalValue(FieldType fieldType, String fileName, Object testObject, Object expectedValue) throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        AtlasInternalSession session = readSession(fieldType, path, testObject);

        assertEquals(expectedValue, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    private void checkResultFromFile(String expectedFilename) throws Exception {
        String filename = "src/test/resources/" + expectedFilename;
        String expected = new String(Files.readAllBytes(Paths.get(filename)));
        checkResult(expected);
    }

    private void checkResult(String s) throws Exception {
        String expected = HEADER + s;
        if (document == null) {
            throw new Exception("document is not initialized.");
        }
        /*
         * Diff diff =
         * DiffBuilder.compare(Input.fromString(expected)).withTest(Input.fromDocument(
         * document)).ignoreWhitespace().build(); assertFalse(diff.toString(),
         * diff.hasDifferences());
         */
        String actual = xmlHelper.writeDocumentToString(true, writer.getDocument());
        expected = expected.replaceAll("\n|\r", "");
        expected = expected.replaceAll("> *?<", "><");
        expected = expected.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>", "");

        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + actual);
        assertEquals(expected, actual);
    }

}
