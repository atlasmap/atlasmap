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
    public void testProcessCollectionListEmpty() throws Exception {
        AtlasContext context = DefaultAtlasContextFactory.getInstance()
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-collection-list-empty.json").toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-collection-list-empty.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        String string = (String) session.getDefaultTargetDocument();
        JsonNode root = new ObjectMapper().readTree(string);
        JsonNode contactList = root.get("contactList");
        assertTrue(contactList.isArray());
        assertEquals(0, ((ArrayNode)contactList).size());
    }

}
