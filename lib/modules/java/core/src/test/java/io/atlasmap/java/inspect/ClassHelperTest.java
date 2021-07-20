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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.atlasmap.java.core.ClassHelper;

public class ClassHelperTest {

    @Test
    public void testDetectGetter() throws Exception {
        Method getter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam");
        assertNotNull(getter);
        assertEquals("getParam", getter.getName());
        assertEquals(String.class, getter.getReturnType());
    }

    @Test
    public void testDetectGetterNotFound() {
        try {
            Method setter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam2");
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertEquals(String.format("No matching getter method for class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "getParam2"), e.getMessage());
        }
    }

    @Test
    public void testDetectSetter() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setParam", String.class);
        assertNotNull(setter);
        assertEquals("setParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(Integer.valueOf(1), Integer.valueOf(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloaded() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam", String.class);
        assertNotNull(setter);
        assertEquals("setOverloadParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(Integer.valueOf(1), Integer.valueOf(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloadedNullParam() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam", null);
        assertNotNull(setter);
        assertEquals("setOverloadParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(Integer.valueOf(1), Integer.valueOf(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloadedNotPresentParamType() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam",
                    Short.class);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertEquals(
                    "No matching setter found for class=io.atlasmap.java.inspect.JavaGetterSetterModel method=setOverloadParam paramType=java.lang.Short",
                    e.getMessage());
        }
    }

    @Test
    public void testDetectSetterOverloadedNoGetter() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoGetter",
                    null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("Unable to auto-detect setter class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "setOverloadParamNoGetter")));
        }
    }

    @Test
    public void testDetectSetterOverloadedNoMatch() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoMatch",
                    null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("No matching setter found for class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "setOverloadParamNoMatch")));
        }
    }
}
