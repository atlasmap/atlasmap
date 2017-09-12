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

import java.util.regex.Pattern;

import org.junit.Test;

import io.atlasmap.v2.CurrentDate;
import io.atlasmap.v2.CurrentTime;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.PadStringLeft;
import io.atlasmap.v2.PadStringRight;
import io.atlasmap.v2.Replace;
import io.atlasmap.v2.SubString;
import io.atlasmap.v2.SubStringAfter;
import io.atlasmap.v2.SubStringBefore;

public class StringComplexFieldActionsTest {

    protected void validateGeneratedUUID(String uuid) {
        assertNotNull(uuid);
        assertTrue(uuid.length() > 0);
        assertTrue(Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}").matcher(uuid)
                .matches());
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
        validateGeneratedUUID(StringComplexFieldActions.genareteUUID(new GenerateUUID(), new String()));
    }

    protected void validateCurrentDate(String dateValue) {
        assertNotNull(dateValue);
        assertTrue(dateValue.length() > 0);
        assertTrue(Pattern.compile("20([1-9][0-9])-(0[0-9]|1[0-2])-(0[0-9]|1[0-9]|2[0-9]|3[0-1])").matcher(dateValue)
                .matches());
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
        validateCurrentDate(StringComplexFieldActions.currentDate(new CurrentDate(), new String()));
    }

    protected void validateCurrentTime(String timeValue) {
        assertNotNull(timeValue);
        assertTrue(timeValue.length() > 0);
        assertTrue(Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]").matcher(timeValue).matches());
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
        validateCurrentTime(StringComplexFieldActions.currentTime(new CurrentTime(), new String()));
    }

    @Test
    public void testCurrentDateTime() {

    }

    @Test
    public void testPadStringRight() {
        PadStringRight padStringRight = new PadStringRight();
        padStringRight.setPadCharacter("a");
        padStringRight.setPadCount(3);

        assertNull(StringComplexFieldActions.padStringRight(padStringRight, null));
        assertEquals("aaa", StringComplexFieldActions.padStringRight(padStringRight, ""));
        assertEquals("aaaa", StringComplexFieldActions.padStringRight(padStringRight, "a"));
        assertEquals("baaa", StringComplexFieldActions.padStringRight(padStringRight, "b"));

        try {
            StringComplexFieldActions.padStringRight(null, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            StringComplexFieldActions.padStringRight(new PadStringRight(), "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            PadStringRight incomplete = new PadStringRight();
            incomplete.setPadCharacter("f");
            StringComplexFieldActions.padStringRight(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            PadStringRight incomplete = new PadStringRight();
            incomplete.setPadCount(3);
            StringComplexFieldActions.padStringRight(incomplete, "aa");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testPadStringLeft() {
        PadStringLeft padStringLeft = new PadStringLeft();
        padStringLeft.setPadCharacter("a");
        padStringLeft.setPadCount(3);

        assertNull(StringComplexFieldActions.padStringLeft(padStringLeft, null));
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

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceFailsWithNullOldString() {
        Replace replace = new Replace();
        StringComplexFieldActions.replace(replace, " ");
    }

      @Test(expected = IllegalArgumentException.class)
      public void testReplaceFailsWithEmptyOldString() {
          Replace replace = new Replace();
          replace.setOldString("");
          StringComplexFieldActions.replace(replace, " ");
      }

    @Test
    public void testReplace() {
        Replace replace = new Replace();
        assertNull(StringComplexFieldActions.replace(replace, null));
        assertEquals("", StringComplexFieldActions.replace(replace, ""));
        replace.setOldString(" ");
        assertEquals("test", StringComplexFieldActions.replace(replace, "test"));
        replace.setOldString("e");
        assertEquals("tst", StringComplexFieldActions.replace(replace, "test"));
        replace.setOldString("t");
        replace.setNewString("h");
        assertEquals("hesh", StringComplexFieldActions.replace(replace, "test"));
        replace.setOldString("is");
        replace.setNewString("at");
        assertEquals("That at a test", StringComplexFieldActions.replace(replace, "This is a test"));
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

}
