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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.CachedComplexClass;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

public class CachedComplexClassInspectClassTest {

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
    public void testCachedComplexClass() {
        JavaClass c = classInspectionService.inspectClass(CachedComplexClass.class, CollectionType.NONE, null);
        assertNotNull(c);
        assertNull(c.getAnnotations());
        assertNull(c.getArrayDimensions());
        assertEquals("io.atlasmap.java.test.CachedComplexClass", c.getClassName());
        assertNull(c.getGetMethod());
        assertNotNull(c.getJavaEnumFields());
        assertNotNull(c.getJavaEnumFields().getJavaEnumField());
        assertTrue(c.getJavaEnumFields().getJavaEnumField().size() == 0);
        assertNotNull(c.getJavaFields());
        assertNotNull(c.getJavaFields().getJavaField());
        assertNull(c.getName());
        assertEquals("io.atlasmap.java.test", c.getPackageName());
        assertNull(c.getSetMethod());
        assertEquals(FieldType.COMPLEX, c.getFieldType());
        assertNotNull(c.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.CachedComplexClass"),
                c.getUri());
        assertNull(c.getValue());

        assertEquals(Integer.valueOf(3), Integer.valueOf(c.getJavaFields().getJavaField().size()));

        Integer validated = 0;
        for (JavaField f : c.getJavaFields().getJavaField()) {
            if ("io.atlasmap.java.test.BaseOrder".equals(f.getClassName())) {
                // ClassValidationUtil.validateSimpleTestContact((JavaClass)f);
                validated++;
            }
            if ("io.atlasmap.java.test.BaseContact".equals(f.getClassName())) {
                if (!FieldStatus.CACHED.equals(f.getStatus())) {
                    ClassValidationUtil.validateSimpleTestContact((JavaClass) f);
                }
                validated++;
            }
            if ("io.atlasmap.java.test.BaseAddress".equals(f.getClassName())) {
                if (!FieldStatus.CACHED.equals(f.getStatus())) {
                    ClassValidationUtil.validateSimpleTestAddress((JavaClass) f);
                }
                validated++;
            }
            if ("long".equals(f.getClassName())) {
                ClassValidationUtil.validateSerialVersionUID(f);
                validated++;
            }
        }

        assertEquals(validated, Integer.valueOf(c.getJavaFields().getJavaField().size()));
    }

}
