package io.atlasmap.xml.inspect.v2;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.v2.Restriction;
import io.atlasmap.xml.v2.RestrictionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;
import io.atlasmap.xml.v2.XmlNamespace;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class XmlDocumentInspectionServiceTest {

    @Test
    public void testInspectXmlStringAsSource() throws Exception {
        final String source =
            "<data>\n" +
                "     <intField>32000</intField>\n" +
                "     <longField>12421</longField>\n" +
                "     <stringField>abc</stringField>\n" +
                "     <booleanField>true</booleanField>\n" +
                "     <doubleField>12.0</doubleField>\n" +
                "     <shortField>1000</shortField>\n" +
                "     <floatField>234.5f</floatField>\n" +
                "     <charField>A</charField>\n" +
                "</data>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));

        List<XmlComplexType> complexTypeList = xmlDocument.getFields().getField().stream()
            .filter(xmlField -> xmlField instanceof XmlComplexType)
            .map(xmlField -> (XmlComplexType) xmlField).collect(Collectors.toList());
        Assert.assertThat(complexTypeList.size(), Is.is(1));

        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));

        complexTypeList = root.getXmlFields().getXmlField().stream()
            .filter(xmlField -> xmlField instanceof XmlComplexType)
            .map(xmlField -> (XmlComplexType) xmlField).collect(Collectors.toList());
        Assert.assertThat(complexTypeList.size(), Is.is(0));

