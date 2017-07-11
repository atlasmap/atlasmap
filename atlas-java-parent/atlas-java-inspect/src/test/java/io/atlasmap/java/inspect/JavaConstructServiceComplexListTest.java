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
import java.util.ArrayList;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.PopulatedListOrderList;
import io.atlasmap.java.test.SourceOrderList;
import io.atlasmap.java.test.TargetOrderList;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;

public class JavaConstructServiceComplexListTest {

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
    public void testConstructSourceOrderList() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderList("Source"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.SourceOrderList);
        SourceOrderList orderList = (SourceOrderList)targetObject;
        assertNotNull(orderList.getOrders());
        assertTrue(orderList.getOrders().isEmpty());
        assertNull(orderList.getNumberOrders());
        assertNull(orderList.getOrderBatchNumber());
    }
    
    @Test
    public void testConstructTargetOrderList() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderList("Target"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.TargetOrderList);
        TargetOrderList orderList = (TargetOrderList)targetObject;
        assertNotNull(orderList.getOrders());
        assertTrue(orderList.getOrders().isEmpty());
        assertNull(orderList.getNumberOrders());
        assertNull(orderList.getOrderBatchNumber());
    }
    
    @Test
    public void testConstructPopulatedOrderList() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderList("PopulatedList"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.PopulatedListOrderList);
        PopulatedListOrderList orderList = (PopulatedListOrderList)targetObject;
        assertNotNull(orderList.getOrders());
        assertTrue(orderList.getOrders().isEmpty());
        assertNull(orderList.getNumberOrders());
        assertNull(orderList.getOrderBatchNumber());
        assertEquals(Vector.class.getName(), orderList.getOrders().getClass().getName());
    }
    
    @Test
    public void testConstructTargetOrderListFiltered() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderList("Target"), new ArrayList<String>());
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.TargetOrderList);
        TargetOrderList orderList = (TargetOrderList)targetObject;
        assertNotNull(orderList.getOrders());
        assertTrue(orderList.getOrders().isEmpty());
        assertNull(orderList.getNumberOrders());
    }
        
    @Test
    public void testConstructAbstractBaseOrderArray() throws Exception {
        try {
            constructService.constructClass(generateOrderList("Base"));
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
    
    protected JavaClass generateOrderList(String prefix) {
        JavaClass j = classInspectionService.inspectClass("io.atlasmap.java.test." + prefix + "OrderList");
        
        for(JavaField jf : j.getJavaFields().getJavaField()) {
            if(jf.getPath().equals("orders")) {
                jf.setClassName("io.atlasmap.java.test." + prefix + "Order");
                jf.setCollectionClassName("java.util.ArrayList");
            }
            if(jf instanceof JavaClass) {
                for(JavaField cjf : ((JavaClass)jf).getJavaFields().getJavaField()) {
                    if(cjf.getPath().equals("orders.contact")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Contact");
                    }
                    if(cjf.getPath().equals("orders.address")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Address");
                    }
                }
            }
        }
        
        return j;
    }
}
