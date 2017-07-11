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
package io.atlasmap.java.inspect;

import static org.junit.Assert.*;

import org.junit.Test;

import io.atlasmap.java.inspect.JavaPath;

public class JavaPathTest {	
	
	@Test
	public void testOneClass() {
		JavaPath foo = new JavaPath();
		foo.appendField("user");
		assertEquals("user", foo.toString());
	}

	
	@Test
	public void testFields() {
		JavaPath foo = new JavaPath();
		foo.appendField("user").appendField("name");
		assertEquals("user.name", foo.toString());
		foo.appendField("bar");
		assertEquals("user.name.bar", foo.toString());
	}
	
	@Test
	public void testCleanPathSegment() {
	    assertNull(JavaPath.cleanPathSegment(null));
	    assertEquals("", JavaPath.cleanPathSegment(""));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo[]"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo<>"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo{}"));
	    
	    assertEquals("foo", JavaPath.cleanPathSegment("foo"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo[0]"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo<1234>"));
	    assertEquals("foo", JavaPath.cleanPathSegment("foo{bar}"));
	    
	    // We do not try to outsmart busted paths
        assertEquals("foo[0", JavaPath.cleanPathSegment("foo[0"));
        assertEquals("foo1234>", JavaPath.cleanPathSegment("foo1234>"));
        assertEquals("foo}", JavaPath.cleanPathSegment("foo}"));
	}
	
	@Test
	public void testGetLastSegmentParent() {
	    JavaPath p = new JavaPath("orders.contact.firstName");
	    assertEquals("contact", p.getLastSegmentParent());
	}
	
	@Test
	public void testGetLastSegmentParentPath() {
	    JavaPath p = new JavaPath("orders[].contact.firstName");
	    assertEquals("orders[].contact", p.getLastSegmentParentPath().toString());
	}
	
	@Test
	public void testDeParentify() {
	    JavaPath p = new JavaPath("orders[].contact.firstName");
	    assertEquals("contact.firstName", p.deParentify().toString());
	    
	    p = new JavaPath("orders.contact[].firstName");
	    assertEquals("contact[].firstName", p.deParentify().toString());
	}
	
    @Test
    public void testDeCollectionfy() {
        JavaPath p = new JavaPath("orders[].contact.firstName");
        assertEquals("contact.firstName", p.deCollectionify("orders").toString());
        assertEquals("contact.firstName", p.deCollectionify("orders[]").toString());
        assertEquals("contact.firstName", p.deCollectionify("orders[3]").toString());

        p = new JavaPath("orders.contact[].firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact[]").toString());
        assertEquals("firstName", p.deCollectionify("contact[3]").toString());
        
        p = new JavaPath("orders<>.contact.firstName");
        assertEquals("contact.firstName", p.deCollectionify("orders").toString());
        assertEquals("contact.firstName", p.deCollectionify("orders<>").toString());
        assertEquals("contact.firstName", p.deCollectionify("orders<3>").toString());

        p = new JavaPath("orders.contact<>.firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact<>").toString());
        assertEquals("firstName", p.deCollectionify("contact<3>").toString());
    }
	
	@Test
	public void testCollectionIndexHandling() {
	    assertNull(JavaPath.indexOfSegment(null));
	    assertNull(JavaPath.indexOfSegment(""));
	    assertNull(JavaPath.indexOfSegment("foo"));
	    assertNull(JavaPath.indexOfSegment("foo[]"));
	    assertNull(JavaPath.indexOfSegment("foo<>"));
	    assertNull(JavaPath.indexOfSegment("foo{}"));
	        
	    assertEquals(new Integer(0), JavaPath.indexOfSegment("foo[0]"));
	    assertEquals(new Integer(1234), JavaPath.indexOfSegment("foo<1234>"));
	    assertNull(JavaPath.indexOfSegment("foo{bar}"));
	    
	    JavaPath p = new JavaPath("orders[4].contact.firstName");
	    p.setCollectionIndex("orders[4]", 5);
	    assertEquals("orders[5].contact.firstName", p.toString());
	    assertEquals(new Integer(5), JavaPath.indexOfSegment("orders[5]"));
	    
	    try { p.setCollectionIndex("orders<>", -3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("orders{}", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("orders", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("orders.contact.foo", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    
	    JavaPath q = new JavaPath("orders<4>.contact.firstName");
	    q.setCollectionIndex("orders<7>", 6);
	    assertEquals("orders<6>.contact.firstName", q.toString());
	    assertEquals(new Integer(5), JavaPath.indexOfSegment("orders<5>"));
	    
	    JavaPath r = new JavaPath("orders<>.contact.firstName");
	    assertEquals("orders<>", r.getCollectionSegment());
	    r.setCollectionIndex("orders<>", 6);
	    assertEquals("orders<6>.contact.firstName", r.toString());
	    assertEquals(new Integer(6), JavaPath.indexOfSegment(r.getCollectionSegment()));
	}
	
	@Test
	public void testIsIndexedCollection() {
	    JavaPath p = null;
	    
	    p = new JavaPath("order");
	    assertFalse(p.isIndexedCollection());
	        
	    p = new JavaPath("order.contact.firstName");
	    assertFalse(p.isIndexedCollection());

	    p = new JavaPath("order[]");
        assertFalse(p.isIndexedCollection());

	    p = new JavaPath("orders[4].contact.firstName");
	    assertTrue(p.isIndexedCollection());
	    
	    p = new JavaPath("orders[0].contact.firstName");
	    assertTrue(p.isIndexedCollection());

	    p = new JavaPath("orders[].contact.firstName");
	    assertFalse(p.isIndexedCollection());
	    
	    p = new JavaPath("orders<>");
        assertFalse(p.isIndexedCollection());
	    
        p = new JavaPath("orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new JavaPath("foo.orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new JavaPath("foo.orders<6>.bar");
        assertTrue(p.isIndexedCollection());
        
	    p = new JavaPath("orders<3>.contact.firstName");
	    assertTrue(p.isIndexedCollection());
	        
	    p = new JavaPath("orders<0>.contact.firstName");
	    assertTrue(p.isIndexedCollection());

	    p = new JavaPath("orders<>.contact.firstName");
	    assertFalse(p.isIndexedCollection());

	}
}
