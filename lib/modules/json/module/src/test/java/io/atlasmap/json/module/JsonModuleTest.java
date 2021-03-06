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
package io.atlasmap.json.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public class JsonModuleTest {

    private JsonModule module = null;

    @Before
    public void setUp() {
        module = new JsonModule();
    }

    @After
    public void tearDown() {
        module = null;
    }

    @Test
    public void testIsSupportedField() {
        assertTrue(module.isSupportedField(new JsonField()));
        assertFalse(module.isSupportedField(new PropertyField()));
        assertFalse(module.isSupportedField(new ConstantField()));
        assertTrue(module.isSupportedField(new SimpleField()));
    }

}
