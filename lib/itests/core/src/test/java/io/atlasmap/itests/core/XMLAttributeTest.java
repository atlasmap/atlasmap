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
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class XMLAttributeTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-xml-attribute-ns.json");
        AtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        AtlasContext context = factory.createContext(url.toURI());
        AtlasSession session = context.createSession();
        String xmlSource = TestHelper.readStringFromFile("data/xml-source-attribute-ns.xml");
        session.setSourceDocument("po-example-schema-f81424a0-8871-4483-abaf-059cc432ea78", xmlSource);

        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object target = session.getTargetDocument("po-example-schema-b12bc688-7bf3-4626-97a2-d8c3981ecd3a");
        assertNotNull(target, "target XML is null");
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("ns1", "http://tempuri.org/po.xsd");
        XmlAssert.assertThat(target).withNamespaceContext(namespaces)
            .valueByXPath("//ns1:purchaseOrder/@ns1:orderDate").isEqualTo("1985-05-02");
        XmlAssert.assertThat(target).withNamespaceContext(namespaces)
        .valueByXPath("//ns1:purchaseOrder/ns1:comment").isEqualTo("1985-05-02");
    }

}
