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
package io.atlasmap.itests.reference.json_to_json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class JsonJsonCollectionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionComplex() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-collection-complex.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        JsonNode root = new ObjectMapper().readTree(string);
        JsonNode contactList = root.get("contactList");
        assertTrue(contactList.isArray());
        assertEquals(3, ((ArrayNode)contactList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactList).get(i);
            assertEquals("first" + (i+1), contact.get("firstName").asText(), contact.toString());
            assertNull(contact.get("lastName"));
        }
        JsonNode contactSAList = root.get("contactSAList");
        assertTrue(contactSAList.isArray());
        assertEquals(3, ((ArrayNode)contactSAList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactSAList).get(i);
            assertEquals("FIRSTSA" + (i+1), contact.get("firstName").asText(), contact.toString());
            assertNull(contact.get("lastName"));
        }
        JsonNode contactTAList = root.get("contactTAList");
        assertTrue(contactTAList.isArray());
        assertEquals(3, ((ArrayNode)contactTAList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactTAList).get(i);
            assertEquals("FIRSTTA" + (i+1), contact.get("firstName").asText(), contact.toString());
            assertNull(contact.get("lastName"));
        }
    }

    @Test
    public void testProcessCollectionComplexEmpty() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-collection-complex-empty.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        JsonNode root = new ObjectMapper().readTree(string);
        JsonNode contactList = root.get("contactList");
        assertTrue(contactList.isArray());
        assertEquals(0, ((ArrayNode)contactList).size());
        JsonNode contactSAList = root.get("contactSAList");
        assertTrue(contactSAList.isArray());
        assertEquals(0, ((ArrayNode)contactSAList).size());
        JsonNode contactTAList = root.get("contactTAList");
        assertTrue(contactTAList.isArray());
        assertEquals(0, ((ArrayNode)contactTAList).size());
    }

    @Test
    public void testProcessCollectionComplexEmptyItem() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-collection-complex.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-collection-complex-empty-item.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        JsonNode root = new ObjectMapper().readTree(string);
        JsonNode contactList = root.get("contactList");
        assertTrue(contactList.isArray());
        assertEquals(3, ((ArrayNode)contactList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactList).get(i);
            if (i==1) {
                assertNull(contact.get("firstName"));
            } else {
                assertEquals("first" + (i+1), contact.get("firstName").asText());
            }
            assertNull(contact.get("lastName"));
        }
        JsonNode contactSAList = root.get("contactSAList");
        assertTrue(contactSAList.isArray());
        assertEquals(3, ((ArrayNode)contactSAList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactSAList).get(i);
            if (i==1) {
                assertNull(contact.get("firstName"));
            } else {
                assertEquals("FIRSTSA" + (i+1), contact.get("firstName").asText());
            }
            assertNull(contact.get("lastName"));
        }
        JsonNode contactTAList = root.get("contactTAList");
        assertTrue(contactTAList.isArray());
        assertEquals(3, ((ArrayNode)contactTAList).size());
        for (int i=0; i<3; i++) {
            JsonNode contact = ((ArrayNode)contactTAList).get(i);
            if (i==1) {
                assertNull(contact.get("firstName"));
            } else {
                assertEquals("FIRSTTA" + (i+1), contact.get("firstName").asText());
            }
            assertNull(contact.get("lastName"));
        }
    }
}
