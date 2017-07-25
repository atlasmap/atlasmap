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
package io.atlasmap.java.v2;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.atlasmap.v2.AtlasMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import static org.junit.Assert.assertNotNull;

public class JsonMarshallerTest extends BaseMarshallerTest {

	public ObjectMapper mapper = null;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		this.deleteTestFolders = false;
		
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Include.NON_NULL);		
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		
		mapper = null;
	}
	
	@Test
	public void testJsonMapJavaField() throws Exception {	
		AtlasMapping atlasMapping = generateAtlasMapping();
		//Object to JSON in file
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), atlasMapping);
		AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), AtlasMapping.class);
		assertNotNull(uMapping);
		//validateAtlasMapping(uMapping);
	}
	
//	@Test
//	public void testJsonSeparateJavaField() throws Exception {	
//		AtlasMapping atlasMapping = generateSeparateAtlasMapping();
//		//Object to JSON in file
//		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-separate.json"), atlasMapping);
//		AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-separate.json"), AtlasMapping.class);
//		assertNotNull(uMapping);
//		//validateSeparateAtlasMapping(uMapping);
//	}
	
	@Test
	public void testJsonMavenClasspathRequest() throws Exception {	
		MavenClasspathRequest request = generateMavenClasspathRequest();
		//Object to JSON in file
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-mavenclasspath-request.json"), request);
		MavenClasspathRequest uRequest = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-mavenclasspath-request.json"), MavenClasspathRequest.class);
		assertNotNull(uRequest);
	}

	@Test
	public void testJsonClassInspectionRequest() throws Exception {	
		ClassInspectionRequest request = generateClassInspectionRequest();
		//Object to JSON in file
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-classinspection-request.json"), request);
		ClassInspectionRequest uRequest = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-classinspection-request.json"), ClassInspectionRequest.class);
		assertNotNull(uRequest);
	}
	
	@Test
	public void testComplexRequests() throws Exception {	
		runJSONSerializationTest(generatePropertyReferenceMapping(), "atlasmapping-property-request.json");
		runJSONSerializationTest(generateConstantMapping(), "atlasmapping-constant-request.json");
		runJSONSerializationTest(generateMultiSourceMapping(), "atlasmapping-multisource-request.json");
		runJSONSerializationTest(generateCollectionMapping(), "atlasmapping-collection-request.json");
		runJSONSerializationTest(generateCombineMapping(), "atlasmapping-combine-request.json");
		runJSONSerializationTest(generateActionMapping(), "atlasmapping-field-action-request.json");
	}
	
	private void runJSONSerializationTest(Object object, String filename) throws Exception {
		//Object to JSON in file
		File f =  new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + filename);
		mapper.writerWithDefaultPrettyPrinter().writeValue(f, object);
		Object readObject = mapper.readValue(f, object.getClass());
		assertNotNull(readObject);		
	}
}
