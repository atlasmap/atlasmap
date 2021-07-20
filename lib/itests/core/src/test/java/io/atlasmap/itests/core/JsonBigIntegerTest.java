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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class JsonBigIntegerTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-json-biginteger.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
                Thread.currentThread().getContextClassLoader().getResource("data/json-source-biginteger.json").toURI())));
        session.setSourceDocument("json-source", sourceJson);

        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        String output = (String) session.getTargetDocument("json-target");
        JsonNode root = new ObjectMapper().readTree(output);
        JsonNode field = root.get("field");
        assertEquals(true, field.isBigInteger());
        assertEquals("1234567890123456789012345678901234567890", field.asText());
        JsonNode auto = root.get("auto");
        assertEquals(true, auto.isBigInteger());
        assertEquals("12345678901234567890123456789012345678901", auto.asText());
    }

}
