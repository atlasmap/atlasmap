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
package io.atlasmap.xml.v2;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;

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
	public void testJsonMapXMLField() throws Exception {	
		AtlasMapping atlasMapping = generateAtlasMapping();
		
		XmlDataSource ds = new XmlDataSource();
		ds.setUri("someuri");
		ds.setId("someid");
		ds.setDataSourceType(DataSourceType.SOURCE);
		ds.setXmlNamespaces(new XmlNamespaces());
		XmlNamespace ns = new XmlNamespace();
		ns.setAlias("tns");
		ns.setLocationUri("location");
		ns.setTargetNamespace(false);
		ns.setUri("uri");
		ds.getXmlNamespaces().getXmlNamespace().add(ns);
		atlasMapping.getDataSource().add(ds);
		
		DataSource ds2 = new DataSource();
		ds2.setUri("someuri2");
		ds2.setId("someid2");
		ds2.setDataSourceType(DataSourceType.TARGET);
		atlasMapping.getDataSource().add(ds2);
		
		//Object to JSON in file
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), atlasMapping);
		AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), AtlasMapping.class);
		assertNotNull(uMapping);
		validateAtlasMapping(uMapping);
	}
	
	@Test
	public void testJsonXmlInspectionRequest() throws Exception {	
		XmlInspectionRequest request = generateInspectionRequest();
		//Object to JSON in file
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-xmlinspection-request.json"), request);
		XmlInspectionRequest uRequest = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping-xmlinspection-request.json"), XmlInspectionRequest.class);
		assertNotNull(uRequest);
	}
}
