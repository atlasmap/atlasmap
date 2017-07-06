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
package io.atlasmap.actions;

import static org.junit.Assert.*;
import org.junit.Test;

import io.atlasmap.actions.StringFieldActions;

public class StringFieldActionsTest {

    @Test
    public void testUpperCase() {
        assertNull(StringFieldActions.uppercase(null));
        assertEquals("", StringFieldActions.uppercase(""));
        assertEquals("FOO", StringFieldActions.uppercase("foo"));
        assertEquals("FOO", StringFieldActions.uppercase("Foo"));
        assertEquals("FOO", StringFieldActions.uppercase("fOo"));
        assertEquals("FOO", StringFieldActions.uppercase("foO"));
        assertEquals("FOO", StringFieldActions.uppercase("FOO"));
        assertEquals("FOO BAR", StringFieldActions.uppercase("foo bar"));
    }

    @Test
    public void testLowerCase() {
        assertNull(StringFieldActions.lowercase(null));
        assertEquals("", StringFieldActions.lowercase(""));
        assertEquals("foo", StringFieldActions.lowercase("foo"));
        assertEquals("foo", StringFieldActions.lowercase("Foo"));
        assertEquals("foo", StringFieldActions.lowercase("fOo"));
        assertEquals("foo", StringFieldActions.lowercase("foO"));
        assertEquals("foo", StringFieldActions.lowercase("FOO"));
        assertEquals("foo bar", StringFieldActions.lowercase("FOO BAR"));    
    }

    @Test
    public void testTrim() {
        assertNull(StringFieldActions.trim(null));
        assertEquals("", StringFieldActions.trim(""));
        assertEquals("foo", StringFieldActions.trim(" foo "));
        assertEquals("Foo", StringFieldActions.trim("   Foo"));
        assertEquals("fOo", StringFieldActions.trim("fOo   "));
        assertEquals("foO", StringFieldActions.trim("    foO   "));
        assertEquals("FOO", StringFieldActions.trim("\t\n   FOO"));
        assertEquals("FOO", StringFieldActions.trim("\t\n   FOO\f\r")); 
    }

    @Test
    public void testTrimLeft() {
        assertNull(StringFieldActions.trimLeft(null));
        assertEquals("", StringFieldActions.trimLeft(""));
        assertEquals("foo ", StringFieldActions.trimLeft(" foo "));
        assertEquals("Foo", StringFieldActions.trimLeft("   Foo"));
        assertEquals("fOo   ", StringFieldActions.trimLeft("fOo   "));
        assertEquals("foO   ", StringFieldActions.trimLeft("    foO   "));
        assertEquals("FOO", StringFieldActions.trimLeft("\t\n   FOO"));
        assertEquals("FOO\f\r", StringFieldActions.trimLeft("\t\n   FOO\f\r"));     
    }

    @Test
    public void testTrimRight() {
        assertNull(StringFieldActions.trimRight(null));
        assertEquals("", StringFieldActions.trimRight(""));
        assertEquals(" foo", StringFieldActions.trimRight(" foo "));
        assertEquals("   Foo", StringFieldActions.trimRight("   Foo"));
        assertEquals("fOo", StringFieldActions.trimRight("fOo   "));
        assertEquals("    foO", StringFieldActions.trimRight("    foO   "));
        assertEquals("\t\n   FOO", StringFieldActions.trimRight("\t\n   FOO"));
        assertEquals("\t\n   FOO", StringFieldActions.trimRight("\t\n   FOO\f\r"));
    }

    @Test
    public void testCapitalize() {
        assertNull(StringFieldActions.capitalize(null));
        assertEquals("", StringFieldActions.capitalize(""));
        assertEquals(" foo ", StringFieldActions.capitalize(" foo "));
        assertEquals("   Foo", StringFieldActions.capitalize("   Foo"));
        assertEquals("FOo   ", StringFieldActions.capitalize("fOo   "));
        assertEquals("    foO   ", StringFieldActions.capitalize("    foO   "));
        assertEquals("\t\n   FOO", StringFieldActions.capitalize("\t\n   FOO"));
        assertEquals("\t\n   FOO\f\r", StringFieldActions.capitalize("\t\n   FOO\f\r"));    
    }

    @Test
    public void testStringLength() {
        assertEquals(new Integer(0), StringFieldActions.stringLength(null));
        assertEquals(new Integer(0), StringFieldActions.stringLength(""));
        assertEquals(new Integer(5), StringFieldActions.stringLength(" foo "));
    }

    @Test
    public void testDashSeparate() {

    }

    @Test
    public void testUnderscoreSeparate() {
    
    }

}
