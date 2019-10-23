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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.api.AtlasException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Mapping;
import org.w3c.dom.Element;

public class NestedCollectionJsonTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedCollectionJsonTest.class);

     private AtlasMappingService mappingService;
     private ObjectMapper mapper;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    public void testAsymmetricSingleTarget() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-asymmetric.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        mapping.getMappings().getMapping().removeIf(m -> !"3-1".equals(((Mapping)m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("JSONInstanceNestedCollection");
        assertEquals(String.class, output.getClass());
        JsonNode outputJson = mapper.readTree((String)output);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(prettyPrinted, 10, firstArray.size());
        assertEquals(prettyPrinted, "thirdArrayValue0-0-0", firstArray.get(0).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(0).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-0-1", firstArray.get(1).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(1).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-1-0", firstArray.get(2).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(2).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-1-1", firstArray.get(3).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(3).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-1-2", firstArray.get(4).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(4).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-0-0", firstArray.get(5).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(5).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-0-1", firstArray.get(6).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(6).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-0-2", firstArray.get(7).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(7).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-1-0", firstArray.get(8).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(8).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-1-1", firstArray.get(9).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(9).get("secondArray"));
    }

    @Test
    public void testSamePaths1stLevelCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        ArrayNode firstArray = assert1stLevelCollection(outputJson, prettyPrinted);
        assertNull(prettyPrinted, firstArray.get(0).get("secondArray"));
        assertNull(prettyPrinted, firstArray.get(1).get("secondArray"));
    }

    @Test
    public void testSamePaths1stAnd2ndLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths2ndLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("2-2"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths2ndAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("2-2", "3-3"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths1stAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "3-3"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("3-3"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("3-3renamed"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "3-3renamed"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAnd2nAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3renamed"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3", "3-3renamed"));
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    private JsonNode processJsonNestedCollection(List<String> mappingsToProcess) throws AtlasException, IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-symmetric.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        mapping.getMappings().getMapping().removeIf(m -> !mappingsToProcess.contains(((Mapping) m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("JSONInstanceNestedCollection");
        return mapper.readTree((String) output);
    }

    private ArrayNode assert1stLevelCollection(JsonNode outputJson, String prettyPrinted) {
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(prettyPrinted, 2, firstArray.size());
        assertEquals(prettyPrinted, "firstArrayValue0", firstArray.get(0).get("value").asText());
        assertEquals(prettyPrinted, "firstArrayValue1", firstArray.get(1).get("value").asText());
        return firstArray;
    }

    private void assert2ndLevelNestedCollection(JsonNode outputJson, String prettyPrinted) {
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(prettyPrinted, 2, firstArray.size());
        assertEquals(prettyPrinted, 2, firstArray.get(0).get("secondArray").size());
        assertEquals(prettyPrinted, "secondArrayValue0-0", firstArray.get(0).get("secondArray").get(0).get("value").asText());
        assertEquals(prettyPrinted, "secondArrayValue0-1", firstArray.get(0).get("secondArray").get(1).get("value").asText());
        assertEquals(prettyPrinted, 2, firstArray.get(1).get("secondArray").size());
        assertEquals(prettyPrinted, "secondArrayValue1-0", firstArray.get(1).get("secondArray").get(0).get("value").asText());
        assertEquals(prettyPrinted, "secondArrayValue1-1", firstArray.get(1).get("secondArray").get(1).get("value").asText());
    }

    private void assert3rdLevelNestedCollection(JsonNode outputJson, String prettyPrinted) {
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, null);
    }

    private void assert3rdLevelNestedCollection(JsonNode outputJson, String prettyPrinted, String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray" + suffix);
        assertEquals(prettyPrinted, 2, firstArray.size());
        JsonNode secondArray0 = firstArray.get(0).get("secondArray" + suffix);
        assertEquals(prettyPrinted, 2, secondArray0.size());
        JsonNode thirdArray00 = secondArray0.get(0).get("thirdArray" + suffix);
        assertEquals(prettyPrinted, 2, thirdArray00.size());
        assertEquals(prettyPrinted, "thirdArrayValue0-0-0", thirdArray00.get(0).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue0-0-1", thirdArray00.get(1).get("value").asText());
        JsonNode thirdArray01 = secondArray0.get(1).get("thirdArray" + suffix);
        assertEquals(prettyPrinted, 3, thirdArray01.size());
        assertEquals(prettyPrinted, "thirdArrayValue0-1-0", thirdArray01.get(0).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue0-1-1", thirdArray01.get(1).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue0-1-2", thirdArray01.get(2).get("value").asText());
        JsonNode secondArray1 = firstArray.get(1).get("secondArray" + suffix);
        assertEquals(prettyPrinted, 2, secondArray1.size());
        JsonNode thirdArray10 = secondArray1.get(0).get("thirdArray" + suffix);
        assertEquals(prettyPrinted, 3, thirdArray10.size());
        assertEquals(prettyPrinted, "thirdArrayValue1-0-0", thirdArray10.get(0).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue1-0-1", thirdArray10.get(1).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue1-0-2", thirdArray10.get(2).get("value").asText());
        JsonNode thirdArray11 = secondArray1.get(1).get("thirdArray" + suffix);
        assertEquals(prettyPrinted, 2, thirdArray11.size());
        assertEquals(prettyPrinted, "thirdArrayValue1-1-0", thirdArray11.get(0).get("value").asText());
        assertEquals(prettyPrinted, "thirdArrayValue1-1-1", thirdArray11.get(1).get("value").asText());
    }

    @Test
    public void testAsymmetricFull() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-asymmetric.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertTrue(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        assertTrue(TestHelper.printAudit(session), session.getAudits().getAudit().get(0).getMessage().contains("/firstArray<>/secondArray<>/value"));
        assertEquals(TestHelper.printAudit(session), AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
        assertTrue(TestHelper.printAudit(session), session.getAudits().getAudit().get(1).getMessage().contains("/firstArray<>/secondArray<>/thirdArray<>/value"));
        assertEquals(TestHelper.printAudit(session), AuditStatus.ERROR, session.getAudits().getAudit().get(1).getStatus());
    }
}
