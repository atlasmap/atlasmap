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
package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AtlasPathTest {

    @Test
    public void testOneClass() {
        AtlasPath foo = new AtlasPath("");
        foo.appendField("user");
        assertEquals("user", foo.toString());
    }

    @Test
    public void testFields() {
        AtlasPath foo = new AtlasPath("");
        foo.appendField("user").appendField("name");
        assertEquals("/user/name", foo.toString());
        foo.appendField("bar");
        assertEquals("/user/name/bar", foo.toString());
    }

    @Test
    public void testCleanPathSegment() {
        assertNull(AtlasPath.cleanPathSegment(null));
        assertEquals("", AtlasPath.cleanPathSegment(""));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo[]"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo<>"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo{}"));

        assertEquals("foo", AtlasPath.cleanPathSegment("foo"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo[0]"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo<1234>"));
        assertEquals("foo", AtlasPath.cleanPathSegment("foo{bar}"));

        // We do not try to outsmart busted paths
        assertEquals("foo[0", AtlasPath.cleanPathSegment("foo[0"));
        assertEquals("foo1234>", AtlasPath.cleanPathSegment("foo1234>"));
        assertEquals("foo}", AtlasPath.cleanPathSegment("foo}"));
    }

    @Test
    public void testGetLastSegmentParent() {
        AtlasPath p = new AtlasPath("/orders/contact/firstName");
        assertEquals("contact", p.getLastSegmentParent());
    }

    @Test
    public void testGetLastSegmentParentPath() {
        AtlasPath p = new AtlasPath("/orders[]/contact/firstName");
        assertEquals("/orders[]/contact", p.getLastSegmentParentPath().toString());
    }

    @Test
    public void testDeParentify() {
        AtlasPath p = new AtlasPath("/orders[]/contact/firstName");
        assertEquals("/contact/firstName", p.deParentify().toString());

        p = new AtlasPath("/orders/contact[]/firstName");
        assertEquals("/contact[]/firstName", p.deParentify().toString());
    }

    @Test
    public void testDeCollectionfy() {
        AtlasPath p = new AtlasPath("/orders[]/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[]").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[3]").toString());

        p = new AtlasPath("/orders/contact[]/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact[]").toString());
        assertEquals("firstName", p.deCollectionify("contact[3]").toString());

        p = new AtlasPath("/orders<>/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<>").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<3>").toString());

        p = new AtlasPath("/orders/contact<>/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact<>").toString());
        assertEquals("firstName", p.deCollectionify("contact<3>").toString());
    }

    @Test
    public void testCollectionIndexHandling() {
        assertNull(AtlasPath.indexOfSegment(null));
        assertNull(AtlasPath.indexOfSegment(""));
        assertNull(AtlasPath.indexOfSegment("foo"));
        assertNull(AtlasPath.indexOfSegment("foo[]"));
        assertNull(AtlasPath.indexOfSegment("foo<>"));
        assertNull(AtlasPath.indexOfSegment("foo{}"));

        assertEquals(new Integer(0), AtlasPath.indexOfSegment("foo[0]"));
        assertEquals(new Integer(1234), AtlasPath.indexOfSegment("foo<1234>"));
        assertNull(AtlasPath.indexOfSegment("foo{bar}"));

        AtlasPath p = new AtlasPath("/orders[4]/contact/firstName");
        p.setCollectionIndex("orders[4]", 5);
        assertEquals("/orders[5]/contact/firstName", p.toString());
        assertEquals(new Integer(5), AtlasPath.indexOfSegment("orders[5]"));

        try {
            p.setCollectionIndex("orders<>", -3);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            p.setCollectionIndex("orders{}", 3);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            p.setCollectionIndex("orders", 3);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            p.setCollectionIndex("/orders/contact/foo", 3);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        AtlasPath q = new AtlasPath("/orders<4>/contact/firstName");
        q.setCollectionIndex("orders<7>", 6);
        assertEquals("/orders<6>/contact/firstName", q.toString());
        assertEquals(new Integer(5), AtlasPath.indexOfSegment("orders<5>"));

        AtlasPath r = new AtlasPath("/orders<>/contact/firstName");
        assertEquals("orders<>", r.getCollectionSegment());
        r.setCollectionIndex("orders<>", 6);
        assertEquals("/orders<6>/contact/firstName", r.toString());
        assertEquals(new Integer(6), AtlasPath.indexOfSegment(r.getCollectionSegment()));
    }

    @Test
    public void testIsIndexedCollection() {
        AtlasPath p = null;

        p = new AtlasPath("order");
        assertFalse(p.isIndexedCollection());

        p = new AtlasPath("/order/contact/firstName");
        assertFalse(p.isIndexedCollection());

        p = new AtlasPath("order[]");
        assertFalse(p.isIndexedCollection());

        p = new AtlasPath("/orders[4]/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/orders[0]/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/orders[]/contact/firstName");
        assertFalse(p.isIndexedCollection());

        p = new AtlasPath("orders<>");
        assertFalse(p.isIndexedCollection());

        p = new AtlasPath("orders<6>");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/foo/orders<6>");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/foo/orders<6>/bar");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/orders<3>/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/orders<0>/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new AtlasPath("/orders<>/contact/firstName");
        assertFalse(p.isIndexedCollection());

    }

    @Test
    public void testRemoveCollectionIndexes() {
        // no collection cases
        assertEquals(null, AtlasPath.removeCollectionIndex(null));
        assertEquals("blah", AtlasPath.removeCollectionIndex("blah"));
        assertEquals("@blah", AtlasPath.removeCollectionIndex("@blah"));
        assertEquals("@x:blah", AtlasPath.removeCollectionIndex("@x:blah"));

        // cases with already empty collections
        assertEquals("blah[]", AtlasPath.removeCollectionIndex("blah[]"));
        assertEquals("@blah[]", AtlasPath.removeCollectionIndex("@blah[]"));
        assertEquals("@x:blah[]", AtlasPath.removeCollectionIndex("@x:blah[]"));
        assertEquals("blah<>", AtlasPath.removeCollectionIndex("blah<>"));
        assertEquals("@blah<>", AtlasPath.removeCollectionIndex("@blah<>"));
        assertEquals("@x:blah<>", AtlasPath.removeCollectionIndex("@x:blah<>"));
        assertEquals("blah{}", AtlasPath.removeCollectionIndex("blah{}"));
        assertEquals("@blah{}", AtlasPath.removeCollectionIndex("@blah{}"));
        assertEquals("@x:blah{}", AtlasPath.removeCollectionIndex("@x:blah{}"));

        // cases with stuff in collections
        assertEquals("blah[]", AtlasPath.removeCollectionIndex("blah[8]"));
        assertEquals("@blah[]", AtlasPath.removeCollectionIndex("@blah[955]"));
        assertEquals("@x:blah[]", AtlasPath.removeCollectionIndex("@x:blah[800]"));
        assertEquals("blah<>", AtlasPath.removeCollectionIndex("blah<5>"));
        assertEquals("@blah<>", AtlasPath.removeCollectionIndex("@blah<6>"));
        assertEquals("@x:blah<>", AtlasPath.removeCollectionIndex("@x:blah<75555>"));
        assertEquals("blah{}", AtlasPath.removeCollectionIndex("blah{5}"));
        assertEquals("@blah{}", AtlasPath.removeCollectionIndex("@blah{6}"));
        assertEquals("@x:blah{}", AtlasPath.removeCollectionIndex("@x:blah{65565657}"));

        // strange cases with malformed collections
        testMalformed("blah");
        testMalformed("@blah");
        testMalformed("@x:blah");

        assertEquals("/a/b[]/c/d[]/e{}/f<>/g[112/x{/z>>11",
                AtlasPath.removeCollectionIndexes("/a/b[]/c/d[15]/e{11}/f<111>/g[112/x{/z>>11"));
    }

    public void testMalformed(String var) {
        assertEquals(var + "[", AtlasPath.removeCollectionIndex(var + "["));
        assertEquals(var + "]", AtlasPath.removeCollectionIndex(var + "]"));
        assertEquals(var + "<", AtlasPath.removeCollectionIndex(var + "<"));
        assertEquals(var + ">", AtlasPath.removeCollectionIndex(var + ">"));
        assertEquals(var + "{", AtlasPath.removeCollectionIndex(var + "{"));
        assertEquals(var + "}", AtlasPath.removeCollectionIndex(var + "}"));
        assertEquals(var + "{{", AtlasPath.removeCollectionIndex(var + "{{"));
        assertEquals(var + "}}", AtlasPath.removeCollectionIndex(var + "}}"));
    }
}
