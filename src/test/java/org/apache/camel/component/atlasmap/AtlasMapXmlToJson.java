package org.apache.camel.component.atlasmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import com.mediadriver.atlas.core.v2.AtlasMappingService;
import com.mediadriver.atlas.v2.AtlasMapping;

@Ignore
public class AtlasMapXmlToJson {
    
    @Test
    public void testConvertXmlToJson() throws Exception {
        List<String> pkgs = new ArrayList<String>();
        pkgs.add("com.mediadriver.atlas.java.v2");
        AtlasMappingService atlasMappingService = new AtlasMappingService(pkgs);
        AtlasMapping atlasMapping = atlasMappingService.loadMapping("src/test/resources/atlasmapping.xml");
        atlasMappingService.saveMappingAsFileJson(atlasMapping, new File("src/test/resources/atlasmapping.json"));
    }
    
}
