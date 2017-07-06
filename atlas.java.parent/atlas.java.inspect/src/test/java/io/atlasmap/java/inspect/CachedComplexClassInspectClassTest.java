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

import io.atlasmap.java.test.CachedComplexClass;

import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.FieldStatus;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;

public class CachedComplexClassInspectClassTest {

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
	public void testCachedComplexClass() throws Exception {
		JavaClass c = classInspectionService.inspectClass(CachedComplexClass.class);
		assertNotNull(c);
		assertNull(c.getAnnotations());
		assertNull(c.getArrayDimensions());
		assertEquals("io.atlasmap.java.test.CachedComplexClass", c.getClassName());
		assertNull(c.getGetMethod());
		assertNotNull(c.getJavaEnumFields());
		assertNotNull(c.getJavaEnumFields().getJavaEnumField());
		assertTrue(c.getJavaEnumFields().getJavaEnumField().size() == 0);
		assertNotNull(c.getJavaFields());
		assertNotNull(c.getJavaFields().getJavaField());
		assertNull(c.getName());
		assertEquals("io.atlasmap.java.test", c.getPackageName());
		assertNull(c.getSetMethod());
		assertNull(c.getType());
		assertNotNull(c.getUri());
		assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.CachedComplexClass"), c.getUri());
		assertNull(c.getValue());
		
		assertEquals(new Integer(3), new Integer(c.getJavaFields().getJavaField().size()));
		
		Integer validated = 0;
		for(JavaField f : c.getJavaFields().getJavaField()) {
			if("io.atlasmap.java.test.TestOrder".equals(f.getClassName())) {
				//ClassValidationUtil.validateSimpleTestContact((JavaClass)f);
				validated++;
			}
			if("io.atlasmap.java.test.TestContact".equals(f.getClassName())) {
				if(!FieldStatus.CACHED.equals(f.getStatus())) {
					ClassValidationUtil.validateSimpleTestContact((JavaClass)f);
				}
				validated++;
			}
			if("io.atlasmap.java.test.TestAddress".equals(f.getClassName())) {
				if(!FieldStatus.CACHED.equals(f.getStatus())) {
					ClassValidationUtil.validateSimpleTestAddress((JavaClass)f);
				}
				validated++;
			}
			if("long".equals(f.getClassName())) {
				ClassValidationUtil.validateSerialVersionUID(f);
				validated++;
			}		
		}
		
		assertEquals(validated, new Integer(c.getJavaFields().getJavaField().size()));
	}

}
