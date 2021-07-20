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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.builder.DefaultAtlasMappingBuilder;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class MappingBuilderTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-builder.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
                Thread.currentThread().getContextClassLoader().getResource("data/json-source-builder.json").toURI())));
            session.setSourceDocument("SourceJson", sourceJson);
            context.process(session);
            assertFalse(session.hasErrors(), TestHelper.printAudit(session));
            String targetXml = (String) session.getTargetDocument("TargetXml");
            assertNotNull("target XML is null", targetXml);
            HashMap<String, String> namespaces = new HashMap<>();
            namespaces.put("tns", "http://atlasmap.io/itests/builder");
            XmlAssert.assertThat(targetXml).withNamespaceContext(namespaces)
                .valueByXPath("//foo/bar/test").isEqualTo("4123562");
    }

    public static class JsonXmlBuilder extends DefaultAtlasMappingBuilder {
        @Override
        public void processMapping() throws Exception {
            read("SourceJson", "/sourceOrderList/orderBatchNumber")
                .write("TargetXml", "/foo/bar/test");
        }
    }

}
