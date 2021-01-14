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
package io.atlasmap.xml.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.Restriction;
import io.atlasmap.xml.v2.RestrictionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;

public class XmlSchemaInspectionTest extends BaseXmlInspectionServiceTest {

    @Test
    public void testInspectSchemaString() throws Exception {
        final String source = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"data\">\n" + "    <xs:complexType>\n" + "      <xs:sequence>\n"
                + "        <xs:element type=\"xs:int\" name=\"intField\"/>\n"
                + "        <xs:element type=\"xs:long\" name=\"longField\"/>\n"
                + "        <xs:element type=\"xs:string\" name=\"stringField\"/>\n"
                + "        <xs:element type=\"xs:boolean\" name=\"booleanField\"/>\n"
                + "        <xs:element type=\"xs:double\" name=\"doubleField\"/>\n"
                + "        <xs:element type=\"xs:short\" name=\"shortField\"/>\n"
                + "        <xs:element type=\"xs:float\" name=\"floatField\"/>\n"
                + "        <xs:element type=\"xs:string\" name=\"charField\"/>\n" + "      </xs:sequence>\n"
                + "    </xs:complexType>\n" + "  </xs:element>\n" + "</xs:schema>";

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/simple-schema.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(15, root.getXmlFields().getXmlField().size());

        XmlField nmtokenField = root.getXmlFields().getXmlField().get(8);
        assertEquals("nmtokenField", nmtokenField.getName());
        assertEquals(FieldType.STRING, nmtokenField.getFieldType());
        XmlField anyuriField = root.getXmlFields().getXmlField().get(9);
        assertEquals("anyuriField", anyuriField.getName());
        assertEquals(FieldType.STRING, anyuriField.getFieldType());
        XmlField base64BinaryField = root.getXmlFields().getXmlField().get(10);
        assertEquals("base64binaryField", base64BinaryField.getName());
        assertEquals(FieldType.STRING, base64BinaryField.getFieldType());
        XmlField byteField = root.getXmlFields().getXmlField().get(11);
        assertEquals("byteField", byteField.getName());
        assertEquals(FieldType.SHORT, byteField.getFieldType());
        XmlField unsignedByteField = root.getXmlFields().getXmlField().get(12);
        assertEquals("unsignedByteField", unsignedByteField.getName());
        assertEquals(FieldType.UNSIGNED_SHORT, unsignedByteField.getFieldType());
        XmlField hexBinaryField = root.getXmlFields().getXmlField().get(13);
        assertEquals("hexBinaryField", hexBinaryField.getName());
        assertEquals(FieldType.STRING, hexBinaryField.getFieldType());
        XmlField qnameField = root.getXmlFields().getXmlField().get(14);
        assertEquals("qnameField", qnameField.getName());
        assertEquals(FieldType.STRING, qnameField.getFieldType());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileComplex() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/complex-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileWithNamespace() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/simple-namespace-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());

        assertNotNull(xmlDocument.getXmlNamespaces());
        assertEquals(1, xmlDocument.getXmlNamespaces().getXmlNamespace().size());

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals("tns", namespace.getAlias());
        assertEquals("http://example.com/", namespace.getUri());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectShipOrderSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/ship-order-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(4, root.getXmlFields().getXmlField().size());

        // @orderId
        XmlField orderIdAttr = root.getXmlFields().getXmlField().get(0);
        assertNotNull(orderIdAttr);
        assertEquals("orderid", orderIdAttr.getName());
        assertEquals("2", orderIdAttr.getValue());
        assertEquals("/shiporder/@orderid", orderIdAttr.getPath());
        assertEquals(FieldType.STRING, orderIdAttr.getFieldType());
        assertEquals(true, orderIdAttr.isAttribute());
        // orderperson
        XmlField orderPerson = root.getXmlFields().getXmlField().get(1);
        assertNotNull(orderPerson);
        assertEquals("orderperson", orderPerson.getName());
        assertNull(orderPerson.getValue());
        assertEquals("/shiporder/orderperson", orderPerson.getPath());
        assertEquals(FieldType.STRING, orderPerson.getFieldType());
        assertEquals(false, orderPerson.isAttribute());
        // shipTo
        XmlField shipTo = root.getXmlFields().getXmlField().get(2);
        assertNotNull(shipTo);
        assertTrue(shipTo instanceof XmlComplexType);
        assertEquals(4, ((XmlComplexType) shipTo).getXmlFields().getXmlField().size());
        // item
        XmlField item = root.getXmlFields().getXmlField().get(3);
        assertNotNull(item);
        assertTrue(item instanceof XmlComplexType);
        assertEquals(4, ((XmlComplexType) item).getXmlFields().getXmlField().size());

        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectPOExampleSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/po-example-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(2, xmlDocument.getFields().getField().size());

