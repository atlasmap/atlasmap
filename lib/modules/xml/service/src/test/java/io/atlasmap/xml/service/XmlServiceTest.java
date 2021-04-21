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
package io.atlasmap.xml.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.v2.Json;
import io.atlasmap.xml.v2.InspectionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;

import java.util.ArrayList;
import java.util.List;

public class XmlServiceTest {

    private XmlService xmlService = null;

    final String sourceXml = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
        "  <xs:element name=\"root\"><xs:complexType><xs:sequence>\n" +
        "    <xs:element type=\"xs:int\" name=\"intField\"/>\n" +
        "    <xs:element type=\"xs:long\" name=\"longField\"/>\n" +
        "    <xs:element type=\"xs:string\" name=\"stringField\"/>\n" +
        "    <xs:element type=\"xs:boolean\" name=\"booleanField\"/>\n" +
        "    <xs:element type=\"xs:double\" name=\"doubleField\"/>\n" +
        "    <xs:element type=\"xs:short\" name=\"shortField\"/>\n" +
        "    <xs:element type=\"xs:float\" name=\"floatField\"/>\n" +
        "    <xs:element name=\"level1Field\"><xs:complexType><xs:sequence>" +
        "      <xs:element type=\"xs:string\" name=\"level2SimpleField\"/>" +
        "      <xs:element name=\"level2Field\"><xs:complexType><xs:sequence>" +
        "        <xs:element type=\"xs:string\" name=\"level3SimpleField\"/>" +
        "      </xs:sequence></xs:complexType></xs:element>" +
        "      <xs:element name=\"level2Field2\"><xs:complexType><xs:sequence>" +
        "        <xs:element type=\"xs:string\" name=\"level3SimpleField\"/>" +
        "      </xs:sequence></xs:complexType></xs:element>" +
        "    </xs:sequence></xs:complexType></xs:element>\n" +
        "    <xs:element type=\"xs:string\" name=\"charField\"/>\n" +
        "  </xs:sequence></xs:complexType></xs:element>" +
        "</xs:schema>";

    @BeforeEach
    public void setUp() {
        xmlService = new XmlService();
    }

    @AfterEach
    public void tearDown() {
        xmlService = null;
    }

    @Test
    public void testValidJsonData() throws Exception {
        final String source = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xs:element name=\"data\">\n" + "    <xs:complexType>\n" + "      <xs:sequence>\n"
                + "        <xs:element type=\"xs:int\" name=\"intField\"/>\n"
                + "        <xs:element type=\"xs:long\" name=\"longField\"/>\n"
                + "        <xs:element type=\"xs:string\" name=\"stringField\"/>\n"
                + "        <xs:element type=\"xs:boolean\" name=\"booleanField\"/>\n"
                + "        <xs:element type=\"xs:double\" name=\"doubleField\"/>\n"
                + "        <xs:element type=\"xs:short\" name=\"shortField\"/>\n"
                + "        <xs:element type=\"xs:float\" name=\"floatField\"/>\n"
                + "        <xs:element type=\"xs:string\" name=\"charField\"/>\n" + "      </xs:sequence>\n"
                + "    </xs:complexType>\n" + "  </xs:element>\n" + "</xs:schema>";

        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(source);
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
    }

    @Test
    public void testAllLevelsIncludedIfIncludePathsEmpty() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        request.setInspectPaths(new ArrayList<>());
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        XmlComplexType level1Field = (XmlComplexType) root.getXmlFields().getXmlField().get(7);
        assertEquals(3, level1Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(1);
        assertEquals(1, level2Field.getXmlFields().getXmlField().size());
    }

    @Test
    public void testRootLevelIncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root");
        request.setInspectPaths(includePaths);
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        XmlComplexType level1Field = (XmlComplexType) root.getXmlFields().getXmlField().get(7);
        assertEquals(0, level1Field.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel1IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field");
        request.setInspectPaths(includePaths);
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        XmlComplexType level1Field = (XmlComplexType) root.getXmlFields().getXmlField().get(7);
        assertEquals(3, level1Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(1);
        assertEquals(0, level2Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field2 = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(2);
        assertEquals(0, level2Field2.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel2IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field/level2Field");
        request.setInspectPaths(includePaths);
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        XmlComplexType level1Field = (XmlComplexType) root.getXmlFields().getXmlField().get(7);
        assertEquals(3, level1Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(1);
        assertEquals(1, level2Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field2 = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(2);
        assertEquals(0, level2Field2.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel2Field2IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field/level2Field2");
        request.setInspectPaths(includePaths);
        Response res = xmlService.inspect(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(9, root.getXmlFields().getXmlField().size());
        XmlComplexType level1Field = (XmlComplexType) root.getXmlFields().getXmlField().get(7);
        assertEquals(3, level1Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(1);
        assertEquals(0, level2Field.getXmlFields().getXmlField().size());
        XmlComplexType level2Field2 = (XmlComplexType) level1Field.getXmlFields().getXmlField().get(2);
        assertEquals(1, level2Field2.getXmlFields().getXmlField().size());
    }
}
