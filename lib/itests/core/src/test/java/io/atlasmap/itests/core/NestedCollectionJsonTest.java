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
package io.atlasmap.itests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Mapping;

public class NestedCollectionJsonTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedCollectionJsonTest.class);

     private ObjectMapper mapper;

    @BeforeEach
    public void before() {
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    public void testAsymmetricSingleTarget() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mappings/atlasmapping-nested-collection-asymmetric.json");
        ADMArchiveHandler admHandler = new ADMArchiveHandler(Thread.currentThread().getContextClassLoader());
        admHandler.load(AtlasContextFactory.Format.JSON, in);
        AtlasMapping mapping = admHandler.getMappingDefinition();
        mapping.getMappings().getMapping().removeIf(m -> !"3-1".equals(((Mapping)m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertTrue(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("JSONInstanceNestedCollection");
        assertEquals(String.class, output.getClass());
        JsonNode outputJson = mapper.readTree((String)output);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(10, firstArray.size(), prettyPrinted);
        assertEquals("thirdArrayValue0-0-0", firstArray.get(0).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(0).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue0-0-1", firstArray.get(1).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(1).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue0-1-0", firstArray.get(2).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(2).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue0-1-1", firstArray.get(3).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(3).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue0-1-2", firstArray.get(4).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(4).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue1-0-0", firstArray.get(5).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(5).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue1-0-1", firstArray.get(6).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(6).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue1-0-2", firstArray.get(7).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(7).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue1-1-0", firstArray.get(8).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(8).get("secondArray"), prettyPrinted);
        assertEquals("thirdArrayValue1-1-1", firstArray.get(9).get("value").asText(), prettyPrinted);
        assertNull(firstArray.get(9).get("secondArray"), prettyPrinted);
    }

    @Test
    public void testSamePaths1stLevelCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        ArrayNode firstArray = assert1stLevelCollection(outputJson, prettyPrinted);
        assertNull(firstArray.get(0).get("secondArray"), prettyPrinted);
        assertNull(firstArray.get(1).get("secondArray"), prettyPrinted);
    }

    @Test
    public void testSamePaths1stAnd2ndLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths2ndLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("2-2"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths2ndAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("2-2", "3-3"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths1stAnd3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "3-3"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testSamePaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("3-3"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
    }

    @Test
    public void testRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("3-3renamed"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "3-3renamed"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAnd2nAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3renamed"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAnd2ndAnd3rdAndRenamedPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-1", "2-2", "3-3", "3-3renamed"), true);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        assert1stLevelCollection(outputJson, prettyPrinted);
        assert2ndLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted);
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, "Renamed");
    }

    @Test
    public void testSamePaths1stAndPaths3rdLevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("1-3"), false);
        String prettyPrinted = mapper.writeValueAsString(outputJson);
        ArrayNode thirdArray = (ArrayNode) outputJson.get("firstArray").get(0).get("secondArray").get(0).get("thirdArray");
        assertEquals(2, thirdArray.size(), prettyPrinted);
        assertEquals("firstArrayValue0", thirdArray.get(0).get("value").asText(), prettyPrinted);
        assertEquals("firstArrayValue1", thirdArray.get(1).get("value").asText(), prettyPrinted);
    }

    @Test
    public void test3To2LevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("3-2"), false);
        assertEquals(2, outputJson.get("firstArray").size());
        ArrayNode secondArray0 = (ArrayNode) outputJson.get("firstArray").get(0).get("secondArray");
        assertHasValues(secondArray0, "thirdArrayValue0-0-0", "thirdArrayValue0-0-1", "thirdArrayValue0-1-0",
            "thirdArrayValue0-1-1", "thirdArrayValue0-1-2");
        ArrayNode secondArray1 = (ArrayNode) outputJson.get("firstArray").get(1).get("secondArray");
        assertHasValues(secondArray1, "thirdArrayValue1-0-0", "thirdArrayValue1-0-1", "thirdArrayValue1-0-2",
            "thirdArrayValue1-1-0", "thirdArrayValue1-1-1");
    }

    @Test
    public void test4To2LevelNestedCollection() throws Exception {
        JsonNode outputJson = processJsonNestedCollection(Arrays.asList("4-2"), false);
        assertEquals(2, outputJson.get("firstArray").size());
        ArrayNode secondArray0 = (ArrayNode) outputJson.get("firstArray").get(0).get("secondArray");
        assertHasValues(secondArray0, "fourthArrayValue0-0-0-0", "fourthArrayValue0-0-0-1");
        ArrayNode secondArray1 = (ArrayNode) outputJson.get("firstArray").get(1).get("secondArray");
        assertHasValues(secondArray1, "fourthArrayValue1-1-0-0", "fourthArrayValue1-1-0-1");
    }

    private void assertHasValues(ArrayNode actual, String... expectedValues) {
        List<String> actualValues = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        actual.elements().forEachRemaining(n -> actualValues.add(n.get("value").asText()));
        for (String expected : expectedValues) {
            if (!actualValues.contains(expected)) {
                notFound.add(expected);
            }
        }
        if (!notFound.isEmpty()) {
            fail(notFound.toString() + " were not found");
        }
    }

    private JsonNode processJsonNestedCollection(List<String> mappingsToProcess, boolean assertNoWarnings) throws AtlasException, IOException, URISyntaxException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mappings/atlasmapping-nested-collection-json.json");
        ADMArchiveHandler admHandler = new ADMArchiveHandler(Thread.currentThread().getContextClassLoader());
        admHandler.load(AtlasContextFactory.Format.JSON, in);
        AtlasMapping mapping = admHandler.getMappingDefinition();
        mapping.getMappings().getMapping().removeIf(m -> !mappingsToProcess.contains(((Mapping) m).getId()));
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        if (assertNoWarnings) {
            assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        }
        Object output = session.getTargetDocument("JSONInstanceNestedCollection");
        return mapper.readTree((String) output);
    }

    private ArrayNode assert1stLevelCollection(JsonNode outputJson, String prettyPrinted) {
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(2, firstArray.size(), prettyPrinted);
        assertEquals("firstArrayValue0", firstArray.get(0).get("value").asText(), prettyPrinted);
        assertEquals("firstArrayValue1", firstArray.get(1).get("value").asText(), prettyPrinted);
        return firstArray;
    }

    private void assert2ndLevelNestedCollection(JsonNode outputJson, String prettyPrinted) {
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray");
        assertEquals(2, firstArray.size(), prettyPrinted);
        assertEquals(2, firstArray.get(0).get("secondArray").size(), prettyPrinted);
        assertEquals("secondArrayValue0-0", firstArray.get(0).get("secondArray").get(0).get("value").asText(), prettyPrinted);
        assertEquals("secondArrayValue0-1", firstArray.get(0).get("secondArray").get(1).get("value").asText(), prettyPrinted);
        assertEquals(2, firstArray.get(1).get("secondArray").size(), prettyPrinted);
        assertEquals("secondArrayValue1-0", firstArray.get(1).get("secondArray").get(0).get("value").asText(), prettyPrinted);
        assertEquals("secondArrayValue1-1", firstArray.get(1).get("secondArray").get(1).get("value").asText(), prettyPrinted);
    }

    private void assert3rdLevelNestedCollection(JsonNode outputJson, String prettyPrinted) {
        assert3rdLevelNestedCollection(outputJson, prettyPrinted, null);
    }

    private void assert3rdLevelNestedCollection(JsonNode outputJson, String prettyPrinted, String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        ArrayNode firstArray = (ArrayNode) outputJson.get("firstArray" + suffix);
        assertEquals(2, firstArray.size(), prettyPrinted);
        JsonNode secondArray0 = firstArray.get(0).get("secondArray" + suffix);
        assertEquals(2, secondArray0.size(), prettyPrinted);
        JsonNode thirdArray00 = secondArray0.get(0).get("thirdArray" + suffix);
        assertEquals(2, thirdArray00.size(), prettyPrinted);
        assertEquals("thirdArrayValue0-0-0", thirdArray00.get(0).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue0-0-1", thirdArray00.get(1).get("value").asText(), prettyPrinted);
        JsonNode thirdArray01 = secondArray0.get(1).get("thirdArray" + suffix);
        assertEquals(3, thirdArray01.size(), prettyPrinted);
        assertEquals("thirdArrayValue0-1-0", thirdArray01.get(0).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue0-1-1", thirdArray01.get(1).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue0-1-2", thirdArray01.get(2).get("value").asText(), prettyPrinted);
        JsonNode secondArray1 = firstArray.get(1).get("secondArray" + suffix);
        assertEquals(2, secondArray1.size(), prettyPrinted);
        JsonNode thirdArray10 = secondArray1.get(0).get("thirdArray" + suffix);
        assertEquals(3, thirdArray10.size(), prettyPrinted);
        assertEquals("thirdArrayValue1-0-0", thirdArray10.get(0).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue1-0-1", thirdArray10.get(1).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue1-0-2", thirdArray10.get(2).get("value").asText(), prettyPrinted);
        JsonNode thirdArray11 = secondArray1.get(1).get("thirdArray" + suffix);
        assertEquals(2, thirdArray11.size(), prettyPrinted);
        assertEquals("thirdArrayValue1-1-0", thirdArray11.get(0).get("value").asText(), prettyPrinted);
        assertEquals("thirdArrayValue1-1-1", thirdArray11.get(1).get("value").asText(), prettyPrinted);
    }

    @Test
    public void testAsymmetricFull() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-asymmetric.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertTrue(session.hasWarns(), TestHelper.printAudit(session));
        assertTrue(session.getAudits().getAudit().get(0).getMessage().contains("/firstArray<>/secondArray<>/value"), TestHelper.printAudit(session));
        assertEquals(AuditStatus.WARN, session.getAudits().getAudit().get(0).getStatus(), TestHelper.printAudit(session));
        assertTrue(session.getAudits().getAudit().get(1).getMessage().contains("/firstArray<>/secondArray<>/value"), TestHelper.printAudit(session));
        assertEquals(AuditStatus.WARN, session.getAudits().getAudit().get(1).getStatus(), TestHelper.printAudit(session));
        assertTrue(session.getAudits().getAudit().get(2).getMessage().contains("/firstArray<>/value"), TestHelper.printAudit(session));
        assertEquals(AuditStatus.WARN, session.getAudits().getAudit().get(2).getStatus(), TestHelper.printAudit(session));
    }
}
