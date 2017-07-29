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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.LinkedList;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.StateEnumClassShort;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJavaComplexTest extends AtlasMappingBaseTest {
	
	@Test
    public void testProcessBasic() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-basic.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        Object object = session.getOutput();
        assertEquals(TargetOrder.class.getName(), object.getClass().getName());
        TargetOrder targetOrder = (TargetOrder)object;
        assertEquals(new Integer(8765309), targetOrder.getOrderId());
	}
	
	@Test
    public void testProcessComplexBasic() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-simple.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(TargetContact.class.getName(), object.getContact().getClass().getName());
        assertEquals("Ozzie", object.getContact().getFirstName());
	}
	
	@Test
    public void testProcessCollectionList() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-list.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactList().size());
        for (int i = 0; i < 20; i++) {
        	TargetContact contact = (TargetContact) object.getContactList().get(i);
        	if (i == 4 || i == 19) {        		
        		assertEquals("Ozzie", contact.getFirstName());
        	} else {
        		assertNull(contact);
        	}
        }
	}
	
	@Test
    public void testProcessCollectionArray() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-array.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactArray().length);
        for (int i = 0; i < 20; i++) {
        	TargetContact contact = (TargetContact) object.getContactArray()[i];
        	if (i == 6 || i == 19) {        		
        		assertEquals("Ozzie", contact.getFirstName());
        	} else {
        		assertNull(contact);
        	}
        }
	}
	
	@Test 
	public void testProcessCollectionListSimple() throws Exception {
	    AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-list-simple.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }	    
	}
	
	@Test 
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-array-simple.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }       
    }
	
	@Test 
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-to-noncollection.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertNull(object.getContactArray());
        assertNull(object.getContactList());
        assertEquals("fname4", object.getContact().getFirstName());
    }
	
	@Test 
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-from-noncollection.xml").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContact(new TargetContact());
        input.getContact().setFirstName("first name");
        input.getContact().setLastName("last name");

        AtlasSession session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);
        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertEquals(1, object.getContactList().size());
        assertEquals("first name", object.getContactList().get(0).getFirstName());
        assertEquals("last name", object.getContactList().get(0).getLastName());
    }
    	
	@Test
    public void testProcessLookup() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-lookup.xml").toURI());
        
        TargetTestClass input = new TargetTestClass();
        
        input.setStatesLong(StateEnumClassLong.Arizona);
        AtlasSession session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);        
        TargetTestClass object = (TargetTestClass) session.getOutput();
        assertNotNull(object);
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(StateEnumClassShort.AZ, object.getStatesShort());
        
        input.setStatesLong(StateEnumClassLong.Alabama);
        session = context.createSession();
        session.setInput(input, "io.atlasmap.java.test.TargetTestClass");
        context.process(session);        
        object = (TargetTestClass) session.getOutput();
        assertNotNull(object);
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertNull(object.getStatesShort());
    }		

    @Test
    public void testProcessJavaJavaComplexWithAbstractBasic() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-abstract.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        TargetOrder targetOrder = (TargetOrder)object;
        assertNotNull(targetOrder.getOrderId());
        assertEquals(new Integer(8765309), targetOrder.getOrderId());
        
        // Address should _not_ be populated
        assertNull(targetOrder.getAddress());
        
        // Contact should only have firstName populated
        assertNotNull(targetOrder.getContact());
        assertTrue(targetOrder.getContact() instanceof TargetContact);
        TargetContact targetContact = (TargetContact)targetOrder.getContact();
        assertNotNull(targetContact.getFirstName());
        assertEquals("Ozzie", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());     
    }
    
    @Test
    public void testProcessJavaJavaComplexAutoDetectFull() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-autodetect-full.xml").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        AtlasTestUtil.validateOrder((TargetOrder)object);
    }
    
    
    @Test
    public void testProcessJavaJavaComplexAutoDetectFullActions() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-autodetect-full-actions.xml"));
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
    }
}
