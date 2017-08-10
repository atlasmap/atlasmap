package org.apache.camel.component.atlasmap;

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
        AtlasMappingService atlasMappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
        AtlasMapping atlasMapping = atlasMappingService.loadMapping("src/test/resources/atlasmapping.xml", AtlasMappingFormat.XML);
        atlasMappingService.saveMappingAsFile(atlasMapping, new File("src/test/resources/atlasmapping.json"), AtlasMappingFormat.JSON);
    }
    
}
