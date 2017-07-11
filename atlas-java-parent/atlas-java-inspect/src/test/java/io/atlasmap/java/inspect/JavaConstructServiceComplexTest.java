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
package io.atlasmap.java.inspect;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.SourceParentOrder;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetParentOrder;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;

public class JavaConstructServiceComplexTest {

    private JavaConstructService constructService = null;
    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() throws Exception {
        constructService = new JavaConstructService();
        constructService.setConversionService(DefaultAtlasConversionService.getRegistry());
        
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getRegistry());
    }

    @After
    public void tearDown() throws Exception {
        constructService = null;
        classInspectionService = null;
    }

    @Test
    public void testConstructSourceOrder() throws Exception {
        Object targetObject = constructService.constructClass(generateOrder("Source"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.SourceOrder);
        SourceOrder order = (SourceOrder)targetObject;
        
        assertNotNull(order.getAddress());
        assertTrue(order.getAddress() instanceof io.atlasmap.java.test.SourceAddress);
        SourceAddress address = (SourceAddress)order.getAddress();
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getZipCode());
        
        assertNotNull(order.getContact());
        assertTrue(order.getContact() instanceof io.atlasmap.java.test.SourceContact);
        SourceContact contact = (SourceContact)order.getContact();
        assertNull(contact.getFirstName());
        assertNull(contact.getLastName());
        assertNull(contact.getPhoneNumber());
        assertNull(contact.getZipCode());
    }
    
    @Test
    public void testConstructTargetOrder() throws Exception {
        Object targetObject = constructService.constructClass(generateOrder("Target"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.TargetOrder);
        TargetOrder order = (TargetOrder)targetObject;
        
        assertNotNull(order.getAddress());
        assertTrue(order.getAddress() instanceof io.atlasmap.java.test.TargetAddress);
        TargetAddress address = (TargetAddress)order.getAddress();
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getZipCode());
        
        assertNotNull(order.getContact());
        assertTrue(order.getContact() instanceof io.atlasmap.java.test.TargetContact);
        TargetContact contact = (TargetContact)order.getContact();
        assertNull(contact.getFirstName());
        assertNull(contact.getLastName());
        assertNull(contact.getPhoneNumber());
        assertNull(contact.getZipCode());
    }
    
    @Test
    public void testConstructTargetOrderFiltered() throws Exception {
        JavaClass javaClass = generateOrder("Target");
        Object targetObject = constructService.constructClass(javaClass, Arrays.asList("address"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.TargetOrder);
        TargetOrder order = (TargetOrder)targetObject;
        
        assertNotNull(order.getAddress());
        assertTrue(order.getAddress() instanceof io.atlasmap.java.test.TargetAddress);
        TargetAddress address = (TargetAddress)order.getAddress();
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getZipCode());
        
        assertNull(order.getContact());
    }
    
    @Test
    public void testConstructSourceParentOrder() throws Exception {
        JavaClass javaClass = generateParentOrder("Source");
        Object targetObject = constructService.constructClass(javaClass);
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.SourceParentOrder);
        SourceParentOrder parentOrder = (SourceParentOrder)targetObject;
        
        assertNotNull(parentOrder.getOrder());
        SourceOrder order = (SourceOrder)parentOrder.getOrder();
        
        assertNotNull(order.getAddress());
        assertTrue(order.getAddress() instanceof io.atlasmap.java.test.SourceAddress);
        SourceAddress address = (SourceAddress)order.getAddress();
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getZipCode());
        
        assertNotNull(order.getContact());
        assertTrue(order.getContact() instanceof io.atlasmap.java.test.SourceContact);
        SourceContact contact = (SourceContact)order.getContact();
        assertNull(contact.getFirstName());
        assertNull(contact.getLastName());
        assertNull(contact.getPhoneNumber());
        assertNull(contact.getZipCode());
    }
    
    @Test
    public void testConstructTargetParentOrder() throws Exception {
        JavaClass javaClass = generateParentOrder("Target");
        Object targetObject = constructService.constructClass(javaClass);
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.TargetParentOrder);
        TargetParentOrder parentOrder = (TargetParentOrder)targetObject;
        
        assertNotNull(parentOrder.getOrder());
        TargetOrder order = (TargetOrder)parentOrder.getOrder();
        
        assertNotNull(order.getAddress());
        assertTrue(order.getAddress() instanceof io.atlasmap.java.test.TargetAddress);
        TargetAddress address = (TargetAddress)order.getAddress();
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getCity());
        assertNull(address.getState());
        assertNull(address.getZipCode());
        
        assertNotNull(order.getContact());
        assertTrue(order.getContact() instanceof io.atlasmap.java.test.TargetContact);
        TargetContact contact = (TargetContact)order.getContact();
        assertNull(contact.getFirstName());
        assertNull(contact.getLastName());
        assertNull(contact.getPhoneNumber());
        assertNull(contact.getZipCode());
    }
        
    @Test
    public void testConstructAbstractBaseOrder() throws Exception {
        try {
            constructService.constructClass(generateOrder("Base"));
            fail("Expected ConstructInvalidException");
            /*
            } catch (ConstructInvalidException e) {
               TODO: Fix modifiers problem 
            } 
            */ 
        } catch (InstantiationException e) {
       
        } catch (Exception e) {
            fail("Expected ConstructInvalidException instead: " + e.getClass().getName());
        }
    }
    
    protected JavaClass generateOrder(String prefix) {
        JavaClass j = classInspectionService.inspectClass("io.atlasmap.java.test." + prefix + "Order");
        
        for(JavaField jf : j.getJavaFields().getJavaField()) {
            if(jf.getPath().equals("contact")) {
                jf.setClassName("io.atlasmap.java.test." + prefix + "Contact");
            }
            if(jf.getPath().equals("address")) {
                jf.setClassName("io.atlasmap.java.test." + prefix + "Address");
            }
        }
        
        return j;
    }
    
    protected JavaClass generateParentOrder(String prefix) {
        JavaClass j = classInspectionService.inspectClass("io.atlasmap.java.test." + prefix + "ParentOrder");
        
        for(JavaField jf : j.getJavaFields().getJavaField()) {
            if(jf.getPath().equals("order")) {
                jf.setClassName("io.atlasmap.java.test." + prefix + "Order");
            }
            
            if(jf instanceof JavaClass) {
                for(JavaField cjf : ((JavaClass)jf).getJavaFields().getJavaField()) {
                    if(cjf.getPath().equals("order.contact")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Contact");
                    }
                    if(cjf.getPath().equals("order.address")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Address");
                    }
                }
            }
        }
        
        return j;
    }
}
