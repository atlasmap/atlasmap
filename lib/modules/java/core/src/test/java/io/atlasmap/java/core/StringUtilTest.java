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
package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Array;

import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testRemoveGetterAndLowercaseFirstLetter() {
        assertNull(StringUtil.getFieldNameFromGetter(null));
        assertEquals("", StringUtil.getFieldNameFromGetter(""));
        assertEquals("g", StringUtil.getFieldNameFromGetter("g"));
        assertEquals("ge", StringUtil.getFieldNameFromGetter("ge"));
        assertEquals("get", StringUtil.getFieldNameFromGetter("get"));
        assertEquals("i", StringUtil.getFieldNameFromGetter("i"));
        assertEquals("is", StringUtil.getFieldNameFromGetter("is"));
        assertEquals("abc", StringUtil.getFieldNameFromGetter("getAbc"));
        assertEquals("abc", StringUtil.getFieldNameFromGetter("isAbc"));
    }

    @Test
    public void testInspectArraySize() {
        int[] foo = new int[10];

        int arraySize = Array.getLength(foo);
        System.out.println("ArraySize: " + arraySize);
    }
}
