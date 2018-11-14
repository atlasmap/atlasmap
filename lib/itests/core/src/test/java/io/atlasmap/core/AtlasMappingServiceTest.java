package io.atlasmap.core;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;

public class AtlasMappingServiceTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void testMappingXML() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping.xml");
        assertAtlasMapping(mappingService.loadMapping(url));
        assertAtlasMapping(mappingService.loadMapping(url.toURI()));
        assertAtlasMapping(mappingService.loadMapping(new File(url.toURI())));
        assertAtlasMapping(mappingService.loadMapping(url.openStream()));
        assertAtlasMapping(mappingService.loadMapping(new InputStreamReader(url.openStream())));
        File outputXml = new File("target/output-atlasmapping.xml");
        mappingService.saveMappingAsFile(mappingService.loadMapping(url), outputXml, AtlasMappingFormat.XML);
        assertAtlasMapping(mappingService.loadMapping(outputXml));
    }

    @Test
    public void testMappingXMLViaContext() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping.xml");
        assertAtlasMapping(
                DefaultAtlasContextFactory.getInstance().createContext(url.toURI()).createSession().getMapping());
        assertAtlasMapping(DefaultAtlasContextFactory.getInstance().createContext(new File(url.toURI())).createSession()
                .getMapping());
        assertAtlasMapping(DefaultAtlasContextFactory.getInstance().createContext(url.toURI(), AtlasMappingFormat.XML)
                .createSession().getMapping());
        assertAtlasMapping(DefaultAtlasContextFactory.getInstance()
                .createContext(new File(url.toURI()), AtlasMappingFormat.XML).createSession().getMapping());
    }

    @Test
    public void testMappingJSON() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping.json");
        assertAtlasMapping(mappingService.loadMapping(url, AtlasMappingFormat.JSON));
        assertAtlasMapping(mappingService.loadMapping(url.toURI(), AtlasMappingFormat.JSON));
        assertAtlasMapping(mappingService.loadMapping(new File(url.toURI()), AtlasMappingFormat.JSON));
        assertAtlasMapping(mappingService.loadMapping(url.openStream(), AtlasMappingFormat.JSON));
        assertAtlasMapping(
                mappingService.loadMapping(new InputStreamReader(url.openStream()), AtlasMappingFormat.JSON));
        File outputJson = new File("target/output-atlasmapping.json");
        mappingService.saveMappingAsFile(mappingService.loadMapping(url, AtlasMappingFormat.JSON), outputJson,
                AtlasMappingFormat.JSON);
        assertAtlasMapping(mappingService.loadMapping(outputJson, AtlasMappingFormat.JSON));
    }

    @Test
    public void testMappingJSONViaContext() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping.json");
        assertAtlasMapping(DefaultAtlasContextFactory.getInstance().createContext(url.toURI(), AtlasMappingFormat.JSON)
                .createSession().getMapping());
        assertAtlasMapping(DefaultAtlasContextFactory.getInstance()
                .createContext(new File(url.toURI()), AtlasMappingFormat.JSON).createSession().getMapping());
    }

    private void assertAtlasMapping(AtlasMapping mapping) {
        Assert.assertNotNull(mapping);
        Assert.assertEquals("core-unit-test", mapping.getName());
        Assert.assertNotNull(mapping.getMappings());
        Assert.assertNotNull(mapping.getMappings().getMapping());
        Assert.assertNotNull(mapping.getMappings().getMapping().get(0));
        BaseMapping m = mapping.getMappings().getMapping().get(0);
        Assert.assertEquals(MappingType.MAP, m.getMappingType());
        Assert.assertEquals(Mapping.class, m.getClass());
        Assert.assertNotNull(((Mapping) m).getInputField());
        Field input = ((Mapping) m).getInputField().get(0);
        Assert.assertEquals("/orderId", input.getPath());
        Assert.assertNotNull(((Mapping) m).getOutputField());
        Field output = ((Mapping) m).getOutputField().get(0);
        Assert.assertEquals("/orderId", output.getPath());
    }
}
