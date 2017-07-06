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

import static org.junit.Assert.*;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJavaComplexTest {

    protected AtlasContextFactory atlasContextFactory = null;

    @Before
    public void setUp() {
        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    }

    @After
    public void tearDown() {
        atlasContextFactory = null;
    }

    @Test
    public void testProcessJavaJavaComplexBasic() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-complex-basic.xml").toURI());
        ((DefaultAtlasContext)context).setNewProcessFlow(true);
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
        ((DefaultAtlasContext)context).setNewProcessFlow(true);
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
        ((DefaultAtlasContext)context).setNewProcessFlow(true);
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class, SourceContact.class);
        session.setInput(sourceOrder);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        AtlasTestUtil.validateOrder((TargetOrder)object);
    }
}
