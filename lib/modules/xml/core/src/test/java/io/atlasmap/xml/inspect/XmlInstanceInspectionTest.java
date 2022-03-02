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
package io.atlasmap.xml.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;

public class XmlInstanceInspectionTest extends BaseXmlInspectionServiceTest {

    @Test
    public void testInspectXmlStringAsSource() throws Exception {
        final String source = "<data>\n" + "     <intField>32000</intField>\n" + "     <longField>12421</longField>\n"
                + "     <stringField>abc</stringField>\n" + "     <booleanField>true</booleanField>\n"
                + "     <doubleField>12.0</doubleField>\n" + "     <shortField>1000</shortField>\n"
                + "     <floatField>234.5f</floatField>\n" + "     <charField>A</charField>\n" + "</data>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());

        List<XmlComplexType> complexTypeList = xmlDocument.getFields().getField().stream()
                .filter(xmlField -> xmlField instanceof XmlComplexType).map(xmlField -> (XmlComplexType) xmlField)
                .collect(Collectors.toList());
        assertEquals(1, complexTypeList.size());

        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());

        complexTypeList = root.getXmlFields().getXmlField().stream()
                .filter(xmlField -> xmlField instanceof XmlComplexType).map(xmlField -> (XmlComplexType) xmlField)
                .collect(Collectors.toList());
        assertEquals(0, complexTypeList.size());

        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceElementsWithAttrs() throws Exception {
        final String source = "<data>\n" + "     <intField a='1'>32000</intField>\n"
                + "     <longField>12421</longField>\n" + "     <stringField>abc</stringField>\n"
                + "     <booleanField>true</booleanField>\n" + "     <doubleField b='2'>12.0</doubleField>\n"
                + "     <shortField>1000</shortField>\n" + "     <floatField>234.5f</floatField>\n"
                + "     <charField>A</charField>\n" + "</data>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(10, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceUsingAttrs() throws Exception {
        final String source = "<data intField='32000' longField='12421' stringField='abc' "
                + "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' charField='A' />";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringWithDefaultNamespace() throws Exception {
        final String source = "<data xmlns=\"http://x.namespace.com/\">\n" + "     <intField>32000</intField>\n"
                + "     <longField>12421</longField>\n" + "     <stringField>abc</stringField>\n"
                + "     <booleanField>true</booleanField>\n" + "     <doubleField>12.0</doubleField>\n"
                + "     <shortField>1000</shortField>\n" + "     <floatField>234.5f</floatField>\n"
                + "     <charField>A</charField>\n" + "</data>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);

        // check for namespace
        assertNotNull(xmlDocument.getXmlNamespaces());
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals(1, xmlDocument.getXmlNamespaces().getXmlNamespace().size());
        assertNotNull(namespace);
        assertNull(namespace.getAlias());
        assertEquals("http://x.namespace.com/", namespace.getUri());

        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringWithNamespaces() throws Exception {
        final String source = "<x:data xmlns:x=\"http://x.namespace.com/\">\n" + "     <x:intField>32000</x:intField>\n"
                + "     <x:longField>12421</x:longField>\n" + "     <x:stringField>abc</x:stringField>\n"
                + "     <x:booleanField>true</x:booleanField>\n" + "     <x:doubleField>12.0</x:doubleField>\n"
                + "     <x:shortField>1000</x:shortField>\n" + "     <x:floatField>234.5f</x:floatField>\n"
                + "     <x:charField>A</x:charField>\n" + "</x:data>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        // check for namespace
        assertNotNull(xmlDocument.getXmlNamespaces());
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals(1, xmlDocument.getXmlNamespaces().getXmlNamespace().size());
        assertNotNull(namespace);
        assertEquals("x", namespace.getAlias());
        assertEquals("http://x.namespace.com/", namespace.getUri());

        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceAttrsWithNamespace() throws Exception {
        final String source = "<data xmlns:y=\"http://y.namespace.com/\" y:intField='32000' longField='12421' stringField='abc' "
                + "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' y:charField='A' />";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        // check for namespace
        assertNotNull(xmlDocument.getXmlNamespaces());
        assertEquals(1, xmlDocument.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = xmlDocument.getXmlNamespaces().getXmlNamespace().get(0);
        assertNotNull(namespace);
        assertEquals("y", namespace.getAlias());
        assertEquals("http://y.namespace.com/", namespace.getUri());

        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceMultipleChildren() throws Exception {
        final String source = "<data>\n" + "     <intFields><int>3200</int><int>2500</int><int>15</int></intFields>\n"
                + "     <longFields><long>12421</long></longFields>\n"
                + "     <stringFields><string>abc</string></stringFields>\n"
                + "     <booleanFields><boolean>true</boolean></booleanFields>\n"
                + "     <doubleFields><double>12.0</double></doubleFields>\n"
                + "     <shortFields><short>1000</short></shortFields>\n"
                + "     <floatFields><float>234.5f</float></floatFields>\n"
                + "     <charFields><char>A</char></charFields>\n" + "</data>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // children
        XmlComplexType childZero = (XmlComplexType) root.getXmlFields().getXmlField().get(0);
        assertNotNull(childZero);
        assertEquals(1, childZero.getXmlFields().getXmlField().size());
        XmlField childZeroZero = childZero.getXmlFields().getXmlField().get(0);
        assertNotNull(childZeroZero);
        assertEquals("int", childZeroZero.getName());
        assertEquals("3200", childZeroZero.getValue());
        assertEquals("/data/intFields/int<>", childZeroZero.getPath());
    }

    @Test
    public void testInspectXmlStringAsSourceAT370A() throws Exception {
        final String source = "<order>\n" + "   <orders>\n" + "      <order>\n" + "\t      <items>\n"
                + "\t\t     <item sku=\"4\"/>\n" + "\t\t     <item sku=\"5\"/>\n" + "\t      </items>\n"
                + "      </order>\n" + "   </orders>\n" + "</order>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(1, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceAT370B() throws Exception {
        final String source = "<order>\n" + "   <orders>\n" + "      <order>\n" + "\t      <items>\n"
                + "\t\t     <item sku=\"4\"/>\n" + "\t\t     <item sku=\"7\"/>\n" + "\t      </items>\n"
                + "      </order>\n" + "      <order>\n" + "\t      <items>\n" + "\t\t     <item sku=\"5\"/>\n"
                + "\t\t     <item sku=\"8\"/>\n" + "\t      </items>\n" + "      </order>\n" + "   </orders>\n"
                + "</order>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(1, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectInstanceFileWithXSIType() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/xsi-type-instance.xml")));
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(instance);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceNull() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = null;
        assertThrows(IllegalArgumentException.class, () -> {
            service.inspectXmlDocument(xmlDocument);
        });
    }

    @Test
    public void testInspectXmlStringAsSourceBlank() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "";
        assertThrows(IllegalArgumentException.class, () -> {
            service.inspectXmlDocument(xmlDocument);
        });
    }

    @Test
    public void testInspectXmlStringAsSourceParseExpection() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "<?>";
        assertThrows(XmlInspectionException.class, () -> {
            service.inspectXmlDocument(xmlDocument);
        });
    }

