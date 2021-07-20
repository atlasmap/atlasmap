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

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;

public class XmlSchemaInspection2372Test extends BaseXmlInspectionServiceTest {

    @Test
    public void testMultipleNamespaces() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/schemaset-2372.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        XmlComplexType envelope = (XmlComplexType) answer.getFields().getField().get(0);
        assertEquals("tns:Envelope", envelope.getName());
        XmlComplexType body = (XmlComplexType) envelope.getXmlFields().getXmlField().get(0);
        assertEquals("tns:Body", body.getName());
        XmlComplexType fault = (XmlComplexType) body.getXmlFields().getXmlField().get(1);
        assertEquals("tns:Fault", fault.getName());
        XmlField faultstring = fault.getXmlFields().getXmlField().get(1);
        assertEquals("faultstring", faultstring.getName());
        XmlComplexType detail = (XmlComplexType) fault.getXmlFields().getXmlField().get(3);
        assertEquals("detail", detail.getName());
        XmlComplexType bankException = (XmlComplexType) detail.getXmlFields().getXmlField().get(0);
        assertEquals("ns1:bankException", bankException.getName());
        XmlField code = (XmlField) bankException.getXmlFields().getXmlField().get(0);
        assertEquals("ns1:code", code.getName());
    }

}
