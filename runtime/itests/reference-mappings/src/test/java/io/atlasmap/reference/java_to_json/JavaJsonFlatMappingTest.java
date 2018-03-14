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
package io.atlasmap.reference.java_to_json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
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

public class JavaJsonFlatMappingTest extends AtlasMappingBaseTest {

    protected AtlasMapping generateJsonJavaFlatMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("JsonJavaFlatMapping");
        atlasMapping.getDataSource().add(generateDataSource(
                "atlas:java?className=io.atlasmap.java.test.SourceFlatPrimitiveClass", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource("atlas:json", DataSourceType.TARGET));

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();

        // Add fieldMappings
        for (String fieldName : FLAT_FIELDS) {
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(generateJavaField("/" + fieldName));
            mfm.getOutputField().add(generateJsonField("/" + fieldName));
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

    protected BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz)
            throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.newInstance();

        newObject.setBooleanField(false);
        newObject.setByteField((byte) 99);
        newObject.setCharField('a');
        newObject.setDoubleField(50000000d);
        newObject.setFloatField(40000000f);
        newObject.setIntField(2);
        newObject.setLongField(30000L);
        newObject.setShortField((short) 1);
        return newObject;
    }

    protected BaseFlatPrimitiveClass generateFlatPrimitiveClassPrimitiveFieldsBoxedValues(
            Class<? extends BaseFlatPrimitiveClass> clazz) throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.newInstance();

        newObject.setBooleanField(Boolean.FALSE);
        newObject.setByteField(new Byte((byte) 99));
        newObject.setCharField(new Character('a'));
        newObject.setDoubleField(new Double(50000000d));
        newObject.setFloatField(new Float(40000000f));
        newObject.setIntField(new Integer(2));
        newObject.setLongField(new Long(30000L));
        newObject.setShortField(new Short((short) 1));
        return newObject;
    }

    protected BaseFlatPrimitiveClass generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(
            Class<? extends BaseFlatPrimitiveClass> clazz) throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.newInstance();

        newObject.setBoxedBooleanField(Boolean.valueOf(Boolean.TRUE));
        newObject.setBoxedByteField(new Byte((byte) 87));
        newObject.setBoxedCharField(new Character('z'));
        newObject.setBoxedDoubleField(new Double(90000000d));
        newObject.setBoxedFloatField(new Float(70000000f));
        newObject.setBoxedIntField(new Integer(5));
        newObject.setBoxedLongField(new Long(20000L));
        newObject.setBoxedShortField(new Short((short) 5));
        newObject.setBoxedStringField("boxedStringValue");
        return newObject;
    }

    @Test
    public void testCreateJavaJsonFlatFieldMappings() throws Exception {
        AtlasMapping atlasMapping = generateJsonJavaFlatMapping();
        AtlasMappingService atlasMappingService = new AtlasMappingService(
                Arrays.asList("io.atlasmap.v2", "io.atlasmap.java.v2", "io.atlasmap.json.v2"));
        File path = new File("target/reference-mappings/javaToJson");
        path.mkdirs();
        atlasMappingService.saveMappingAsFile(atlasMapping,
                new File(path, "atlasmapping-flatprimitive.xml"));
    }

    @Test
    public void testProcessJavaJsonFlatPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted.xml"));

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class));
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
    }

    @Test
    public void testProcessJavaJsonFlatPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-rooted.xml"));

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class));
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
    }

    @Test
    public void testProcessJavaJsonBoxedFlatMappingPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJson/atlasmapping-boxedflatprimitive-unrooted.xml"));

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(SourceFlatPrimitiveClass.class));
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
    }

    @Test
    public void testProcessJavaJsonBoxedFlatMappingPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJson/atlasmapping-boxedflatprimitive-rooted.xml"));

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(SourceFlatPrimitiveClass.class));
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitiveBoxedPrimitiveFields(targetObject);
    }

}
