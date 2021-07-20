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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;

public class XmlSchemaInspectionRecursiveTest extends BaseXmlInspectionServiceTest {

    @Test
    public void test() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/recursive.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(1, root.getXmlFields().getXmlField().size());
        debugFields(xmlDocument.getFields());
    }

}
