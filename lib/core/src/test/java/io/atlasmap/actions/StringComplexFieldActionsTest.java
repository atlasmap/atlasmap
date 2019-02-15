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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import io.atlasmap.v2.Append;
import io.atlasmap.v2.Concatenate;
import io.atlasmap.v2.EndsWith;
import io.atlasmap.v2.Format;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.IndexOf;
import io.atlasmap.v2.LastIndexOf;
import io.atlasmap.v2.PadStringLeft;
import io.atlasmap.v2.PadStringRight;
import io.atlasmap.v2.Prepend;
import io.atlasmap.v2.ReplaceAll;
import io.atlasmap.v2.ReplaceFirst;
import io.atlasmap.v2.Split;
import io.atlasmap.v2.StartsWith;
import io.atlasmap.v2.SubString;
import io.atlasmap.v2.SubStringAfter;
import io.atlasmap.v2.SubStringBefore;

public class StringComplexFieldActionsTest {

    @Test
    public void testAppend() {
        Append action = new Append();
        assertEquals(null, action.append(null));
        assertEquals("foo", action.append("foo"));
        assertEquals("1", action.append(1));
        action.setSuffix("");
        assertEquals("", action.append(null));
        assertEquals("foo", action.append("foo"));
        action.setSuffix("bar");
        assertEquals("bar", action.append(null));
        assertEquals("foobar", action.append("foo"));
        assertEquals("1bar", action.append(1));
    }

