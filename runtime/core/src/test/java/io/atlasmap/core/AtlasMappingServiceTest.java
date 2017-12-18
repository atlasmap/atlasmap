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
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;

public class AtlasMappingServiceTest {

    private static AtlasMappingService atlasMappingService;
    private static AtlasMapping atlasMapping;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public static void tearDownAfterClass() throws Exception {
        atlasMappingService = null;
        atlasMapping = null;
    }

    @Test
    public void testAtlasMappingService() {
        assertNotNull(atlasMappingService);
    }

    @Test
    public void testAtlasMappingServiceListOfString() {
        List<String> modulePackages = new ArrayList<>();
        modulePackages.add("io.atlasmap.v2");

        atlasMappingService = new AtlasMappingService(modulePackages);

        assertNotNull(atlasMappingService);
    }

    @Test(expected = Exception.class)
    public void testAtlasMappingServiceListOfStringException() {
        List<String> modulePackages = new ArrayList<>();
        modulePackages.add("xio.atlasmap.v2");

        atlasMappingService = new AtlasMappingService(modulePackages);
    }

    @Test
    public void testInitialize() {
        List<String> modulePackages = new ArrayList<>();
        atlasMappingService = new AtlasMappingService(modulePackages);

        assertNotNull(atlasMappingService);
    }

    @Test(expected = IllegalStateException.class)
    public void testInitializeIllegalStateException() {
        List<String> modulePackages = new ArrayList<>();

        modulePackages.add("xio.atlasmap.v2");
        modulePackages.add("yio.atlasmap.v2");
        atlasMappingService = new AtlasMappingService(modulePackages);
    }

    @Test(expected = IllegalStateException.class)
    public void testInitializeIllegalStateExceptionNull() {
        List<String> modulePackages = null;
        new AtlasMappingService(modulePackages);
    }

    @Test
    public void testLoadMappingFile() throws AtlasValidationException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml").toFile();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(file);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingFileAtlasMappingFormat() throws AtlasValidationException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toFile();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(file, AtlasMappingFormat.JSON);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingFileAtlasMappingFormatAtlasValidationException() throws AtlasValidationException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping2.json").toFile();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(file, AtlasMappingFormat.JSON);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingReader() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml"));
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(reader);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingReaderAtlasMappingFormat() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json"));
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(reader, AtlasMappingFormat.JSON);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingReaderAtlasMappingFormatAtlasValidationException() throws Exception {
        BufferedReader reader = null;
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(reader, AtlasMappingFormat.JSON);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingStringAtlasMappingFormat() throws AtlasValidationException {
        String filename = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml";
        AtlasMappingFormat format = AtlasMappingFormat.XML;
        assertNotNull(format.value());
        assertNotNull(AtlasMappingFormat.valueOf("XML"));

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(filename, format);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingString() throws AtlasValidationException {
        String filename = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml";

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(filename);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingInputStream() throws Exception {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml").toFile();
        FileInputStream fis = new FileInputStream(file);

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(fis);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingURI() throws Exception {
        URI uri = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml").toUri();

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(uri);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testLoadMappingURL() throws Exception {
        URL url = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml").toUri().toURL();

        AtlasMapping atlasMapping = atlasMappingService.loadMapping(url);
        assertNotNull(atlasMapping);
    }

    @Test(expected = AtlasValidationException.class)
    public void testLoadMappingURLAtlasMappingFormat() throws Exception {
        URL url = new URL("http://www.redhat.com/q/h?s=^IXIC");
        AtlasMapping atlasMapping = atlasMappingService.loadMapping(url, AtlasMappingFormat.XML);
        assertNotNull(atlasMapping);
    }

    @Test
    public void testSaveMappingAsFileAtlasMappingFileAtlasMappingFormat() throws AtlasException {
        File file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml").toFile();
        atlasMappingService.saveMappingAsFile(atlasMapping, file);
        atlasMappingService.saveMappingAsFile(atlasMapping, file, AtlasMappingFormat.XML);

        file = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json").toFile();
        atlasMappingService.saveMappingAsFile(atlasMapping, file, AtlasMappingFormat.JSON);
    }

    @Test
    public void testSetObjectMapper() {
        atlasMappingService.setObjectMapper(atlasMappingService.getObjectMapper());
    }

    @Test(expected = AtlasException.class)
    public void testSaveMappingAsJsonFile() throws AtlasException {
        AtlasMappingService atlasMappingService = new AtlasMappingService();
        atlasMappingService.setObjectMapper(null);

        File file = Paths.get("target" + File.separator + "generated-test-sources" + File.separator + "atlasmapping.json").toFile();
        atlasMappingService.saveMappingAsJsonFile(atlasMapping, file);
    }

    @Test(expected = AtlasException.class)
    public void testSaveMappingAsXmlFile() throws Exception {
        AtlasMappingService atlasMappingService = new AtlasMappingService();
        atlasMappingService.setJAXBContext(JAXBContext.newInstance());

        File file = Paths.get("target" + File.separator + "generated-test-sources" + File.separator + "atlasmapping.xml").toFile();
        atlasMappingService.saveMappingAsXmlFile(atlasMapping, file);
    }
}
