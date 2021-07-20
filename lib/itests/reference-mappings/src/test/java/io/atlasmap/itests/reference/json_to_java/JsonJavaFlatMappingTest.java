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
package io.atlasmap.itests.reference.json_to_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;

public class JsonJavaFlatMappingTest extends AtlasMappingBaseTest {

    protected AtlasMapping generateJsonJavaFlatMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("JsonJavaFlatMapping");
        atlasMapping.getDataSource().add(generateDataSource("atlas:json", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource(
                "atlas:java?className=io.atlasmap.java.test.TargetFlatPrimitiveClass", DataSourceType.TARGET));

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();

        // Add fieldMappings
        for (String fieldName : FLAT_FIELDS) {
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(generateJsonField("/" + fieldName));
            mfm.getOutputField().add(generateJavaField("/" + fieldName));
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
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.getDeclaredConstructor().newInstance();

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
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setBooleanField(Boolean.valueOf(Boolean.FALSE));
        newObject.setByteField(Byte.valueOf((byte) 99));
        newObject.setCharField(Character.valueOf('a'));
        newObject.setDoubleField(Double.valueOf(50000000d));
        newObject.setFloatField(Float.valueOf(40000000f));
        newObject.setIntField(Integer.valueOf(2));
        newObject.setLongField(Long.valueOf(30000L));
        newObject.setShortField(Short.valueOf((short) 1));
        return newObject;
    }

    protected BaseFlatPrimitiveClass generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(
            Class<? extends BaseFlatPrimitiveClass> clazz) throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setBoxedBooleanField(Boolean.valueOf(Boolean.TRUE));
        newObject.setBoxedByteField(Byte.valueOf((byte) 87));
        newObject.setBoxedCharField(Character.valueOf('z'));
        newObject.setBoxedDoubleField(Double.valueOf(90000000d));
        newObject.setBoxedFloatField(Float.valueOf(70000000f));
        newObject.setBoxedIntField(Integer.valueOf(5));
        newObject.setBoxedLongField(Long.valueOf(20000L));
        newObject.setBoxedShortField(Short.valueOf((short) 5));
        return newObject;
    }

    protected void validateFlatPrimitiveClassPrimitiveFields(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(50000000d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(40000000f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(2), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(30000L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 1), Short.valueOf(targetObject.getShortField()));
        assertEquals(Boolean.FALSE, targetObject.isBooleanField());
        // assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(Character.valueOf('a'), Character.valueOf(targetObject.getCharField()));
        assertNull(targetObject.getBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanField());
        assertNull(targetObject.getBoxedByteArrayField());
        assertNull(targetObject.getBoxedByteField());
        assertNull(targetObject.getBoxedCharArrayField());
        assertNull(targetObject.getBoxedCharField());
        assertNull(targetObject.getBoxedDoubleArrayField());
        assertNull(targetObject.getBoxedDoubleField());
        assertNull(targetObject.getBoxedFloatArrayField());
        assertNull(targetObject.getBoxedFloatField());
        assertNull(targetObject.getBoxedIntArrayField());
        assertNull(targetObject.getBoxedIntField());
        assertNull(targetObject.getBoxedLongArrayField());
        assertNull(targetObject.getBoxedLongField());
        assertNull(targetObject.getBoxedShortArrayField());
        assertNull(targetObject.getBoxedShortField());
        assertNull(targetObject.getBoxedStringArrayField());
        assertNull(targetObject.getBoxedStringField());
    }

    protected void validateFlatPrimitiveClassBoxedPrimitiveFields(BaseFlatPrimitiveClass targetObject) {
        assertEquals(Double.valueOf(90000000d), Double.valueOf(targetObject.getBoxedDoubleField()));
        assertEquals(Float.valueOf(70000000f), Float.valueOf(targetObject.getBoxedFloatField()));
        assertEquals(Integer.valueOf(5), Integer.valueOf(targetObject.getBoxedIntField()));
        assertEquals(Long.valueOf(20000L), Long.valueOf(targetObject.getBoxedLongField()));
        assertEquals(Short.valueOf((short) 5), Short.valueOf(targetObject.getBoxedShortField()));
        assertEquals(Boolean.TRUE, targetObject.getBoxedBooleanField());
        // assertEquals(new Byte((byte) 87), new
        // Byte(targetObject.getBoxedByteField()));
        assertEquals(Character.valueOf('z'), Character.valueOf(targetObject.getBoxedCharField()));
        assertNull(targetObject.getBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanArrayField());
        assertFalse(targetObject.isBooleanField());
        assertNull(targetObject.getBoxedByteArrayField());
        assertTrue((byte) 0 == targetObject.getByteField());
        assertNull(targetObject.getBoxedCharArrayField());
        assertTrue('\u0000' == targetObject.getCharField());
        assertNull(targetObject.getBoxedDoubleArrayField());
        assertTrue(0.0d == targetObject.getDoubleField());
        assertNull(targetObject.getBoxedFloatArrayField());
        assertTrue(0.0f == targetObject.getFloatField());
        assertNull(targetObject.getBoxedIntArrayField());
        assertTrue(0 == targetObject.getIntField());
        assertNull(targetObject.getBoxedLongArrayField());
        assertTrue(0L == targetObject.getLongField());
        assertNull(targetObject.getBoxedShortArrayField());
        assertTrue(0 == targetObject.getShortField());
        assertNull(targetObject.getBoxedStringArrayField());
        assertNotNull(targetObject.getBoxedStringField());
        assertEquals("boxedStringValue", targetObject.getBoxedStringField());
    }

    @Test
    public void testCreateJsonJavaFlatFieldMappings() throws Exception {
        AtlasMapping atlasMapping = generateJsonJavaFlatMapping();
        File path = new File("target/reference-mappings/jsonToJava");
        path.mkdirs();
        Json.mapper().writeValue(new File(path, "atlasmapping-flatprimitive.xml"), atlasMapping);
    }

    @Test
    public void testProcessJsonJavaFlatPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted.json"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-flatprimitive-unrooted.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJava/atlasmapping-flatprimitive-rooted.json"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-flatprimitive-rooted.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaBoxedFlatMappingPrimitiveUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJava/atlasmapping-boxedflatprimitive-unrooted.json"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-boxedflatprimitive-unrooted.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaBoxedFlatMappingPrimitiveRooted() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJava/atlasmapping-boxedflatprimitive-rooted.json"));

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-boxedflatprimitive-rooted.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

}
