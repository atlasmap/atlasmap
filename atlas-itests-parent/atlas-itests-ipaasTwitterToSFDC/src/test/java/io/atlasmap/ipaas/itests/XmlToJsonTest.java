package io.atlasmap.ipaas.itests;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.atlasmap.v2.AtlasMapping;

public class XmlToJsonTest {

    public ObjectMapper mapper = null;
    private JAXBContext jaxbContext = null;
    private Marshaller marshaller = null;
    private Unmarshaller unmarshaller = null;

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
    public void testXmlToJson() throws Exception {
        AtlasMapping atlasMapping = loadAtlasMapping(
                "src/test/resources/atlasmapping-twitterStatusToSalesforceContact.xml");
        // Object to JSON in file
        mapper.writeValue(new File("target" + File.separator + "atlasmapping.json"), atlasMapping);
        AtlasMapping uMapping = mapper.readValue(new File("target/atlasmapping.json"), AtlasMapping.class);
        assertNotNull(uMapping);
    }

    protected AtlasMapping loadAtlasMapping(String filename) throws Exception {
        jaxbContext = JAXBContext.newInstance("io.atlasmap.v2:io.atlasmap.java.v2");
        unmarshaller = jaxbContext.createUnmarshaller();
        return ((JAXBElement<AtlasMapping>) unmarshaller.unmarshal(new File(filename))).getValue();
    }

}
