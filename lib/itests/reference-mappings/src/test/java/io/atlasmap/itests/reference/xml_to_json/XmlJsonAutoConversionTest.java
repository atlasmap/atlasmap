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
package io.atlasmap.itests.reference.xml_to_json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class XmlJsonAutoConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion1() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-1.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 1);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion2() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-2.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 2);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion3() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-3.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 3);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion4() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-4.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 4);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion5() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-5.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 5);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion6() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-6.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 6);
    }

    @Test
    public void testProcessXmlJsonFlatFieldMappingAutoConversion7() throws Exception {
        processXmlJsonFlatMappingAutoConversion(
                "src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-autoconversion-7.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-autoconversion.xml", 7);
    }

    protected void processXmlJsonFlatMappingAutoConversion(String mappingFile, String inputFile, int num)
            throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(inputFile);
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        switch (num) {
        case 1:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(targetObject);
            break;
        case 2:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(targetObject);
            break;
        case 3:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(targetObject);
            break;
        case 4:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(targetObject);
            break;
        case 5:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(targetObject);
            break;
        case 6:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(targetObject);
            break;
        case 7:
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion7(targetObject);
            break;
        default:
            fail("Unexpected number: " + num);
        }
    }

}
