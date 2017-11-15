package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.v2.AtlasMapping;

@Ignore
public class AtlasMapXmlToJson {

    @Test
    public void testConvertXmlToJson() throws Exception {
        String sourceXmlPath = "src/test/resources/atlasmapping.xml";
        String destJsonPath = "src/test/resources/atlasmapping.json";
        AtlasMappingService atlasMappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
        AtlasMapping mappingFromXml = atlasMappingService.loadMapping(sourceXmlPath, AtlasMappingFormat.XML);
        atlasMappingService.saveMappingAsFile(mappingFromXml, new File(destJsonPath), AtlasMappingFormat.JSON);
        AtlasMapping mappingFromJson = atlasMappingService.loadMapping(destJsonPath, AtlasMappingFormat.JSON);
        assertEquals(mappingFromXml.getName(), mappingFromJson.getName());
        assertEquals(mappingFromXml.getDataSource().size(), mappingFromJson.getDataSource().size());
        assertEquals(mappingFromXml.getMappings().getMapping().size(), mappingFromJson.getMappings().getMapping().size());
    }

}
