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
package io.atlasmap.java.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public class JavaModuleTest {

    private JavaModule module = null;

    @BeforeEach
    public void setUp() {
        module = new JavaModule();
    }

    @AfterEach
    public void tearDown() {
        module = null;
    }

    @Test
    public void testIsSupportedField() {
        assertTrue(module.isSupportedField(new JavaField()));
        assertTrue(module.isSupportedField(new JavaEnumField()));
        assertFalse(module.isSupportedField(new PropertyField()));
        assertFalse(module.isSupportedField(new ConstantField()));
        assertTrue(module.isSupportedField(new SimpleField()));
    }

}
