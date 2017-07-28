package io.atlasmap.xml.v2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;

/**
 */
public class DocumentXmlFieldReaderTest {
    private DocumentXmlFieldReader reader = new DocumentXmlFieldReader();


    @Test
    public void testReadDocumentSetElementValueAsString() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id");

        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentSetValueFromAttrAsString() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/@totalCost");

        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12525.00"));
    }

    @Test
    public void testReadDocumentSetElementValueComplex() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[3]/id[1]");

        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("54554555"));
    }
    
    @Test
    public void testCountCollectionIndex() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[]/id[]");

        Integer orderCount = null;
        Integer idCount = null;
        
        PathUtil pathUtil = new PathUtil(xmlField.getPath());
        for(String seg : pathUtil.getSegments()) {
            if(PathUtil.isCollectionSegment(seg)) {
                if("order".equals(PathUtil.cleanPathSegment(seg))) {
                    orderCount = reader.getCollectionCount(doc, xmlField, PathUtil.cleanPathSegment(seg));
                }
                if("id".equals(PathUtil.cleanPathSegment(seg))) {
                    idCount = reader.getCollectionCount(doc, xmlField, PathUtil.cleanPathSegment(seg));
                }

            }
        }
        
        assertNotNull(orderCount);
        assertNotNull(idCount);
        assertEquals(Integer.valueOf(4), Integer.valueOf(orderCount));
        assertEquals(Integer.valueOf(8), Integer.valueOf(idCount));
    }

    @Test
    public void testReadDocumentSetAttributeValueAsString() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("a"));
    }

    @Test
    public void testReadDocumentSetAttributeValueComplex() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = getDocument("src/test/resources/complex_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[1]/@custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("b"));
    }

    @Test
    public void testReadDocumentWithSingleNamespaceSetElementValueAsString() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example_single_ns.xml", true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/id");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentWithMultipleNamespaceSetElementValueAsString() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example_multiple_ns.xml", true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/x:orders/order/y:id/@custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("a"));
    }

    @Test
    public void testReadDocumentWithMultipleNamespaceComplex() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example_ns.xml", true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order[2]/id[1]/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("b"));
    }

    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplex() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example_multiple_ns.xml", true);
        //NB: the index is namespace aware, that is, if there are multiple namespaces, each namespace has an index starting at zero regardless of the element's indexed position in the document...
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/@y:custId");
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");
        reader.setNamespaces(namespaces);

        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("aa"));

        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order[1]/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("ea"));
    }


    @Test
    public void testReadDocumentElementWithMultipleNamespaceComplexConstructorArg() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example_multiple_ns.xml", true);
//NB: the index is namespace aware, that is, if there are multiple namespaces, each namespace has an index starting at zero regardless of the element's indexed position in the document...
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/q:order/id/@y:custId");
        assertNull(xmlField.getValue());

        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://www.example.com/q/", "q");
        namespaces.put("http://www.example.com/y/", "y");
        namespaces.put("http://www.example.com/x/", "");

        reader = new DocumentXmlFieldReader(namespaces);

        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("cx"));
        xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath("/orders/order/id/@y:custId");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("aa"));
    }

    @Test
    public void testReadDocumentMultipleFieldsSetElementValuesComplex() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example.xml");
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

        for (XmlField field : fieldList) {
            assertNull(field.getValue());
        }
        reader.read(doc, fieldList);
        for (XmlField field : fieldList) {
            assertNotNull(field.getValue());
        }
        assertThat(fieldList.getFirst().getValue(), is("54554555"));
        assertThat(fieldList.get(1).getValue(), is("12312"));
        assertThat(fieldList.getLast().getValue(), is("54554555"));
    }

    @Test
    public void testReadDocumentMisMatchedFieldName_AT416() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        //correct field name should be id or id[1]
        xmlField.setPath("/orders/order/id1");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNull(xmlField.getValue());
    }

    @Test
    public void testReadDocumentMixedNamespaces_NoNSDocument() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example.xml");
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        //there is no namespace on the document but there is this field....
        xmlField.setPath("/ns:orders/order/id");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }

    @Test
    public void testReadDocumentMixedNamespaces_NoNSOnPaths() throws Exception {
        Document doc = getDocument("src/test/resources/simple_example_single_ns.xml", true);
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        //there is a namespace on the document but there is not on the paths....
        xmlField.setPath("/orders/order/id");
        assertNull(xmlField.getValue());
        reader.read(doc, xmlField);
        assertNotNull(xmlField.getValue());
        assertThat(xmlField.getValue(), is("12312"));
    }


    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullDocument() throws Exception {
        reader.read(null, new XmlField());
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullAmlFeilds() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example.xml");
        List<XmlField> fieldList = null;
        reader.read(doc, fieldList);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlField() throws Exception {
        Document doc = getDocument("src/test/resources/complex_example.xml");
        XmlField xmlField = null;
        reader.read(doc, xmlField);
    }

    @Test
    public void testFieldTypeValueOf() {
        System.out.println(Boolean.valueOf("Foo"));
    }
    
    private Document getDocument(String uri) throws IOException, SAXException, ParserConfigurationException {
        return getDocument(uri, false);
    }

    private Document getDocument(String uri, boolean namespaced) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); //this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(new FileInputStream(uri));
    }
}
