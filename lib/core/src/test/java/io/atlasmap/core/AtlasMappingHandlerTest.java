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

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;

public class AtlasMappingHandlerTest {

    @Test
    public void testRemoveDocumentReference() throws Exception {
        ADMArchiveHandler admHandler = new ADMArchiveHandler();
        admHandler.setLibraryDirectory(Paths.get("target/testRemoveDocumentReference/lib"));
        admHandler.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("atlasmap-mapping.adm"));
        AtlasMappingHandler mappingHandler = admHandler.getAtlasMappingHandler();
        assertEquals(2, mappingHandler.getAtlasMapping().getDataSource().size());
        assertEquals(2, mappingHandler.getAtlasMapping().getMappings().getMapping().size());
        mappingHandler.removeDocumentReference(new DocumentKey(DataSourceType.SOURCE, "io.atlasmap.java.test.TargetTestClass"));
        assertEquals(1, mappingHandler.getAtlasMapping().getDataSource().size());
        assertEquals(0, mappingHandler.getAtlasMapping().getMappings().getMapping().size());
    }

}
