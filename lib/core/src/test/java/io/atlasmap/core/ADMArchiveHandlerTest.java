/**
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Json;

public class ADMArchiveHandlerTest {

    @Test
    public void testExportLoadEmpty() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        handler.export(new FileOutputStream("target/test.adm"));
        handler = new ADMArchiveHandler();
        handler.load(Paths.get("target/test.adm"));
        assertNull(handler.getMappingDefinition());
        assertNull(handler.getMappingDefinitionBytes());
    }

    @Test
    public void testFileName() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        handler.setMappingDefinitionId("2");
        assertEquals("atlasmapping-UI.2.json", handler.getMappingDefinitionFileName());
        assertEquals("adm-catalog-files-2.gz", handler.getGzippedADMDigestFileName());
    }

    @Test
    public void testExportLoadPersist() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        AtlasMapping mapping = new AtlasMapping();
        mapping.setName("foo");
        byte[] mappingBytes = Json.mapper().writeValueAsBytes(mapping);
        handler.setMappingDefinitionBytes(new ByteArrayInputStream(mappingBytes));
        assertNotNull(handler.getMappingDefinition());
        assertEquals("foo", handler.getMappingDefinition().getName());
        byte[] digestBytes = "dummy".getBytes();
        handler.setGzippedADMDigest(new ByteArrayInputStream(digestBytes));
        assertNotNull(handler.getGzippedADMDigestBytes());
        handler.export(new FileOutputStream("target/test2.adm"));
        handler = new ADMArchiveHandler();
        handler.load(Paths.get("target/test2.adm"));
        assertNotNull(handler.getMappingDefinition());
        assertEquals("foo", handler.getMappingDefinition().getName());
        assertNotNull(handler.getGzippedADMDigestBytes());
        Path persistPath = Paths.get("target/test2");
        AtlasUtil.deleteDirectory(persistPath.toFile());
        persistPath.toFile().mkdirs();
        handler.setPersistDirectory(persistPath);
        handler.persist();
        assertTrue(persistPath.resolve(handler.getMappingDefinitionFileName()).toFile().exists());
        assertTrue(persistPath.resolve(handler.getGzippedADMDigestFileName()).toFile().exists());
    }

    @Test
    public void testPersistIgnoreInvalidBytes() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        byte[] mappingBytes = "no data".getBytes();
        handler.setMappingDefinitionBytes(new ByteArrayInputStream(mappingBytes));
        Path persistPath = Paths.get("target/test3");
        AtlasUtil.deleteDirectory(persistPath.toFile());
        persistPath.toFile().mkdirs();
        handler.setPersistDirectory(persistPath);
        handler.persist();
        assertFalse(persistPath.resolve(handler.getMappingDefinitionFileName()).toFile().exists());
        handler.setMappingDefinitionBytes(new ByteArrayInputStream(mappingBytes));
        assertNull(handler.getMappingDefinition());
    }
}