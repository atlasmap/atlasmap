package io.atlasmap.xml.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.Json;
import io.atlasmap.xml.v2.InspectionType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;

public class XmlServiceTest {

    private XmlService xmlService = null;

    @Before
    public void setUp() throws Exception {
        xmlService = new XmlService();
    }

    @After
    public void tearDown() throws Exception {
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
        Response res = xmlService.inspectClass(request);
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        XmlInspectionResponse inspectionResponse = Json.mapper().readValue((byte[])entity, XmlInspectionResponse.class);
        XmlDocument xmlDoc = inspectionResponse.getXmlDocument();
        assertEquals(1, xmlDoc.getFields().getField().size());
        XmlComplexType root = (XmlComplexType) xmlDoc.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(8, root.getXmlFields().getXmlField().size());
    }
}
