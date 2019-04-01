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
package io.atlasmap.itests.reference.java_to_xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class JavaXmlCombineTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCombineSimple() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-combine-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertThat(object).valueByXPath("/Contact/@firstName").isEqualTo("Ozzie    Smith   5551212                                                                                            81111");
        assertThat(object).valueByXPath("/Contact/@lastName").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@phoneNumber").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@zipCode").isNullOrEmpty();
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineSkip() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-combine-skip.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertThat(object).valueByXPath("/Contact/@firstName").isEqualTo("Ozzie Smith 5551212 81111");
        assertThat(object).valueByXPath("/Contact/@lastName").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@phoneNumber").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@zipCode").isNullOrEmpty();
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineOutOfOrder() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-combine-outoforder.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertThat(object).valueByXPath("/Contact/@firstName").isEqualTo("Ozzie Smith 5551212 81111");
        assertThat(object).valueByXPath("/Contact/@lastName").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@phoneNumber").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@zipCode").isNullOrEmpty();
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineNullInput() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-combine-inputnull.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertThat(object).valueByXPath("/Contact/@firstName").isEqualTo("Ozzie  5551212 81111");
        assertFalse(session.hasErrors());
    }
}