//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceElementsWithAttrs() throws Exception {
        final String source =
            "<data>\n" +
                "     <intField a='1'>32000</intField>\n" +
                "     <longField>12421</longField>\n" +
                "     <stringField>abc</stringField>\n" +
                "     <booleanField>true</booleanField>\n" +
                "     <doubleField b='2'>12.0</doubleField>\n" +
                "     <shortField>1000</shortField>\n" +
                "     <floatField>234.5f</floatField>\n" +
                "     <charField>A</charField>\n" +
                "</data>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(10));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceUsingAttrs() throws Exception {
        final String source =
            "<data intField='32000' longField='12421' stringField='abc' " +
                "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' charField='A' />";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringWithDefaultNamespace() throws Exception {
        final String source =
            "<data xmlns=\"http://x.namespace.com/\">\n" +
                "     <intField>32000</intField>\n" +
                "     <longField>12421</longField>\n" +
                "     <stringField>abc</stringField>\n" +
                "     <booleanField>true</booleanField>\n" +
                "     <doubleField>12.0</doubleField>\n" +
                "     <shortField>1000</shortField>\n" +
                "     <floatField>234.5f</floatField>\n" +
                "     <charField>A</charField>\n" +
                "</data>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);

        //check for namespace
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));
        Assert.assertNotNull(namespace);
        Assert.assertNull(namespace.getAlias());
        Assert.assertEquals("http://x.namespace.com/", namespace.getUri());

        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringWithNamespaces() throws Exception {
        final String source =
            "<x:data xmlns:x=\"http://x.namespace.com/\">\n" +
                "     <x:intField>32000</x:intField>\n" +
                "     <x:longField>12421</x:longField>\n" +
                "     <x:stringField>abc</x:stringField>\n" +
                "     <x:booleanField>true</x:booleanField>\n" +
                "     <x:doubleField>12.0</x:doubleField>\n" +
                "     <x:shortField>1000</x:shortField>\n" +
                "     <x:floatField>234.5f</x:floatField>\n" +
                "     <x:charField>A</x:charField>\n" +
                "</x:data>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        //check for namespace
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));
        Assert.assertNotNull(namespace);
        Assert.assertEquals("x", namespace.getAlias());
        Assert.assertEquals("http://x.namespace.com/", namespace.getUri());

        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceAttrsWithNamespace() throws Exception {
        final String source =
            "<data xmlns:y=\"http://y.namespace.com/\" y:intField='32000' longField='12421' stringField='abc' " +
                "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' y:charField='A' />";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        //check for namespace
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertNotNull(namespace);
        Assert.assertEquals("y", namespace.getAlias());
        Assert.assertEquals("http://y.namespace.com/", namespace.getUri());

        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSource_MultipleChildren() throws Exception {
        final String source =
            "<data>\n" +
                "     <intFields><int>3200</int><int>2500</int><int>15</int></intFields>\n" +
                "     <longFields><long>12421</long></longFields>\n" +
                "     <stringFields><string>abc</string></stringFields>\n" +
                "     <booleanFields><boolean>true</boolean></booleanFields>\n" +
                "     <doubleFields><double>12.0</double></doubleFields>\n" +
                "     <shortFields><short>1000</short></shortFields>\n" +
                "     <floatFields><float>234.5f</float></floatFields>\n" +
                "     <charFields><char>A</char></charFields>\n" +
                "</data>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
        //children
        XmlComplexType childZero = (XmlComplexType) root.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(childZero);
        Assert.assertThat(childZero.getXmlFields().getXmlField().size(), Is.is(3));
        XmlField childZeroZero = childZero.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(childZeroZero);
        Assert.assertThat(childZeroZero.getName(), Is.is("int"));
        Assert.assertThat(childZeroZero.getValue(), Is.is("3200"));
        Assert.assertThat(childZeroZero.getPath(), Is.is("/data/intFields/int"));

        XmlField childZeroOne = childZero.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(childZeroOne);
        Assert.assertThat(childZeroOne.getName(), Is.is("int"));
        Assert.assertThat(childZeroOne.getValue(), Is.is("2500"));
        Assert.assertThat(childZeroOne.getPath(), Is.is("/data/intFields/int[1]"));

//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSource_AT370_A() throws Exception {
        final String source =
            "<order>\n" +
                "   <orders>\n" +
                "      <order>\n" +
                "\t      <items>\n" +
                "\t\t     <item sku=\"4\"/>\n" +
                "\t\t     <item sku=\"5\"/>\n" +
                "\t      </items>\n" +
                "      </order>\n" +
                "   </orders>\n" +
                "</order>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(1));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSource_AT370_B() throws Exception {
        final String source =
            "<order>\n" +
                "   <orders>\n" +
                "      <order>\n" +
                "\t      <items>\n" +
                "\t\t     <item sku=\"4\"/>\n" +
                "\t\t     <item sku=\"7\"/>\n" +
                "\t      </items>\n" +
                "      </order>\n" +
                "      <order>\n" +
                "\t      <items>\n" +
                "\t\t     <item sku=\"5\"/>\n" +
                "\t\t     <item sku=\"8\"/>\n" +
                "\t      </items>\n" +
                "      </order>\n" +
                "   </orders>\n" +
                "</order>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(1));
//        debugFields(xmlDocument.getFields());
    }


    @Test
    public void testInspectInstanceFileWithXSIType() throws Exception {
        final String instance = new String(Files.readAllBytes(Paths.get("src/test/resources/xsi-type-instance.xml")));
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(instance);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlStringAsSource_Null() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String xmlDocument = null;
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlStringAsSource_Blank() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String xmlDocument = "";
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectXmlStringAsSource_ParseExpection() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String xmlDocument = "<?>";
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlDocumentAsSource_Null() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        Document xmlDocument = null;
        service.inspectXmlDocument(xmlDocument);
    }


    @Test(expected = XmlInspectionException.class)
    public void testInspectXmlStringAsSource_BadHeaderWithBOM() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\ufeff" +
            "<foo>bar</foo>";
        service.inspectXmlDocument(xmlDocument);
    }

    @Test
    public void testInspectSchemaString() throws Exception {
        final String source =
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xs:element name=\"data\">\n" +
                "    <xs:complexType>\n" +
                "      <xs:sequence>\n" +
                "        <xs:element type=\"xs:int\" name=\"intField\"/>\n" +
                "        <xs:element type=\"xs:long\" name=\"longField\"/>\n" +
                "        <xs:element type=\"xs:string\" name=\"stringField\"/>\n" +
                "        <xs:element type=\"xs:boolean\" name=\"booleanField\"/>\n" +
                "        <xs:element type=\"xs:double\" name=\"doubleField\"/>\n" +
                "        <xs:element type=\"xs:short\" name=\"shortField\"/>\n" +
                "        <xs:element type=\"xs:float\" name=\"floatField\"/>\n" +
                "        <xs:element type=\"xs:string\" name=\"charField\"/>\n" +
                "      </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "  </xs:element>\n" +
                "</xs:schema>";

        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/simple-schema.xsd").toFile();
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileComplex() throws Exception {
        File schemaFile = Paths.get("src/test/resources/complex-schema.xsd").toFile();

        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(9));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileWithNamespace() throws Exception {
        File schemaFile = Paths.get("src/test/resources/simple-namespace-schema.xsd").toFile();

        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));

        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertThat(namespace.getAlias(), Is.is("tns"));
        Assert.assertThat(namespace.getUri(), Is.is("http://example.com/"));
