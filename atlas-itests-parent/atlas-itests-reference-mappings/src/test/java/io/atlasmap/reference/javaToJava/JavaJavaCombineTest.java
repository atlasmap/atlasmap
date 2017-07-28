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
package io.atlasmap.reference.javaToJava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJavaCombineTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCombineSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-combine-simple.xml").toURI());
        ((DefaultAtlasContext) context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setInput(sourceContact);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        TargetContact targetContact = (TargetContact) object;
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineSkip() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-combine-skip.xml").toURI());
        ((DefaultAtlasContext) context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setInput(sourceContact);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        TargetContact targetContact = (TargetContact) object;
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }
    
    @Test
    public void testProcessCombineOutOfOrder() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-combine-outoforder.xml").toURI());
        ((DefaultAtlasContext) context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        session.setInput(sourceContact);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        TargetContact targetContact = (TargetContact) object;
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }
    
    
    @Test
    @Ignore // TODO: see: https://github.com/atlasmap/atlasmap/issues/107
    public void testProcessCombineNullInput() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-combine-inputnull.xml").toURI());
        ((DefaultAtlasContext) context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setLastName(null);
        session.setInput(sourceContact);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        TargetContact targetContact = (TargetContact) object;
        assertNotNull(targetContact);
        assertEquals("Ozzie 5551212 81111", targetContact.getFirstName());
        assertFalse(session.hasErrors());
    }
}
