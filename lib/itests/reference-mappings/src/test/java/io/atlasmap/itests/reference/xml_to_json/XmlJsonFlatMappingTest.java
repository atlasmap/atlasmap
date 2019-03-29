/**
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
package io.atlasmap.itests.reference.xml_to_json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.json.test.AtlasJsonTestRootedMapper;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlField;

public class XmlJsonFlatMappingTest extends AtlasMappingBaseTest {

    protected AtlasMapping generateXmlJsonFlatMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("XmlJsonFlatMapping");
        atlasMapping.getDataSource()
                .add(generateDataSource("atlas:xml?complexType=XmlFlatPrimitiveAttribute", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource("atlas:json", DataSourceType.TARGET));

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();

        // Add fieldMappings
        for (String fieldName : FLAT_FIELDS) {
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(generateXmlField("/XmlFPA/", fieldName));
            mfm.getOutputField().add(generateJsonField(fieldName));
            mappings.add(mfm);
        }

        return atlasMapping;
    }

    protected DataSource generateDataSource(String uri, DataSourceType type) {
        DataSource ds = new DataSource();
        ds.setUri(uri);
        ds.setDataSourceType(type);
        return ds;
    }

    protected XmlField generateXmlField(String parent, String path) {
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        xmlField.setPath(parent + "@" + path);
        return xmlField;
    }

    protected JsonField generateJsonField(String path) {
        JsonField jsonField = AtlasJsonModelFactory.createJsonField();
        jsonField.setPath(path);
        return jsonField;
    }

    @Test
    public void testCreateXmlJsonFlatFieldMapping() throws Exception {
        AtlasMapping atlasMapping = generateXmlJsonFlatMapping();
        AtlasMappingService atlasMappingService = new AtlasMappingService();
        File path = new File("target/reference-mappings/xmlToJson");
        path.mkdirs();
        atlasMappingService.saveMappingAsFile(atlasMapping,
                new File(path, "atlasmapping-flatprimitive.xml"));
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveAttributeUnrooted() throws Exception {
        processXmlToJsonUnrooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute.xml", false);
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveAttributeRooted() throws Exception {
        processXmlToJsonRooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute.xml", false);
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveAttributeNSUnrooted() throws Exception {
        processXmlToJsonUnrooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-ns-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-ns.xml", false);
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveAttributeNSRooted() throws Exception {
        processXmlToJsonRooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-attribute-ns-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-flatprimitive-attribute-ns.xml", false);
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveElementRooted() throws Exception {
        processXmlToJsonRooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-element-rooted.json",
                "src/test/resources/xmlToJava/atlas-xml-flatprimitive-element.xml", false);
    }

    @Test
    public void testProcessXmlJsonFlatPrimitiveElementUnrooted() throws Exception {
        processXmlToJsonUnrooted("src/test/resources/xmlToJson/atlasmapping-flatprimitive-element-unrooted.json",
                "src/test/resources/xmlToJava/atlas-xml-flatprimitive-element.xml", false);
    }

    @Test
    public void testProcessXmlJavaFlatPrimitiveElementNS() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-element-ns.json"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-element-ns.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        // validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass)object);
    }

    @Test
    public void testProcessXmlJsonBoxedFlatMappingPrimitiveAttributeRooted() throws Exception {
        processXmlToJsonRooted("src/test/resources/xmlToJson/atlasmapping-boxedflatprimitive-attribute-rooted.json",
                "src/test/resources/xmlToJson/atlas-xml-boxedflatprimitive-attribute.xml", true);
    }

    @Test
    public void testProcessXmlJsonBoxedFlatMappingPrimitiveAttributeUnrooted() throws Exception {
        processXmlToJsonUnrooted("src/test/resources/xmlToJson/atlasmapping-boxedflatprimitive-attribute-unrooted.json",
                "src/test/resources/xmlToJson/atlas-xml-boxedflatprimitive-attribute.xml", true);
    }

    @Test
    public void testProcessXmlJavaBoxedFlatMappingPrimitiveAttributeNS() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-boxedflatprimitive-attribute-ns.json"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-boxedflatprimitive-attribute-ns.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        // validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass)object);
    }

    @Test
    public void testProcessXmlJavaBoxedFlatMappingPrimitiveElement() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToJava/atlasmapping-boxedflatprimitive-element.json"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-boxedflatprimitive-element.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        // validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass)object);
    }

    @Test
    public void testProcessXmlJavaBoxedFlatMappingPrimitiveElementNS() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToJava/atlasmapping-boxedflatprimitive-element-ns.json"));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-boxedflatprimitive-element-ns.xml");
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        // validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass)object);
    }

    protected void processXmlToJsonUnrooted(String mappingFile, String inputFile, boolean boxed) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString(inputFile);
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        if (boxed) {
            AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
        } else {
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
        }
    }

    protected void processXmlToJsonRooted(String mappingFile, String inputFile, boolean boxed) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile));

        AtlasSession session = context.createSession();
        String sourceXml = AtlasTestUtil.loadFileAsString(inputFile);
        session.setDefaultSourceDocument(sourceXml);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        if (boxed) {
            AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
        } else {
            AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
        }
    }
}