    @Test
    public void testInspectXmlDocumentAsSourceNull() {
        XmlInspectionService service = new XmlInspectionService();
        Document xmlDocument = null;
        assertThrows(IllegalArgumentException.class, () -> {
            service.inspectXmlDocument(xmlDocument);
        });
    }

    @Test
    public void testInspectXmlStringAsSourceBadHeaderWithBOM() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\ufeff" + "<foo>bar</foo>";
        assertThrows(XmlInspectionException.class, () -> {
            service.inspectXmlDocument(xmlDocument);
        });
    }

    @Test
    public void testInspectXmlNamespace3794() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/namespace-3794.xml")));
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(instance);
        assertNotNull(xmlDocument);
        List<XmlNamespace> namespaces = xmlDocument.getXmlNamespaces().getXmlNamespace();
        assertEquals(2, namespaces.size());
        XmlNamespace acme = namespaces.get(0);
        assertEquals("acme", acme.getAlias());
        XmlNamespace soapns = namespaces.get(1);
        assertEquals("soapenv", soapns.getAlias());
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType envelope = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(envelope);
        XmlComplexType body = (XmlComplexType) envelope.getXmlFields().getXmlField().get(0);
        assertNotNull(body);
        XmlComplexType request = (XmlComplexType) body.getXmlFields().getXmlField().get(0);
        assertNotNull(request);
        assertEquals("acme:Request", request.getName());
    }

    @Test
    public void testInspectXmlNestedCollection() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/nested-collection-instance.xml")));
        XmlInspectionService service = new XmlInspectionService();
        assertNestedCollection(service.inspectXmlDocument(instance));
    }

    @Test
    public void testInspectXmlNestedCollection2() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/nested-collection-2-instance.xml")));
        XmlInspectionService service = new XmlInspectionService();
        assertNestedCollection(service.inspectXmlDocument(instance));
    }

    private void assertNestedCollection(XmlDocument xmlDocument) throws Exception {
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals("root", root.getName());
        assertEquals(2, root.getXmlFields().getXmlField().size());
        XmlComplexType firstArray = (XmlComplexType) root.getXmlFields().getXmlField().get(0);
        assertNotNull(firstArray);
        assertEquals("firstArray", firstArray.getName());
        assertEquals(CollectionType.LIST, firstArray.getCollectionType());
        assertEquals(3, firstArray.getXmlFields().getXmlField().size());
        XmlField firstValue = (XmlField) firstArray.getXmlFields().getXmlField().get(0);
        assertNotNull(firstValue);
        assertEquals("value", firstValue.getName());
        assertNull(firstValue.getCollectionType());
        assertEquals(FieldType.STRING, firstValue.getFieldType());
        XmlField firstArrayAttr = (XmlField) firstArray.getXmlFields().getXmlField().get(2);
        assertNotNull(firstArrayAttr);
        assertEquals("firstArrayAttr", firstArrayAttr.getName());
        assertNull(firstArrayAttr.getCollectionType());
        assertEquals(FieldType.STRING, firstArrayAttr.getFieldType());
        assertTrue(firstArrayAttr.isAttribute());
        XmlComplexType secondArray = (XmlComplexType) firstArray.getXmlFields().getXmlField().get(1);
        assertNotNull(secondArray);
        assertEquals("secondArray", secondArray.getName());
        assertEquals(CollectionType.LIST, secondArray.getCollectionType());
        assertEquals(3, firstArray.getXmlFields().getXmlField().size());
        XmlField secondValue = (XmlField) secondArray.getXmlFields().getXmlField().get(0);
        assertNotNull(secondValue);
        assertEquals("value", secondValue.getName());
        assertNull(secondValue.getCollectionType());
        assertEquals(FieldType.STRING, secondValue.getFieldType());
        XmlField secondArrayAttr = (XmlField) secondArray.getXmlFields().getXmlField().get(2);
        assertNotNull(secondArrayAttr);
        assertEquals("secondArrayAttr", secondArrayAttr.getName());
        assertNull(secondArrayAttr.getCollectionType());
        assertEquals(FieldType.STRING, secondArrayAttr.getFieldType());
        assertTrue(secondArrayAttr.isAttribute());
        XmlComplexType thirdArray = (XmlComplexType) secondArray.getXmlFields().getXmlField().get(1);
        assertNotNull(thirdArray);
        assertEquals("thirdArray", thirdArray.getName());
        assertEquals(CollectionType.LIST, thirdArray.getCollectionType());
        assertEquals(2, thirdArray.getXmlFields().getXmlField().size());
        XmlField thirdValue = (XmlField) thirdArray.getXmlFields().getXmlField().get(0);
        assertNotNull(thirdValue);
        assertEquals("value", thirdValue.getName());
        assertNull(thirdValue.getCollectionType());
        assertEquals(FieldType.STRING, thirdValue.getFieldType());
        XmlField thirdArrayAttr = (XmlField) thirdArray.getXmlFields().getXmlField().get(1);
        assertNotNull(thirdArrayAttr);
        assertEquals("thirdArrayAttr", thirdArrayAttr.getName());
        assertNull(thirdArrayAttr.getCollectionType());
        assertEquals(FieldType.STRING, thirdArrayAttr.getFieldType());
        assertTrue(thirdArrayAttr.isAttribute());
    }
}
