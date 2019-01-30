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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.v2.CollectionType;

public class PrimitiveInspectTest {

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testPrimitives() throws Exception {
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(boolean.class, CollectionType.NONE, null), "boolean");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(byte.class, CollectionType.NONE, null), "byte");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(char.class, CollectionType.NONE, null), "char");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(double.class, CollectionType.NONE, null), "double");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(float.class, CollectionType.NONE, null), "float");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(int.class, CollectionType.NONE, null), "int");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(long.class, CollectionType.NONE, null), "long");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(short.class, CollectionType.NONE, null), "short");
    }

    @Test
    public void testPrimitiveArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[].class, CollectionType.NONE, null), "boolean",
                1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[].class, CollectionType.ARRAY, null), "byte", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[].class, CollectionType.ARRAY, null), "char", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[].class, CollectionType.ARRAY, null), "double",
                1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[].class, CollectionType.ARRAY, null), "float", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[].class, CollectionType.ARRAY, null), "int", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[].class, CollectionType.ARRAY, null), "long", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[].class, CollectionType.ARRAY, null), "short", 1);
    }

    @Test
    public void testPrimitiveTwoDimArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[][].class, CollectionType.ARRAY, null),
                "boolean", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[][].class, CollectionType.ARRAY, null), "byte", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[][].class, CollectionType.ARRAY, null), "char", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[][].class, CollectionType.ARRAY, null), "double",
                2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[][].class, CollectionType.ARRAY, null), "float",
                2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[][].class, CollectionType.ARRAY, null), "int", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[][].class, CollectionType.ARRAY, null), "long", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[][].class, CollectionType.ARRAY, null), "short",
                2);
    }

    @Test
    public void testPrimitiveThreeDimArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[][][].class, CollectionType.ARRAY, null),
                "boolean", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[][][].class, CollectionType.ARRAY, null), "byte",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[][][].class, CollectionType.ARRAY, null), "char",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[][][].class, CollectionType.ARRAY, null),
                "double", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[][][].class, CollectionType.ARRAY, null), "float",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[][][].class, CollectionType.ARRAY, null), "int", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[][][].class, CollectionType.ARRAY, null), "long",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[][][].class, CollectionType.ARRAY, null), "short",
                3);
    }
}
