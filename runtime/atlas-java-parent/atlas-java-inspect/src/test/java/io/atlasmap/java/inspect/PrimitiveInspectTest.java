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
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(boolean.class), "boolean");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(byte.class), "byte");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(char.class), "char");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(double.class), "double");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(float.class), "float");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(int.class), "int");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(long.class), "long");
        PrimitiveValidationUtil.validatePrimitive(classInspectionService.inspectClass(short.class), "short");
    }

    @Test
    public void testPrimitiveArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[].class), "boolean",
                1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[].class), "byte", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[].class), "char", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[].class), "double",
                1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[].class), "float", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[].class), "int", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[].class), "long", 1);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[].class), "short", 1);
    }

    @Test
    public void testPrimitiveTwoDimArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[][].class),
                "boolean", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[][].class), "byte", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[][].class), "char", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[][].class), "double",
                2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[][].class), "float",
                2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[][].class), "int", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[][].class), "long", 2);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[][].class), "short",
                2);
    }

    @Test
    public void testPrimitiveThreeDimArrays() throws Exception {
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(boolean[][][].class),
                "boolean", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(byte[][][].class), "byte",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(char[][][].class), "char",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(double[][][].class),
                "double", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(float[][][].class), "float",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(int[][][].class), "int", 3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(long[][][].class), "long",
                3);
        PrimitiveValidationUtil.validatePrimitiveArray(classInspectionService.inspectClass(short[][][].class), "short",
                3);
    }
}
