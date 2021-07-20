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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.FlatPrimitiveInterface;

public class FlatInterfaceInspectTest {

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
    public void testFlatPrimitiveInterface() {
        ClassValidationUtil.validateFlatPrimitiveInterface(classInspectionService, FlatPrimitiveInterface.class);
    }

    @Test
    public void testFlatPrimitiveInterfaceArray() {
        ClassValidationUtil.validateFlatPrimitiveInterfaceArray(classInspectionService, FlatPrimitiveInterface[].class);
    }

    @Test
    public void testFlatPrimitiveInterfaceTwoDimArray() {
        ClassValidationUtil.validateFlatPrimitiveInterfaceTwoDimArray(classInspectionService,
                FlatPrimitiveInterface[][].class);
    }

    @Test
    public void testFlatPrimitiveInterfaceThreeDimArray() {
        ClassValidationUtil.validateFlatPrimitiveInterfaceThreeDimArray(classInspectionService,
                FlatPrimitiveInterface[][][].class);
    }

}
