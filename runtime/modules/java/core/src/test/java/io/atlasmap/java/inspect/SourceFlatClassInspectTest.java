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
import io.atlasmap.java.test.SourceFlatPrimitiveClass;

public class SourceFlatClassInspectTest {

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
    public void testSourceFlatPrimitiveClass() {
        ClassValidationUtil.validateFlatPrimitiveClass(classInspectionService, SourceFlatPrimitiveClass.class,
                "io.atlasmap.java.test.SourceFlatPrimitiveClass");
    }

    @Test
    public void testSourceFlatPrimitiveClassArray() {
        ClassValidationUtil.validateFlatPrimitiveClassArray(classInspectionService, SourceFlatPrimitiveClass[].class,
                "io.atlasmap.java.test.SourceFlatPrimitiveClass");
    }

    @Test
    public void testSourceFlatPrimitiveClassTwoDimArray() {
        ClassValidationUtil.validateFlatPrimitiveClassTwoDimArray(classInspectionService,
                SourceFlatPrimitiveClass[][].class, "io.atlasmap.java.test.SourceFlatPrimitiveClass");
    }

    @Test
    public void testSourceFlatPrimitiveClassThreeDimArray() {
        ClassValidationUtil.validateFlatPrimitiveClassThreeDimArray(classInspectionService,
                SourceFlatPrimitiveClass[][][].class, "io.atlasmap.java.test.SourceFlatPrimitiveClass");
    }
}
