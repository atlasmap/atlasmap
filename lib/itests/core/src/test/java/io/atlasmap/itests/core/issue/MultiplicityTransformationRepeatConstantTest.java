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
package io.atlasmap.itests.core.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;

public class MultiplicityTransformationRepeatConstantTest {

    @Test
    public void testRepeatConstant() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmapping-multiplicity-transformation-action-repeat-constant.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
                Thread.currentThread().getContextClassLoader().getResource("data/issue/json-source-repeat-constant.json").toURI())));
        session.setSourceDocument("JSONSchemaSource-9c57df5a-9511-411a-a8a9-ce232fc8f3f6", sourceJson);
        
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("JSONSchemaSource-3ddde8cd-f588-4fd4-a996-75c048c648ea");
        JsonNode root = new ObjectMapper().readTree((String)output);
        ArrayNode addrs = (ArrayNode) root.get("addressList");
        assertEquals(5, addrs.size());
        for (int i=0; i<addrs.size(); i++) {
            JsonNode addr = addrs.get(i);
            assertEquals("constant-test", addr.get("city").textValue());
            if (i<3) {
                assertEquals("constant-test", addr.get("state").textValue());
            } else {
                assertNull(addr.get("state"));
            }
        }
    }

}
