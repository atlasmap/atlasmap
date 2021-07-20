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
package io.atlasmap.actions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

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
        assertEquals(null, StringComplexFieldActions.append(action, null));
        assertEquals("foo", StringComplexFieldActions.append(action, "foo"));
        assertEquals("1", StringComplexFieldActions.append(action, "1"));
        action.setString("");
        assertEquals("", StringComplexFieldActions.append(action, null));
        assertEquals("foo", StringComplexFieldActions.append(action, "foo"));
        action.setString("bar");
        assertEquals("bar", StringComplexFieldActions.append(action, null));
        assertEquals("foobar", StringComplexFieldActions.append(action, "foo"));
        assertEquals("1bar", StringComplexFieldActions.append(action, "1"));
    }

    @Test
    public void testAppendNullAction() {
        assertThrows(IllegalArgumentException.class, ()-> {
            StringComplexFieldActions.append(null, null);
        });
    }

    @Test
    public void testConcatenate() {
        Concatenate action = new Concatenate();

        assertEquals(null, StringComplexFieldActions.concatenate(action, null));
        assertEquals("1true2.0", StringComplexFieldActions.concatenate(action, Arrays.asList(new String[] {"1", "true", "2.0"})));
        assertEquals("1true2.0", StringComplexFieldActions.concatenate(action, Arrays.asList("1", "true", "2.0")));
        action.setDelimiter("-");
        assertEquals(null, StringComplexFieldActions.concatenate(action, null));
        assertEquals("1-true-2.0", StringComplexFieldActions.concatenate(action, Arrays.asList(new String[] {"1", "true", "2.0"})));
        assertEquals("1-true-2.0", StringComplexFieldActions.concatenate(action, Arrays.asList("1", "true", "2.0")));
    }

    @Test
    public void testConcatenateNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.concatenate(null, null);
        });
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

    @Test
    public void testEndsWithNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.endsWith(null, null);
        });
    }

    @Test
    public void testEndsWithNullString() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.endsWith(new EndsWith(), null);
        });
    }

    @Test
    public void testFormat() {
        Format action = new Format();
        action.setTemplate("foofoo");
        assertEquals("foofoo", StringComplexFieldActions.format(action, null));
        assertEquals("foofoo", StringComplexFieldActions.format(action, Arrays.asList("")));
        assertEquals("foofoo", StringComplexFieldActions.format(action, Arrays.asList("bar")));
        action.setTemplate("foo%sfoo");
        assertEquals("foonullfoo", StringComplexFieldActions.format(action, null));
        assertEquals("foofoo", StringComplexFieldActions.format(action, Arrays.asList("")));
        assertEquals("foobarfoo", StringComplexFieldActions.format(action, Arrays.asList("bar")));
        action.setTemplate("foo%1$sfoo%1$s");
        assertEquals("foobarfoobar", StringComplexFieldActions.format(action, Arrays.asList("bar")));
    }

    @Test
    public void testFormatNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.format(null, null);
        });
    }

    @Test
    public void testFormatNullTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.format(new Format(), null);
        });
    }

    @Test
    public void testGenareteUUID() {
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID()));
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

    @Test
    public void testIndexOfNullAction() {
        assertThrows(IllegalArgumentException.class, () ->{
            StringComplexFieldActions.indexOf(null, null);
        });
    }

    @Test
    public void testIndexOfNullString() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.indexOf(new IndexOf(), null);
        });
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

    @Test
    public void testLastIndexOfNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.lastIndexOf(null, null);
        });
    }

    @Test
    public void testLastIndexOfNullString() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.lastIndexOf(new LastIndexOf(), null);
        });
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
    public void testPrepend() {
        Prepend action = new Prepend();
        assertEquals(null, StringComplexFieldActions.prepend(action, null));
        assertEquals("foo", StringComplexFieldActions.prepend(action, "foo"));
        assertEquals("1", StringComplexFieldActions.prepend(action, "1"));
        action.setString("");
        assertEquals("", StringComplexFieldActions.prepend(action, null));
        assertEquals("foo", StringComplexFieldActions.prepend(action, "foo"));
        action.setString("bar");
        assertEquals("bar", StringComplexFieldActions.prepend(action, null));
        assertEquals("barfoo", StringComplexFieldActions.prepend(action, "foo"));
        assertEquals("bar1", StringComplexFieldActions.prepend(action, "1"));
    }

    @Test
    public void testPrependNullAction() {
        assertThrows(NullPointerException.class, () -> {
            StringComplexFieldActions.prepend(null, null);
        });
    }

    @Test
    public void testReplaceFirst() {
        ReplaceFirst replaceFirst = new ReplaceFirst();
        replaceFirst.setMatch(" ");
        assertNull(StringComplexFieldActions.replaceFirst(replaceFirst, null));
        assertEquals("", StringComplexFieldActions.replaceFirst(replaceFirst, ""));
        assertEquals("test", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setMatch("e");
        assertEquals("tst", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setMatch("t");
        replaceFirst.setNewString("h");
        assertEquals("hest", StringComplexFieldActions.replaceFirst(replaceFirst, "test"));
        replaceFirst.setMatch("is");
        replaceFirst.setNewString("at");
        assertEquals("That is a test", StringComplexFieldActions.replaceFirst(replaceFirst, "This is a test"));
    }

    @Test
    public void testReplaceFirstEmptyMatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReplaceFirst replaceFirst = new ReplaceFirst();
            replaceFirst.setMatch("");
            StringComplexFieldActions.replaceFirst(replaceFirst, " ");
        });
    }

    @Test
    public void testReplaceFirstNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.replaceFirst(null, null);
        });
    }

    @Test
    public void testReplaceFirstNullMatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReplaceFirst replaceFirst = new ReplaceFirst();
            StringComplexFieldActions.replaceFirst(replaceFirst, " ");
        });
    }

    @Test
    public void testReplaceAll() {
        ReplaceAll replaceAll = new ReplaceAll();
        replaceAll.setMatch(" ");
        assertNull(StringComplexFieldActions.replaceAll(replaceAll, null));
        assertEquals("", StringComplexFieldActions.replaceAll(replaceAll, ""));
        assertEquals("test", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setMatch("e");
        assertEquals("tst", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setMatch("t");
        replaceAll.setNewString("h");
        assertEquals("hesh", StringComplexFieldActions.replaceAll(replaceAll, "test"));
        replaceAll.setMatch("is");
        replaceAll.setNewString("at");
        assertEquals("That at a test", StringComplexFieldActions.replaceAll(replaceAll, "This is a test"));
    }

    @Test
    public void testReplaceAllEmptyMatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReplaceAll replaceAll = new ReplaceAll();
            replaceAll.setMatch("");
            StringComplexFieldActions.replaceAll(replaceAll, " ");
        });
    }

    @Test
    public void testReplaceAllNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.replaceAll(null, null);
        });
    }

    @Test
    public void testReplaceAllNullOldString() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReplaceAll replaceAll = new ReplaceAll();
            StringComplexFieldActions.replaceAll(replaceAll, " ");
        });
    }

    @Test
    public void testSplitNoDelimiter() {
        assertThrows(IllegalArgumentException.class, () -> {
            Split action = new Split();
            StringComplexFieldActions.split(action, "foobar");
        });
    }

    @Test
    public void testSplit() {
        Split action = new Split();
        action.setDelimiter(",");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1,2,3"));
    }

    @Test
    public void testSplitEscape() {
        Split action = new Split();
        action.setDelimiter("\\");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1\\2\\3"));
        action.setDelimiter("|");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1|2|3"));
        action.setDelimiter(".");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1.2.3"));
        action.setDelimiter("$");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1$2$3"));
    }

    @Test
    public void testSplitCollapse() {
        Split action = new Split();
        action.setCollapseRepeatingDelimiters(true);
        action.setDelimiter("\\");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1\\2\\\\3"));
        action.setDelimiter("|");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1|2|||3"));
        action.setDelimiter(".");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1..2...3"));
        action.setDelimiter("$");
        assertArrayEquals(null, StringComplexFieldActions.split(action, null));
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1$$$$2$$$$3"));
        action.setDelimiter(" ");
        assertArrayEquals(new String[] {"1", "2", "3"}, StringComplexFieldActions.split(action, "1  2     3"));
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

    @Test
    public void testStartsWithNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.startsWith(null, null);
        });
    }

    @Test
    public void testStartsWithNullString() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringComplexFieldActions.startsWith(new StartsWith(), null);
        });
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

        action.setEndIndex(7);
        assertEquals("blah", StringComplexFieldActions.subStringAfter(action, "foobarblahfoo"));

        action.setEndIndex(3);
        assertEquals("", StringComplexFieldActions.subStringAfter(action, "foobarblahfoo"));

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

        action.setEndIndex(5);
        assertEquals("ba", StringComplexFieldActions.subStringBefore(action, "foobarblah"));
        action.setEndIndex(3);
        assertEquals("", StringComplexFieldActions.subStringBefore(action, "foobarblah"));

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
