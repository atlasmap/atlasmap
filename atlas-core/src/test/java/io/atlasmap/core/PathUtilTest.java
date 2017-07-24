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

import io.atlasmap.core.PathUtil;

public class PathUtilTest { 
    
    @Test
    public void testOneClass() {
        PathUtil foo = new PathUtil();
        foo.appendField("user");
        assertEquals("user", foo.toString());
    }

    
    @Test
    public void testFields() {
        PathUtil foo = new PathUtil();
        foo.appendField("user").appendField("name");
        assertEquals("/user/name", foo.toString());
        foo.appendField("bar");
        assertEquals("/user/name/bar", foo.toString());
    }
    
    @Test
    public void testCleanPathSegment() {
        assertNull(PathUtil.cleanPathSegment(null));
        assertEquals("", PathUtil.cleanPathSegment(""));
        assertEquals("foo", PathUtil.cleanPathSegment("foo"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo[]"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo<>"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo{}"));
        
        assertEquals("foo", PathUtil.cleanPathSegment("foo"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo[0]"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo<1234>"));
        assertEquals("foo", PathUtil.cleanPathSegment("foo{bar}"));
        
        // We do not try to outsmart busted paths
        assertEquals("foo[0", PathUtil.cleanPathSegment("foo[0"));
        assertEquals("foo1234>", PathUtil.cleanPathSegment("foo1234>"));
        assertEquals("foo}", PathUtil.cleanPathSegment("foo}"));
    }
    
    @Test
    public void testGetLastSegmentParent() {
        PathUtil p = new PathUtil("/orders/contact/firstName");
        assertEquals("contact", p.getLastSegmentParent());
    }
    
    @Test
    public void testGetLastSegmentParentPath() {
        PathUtil p = new PathUtil("/orders[]/contact/firstName");
        assertEquals("/orders[]/contact", p.getLastSegmentParentPath().toString());
    }
    
    @Test
    public void testDeParentify() {
        PathUtil p = new PathUtil("/orders[]/contact/firstName");
        assertEquals("/contact/firstName", p.deParentify().toString());
        
        p = new PathUtil("/orders/contact[]/firstName");
        assertEquals("/contact[]/firstName", p.deParentify().toString());
    }
    
    @Test
    public void testDeCollectionfy() {
        PathUtil p = new PathUtil("/orders[]/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[]").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[3]").toString());

        p = new PathUtil("/orders/contact[]/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact[]").toString());
        assertEquals("firstName", p.deCollectionify("contact[3]").toString());
        
        p = new PathUtil("/orders<>/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<>").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<3>").toString());

        p = new PathUtil("/orders/contact<>/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact<>").toString());
        assertEquals("firstName", p.deCollectionify("contact<3>").toString());
    }
    
    @Test
    public void testCollectionIndexHandling() {
        assertNull(PathUtil.indexOfSegment(null));
        assertNull(PathUtil.indexOfSegment(""));
        assertNull(PathUtil.indexOfSegment("foo"));
        assertNull(PathUtil.indexOfSegment("foo[]"));
        assertNull(PathUtil.indexOfSegment("foo<>"));
        assertNull(PathUtil.indexOfSegment("foo{}"));
            
        assertEquals(new Integer(0), PathUtil.indexOfSegment("foo[0]"));
        assertEquals(new Integer(1234), PathUtil.indexOfSegment("foo<1234>"));
        assertNull(PathUtil.indexOfSegment("foo{bar}"));
        
        PathUtil p = new PathUtil("/orders[4]/contact/firstName");
        p.setCollectionIndex("orders[4]", 5);
        assertEquals("/orders[5]/contact/firstName", p.toString());
        assertEquals(new Integer(5), PathUtil.indexOfSegment("orders[5]"));
        
        try { p.setCollectionIndex("orders<>", -3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
        try { p.setCollectionIndex("orders{}", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
        try { p.setCollectionIndex("orders", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
        try { p.setCollectionIndex("/orders/contact/foo", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
        
        PathUtil q = new PathUtil("/orders<4>/contact/firstName");
        q.setCollectionIndex("orders<7>", 6);
        assertEquals("/orders<6>/contact/firstName", q.toString());
        assertEquals(new Integer(5), PathUtil.indexOfSegment("orders<5>"));
        
        PathUtil r = new PathUtil("/orders<>/contact/firstName");
        assertEquals("orders<>", r.getCollectionSegment());
        r.setCollectionIndex("orders<>", 6);
        assertEquals("/orders<6>/contact/firstName", r.toString());
        assertEquals(new Integer(6), PathUtil.indexOfSegment(r.getCollectionSegment()));
    }
    
    @Test
    public void testIsIndexedCollection() {
        PathUtil p = null;
        
        p = new PathUtil("order");
        assertFalse(p.isIndexedCollection());
            
        p = new PathUtil("/order/contact/firstName");
        assertFalse(p.isIndexedCollection());

        p = new PathUtil("order[]");
        assertFalse(p.isIndexedCollection());

        p = new PathUtil("/orders[4]/contact/firstName");
        assertTrue(p.isIndexedCollection());
        
        p = new PathUtil("/orders[0]/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new PathUtil("/orders[]/contact/firstName");
        assertFalse(p.isIndexedCollection());
        
        p = new PathUtil("orders<>");
        assertFalse(p.isIndexedCollection());
        
        p = new PathUtil("orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new PathUtil("/foo/orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new PathUtil("/foo/orders<6>/bar");
        assertTrue(p.isIndexedCollection());
        
        p = new PathUtil("/orders<3>/contact/firstName");
        assertTrue(p.isIndexedCollection());
            
        p = new PathUtil("/orders<0>/contact/firstName");
        assertTrue(p.isIndexedCollection());

        p = new PathUtil("/orders<>/contact/firstName");
        assertFalse(p.isIndexedCollection());

    }
}
