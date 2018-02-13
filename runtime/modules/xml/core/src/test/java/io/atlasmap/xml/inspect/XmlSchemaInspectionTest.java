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

import java.io.File;
import java.nio.file.Paths;

import org.hamcrest.core.Is;
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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/simple-schema.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileComplex() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/complex-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(9));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectSchemaFileWithNamespace() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/simple-namespace-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));

        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertThat(namespace.getAlias(), Is.is("tns"));
        Assert.assertThat(namespace.getUri(), Is.is("http://example.com/"));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectShipOrderSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/ship-order-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(4));

        // @orderId
        XmlField orderIdAttr = root.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(orderIdAttr);
        Assert.assertThat(orderIdAttr.getName(), Is.is("orderid"));
        Assert.assertThat(orderIdAttr.getValue(), Is.is("2"));
        Assert.assertThat(orderIdAttr.getPath(), Is.is("/shiporder/@orderid"));
        Assert.assertThat(orderIdAttr.getFieldType(), Is.is(FieldType.STRING));
        // orderperson
        XmlField orderPerson = root.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(orderPerson);
        Assert.assertThat(orderPerson.getName(), Is.is("orderperson"));
        Assert.assertNull(orderPerson.getValue());
        Assert.assertThat(orderPerson.getPath(), Is.is("/shiporder/orderperson"));
        Assert.assertThat(orderPerson.getFieldType(), Is.is(FieldType.STRING));
        // shipTo
        XmlField shipTo = root.getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(shipTo);
        Assert.assertTrue(shipTo instanceof XmlComplexType);
        Assert.assertThat(((XmlComplexType) shipTo).getXmlFields().getXmlField().size(), Is.is(4));
        // item
        XmlField item = root.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof XmlComplexType);
        Assert.assertThat(((XmlComplexType) item).getXmlFields().getXmlField().size(), Is.is(4));

        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectPOExampleSchemaFile() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/po-example-schema.xsd").toFile();

        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);

        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(2));

        // PurchaseOrderType
        XmlComplexType purchaseOrder = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(purchaseOrder);
        Assert.assertThat(purchaseOrder.getXmlFields().getXmlField().size(), Is.is(5));

        // orderDate
        XmlField orderDateAttr = purchaseOrder.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(orderDateAttr);
        Assert.assertThat(orderDateAttr.getName(), Is.is("tns:orderDate"));
        Assert.assertNull(orderDateAttr.getValue());
        Assert.assertThat(orderDateAttr.getPath(), Is.is("/tns:purchaseOrder/@tns:orderDate"));
        Assert.assertThat(orderDateAttr.getFieldType(), Is.is(FieldType.DATE));

        // shipTo
        XmlField shipTo = purchaseOrder.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(shipTo);
        Assert.assertThat(shipTo.getName(), Is.is("tns:shipTo"));
        Assert.assertNull(shipTo.getValue());
        Assert.assertThat(shipTo.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo"));
        Assert.assertThat(shipTo.getFieldType(), Is.is(FieldType.COMPLEX));
        Assert.assertThat(((XmlComplexType) shipTo).getXmlFields().getXmlField().size(), Is.is(6));
        // shipTo/@country
        XmlField shipToCountry = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(shipTo);
        Assert.assertThat(shipToCountry.getName(), Is.is("tns:country"));
        Assert.assertThat(shipToCountry.getValue(), Is.is("US"));
        Assert.assertThat(shipToCountry.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/@tns:country"));
        Assert.assertThat(shipToCountry.getFieldType(), Is.is(FieldType.UNSUPPORTED));

        XmlField shipToName = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(shipToName);
        Assert.assertThat(shipToName.getName(), Is.is("tns:name"));
        Assert.assertNull(shipToName.getValue());
        Assert.assertThat(shipToName.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/tns:name"));
        Assert.assertThat(shipToName.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToStreet = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(shipToStreet);
        Assert.assertThat(shipToStreet.getName(), Is.is("tns:street"));
        Assert.assertNull(shipToStreet.getValue());
        Assert.assertThat(shipToStreet.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/tns:street"));
        Assert.assertThat(shipToStreet.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToCity = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(shipToCity);
        Assert.assertThat(shipToCity.getName(), Is.is("tns:city"));
        Assert.assertNull(shipToCity.getValue());
        Assert.assertThat(shipToCity.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/tns:city"));
        Assert.assertThat(shipToCity.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToState = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(shipToState);
        Assert.assertThat(shipToState.getName(), Is.is("tns:state"));
        Assert.assertNull(shipToState.getValue());
        Assert.assertThat(shipToState.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/tns:state"));
        Assert.assertThat(shipToState.getFieldType(), Is.is(FieldType.STRING));

        XmlField shipToZip = ((XmlComplexType) shipTo).getXmlFields().getXmlField().get(5);
        Assert.assertNotNull(shipToZip);
        Assert.assertThat(shipToZip.getName(), Is.is("tns:zip"));
        Assert.assertNull(shipToZip.getValue());
        Assert.assertThat(shipToZip.getPath(), Is.is("/tns:purchaseOrder/tns:shipTo/tns:zip"));
        Assert.assertThat(shipToZip.getFieldType(), Is.is(FieldType.DECIMAL));

        // comment
        XmlField comment = purchaseOrder.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(comment);
        Assert.assertThat(comment.getName(), Is.is("tns:comment"));
        Assert.assertNull(comment.getValue());
        Assert.assertThat(comment.getPath(), Is.is("/tns:purchaseOrder/tns:comment"));
        Assert.assertThat(comment.getFieldType(), Is.is(FieldType.STRING));
        // items
        XmlField items = purchaseOrder.getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(items);
        Assert.assertThat(items.getName(), Is.is("tns:items"));
        Assert.assertNull(items.getValue());
        Assert.assertThat(items.getPath(), Is.is("/tns:purchaseOrder/tns:items"));
        Assert.assertThat(items.getFieldType(), Is.is(FieldType.COMPLEX));

        Assert.assertThat(((XmlComplexType) items).getXmlFields().getXmlField().size(), Is.is(1));

        // items/item
        XmlComplexType item = (XmlComplexType) ((XmlComplexType) items).getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(item);
        Assert.assertThat(item.getName(), Is.is("tns:item"));
        Assert.assertNull(item.getValue());
        Assert.assertThat(item.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item"));
        Assert.assertThat(item.getFieldType(), Is.is(FieldType.COMPLEX));
        Assert.assertThat(item.getCollectionType(), Is.is(CollectionType.LIST));
        Assert.assertThat(item.getXmlFields().getXmlField().size(), Is.is(6));

        // partNum
        XmlField partNum = item.getXmlFields().getXmlField().get(0);
        Assert.assertNotNull(partNum);
        Assert.assertThat(partNum.getName(), Is.is("tns:partNum"));
        Assert.assertNull(partNum.getValue());
        Assert.assertThat(partNum.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/@tns:partNum"));
        Assert.assertThat(partNum.getFieldType(), Is.is(FieldType.STRING));
        Assert.assertThat(partNum.getTypeName(), Is.is("SKU"));

        // productName
        XmlField productName = item.getXmlFields().getXmlField().get(1);
        Assert.assertNotNull(productName);
        Assert.assertThat(productName.getName(), Is.is("tns:productName"));
        Assert.assertNull(productName.getValue());
        Assert.assertThat(productName.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/tns:productName"));
        Assert.assertThat(productName.getFieldType(), Is.is(FieldType.STRING));

        // quantity
        XmlField quantity = item.getXmlFields().getXmlField().get(2);
        Assert.assertNotNull(quantity);
        Assert.assertThat(quantity.getName(), Is.is("tns:quantity"));
        Assert.assertNull(quantity.getValue());
        Assert.assertThat(quantity.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/tns:quantity"));
        Assert.assertThat(quantity.getFieldType(), Is.is(FieldType.INTEGER));
        Assert.assertNotNull(quantity.getRestrictions().getRestriction());
        Assert.assertThat(quantity.getRestrictions().getRestriction().size(), Is.is(1));
        Restriction qRestriction = quantity.getRestrictions().getRestriction().get(0);
        Assert.assertNotNull(qRestriction);
        Assert.assertNotNull(qRestriction.getType());
        Assert.assertThat(qRestriction.getType(), Is.is(RestrictionType.MAX_EXCLUSIVE));
        Assert.assertNotNull(qRestriction.getValue());
        Assert.assertThat(qRestriction.getValue(), Is.is("99"));

        // USPrice
        XmlField usPrice = item.getXmlFields().getXmlField().get(3);
        Assert.assertNotNull(usPrice);
        Assert.assertThat(usPrice.getName(), Is.is("tns:USPrice"));
        Assert.assertNull(usPrice.getValue());
        Assert.assertThat(usPrice.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/tns:USPrice"));
        Assert.assertThat(usPrice.getFieldType(), Is.is(FieldType.DECIMAL));

        // comment
        XmlField itemComment = item.getXmlFields().getXmlField().get(4);
        Assert.assertNotNull(itemComment);
        Assert.assertThat(itemComment.getName(), Is.is("tns:comment"));
        Assert.assertNull(itemComment.getValue());
        Assert.assertThat(itemComment.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/tns:comment"));
        Assert.assertThat(itemComment.getFieldType(), Is.is(FieldType.STRING));

        // shipDate
        XmlField shipDate = item.getXmlFields().getXmlField().get(5);
        Assert.assertNotNull(shipDate);
        Assert.assertThat(shipDate.getName(), Is.is("tns:shipDate"));
        Assert.assertNull(shipDate.getValue());
        Assert.assertThat(shipDate.getPath(), Is.is("/tns:purchaseOrder/tns:items/tns:item/tns:shipDate"));
        Assert.assertThat(shipDate.getFieldType(), Is.is(FieldType.DATE));

        // namespaces
        Assert.assertNotNull(xmlDocument.getXmlNamespaces());
        Assert.assertThat(xmlDocument.getXmlNamespaces().getXmlNamespace().size(), Is.is(1));

        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);

        Assert.assertThat(namespace.getAlias(), Is.is("tns"));
        Assert.assertThat(namespace.getUri(), Is.is("http://tempuri.org/po.xsd"));

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
