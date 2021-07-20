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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;

/**
 * https://github.com/atlasmap/atlasmap/issues/1408 .
 */
public class AtlasMap1408Test {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMap1408Test.class);

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-source.xml");
        session.setSourceDocument("swagger-request", new String(Files.readAllBytes(Paths.get(url.toURI()))));
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object outputJson = session.getTargetDocument("request");
        assertNotNull(outputJson, "target json document was null");
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree(Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-target.json"));
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target json >>> {}", actual.toString());
        assertTrue(expected.equals(actual), actual.toString());
    }

}
