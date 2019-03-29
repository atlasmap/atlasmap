package io.atlasmap.core;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;

public class AtlasMappingServiceTest {

    private static AtlasMappingService atlasMappingService;
    private static AtlasMapping atlasMapping;

    @BeforeClass
    public static void setUpBeforeClass() {
        atlasMappingService = new AtlasMappingService();

        atlasMapping = new AtlasMapping();
        atlasMapping.setName("testname");

        Mappings mappings = new Mappings();
        Mapping mapping = new Mapping();
        mapping.setId("1");
        mapping.setAlias("alias1");
        mappings.getMapping().add(mapping);
        mapping = new Mapping();
        mapping.setId("2");
        mapping.setAlias("alias2");
        mappings.getMapping().add(mapping);
        atlasMapping.setMappings(mappings);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        atlasMappingService = null;
        atlasMapping = null;
    }

    @Test
    public void testAtlasMappingService() {
        assertNotNull(atlasMappingService);
    }

    @Test
    public void testInitialize() {
        atlasMappingService = new AtlasMappingService();
        assertNotNull(atlasMappingService);
    }

    @Test
    public void testLoadMappingFileAtlasMappingFormat() throws AtlasValidationException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toFile();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(file);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingFileAtlasMappingFormatAtlasValidationException() throws AtlasValidationException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping2.json").toFile();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(file);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingReader() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json"));
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(reader);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingReaderAtlasMappingFormatAtlasValidationException() throws Exception {
        BufferedReader reader = null;

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(reader);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingString() throws AtlasValidationException {
        String filename = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json";

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(filename);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingInputStream() throws Exception {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toFile();
        FileInputStream fis = new FileInputStream(file);

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(fis);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingURI() throws Exception {
        URI uri = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toUri();

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(uri);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingURL() throws Exception {
        URL url = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toUri().toURL();

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(url);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingURLAtlasMappingFormat() throws Exception {
        URL url = new URL("http://www.redhat.com/q/h?s=^IXIC");
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(url);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testSetObjectMapper() {
        atlasMappingService.setObjectMapper(atlasMappingService.getObjectMapper());
    }

    @Test(expected = AtlasException.class)
    public void testSaveMappingAsFile() throws AtlasException {
        AtlasMappingService atlasMappingService = new AtlasMappingService();
        atlasMappingService.setObjectMapper(null);

        File file = Paths.get("target" + File.separator + "generated-test-sources" + File.separator + "atlasmapping.json").toFile();
        atlasMappingService.saveMappingAsFile(atlasMapping, file);
    }

}
