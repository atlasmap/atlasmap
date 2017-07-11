package io.atlasmap.xml.v2;

import io.atlasmap.api.AtlasException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 */
public class DocumentXmlFieldWriterTest {

    private DocumentXmlFieldWriter writer = new DocumentXmlFieldWriter();


    @Test
    public void testWriteValueToDefaultDocument() throws Exception {
        final String control = "<orders><order><id>3333333354</id></order></orders>";
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");
        xmlField.setValue("3333333354");
        Document document = writer.write(xmlField);
        assertNotNull(document);
        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueFromListToDefaultDocument() throws Exception {
        final String control = "<orders><order><id>3333333354</id></order></orders>";
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");
        xmlField.setValue("3333333354");
        Document document = writer.write(Collections.singletonList(xmlField));
        assertNotNull(document);
        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueToAttributeWithDefaultDocument() throws Exception {
        final String control = "<orders><order><id custId=\"b\"/></order></orders>";
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@custId");
        xmlField.setValue("b");
        Document document = writer.write(xmlField);
        assertNotNull(document);
        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueWithSeedDocument() throws Exception {
        final String control = "<orders><order><id custId=\"b\">3333333354</id></order></orders>";
        final String seedDocument = "<orders/>";
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));
        XmlField xmlField1 = AtlasXmlModelFactory.createXmlField();
        xmlField1.setPath("/orders/order/id/@custId");
        xmlField1.setValue("b");

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/order/id");
        xmlField2.setValue("3333333354");

        writer.write(Arrays.asList(xmlField1, xmlField2), document);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueWithSeedDocumentWithNamespaces() throws Exception {
        final String control = "<orders xmlns:x=\"http://www.example.com/x/\"><order><x:id custId=\"b\">3333333354</x:id></order></orders>";
        final String seedDocument = "<orders xmlns:x=\"http://www.example.com/x/\"/>";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
        Document document = b.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));

        XmlField xmlField1 = AtlasXmlModelFactory.createXmlField();
        xmlField1.setPath("/orders/order/x:id/@custId");
        xmlField1.setValue("b");

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/order/x:id");
        xmlField2.setValue("3333333354");

        writer.write(Arrays.asList(xmlField1, xmlField2), document);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueWithSeedDocumentWithDefaultNamespace() throws Exception {
        final String control = "<orders xmlns=\"http://www.example.com/x/\"><order><id custId=\"b\">3333333354</id></order></orders>";
        final String seedDocument = "<orders xmlns=\"http://www.example.com/x/\"/>";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
        Document document = b.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));

        XmlField xmlField1 = AtlasXmlModelFactory.createXmlField();
        xmlField1.setPath("/orders/order/id/@custId");
        xmlField1.setValue("b");

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/order/id");
        xmlField2.setValue("3333333354");

