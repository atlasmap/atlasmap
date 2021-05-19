/*
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
package io.atlasmap.itests.reference.json_to_xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class JsonXmlAutoConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion1() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-1.json", 1);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion2() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-2.json", 2);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion3() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-3.json", 3);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion4() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-4.json", 4);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion5() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-5.json", 5);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion6() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-6.json", 6);
    }

    @Test
    public void testProcessJsonXmlFlatFieldMappingAutoConversion7() throws Exception {
        processJsonXmlFlatMappingAutoConversion(
                "src/test/resources/jsonToXml/atlasmapping-flatprimitive-attribute-autoconversion-7.json", 7);
    }

    protected void processJsonXmlFlatMappingAutoConversion(String mappingFile, int num) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
                "src/test/resources/jsonToJson/atlas-json-flatprimitive-unrooted-autoconversion.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        switch (num) {
        case 1:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion1(object);
            break;
        case 2:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion2(object);
            break;
        case 3:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion3(object);
            break;
        case 4:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion4(object);
            break;
        case 5:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion5(object);
            break;
        case 6:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion6(object);
            break;
        case 7:
            AtlasTestUtil.validateXmlFlatPrimitivePrimitiveElementAutoConversion7(object);
            break;
        default:
            fail("Unexpected number: " + num);
        }
    }
}