    @Test
    public void testConcatenate() {
        Concatenate action = new Concatenate();
        assertEquals(null, action.concatenate(null));
        assertEquals("1true2.0", action.concatenate(new Object[] {1, true, 2.0}));
        assertEquals("1true2.0", action.concatenate(Arrays.asList(1, true, 2.0)));
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(1, 1);
        map.put(true, true);
        map.put(2.0, 2.0);
        assertEquals("1true2.0", action.concatenate(map));
        action.setDelimiter("-");
        assertEquals(null, action.concatenate(null));
        assertEquals("1-true-2.0", action.concatenate(new Object[] {1, true, 2.0}));
        assertEquals("1-true-2.0", action.concatenate(Arrays.asList(1, true, 2.0)));
        assertEquals("1-true-2.0", action.concatenate(map));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConcatenateNonCollection() {
        Concatenate action = new Concatenate();
        action.concatenate("");
    }

    @Test
    public void testEndsWith() {
        EndsWith action = new EndsWith();
        action.setString("");
        assertFalse(action.endsWith(null));
        assertTrue(action.endsWith(""));
        assertTrue(action.endsWith("foo"));
        action.setString("bar");
        assertFalse(action.endsWith(null));
        assertFalse(action.endsWith(""));
        assertFalse(action.endsWith("foo"));
        assertTrue(action.endsWith("foobar"));
        assertFalse(action.endsWith("barfoo"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEndsWithNullString() {
        EndsWith action = new EndsWith();
        action.endsWith(null);
    }

    @Test
    public void testFormat() {
        Format action = new Format();
        action.setTemplate("foofoo");
        assertEquals("foofoo", action.format(null));
        assertEquals("foofoo", action.format(""));
        assertEquals("foofoo", action.format("bar"));
        action.setTemplate("foo%sfoo");
        assertEquals("foonullfoo", action.format(null));
        assertEquals("foofoo", action.format(""));
        assertEquals("foobarfoo", action.format("bar"));
        action.setTemplate("foo%1$sfoo%1$s");
        assertEquals("foobarfoobar", action.format("bar"));
        action.setTemplate("%,.2f");
        assertEquals("1,234.00", action.format(1234f));
        assertEquals("0.05", action.format(.05));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFormatNullTemplate() {
        Format action = new Format();
        action.format(null);
    }

    @Test
    public void testGenerateUUID() {
        GenerateUUID action = new GenerateUUID();
        validateGeneratedUUID(action.genareteUUID(null));
        validateGeneratedUUID(action.genareteUUID(new Byte(Byte.parseByte("0"))));
        validateGeneratedUUID(action.genareteUUID(new Character('a')));
        validateGeneratedUUID(action.genareteUUID(new Double(14324d)));
        validateGeneratedUUID(action.genareteUUID(new Float(234235235325f)));
        validateGeneratedUUID(action.genareteUUID(new Integer(32523)));
        validateGeneratedUUID(action.genareteUUID(new Long(235325L)));
        validateGeneratedUUID(action.genareteUUID(new Short((short) 4323)));
        validateGeneratedUUID(action.genareteUUID(""));
    }

    @Test
    public void testIndexOf() {
        IndexOf action = new IndexOf();
        action.setString("");
        assertEquals(-1, action.indexOf(null));
        assertEquals(0, action.indexOf(""));
        assertEquals(0, action.indexOf("foo"));
        action.setString("bar");
        assertEquals(-1, action.indexOf(null));
        assertEquals(-1, action.indexOf(""));
        assertEquals(-1, action.indexOf("foo"));
        assertEquals(3, action.indexOf("foobar"));
        assertEquals(3, action.indexOf("foobarbar"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIndexOfNullString() {
        IndexOf action = new IndexOf();
        action.indexOf(null);
    }

    @Test
    public void testLastIndexOf() {
        LastIndexOf action = new LastIndexOf();
        action.setString("");
        assertEquals(-1, action.lastIndexOf(null));
        assertEquals(0, action.lastIndexOf(""));
        assertEquals(3, action.lastIndexOf("foo"));
        action.setString("bar");
        assertEquals(-1, action.lastIndexOf(null));
        assertEquals(-1, action.lastIndexOf(""));
        assertEquals(-1, action.lastIndexOf("foo"));
        assertEquals(3, action.lastIndexOf("foobar"));
        assertEquals(6, action.lastIndexOf("foobarbar"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLastIndexOfNullString() {
        LastIndexOf action = new LastIndexOf();
        action.lastIndexOf(null);
    }

    @Test
    public void testPadStringLeft() {
        PadStringLeft action = new PadStringLeft();
        action.setPadCharacter("a");
        action.setPadCount(3);

        assertEquals("aaa", action.padStringLeft(null));
        assertEquals("aaa", action.padStringLeft(""));
        assertEquals("aaaa", action.padStringLeft("a"));
        assertEquals("aaab", action.padStringLeft("b"));

        try {
            new PadStringLeft().padStringLeft("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        action.setPadCharacter("f");
        action.setPadCount(null);
        assertEquals("aa", action.padStringLeft("aa"));

        try {
            PadStringLeft incomplete = new PadStringLeft();
            incomplete.setPadCount(3);
            incomplete.padStringLeft("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testPadStringRight() {
        PadStringRight action = new PadStringRight();
        action.setPadCharacter("a");
        action.setPadCount(3);

        assertEquals("aaa", action.padStringRight(null));
        assertEquals("aaa", action.padStringRight(""));
        assertEquals("aaaa", action.padStringRight("a"));
        assertEquals("baaa", action.padStringRight("b"));

        try {
            new PadStringRight().padStringRight("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        action.setPadCharacter("f");
        action.setPadCount(null);
        assertEquals("aa", action.padStringRight("aa"));

        try {
            PadStringRight incomplete = new PadStringRight();
            incomplete.setPadCount(3);
            incomplete.padStringRight("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPrepend() {
        Prepend action = new Prepend();
        assertEquals(null, action.prepend(null));
        assertEquals("foo", action.prepend("foo"));
        assertEquals("1", action.prepend(1));
        action.setPrefix("");
        assertEquals("", action.prepend(null));
        assertEquals("foo", action.prepend("foo"));
        action.setPrefix("bar");
        assertEquals("bar", action.prepend(null));
        assertEquals("barfoo", action.prepend("foo"));
        assertEquals("bar1", action.prepend(1));
    }

    @Test
    public void testReplaceFirst() {
        ReplaceFirst action = new ReplaceFirst();
        action.setMatch(" ");
        assertNull(action.replaceFirst(null));
        assertEquals("", action.replaceFirst(""));
        assertEquals("test", action.replaceFirst("test"));
        action.setMatch("e");
        assertEquals("tst", action.replaceFirst("test"));
        action.setMatch("t");
        action.setNewString("h");
        assertEquals("hest", action.replaceFirst("test"));
        action.setMatch("is");
        action.setNewString("at");
        assertEquals("That is a test", action.replaceFirst("This is a test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceFirstEmptyMatch() {
        ReplaceFirst action = new ReplaceFirst();
        action.setMatch("");
        action.replaceFirst(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceFirstNullMatch() {
        ReplaceFirst action = new ReplaceFirst();
        action.replaceFirst(" ");
    }

    @Test
    public void testReplaceAll() {
        ReplaceAll action = new ReplaceAll();
        action.setMatch(" ");
        assertNull(action.replaceAll(null));
        assertEquals("", action.replaceAll(""));
        assertEquals("test", action.replaceAll("test"));
        action.setMatch("e");
        assertEquals("tst", action.replaceAll("test"));
        action.setMatch("t");
        action.setNewString("h");
        assertEquals("hesh", action.replaceAll("test"));
        action.setMatch("is");
        action.setNewString("at");
        assertEquals("That at a test", action.replaceAll("This is a test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceAllEmptyMatch() {
        ReplaceAll action = new ReplaceAll();
        action.setMatch("");
        action.replaceAll(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceAllNullOldString() {
        ReplaceAll action = new ReplaceAll();
        action.replaceAll(" ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSplitNoDelimiter() {
        Split action = new Split();
        action.split("foobar");
    }

    @Test
    public void testSplit() {
        Split action = new Split();
        action.setDelimiter(",");
        assertArrayEquals(null, action.split(null));
        assertArrayEquals(new String[] {"1", "2", "3"}, action.split("1,2,3"));
    }

    @Test
    public void testStartsWith() {
        StartsWith action = new StartsWith();
        action.setString("");
        assertFalse(action.startsWith(null));
        assertTrue(action.startsWith(""));
        assertTrue(action.startsWith("foo"));
        action.setString("foo");
        assertFalse(action.startsWith(null));
        assertFalse(action.startsWith(""));
        assertTrue(action.startsWith("foo"));
        assertTrue(action.startsWith("foobar"));
        assertFalse(action.startsWith("barfoo"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testStartsWithNullString() {
        StartsWith action = new StartsWith();
        action.startsWith(null);
    }

    @Test
    public void testSubString() {
        SubString action = new SubString();
        action.setStartIndex(2);
        action.setEndIndex(4);

        assertNull(action.subString(null));
        assertEquals("", action.subString(""));
    }

    @Test
    public void testSubStringAfter() {
        SubStringAfter action = new SubStringAfter();
        action.setStartIndex(3);
        action.setEndIndex(null);
        action.setMatch("foo");

        assertNull(action.subStringAfter(null));
        assertEquals("", action.subStringAfter(""));
        assertEquals("blah", action.subStringAfter("foobarblah"));
        assertEquals("blahfoo", action.subStringAfter("foobarblahfoo"));

        assertEquals("barblah", action.subStringAfter("barblah"));

        action.setEndIndex(7);
        assertEquals("blah", action.subStringAfter("foobarblahfoo"));

        action.setEndIndex(3);
        assertEquals("", action.subStringAfter("foobarblahfoo"));

        try {
            SubStringAfter err = new SubStringAfter();
            err.subStringAfter("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringAfter err = new SubStringAfter();
            err.setEndIndex(5);
            err.setStartIndex(0);
            err.subStringAfter("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringAfter err = new SubStringAfter();
            err.setEndIndex(0);
            err.setStartIndex(5);
            err.subStringAfter("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSubStringBefore() {
        SubStringBefore action = new SubStringBefore();
        action.setStartIndex(3);
        action.setEndIndex(null);
        action.setMatch("blah");

        assertNull(action.subStringBefore(null));
        assertEquals("", action.subStringBefore(""));
        assertEquals("bar", action.subStringBefore("foobarblah"));
        assertEquals("foobar", action.subStringBefore("foofoobarblahfoo"));
        assertEquals("", action.subStringBefore("barblah"));

        action.setEndIndex(5);
        assertEquals("ba", action.subStringBefore("foobarblah"));
        action.setEndIndex(3);
        assertEquals("", action.subStringBefore("foobarblah"));

        try {
            SubStringBefore err = new SubStringBefore();
            err.subStringBefore("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringBefore err = new SubStringBefore();
            err.setEndIndex(5);
            err.setStartIndex(0);
            err.subStringBefore("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringBefore err = new SubStringBefore();
            err.setEndIndex(0);
            err.setStartIndex(5);
            err.subStringBefore("aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    protected void validateCurrentDate(String dateValue) {
        assertNotNull(dateValue);
        assertTrue(dateValue.length() > 0);
        assertTrue(Pattern.compile("20([1-9][0-9])-(0[0-9]|1[0-2])-(0[0-9]|1[0-9]|2[0-9]|3[0-1])").matcher(dateValue)
                .matches());
    }

    protected void validateCurrentDateTime(String dateTimeValue) {
        assertNotNull(dateTimeValue);
        assertTrue(dateTimeValue.length() > 0);
        assertTrue(Pattern.compile("20([1-9][0-9])-(0[0-9]|1[0-2])-(0[0-9]|1[0-9]|2[0-9]|3[0-1])T([01]?[0-9]|2[0-3]):[0-5][0-9]Z").matcher(dateTimeValue)
                .matches());
    }

    protected void validateCurrentTime(String timeValue) {
        assertNotNull(timeValue);
        assertTrue(timeValue.length() > 0);
        assertTrue(Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]").matcher(timeValue).matches());
    }

    protected void validateGeneratedUUID(String uuid) {
        assertNotNull(uuid);
        assertTrue(uuid.length() > 0);
        assertTrue(Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(uuid)
                .matches());
    }
}
