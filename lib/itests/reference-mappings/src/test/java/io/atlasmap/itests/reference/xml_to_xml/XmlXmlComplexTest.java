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
package io.atlasmap.itests.reference.xml_to_xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class XmlXmlComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectAttributeToElement() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-attributeToElement.json")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-attribute.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlOE><orderId>8765309</orderId><Contact><firstName>Ozzie</firstName><lastName>Smith</lastName><phoneNumber>5551212</phoneNumber><zipCode>81111</zipCode></Contact><Address><addressLine1>123 Main St</addressLine1><addressLine2>Suite 42b</addressLine2><city>Anytown</city><state>NY</state><zipCode>90210</zipCode></Address></XmlOE>",
                object);
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectElementToAttribute() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-elementToAttribute.json")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-element.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlOA orderId=\"8765309\"><Contact firstName=\"Ozzie\" lastName=\"Smith\" phoneNumber=\"5551212\" zipCode=\"81111\"/><Address addressLine1=\"123 Main St\" addressLine2=\"Suite 42b\" city=\"Anytown\" state=\"NY\" zipCode=\"90210\"/></XmlOA>",
                object);
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectAttributeToElementNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-attributeToElement-ns.json")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-attribute-ns.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasTestUtil.validateXmlOrderElement(object);
    }

    @Test
    public void testProcessXmlXmlComplexOrderAutodetectElementToAttributeNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-complex-order-autodetect-elementToAttribute-ns.json")
                        .toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-complex-order-autodetect-element-ns.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasTestUtil.validateXmlOrderAttribute((String)object);
    }
}
