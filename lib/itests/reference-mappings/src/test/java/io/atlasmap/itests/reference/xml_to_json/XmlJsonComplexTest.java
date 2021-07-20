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
package io.atlasmap.itests.reference.xml_to_json;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.json.test.AtlasJsonTestRootedMapper;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetOrder;

public class XmlJsonComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectAttributeRooted() throws Exception {
        processXmlToJsonOrder("src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-attribute-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-attribute.xml", true);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectAttributeUnrooted() throws Exception {
        processXmlToJsonOrder(
                "src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-attribute-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-attribute.xml", false);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectAttributeNSRooted() throws Exception {
        processXmlToJsonOrder(
                "src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-attribute-ns-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-attribute-ns.xml", true);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectAttributeNSUnrooted() throws Exception {
        processXmlToJsonOrder(
                "src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-attribute-ns-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-attribute-ns.xml", false);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectElementRooted() throws Exception {
        processXmlToJsonOrder("src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-element-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-element.xml", true);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectElementUnrooted() throws Exception {
        processXmlToJsonOrder("src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-element-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-element.xml", false);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectElementNSRooted() throws Exception {
        processXmlToJsonOrder(
                "src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-element-ns-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-element-ns.xml", true);
    }

    @Test
    public void testProcessXmlJsonComplexOrderAutodetectElementNSUnrooted() throws Exception {
        processXmlToJsonOrder(
                "src/test/resources/xmlToJson/atlasmapping-complex-order-autodetect-element-ns-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-complex-order-autodetect-element-ns.xml", false);
    }

    protected void processXmlToJsonOrder(String mappingFile, String inputFile, boolean rooted) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString(inputFile);
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        TargetOrder targetObject = null;
        if (rooted) {
            AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
            targetObject = testMapper.readValue((String) object, TargetOrder.class);
        } else {
            AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
            targetObject = testMapper.readValue((String) object, TargetOrder.class);
        }

        AtlasTestUtil.validateJsonOrder(targetObject);
    }
}
