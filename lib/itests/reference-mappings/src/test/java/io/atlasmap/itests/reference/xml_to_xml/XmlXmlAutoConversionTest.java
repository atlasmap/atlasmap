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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.xml.test.v2.AtlasXmlTestHelper;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveElement;

public class XmlXmlAutoConversionTest extends AtlasMappingBaseTest {

    protected JAXBElement<XmlFlatPrimitiveElement> executeMapper(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = AtlasXmlTestHelper
                .unmarshal((String) object, XmlFlatPrimitiveElement.class);
        return xmlFPE;
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion1() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-1.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion1(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion2() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-2.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion2(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion3() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-3.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion3(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion4() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-4.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion4(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion5() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-5.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion5(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion6() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-6.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion6(xmlFPE.getValue());
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion7() throws Exception {
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-7.xml");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion7(xmlFPE.getValue());
    }

}
