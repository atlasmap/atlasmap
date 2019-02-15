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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import io.atlasmap.v2.ActionUtil;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.FileExtension;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.LowercaseChar;
import io.atlasmap.v2.Normalize;
import io.atlasmap.v2.RemoveFileExtension;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;
import io.atlasmap.v2.UppercaseChar;

public class StringSimpleFieldActionsTest {

    @Test
    public void testCapitalize() {
        Capitalize action = new Capitalize();
        assertNull(action.capitalize(null));
        assertEquals("", action.capitalize(""));
        assertEquals(" foo ", action.capitalize(" foo "));
        assertEquals("   Foo", action.capitalize("   Foo"));
        assertEquals("FOo   ", action.capitalize("fOo   "));
        assertEquals("    foO   ", action.capitalize("    foO   "));
        assertEquals("\t\n   FOO", action.capitalize("\t\n   FOO"));
        assertEquals("\t\n   FOO\f\r", action.capitalize("\t\n   FOO\f\r"));
    }

    @Test
    public void testFileExtension() {
        FileExtension action = new FileExtension();
        assertNull(action.fileExtension(null));
        assertNull(action.fileExtension(""));
        assertNull(action.fileExtension("foo"));
        assertEquals("", action.fileExtension("."));
        assertEquals("", action.fileExtension("foo."));
        assertEquals("bar", action.fileExtension("foo.bar"));
        assertEquals("bar", action.fileExtension("foo.foo.bar"));
    }

    @Test
    public void testLowerCase() {
        Lowercase action = new Lowercase();
        assertNull(action.lowercase(null));
        assertEquals("", action.lowercase(""));
        assertEquals("foo", action.lowercase("foo"));
        assertEquals("foo", action.lowercase("Foo"));
        assertEquals("foo", action.lowercase("fOo"));
        assertEquals("foo", action.lowercase("foO"));
        assertEquals("foo", action.lowercase("FOO"));
        assertEquals("foo bar", action.lowercase("FOO BAR"));
    }

    @Test
    public void testLowerCaseChar() {
        LowercaseChar action = new LowercaseChar();
        assertNull(action.lowercaseChar(null));
        assertEquals('\0', action.lowercaseChar('\0').charValue());
        assertEquals('f', action.lowercaseChar('f').charValue());
        assertEquals('f', action.lowercaseChar('F').charValue());
    }

    @Test
    public void testNormalize() {
        Normalize action = new Normalize();
        assertNull(action.normalize(null));
        assertEquals("", action.normalize(""));
        assertEquals("foo bar", action.normalize(" foo bar "));
        assertEquals("Foo Bar", action.normalize("   Foo Bar   "));
        assertEquals("fOo bar", action.normalize("fOo   bar"));
        assertEquals("foO bar", action.normalize("    foO   bar   "));
        assertEquals("FOO BAR", action.normalize("\t\n   FOO \f\t BAR "));
        assertEquals("FOO BAR", action.normalize("\t\n   FOO \f\r BAR\f\r"));
    }

    @Test
    public void testRemoveFileExtension() {
        RemoveFileExtension action = new RemoveFileExtension();
        assertNull(action.removeFileExtension(null));
        assertEquals("", action.removeFileExtension(""));
        assertEquals("foo", action.removeFileExtension("foo"));
        assertEquals("", action.removeFileExtension("."));
        assertEquals("foo", action.removeFileExtension("foo."));
        assertEquals("foo", action.removeFileExtension("foo.bar"));
        assertEquals("foo.foo", action.removeFileExtension("foo.foo.bar"));
    }

    @Test
    public void testSeparateByDash() {
        SeparateByDash action = new SeparateByDash();
        assertNull(action.separateByDash(null));
        assertEquals("", action.separateByDash(""));
        assertEquals("-", action.separateByDash("-"));
        assertEquals("foo", action.separateByDash("foo"));
        assertEquals("foo-bar", action.separateByDash("foo bar"));
        assertEquals("foo-bar", action.separateByDash("foo+bar"));
        assertEquals("foo-bar", action.separateByDash("foo=bar"));
        assertEquals("foo-bar", action.separateByDash("foo:bar"));
        assertEquals("f-o-o-b-a-r", action.separateByDash("f:o:o:b:a:r"));
    }

