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
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.v2.Json;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasMappingHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.InspectionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import io.atlasmap.xml.v2.XmlNamespace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlServiceTest {

    private AtlasService atlasService;
    private XmlService xmlService = null;
    private DocumentService documentService;

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
    public void setUp() throws AtlasException {
        atlasService = new AtlasService();
        documentService = new DocumentService(atlasService);
        xmlService = new XmlService(atlasService, documentService);
    }

    @AfterEach
    public void tearDown() {
        xmlService = null;
    }

    @Test
    public void testImportXmlDocument() throws Exception {
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
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(source);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        AtlasMappingHandler handler = atlasService.getADMArchiveHandler(0).getAtlasMappingHandler();
        XmlDataSource ds = (XmlDataSource) handler.getDataSource(new DocumentKey(DataSourceType.SOURCE, "test"));
        assertEquals("test", AtlasUtil.getUriDataType(ds.getUri()));
        assertNull(ds.getXmlNamespaces());
    }

    @Test
    public void testImportXmlDocumentNS() throws Exception {
        final String source = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" "
                + "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://atlasmap.io/namespace/test\">\n"
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
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(source);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(8, root.getXmlFields().getXmlField().size());
        AtlasMappingHandler handler = atlasService.getADMArchiveHandler(0).getAtlasMappingHandler();
        XmlDataSource ds = (XmlDataSource) handler.getDataSource(new DocumentKey(DataSourceType.SOURCE, "test"));
        assertEquals("xml", AtlasUtil.getUriModule(ds.getUri()));
        assertEquals("test", AtlasUtil.getUriDataType(ds.getUri()));
        List<XmlNamespace> namespaces = ds.getXmlNamespaces().getXmlNamespace();
        assertEquals(1, namespaces.size());
        assertEquals("tns", namespaces.get(0).getAlias());
        assertEquals("http://atlasmap.io/namespace/test", namespaces.get(0).getUri());

    }

    @Test
    public void testAllLevelsIncludedIfIncludePathsEmpty() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        request.setInspectPaths(new ArrayList<>());
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
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
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(9, root.getXmlFields().getXmlField().size());
    }

    @Test
    public void testRootLevelIncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root");
        request.setInspectPaths(includePaths);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
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
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(9, root.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel1IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field");
        request.setInspectPaths(includePaths);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
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
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(9, root.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel2IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field/level2Field");
        request.setInspectPaths(includePaths);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
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
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(9, root.getXmlFields().getXmlField().size());
    }

    @Test
    public void testLevel2Field2IncludedIfIncludePathSpecified() throws Exception {
        XmlInspectionRequest request = new XmlInspectionRequest();
        request.setDataSourceType(DataSourceType.SOURCE);
        request.setInspectionType(InspectionType.SCHEMA);
        request.setXmlData(sourceXml);
        List<String> includePaths = new ArrayList<>();
        includePaths.add("/root/level1Field/level2Field2");
        request.setInspectPaths(includePaths);
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = xmlService.importXmlDocument(new ByteArrayInputStream(bytes), 0, DataSourceType.SOURCE, "test", null);
        assertEquals(200, res.getStatus());
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
        res = documentService.getDocumentInspectionResultRequest(0, DataSourceType.SOURCE, "test");
        assertEquals(200, res.getStatus());
        xmlDoc = Json.mapper().readValue((File)res.getEntity(), XmlDocument.class);
        root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertEquals(9, root.getXmlFields().getXmlField().size());
    }
}
