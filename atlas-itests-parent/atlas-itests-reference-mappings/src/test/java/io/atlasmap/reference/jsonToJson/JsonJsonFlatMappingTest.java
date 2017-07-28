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
package io.atlasmap.reference.jsonToJson;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.json.test.AtlasJsonTestRootedMapper;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;

public class JsonJsonFlatMappingTest extends AtlasMappingBaseTest {
    
    protected AtlasMapping generateJsonJsonFlatMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("JsonJsonFlatMapping");
        atlasMapping.getDataSource().add(generateDataSource("atlas:json", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource("atlas:json", DataSourceType.TARGET));

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();

        // Add fieldMappings
        for (String fieldName : FLAT_FIELDS) {
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(generateJsonField(fieldName));
            mfm.getOutputField().add(generateJavaField(fieldName));
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
    
    protected JsonField generateJsonField(String path) {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath(path);
        return field;
    }
    
    protected JsonField generateJsonField(String parent, String path) {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/" + parent + "/" + path);
        return field;
    }
    
    protected JavaField generateJavaField(String path) {
        JavaField javaField = AtlasJavaModelFactory.createJavaField();
        javaField.setPath(path);
        javaField.setModifiers(null);
        return javaField;
    }

    @Test   
    public void testCreateJsonJavaFlatFieldMappings() throws Exception {
        AtlasMapping atlasMapping = generateJsonJsonFlatMapping();
        AtlasMappingService atlasMappingService = new AtlasMappingService(Arrays.asList("io.atlasmap.v2", "io.atlasmap.json.v2"));
        atlasMappingService.saveMappingAsFile(atlasMapping, new File("src/test/resources/jsonToJson/atlasmapping-flatprimitive.xml"));
    }

    @Test
    public void testProcessJsonJsonFlatPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted.xml"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString("src/test/resources/jsonToJson/atlas-json-flatprimitive-unrooted.json");
        session.setInput(source);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String)object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
    }
        
    @Test
    public void testProcessJsonJsonFlatPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/jsonToJson/atlasmapping-flatprimitive-rooted.xml"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString("src/test/resources/jsonToJson/atlas-json-flatprimitive-rooted.json");
        session.setInput(source);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String)object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
    }
              
    @Test
    public void testProcessJsonJsonBoxedFlatMappingPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/jsonToJson/atlasmapping-boxedflatprimitive-unrooted.xml"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString("src/test/resources/jsonToJson/atlas-json-boxedflatprimitive-unrooted.json");
        session.setInput(source);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String)object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
    }
        
    @Test
    public void testProcessJsonJsonBoxedFlatMappingPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/jsonToJson/atlasmapping-boxedflatprimitive-rooted.xml"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString("src/test/resources/jsonToJson/atlas-json-boxedflatprimitive-rooted.json");
        session.setInput(source);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String)object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
    }

} 
