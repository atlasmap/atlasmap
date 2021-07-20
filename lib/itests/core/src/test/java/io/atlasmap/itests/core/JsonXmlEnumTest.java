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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class JsonXmlEnumTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-json-xml-enum.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        assertForValues(context, "NA", "Available", "NA", "Available");
        assertForValues(context, "EMEA", "Pending", "EMEA", "Pending");
        assertForValues(context, "LATAM", "Sold", "LATAM", "Sold");
        assertForValues(context, "APAC", "Available", "NA", "Available");
    }

    private void assertForValues(AtlasContext context, String sourceJsonValue, String sourceXmlValue,
            String targetJsonValue, String targetXmlValue) throws Exception {
        AtlasSession session = context.createSession();
        String sourceJson = String.format("{\"region\": \"%s\"}", sourceJsonValue);
        session.setSourceDocument("address-enum-schema-19eabdd2-fec0-439a-824f-47f514a06177", sourceJson);
        String sourceXml = String.format(
                "<tns:request xmlns:tns=\"http://syndesis.io/v1/swagger-connector-template/request\">"
                + "<tns:body><Pet><status>%s</status></Pet></tns:body></tns:request>", sourceXmlValue);
        session.setSourceDocument("XMLSchemaSource-2c88ee00-7ddc-4137-b906-52d56e9b7f9e", sourceXml);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));

        String targetJson = (String) session.getTargetDocument("address-enum-schema-afdf5b0b-416a-4b7a-a4ba-f6219af64f43");
        JsonNode root = new ObjectMapper().readTree(targetJson);
        JsonNode field = root.get("region");
        assertFalse(field.isNull());
        assertTrue(field.isTextual());
        assertEquals(targetJsonValue, field.asText());

        String targetXml = (String) session.getTargetDocument("XMLSchemaSource-c1b7b86e-959a-4cd8-b1fd-0bf52ddf0f43");
        assertNotNull("target XML is null", targetXml);
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("tns", "http://syndesis.io/v1/swagger-connector-template/request");
        XmlAssert.assertThat(targetXml).withNamespaceContext(namespaces)
            .valueByXPath("//tns:request/tns:body/Pet/status").isEqualTo(targetXmlValue);
        
    }
}
