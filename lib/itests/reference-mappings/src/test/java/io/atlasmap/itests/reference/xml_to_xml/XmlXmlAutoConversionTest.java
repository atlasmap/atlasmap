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

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class XmlXmlAutoConversionTest extends AtlasMappingBaseTest {

    protected String executeMapper(String fileName) throws Exception {
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
        return (String)object;
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion1() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-1.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion1(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion2() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-2.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion2(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion3() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-3.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion3(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion4() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-4.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion4(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion5() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-5.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion5(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion6() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-6.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion6(xmlFPE);
    }

    @Test
    public void testProcessXmlXmlFlatFieldMappingAutoConversion7() throws Exception {
        String xmlFPE = executeMapper(
                "src/test/resources/xmlToXml/atlasmapping-flatprimitive-attribute-autoconversion-7.json");
        AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion7(xmlFPE);
    }

}
