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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));

        List<XmlComplexType> complexTypeList = xmlDocument.getFields().getField().stream()
                .filter(xmlField -> xmlField instanceof XmlComplexType).map(xmlField -> (XmlComplexType) xmlField)
                .collect(Collectors.toList());
        Assert.assertThat(complexTypeList.size(), Is.is(1));

        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));

        complexTypeList = root.getXmlFields().getXmlField().stream()
                .filter(xmlField -> xmlField instanceof XmlComplexType).map(xmlField -> (XmlComplexType) xmlField)
                .collect(Collectors.toList());
        Assert.assertThat(complexTypeList.size(), Is.is(0));

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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(10));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceUsingAttrs() throws Exception {
        final String source = "<data intField='32000' longField='12421' stringField='abc' "
                + "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' charField='A' />";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
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
        Assert.assertNotNull(xmlDocument);

        // check for namespace
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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        // check for namespace
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
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceAttrsWithNamespace() throws Exception {
        final String source = "<data xmlns:y=\"http://y.namespace.com/\" y:intField='32000' longField='12421' stringField='abc' "
                + "booleanField='true' doubleField='12.0' shortField='1000' floatField='234.5f' y:charField='A' />";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        // check for namespace
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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
        // children
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

        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectXmlStringAsSourceAT370A() throws Exception {
        final String source = "<order>\n" + "   <orders>\n" + "      <order>\n" + "\t      <items>\n"
                + "\t\t     <item sku=\"4\"/>\n" + "\t\t     <item sku=\"5\"/>\n" + "\t      </items>\n"
                + "      </order>\n" + "   </orders>\n" + "</order>";
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(source);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(1));
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
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(1));
        // debugFields(xmlDocument.getFields());
    }

    @Test
    public void testInspectInstanceFileWithXSIType() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/xsi-type-instance.xml")));
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectXmlDocument(instance);
        Assert.assertNotNull(xmlDocument);
        Assert.assertNotNull(xmlDocument.getFields());
        Assert.assertThat(xmlDocument.getFields().getField().size(), Is.is(1));
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        Assert.assertNotNull(root);
        Assert.assertThat(root.getXmlFields().getXmlField().size(), Is.is(8));
        // debugFields(xmlDocument.getFields());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlStringAsSourceNull() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = null;
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlStringAsSourceBlank() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "";
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectXmlStringAsSourceParseExpection() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "<?>";
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInspectXmlDocumentAsSourceNull() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        Document xmlDocument = null;
        service.inspectXmlDocument(xmlDocument);
    }

    @Test(expected = XmlInspectionException.class)
    public void testInspectXmlStringAsSourceBadHeaderWithBOM() throws Exception {
        XmlInspectionService service = new XmlInspectionService();
        String xmlDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\ufeff" + "<foo>bar</foo>";
        service.inspectXmlDocument(xmlDocument);
    }

}
