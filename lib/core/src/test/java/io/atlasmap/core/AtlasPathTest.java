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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import io.atlasmap.core.AtlasPath.SegmentContext;

public class AtlasPathTest {

    @Test
    public void testOneClass() {
        AtlasPath foo = new AtlasPath("");
        foo.appendField("user");
        assertEquals("/user", foo.toString());
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
        assertEquals("", new SegmentContext("").getName());
        assertEquals("foo", new SegmentContext("foo").getName());
        assertEquals("foo", new SegmentContext("foo[]").getName());
        assertEquals("foo", new SegmentContext("foo<>").getName());
        assertEquals("foo", new SegmentContext("foo{}").getName());

        assertEquals("foo", new SegmentContext("foo[0]").getName());
        assertEquals("foo", new SegmentContext("foo<1234>").getName());
        assertEquals("foo", new SegmentContext("foo{bar}").getName());

        // We do not try to outsmart busted paths
        assertEquals("foo[0", new SegmentContext("foo[0").getName());
        assertEquals("foo1234>", new SegmentContext("foo1234>").getName());
        assertEquals("foo}", new SegmentContext("foo}").getName());

        assertEquals("bar", new SegmentContext("foo:bar").getName());
        assertEquals("bar", new SegmentContext("foo:@bar").getName());
        assertEquals("bar", new SegmentContext("@bar").getName());
    }

    @Test
    public void testGetLastSegmentParent() {
        AtlasPath p = new AtlasPath("/orders/contact/firstName");
        assertEquals("contact", p.getLastSegmentParent().getName());
        assertTrue(new AtlasPath("orders").getLastSegmentParent().isRoot());
    }

    @Test
    public void testGetLastSegmentParentPath() {
        AtlasPath p = new AtlasPath("/orders[]/contact/firstName");
        assertEquals("/orders[]/contact", p.getLastSegmentParentPath().toString());
        assertEquals("/", new AtlasPath("orders").getLastSegmentParentPath().toString());
    }

    @Test
    public void testCollectionIndexHandling() {
        assertNull(new SegmentContext("").getCollectionIndex());
        assertNull(new SegmentContext("foo").getCollectionIndex());
        assertNull(new SegmentContext("foo[]").getCollectionIndex());
        assertNull(new SegmentContext("foo<>").getCollectionIndex());
        assertNull(new SegmentContext("foo{}").getCollectionIndex());

        assertEquals(new Integer(0), new SegmentContext("foo[0]").getCollectionIndex());
        assertEquals(new Integer(1234), new SegmentContext("foo<1234>").getCollectionIndex());
        assertNull(new SegmentContext("foo{bar}").getCollectionIndex());

        AtlasPath p = new AtlasPath("/orders[4]/contact/firstName");
        p.setCollectionIndex(1, 5);
        assertEquals("/orders[5]/contact/firstName", p.toString());
        assertEquals(new Integer(5), p.getSegments(true).get(1).getCollectionIndex());

        try {
            p.setCollectionIndex(1, -3);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        AtlasPath q = new AtlasPath("/orders<4>/contact/firstName");
        q.setCollectionIndex(1, 6);
        assertEquals("/orders<6>/contact/firstName", q.toString());
        assertEquals(new Integer(6), q.getSegments(true).get(1).getCollectionIndex());

        AtlasPath r = new AtlasPath("/orders<>/contact/firstName");
        assertEquals("orders<>", r.getSegments(true).get(1).getExpression());
        r.setCollectionIndex(1, 6);
        assertEquals("/orders<6>/contact/firstName", r.toString());
        assertEquals(new Integer(6), r.getSegments(false).get(0).getCollectionIndex());
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
    public void testGetLastSegment() {
        AtlasPath p = new AtlasPath("/order/contact/firstName");
        assertEquals("firstName", p.getLastSegment().getName());
        assertEquals("firstName", p.getLastSegment().getExpression());

        assertTrue((new AtlasPath("")).getLastSegment().isRoot());
        assertNotNull(p.getOriginalPath());
    }

    @Test
    public void testHasCollection() {
        AtlasPath p = new AtlasPath("/order/contact/firstName");
        assertFalse(p.hasCollection());
    }

    @Test
    public void testIsCollectionRoot() {
        AtlasPath p = new AtlasPath("/order/contact/firstName");
        assertFalse(p.isCollectionRoot());

        p = new AtlasPath("/order/contact/phone<>");
        assertFalse(p.isCollectionRoot());

        p = new AtlasPath("/order<>/contact/phone");
        assertFalse(p.isCollectionRoot());

        p = new AtlasPath("/<>/order/contact/phone");
        assertFalse(p.isCollectionRoot());
    }

    @Test
    public void testIsAttributeSegment() {
        assertFalse(new AtlasPath((String)null).getSegments(true).get(0).isAttribute());
        assertFalse(new AtlasPath("order").getSegments(false).get(0).isAttribute());
        assertTrue(new AtlasPath("@order").getSegments(false).get(0).isAttribute());
    }

    @Test
    public void testGetCollectionIndex() {
        AtlasPath p = new AtlasPath("orders/order[1]");
        assertEquals(new Integer(1), p.getSegments(false).get(1).getCollectionIndex());

        p = new AtlasPath("order");
        assertEquals(null, p.getSegments(false).get(0).getCollectionIndex());

        p = new AtlasPath("orders/order<2>");
        assertEquals(new Integer(2), p.getSegments(false).get(1).getCollectionIndex());
    }

    @Test
    public void testGetSegments() {
        AtlasPath path = new AtlasPath("/orders");
        assertNotNull(path.getSegments(true));

        List<SegmentContext> segmentContexts = path.getSegments(true);
        assertNotNull(segmentContexts);
        assertEquals(2, segmentContexts.size());

        SegmentContext sc = segmentContexts.get(1);
        assertEquals("orders", sc.getName());
        assertNull(sc.getCollectionIndex());
        assertEquals("orders", sc.getExpression());
        assertNotNull(sc.toString());
    }

    @Test
    public void testGetParentSegmentOf() throws Exception {
        AtlasPath path = new AtlasPath("/orders/order<4>/product<6>/name");
        List<SegmentContext> segments = path.getSegments(true);
        assertEquals(null, path.getParentSegmentOf(segments.get(0)));
        assertEquals(segments.get(0), path.getParentSegmentOf(segments.get(1)));
        assertEquals(segments.get(1), path.getParentSegmentOf(segments.get(2)));
        assertEquals(segments.get(2), path.getParentSegmentOf(segments.get(3)));
        assertEquals(segments.get(3), path.getParentSegmentOf(segments.get(4)));
    }

}
