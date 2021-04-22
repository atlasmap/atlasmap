package io.atlasmap.service.my;

import io.atlasmap.builder.DefaultAtlasMappingBuilder;

public class MyCustomMappingBuilder extends DefaultAtlasMappingBuilder {

    @Override
    public void processMapping() {
        try {
            read("JsonSource", "name").write("XmlTarget", "name");
        } catch (Exception e) {
            addAudit(e);
        }
    }

}
