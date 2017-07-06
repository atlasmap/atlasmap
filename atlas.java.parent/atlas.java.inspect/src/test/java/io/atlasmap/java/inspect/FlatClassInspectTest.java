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

import io.atlasmap.java.test.FlatPrimitiveClass;
import io.atlasmap.java.test.FlatPrimitiveInterface;

import io.atlasmap.java.inspect.ClassInspectionService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlatClassInspectTest {

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
	public void testFlatPrimitiveClass() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveClass(classInspectionService, FlatPrimitiveClass.class);
	}

	@Test
	public void testFlatPrimitiveClassArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveClassArray(classInspectionService, FlatPrimitiveClass[].class);
	}
	
	@Test
	public void testFlatPrimitiveClassTwoDimArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveClassTwoDimArray(classInspectionService, FlatPrimitiveClass[][].class);
	}
	
	@Test
	public void testFlatPrimitiveClassThreeDimArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveClassThreeDimArray(classInspectionService, FlatPrimitiveClass[][][].class);
	}
	
	@Test
	public void testFlatPrimitiveInterface() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveInterface(classInspectionService, FlatPrimitiveInterface.class);
	}
	
	@Test
	public void testFlatPrimitiveInterfaceArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveInterfaceArray(classInspectionService, FlatPrimitiveInterface[].class);
	}
	
	@Test
	public void testFlatPrimitiveInterfaceTwoDimArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveInterfaceTwoDimArray(classInspectionService, FlatPrimitiveInterface[][].class);
	}
	
	@Test
	public void testFlatPrimitiveInterfaceThreeDimArray() throws Exception {
		ClassValidationUtil.validateFlatPrimitiveInterfaceThreeDimArray(classInspectionService, FlatPrimitiveInterface[][][].class);
	}

}
