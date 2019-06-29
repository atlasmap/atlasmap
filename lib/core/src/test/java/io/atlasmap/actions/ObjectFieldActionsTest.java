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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.atlasmap.v2.CollectionSize;
import io.atlasmap.v2.Contains;
import io.atlasmap.v2.Equals;
import io.atlasmap.v2.ItemAt;
import io.atlasmap.v2.Length;

public class ObjectFieldActionsTest {

    @Test
    public void testCollectionSize() {
        assertEquals(new Integer(0), ObjectFieldActions.collectionSize(new CollectionSize(), new ArrayList<>()));
        Object[] array = new Object[] {false, "foo", 2};
        assertEquals(new Integer(3), ObjectFieldActions.collectionSize(new CollectionSize(), Arrays.asList(array)));
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

    @Test(expected=IllegalArgumentException.class)
    public void testContainsWithNullAction() {
        ObjectFieldActions.contains(null, Arrays.asList(new Object[] {""}));
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

    @Test(expected=IllegalArgumentException.class)
    public void testEqualsWithNullAction() {
        ObjectFieldActions.equals(null, "");
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

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testItemAtOutOfBounds() {
        ItemAt action = new ItemAt();
        action.setIndex(2);
        ObjectFieldActions.itemAt(action, Arrays.asList(new Object[] {"one", "two"}));
    }

    @Test
    public void testLength() {
        assertEquals(new Integer(-1), ObjectFieldActions.length(new Length(), null));
        assertEquals(new Integer(0), ObjectFieldActions.length(new Length(), ""));
        assertEquals(new Integer(5), ObjectFieldActions.length(new Length(), " foo "));
        assertEquals(new Integer(4), ObjectFieldActions.length(new Length(), true));
        assertEquals(new Integer(3), ObjectFieldActions.length(new Length(), 169));
    }
}
