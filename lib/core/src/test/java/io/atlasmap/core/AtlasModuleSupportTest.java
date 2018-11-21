package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.MockField;

public class AtlasModuleSupportTest {

    @Test
    public void testListTargetPathsAtlasMapping() {
        AtlasMapping atlasMapping = null;
        assertEquals(0, AtlasModuleSupport.listTargetPaths(atlasMapping).size());

        atlasMapping = new AtlasMapping();
        assertEquals(0, AtlasModuleSupport.listTargetPaths(atlasMapping).size());

        Mappings mappings = new Mappings();
        atlasMapping.setMappings(mappings);
        assertEquals(0, AtlasModuleSupport.listTargetPaths(atlasMapping).size());

        Mapping mapping = new Mapping();
        mappings.getMapping().add(mapping);
        assertEquals(0, AtlasModuleSupport.listTargetPaths(atlasMapping).size());

        class MockMapping extends Mappings {
            private static final long serialVersionUID = 1L;

            @Override
            public List<BaseMapping> getMapping() {
                return null;
            }
        }

        Mappings mockMapping = new MockMapping();
        atlasMapping.setMappings(mockMapping);
        assertEquals(0, AtlasModuleSupport.listTargetPaths(atlasMapping).size());
    }

    @Test
    public void testListTargetPathsListOfBaseMapping() {
        List<BaseMapping> mappings = null;
        assertEquals(0, AtlasModuleSupport.listTargetPaths(mappings).size());

        mappings = new ArrayList<>();
        assertEquals(0, AtlasModuleSupport.listTargetPaths(mappings).size());

        Mapping mapping = new Mapping();
        Field field = new MockField();
        field.setPath("MockPath");
        mapping.getOutputField().add(field);
        mappings.add(mapping);
        assertEquals(1, AtlasModuleSupport.listTargetPaths(mappings).size());

        Collection collection = null;
        mappings.add(collection);
        assertEquals(1, AtlasModuleSupport.listTargetPaths(mappings).size());

        collection = new Collection();
        mappings.add(collection);
        assertEquals(1, AtlasModuleSupport.listTargetPaths(mappings).size());

        Mappings mapings = new Mappings();
        collection.setMappings(mapings);
        assertEquals(1, AtlasModuleSupport.listTargetPaths(mappings).size());
    }

    @Test
    public void testAtlasModuleSupportContructor() {
        assertNotNull(new AtlasModuleSupport());
    }

}
