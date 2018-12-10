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
package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetParentOrder;

public class JavaFieldWriterUtilInstantiateObjectTest extends BaseJavaFieldWriterTest {

    @Test
    public void testString() throws Exception {
        Object targetObject = writerUtil.instantiateObject(writerUtil.loadClass("java.lang.String"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof java.lang.String);
        assertEquals("", targetObject);
    }

    @Test
    public void testSourceAddress() throws Exception {
        Object targetObject = writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.SourceAddress"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof SourceAddress);
        SourceAddress source = (SourceAddress) targetObject;
        assertNull(source.getAddressLine1());
        assertNull(source.getAddressLine2());
        assertNull(source.getCity());
        assertNull(source.getState());
        assertNull(source.getZipCode());
    }

    @Test
    public void testSourceContact() throws Exception {
        Object targetObject = writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.SourceContact"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof SourceContact);
        SourceContact source = (SourceContact) targetObject;
        assertNull(source.getFirstName());
        assertNull(source.getLastName());
        assertNull(source.getPhoneNumber());
        assertNull(source.getZipCode());
    }

    @Test
    public void testObjectArray() throws Exception {
        Object targetObject = writerUtil.instantiateObject(Object[].class);
        assertNotNull(targetObject);
        assertEquals(Object[].class, targetObject.getClass());
    }

    @Test(expected=AtlasException.class)
    public void testAbstractBaseContact() throws Exception {
        writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.BaseContact"));
    }


    @Test(expected = AtlasException.class)
    public void testNullClass() throws Exception {
        Object created = writerUtil.instantiateObject(null);
        fail(created.getClass().getName());
    }

    @Test
    public void testtPrimitives() {
        for (String prim : DefaultAtlasConversionService.listPrimitiveClassNames()) {
            try {
                writerUtil.loadClass(prim);
                fail("AtlasException expected");
            } catch (AtlasException e) {
                assertTrue(e.getMessage().contains("java.lang.ClassNotFoundException"));
            }
        }
    }

    @Test
    public void testTargetOrder() throws Exception {
        Object targetObject = writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.TargetOrder"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof TargetOrder);
        TargetOrder order = (TargetOrder) targetObject;
        assertNull(order.getAddress());
        assertNull(order.getContact());
    }

    @Test
    public void testTargetParentOrder() throws Exception {
        Object targetObject = writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.TargetParentOrder"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof TargetParentOrder);
        TargetParentOrder parentOrder = (TargetParentOrder) targetObject;
        assertNull(parentOrder.getOrder());
    }

    @Test(expected=AtlasException.class)
    public void testAbstractBaseOrder() throws Exception {
        writerUtil.instantiateObject(writerUtil.loadClass("io.atlasmap.java.test.BaseOrder"));
    }

}
