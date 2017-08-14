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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.v2.JavaClass;

public class JavaConstructServiceSimpleTest {

    private JavaConstructService constructService = null;

    @Before
    public void setUp() throws Exception {
        constructService = new JavaConstructService();
        constructService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        constructService = null;
    }

    @Test
    public void testConstructString() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("java.lang.String"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof java.lang.String);
        assertEquals("", ((String) targetObject));
        assertEquals(new Integer(0), new Integer(((String) targetObject).length()));
    }

    @Test
    public void testConstructSourceAddress() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("io.atlasmap.java.test.SourceAddress"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.SourceAddress);
        SourceAddress source = (SourceAddress) targetObject;
        assertNull(source.getAddressLine1());
        assertNull(source.getAddressLine2());
        assertNull(source.getCity());
        assertNull(source.getState());
        assertNull(source.getZipCode());
    }

    @Test
    public void testConstructSourceContact() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("io.atlasmap.java.test.SourceContact"));
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof io.atlasmap.java.test.SourceContact);
        SourceContact source = (SourceContact) targetObject;
        assertNull(source.getFirstName());
        assertNull(source.getLastName());
        assertNull(source.getPhoneNumber());
        assertNull(source.getZipCode());
    }

    @Test
    public void testConstructAbstractBaseContactMinimalData() throws Exception {
        try {
            constructService.constructClass(generateJavaClass("io.atlasmap.java.test.BaseContact"));
            fail("InstantiationException expected");
        } catch (InstantiationException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("InstantiationException expected instead: " + e.getMessage());
        }
    }

    protected JavaClass generateJavaClass(String className) {
        JavaClass j = new JavaClass();
        j.setClassName(className);
        return j;
    }
}