        // PurchaseOrderType
        XmlComplexType purchaseOrder = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(purchaseOrder);
        assertEquals(5, purchaseOrder.getXmlFields().getXmlField().size());

        // orderDate
        XmlField orderDateAttr = purchaseOrder.getXmlFields().getXmlField().get(0);
        assertNotNull(orderDateAttr);
        assertEquals("tns:orderDate", orderDateAttr.getName());
        assertNull(orderDateAttr.getValue());
        assertEquals("/tns:purchaseOrder/@tns:orderDate", orderDateAttr.getPath());
        assertEquals(FieldType.DATE, orderDateAttr.getFieldType());
        assertEquals(true, orderDateAttr.isAttribute());

        // shipTo
        XmlField shipTo = purchaseOrder.getXmlFields().getXmlField().get(1);
        assertNotNull(shipTo);
        assertEquals("tns:shipTo", shipTo.getName());
        assertNull(shipTo.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo", shipTo.getPath());
        assertEquals(FieldType.COMPLEX, shipTo.getFieldType());
        assertEquals(6, ((XmlComplexType) shipTo).getXmlFields().getXmlField().size());
        // shipTo/@country
        XmlField shipToCountry = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(0);
        assertNotNull(shipTo);
        assertEquals("tns:country", shipToCountry.getName());
        assertEquals("US", shipToCountry.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/@tns:country", shipToCountry.getPath());
        assertEquals(FieldType.STRING, shipToCountry.getFieldType());
        assertEquals(true, shipToCountry.isAttribute());

        XmlField shipToName = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(1);
        assertNotNull(shipToName);
        assertEquals("tns:name", shipToName.getName());
        assertNull(shipToName.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/tns:name", shipToName.getPath());
        assertEquals(FieldType.STRING, shipToName.getFieldType());
        assertEquals(false, shipToName.isAttribute());

        XmlField shipToStreet = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(2);
        assertNotNull(shipToStreet);
        assertEquals("tns:street", shipToStreet.getName());
        assertNull(shipToStreet.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/tns:street", shipToStreet.getPath());
        assertEquals(FieldType.STRING, shipToStreet.getFieldType());
        assertEquals(false, shipToStreet.isAttribute());

        XmlField shipToCity = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(3);
        assertNotNull(shipToCity);
        assertEquals("tns:city", shipToCity.getName());
        assertNull(shipToCity.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/tns:city", shipToCity.getPath());
        assertEquals(FieldType.STRING, shipToCity.getFieldType());
        assertEquals(false, shipToCity.isAttribute());

        XmlField shipToState = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(4);
        assertNotNull(shipToState);
        assertEquals("tns:state", shipToState.getName());
        assertNull(shipToState.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/tns:state", shipToState.getPath());
        assertEquals(FieldType.STRING, shipToState.getFieldType());
        assertEquals(false, shipToState.isAttribute());

        XmlField shipToZip = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(5);
        assertNotNull(shipToZip);
        assertEquals("tns:zip", shipToZip.getName());
        assertNull(shipToZip.getValue());
        assertEquals("/tns:purchaseOrder/tns:shipTo/tns:zip", shipToZip.getPath());
        assertEquals(FieldType.DECIMAL, shipToZip.getFieldType());
        assertEquals(false, shipToZip.isAttribute());

        // comment
        XmlField comment = purchaseOrder.getXmlFields().getXmlField().get(3);
        assertNotNull(comment);
        assertEquals("tns:comment", comment.getName());
        assertNull(comment.getValue());
        assertEquals("/tns:purchaseOrder/tns:comment", comment.getPath());
        assertEquals(FieldType.STRING, comment.getFieldType());
        assertEquals(false, comment.isAttribute());

        // items
        XmlField items = purchaseOrder.getXmlFields().getXmlField().get(4);
        assertNotNull(items);
        assertEquals("tns:items", items.getName());
        assertNull(items.getValue());
        assertEquals("/tns:purchaseOrder/tns:items", items.getPath());
        assertEquals(FieldType.COMPLEX, items.getFieldType());
        assertEquals(false, items.isAttribute());

        assertEquals(1, ((XmlComplexType) items).getXmlFields().getXmlField().size());

        // items/item
        XmlComplexType item = (XmlComplexType) ((XmlComplexType) items).getXmlFields().getXmlField().get(0);
        assertNotNull(item);
        assertEquals("tns:item", item.getName());
        assertNull(item.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item", item.getPath());
        assertEquals(FieldType.COMPLEX, item.getFieldType());
        assertEquals(false, item.isAttribute());
        assertEquals(CollectionType.LIST, item.getCollectionType());
        assertEquals(6, item.getXmlFields().getXmlField().size());

        // partNum
        XmlField partNum = item.getXmlFields().getXmlField().get(0);
        assertNotNull(partNum);
        assertEquals("tns:partNum", partNum.getName());
        assertNull(partNum.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/@tns:partNum", partNum.getPath());
        assertEquals(FieldType.STRING, partNum.getFieldType());
        assertEquals("SKU", partNum.getTypeName());
        assertEquals(true, partNum.isAttribute());

        // productName
        XmlField productName = item.getXmlFields().getXmlField().get(1);
        assertNotNull(productName);
        assertEquals("tns:productName", productName.getName());
        assertNull(productName.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/tns:productName", productName.getPath());
        assertEquals(FieldType.STRING, productName.getFieldType());
        assertEquals(false, productName.isAttribute());

        // quantity
        XmlField quantity = item.getXmlFields().getXmlField().get(2);
        assertNotNull(quantity);
        assertEquals("tns:quantity", quantity.getName());
        assertNull(quantity.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/tns:quantity", quantity.getPath());
        assertEquals(FieldType.BIG_INTEGER, quantity.getFieldType());
        assertEquals(false, quantity.isAttribute());
        assertNotNull(quantity.getRestrictions().getRestriction());
        assertEquals(1, quantity.getRestrictions().getRestriction().size());
        Restriction qRestriction = quantity.getRestrictions().getRestriction().get(0);
        assertNotNull(qRestriction);
        assertNotNull(qRestriction.getType());
        assertEquals(RestrictionType.MAX_EXCLUSIVE, qRestriction.getType());
        assertNotNull(qRestriction.getValue());
        assertEquals("99", qRestriction.getValue());

        // USPrice
        XmlField usPrice = item.getXmlFields().getXmlField().get(3);
        assertNotNull(usPrice);
        assertEquals("tns:USPrice", usPrice.getName());
        assertNull(usPrice.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/tns:USPrice", usPrice.getPath());
        assertEquals(FieldType.DECIMAL, usPrice.getFieldType());
        assertEquals(false, usPrice.isAttribute());

        // comment
        XmlField itemComment = item.getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(itemComment);
        assertEquals("tns:comment", itemComment.getName());
        assertNull(itemComment.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/tns:comment", itemComment.getPath());
        assertEquals(FieldType.STRING, itemComment.getFieldType());
        assertEquals(false, itemComment.isAttribute());

        // shipDate
        XmlField shipDate = item.getXmlFields().getXmlField().get(5);
        assertNotNull(shipDate);
        assertEquals("tns:shipDate", shipDate.getName());
        assertNull(shipDate.getValue());
        assertEquals("/tns:purchaseOrder/tns:items/tns:item/tns:shipDate", shipDate.getPath());
        assertEquals(FieldType.DATE, shipDate.getFieldType());
        assertEquals(false, shipDate.isAttribute());

        // namespaces
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        assertEquals(1, xmlDocument.getXmlNamespaces().getXmlNamespace().size());

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);

        assertEquals("tns", namespace.getAlias());
        assertEquals("http://tempuri.org/po.xsd", namespace.getUri());

        // debugFields(xmlDocument.getFields());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaStringAsSourceNull() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String schema = null;
        service.inspectSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaStringAsSourceBlank() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String schema = "";
        service.inspectSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectSchemaFileAsSourceNull() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        File schema = null;
        service.inspectSchema(schema);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectSchemaBad() throws Exception {
        final String source = "<xs:schema/>";
        XmlInspectionService service = new XmlInspectionService();
        service.inspectSchema(source);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectSchemaFileBad() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/simple-schema-bad.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        service.inspectSchema(schemaFile);
    }

}
