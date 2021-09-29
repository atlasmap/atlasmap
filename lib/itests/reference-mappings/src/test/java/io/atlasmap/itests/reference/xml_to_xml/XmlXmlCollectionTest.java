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
package io.atlasmap.itests.reference.xml_to_xml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class XmlXmlCollectionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionComplex() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-collection-complex.xml");
        session.setSourceDocument("XmlSource", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        assertThat(string).hasXPath("//XmlOE");
        assertThat(string).valueByXPath("count(//XmlOE/contactList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            assertThat(string).valueByXPath("//XmlOE/contactList[" + (i+1) + "]/firstName").isEqualTo("first" + (i+1));
            assertThat(string).doesNotHaveXPath("//XmlOE/contactList[" + (i+1) + "]/lastName");
        }
        assertThat(string).valueByXPath("count(//XmlOE/contactSAList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            assertThat(string).valueByXPath("//XmlOE/contactSAList[" + (i+1) + "]/firstName").isEqualTo("FIRSTSA" + (i+1));
            assertThat(string).doesNotHaveXPath("//XmlOE/contactSAList[" + (i+1) + "]/lastName");
        }
        assertThat(string).valueByXPath("count(//XmlOE/contactTAList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            assertThat(string).valueByXPath("//XmlOE/contactTAList[" + (i+1) + "]/firstName").isEqualTo("FIRSTTA" + (i+1));
            assertThat(string).doesNotHaveXPath("//XmlOE/contactTAList[" + (i+1) + "]/lastName");
        }
    }

    @Test
    public void testProcessCollectionComplexEmpty() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-collection-complex-empty.xml");
        session.setSourceDocument("XmlSource", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        assertThat(string).hasXPath("//XmlOE");
        assertThat(string).doesNotHaveXPath("//XmlOE/contactList");
        assertThat(string).doesNotHaveXPath("//XmlOE/contactSAList");
        assertThat(string).doesNotHaveXPath("//XmlOE/contactTAList");
    }

    @Test
    public void testProcessCollectionComplexEmptyItem() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-collection-complex-empty-item.xml");
        session.setSourceDocument("XmlSource", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        assertThat(string).hasXPath("//XmlOE");
        assertThat(string).valueByXPath("count(//XmlOE/contactList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            if (i==1) {
                assertThat(string).doesNotHaveXPath("//XmlOE/contactList[" + (i+1) + "]/firstName");
            } else {
                assertThat(string).valueByXPath("//XmlOE/contactList[" + (i+1) + "]/firstName").isEqualTo("first" + (i+1));
            }
            assertThat(string).doesNotHaveXPath("//XmlOE/contactList[" + (i+1) + "]/lastName");
        }
        assertThat(string).valueByXPath("count(//XmlOE/contactList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            if (i==1) {
                assertThat(string).doesNotHaveXPath("//XmlOE/contactSAList[" + (i+1) + "]/firstName");
            } else {
                assertThat(string).valueByXPath("//XmlOE/contactSAList[" + (i+1) + "]/firstName").isEqualTo("FIRSTSA" + (i+1));
            }
            assertThat(string).doesNotHaveXPath("//XmlOE/contactSAList[" + (i+1) + "]/lastName");
        }
        assertThat(string).valueByXPath("count(//XmlOE/contactTAList)").isEqualTo(3);
        for (int i=0; i<3; i++) {
            if (i==1) {
                assertThat(string).doesNotHaveXPath("//XmlOE/contactTAList[" + (i+1) + "]/firstName");
            } else {
                assertThat(string).valueByXPath("//XmlOE/contactTAList[" + (i+1) + "]/firstName").isEqualTo("FIRSTTA" + (i+1));
            }
            assertThat(string).doesNotHaveXPath("//XmlOE/contactTAList[" + (i+1) + "]/lastName");
        }
    }

}
