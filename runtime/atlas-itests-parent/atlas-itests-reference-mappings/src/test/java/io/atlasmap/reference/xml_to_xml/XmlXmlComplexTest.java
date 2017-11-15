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
package io.atlasmap.reference.xml_to_xml;

import java.io.File;
import javax.xml.bind.JAXBElement;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;
import io.atlasmap.xml.test.v2.AtlasXmlTestHelper;
import io.atlasmap.xml.test.v2.XmlOrderAttribute;
import io.atlasmap.xml.test.v2.XmlOrderElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XmlXmlComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectAttributeToElement() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-attributeToElement.xml")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-attribute.xml");
        session.setInput(sourceXml);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlOE><orderId>8765309</orderId><Contact><firstName>Ozzie</firstName><lastName>Smith</lastName><phoneNumber>5551212</phoneNumber><zipCode>81111</zipCode></Contact><Address><addressLine1>123 Main St</addressLine1><addressLine2>Suite 42b</addressLine2><city>Anytown</city><state>NY</state><zipCode>90210</zipCode></Address></XmlOE>",
                (String) object);
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectElementToAttribute() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-elementToAttribute.xml")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-element.xml");
        session.setInput(sourceXml);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlOA orderId=\"8765309\"><Contact firstName=\"Ozzie\" lastName=\"Smith\" phoneNumber=\"5551212\" zipCode=\"81111\"/><Address addressLine1=\"123 Main St\" addressLine2=\"Suite 42b\" city=\"Anytown\" state=\"NY\" zipCode=\"90210\"/></XmlOA>",
                (String) object);
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectAttributeToElementNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-attributeToElement-ns.xml")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-attribute-ns.xml");
        session.setInput(sourceXml);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        @SuppressWarnings("unchecked")
        JAXBElement<XmlOrderElement> xmlOE = (JAXBElement<XmlOrderElement>) AtlasXmlTestHelper
                .unmarshal((String) object, XmlOrderElement.class);
        AtlasTestUtil.validateXmlOrderElement(xmlOE.getValue());
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectElementToAttributeNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-elementToAttribute-ns.xml")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-element-ns.xml");
        session.setInput(sourceXml);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        @SuppressWarnings("unchecked")
        JAXBElement<XmlOrderAttribute> xmlOA = (JAXBElement<XmlOrderAttribute>) AtlasXmlTestHelper
                .unmarshal((String) object, XmlOrderAttribute.class);
        AtlasTestUtil.validateXmlOrderAttribute(xmlOA.getValue());
    }
}
