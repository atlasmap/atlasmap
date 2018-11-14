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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    public void setUp() {
        constructService = new JavaConstructService();
        constructService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        constructService = null;
    }

    @Test
    public void testConstructString() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("java.lang.String"),null);
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof java.lang.String);
        assertEquals("", targetObject);
        assertEquals(new Integer(0), new Integer(((String) targetObject).length()));
    }

    @Test
    public void testConstructSourceAddress() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("io.atlasmap.java.test.SourceAddress"),null);
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
    public void testConstructSourceContact() throws Exception {
        Object targetObject = constructService.constructClass(generateJavaClass("io.atlasmap.java.test.SourceContact"),null);
        assertNotNull(targetObject);
        assertTrue(targetObject instanceof SourceContact);
        SourceContact source = (SourceContact) targetObject;
        assertNull(source.getFirstName());
        assertNull(source.getLastName());
        assertNull(source.getPhoneNumber());
        assertNull(source.getZipCode());
    }

    @Test(expected=InstantiationException.class)
    public void testConstructAbstractBaseContactMinimalData() throws Exception {
        constructService.constructClass(generateJavaClass("io.atlasmap.java.test.BaseContact"),null);
    }

    protected JavaClass generateJavaClass(String className) {
        JavaClass j = new JavaClass();
        j.setClassName(className);
        return j;
    }
}
