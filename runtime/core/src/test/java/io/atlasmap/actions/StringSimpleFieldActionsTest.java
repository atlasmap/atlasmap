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

import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.StringLength;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;

public class StringSimpleFieldActionsTest {

    @Test
    public void testCapitalize() {
        assertNull(StringSimpleFieldActions.capitalize(new Capitalize(), null));
        assertEquals("", StringSimpleFieldActions.capitalize(new Capitalize(), ""));
        assertEquals(" foo ", StringSimpleFieldActions.capitalize(new Capitalize(), " foo "));
        assertEquals("   Foo", StringSimpleFieldActions.capitalize(new Capitalize(), "   Foo"));
        assertEquals("FOo   ", StringSimpleFieldActions.capitalize(new Capitalize(), "fOo   "));
        assertEquals("    foO   ", StringSimpleFieldActions.capitalize(new Capitalize(), "    foO   "));
        assertEquals("\t\n   FOO", StringSimpleFieldActions.capitalize(new Capitalize(), "\t\n   FOO"));
        assertEquals("\t\n   FOO\f\r", StringSimpleFieldActions.capitalize(new Capitalize(), "\t\n   FOO\f\r"));
    }

    @Test
    public void testFileExtension() {
        assertNull(StringSimpleFieldActions.fileExtension(null, null));
        assertNull(StringSimpleFieldActions.fileExtension(null, ""));
        assertNull(StringSimpleFieldActions.fileExtension(null, "foo"));
        assertEquals("", StringSimpleFieldActions.fileExtension(null, "."));
        assertEquals("", StringSimpleFieldActions.fileExtension(null, "foo."));
        assertEquals("bar", StringSimpleFieldActions.fileExtension(null, "foo.bar"));
        assertEquals("bar", StringSimpleFieldActions.fileExtension(null, "foo.foo.bar"));
    }

    @Test
    public void testLowerCase() {
        assertNull(StringSimpleFieldActions.lowercase(new Lowercase(), null));
        assertEquals("", StringSimpleFieldActions.lowercase(new Lowercase(), ""));
        assertEquals("foo", StringSimpleFieldActions.lowercase(new Lowercase(), "foo"));
        assertEquals("foo", StringSimpleFieldActions.lowercase(new Lowercase(), "Foo"));
        assertEquals("foo", StringSimpleFieldActions.lowercase(new Lowercase(), "fOo"));
        assertEquals("foo", StringSimpleFieldActions.lowercase(new Lowercase(), "foO"));
        assertEquals("foo", StringSimpleFieldActions.lowercase(new Lowercase(), "FOO"));
        assertEquals("foo bar", StringSimpleFieldActions.lowercase(new Lowercase(), "FOO BAR"));
    }

    @Test
    public void testNormalize() {
        assertNull(StringSimpleFieldActions.normalize(null, null));
        assertEquals("", StringSimpleFieldActions.normalize(null, ""));
        assertEquals("foo bar", StringSimpleFieldActions.normalize(null, " foo bar "));
        assertEquals("Foo Bar", StringSimpleFieldActions.normalize(null, "   Foo Bar   "));
        assertEquals("fOo bar", StringSimpleFieldActions.normalize(null, "fOo   bar"));
        assertEquals("foO bar", StringSimpleFieldActions.normalize(null, "    foO   bar   "));
        assertEquals("FOO BAR", StringSimpleFieldActions.normalize(null, "\t\n   FOO \f\t BAR "));
        assertEquals("FOO BAR", StringSimpleFieldActions.normalize(null, "\t\n   FOO \f\r BAR\f\r"));
    }

    @Test
    public void testRemoveFileExtension() {
        assertNull(StringSimpleFieldActions.removeFileExtension(null, null));
        assertEquals("", StringSimpleFieldActions.removeFileExtension(null, ""));
        assertEquals("foo", StringSimpleFieldActions.removeFileExtension(null, "foo"));
        assertEquals("", StringSimpleFieldActions.removeFileExtension(null, "."));
        assertEquals("foo", StringSimpleFieldActions.removeFileExtension(null, "foo."));
        assertEquals("foo", StringSimpleFieldActions.removeFileExtension(null, "foo.bar"));
        assertEquals("foo.foo", StringSimpleFieldActions.removeFileExtension(null, "foo.foo.bar"));
    }

