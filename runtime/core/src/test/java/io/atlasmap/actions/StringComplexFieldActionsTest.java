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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import io.atlasmap.v2.Concatenate;
import io.atlasmap.v2.CurrentDate;
import io.atlasmap.v2.CurrentDateTime;
import io.atlasmap.v2.CurrentTime;
import io.atlasmap.v2.EndsWith;
import io.atlasmap.v2.Format;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.IndexOf;
import io.atlasmap.v2.LastIndexOf;
import io.atlasmap.v2.PadStringLeft;
import io.atlasmap.v2.PadStringRight;
import io.atlasmap.v2.ReplaceAll;
import io.atlasmap.v2.ReplaceFirst;
import io.atlasmap.v2.StartsWith;
import io.atlasmap.v2.SubString;
import io.atlasmap.v2.SubStringAfter;
import io.atlasmap.v2.SubStringBefore;

public class StringComplexFieldActionsTest {

    @Test
    public void testConcatenate() {
        Concatenate action = new Concatenate();

        assertEquals(null, StringComplexFieldActions.concatenate(action, null));
        assertEquals("1true2.0", StringComplexFieldActions.concatenate(action, new Object[] {1, true, 2.0}));
        assertEquals("1true2.0", StringComplexFieldActions.concatenate(action, Arrays.asList(1, true, 2.0)));
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(1, 1);
        map.put(true, true);
        map.put(2.0, 2.0);
        assertEquals("1true2.0", StringComplexFieldActions.concatenate(action, map));
        action.setDelimiter("-");
        assertEquals(null, StringComplexFieldActions.concatenate(action, null));
        assertEquals("1-true-2.0", StringComplexFieldActions.concatenate(action, new Object[] {1, true, 2.0}));
        assertEquals("1-true-2.0", StringComplexFieldActions.concatenate(action, Arrays.asList(1, true, 2.0)));
        assertEquals("1-true-2.0", StringComplexFieldActions.concatenate(action, map));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConcatenateNonCollection() {
        StringComplexFieldActions.concatenate(new Concatenate(), "");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConcatenateNullAction() {
        StringComplexFieldActions.concatenate(null, null);
    }

    @Test
    public void testCurrentDate() {
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), null));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Byte(Byte.parseByte("0"))));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Character('a')));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Double(14324d)));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Float(234235235325f)));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Integer(32523)));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Long(235325L)));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new Short((short) 4323)));
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), ""));
    }

    @Test
    public void testCurrentDateTime() {
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentDateTime(), null));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Byte(Byte.parseByte("0"))));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Character('a')));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Double(14324d)));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Float(234235235325f)));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Integer(32523)));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Long(235325L)));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), new Short((short) 4323)));
        validateCurrentDateTime(StringComplexFieldActions.currentDateTime(new CurrentTime(), ""));
    }

    @Test
    public void testCurrentTime() {
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), null));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Byte(Byte.parseByte("0"))));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Character('a')));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Double(14324d)));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Float(234235235325f)));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Integer(32523)));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Long(235325L)));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new Short((short) 4323)));
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), ""));
    }

    @Test
    public void testEndsWith() {
        EndsWith action = new EndsWith();
        action.setString("");
        assertFalse(StringComplexFieldActions.endsWith(action, null));
        assertTrue(StringComplexFieldActions.endsWith(action, ""));
        assertTrue(StringComplexFieldActions.endsWith(action, "foo"));
        action.setString("bar");
        assertFalse(StringComplexFieldActions.endsWith(action, null));
        assertFalse(StringComplexFieldActions.endsWith(action, ""));
        assertFalse(StringComplexFieldActions.endsWith(action, "foo"));
        assertTrue(StringComplexFieldActions.endsWith(action, "foobar"));
        assertFalse(StringComplexFieldActions.endsWith(action, "barfoo"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEndsWithNullAction() {
        StringComplexFieldActions.endsWith(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEndsWithNullString() {
        StringComplexFieldActions.endsWith(new EndsWith(), null);
    }

    @Test
    public void testFormat() {
        Format action = new Format();
        action.setTemplate("foofoo");
        assertEquals("foofoo", StringComplexFieldActions.format(action, null));
        assertEquals("foofoo", StringComplexFieldActions.format(action, ""));
        assertEquals("foofoo", StringComplexFieldActions.format(action, "bar"));
        action.setTemplate("foo%sfoo");
        assertEquals("foonullfoo", StringComplexFieldActions.format(action, null));
        assertEquals("foofoo", StringComplexFieldActions.format(action, ""));
        assertEquals("foobarfoo", StringComplexFieldActions.format(action, "bar"));
        action.setTemplate("foo%1$sfoo%1$s");
        assertEquals("foobarfoobar", StringComplexFieldActions.format(action, "bar"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFormatNullAction() {
        StringComplexFieldActions.format(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFormatNullTemplate() {
        StringComplexFieldActions.format(new Format(), null);
    }

    @Test
    public void testGenareteUUID() {
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), null));
        validateGeneratedUUID(
                StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Byte(Byte.parseByte("0"))));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Character('a')));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Double(14324d)));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Float(234235235325f)));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Integer(32523)));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Long(235325L)));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new Short((short) 4323)));
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), ""));
    }

    @Test
    public void testIndexOf() {
        IndexOf action = new IndexOf();
        action.setString("");
        assertEquals(-1, StringComplexFieldActions.indexOf(action, null));
        assertEquals(0, StringComplexFieldActions.indexOf(action, ""));
        assertEquals(0, StringComplexFieldActions.indexOf(action, "foo"));
        action.setString("bar");
        assertEquals(-1, StringComplexFieldActions.indexOf(action, null));
        assertEquals(-1, StringComplexFieldActions.indexOf(action, ""));
        assertEquals(-1, StringComplexFieldActions.indexOf(action, "foo"));
        assertEquals(3, StringComplexFieldActions.indexOf(action, "foobar"));
        assertEquals(3, StringComplexFieldActions.indexOf(action, "foobarbar"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIndexOfNullAction() {
        StringComplexFieldActions.indexOf(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIndexOfNullString() {
        StringComplexFieldActions.indexOf(new IndexOf(), null);
    }

    @Test
    public void testLastIndexOf() {
        LastIndexOf action = new LastIndexOf();
        action.setString("");
        assertEquals(-1, StringComplexFieldActions.lastIndexOf(action, null));
        assertEquals(0, StringComplexFieldActions.lastIndexOf(action, ""));
        assertEquals(3, StringComplexFieldActions.lastIndexOf(action, "foo"));
        action.setString("bar");
        assertEquals(-1, StringComplexFieldActions.lastIndexOf(action, null));
        assertEquals(-1, StringComplexFieldActions.lastIndexOf(action, ""));
        assertEquals(-1, StringComplexFieldActions.lastIndexOf(action, "foo"));
        assertEquals(3, StringComplexFieldActions.lastIndexOf(action, "foobar"));
        assertEquals(6, StringComplexFieldActions.lastIndexOf(action, "foobarbar"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLastIndexOfNullAction() {
        StringComplexFieldActions.lastIndexOf(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLastIndexOfNullString() {
        StringComplexFieldActions.lastIndexOf(new LastIndexOf(), null);
    }

    @Test
    public void testPadStringLeft() {
        PadStringLeft padStringLeft = new PadStringLeft();
        padStringLeft.setPadCharacter("a");
        padStringLeft.setPadCount(3);

        assertEquals("aaa", StringComplexFieldActions.padStringLeft(padStringLeft, null));
        assertEquals("aaa", StringComplexFieldActions.padStringLeft(padStringLeft, ""));
        assertEquals("aaaa", StringComplexFieldActions.padStringLeft(padStringLeft, "a"));
        assertEquals("aaab", StringComplexFieldActions.padStringLeft(padStringLeft, "b"));

        try {
            StringComplexFieldActions.padStringLeft(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            StringComplexFieldActions.padStringLeft(new PadStringLeft(), "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            PadStringLeft incomplete = new PadStringLeft();
            incomplete.setPadCharacter("f");
            StringComplexFieldActions.padStringLeft(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            PadStringLeft incomplete = new PadStringLeft();
            incomplete.setPadCount(3);
            StringComplexFieldActions.padStringLeft(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testPadStringRight() {
        PadStringRight padStringRight = new PadStringRight();
        padStringRight.setPadCharacter("a");
        padStringRight.setPadCount(3);

        assertEquals("aaa", StringComplexFieldActions.padStringRight(padStringRight, null));
        assertEquals("aaa", StringComplexFieldActions.padStringRight(padStringRight, ""));
        assertEquals("aaaa", StringComplexFieldActions.padStringRight(padStringRight, "a"));
        assertEquals("baaa", StringComplexFieldActions.padStringRight(padStringRight, "b"));

        try {
            StringComplexFieldActions.padStringRight(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            StringComplexFieldActions.padStringRight(new PadStringRight(), "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            PadStringRight incomplete = new PadStringRight();
            incomplete.setPadCharacter("f");
            StringComplexFieldActions.padStringRight(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            PadStringRight incomplete = new PadStringRight();
            incomplete.setPadCount(3);
            StringComplexFieldActions.padStringRight(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testReplaceFirst() {
        ReplaceFirst replaceFirst = new ReplaceFirst();
        replaceFirst.setOldString(" ");
        assertNull(StringComplexFieldActions.replaceFirst(replaceFirst, null));
        assertEquals("", StringComplexFieldActions.replaceFirst(replaceFirst, ""));
        assertEquals("test", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setOldString("e");
        assertEquals("tst", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setOldString("t");
        replaceFirst.setNewString("h");
        assertEquals("hest", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setOldString("is");
        replaceFirst.setNewString("at");
        assertEquals("That is a test", StringComplexFieldActions.replaceFirst(replaceFirst, "This is a test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceFirstEmptyOldString() {
        ReplaceFirst replaceFirst = new ReplaceFirst();
        replaceFirst.setOldString("");
        StringComplexFieldActions.replaceFirst(replaceFirst, " ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testReplaceFirstNullAction() {
        StringComplexFieldActions.replaceFirst(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceFirstNullOldString() {
        ReplaceFirst replaceFirst = new ReplaceFirst();
        StringComplexFieldActions.replaceFirst(replaceFirst, " ");
    }

    @Test
    public void testReplaceAll() {
        ReplaceAll replaceAll = new ReplaceAll();
        replaceAll.setOldString(" ");
        assertNull(StringComplexFieldActions.replaceAll(replaceAll, null));
        assertEquals("", StringComplexFieldActions.replaceAll(replaceAll, ""));
        assertEquals("test", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setOldString("e");
        assertEquals("tst", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setOldString("t");
        replaceAll.setNewString("h");
        assertEquals("hesh", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setOldString("is");
        replaceAll.setNewString("at");
        assertEquals("That at a test", StringComplexFieldActions.replaceAll(replaceAll, "This is a test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceAllEmptyOldString() {
        ReplaceAll replaceAll = new ReplaceAll();
        replaceAll.setOldString("");
        StringComplexFieldActions.replaceAll(replaceAll, " ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testReplaceAllNullAction() {
        StringComplexFieldActions.replaceAll(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceAllNullOldString() {
        ReplaceAll replaceAll = new ReplaceAll();
        StringComplexFieldActions.replaceAll(replaceAll, " ");
    }

    @Test
    public void testStartsWith() {
        StartsWith action = new StartsWith();
        action.setString("");
        assertFalse(StringComplexFieldActions.startsWith(action, null));
        assertTrue(StringComplexFieldActions.startsWith(action, ""));
        assertTrue(StringComplexFieldActions.startsWith(action, "foo"));
        action.setString("foo");
        assertFalse(StringComplexFieldActions.startsWith(action, null));
        assertFalse(StringComplexFieldActions.startsWith(action, ""));
        assertTrue(StringComplexFieldActions.startsWith(action, "foo"));
        assertTrue(StringComplexFieldActions.startsWith(action, "foobar"));
        assertFalse(StringComplexFieldActions.startsWith(action, "barfoo"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testStartsWithNullAction() {
        StringComplexFieldActions.startsWith(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testStartsWithNullString() {
        StringComplexFieldActions.startsWith(new StartsWith(), null);
    }

    @Test
    public void testSubString() {
        SubString action = new SubString();
        action.setStartIndex(2);
        action.setEndIndex(4);

        assertNull(StringComplexFieldActions.subString(action, null));
        assertEquals("", StringComplexFieldActions.subString(action, ""));

        try {
            StringComplexFieldActions.subString(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSubStringAfter() {
        SubStringAfter action = new SubStringAfter();
        action.setStartIndex(3);
        action.setEndIndex(null);
        action.setMatch("foo");

        assertNull(StringComplexFieldActions.subStringAfter(action, null));
        assertEquals("", StringComplexFieldActions.subStringAfter(action, ""));
        assertEquals("blah", StringComplexFieldActions.subStringAfter(action, "foobarblah"));
        assertEquals("blahfoo", StringComplexFieldActions.subStringAfter(action, "foobarblahfoo"));

        assertEquals("barblah", StringComplexFieldActions.subStringAfter(action, "barblah"));

        try {
            StringComplexFieldActions.subStringAfter(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringAfter err = new SubStringAfter();
            StringComplexFieldActions.subStringAfter(err, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringAfter err = new SubStringAfter();
            err.setEndIndex(5);
            err.setStartIndex(0);
            StringComplexFieldActions.subStringAfter(err, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringAfter err = new SubStringAfter();
            err.setEndIndex(0);
            err.setStartIndex(5);
            StringComplexFieldActions.subStringAfter(err, "aa");
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

        assertNull(StringComplexFieldActions.subStringBefore(action, null));
        assertEquals("", StringComplexFieldActions.subStringBefore(action, ""));
        assertEquals("bar", StringComplexFieldActions.subStringBefore(action, "foobarblah"));
        assertEquals("foobar", StringComplexFieldActions.subStringBefore(action, "foofoobarblahfoo"));
        assertEquals("", StringComplexFieldActions.subStringBefore(action, "barblah"));

        try {
            StringComplexFieldActions.subStringBefore(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringBefore err = new SubStringBefore();
            StringComplexFieldActions.subStringBefore(err, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringBefore err = new SubStringBefore();
            err.setEndIndex(5);
            err.setStartIndex(0);
            StringComplexFieldActions.subStringBefore(err, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            SubStringBefore err = new SubStringBefore();
            err.setEndIndex(0);
            err.setStartIndex(5);
            StringComplexFieldActions.subStringBefore(err, "aa");
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
