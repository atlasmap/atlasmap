/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

public class TestListOrdersTest {

    private ClassInspectionService classInspectionService = null;

    @BeforeEach
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @AfterEach
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testInspectJavaList() {
        JavaClass c = classInspectionService.inspectClass(TestListOrders.class, CollectionType.NONE, null);
        assertNotNull(c);
        assertNull(c.getAnnotations());
        assertNull(c.getArrayDimensions());
        assertEquals("io.atlasmap.java.test.TestListOrders", c.getClassName());
        assertNull(c.getCollectionClassName());
        assertEquals(CollectionType.NONE, c.getCollectionType());
        assertNull(c.getGetMethod());
        assertNotNull(c.getJavaEnumFields());
        assertNotNull(c.getJavaEnumFields().getJavaEnumField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(c.getJavaEnumFields().getJavaEnumField().size()));
        assertNotNull(c.getJavaFields());
        assertNotNull(c.getJavaFields().getJavaField());
        assertNull(c.getName());
        assertEquals("io.atlasmap.java.test", c.getPackageName());
        assertNull(c.getSetMethod());
        assertEquals(FieldType.COMPLEX, c.getFieldType());
        assertNotNull(c.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.TestListOrders"),
                c.getUri());
        assertNull(c.getValue());
        assertEquals(Integer.valueOf(2), new Integer(c.getJavaFields().getJavaField().size()));

        JavaField f1 = c.getJavaFields().getJavaField().get(0);
        assertNotNull(f1);
        assertTrue(f1 instanceof JavaClass);
        JavaClass c2 = (JavaClass) f1;

        assertNotNull(c2.getCollectionClassName());
        assertEquals("java.util.List", c2.getCollectionClassName());
        assertEquals(CollectionType.LIST, c2.getCollectionType());

        boolean foundAddress = false;
        boolean foundContact = false;
        boolean foundCreated = false;
        boolean foundSomeStatic = false;

        for (JavaField c2f : c2.getJavaFields().getJavaField()) {
            if (c2f instanceof JavaClass) {
                if ("io.atlasmap.java.test.BaseAddress".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateSimpleTestAddress(((JavaClass) c2f));
                    foundAddress = true;
                } else if ("io.atlasmap.java.test.BaseContact".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateSimpleTestContact(((JavaClass) c2f));
                    foundContact = true;
                } else if ("java.util.Date".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateCreated(c2f);
                    foundCreated = true;
                } else if ("io.atlasmap.java.test.BaseOrder$SomeStaticClass".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateSomeStaticClass(c2f);
                    foundSomeStatic = true;
                } else {
                    fail("Unexpected class: " + ((JavaClass) c2f).getClassName());
                }
            } else if ("java.util.Date".equals(c2f.getClassName())) {
                ClassValidationUtil.validateCreated(c2f);
                foundCreated = true;
            }
        }

        assertTrue(foundAddress);
        assertTrue(foundContact);
        assertTrue(foundCreated);
        assertTrue(foundSomeStatic);
    }

}
