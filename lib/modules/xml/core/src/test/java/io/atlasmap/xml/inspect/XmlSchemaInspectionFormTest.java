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
import io.atlasmap.xml.v2.XmlField;

public class XmlSchemaInspectionFormTest extends BaseXmlInspectionServiceTest {

    @Test
    public void testInspectSchemaFileFormNoDefault() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/form-no-default-schema.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertDefaultUnqualified(xmlDocument);
    }

    @Test
    public void testInspectSchemaFileFormDefaultUnqualified() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/form-default-unqualified-schema.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertDefaultUnqualified(xmlDocument);
    }

    private void assertDefaultUnqualified(XmlDocument xmlDocument) throws Exception {
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType a = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(a);
        assertEquals("tns:a", a.getName());
        assertEquals(6, a.getXmlFields().getXmlField().size());
        XmlComplexType aa = (XmlComplexType) a.getXmlFields().getXmlField().get(3);
        assertEquals("aa", aa.getName());
        assertEquals(4, aa.getXmlFields().getXmlField().size());
        XmlField aaa = aa.getXmlFields().getXmlField().get(1);
        assertEquals("aaa", aaa.getName());
        XmlField aab = aa.getXmlFields().getXmlField().get(2);
        assertEquals("tns:aab", aab.getName());
        XmlField aac = aa.getXmlFields().getXmlField().get(3);
        assertEquals("aac", aac.getName());
        XmlField aad = aa.getXmlFields().getXmlField().get(0);
        assertEquals("aad", aad.getName());
        XmlComplexType ab = (XmlComplexType) a.getXmlFields().getXmlField().get(4);
        assertEquals("tns:ab", ab.getName());
        XmlField aba = ab.getXmlFields().getXmlField().get(1);
        assertEquals("aba", aba.getName());
        XmlField abb = ab.getXmlFields().getXmlField().get(2);
        assertEquals("tns:abb", abb.getName());
        XmlField abc = ab.getXmlFields().getXmlField().get(3);
        assertEquals("abc", abc.getName());
        XmlField abd = ab.getXmlFields().getXmlField().get(0);
        assertEquals("abd", abd.getName());
        XmlComplexType ac = (XmlComplexType) a.getXmlFields().getXmlField().get(5);
        assertEquals("ac", ac.getName());
        XmlField aca = ac.getXmlFields().getXmlField().get(1);
        assertEquals("aca", aca.getName());
        XmlField acb = ac.getXmlFields().getXmlField().get(2);
        assertEquals("tns:acb", acb.getName());
        XmlField acc = ac.getXmlFields().getXmlField().get(3);
        assertEquals("acc", acc.getName());
        XmlField acd = ac.getXmlFields().getXmlField().get(0);
        assertEquals("acd", acd.getName());
        XmlField ad = a.getXmlFields().getXmlField().get(0);
        assertEquals("ad", ad.getName());
        XmlField ae = a.getXmlFields().getXmlField().get(1);
        assertEquals("tns:ae", ae.getName());
        XmlField af = a.getXmlFields().getXmlField().get(2);
        assertEquals("af", af.getName());
    }

    @Test
    public void testInspectSchemaFileFormDefaultQualified() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/form-default-qualified-schema.xsd").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument xmlDocument = service.inspectSchema(schemaFile);
        assertNotNull(xmlDocument);
        assertNotNull(xmlDocument.getFields());
        assertEquals(1, xmlDocument.getFields().getField().size());
        XmlComplexType a = (XmlComplexType) xmlDocument.getFields().getField().get(0);
        assertNotNull(a);
        assertEquals("tns:a", a.getName());
        assertEquals(6, a.getXmlFields().getXmlField().size());
        XmlComplexType aa = (XmlComplexType) a.getXmlFields().getXmlField().get(3);
        assertEquals("tns:aa", aa.getName());
        assertEquals(4, aa.getXmlFields().getXmlField().size());
        XmlField aaa = aa.getXmlFields().getXmlField().get(1);
        assertEquals("tns:aaa", aaa.getName());
        XmlField aab = aa.getXmlFields().getXmlField().get(2);
        assertEquals("tns:aab", aab.getName());
        XmlField aac = aa.getXmlFields().getXmlField().get(3);
        assertEquals("aac", aac.getName());
        XmlField aad = aa.getXmlFields().getXmlField().get(0);
        assertEquals("tns:aad", aad.getName());
        XmlComplexType ab = (XmlComplexType) a.getXmlFields().getXmlField().get(4);
        assertEquals("tns:ab", ab.getName());
        XmlField aba = ab.getXmlFields().getXmlField().get(1);
        assertEquals("tns:aba", aba.getName());
        XmlField abb = ab.getXmlFields().getXmlField().get(2);
        assertEquals("tns:abb", abb.getName());
        XmlField abc = ab.getXmlFields().getXmlField().get(3);
        assertEquals("abc", abc.getName());
        XmlField abd = ab.getXmlFields().getXmlField().get(0);
        assertEquals("tns:abd", abd.getName());
        XmlComplexType ac = (XmlComplexType) a.getXmlFields().getXmlField().get(5);
        assertEquals("ac", ac.getName());
        XmlField aca = ac.getXmlFields().getXmlField().get(1);
        assertEquals("tns:aca", aca.getName());
        XmlField acb = ac.getXmlFields().getXmlField().get(2);
        assertEquals("tns:acb", acb.getName());
        XmlField acc = ac.getXmlFields().getXmlField().get(3);
        assertEquals("acc", acc.getName());
        XmlField acd = ac.getXmlFields().getXmlField().get(0);
        assertEquals("tns:acd", acd.getName());
        XmlField ad = a.getXmlFields().getXmlField().get(0);
        assertEquals("tns:ad", ad.getName());
        XmlField ae = a.getXmlFields().getXmlField().get(1);
        assertEquals("tns:ae", ae.getName());
        XmlField af = a.getXmlFields().getXmlField().get(2);
        assertEquals("af", af.getName());
    }

}