//        debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectShipOrderSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/ship-order-schema.xsd").toFile();

        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(4));

        //@orderId
        XmlField orderIdAttr = root.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(orderIdAttr);
        Assert.assertThat(orderIdAttr.getName(), Is.is("orderid"));
        Assert.assertThat(orderIdAttr.getValue(), Is.is("2"));
        Assert.assertThat(orderIdAttr.getPath(), Is.is("/shiporder/@orderid"));
        Assert.assertThat(orderIdAttr.getFieldType(), Is.is(FieldType.STRING));
        //orderperson
        XmlField orderPerson = root.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(orderPerson);
        Assert.assertThat(orderPerson.getName(), Is.is("orderperson"));
        Assert.assertNull(orderPerson.getValue());
        Assert.assertThat(orderPerson.getPath(), Is.is("/shiporder/orderperson"));
        Assert.assertThat(orderPerson.getFieldType(), Is.is(FieldType.STRING));
        //shipTo
        XmlField shipTo = root.getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(shipTo);
        Assert.assertTrue(shipTo instanceof XmlComplexType);
        Assert.assertThat(((XmlComplexType) shipTo).getXmlFields().getXmlField().size(), Is.is(4));
        //item
        XmlField item = root.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof XmlComplexType);
        Assert.assertThat(((XmlComplexType) item).getXmlFields().getXmlField().size(), Is.is(4));

