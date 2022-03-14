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
import java.util.HashMap;
import java.util.Map;

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
 * https://github.com/atlasmap/atlasmap/issues/3829 .
 */
public class AtlasMap3829Test {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMap3829Test.class);

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-3829.adm");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/issue/atlasmap-3829-source.json");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("CreateEquipment_nbi-62470cd0-20b6-4e32-8722-0ec55f738387", buf.toString());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("testxsd-5d290ff3-bfdd-4f92-80fe-f5fbe2512d06");
        assertNotNull(output);
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("tns", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("ns1", "http://schemas.ericsson.com/cai3g1.2/");
        namespaces.put("ns2", "http://schemas.ericsson.com/ma/EIR/");
        assertThat(output).withNamespaceContext(namespaces)
            .valueByXPath("/tns:Envelope/tns:Body/ns1:Create/ns1:MOAttributes/ns2:CreateEquipment/@imei").isEqualTo("55555987654321");
        assertThat(output).withNamespaceContext(namespaces)
            .valueByXPath("/tns:Envelope/tns:Body/ns1:Create/ns1:MOAttributes/ns2:CreateEquipment/@svn").isEqualTo("16");
    }

}
