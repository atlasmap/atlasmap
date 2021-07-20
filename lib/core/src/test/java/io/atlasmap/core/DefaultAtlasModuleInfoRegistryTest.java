/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.atlasmap.spi.AtlasModule;

public class DefaultAtlasModuleInfoRegistryTest {
    private static DefaultAtlasModuleInfoRegistry atlasModuleInfoRegistry;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        DefaultAtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        factory.setObjectName();
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
