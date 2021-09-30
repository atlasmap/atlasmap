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
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * https://issues.redhat.com/browse/ENTESB-17399 .
 */
public class ENTESB17399Test {

    private static final Logger LOG = LoggerFactory.getLogger(ENTESB16270Test.class);

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/entesb-17399-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        URL in = Thread.currentThread().getContextClassLoader().getResource("data/issue/entesb-17399-source1.json");
        String source = new String(Files.readAllBytes(Paths.get(in.toURI())));
        session.setSourceDocument("-MkqPBsPav60JEAxwZ2K", source);
        in = Thread.currentThread().getContextClassLoader().getResource("data/issue/entesb-17399-source2.json");
        source = new String(Files.readAllBytes(Paths.get(in.toURI())));
        session.setSourceDocument("-MkqPH7Gav60JEAxwZ2K", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object target = session.getTargetDocument("-MkqPFJhav60JEAxwZ2K");
        assertNotNull(target);
        LOG.info(target.toString());
        JsonNode root = new ObjectMapper().readTree(target.toString());
        JsonNode column2 = root.get("column2");
        assertEquals("task1", column2.asText());
        JsonNode task = root.get("task");
        assertEquals("Red Hat", task.asText());
    }

}
