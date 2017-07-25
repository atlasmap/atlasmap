package io.atlasmap.reference.javaToXml;

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

public class JavaToXMLJSONMarshallingTest {
public ObjectMapper mapper = null;
	
	@Before
	public void setUp() throws Exception {		
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Include.NON_NULL);		
	}
	
	@After
	public void tearDown() throws Exception {		
		mapper = null;
	}
	
	@Test
	public void testCombineMappingDemarshaller() throws Exception {
		//this test is for AT-466: issue saving mappings in combine mode (parser complaining about strategy property)
		//the json has been changed from what the UI was sending, now the "actions" property on the output field is "null" rather than "[]"
		String filename = "src/test/resources/javaToXml/javaToXmlMapping-combine.json";
		AtlasMapping uMapping = mapper.readValue(new File(filename), AtlasMapping.class);
		assertNotNull(uMapping);
	}

}
