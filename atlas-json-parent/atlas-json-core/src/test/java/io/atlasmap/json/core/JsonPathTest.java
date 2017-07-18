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
package io.atlasmap.json.core;

import static org.junit.Assert.*;
import org.junit.Test;
import io.atlasmap.json.core.JsonPath;

public class JsonPathTest {	
	
	@Test
	public void testOneClass() {
		JsonPath foo = new JsonPath();
		foo.appendField("user");
		assertEquals("user", foo.toString());
	}

	
	@Test
	public void testFields() {
		JsonPath foo = new JsonPath();
		foo.appendField("user").appendField("name");
		assertEquals("/user/name", foo.toString());
		foo.appendField("bar");
		assertEquals("/user/name/bar", foo.toString());
	}
	
	@Test
	public void testCleanPathSegment() {
	    assertNull(JsonPath.cleanPathSegment(null));
	    assertEquals("", JsonPath.cleanPathSegment(""));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo[]"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo<>"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo{}"));
	    
	    assertEquals("foo", JsonPath.cleanPathSegment("foo"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo[0]"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo<1234>"));
	    assertEquals("foo", JsonPath.cleanPathSegment("foo{bar}"));
	    
	    // We do not try to outsmart busted paths
        assertEquals("foo[0", JsonPath.cleanPathSegment("foo[0"));
        assertEquals("foo1234>", JsonPath.cleanPathSegment("foo1234>"));
        assertEquals("foo}", JsonPath.cleanPathSegment("foo}"));
	}
	
	@Test
	public void testGetLastSegmentParent() {
	    JsonPath p = new JsonPath("/orders/contact/firstName");
	    assertEquals("contact", p.getLastSegmentParent());
	}
	
	@Test
	public void testGetLastSegmentParentPath() {
	    JsonPath p = new JsonPath("/orders[]/contact/firstName");
	    assertEquals("/orders[]/contact", p.getLastSegmentParentPath().toString());
	}
	
	@Test
	public void testDeParentify() {
	    JsonPath p = new JsonPath("/orders[]/contact/firstName");
	    assertEquals("/contact/firstName", p.deParentify().toString());
	    
	    p = new JsonPath("/orders/contact[]/firstName");
	    assertEquals("/contact[]/firstName", p.deParentify().toString());
	}
	
    @Test
    public void testDeCollectionfy() {
        JsonPath p = new JsonPath("/orders[]/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[]").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders[3]").toString());

        p = new JsonPath("/orders/contact[]/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact[]").toString());
        assertEquals("firstName", p.deCollectionify("contact[3]").toString());
        
        p = new JsonPath("/orders<>/contact/firstName");
        assertEquals("/contact/firstName", p.deCollectionify("orders").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<>").toString());
        assertEquals("/contact/firstName", p.deCollectionify("orders<3>").toString());

        p = new JsonPath("/orders/contact<>/firstName");
        assertEquals("firstName", p.deCollectionify("contact").toString());
        assertEquals("firstName", p.deCollectionify("contact<>").toString());
        assertEquals("firstName", p.deCollectionify("contact<3>").toString());
    }
	
	@Test
	public void testCollectionIndexHandling() {
	    assertNull(JsonPath.indexOfSegment(null));
	    assertNull(JsonPath.indexOfSegment(""));
	    assertNull(JsonPath.indexOfSegment("foo"));
	    assertNull(JsonPath.indexOfSegment("foo[]"));
	    assertNull(JsonPath.indexOfSegment("foo<>"));
	    assertNull(JsonPath.indexOfSegment("foo{}"));
	        
	    assertEquals(new Integer(0), JsonPath.indexOfSegment("foo[0]"));
	    assertEquals(new Integer(1234), JsonPath.indexOfSegment("foo<1234>"));
	    assertNull(JsonPath.indexOfSegment("foo{bar}"));
	    
	    JsonPath p = new JsonPath("/orders[4]/contact/firstName");
	    p.setCollectionIndex("orders[4]", 5);
	    assertEquals("/orders[5]/contact/firstName", p.toString());
	    assertEquals(new Integer(5), JsonPath.indexOfSegment("orders[5]"));
	    
	    try { p.setCollectionIndex("orders<>", -3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("orders{}", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("orders", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    try { p.setCollectionIndex("/orders/contact/foo", 3); fail("Exception expected"); } catch (IllegalArgumentException e) { }
	    
	    JsonPath q = new JsonPath("/orders<4>/contact/firstName");
	    q.setCollectionIndex("orders<7>", 6);
	    assertEquals("/orders<6>/contact/firstName", q.toString());
	    assertEquals(new Integer(5), JsonPath.indexOfSegment("orders<5>"));
	    
	    JsonPath r = new JsonPath("/orders<>/contact/firstName");
	    assertEquals("orders<>", r.getCollectionSegment());
	    r.setCollectionIndex("orders<>", 6);
	    assertEquals("/orders<6>/contact/firstName", r.toString());
	    assertEquals(new Integer(6), JsonPath.indexOfSegment(r.getCollectionSegment()));
	}
	
	@Test
	public void testIsIndexedCollection() {
	    JsonPath p = null;
	    
	    p = new JsonPath("order");
	    assertFalse(p.isIndexedCollection());
	        
	    p = new JsonPath("/order/contact/firstName");
	    assertFalse(p.isIndexedCollection());

	    p = new JsonPath("order[]");
        assertFalse(p.isIndexedCollection());

	    p = new JsonPath("/orders[4]/contact/firstName");
	    assertTrue(p.isIndexedCollection());
	    
	    p = new JsonPath("/orders[0]/contact/firstName");
	    assertTrue(p.isIndexedCollection());

	    p = new JsonPath("/orders[]/contact/firstName");
	    assertFalse(p.isIndexedCollection());
	    
	    p = new JsonPath("orders<>");
        assertFalse(p.isIndexedCollection());
	    
        p = new JsonPath("orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new JsonPath("/foo/orders<6>");
        assertTrue(p.isIndexedCollection());
        
        p = new JsonPath("/foo/orders<6>/bar");
        assertTrue(p.isIndexedCollection());
        
	    p = new JsonPath("/orders<3>/contact/firstName");
	    assertTrue(p.isIndexedCollection());
	        
	    p = new JsonPath("/orders<0>/contact/firstName");
	    assertTrue(p.isIndexedCollection());

	    p = new JsonPath("/orders<>/contact/firstName");
	    assertFalse(p.isIndexedCollection());

	}
}
