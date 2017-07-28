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
package io.atlasmap.reference.xmlToXml;

import static org.junit.Assert.*;

import java.io.File;
import javax.xml.bind.JAXBElement;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;
import io.atlasmap.xml.test.v2.AtlasXmlTestHelper;
import io.atlasmap.xml.test.v2.XmlFlatBoxedPrimitiveAttribute;
import io.atlasmap.xml.test.v2.XmlFlatBoxedPrimitiveElement;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveAttribute;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveElement;

public class XmlXmlFlatMappingTest extends AtlasMappingBaseTest {
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveAttributeToElementNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-attributeToElement-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-attribute-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = (JAXBElement<XmlFlatPrimitiveElement>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatPrimitiveElement.class);
        AtlasTestUtil.validateXmlFlatPrimitiveElement(xmlFPE.getValue());
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveAttributeToElementNoMappedBoxedFieldsNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-attributeToElement-noboxed-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-attribute-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = (JAXBElement<XmlFlatPrimitiveElement>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatPrimitiveElement.class);
        AtlasTestUtil.validateXmlFlatPrimitiveElement(xmlFPE.getValue());
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveElementToAttributeNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-elementToAttribute-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-element-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatPrimitiveAttribute> xmlFPA = (JAXBElement<XmlFlatPrimitiveAttribute>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatPrimitiveAttribute.class);
        AtlasTestUtil.validateXmlFlatPrimitiveAttribute(xmlFPA.getValue());
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveElementToAtributeNoMappedBoxedFieldsNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-elementToAttribute-noboxed-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-element-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatPrimitiveAttribute> xmlFPA = (JAXBElement<XmlFlatPrimitiveAttribute>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatPrimitiveAttribute.class);
        AtlasTestUtil.validateXmlFlatPrimitiveAttribute(xmlFPA.getValue());
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveAttributeToElement() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-attributeToElement.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-attribute.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFPE><intField>2</intField><shortField>1</shortField><longField>30000</longField><doubleField>50000000.0</doubleField><floatField>40000000.0</floatField><booleanField>false</booleanField><charField>a</charField><byteField>99</byteField><boxedBooleanField/><boxedByteField/><boxedCharField/><boxedDoubleField/><boxedFloatField/><boxedIntField/><boxedLongField/><boxedStringField/></XmlFPE>", (String) object);
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveAttributeToElementNoBoxed() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-attributeToElement-noboxed.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-attribute.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFPE><intField>2</intField><shortField>1</shortField><longField>30000</longField><doubleField>50000000.0</doubleField><floatField>40000000.0</floatField><booleanField>false</booleanField><charField>a</charField><byteField>99</byteField></XmlFPE>", (String) object);
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveElementToAttribute() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-elementToAttribute.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-element.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFPA booleanField=\"false\" byteField=\"99\" charField=\"a\" doubleField=\"50000000.0\" floatField=\"40000000.0\" intField=\"2\" longField=\"30000\" shortField=\"1\"/>", (String)object);
    }
    
    @Test
    public void testProcessXmlXmlFlatPrimitiveElementToAttributeNoBoxed() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-flatprimitive-elementToAttribute-noboxed.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-element.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFPA booleanField=\"false\" byteField=\"99\" charField=\"a\" doubleField=\"50000000.0\" floatField=\"40000000.0\" intField=\"2\" longField=\"30000\" shortField=\"1\"/>", (String)object);
    }
          
    @Test
    public void testProcessXmlXmlBoxedFlatMappingPrimitiveAttributeToElement() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-boxedflatprimitive-attributeToElement.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-boxedflatprimitive-attribute.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFBPE><boxedIntField>5</boxedIntField><boxedShortField>5</boxedShortField><boxedLongField>20000</boxedLongField><boxedDoubleField>90000000.0</boxedDoubleField><boxedFloatField>70000000.0</boxedFloatField><boxedBooleanField>true</boxedBooleanField><boxedCharField>z</boxedCharField><boxedByteField>87</boxedByteField></XmlFBPE>", (String)object);
    }
    
    @Test
    public void testProcessXmlXmlBoxedFlatMappingPrimitiveAttributeToElementNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-boxedflatprimitive-attributeToElement-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-boxedflatprimitive-attribute-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatBoxedPrimitiveElement> xmlFPE = (JAXBElement<XmlFlatBoxedPrimitiveElement>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatBoxedPrimitiveElement.class);
        AtlasTestUtil.validateXmlFlatPrimitiveBoxedPrimitiveElementFields(xmlFPE.getValue());
    }
    
    @Test
    public void testProcessXmlXmlBoxedFlatMappingPrimitiveElementToAttribute() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-boxedflatprimitive-elementToAttribute.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-boxedflatprimitive-element.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFBPA boxedBooleanField=\"true\" boxedByteField=\"87\" boxedCharField=\"z\" boxedDoubleField=\"90000000.0\" boxedFloatField=\"70000000.0\" boxedIntField=\"5\" boxedLongField=\"20000\" boxedShortField=\"5\"/>", (String)object);
    }
    
    @Test
    public void testProcessXmlXmlBoxedFlatMappingPrimitiveElementToAttributeNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToXml/atlasmapping-boxedflatprimitive-elementToAttribute-ns.xml"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString("src/test/resources/xmlToXml/atlas-xml-boxedflatprimitive-element-ns.xml");
        session.setInput(sourceXml);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatBoxedPrimitiveAttribute> xmlFPE = (JAXBElement<XmlFlatBoxedPrimitiveAttribute>)AtlasXmlTestHelper.unmarshal((String)object, XmlFlatBoxedPrimitiveAttribute.class);
        AtlasTestUtil.validateXmlFlatPrimitiveBoxedPrimitiveAttributeFields(xmlFPE.getValue());
    }
} 
