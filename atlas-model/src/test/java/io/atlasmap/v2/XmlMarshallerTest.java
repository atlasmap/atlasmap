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
package io.atlasmap.v2;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.v2.WellKnownNamespace;

import io.atlasmap.v2.AtlasMapping;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

import static org.junit.Assert.assertNotNull;

public class XmlMarshallerTest extends BaseMarshallerTest {

	private JAXBContext jaxbContext = null;
	private Marshaller marshaller = null;
	private Unmarshaller unmarshaller = null;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		super.deleteTestFolders = false;
		
		jaxbContext = JAXBContext.newInstance("io.atlasmap.v2");
		
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "atlas-model-v2.xsd");
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String[] getPreDeclaredNamespaceUris() { return new String[] { XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI }; }

            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI))
                    return "xsi";
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
                    return "xs";
                if (namespaceUri.equals(WellKnownNamespace.XML_MIME_URI))
                    return "xmime";
                return suggestion;

            }
        });
		unmarshaller = jaxbContext.createUnmarshaller();
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		
		marshaller = null;
		unmarshaller = null;
		jaxbContext = null;
	}
	
	@Test
	public void testReferenceMapping() throws Exception {
		marshaller.marshal(generateReferenceAtlasMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
		StreamSource fileSource = new StreamSource(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
		JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
		assertNotNull(mappingElem);
		assertNotNull(mappingElem.getValue());
		validateReferenceAtlasMapping(mappingElem.getValue());
	}
	
//	@Test
//	public void testXmlLookupTable() throws Exception {
//		AtlasMapping atlasMapping = generateAtlasMapping();
//		atlasMapping.getLookupTables().getLookupTable().add(generateLookupTable());
//		marshaller.marshal(atlasMapping, new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//		StreamSource fileSource = new StreamSource(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//		JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
//		assertNotNull(mappingElem);
//		
//		AtlasMapping tmpMapping = mappingElem.getValue();
//		assertNotNull(tmpMapping);
//		validateAtlasMapping(tmpMapping);
//		assertNotNull(tmpMapping.getLookupTables());
//		assertNotNull(tmpMapping.getLookupTables().getLookupTable());
//		assertEquals(new Integer(1), new Integer(tmpMapping.getLookupTables().getLookupTable().size()));
//		assertNotNull(tmpMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList());
//		assertNotNull(tmpMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList().getLookupEntry());
//		assertEquals(new Integer(2), new Integer(tmpMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList().getLookupEntry().size()));
//	}
//
//	@Test
//	public void testFieldActions() throws Exception {
//	    AtlasMapping atlasMapping = generateAtlasMapping();
//	    Mapping fm = atlasMapping.getMappings().getMapping().get(0);
//	    ((Map)fm).getOutputField().setActions(new Actions());
//	    ((Map)fm).getOutputField().getActions().setUppercase(new Uppercase());
//	    
//        marshaller.marshal(atlasMapping, new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//        StreamSource fileSource = new StreamSource(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//        JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
//	}
//	
//    @Test
//    public void testMulitSourceMapping() throws Exception {
//        AtlasMapping atlasMapping = generateMultiSourceMapping();
//        Mapping fm = atlasMapping.getMappings().getMapping().get(0);
//        ((Map)fm).getOutputField().setActions(new Actions());
//        ((Map)fm).getOutputField().getActions().setUppercase(new Uppercase());
//        
//        marshaller.marshal(atlasMapping, new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//        StreamSource fileSource = new StreamSource(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
//        JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
//    }
}
