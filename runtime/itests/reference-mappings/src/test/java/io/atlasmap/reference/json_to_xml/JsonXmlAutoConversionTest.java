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
package io.atlasmap.reference.json_to_xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;
import io.atlasmap.xml.test.v2.AtlasXmlTestHelper;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveElement;

public class JsonXmlAutoConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion1() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-1.xml", 1);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion2() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-2.xml", 2);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion3() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-3.xml", 3);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion4() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-4.xml", 4);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion5() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-5.xml", 5);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion6() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-6.xml", 6);
    }

    protected void processJsonXmlFlatMappingAutoConversion(String mappingFile, int num) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
                "src/test/resources/jsonToJson/atlas-json-flatprimitive-unrooted-autoconversion.json");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        @SuppressWarnings("unchecked")
        JAXBElement<XmlFlatPrimitiveElement> xmlFPE = (JAXBElement<XmlFlatPrimitiveElement>) AtlasXmlTestHelper
                .unmarshal((String) object, XmlFlatPrimitiveElement.class);

        switch (num) {
        case 1:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion1(xmlFPE.getValue());
            break;
        case 2:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion2(xmlFPE.getValue());
            break;
        case 3:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion3(xmlFPE.getValue());
            break;
        case 4:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion4(xmlFPE.getValue());
            break;
        case 5:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion5(xmlFPE.getValue());
            break;
        case 6:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion6(xmlFPE.getValue());
            break;
        default:
            fail("Unexpected number: " + num);
        }
    }
}
