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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;

public class XmlSchemaInspectionFhirTest extends BaseXmlInspectionServiceTest {

    @Test
    public void test() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/fhir-patient.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(26, root.getXmlFields().getXmlField().size());
        XmlComplexType id = (XmlComplexType) root.getXmlFields().getXmlField().get(0);
        assertEquals("tns:id", id.getName());
        assertEquals(2, id.getXmlFields().getXmlField().size());
        XmlField value = (XmlField) id.getXmlFields().getXmlField().get(0);
        assertEquals("value", value.getName());
        assertTrue(value.isAttribute());
        XmlComplexType extension = (XmlComplexType) id.getXmlFields().getXmlField().get(1);
        assertEquals("tns:extension", extension.getName());
        assertNull(extension.getStatus());
        assertEquals(40, extension.getXmlFields().getXmlField().size());
        XmlComplexType meta = (XmlComplexType) root.getXmlFields().getXmlField().get(1);
        assertEquals("tns:meta", meta.getName());
        assertNull(meta.getStatus());
        XmlComplexType name = (XmlComplexType) root.getXmlFields().getXmlField().get(9);
        assertEquals("tns:name", name.getName());
        assertEquals(8, name.getXmlFields().getXmlField().size());
        XmlComplexType family = (XmlComplexType) name.getXmlFields().getXmlField().get(3);
        assertEquals("tns:family", family.getName());
        assertEquals(2, family.getXmlFields().getXmlField().size());
        XmlField familyValue = (XmlField) family.getXmlFields().getXmlField().get(0);
        assertEquals(FieldType.STRING, familyValue.getFieldType());
        assertTrue(familyValue.isAttribute());
        XmlComplexType given = (XmlComplexType) name.getXmlFields().getXmlField().get(4);
        assertEquals("tns:given", given.getName());
        assertEquals(2, given.getXmlFields().getXmlField().size());
        XmlField givenValue = (XmlField) given.getXmlFields().getXmlField().get(0);
        assertEquals(FieldType.STRING, givenValue.getFieldType());
        assertTrue(givenValue.isAttribute());
    }

}
