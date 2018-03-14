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
package io.atlasmap.reference.java_to_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.LinkedList;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJavaCollectionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionList() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-list.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactList().size());
        for (int i = 0; i < 20; i++) {
            TargetContact contact = object.getContactList().get(i);
            if (i == 4 || i == 19) {
                assertEquals("Ozzie", contact.getFirstName());
            } else {
                assertNull(contact);
            }
        }
    }

    @Test
    public void testProcessCollectionArray() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-array.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactArray().length);
        for (int i = 0; i < 20; i++) {
            TargetContact contact = object.getContactArray()[i];
            if (i == 6 || i == 19) {
                assertEquals("Ozzie", contact.getFirstName());
            } else {
                assertNull(contact);
            }
        }
    }

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-list-simple.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-array-simple.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-to-noncollection.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertNull(object.getContactArray());
        assertNull(object.getContactList());
        assertEquals("fname4", object.getContact().getFirstName());
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-from-noncollection.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContact(new TargetContact());
        input.getContact().setFirstName("first name");
        input.getContact().setLastName("last name");

        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(1, object.getContactList().size());
        assertEquals("first name", object.getContactList().get(0).getFirstName());
        assertEquals("last name", object.getContactList().get(0).getLastName());
    }

    @Test
    public void testProcessCollectionPrimitive() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-flatprimitive.xml").toURI());
        SourceFlatPrimitiveClass source = new SourceFlatPrimitiveClass();
        source.setBoxedStringField("fuga");
        source.setBoxedStringArrayField(new String[] {"foo", "bar", "hoge", "fuga"});
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetFlatPrimitiveClass target = (TargetFlatPrimitiveClass) session.getDefaultTargetDocument();
        assertEquals("fuga", target.getBoxedStringField());
        assertEquals(1, target.getBoxedStringArrayField().length);
        assertEquals("fuga", target.getBoxedStringArrayField()[0]);
    }
}
