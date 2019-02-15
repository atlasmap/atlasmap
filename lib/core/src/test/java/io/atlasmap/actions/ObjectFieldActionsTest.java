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
import io.atlasmap.v2.IsNull;
import io.atlasmap.v2.ItemAt;
import io.atlasmap.v2.Length;

public class ObjectFieldActionsTest {

    @Test
    public void testCollectionSize() {
        CollectionSize action = new CollectionSize();
        assertEquals(new Integer(0), action.collectionSize(new Boolean[0]));
        assertEquals(new Integer(0), action.collectionSize(new ArrayList<>()));
        assertEquals(new Integer(0), action.collectionSize(new HashMap<>()));
        Object[] array = new Object[] {false, "foo", 2};
        assertEquals(new Integer(3), action.collectionSize(array));
        assertEquals(new Integer(3), action.collectionSize(Arrays.asList(array)));
        Map<Object, Object> map = new HashMap<>();
        for (Object obj : array) {
            map.put(obj, obj);
        }
        assertEquals(new Integer(3), action.collectionSize(map));
    }

    @Test
    public void testContains() {
        Contains action = new Contains();
        assertTrue(action.contains(null));
        assertFalse(action.contains(""));
        Object[] array = new Object[] {false, "foo", 2};
        Object[] arrayWithNull = new Object[] {false, null, "foo", 2};
        assertFalse(action.contains(array));
        assertTrue(action.contains(arrayWithNull));
        assertFalse(action.contains(Arrays.asList(array)));
        assertTrue(action.contains(Arrays.asList(arrayWithNull)));
        Map<Object, Object> map = new HashMap<>();
        for (Object obj : array) {
            map.put("key-" + obj, obj);
        }
        assertFalse(action.contains(map));
        for (Object obj : arrayWithNull) {
            map.put("key-" + obj, obj);
        }
        assertTrue(action.contains(map));
        action.setValue("foo");
        assertFalse(action.contains(null));
        assertFalse(action.contains(""));
        assertTrue(action.contains("foobar"));
        assertTrue(action.contains(array));
        assertTrue(action.contains(Arrays.asList(array)));
        assertTrue(action.contains(map));
        action.setValue("key-foo");
        assertTrue(action.contains(map));
        action.setValue("6");
        assertTrue(action.contains(169));
        action.setValue("ru");
        assertTrue(action.contains(true));
    }

    @Test
    public void testEquals() {
        Equals action = new Equals();
        assertTrue(action.execute(null));
        action.setValue("6");
        assertFalse(action.execute(169));
        action.setValue("169");
        assertTrue(action.execute(169));
        action.setValue("ru");
        assertFalse(action.execute(true));
        action.setValue("true");
        assertTrue(action.execute(true));
        action.setValue("b");
        assertFalse(action.execute('a'));
        action.setValue("a");
        assertTrue(action.execute('a'));
    }

    @Test
    public void testIsNull() {
        IsNull action = new IsNull();
        assertTrue(action.isNull(null));
        assertFalse(action.isNull(""));
        assertFalse(action.isNull(new Object[0]));
    }

    @Test
    public void testItemAt() {
        ItemAt action = new ItemAt();
        action.setIndex(0);
        assertEquals("one", action.itemAt(new String[] {"one", "two"}));
        action.setIndex(1);
        assertEquals("two", action.itemAt(new String[] {"one", "two"}));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testItemAtOutOfBounds() {
        ItemAt action = new ItemAt();
        action.setIndex(2);
        action.itemAt(new String[] {"one", "two"});
    }

    @Test
    public void testLength() {
        Length action = new Length();
        assertEquals(new Integer(-1), action.length(null));
        assertEquals(new Integer(0), action.length(""));
        assertEquals(new Integer(5), action.length(" foo "));
        assertEquals(new Integer(4), action.length(true));
        assertEquals(new Integer(3), action.length(169));
    }
}