    @Test
    public void testSeparateByDash() {
        assertNull(StringSimpleFieldActions.separateByDash(new SeparateByDash(), null));
        assertEquals("", StringSimpleFieldActions.separateByDash(new SeparateByDash(), ""));
        assertEquals("-", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "-"));
        assertEquals("foo", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "foo"));
        assertEquals("foo-bar", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "foo bar"));
        assertEquals("foo-bar", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "foo+bar"));
        assertEquals("foo-bar", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "foo=bar"));
        assertEquals("foo-bar", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "foo:bar"));
        assertEquals("f-o-o-b-a-r", StringSimpleFieldActions.separateByDash(new SeparateByDash(), "f:o:o:b:a:r"));
    }

    @Test
    public void testSeparateByUnderscore() {
        assertNull(StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), null));
        assertEquals("", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), ""));
        assertEquals("_", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "-"));
        assertEquals("foo", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "foo"));
        assertEquals("foo_bar", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "foo bar"));
        assertEquals("foo_bar", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "foo+bar"));
        assertEquals("foo_bar", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "foo=bar"));
        assertEquals("foo_bar", StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "foo:bar"));
        assertEquals("f_o_o_b_a_r",
                StringSimpleFieldActions.separateByUnderscore(new SeparateByUnderscore(), "f:o:o:b:a:r"));
    }

    @Test
    public void testSeparatorRegex() {
        Pattern pattern = Pattern.compile(StringSimpleFieldActions.STRING_SEPARATOR_REGEX);
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
    public void testStringLength() {
        assertEquals(new Integer(0), StringSimpleFieldActions.stringLength(new StringLength(), null));
        assertEquals(new Integer(0), StringSimpleFieldActions.stringLength(new StringLength(), ""));
        assertEquals(new Integer(5), StringSimpleFieldActions.stringLength(new StringLength(), " foo "));
    }

    @Test
    public void testTrim() {
        assertNull(StringSimpleFieldActions.trim(new Trim(), null));
        assertEquals("", StringSimpleFieldActions.trim(new Trim(), ""));
        assertEquals("foo", StringSimpleFieldActions.trim(new Trim(), " foo "));
        assertEquals("Foo", StringSimpleFieldActions.trim(new Trim(), "   Foo"));
        assertEquals("fOo", StringSimpleFieldActions.trim(new Trim(), "fOo   "));
        assertEquals("foO", StringSimpleFieldActions.trim(new Trim(), "    foO   "));
        assertEquals("FOO", StringSimpleFieldActions.trim(new Trim(), "\t\n   FOO"));
        assertEquals("FOO", StringSimpleFieldActions.trim(new Trim(), "\t\n   FOO\f\r"));
    }

    @Test
    public void testTrimLeft() {
        assertNull(StringSimpleFieldActions.trimLeft(new TrimLeft(), null));
        assertEquals("", StringSimpleFieldActions.trimLeft(new TrimLeft(), ""));
        assertEquals("foo ", StringSimpleFieldActions.trimLeft(new TrimLeft(), " foo "));
        assertEquals("Foo", StringSimpleFieldActions.trimLeft(new TrimLeft(), "   Foo"));
        assertEquals("fOo   ", StringSimpleFieldActions.trimLeft(new TrimLeft(), "fOo   "));
        assertEquals("foO   ", StringSimpleFieldActions.trimLeft(new TrimLeft(), "    foO   "));
        assertEquals("FOO", StringSimpleFieldActions.trimLeft(new TrimLeft(), "\t\n   FOO"));
        assertEquals("FOO\f\r", StringSimpleFieldActions.trimLeft(new TrimLeft(), "\t\n   FOO\f\r"));
    }

    @Test
    public void testTrimRight() {
        assertNull(StringSimpleFieldActions.trimRight(new TrimRight(), null));
        assertEquals("", StringSimpleFieldActions.trimRight(new TrimRight(), ""));
        assertEquals(" foo", StringSimpleFieldActions.trimRight(new TrimRight(), " foo "));
        assertEquals("   Foo", StringSimpleFieldActions.trimRight(new TrimRight(), "   Foo"));
        assertEquals("fOo", StringSimpleFieldActions.trimRight(new TrimRight(), "fOo   "));
        assertEquals("    foO", StringSimpleFieldActions.trimRight(new TrimRight(), "    foO   "));
        assertEquals("\t\n   FOO", StringSimpleFieldActions.trimRight(new TrimRight(), "\t\n   FOO"));
        assertEquals("\t\n   FOO", StringSimpleFieldActions.trimRight(new TrimRight(), "\t\n   FOO\f\r"));
    }

    @Test
    public void testUpperCase() {
        assertNull(StringSimpleFieldActions.uppercase(new Uppercase(), null));
        assertEquals("", StringSimpleFieldActions.uppercase(new Uppercase(), ""));
        assertEquals("FOO", StringSimpleFieldActions.uppercase(new Uppercase(), "foo"));
        assertEquals("FOO", StringSimpleFieldActions.uppercase(new Uppercase(), "Foo"));
        assertEquals("FOO", StringSimpleFieldActions.uppercase(new Uppercase(), "fOo"));
        assertEquals("FOO", StringSimpleFieldActions.uppercase(new Uppercase(), "foO"));
        assertEquals("FOO", StringSimpleFieldActions.uppercase(new Uppercase(), "FOO"));
        assertEquals("FOO BAR", StringSimpleFieldActions.uppercase(new Uppercase(), "foo bar"));
    }

}
