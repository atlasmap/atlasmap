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
package io.atlasmap.itests.reference.java_to_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class JavaJavaCombineTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCombineSimple() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/javaToJava/atlasmapping-combine-simple.json",
                AtlasTestUtil.generateContact(SourceContact.class));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie    Smith   5551212                                                                                            81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
    }

    @Test
    public void testProcessCombineSkip() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/javaToJava/atlasmapping-combine-skip.json",
                AtlasTestUtil.generateContact(SourceContact.class));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
    }

    @Test
    public void testProcessCombineOutOfOrder() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/javaToJava/atlasmapping-combine-outoforder.json",
                AtlasTestUtil.generateContact(SourceContact.class));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
    }

    @Test
    public void testProcessCombineNullInput() throws Exception {
        BaseContact source = AtlasTestUtil.generateContact(SourceContact.class);
        source.setLastName(null);
        AtlasSession session = processCombineMapping(
                "src/test/resources/javaToJava/atlasmapping-combine-inputnull.json", source);
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertNotNull(targetContact);
        assertEquals("Ozzie  5551212 81111", targetContact.getFirstName());
    }

    protected AtlasSession processCombineMapping(String mappingFile, BaseContact sourceContact) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        return session;
    }
}
