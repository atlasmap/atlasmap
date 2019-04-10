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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

public class NestedCollectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(NestedCollectionTest.class);

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
    public void testSymmetricFirst() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-symmetric.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        mapping.getMappings().getMapping().removeIf(m -> !"1-1".equals(((Mapping)m).getId()));
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
        assertEquals(prettyPrinted, 2, firstArray.size());
        assertEquals(prettyPrinted, "firstArrayValue0", firstArray.get(0).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(0).get("secondArray"));
        assertEquals(prettyPrinted, "firstArrayValue1", firstArray.get(1).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(1).get("secondArray"));
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
        assertEquals(prettyPrinted, 8, firstArray.size());
        assertEquals(prettyPrinted, "thirdArrayValue0-0-0", firstArray.get(0).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(0).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-0-1", firstArray.get(1).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(1).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-1-0", firstArray.get(2).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(2).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue0-1-1", firstArray.get(3).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(3).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-0-0", firstArray.get(4).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(4).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-0-1", firstArray.get(5).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(5).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-1-0", firstArray.get(6).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(6).get("secondArray"));
        assertEquals(prettyPrinted, "thirdArrayValue1-1-1", firstArray.get(7).get("value").asText());
        assertNull(prettyPrinted, firstArray.get(7).get("secondArray"));
    }

    @Test
    public void testSymmetricFull() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-collection-symmetric.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String source = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("mappings/document-nested-collection.json").toURI())));
        session.setSourceDocument("JSONInstanceNestedCollection", source);
        context.process(session);
        assertTrue(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        assertEquals(2, session.getAudits().getAudit().size());
        assertTrue(TestHelper.printAudit(session), session.getAudits().getAudit().get(0).getMessage().contains("/firstArray<>/secondArray<>/value"));
        assertEquals(TestHelper.printAudit(session), AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
        assertTrue(TestHelper.printAudit(session), session.getAudits().getAudit().get(1).getMessage().contains("/firstArray<>/secondArray<>/thirdArray<>/value"));
        assertEquals(TestHelper.printAudit(session), AuditStatus.ERROR, session.getAudits().getAudit().get(1).getStatus());
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