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
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;

public class JavaConstructServiceTest {

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
    public void testConstructClassInvalid() {
        try {
            constructService.constructClass(generateJavaClassInvalidNull());
            fail("ConstructException expected");
        } catch (ConstructInvalidException e) {
            assertEquals("JavaClass cannot be null", e.getMessage());
        } catch (Exception e) {
            fail("Expected ConstructException instead: " + e.getMessage());
        }
        try {
            constructService.constructClass(generateJavaClassInvalidNullClassName());
            fail("ConstructException expected");
        } catch (ConstructInvalidException e) {
            assertEquals("JavaClass.className must be specified", e.getMessage());
        } catch (Exception e) {
            fail("Expected ConstructException instead: " + e.getMessage());
        }
        try {
            constructService.constructClass(generateJavaClassInvalidEmptyClassName());
            fail("ConstructException expected");
        } catch (ConstructInvalidException e) {
            assertEquals("JavaClass.className must be specified", e.getMessage());
        } catch (Exception e) {
            fail("Expected ConstructException instead: " + e.getMessage());
        }
    }

    @Test
    public void testConstructClassPrimitives() throws Exception {

        Object object = null;
        for (String prim : DefaultAtlasConversionService.listPrimitiveClassNames()) {
            try {
                object = constructService.constructClass(generatePrimitive(prim));
                fail("ConstructPrimitiveException expected");
            } catch (ConstructPrimitiveException e) {
                assertEquals("Unable to instantiate a Java primitive: " + prim, e.getMessage());
            } catch (Exception e) {
                fail("ConstructException expected instead: " + e.getMessage());
            }
        }

        assertNull(object);
    }

    @Test
    public void testConstructClassCollectionArray() throws Exception {
        Object stringArray = constructService.constructClass(
                generateJavaClassCollection("java.lang.String", CollectionType.ARRAY, new Integer(1), new Integer(3)));
        assertNotNull(stringArray);
        assertTrue(stringArray instanceof java.lang.String[]);
        assertEquals(new Integer(3), new Integer(((String[]) stringArray).length));
    }

    @Test
    public void testConstructClassCollectionList() throws Exception {
        JavaClass javaClass = generateJavaClassCollection("java.lang.String", CollectionType.LIST, null, null);
        javaClass.setCollectionClassName("java.util.ArrayList");
        Object stringList = constructService.constructClass(javaClass);

        assertNotNull(stringList);
        assertTrue(stringList instanceof ArrayList<?>);
        assertTrue(((List<?>) stringList).isEmpty());
    }

    @Test
    public void testConstructClassCollectionMap() throws Exception {
        JavaClass javaClass = generateJavaClassCollection("java.lang.String", CollectionType.MAP, null, null);
        javaClass.setCollectionClassName("java.util.HashMap");
        Object stringMap = constructService.constructClass(javaClass);
        assertNotNull(stringMap);
        assertTrue(stringMap instanceof HashMap<?, ?>);
        assertTrue(((HashMap<?, ?>) stringMap).isEmpty());
    }

    @Test
    public void testConstructClassCollectionUnsupported() throws Exception {
        try {
            constructService
                    .constructClass(generateJavaClassCollection("java.lang.String", CollectionType.ALL, null, null));
            fail("ConstructUnsupportedException expected");
        } catch (ConstructUnsupportedException e) {
            assertEquals(String.format("Unsupported collectionType for instantiation c=%s cType=%s", "java.lang.String",
                    CollectionType.ALL.value()), e.getMessage());
        } catch (Exception e) {
            fail("Expected ConstructException instead: " + e.getMessage());
        }

        try {
            constructService
                    .constructClass(generateJavaClassCollection("java.lang.String", CollectionType.NONE, null, null));
            fail("ConstructUnsupportedException expected");
        } catch (ConstructUnsupportedException e) {
            assertEquals(String.format("Unsupported collectionType for instantiation c=%s cType=%s", "java.lang.String",
                    CollectionType.NONE.value()), e.getMessage());
        } catch (Exception e) {
            fail("Expected ConstructException instead: " + e.getMessage());
        }
    }

    protected JavaClass generateJavaClassInvalidNull() {
        return null;
    }

    protected JavaClass generateJavaClassInvalidNullClassName() {
        JavaClass j = new JavaClass();
        j.setClassName(null);
        return j;
    }

    protected JavaClass generateJavaClassInvalidEmptyClassName() {
        JavaClass j = new JavaClass();
        j.setClassName("");
        return j;
    }

    protected JavaClass generatePrimitive(String className) {
        JavaClass j = new JavaClass();
        j.setClassName(className);
        return j;
    }

    protected JavaClass generateJavaClassCollection(String className, CollectionType collectionType,
            Integer arrayDimensions, Integer arraySize) {
        JavaClass j = new JavaClass();
        j.setClassName(className);
        j.setCollectionType(collectionType);

        if (CollectionType.ARRAY.equals(collectionType)) {
            j.setArrayDimensions(arrayDimensions);
            j.setArraySize(arraySize);
        }
        return j;
    }

}
