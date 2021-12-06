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
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * https://github.com/atlasmap/atlasmap/issues/3549 .
 */
public class AtlasMap3549Test {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMap3549Test.class);

    @Test
    public void test1() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-3549-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/issue/atlasmap-3549-source1.json");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("ocsMapping-b9108c7e-6bce-4b93-99cc-fe46c5c45f5a", buf.toString());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("GBADRequest-508cf937-27b1-4e12-b2a6-2c02f2da2032");
        assertNotNull(output);
        assertThat(output).valueByXPath("/methodCall/methodName").isEqualTo("GetBalanceAndDate");
        assertThat(output).valueByXPath("count(/methodCall/params/param/value/struct/member)").isEqualTo(1);
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/name").isEqualTo("stringFieldName");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/string").isEqualTo("abc");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/boolean").isEqualTo("true");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/dateTime.iso8601").isEqualTo("20070925T21:36:59+0530");
    }

    @Test
    public void test2() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-3549-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/issue/atlasmap-3549-source2.json");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("ocsMapping-b9108c7e-6bce-4b93-99cc-fe46c5c45f5a", buf.toString());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("GBADRequest-508cf937-27b1-4e12-b2a6-2c02f2da2032");
        assertNotNull(output);
        assertThat(output).valueByXPath("/methodCall/methodName").isEqualTo("GetBalanceAndDate");
        assertThat(output).valueByXPath("count(/methodCall/params/param/value/struct/member)").isEqualTo(2);
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/name").isEqualTo("abc1");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/string").isEqualTo("xmyya");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/boolean").isEqualTo("");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[1]/value/dateTime.iso8601").isEqualTo("");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[2]/name").isEqualTo("");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[2]/value/string").isEqualTo("");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[2]/value/boolean").isEqualTo("true");
        assertThat(output).valueByXPath("/methodCall/params/param/value/struct/member[2]/value/dateTime.iso8601").isEqualTo("");
    }

}
