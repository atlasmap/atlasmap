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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import io.atlasmap.api.AtlasException;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldWriterTest {

    private XmlFieldWriter writer = null;

    private Document document = null;
    private String seedDocument = null;
    private Map<String,String> namespaces = new HashMap<>();
    
    @Before
    public void setup() throws Exception {
    	this.writer = null;
    	this.document = null;
    	this.seedDocument = null;
    	this.namespaces = new HashMap<>();    			    	
    }
    
    public void createWriter() throws Exception {
    	writer = new XmlFieldWriter(namespaces, seedDocument);
    	this.document = writer.getDocument();
    	assertNotNull(document);
    }
        
    public void writeValue(String path, String value) throws Exception {
    	    if (writer == null) {
    	        createWriter();
    	    }
    	    XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath(path);
        xmlField.setValue(value);
        writer.write(xmlField);
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
    	this.seedDocument = new String(Files.readAllBytes(Paths.get("src/test/resources/complex_example_write_attr.xml")));
        
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
    
    public void checkResultFromFile(String expectedFilename) throws Exception {    	
    	String filename = "src/test/resources/" + expectedFilename;
    	String expected = new String(Files.readAllBytes(Paths.get(filename)));
    	checkResult(expected);
    }        
    
    public void checkResult(String expected) throws Exception {
    	if (document == null) {
    		throw new Exception("document is not initialized.");
    	}
    	/*
    	Diff diff = DiffBuilder.compare(Input.fromString(expected)).withTest(Input.fromDocument(document)).ignoreWhitespace().build();
        assertFalse(diff.toString(), diff.hasDifferences());
        */
    	String actual = XmlFieldWriter.writeDocumentToString(true, writer.getDocument());    	
    	expected = expected.replaceAll("\n|\r", "");
    	expected = expected.replaceAll("> *?<", "><");
    	expected = expected.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>", "");
    	
    	System.out.println("Expected: " + expected);
    	System.out.println("Actual:   " + actual);
    	assertEquals(expected, actual);
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

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlField() throws Exception {
    	createWriter();
    	XmlField field = null;
        writer.write(field);
    }

    @Test(expected = AtlasException.class)
    public void testThrowExceptionOnNullXmlFields() throws Exception {
    	createWriter();
        List<XmlField> xmlFields = null;
        writer.write(xmlFields);
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
