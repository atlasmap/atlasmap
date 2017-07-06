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
package io.atlasmap.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.atlasmap.service.AtlasService;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.FieldMappings;
import io.atlasmap.v2.MappedField;
import io.atlasmap.v2.MappedFields;
import io.atlasmap.v2.MockField;
import io.atlasmap.v2.SeparateFieldMapping;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertTrue;



public class AtlasServiceTest {
	
	private AtlasService service = null;
	private ObjectMapper mapper = null;
	
	@Before
	public void setUp() throws Exception {
		service = new AtlasService();
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Include.NON_NULL);		
	}

	@After
	public void tearDown() throws Exception {
		service = null;
		mapper = null;
	}

	@Test
	public void testListMappings() throws Exception {
		Response resp = service.listMappings(generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mappings"), null);
		StringMap sMap = (StringMap)resp.getEntity();
		System.out.println("Found " + sMap.getStringMapEntry().size() + " objects");
		for(StringMapEntry s : sMap.getStringMapEntry()) {
			System.out.println("\t n: " + s.getName() + " v: " + s.getValue());
		}
	}
	
	@Test
	public void testGetMapping() throws Exception {
		Response resp = service.getMapping("junit3");
		AtlasMapping mapping = (AtlasMapping)resp.getEntity();		
	}

	@Test
	public void testFilenameMatch() throws Exception {
		String fileName = "atlasmapping-foo.xml";
		assertTrue(fileName.matches("atlasmapping-[a-zA-Z0-9]+.xml"));
	}
	
	@Test
	public void testAtlasMappingDeserialization() throws Exception {
		File f = new File("src/test/resources/atlasmapping-mockfield-PUT-create-sample.json");
		AtlasMapping mapping = mapper.readValue(f, AtlasMapping.class);
	}
	
	@Test
	public void testSeperateMappingSerialization() throws Exception {
		SeparateFieldMapping s = new SeparateFieldMapping();
		s.setOutputFields(new MappedFields());
		MappedField mf = new MappedField();
		mf.setField(new MockField());		
		s.setInputField(mf);
		mf = new MappedField();
		mf.setField(new MockField());		
		s.getOutputFields().getMappedField().add(mf);
		mf = new MappedField();
		mf.setField(new MockField());		
		s.getOutputFields().getMappedField().add(mf);
		AtlasMapping m = new AtlasMapping();
		m.setFieldMappings(new FieldMappings());
		m.getFieldMappings().getFieldMapping().add(s);
		System.out.println(mapper.writeValueAsString(m));
	}
	
	@Test
	public void testAtlasSeparateMappingDeserialization() throws Exception {
		File f = new File("src/test/resources/atasmapping-mockfield-PUT-create-seperate-sample.json");
		AtlasMapping mapping = mapper.readValue(f, AtlasMapping.class);
	}
	
	@Test
	public void testAtlasLookupMappingDeserialization() throws Exception {
		File f = new File("src/test/resources/atlasmapping-lookup-sample.json");
		AtlasMapping mapping = mapper.readValue(f, AtlasMapping.class);
	}
	
	@Test
	public void testSimpleFieldDeserialization() throws Exception {
		File f = new File("src/test/resources/atlasMapping-MockField-simple.json");		
		MockField field = mapper.readValue(f, MockField.class);
	}
	
	protected UriInfo generateTestUriInfo(String baseUri, String absoluteUri) throws Exception {
		return new TestUriInfo(new URI(baseUri), new URI(absoluteUri));
	}
	
}