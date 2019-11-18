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
package io.atlasmap.itests.core;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Mapping;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
public class NestedCollectionXmlTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedCollectionXmlTest.class);

     private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void testAsymmetricSingleTarget() throws Exception {

    }

    @Test
    public void testSamePaths1stLevelCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value></firstArray>" +
            "<firstArray><value>firstArrayValue1</value></firstArray></root>", output);
    }

    @Test
    public void testSamePaths1stAnd2ndLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "2-2"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value><secondArray><value>secondArrayValue0-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value></secondArray></firstArray>" +
            "<firstArray><value>firstArrayValue1</value><secondArray><value>secondArrayValue1-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value></secondArray></firstArray></root>", output);
    }

    @Test
    public void testSamePaths2ndLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("2-2"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><firstArray>" +
            "<secondArray><value>secondArrayValue0-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value></secondArray></firstArray>" +
            "<firstArray><secondArray><value>secondArrayValue1-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value></secondArray></firstArray></root>", output);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "2-2", "3-3"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value><secondArray><value>secondArrayValue0-0</value>" +
            "<thirdArray><value>thirdArrayValue0-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-0-1</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value><thirdArray><value>thirdArrayValue0-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-2</value></thirdArray></secondArray></firstArray>" +
            "<firstArray><value>firstArrayValue1</value><secondArray><value>secondArrayValue1-0</value>" +
            "<thirdArray><value>thirdArrayValue1-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-2</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value><thirdArray><value>thirdArrayValue1-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-1-1</value></thirdArray></secondArray></firstArray></root>", output);
    }

    @Test
    public void testSamePaths2ndAnd3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("2-2", "3-3"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><secondArray><value>secondArrayValue0-0</value>" +
            "<thirdArray><value>thirdArrayValue0-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-0-1</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value><thirdArray><value>thirdArrayValue0-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-2</value></thirdArray></secondArray></firstArray>" +
            "<firstArray><secondArray><value>secondArrayValue1-0</value>" +
            "<thirdArray><value>thirdArrayValue1-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-2</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value><thirdArray><value>thirdArrayValue1-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-1-1</value></thirdArray></secondArray></firstArray></root>", output);
    }

    @Test
    public void testSamePaths1stAnd3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "3-3"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value><secondArray>" +
            "<thirdArray><value>thirdArrayValue0-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-0-1</value></thirdArray></secondArray>" +
            "<secondArray><thirdArray><value>thirdArrayValue0-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-2</value></thirdArray></secondArray></firstArray>" +
            "<firstArray><value>firstArrayValue1</value><secondArray>" +
            "<thirdArray><value>thirdArrayValue1-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-2</value></thirdArray></secondArray>" +
            "<secondArray><thirdArray><value>thirdArrayValue1-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-1-1</value></thirdArray></secondArray></firstArray></root>", output);
    }

    @Test
    public void testSamePaths3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("3-3"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><secondArray><thirdArray><value>thirdArrayValue0-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-0-1</value></thirdArray></secondArray><secondArray>" +
            "<thirdArray><value>thirdArrayValue0-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-2</value></thirdArray></secondArray></firstArray>" +
            "<firstArray><secondArray><thirdArray><value>thirdArrayValue1-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-2</value></thirdArray></secondArray>" +
            "<secondArray><thirdArray><value>thirdArrayValue1-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-1-1</value></thirdArray></secondArray></firstArray></root>", output);
    }

    @Test
    public void testRenamedPaths3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("3-3renamed"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><firstArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-0-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed><firstArrayRenamed><secondArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-1-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed></root>", output);
    }

    @Test
    public void testSamePaths1stAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "3-3renamed"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value></firstArray>" +
            "<firstArray><value>firstArrayValue1</value></firstArray><firstArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-0-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-2</value></thirdArrayRenamed></secondArrayRenamed></firstArrayRenamed><firstArrayRenamed><secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-0-0</value></thirdArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-0-1</value></thirdArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-0-2</value></thirdArrayRenamed></secondArrayRenamed><secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-1-0</value></thirdArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-1-1</value></thirdArrayRenamed></secondArrayRenamed></firstArrayRenamed></root>", output);
    }

    @Test
    public void testSamePaths1stAnd2nAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "2-2", "3-3renamed"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value><secondArray><value>secondArrayValue0-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value></secondArray></firstArray>" +
            "<firstArray><value>firstArrayValue1</value><secondArray><value>secondArrayValue1-0</value></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value></secondArray></firstArray><firstArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-0-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed><firstArrayRenamed><secondArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-1-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed></root>", output);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        String output = processXmlNestedCollection(Arrays.asList("1-1", "2-2", "3-3", "3-3renamed"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
            "<firstArray><value>firstArrayValue0</value><secondArray><value>secondArrayValue0-0</value>" +
            "<thirdArray><value>thirdArrayValue0-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-0-1</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue0-1</value><thirdArray><value>thirdArrayValue0-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue0-1-2</value></thirdArray></secondArray></firstArray>" +
            "<firstArray><value>firstArrayValue1</value><secondArray><value>secondArrayValue1-0</value>" +
            "<thirdArray><value>thirdArrayValue1-0-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-1</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-0-2</value></thirdArray></secondArray>" +
            "<secondArray><value>secondArrayValue1-1</value><thirdArray><value>thirdArrayValue1-1-0</value></thirdArray>" +
            "<thirdArray><value>thirdArrayValue1-1-1</value></thirdArray></secondArray></firstArray>" +
            "<firstArrayRenamed><secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-0-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue0-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue0-1-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed><firstArrayRenamed><secondArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-1</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-0-2</value></thirdArrayRenamed></secondArrayRenamed>" +
            "<secondArrayRenamed><thirdArrayRenamed><value>thirdArrayValue1-1-0</value></thirdArrayRenamed>" +
            "<thirdArrayRenamed><value>thirdArrayValue1-1-1</value></thirdArrayRenamed></secondArrayRenamed>" +
            "</firstArrayRenamed></root>", output);
    }

    private String processXmlNestedCollection(List<String> mappingsToProcess) throws AtlasException, IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-symmetric-xml.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        mapping.getMappings().getMapping().removeIf(m -> !mappingsToProcess.contains(((Mapping) m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.xml").toURI())));
        session.setSourceDocument("XMLInstanceNestedCollection", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("XMLInstanceNestedCollection");
        return (String) output;
    }
}
