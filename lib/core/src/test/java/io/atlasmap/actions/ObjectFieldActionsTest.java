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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.atlasmap.v2.Contains;
import io.atlasmap.v2.Count;
import io.atlasmap.v2.Equals;
import io.atlasmap.v2.ItemAt;
import io.atlasmap.v2.Length;

public class ObjectFieldActionsTest {

    @Test
    public void testCount() {
        assertEquals(Integer.valueOf(0), ObjectFieldActions.count(new Count(), new ArrayList<>()));
        Object[] array = new Object[] {false, "foo", 2};
        assertEquals(Integer.valueOf(3), ObjectFieldActions.count(new Count(), Arrays.asList(array)));
    }

    @Test
    public void testContains() {
        Contains action = new Contains();
        assertTrue(ObjectFieldActions.contains(action, null));
        assertFalse(ObjectFieldActions.contains(action, Arrays.asList(new Object[] {""})));
        Object[] array = new Object[] {false, "foo", 2};
        Object[] arrayWithNull = new Object[] {false, null, "foo", 2};
        assertFalse(ObjectFieldActions.contains(action, Arrays.asList(array)));
        assertTrue(ObjectFieldActions.contains(action, Arrays.asList(arrayWithNull)));
        action.setValue("foo");
        assertFalse(ObjectFieldActions.contains(action, null));
        assertFalse(ObjectFieldActions.contains(action, Arrays.asList(new Object[] {""})));
        assertFalse(ObjectFieldActions.contains(action, Arrays.asList(new Object[] {"foobar"})));
        assertTrue(ObjectFieldActions.contains(action, Arrays.asList(array)));
    }

    @Test
    public void testContainsWithNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectFieldActions.contains(null, Arrays.asList(new Object[] {""}));
        });
    }

    @Test
    public void testEquals() {
        Equals action = new Equals();
        assertTrue(ObjectFieldActions.equals(action, null));
        action.setValue("6");
        assertFalse(ObjectFieldActions.equals(action, 169));
        action.setValue("169");
        assertTrue(ObjectFieldActions.equals(action, 169));
        action.setValue("ru");
        assertFalse(ObjectFieldActions.equals(action, true));
        action.setValue("true");
        assertTrue(ObjectFieldActions.equals(action, true));
        action.setValue("b");
        assertFalse(ObjectFieldActions.equals(action, 'a'));
        action.setValue("a");
        assertTrue(ObjectFieldActions.equals(action, 'a'));
    }

    @Test
    public void testEqualsWithNullAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectFieldActions.equals(null, "");
        });
    }

    @Test
    public void testIsNull() {
        assertTrue(ObjectFieldActions.isNull(null, null));
        assertFalse(ObjectFieldActions.isNull(null, ""));
        assertFalse(ObjectFieldActions.isNull(null, new Object[0]));
    }

    @Test
    public void testItemAt() {
        ItemAt action = new ItemAt();
        action.setIndex(0);
        assertEquals("one", ObjectFieldActions.itemAt(action, Arrays.asList(new Object[] {"one", "two"})));
        action.setIndex(1);
        assertEquals("two", ObjectFieldActions.itemAt(action, Arrays.asList(new Object[] {"one", "two"})));
    }

    @Test
    public void testItemAtOutOfBounds() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            ItemAt action = new ItemAt();
            action.setIndex(2);
            ObjectFieldActions.itemAt(action, Arrays.asList(new Object[] {"one", "two"}));
        });
    }

    @Test
    public void testLength() {
        assertEquals(Integer.valueOf(-1), ObjectFieldActions.length(new Length(), null));
        assertEquals(Integer.valueOf(0), ObjectFieldActions.length(new Length(), ""));
        assertEquals(Integer.valueOf(5), ObjectFieldActions.length(new Length(), " foo "));
        assertEquals(Integer.valueOf(4), ObjectFieldActions.length(new Length(), true));
        assertEquals(Integer.valueOf(3), ObjectFieldActions.length(new Length(), 169));
    }
}
