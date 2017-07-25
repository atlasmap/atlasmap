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

import org.junit.Test;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.exception.JclException;

import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.junit.Assert.*;

public class DynamicClassLoaderTest {

	@Test
	public void testListClassesInJarFile() throws Exception {
		List<String> classes = new ArrayList<String>();
		String folderName = "target/reference-jars";
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderName))) {				
			for (Path entry: stream) {
				if(!entry.toFile().isFile()) {
					continue;
				}
				try (JarInputStream jarFile = new JarInputStream(new FileInputStream(entry.toFile()))) {
					JarEntry jarEntry;
					while (true) {
						jarEntry = jarFile.getNextJarEntry();
						if (jarEntry == null) {
							break;
						}
						if (jarEntry.getName().endsWith(".class")) {
							String className = jarEntry.getName().replaceAll("/", "\\.");
							classes.add(className);
							System.out.println("ClassName: " + className);
						} else {
							System.out.println("Not a class: " + jarEntry.getName());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLoadInspectUnloadJar() throws Exception {
		
		Class<?> flatClazz = null;
		
		try {
			flatClazz = this.getClass().getClassLoader().loadClass("io.atlasmap.java.test.BaseFlatPrimitiveClass");
        	fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException e) {
			// Expected
		}
		
		JarClassLoader jc = new JarClassLoader( new String[] { "target/reference-jars" } );
        try {
        	flatClazz = jc.loadClass("io.atlasmap.java.test.BaseFlatPrimitiveClass");
        	assertNotNull(flatClazz);
        	assertEquals("io.atlasmap.java.test.BaseFlatPrimitiveClass", flatClazz.getName());
        	
        	// TODO: Fix support for inheritance
        	//Object newFlatClazz = flatClazz.newInstance();
        	//assertNotNull(newFlatClazz);
        } catch(ClassNotFoundException e) {
        	fail("Expected class to load");
        }
        
        jc.unloadClass("io.atlasmap.java.test.BaseFlatPrimitiveClass");
        jc = null;
        
		try {
			flatClazz = this.getClass().getClassLoader().loadClass("io.atlasmap.java.test.FlatPrimitiveClass");
        	fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException e) {
			// Expected
		}
	}
	
	@Test
	public void testLoadUnloadNeverLoadedClass() throws Exception {
		
		Class<?> flatClazz = null;
		
		try {
			flatClazz = this.getClass().getClassLoader().loadClass("io.atlasmap.java.test.BaseFlatPrimitiveClass");
        	fail("ClassNotFoundException expected");
		} catch (ClassNotFoundException e) {
			// Expected
		}
		
		JarClassLoader jc = new JarClassLoader( new String[] { "target/reference-jars" } );
		
		try { 
			jc.unloadClass("io.atlasmap.java.test.BaseFlatPrimitiveClass");
		} catch (JclException e) {
			assertEquals("Resource not found in local ClasspathResources", e.getCause().getMessage());
		}
	}

}
