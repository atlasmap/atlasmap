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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.atlasmap.spi.AtlasModule;

public class DefaultAtlasModuleInfoTest {

    private static DefaultAtlasModuleInfo module = null;

    @BeforeAll
    public static void setUpBeforeClass() {

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        module = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null, formats, packageNames);
    }

    @AfterAll
    public static void tearDownAfterClass() {
        module = null;
    }

    @Test
    public void testGetModuleClass() {
        assertNotNull(module.getModuleClass());
    }

    @Test
    public void testGetConstructor() {
        assertNull(module.getConstructor());
    }

    @Test
    public void testGetFormats() {
        assertNotNull(module.getFormats());
    }

    @Test
    public void testGetDataFormats() {
        assertNotNull(module.getDataFormats());

        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", AtlasModule.class, null,
                null, packageNames);
        assertNotNull(moduleInfo.getDataFormats());
    }

    @Test
    public void testGetModuleClassName() {
        assertNotNull(module.getModuleClassName());

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();
        packageNames.add("io.atlasmap.core");

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats,
                packageNames);
        assertNull(moduleInfo.getModuleClassName());
    }

    @Test
    public void testGetPackageNames() {
        assertNotNull(module.getPackageNames());

        List<String> formats = new ArrayList<>();
        formats.add("java");
        List<String> packageNames = new ArrayList<>();

        DefaultAtlasModuleInfo moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats,
                packageNames);
        assertNotNull(moduleInfo.getPackageNames());

        moduleInfo = new DefaultAtlasModuleInfo("name", "atlas:java", null, null, formats, null);
        assertNotNull(moduleInfo.getPackageNames());
    }

    @Test
    public void testGetName() {
        assertNotNull(module.getName());
    }

    @Test
    public void testGetUri() {
        assertNotNull(module.getUri());
    }

    @Test
    public void testIsSourceSupported() {
        assertNull(module.isSourceSupported());
    }

    @Test
    public void testIsTargetSupported() {
        assertNull(module.isTargetSupported());
    }

    @Test
    public void testGetClassName() {
        assertNotNull(module.getClassName());
    }

    @Test
    public void testGetVersion() {
        assertNull(module.getVersion());
    }

    @Test
    public void testToString() {
        assertNotNull(module.toString());
    }

}
