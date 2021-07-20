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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.net.URL;

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
 * https://github.com/atlasmap/atlasmap/issues/846 .
 */
public class AtlasMap846Test {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMap846Test.class);

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-846-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "[]");
        context.process(session);

        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object outputJson = session.getTargetDocument("target-json");
        assertNotNull(outputJson, "target-json document was null");
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree("{\"body\":[],\"three\":[]}");
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target-json >>> {}", actual.toString());
        assertTrue(expected.equals(actual), actual.toString());

        Object outputXml = session.getTargetDocument("target-xml");
        assertNotNull(outputXml, "target-xml document was null");
        assertThat(outputXml).nodesByXPath("/root").exist();
        LOG.info(">>> output:target-xml >>> {}", outputXml.toString());

        Object outputJava = session.getTargetDocument("target-java");
        assertNotNull(outputJava, "target-java document was null");
        assertEquals(TargetClass.class, outputJava.getClass());
        TargetClass targetClass = (TargetClass)outputJava;
        assertEquals(0, targetClass.getTargetList().size());
        assertEquals(0, targetClass.getTargetStringList().size());
    }

    @Test
    public void testHappyPath() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-846-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "[{\"first_name\":\"Tom\",\"last_name\":\"Silva\",\"three\":\"three\"}]");
        context.process(session);

        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object outputJson = session.getTargetDocument("target-json");
        assertNotNull(outputJson, "target-json document was null");
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree("{\"body\":[{\"A\":\"Tom\",\"B\":\"Silva\"}],\"three\":[\"three\"]}");
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target-json >>> {}", actual.toString());
        assertTrue(expected.equals(actual), actual.toString());

        Object outputXml = session.getTargetDocument("target-xml");
        assertNotNull(outputXml, "target-xml document was null");
        assertThat(outputXml).valueByXPath("/root/body/A").isEqualTo("Tom");
        assertThat(outputXml).valueByXPath("/root/body/B").isEqualTo("Silva");
        assertThat(outputXml).valueByXPath("/root/three").isEqualTo("three");
        LOG.info(">>> output:target-xml >>> {}", outputXml.toString());

        Object outputJava = session.getTargetDocument("target-java");
        assertNotNull(outputJava, "target-java document was null");
        assertEquals(TargetClass.class, outputJava.getClass());
        TargetClass targetClass = (TargetClass)outputJava;
        assertEquals(1, targetClass.getTargetList().size());
        assertEquals(1, targetClass.getTargetStringList().size());
    }

}
