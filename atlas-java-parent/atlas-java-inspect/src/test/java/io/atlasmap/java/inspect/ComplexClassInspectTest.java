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

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.FieldStatus;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;

public class ComplexClassInspectTest {

	private ClassInspectionService classInspectionService = null;
	
	@Before
	public void setUp() {
		classInspectionService = new ClassInspectionService();
		classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
	}
	
	@After
	public void tearDown() {
		classInspectionService = null;
	}
	
	@Test
	public void testComplexClassSourceOrder() throws Exception {
		JavaClass c = classInspectionService.inspectClass(SourceOrder.class);
		assertNotNull(c);
	    assertEquals("io.atlasmap.java.test.SourceOrder", c.getClassName());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.SourceOrder"), c.getUri());
        validateComplexClass(c);
	}
	
	@Test
	public void testComplexClassTargetOrder() throws Exception {
	    JavaClass c = classInspectionService.inspectClass(TargetOrder.class);
	    assertNotNull(c);
	    assertEquals("io.atlasmap.java.test.TargetOrder", c.getClassName());
	    assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.TargetOrder"), c.getUri());
	    validateComplexClass(c);
	}
	
    public void validateComplexClass(JavaClass c) {
		assertNull(c.getAnnotations());
		assertNull(c.getArrayDimensions());
		assertNull(c.getGetMethod());
		assertNotNull(c.getJavaEnumFields());
		assertNotNull(c.getJavaEnumFields().getJavaEnumField());
		assertTrue(c.getJavaEnumFields().getJavaEnumField().size() == 0);
		assertNotNull(c.getJavaFields());
		assertNotNull(c.getJavaFields().getJavaField());
		assertNull(c.getName());
		assertEquals("io.atlasmap.java.test", c.getPackageName());
		assertNull(c.getSetMethod());
		assertNull(c.getFieldType());
		assertNotNull(c.getUri());
		assertNull(c.getValue());
		
		// Two serialVersionUID fields due to inheritance
		assertEquals(new Integer(5), new Integer(c.getJavaFields().getJavaField().size()));
		
		Integer validated = 0;
		for(JavaField f : c.getJavaFields().getJavaField()) {
			if("io.atlasmap.java.test.BaseContact".equals(f.getClassName())) {
				if(!FieldStatus.CACHED.equals(f.getStatus())) {
					ClassValidationUtil.validateSimpleTestContact((JavaClass)f);
				}
				validated++;
			}
			if("io.atlasmap.java.test.BaseAddress".equals(f.getClassName())) {
				if(!FieldStatus.CACHED.equals(f.getStatus())) {
					ClassValidationUtil.validateSimpleTestAddress((JavaClass)f);
				}
				validated++;
			}
			if("long".equals(f.getClassName())) {
				ClassValidationUtil.validateSerialVersionUID(f);
				validated++;
			}
			
			if("java.lang.Integer".equals(f.getClassName())) {
			    ClassValidationUtil.validateOrderId(f);
			    validated++;
			}
		}
		
		assertEquals(validated, new Integer(c.getJavaFields().getJavaField().size()));
	}

}
