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

import org.junit.Test;

import io.atlasmap.core.AtlasUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AtlasUtilTest {

	@Test
	public void testIsEmpty() {
		assertTrue(AtlasUtil.isEmpty(null));
		assertTrue(AtlasUtil.isEmpty(""));
		assertTrue(AtlasUtil.isEmpty("     "));
		assertTrue(AtlasUtil.isEmpty("\n\n"));
		assertTrue(AtlasUtil.isEmpty("\t\t"));
		assertTrue(AtlasUtil.isEmpty("\r\n"));
		assertTrue(AtlasUtil.isEmpty("\f\t\n\r"));
		// Note: We can live with 'backspace' not being 'empty'
		assertFalse(AtlasUtil.isEmpty("\b"));
	}
	
	@Test
	public void testAtlasUri() {
		String URI_JAVA_NOVER = "atlas:java";
		String URI_JAVA_NOVER_W_PARM = "atlas:java?foo=bar";
		String URI_JAVA_NOVER_W_PARMS = "atlas:java?foo=bar&bar=blah";

		String URI_JAVA_VER1 = "atlas:java::1";
		String URI_JAVA_VER2 = "atlas:java::2";
		String URI_JAVA_VER2_W_PARM = "atlas:java::2?foo=bar";
		String URI_JAVA_VER2_W_PARMS = "atlas:java::2?foo=bar&bar=blah";

		List<String> JAVA_URIS = Arrays.asList(URI_JAVA_NOVER, URI_JAVA_NOVER_W_PARM, URI_JAVA_NOVER_W_PARMS, URI_JAVA_VER1,URI_JAVA_VER2, URI_JAVA_VER2_W_PARM, URI_JAVA_VER2_W_PARMS);
		List<String> NOVER_URIS = Arrays.asList(URI_JAVA_NOVER, URI_JAVA_NOVER_W_PARM, URI_JAVA_NOVER_W_PARMS); 
		List<String> JAVA_VER1_URIS = Arrays.asList(URI_JAVA_VER1);
		List<String> JAVA_VER2_URIS = Arrays.asList(URI_JAVA_VER2, URI_JAVA_VER2_W_PARM, URI_JAVA_VER2_W_PARMS);
		List<String> PARM_URIS = Arrays.asList(URI_JAVA_NOVER_W_PARM, URI_JAVA_VER2_W_PARM);
		List<String> PARMS_URIS = Arrays.asList(URI_JAVA_NOVER_W_PARMS, URI_JAVA_VER2_W_PARMS);
		
		for(String uri: JAVA_URIS) {
			assertEquals("atlas", AtlasUtil.getUriScheme(uri));
		}
		
		for(String uri: JAVA_URIS) {
			assertEquals("java", AtlasUtil.getUriModule(uri));
		}
		
		for(String uri: JAVA_URIS) {
			assertNull(AtlasUtil.getUriDataType(uri));
		}
		
		for(String uri: NOVER_URIS) {
			assertNull(AtlasUtil.getUriModuleVersion(uri));
		}
		
		for(String uri: JAVA_VER1_URIS) {
			assertEquals("1", AtlasUtil.getUriModuleVersion(uri));
		}
		
		for(String uri: JAVA_VER2_URIS) {
			assertEquals("2", AtlasUtil.getUriModuleVersion(uri));
		}
		
		for(String uri: PARM_URIS) {
			Map<String, String> params = AtlasUtil.getUriParameters(uri);
			assertNotNull(params);
			assertEquals(new Integer(1), new Integer(params.size()));
			assertEquals("bar", params.get("foo"));
			assertNull(params.get("bar"));
		}
		
		for(String uri: PARMS_URIS) {
			Map<String, String> params = AtlasUtil.getUriParameters(uri);
			assertNotNull(params);
			assertEquals(new Integer(2), new Integer(params.size()));
			assertEquals("bar", params.get("foo"));
			assertEquals("blah", params.get("bar"));
			assertNull(params.get("blah"));
		}
	}
	
	@Test
	public void testFindClassesForPackage() {
	    List<Class<?>> classes = AtlasUtil.findClassesForPackage("io.atlasmap.v2");
	    assertNotNull(classes);
	    int found = 0;
	    for(Class<?> clazz : classes) {
	        //System.out.println(clazz);
	        if("io.atlasmap.v2.Field".equals(clazz.getName())) { found++; }
	        if("io.atlasmap.v2.AtlasMapping".equals(clazz.getName())) { found++; }
	        if("io.atlasmap.v2.Action".equals(clazz.getName())) { found++; }
	        if("io.atlasmap.v2.Capitalize".equals(clazz.getName())) { found++; }
	    }
	    
	    assertEquals(new Integer(4), new Integer(found));
	}

}