    @Test
    public void testSeparateByUnderscore() {
        SeparateByUnderscore action = new SeparateByUnderscore();
        assertNull(action.separateByUnderscore(null));
        assertEquals("", action.separateByUnderscore(""));
        assertEquals("_", action.separateByUnderscore("-"));
        assertEquals("foo", action.separateByUnderscore("foo"));
        assertEquals("foo_bar", action.separateByUnderscore("foo bar"));
        assertEquals("foo_bar", action.separateByUnderscore("foo+bar"));
        assertEquals("foo_bar", action.separateByUnderscore("foo=bar"));
        assertEquals("foo_bar", action.separateByUnderscore("foo:bar"));
        assertEquals("f_o_o_b_a_r", action.separateByUnderscore("f:o:o:b:a:r"));
    }

    @Test
    public void testSeparatorRegex() {
        Pattern pattern = Pattern.compile(ActionUtil.STRING_SEPARATOR_REGEX);
        assertFalse(pattern.matcher("foo").find());
        assertFalse(pattern.matcher("").find());
        assertTrue(pattern.matcher("f o").find());
        assertTrue(pattern.matcher("f+o").find());
        assertTrue(pattern.matcher("f=o").find());
        assertTrue(pattern.matcher("f_o").find());
        assertTrue(pattern.matcher("f:o").find());
        assertTrue(pattern.matcher("f:o:o").find());
        assertTrue(pattern.matcher("f  o").find());
    }

    @Test
    public void testTrim() {
        Trim action = new Trim();
        assertNull(action.trim(null));
        assertEquals("", action.trim(""));
        assertEquals("foo", action.trim(" foo "));
        assertEquals("Foo", action.trim("   Foo"));
        assertEquals("fOo", action.trim("fOo   "));
        assertEquals("foO", action.trim("    foO   "));
        assertEquals("FOO", action.trim("\t\n   FOO"));
        assertEquals("FOO", action.trim("\t\n   FOO\f\r"));
    }

    @Test
    public void testTrimLeft() {
        TrimLeft action = new TrimLeft();
        assertNull(action.trimLeft(null));
        assertEquals("", action.trimLeft(""));
        assertEquals("foo ", action.trimLeft(" foo "));
        assertEquals("Foo", action.trimLeft("   Foo"));
        assertEquals("fOo   ", action.trimLeft("fOo   "));
        assertEquals("foO   ", action.trimLeft("    foO   "));
        assertEquals("FOO", action.trimLeft("\t\n   FOO"));
        assertEquals("FOO\f\r", action.trimLeft("\t\n   FOO\f\r"));
    }

    @Test
    public void testTrimRight() {
        TrimRight action = new TrimRight();
        assertNull(action.trimRight(null));
        assertEquals("", action.trimRight(""));
        assertEquals(" foo", action.trimRight(" foo "));
        assertEquals("   Foo", action.trimRight("   Foo"));
        assertEquals("fOo", action.trimRight("fOo   "));
        assertEquals("    foO", action.trimRight("    foO   "));
        assertEquals("\t\n   FOO", action.trimRight("\t\n   FOO"));
        assertEquals("\t\n   FOO", action.trimRight("\t\n   FOO\f\r"));
    }

    @Test
    public void testUpperCase() {
        Uppercase action = new Uppercase();
        assertNull(action.uppercase(null));
        assertEquals("", action.uppercase(""));
        assertEquals("FOO", action.uppercase("foo"));
        assertEquals("FOO", action.uppercase("Foo"));
        assertEquals("FOO", action.uppercase("fOo"));
        assertEquals("FOO", action.uppercase("foO"));
        assertEquals("FOO", action.uppercase("FOO"));
        assertEquals("FOO BAR", action.uppercase("foo bar"));
    }

    @Test
    public void testUpperCaseChar() {
        UppercaseChar action = new UppercaseChar();
        assertNull(action.uppercaseChar(null));
        assertEquals('\0', action.uppercaseChar('\0').charValue());
        assertEquals('F', action.uppercaseChar('f').charValue());
        assertEquals('F', action.uppercaseChar('F').charValue());
    }
}
