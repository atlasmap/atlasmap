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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.StateEnumClassShort;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetTestClass;

public class JavaJavaComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-basic.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertEquals(TargetOrder.class.getName(), object.getClass().getName());
        TargetOrder targetOrder = (TargetOrder) object;
        assertEquals(new Integer(8765309), targetOrder.getOrderId());
    }

    @Test
    public void testProcessComplexBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(TargetContact.class.getName(), object.getContact().getClass().getName());
        assertEquals("Ozzie", object.getContact().getFirstName());
    }

    @Test
    public void testProcessComplexBasicNullContact() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        sourceOrder.setContact(null);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(TargetContact.class.getName(), object.getContact().getClass().getName());
        assertNull(object.getContact().getFirstName());
    }

    @Test
    public void testProcessLookup() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-lookup.json").toURI());

        TargetTestClass input = new TargetTestClass();

        input.setStatesLong(StateEnumClassLong.Arizona);
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertNotNull(object);
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(StateEnumClassShort.AZ, object.getStatesShort());

        input.setStatesLong(StateEnumClassLong.Alabama);
        session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);
        object = (TargetTestClass) session.getDefaultTargetDocument();
        assertNotNull(object);
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertNull(object.getStatesShort());
        assertTrue(printAudit(session), session.hasErrors());
    }

    @Test
    public void testProcessJavaJavaComplexWithAbstractBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-abstract.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        TargetOrder targetOrder = (TargetOrder) object;
        assertNotNull(targetOrder.getOrderId());
        assertEquals(new Integer(8765309), targetOrder.getOrderId());

        // Address should _not_ be populated
        assertNull(targetOrder.getAddress());

        // Contact should only have firstName populated
        assertNotNull(targetOrder.getContact());
        assertTrue(targetOrder.getContact() instanceof TargetContact);
        TargetContact targetContact = (TargetContact) targetOrder.getContact();
        assertNotNull(targetContact.getFirstName());
        assertEquals("Ozzie", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
    }

    @Test
    public void testProcessJavaJavaComplexAutoDetectFull() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-complex-autodetect-full.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        AtlasTestUtil.validateOrder((TargetOrder) object);
    }

    @Test
    public void testProcessJavaJavaComplexAutoDetectFullActions() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-complex-autodetect-full-actions.json"));
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        // ensure our Uppercase action on first name did the right thing
        assertEquals("OZZIE", ((TargetOrder) object).getContact().getFirstName());
        assertEquals("SmithXXX", ((TargetOrder) object).getContact().getLastName());
        // set values to normalized pre-action-processing state so rest of validation
        // passes..
        ((TargetOrder) object).getContact().setFirstName("Ozzie");
        ((TargetOrder) object).getContact().setLastName("Smith");
        AtlasTestUtil.validateOrder((TargetOrder) object);
    }
}
