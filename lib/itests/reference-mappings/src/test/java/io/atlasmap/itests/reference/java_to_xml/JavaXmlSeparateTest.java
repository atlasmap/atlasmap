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

public class JavaXmlSeparateTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessSeparateSimple() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-separate-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName("Ozzie Smith");
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);

        assertTrue(object instanceof String);
        AtlasTestUtil.validateXmlContactAttributeNoNS(object);
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessSeparateSkip() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-separate-skip.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName("Dr. Mr. Ozzie L. Smith Jr.");
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasTestUtil.validateXmlContactAttributeNoNS(object);
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessSeparateOutOfOrder() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-separate-outoforder.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName("Dr. Mr. Ozzie L. Smith Jr.");
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasTestUtil.validateXmlContactAttributeNoNS(object);
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessSeparateNotEnoughSource() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-separate-inputshort.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName("Dr. Mr. Ozzie");
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);

        assertTrue(object instanceof String);
        assertThat(object).valueByXPath("/Contact/@firstName").isEqualTo("Ozzie");
        assertThat(object).valueByXPath("/Contact/@lastName").isNullOrEmpty();
        assertTrue(session.hasWarns());
        assertEquals(
                "Separate returned fewer segments count=3 when targetField.path=/Contact/@lastName requested index=3",
                session.getAudits().getAudit().get(0).getMessage());
    }

    @Test
    public void testProcessSeparateNullSource() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToXml/atlasmapping-separate-inputnull.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName(null);
        sourceContact.setLastName(null);
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);

        assertTrue(object instanceof String);
        assertFalse(session.hasErrors());

        assertThat(object).valueByXPath("/Contact/@firstName").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@lastName").isNullOrEmpty();
        assertThat(object).valueByXPath("/Contact/@phoneNumber").isEqualTo("5551212");
        assertThat(object).valueByXPath("/Contact/@zipCode").isEqualTo("81111");

        assertFalse(session.hasErrors());
    }
}
