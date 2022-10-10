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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.assertj.XmlAssert.assertThat;

/**
 * https://github.com/atlasmap/atlasmap/issues/3942 .
 */
public class AtlasMap3942Test {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMap3942Test.class);

    @Test
    public void testJsonXml() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-3942-jsonxml-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/issue/atlasmap-3942-source-json.json");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("SetSupplementaryService_nbi-46cd3ae2-346c-40f1-bedc-823d7365d834", buf.toString());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("SetSupplementaryService_sbi-8cae15cf-480a-4427-b567-d49a1e9196d2");
        assertNotNull(output);
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("tns", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("ns1", "http://schemas.ericsson.com/cai3g1.2/");
        namespaces.put("ns2", "http://schemas.ericsson.com/ma/CA/Unn/");
        assertThat(output).withNamespaceContext(namespaces)
            .doesNotHaveXPath("/tns:Envelope/tns:Body/ns1:Set/ns1:MOAttributes/ns2:SetUnnSubscription/ns2:lock");
        assertThat(output).withNamespaceContext(namespaces)
            .doesNotHaveXPath("/tns:Envelope/tns:Body/ns1:Set/ns1:MOAttributes/ns2:SetUnnSubscription/ns2:hold/ns2:item/ns2:doesnotexist");
        assertThat(output).withNamespaceContext(namespaces)
            .doesNotHaveXPath("/tns:Envelope/tns:Body/ns1:Set/ns1:MOAttributes/ns2:SetUnnSubscription/ns2:doesnotexist2");
    }

    @Test
    public void testXmlJson() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-3942-xmljson-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/issue/atlasmap-3942-source-xml.xml");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("nb_eda_auc_response_xml_create_xsd_schema-49002548-5abe-4333-9e90-d017b4da6262", buf.toString());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("nb_eda_auc_response_json_create_schema-37a5b6fe-3c92-4f83-a8b2-66c07e833e8f");
        assertNotNull(output);
        JsonNode root = new ObjectMapper().readTree((String)output);
        assertFalse(root.get("SetResponse").get("Status").has("ResultCode"));
        assertFalse(root.get("SetResponse").get("Status").has("Does"));
        assertFalse(root.get("SetResponse").get("Status").has("Does2"));
    }

}
