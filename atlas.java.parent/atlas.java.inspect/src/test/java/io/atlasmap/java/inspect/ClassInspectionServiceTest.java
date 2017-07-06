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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.java.inspect.ClassInspectionService;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ClassInspectionServiceTest {

	private ClassInspectionService classInspectionService = null;
	
	@Before
	public void setUp() {
		classInspectionService = new ClassInspectionService();
	}
	
	@After
	public void tearDown() {
		classInspectionService = null;
	}
	
	@Test
	public void testDetectArrayDimensions() {
		assertNull(classInspectionService.detectArrayDimensions(null));
		assertEquals(new Integer(0), classInspectionService.detectArrayDimensions(String.class));
		assertEquals(new Integer(1), classInspectionService.detectArrayDimensions(int[].class));
		assertEquals(new Integer(2), classInspectionService.detectArrayDimensions(String[][].class));
		assertEquals(new Integer(3), classInspectionService.detectArrayDimensions(List[][][].class));
		assertEquals(new Integer(4), classInspectionService.detectArrayDimensions(Map[][][][].class));
		assertEquals(new Integer(64), classInspectionService.detectArrayDimensions(int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class));
		// MAX_DIM_LIMIT NOTE: 255 is the JVM Spec limit
		assertEquals(new Integer(255), 
				classInspectionService.detectArrayDimensions(int[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][][]
														[][][][][][][][][][][][][][][].class));
	}
	
	@Test
	public void testDetectArrayClass() {
		assertNull(classInspectionService.detectArrayClass(null));
		assertEquals(String.class, classInspectionService.detectArrayClass(String.class));
		assertEquals(int.class, classInspectionService.detectArrayClass(int[].class));
		assertEquals(String.class, classInspectionService.detectArrayClass(String[][].class));
		assertEquals(List.class, classInspectionService.detectArrayClass(List[][][].class));
		assertEquals(Map.class, classInspectionService.detectArrayClass(Map[][][][].class));
	}
	
	@Test
	public void testClasspathToList() {
		// Null
		ClassInspectionService cis = new ClassInspectionService();
		assertNull(cis.classpathStringToList(null));
		
		// Zero
		assertNotNull(cis.classpathStringToList(""));
		assertEquals(new Integer(0), new Integer(cis.classpathStringToList("").size()));
		
		// One
		assertNotNull(cis.classpathStringToList("foo.jar"));
		assertEquals(new Integer(1), new Integer(cis.classpathStringToList("foo.jar").size()));
		assertEquals("foo.jar", cis.classpathStringToList("foo.jar").get(0));

		// Several
		assertNotNull(cis.classpathStringToList("foo.jar:bar.jar:blah.jar"));
		assertEquals(new Integer(3), new Integer(cis.classpathStringToList("foo.jar:bar.jar:blah.jar").size()));
		assertEquals("foo.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(0));
		assertEquals("bar.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(1));
		assertEquals("blah.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(2));

		
		// Several
		String tmpcp = File.separator + "foo.jar:" + File.separator + "bar.jar:" + File.separator + "blah.jar";
		assertNotNull(cis.classpathStringToList(tmpcp));
		assertEquals(new Integer(3), new Integer(cis.classpathStringToList(tmpcp).size()));
		assertEquals("/foo.jar", cis.classpathStringToList(tmpcp).get(0));
		assertEquals("/bar.jar", cis.classpathStringToList(tmpcp).get(1));
		assertEquals("/blah.jar", cis.classpathStringToList(tmpcp).get(2));
	}

}
