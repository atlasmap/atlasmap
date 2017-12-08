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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
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
}