//        debugFields(xmlDocument.getFields());
    }


    @Test
    public void testInspectPOExampleSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/po-example-schema.xsd").toFile();

        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(2));

        //PurchaseOrderType
        XmlComplexType purchaseOrder = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(purchaseOrder);
        Assert.assertThat(purchaseOrder.getXmlFields().getXmlField().size(), Is.is(5));

        //orderDate
        XmlField orderDateAttr = purchaseOrder.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(orderDateAttr);
        Assert.assertThat(orderDateAttr.getName(), Is.is("orderDate"));
        Assert.assertNull(orderDateAttr.getValue());
        Assert.assertThat(orderDateAttr.getPath(), Is.is("/purchaseOrder/@orderDate"));
        Assert.assertThat(orderDateAttr.getFieldType(), Is.is(FieldType.DATE));

        //shipTo
        XmlField shipTo = purchaseOrder.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(shipTo);
        Assert.assertThat(shipTo.getName(), Is.is("shipTo"));
        Assert.assertNull(shipTo.getValue());
        Assert.assertThat(shipTo.getPath(), Is.is("/purchaseOrder/shipTo"));
        Assert.assertThat(shipTo.getFieldType(), Is.is(FieldType.COMPLEX));
        Assert.assertThat(((XmlComplexType) shipTo).getXmlFields().getXmlField().size(), Is.is(6));
        //shipTo/@country
        XmlField shipToCountry = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(shipTo);
        Assert.assertThat(shipToCountry.getName(), Is.is("country"));
        Assert.assertThat(shipToCountry.getValue(), Is.is("US"));
        Assert.assertThat(shipToCountry.getPath(), Is.is("/purchaseOrder/shipTo/@country"));
        Assert.assertThat(shipToCountry.getFieldType(), Is.is(FieldType.UNSUPPORTED));

        XmlField shipToName = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(shipToName);
        Assert.assertThat(shipToName.getName(), Is.is("name"));
        Assert.assertNull(shipToName.getValue());
        Assert.assertThat(shipToName.getPath(), Is.is("/purchaseOrder/shipTo/name"));
        Assert.assertThat(shipToName.getFieldType(), Is.is(FieldType.STRING));


        XmlField shipToStreet = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(shipToStreet);
        Assert.assertThat(shipToStreet.getName(), Is.is("street"));
        Assert.assertNull(shipToStreet.getValue());
        Assert.assertThat(shipToStreet.getPath(), Is.is("/purchaseOrder/shipTo/street"));
        Assert.assertThat(shipToStreet.getFieldType(), Is.is(FieldType.STRING));


        XmlField shipToCity = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(shipToCity);
        Assert.assertThat(shipToCity.getName(), Is.is("city"));
        Assert.assertNull(shipToCity.getValue());
        Assert.assertThat(shipToCity.getPath(), Is.is("/purchaseOrder/shipTo/city"));
        Assert.assertThat(shipToCity.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToState = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(shipToState);
        Assert.assertThat(shipToState.getName(), Is.is("state"));
        Assert.assertNull(shipToState.getValue());
        Assert.assertThat(shipToState.getPath(), Is.is("/purchaseOrder/shipTo/state"));
        Assert.assertThat(shipToState.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToZip = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(5);
        Assert.assertNotNull(shipToZip);
        Assert.assertThat(shipToZip.getName(), Is.is("zip"));
        Assert.assertNull(shipToZip.getValue());
        Assert.assertThat(shipToZip.getPath(), Is.is("/purchaseOrder/shipTo/zip"));
        Assert.assertThat(shipToZip.getFieldType(), Is.is(FieldType.DECIMAL));

        //comment
        XmlField comment = purchaseOrder.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(comment);
        Assert.assertThat(comment.getName(), Is.is("comment"));
        Assert.assertNull(comment.getValue());
        Assert.assertThat(comment.getPath(), Is.is("/purchaseOrder/comment"));
        Assert.assertThat(comment.getFieldType(), Is.is(FieldType.STRING));
        //items
        XmlField items = purchaseOrder.getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(items);
        Assert.assertThat(items.getName(), Is.is("items"));
        Assert.assertNull(items.getValue());
        Assert.assertThat(items.getPath(), Is.is("/purchaseOrder/items"));
        Assert.assertThat(items.getFieldType(), Is.is(FieldType.COMPLEX));

        Assert.assertThat(((XmlComplexType) items).getXmlFields().getXmlField().size(), Is.is(1));

        //items/item
        XmlComplexType item = (XmlComplexType) ((XmlComplexType) items).getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(item);
        Assert.assertThat(item.getName(), Is.is("item"));
        Assert.assertNull(item.getValue());
        Assert.assertThat(item.getPath(), Is.is("/purchaseOrder/items/item"));
        Assert.assertThat(item.getFieldType(), Is.is(FieldType.COMPLEX));
        Assert.assertThat(item.getCollectionType(), Is.is(CollectionType.LIST));
        Assert.assertThat(item.getXmlFields().getXmlField().size(), Is.is(6));

        //partNum
        XmlField partNum = item.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(partNum);
        Assert.assertThat(partNum.getName(), Is.is("partNum"));
        Assert.assertNull(partNum.getValue());
        Assert.assertThat(partNum.getPath(), Is.is("/purchaseOrder/items/item/@partNum"));
        Assert.assertThat(partNum.getFieldType(), Is.is(FieldType.STRING));
        Assert.assertThat(partNum.getTypeName(), Is.is("SKU"));

        //productName
        XmlField productName = item.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(productName);
        Assert.assertThat(productName.getName(), Is.is("productName"));
        Assert.assertNull(productName.getValue());
        Assert.assertThat(productName.getPath(), Is.is("/purchaseOrder/items/item/productName"));
        Assert.assertThat(productName.getFieldType(), Is.is(FieldType.STRING));

        //quantity
        XmlField quantity = item.getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(quantity);
        Assert.assertThat(quantity.getName(), Is.is("quantity"));
        Assert.assertNull(quantity.getValue());
        Assert.assertThat(quantity.getPath(), Is.is("/purchaseOrder/items/item/quantity"));
        Assert.assertThat(quantity.getFieldType(), Is.is(FieldType.INTEGER));
        Assert.assertNotNull(quantity.getRestrictions().getRestriction());
        Assert.assertThat(quantity.getRestrictions().getRestriction().size(), Is.is(1));
        Restriction qRestriction = quantity.getRestrictions().getRestriction().get(0);
        Assert.assertNotNull(qRestriction);
        Assert.assertNotNull(qRestriction.getType());
        Assert.assertThat(qRestriction.getType(), Is.is(RestrictionType.MAX_EXCLUSIVE));
        Assert.assertNotNull(qRestriction.getValue());
        Assert.assertThat(qRestriction.getValue(), Is.is("99"));

        //USPrice
        XmlField usPrice = item.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(usPrice);
        Assert.assertThat(usPrice.getName(), Is.is("USPrice"));
        Assert.assertNull(usPrice.getValue());
        Assert.assertThat(usPrice.getPath(), Is.is("/purchaseOrder/items/item/USPrice"));
        Assert.assertThat(usPrice.getFieldType(), Is.is(FieldType.DECIMAL));

        //comment
        XmlField itemComment = item.getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(itemComment);
        Assert.assertThat(itemComment.getName(), Is.is("comment"));
        Assert.assertNull(itemComment.getValue());
        Assert.assertThat(itemComment.getPath(), Is.is("/purchaseOrder/items/item/comment"));
        Assert.assertThat(itemComment.getFieldType(), Is.is(FieldType.STRING));

        //shipDate
        XmlField shipDate = item.getXmlFields().getXmlField().get(5);
        Assert.assertNotNull(shipDate);
        Assert.assertThat(shipDate.getName(), Is.is("shipDate"));
        Assert.assertNull(shipDate.getValue());
        Assert.assertThat(shipDate.getPath(), Is.is("/purchaseOrder/items/item/shipDate"));
        Assert.assertThat(shipDate.getFieldType(), Is.is(FieldType.DATE));

        // namespaces
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);

        Assert.assertThat(namespace.getAlias(), Is.is("tns"));
        Assert.assertThat(namespace.getUri(), Is.is("http://tempuri.org/po.xsd"));

//        debugFields(xmlDocument.getFields());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaStringAsSource_Null() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String schema = null;
        service.inspectSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaStringAsSource_Blank() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        String schema = "";
        service.inspectSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaFileAsSource_Null() throws Exception {
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        File schema = null;
        service.inspectSchema(schema);
    }


    @Test(expected = XmlInspectionException.class)
    public void testInspectSchemaBad() throws Exception {
        final String source =
            "<xs:schema/>";
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(source);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectSchemaFileBad() throws Exception {
        File schemaFile = Paths.get("src/test/resources/simple-schema-bad.xsd").toFile();
        XmlDocumentInspectionService service = new XmlDocumentInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
    }

    private void debugFields(Fields xmlFields) {
        for (Field field : xmlFields.getField()) {
            Assert.assertTrue(field instanceof XmlField);
            XmlField xmlField = (XmlField) field;
            printXmlField(xmlField);
            if (xmlField instanceof XmlComplexType) {
                debugFields(((XmlComplexType) xmlField).getXmlFields());
            }
        }
    }

    private void debugFields(XmlFields xmlFields) {
        for (XmlField xmlField : xmlFields.getXmlField()) {
            printXmlField(xmlField);
            if (xmlField instanceof XmlComplexType) {
                if (((XmlComplexType) xmlField).getXmlFields() != null) {
                    debugFields(((XmlComplexType) xmlField).getXmlFields());
                }
            }
        }
    }

    private void printXmlField(XmlField xmlField) {
        System.out.println("Name --> " + xmlField.getName());
        System.out.println("Path --> " + xmlField.getPath());
        System.out.println("Value --> " + xmlField.getValue());
        if (xmlField.getFieldType() != null) {
            System.out.println("Type --> " + xmlField.getFieldType().name());
        }
        if (xmlField.getTypeName() != null) {
            System.out.println("Type Name --> " + xmlField.getTypeName());
        }
        if (xmlField.getCollectionType() != null) {
            System.out.println("Collection Type --> " + xmlField.getCollectionType().name());
        }
        if (xmlField.getRestrictions() != null && !xmlField.getRestrictions().getRestriction().isEmpty()) {
            for (Restriction restriction : xmlField.getRestrictions().getRestriction()) {
                if (restriction != null) {
                    System.out.println("Restriction Type--> " + restriction.getType());
                    System.out.println("Restriction Type Value--> " + restriction.getValue());
                }
            }
        }
        System.out.println();
    }
}
