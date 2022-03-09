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
package io.atlasmap.xml.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;

public class XmlSchemaInspection3826Test extends BaseXmlInspectionServiceTest {

    @Test
    public void testMultipleNamespaces() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/schemaset-3826.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        Map<String, XmlNamespace> namespaces = new HashMap<>();
        answer.getXmlNamespaces().getXmlNamespace().forEach(ns -> {
            if (namespaces.containsKey(ns.getAlias())) {
                XmlNamespace dup = namespaces.get(ns.getAlias());
                fail(String.format(
                    "Duplicate namespace detected, xmlns:%s='%s' / xmlns:%s='%s'",
                    dup.getAlias(), dup.getUri(),
                    ns.getAlias(), ns.getUri()
                ));
            }
            namespaces.put(ns.getAlias(), ns);
        });
        XmlComplexType envelop = (XmlComplexType) answer.getFields().getField().get(0);
        assertEquals("tns:envelop", envelop.getName());
        assertEquals("it.redhat.atlasmap.sample/env", namespaces.get("tns").getUri());
        XmlComplexType customer = (XmlComplexType) envelop.getXmlFields().getXmlField().get(0);
        assertEquals("cus:customer", customer.getName());
        assertEquals("it.redhat.atlasmap.sample/customer", namespaces.get("cus").getUri());
        XmlField name = customer.getXmlFields().getXmlField().get(0);
        assertEquals("cus:name", name.getName());
    }

}
