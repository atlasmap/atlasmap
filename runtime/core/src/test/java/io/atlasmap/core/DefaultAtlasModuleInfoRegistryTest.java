package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import io.atlasmap.spi.AtlasModule;

public class DefaultAtlasModuleInfoRegistryTest {
    private static DefaultAtlasModuleInfoRegistry atlasModuleInfoRegistry;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        DefaultAtlasContextFactory factory = new DefaultAtlasContextFactory();
        factory.setObjectName("DefaultAtlasContextFactory");
        atlasModuleInfoRegistry = new DefaultAtlasModuleInfoRegistry(factory);
    }

    @Test
    public void testDefaultAtlasModuleInfoRegistry() {
        assertNotNull(atlasModuleInfoRegistry);
    }

    @Test
    public void testLookupByUri() {
        assertNull(atlasModuleInfoRegistry.lookupByUri(null));

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");
        DefaultAtlasModuleInfo atlasModuleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null, formats, packageNames);

        atlasModuleInfoRegistry.register(atlasModuleInfo);

        assertNotNull(atlasModuleInfoRegistry.lookupByUri("atlas:java"));

        assertNull(atlasModuleInfoRegistry.lookupByUri("java"));
    }

    @Test
    public void testGetAll() {
        assertNotNull(atlasModuleInfoRegistry.getAll());
    }

    @Test
    public void testRegister() {
        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");
        DefaultAtlasModuleInfo atlasModuleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null, formats, packageNames);

        atlasModuleInfoRegistry.register(atlasModuleInfo);
        assertEquals(1, atlasModuleInfoRegistry.size());
    }

    @Test
    public void testSize() {
        assertEquals(0, atlasModuleInfoRegistry.size());
    }

    @Test
    public void testUnregisterAll() {
        atlasModuleInfoRegistry.unregisterAll();
        assertEquals(0, atlasModuleInfoRegistry.size());
    }

}