        writer.write(Arrays.asList(xmlField1, xmlField2), document);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueWithSeedDocumentWithNamespacesAddNamespace() throws Exception {
        final String control = "<orders xmlns:x=\"http://www.example.com/x/\" xmlns:y=\"http://www.example.com/y/\"><y:order><x:id custId=\"b\">3333333354</x:id></y:order></orders>";
        final String seedDocument = "<orders xmlns:x=\"http://www.example.com/x/\"/>";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
        Document document = b.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));

        XmlField xmlField1 = AtlasXmlModelFactory.createXmlField();
        xmlField1.setPath("/orders/y:order/x:id/@custId");
        xmlField1.setValue("b");

        XmlField xmlField2 = AtlasXmlModelFactory.createXmlField();
        xmlField2.setPath("/orders/y:order/x:id");
        xmlField2.setValue("3333333354");

        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/y/", "y");
        writer.setNamespaces(ns);

        writer.write(Arrays.asList(xmlField1, xmlField2), document);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteValueToDefaultDocumentComplex() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.parse(new FileInputStream("src/test/resources/complex_example_write.xml"));
        assertNotNull(document);

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[2]");
        xmlField.setValue("54554555");
        writer.write(xmlField, document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/complex_example.xml"))).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testWriteNewNodeWithAttrToDocumentComplex() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
        Document document = b.parse(new FileInputStream("src/test/resources/complex_example_write_attr.xml"));
        assertNotNull(document);

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[2]");
        xmlField.setValue("54554555");
        writer.write(xmlField, document);
        xmlField.setPath("/orders/order[2]/id[2]/@custId");
        xmlField.setValue("c");
        writer.write(xmlField, document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/complex_example.xml"))).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildSimpleExampleDocument() throws Exception {
        List<XmlField> fields = new LinkedList<>();

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/@totalCost");
        xmlField.setValue("12525.00");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@custId");
        xmlField.setValue("a");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id[1]/@custId");
        xmlField.setValue("b");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id[1]");
        xmlField.setValue("4423423");
        fields.add(xmlField);

        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.newDocument();
        assertNotNull(document);

        writer.write(fields, document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/simple_example.xml"))).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildSimpleExampleDocumentFromSeedWithNamespace() throws Exception {
        List<XmlField> fields = new LinkedList<>();
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/@totalCost");
        xmlField.setValue("12525.00");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id/@custId");
        xmlField.setValue("a");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id[1]/@custId");
        xmlField.setValue("b");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id[1]");
        xmlField.setValue("4423423");
        fields.add(xmlField);

        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.newDocument();
        Node root = document.createElementNS("http://www.example.com/x/", "x:orders");
        document.appendChild(root);
        assertNotNull(document);

        writer.write(fields, document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/simple_example_single_ns.xml"))).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildSimpleExampleDocumentWithMultipleNamespaces() throws Exception {
        List<XmlField> fields = new LinkedList<>();
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/@totalCost");
        xmlField.setValue("12525.00");
        fields.add(xmlField);


        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id/@custId");
        xmlField.setValue("a");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id[1]/@custId");
        xmlField.setValue("b");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id[1]");
        xmlField.setValue("4423423");
        fields.add(xmlField);


        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/x/", "x");
        ns.put("http://www.example.com/y/", "y");

        writer.setNamespaces(ns);

        Document document = writer.write(fields);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/simple_example_multiple_ns.xml"))).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildSimpleExampleDocumentWithMultipleNamespacesConstructor() throws Exception {
        List<XmlField> fields = new LinkedList<>();
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/@totalCost");
        xmlField.setValue("12525.00");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id/@custId");
        xmlField.setValue("a");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id[1]/@custId");
        xmlField.setValue("b");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id[1]");
        xmlField.setValue("4423423");
        fields.add(xmlField);

        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/x/", "x");
        ns.put("http://www.example.com/y/", "y");

        DocumentXmlFieldWriter writer = new DocumentXmlFieldWriter(ns);
        Document document = writer.write(fields);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/simple_example_multiple_ns.xml"))).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildSimpleExampleDocumentWithNamespaceSingleFieldAndNS() throws Exception {
        final String control = "<x:orders totalCost=\"12525.00\" xmlns:x=\"http://www.example.com/x/\"/>";

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/@totalCost");
        xmlField.setValue("12525.00");
        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/x/", "x");
        writer.setNamespaces(ns);

        Document document = writer.write(xmlField);
        assertNotNull(document);
        Diff diff = DiffBuilder.compare(Input.fromString(control)).withTest(Input.fromDocument(document)).build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildDocumentWithMixedParentAttributeNamespaces() throws Exception {
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/y:@totalCost");
        xmlField.setValue("12525.00");

        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/x", "");
        ns.put("http://www.example.com/y", "y");

        writer.setNamespaces(ns);
        Document document = writer.write(xmlField);
        assertNotNull(document);
        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/simple_example_mixed_ns.xml")))
            .withTest(Input.fromDocument(document)).checkForSimilar().ignoreWhitespace().build();

        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void testBuildComplexNamespaceDuplicateElements() throws Exception {
        List<XmlField> fields = new LinkedList<>();

        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/@totalCost");
        xmlField.setValue("12525.00");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/y:@custId");
        xmlField.setValue("a");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id[1]");
        xmlField.setValue("4423423");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id[1]/y:@custId");
        xmlField.setValue("b");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/y:@custId");
        xmlField.setValue("x");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[1]/id");
        xmlField.setValue("54554555");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[1]/id/y:@custId");
        xmlField.setValue("c");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order[1]/id");
        xmlField.setValue("12312");
        fields.add(xmlField);

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order[1]/id/y:@custId");
        xmlField.setValue("a");
        fields.add(xmlField);


        Map<String, String> ns = new LinkedHashMap<>();
        ns.put("http://www.example.com/x/", "");
        ns.put("http://www.example.com/y/", "y");
        ns.put("http://www.example.com/q/", "q");

        writer.setNamespaces(ns);

        Document document = writer.write(fields);
        assertNotNull(document);

        Diff diff = DiffBuilder.compare(Input.fromStream(new FileInputStream("src/test/resources/complex_example_multiple_ns.xml")))
            .withTest(Input.fromDocument(document)).ignoreComments().ignoreWhitespace().build();

        assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullDocument() throws Exception {
        writer.write(new XmlField(), null);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlField() throws Exception {
        XmlField xmlField = null;
        writer.write(xmlField);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlFieldWithDocument() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.newDocument();
        XmlField xmlField = null;
        writer.write(xmlField, document);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlFields() throws Exception {
        List<XmlField> xmlFields = null;
        writer.write(xmlFields);
    }


    @Test(expected = AtlasException.class)
    public void testThrowExceptionWithNullListAndDocument() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = b.newDocument();
        List<XmlField> xmlFields = null;
        writer.write(xmlFields, document);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionWithListAndNullDocument() throws Exception {
        List<XmlField> xmlFields = new LinkedList<>();
        xmlFields.add(new XmlField());
        writer.write(xmlFields, null);
    }

    // --Commented out by Inspection START (5/3/17, 2:48 PM):
//    private void writeDocument(Document document, OutputStream out) throws Exception {
//        DOMSource source = new DOMSource(document.getDocumentElement());
//        StreamResult result = new StreamResult(out);
//        TransformerFactory transFactory = TransformerFactory.newInstance();
//        Transformer transformer = transFactory.newTransformer();
//        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//        transformer.transform(source, result);
//    }
// --Commented out by Inspection STOP (5/3/17, 2:48 PM)
}
