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

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.SourceOrderArray;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JavaConstructServiceComplexArrayTest {

    private JavaConstructService constructService = null;
    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() throws Exception {
        constructService = new JavaConstructService();
        constructService.setConversionService(DefaultAtlasConversionService.getInstance());

        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        constructService = null;
        classInspectionService = null;
    }

    @Test
    public void testConstructSourceOrderArray() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderArray("Source"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof SourceOrderArray);
        SourceOrderArray orderArray = (SourceOrderArray) targetObject;
        assertNotNull(orderArray.getOrders());
        assertTrue(orderArray.getOrders().length > 0);

        for (int i = 0; i < orderArray.getOrders().length; i++) {
            SourceOrder order = (SourceOrder) orderArray.getOrders()[i];
            assertNotNull(order.getAddress());
            assertTrue(order.getAddress() instanceof SourceAddress);
            SourceAddress address = (SourceAddress) order.getAddress();
            assertNull(address.getAddressLine1());
            assertNull(address.getAddressLine2());
            assertNull(address.getCity());
            assertNull(address.getState());
            assertNull(address.getZipCode());

            assertNotNull(order.getContact());
            assertTrue(order.getContact() instanceof SourceContact);
            SourceContact contact = (SourceContact) order.getContact();
            assertNull(contact.getFirstName());
            assertNull(contact.getLastName());
            assertNull(contact.getPhoneNumber());
            assertNull(contact.getZipCode());
        }
    }

    @Test
    public void testConstructTargetOrderArray() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderArray("Target"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof TargetOrderArray);
        TargetOrderArray orderArray = (TargetOrderArray) targetObject;

        for (int i = 0; i < orderArray.getOrders().length; i++) {
            TargetOrder order = (TargetOrder) orderArray.getOrders()[i];
            assertNotNull(order);
            assertNotNull(order.getAddress());
            assertTrue(order.getAddress() instanceof TargetAddress);
            TargetAddress address = (TargetAddress) order.getAddress();
            assertNull(address.getAddressLine1());
            assertNull(address.getAddressLine2());
            assertNull(address.getCity());
            assertNull(address.getState());
            assertNull(address.getZipCode());

            assertNotNull(order.getContact());
            assertTrue(order.getContact() instanceof TargetContact);
            TargetContact contact = (TargetContact) order.getContact();
            assertNull(contact.getFirstName());
            assertNull(contact.getLastName());
            assertNull(contact.getPhoneNumber());
            assertNull(contact.getZipCode());
        }

    }

    @Test
    public void testConstructTargetOrderArrayFiltered() throws Exception {
        Object targetObject = constructService.constructClass(generateOrderArray("Target"),
                Arrays.asList("orders", "orders/address"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof TargetOrderArray);
        TargetOrderArray orderArray = (TargetOrderArray) targetObject;
        assertNotNull(orderArray.getOrders());
        assertTrue(orderArray.getOrders().length > 0);
        assertNull(orderArray.getNumberOrders());

        for (int i = 0; i < orderArray.getOrders().length; i++) {
            TargetOrder order = (TargetOrder) orderArray.getOrders()[i];
            assertNotNull(order.getAddress());
            assertTrue(order.getAddress() instanceof TargetAddress);
            TargetAddress address = (TargetAddress) order.getAddress();
            assertNull(address.getAddressLine1());
            assertNull(address.getAddressLine2());
            assertNull(address.getCity());
            assertNull(address.getState());
            assertNull(address.getZipCode());

            assertNull(order.getContact());
        }
    }

    @Test(expected=InstantiationException.class)
    public void testConstructAbstractBaseOrderArray() throws Exception {
        constructService.constructClass(generateOrderArray("Base"));
    }

    protected JavaClass generateOrderArray(String prefix) {
        JavaClass j = classInspectionService.inspectClass("io.atlasmap.java.test." + prefix + "OrderArray");

        for (JavaField jf : j.getJavaFields().getJavaField()) {
            if (jf.getPath().equals("orders")) {
                jf.setClassName("io.atlasmap.java.test." + prefix + "Order");
                jf.setArraySize(new Integer(3));
            }
            if (jf instanceof JavaClass) {
                for (JavaField cjf : ((JavaClass) jf).getJavaFields().getJavaField()) {
                    if (cjf.getPath().equals("orders/contact")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Contact");
                    }
                    if (cjf.getPath().equals("orders/address")) {
                        cjf.setClassName("io.atlasmap.java.test." + prefix + "Address");
                    }
                }
            }
        }

        return j;
    }
}
