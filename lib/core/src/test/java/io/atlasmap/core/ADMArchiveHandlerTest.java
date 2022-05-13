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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentCatalog;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.InspectionType;
import io.atlasmap.v2.Json;

public class ADMArchiveHandlerTest {

    @Test
    public void testExportLoadEmpty() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        handler.export(new FileOutputStream("target/test.adm"));
        handler = new ADMArchiveHandler();
        handler.load(Paths.get("target/test.adm"));
        assertNull(handler.getMappingDefinition());
    }

    @Test
    public void testFileName() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        handler.setMappingDefinitionId("2");
        assertEquals("atlasmapping-UI.2.json", handler.getMappingDefinitionFileName());
    }

    @Test
    public void testExportLoadPersist() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        AtlasMapping mapping = new AtlasMapping();
        mapping.setName("foo");
        byte[] mappingBytes = Json.mapper().writeValueAsBytes(mapping);
        handler.setMappingDefinitionFromStream(new ByteArrayInputStream(mappingBytes));
        assertNotNull(handler.getMappingDefinition());
        assertEquals("foo", handler.getMappingDefinition().getName());
        DocumentCatalog docCatalog = new DocumentCatalog();
        byte[] catalogBytes = Json.mapper().writeValueAsBytes(docCatalog);
        handler.setDocumentCatalogFromStream(new ByteArrayInputStream(catalogBytes));
        assertNotNull(handler.getDocumentCatalog());
        handler.export(new FileOutputStream("target/test2.adm"));
        handler = new ADMArchiveHandler();
        handler.load(Paths.get("target/test2.adm"));
        assertNotNull(handler.getMappingDefinition());
        assertEquals("foo", handler.getMappingDefinition().getName());
        assertNotNull(handler.getDocumentCatalog());
        Path persistPath = Paths.get("target/test2");
        AtlasUtil.deleteDirectory(persistPath.toFile());
        persistPath.toFile().mkdirs();
        handler.setPersistDirectory(persistPath);
        handler.persist();
        assertTrue(persistPath.resolve(handler.getMappingDefinitionFileName()).toFile().exists());
        assertTrue(persistPath.resolve("document-catalog.json").toFile().exists());
    }

    @Test
    public void testPersistInvalidBytes() throws Exception {
        ADMArchiveHandler handler = new ADMArchiveHandler();
        byte[] mappingBytes = "no data".getBytes();
        assertThrows(AtlasException.class, () -> handler.setMappingDefinitionFromStream(new ByteArrayInputStream(mappingBytes)));
        Path persistPath = Paths.get("target/test3");
        AtlasUtil.deleteDirectory(persistPath.toFile());
        persistPath.toFile().mkdirs();
        handler.setPersistDirectory(persistPath);
        handler.persist();
        assertFalse(persistPath.resolve(handler.getMappingDefinitionFileName()).toFile().exists());
        assertThrows(AtlasException.class, () -> handler.setMappingDefinitionFromStream(new ByteArrayInputStream(mappingBytes)));
        assertNull(handler.getMappingDefinition());
    }

    @Test
    public void testImportOldADM() throws Exception {
        Path persistDir = Paths.get("target/testImportOldADM/persist");
        AtlasUtil.deleteDirectoryContents(persistDir.toFile());;
        Path libDir = Paths.get("target/testImportOldADM/lib");
        AtlasUtil.deleteDirectoryContents(libDir.toFile());
        ADMArchiveHandler handler = new ADMArchiveHandler();
        handler.setPersistDirectory(persistDir);
        handler.setLibraryDirectory(libDir);
        handler.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"));
        assertEquals(2, handler.getMappingDefinition().getMappings().getMapping().size());
        DocumentCatalog docCatalog = handler.getDocumentCatalog();
        assertEquals(1, docCatalog.getSources().size());
        DocumentMetadata sourceDoc = docCatalog.getSources().get(0);
        assertEquals("io.atlasmap.java.test.TargetTestClass", sourceDoc.getId());
        assertEquals("io.atlasmap.java.test.TargetTestClass", sourceDoc.getName());
        assertEquals(DocumentType.JAVA, sourceDoc.getDocumentType());
        assertEquals(InspectionType.JAVA_CLASS, sourceDoc.getInspectionType());
        assertEquals(DataSourceType.SOURCE, sourceDoc.getDataSourceType());
        assertEquals("io.atlasmap.java.test.TargetTestClass", sourceDoc.getInspectionParameters().get("className"));
        DocumentKey sourceDocKey = new DocumentKey(sourceDoc.getDataSourceType(), sourceDoc.getId());
        File sourceSpecFile = handler.getDocumentSpecificationFile(sourceDocKey);
        assertTrue(sourceSpecFile.exists());
        assertNull(handler.getDocumentInspectionResultFile(sourceDocKey));

        assertEquals(1, docCatalog.getTargets().size());
        DocumentMetadata targetDoc = docCatalog.getTargets().get(0);
        assertEquals("io.atlasmap.java.test.TargetTestClass", targetDoc.getId());
        assertEquals("io.atlasmap.java.test.TargetTestClass", targetDoc.getName());
        assertEquals(DocumentType.JAVA, targetDoc.getDocumentType());
        assertEquals(InspectionType.JAVA_CLASS, targetDoc.getInspectionType());
        assertEquals(DataSourceType.TARGET, targetDoc.getDataSourceType());
        assertEquals("io.atlasmap.java.test.TargetTestClass", targetDoc.getInspectionParameters().get("className"));
        DocumentKey targetDocKey = new DocumentKey(sourceDoc.getDataSourceType(), targetDoc.getId());
        File targetSpecFile = handler.getDocumentSpecificationFile(targetDocKey);
        assertTrue(targetSpecFile.exists());
        assertNull(handler.getDocumentInspectionResultFile(targetDocKey));
        handler.setDocumentInspectionResultFile(targetDocKey, new ByteArrayInputStream("dummy".getBytes()));
        File targetInspectedFile = handler.getDocumentInspectionResultFile(targetDocKey);
        assertTrue(targetInspectedFile.exists());

        handler.clear();
        assertNull(handler.getMappingDefinition());
        assertNull(handler.getDocumentCatalog());
        assertNull(handler.getDocumentSpecificationFile(sourceDocKey));
        assertNull(handler.getDocumentInspectionResultFile(sourceDocKey));
        assertNull(handler.getDocumentSpecificationFile(targetDocKey));
        assertNull(handler.getDocumentInspectionResultFile(targetDocKey));
    }
}
