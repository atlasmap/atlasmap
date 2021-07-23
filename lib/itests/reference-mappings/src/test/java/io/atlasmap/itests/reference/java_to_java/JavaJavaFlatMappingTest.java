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
package io.atlasmap.itests.reference.java_to_java;

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
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

public class JavaJavaFlatMappingTest extends AtlasMappingBaseTest {

    protected AtlasMapping generateJavaJavaFlatMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("JavaJavaFlatMapping");
        atlasMapping.getDataSource().add(generateDataSource(
                "atlas:java?className=io.atlasmap.java.test.SourceFlatPrimitiveClass", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource(
                "atlas:java?className=io.atlasmap.java.test.TargetFlatPrimitiveClass", DataSourceType.TARGET));

        List<BaseMapping> mappings = atlasMapping.getMappings().getMapping();

        // Add fieldMappings
        for (String fieldName : FLAT_FIELDS) {
            Mapping mfm = AtlasModelFactory.createMapping(MappingType.MAP);
            mfm.getInputField().add(generateJavaField(fieldName));
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
        assertEquals(Byte.valueOf((byte) 99), Byte.valueOf(targetObject.getByteField()));
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
        assertEquals(Byte.valueOf((byte) 87), Byte.valueOf(targetObject.getBoxedByteField()));
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
        assertNull(targetObject.getBoxedStringField());
    }

    @Test
    public void testCreateJavaJavaFlatFieldMapping() throws Exception {
        AtlasMapping atlasMapping = generateJavaJavaFlatMapping();
        File path = new File("target/reference-mappings/javaToJava");
        path.mkdirs();
        Json.mapper().writeValue(new File(path, "atlasmapping-flatprimitive.json"), atlasMapping);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMapping() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive.json").toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setDefaultSourceDocument(sourceClass);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingPrimitivesBoxedValues() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive.json").toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClassPrimitiveFieldsBoxedValues(
                SourceFlatPrimitiveClass.class);
        session.setDefaultSourceDocument(sourceClass);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass) object);

        Validations validations = session.getValidations();
        for (Validation v : validations.getValidation()) {
            printValidation(v);
        }
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingBoxedPrimitives() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-boxed.json").toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(
                SourceFlatPrimitiveClass.class);
        session.setDefaultSourceDocument(sourceClass);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassBoxedPrimitiveFields((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatExpression() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-expression.json").toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClassBoxedPrimitiveFieldsBoxedValues(
                SourceFlatPrimitiveClass.class);
        sourceClass.setBooleanField(true);
        session.setSourceDocument("SourceDoc", sourceClass);
        context.process(session);

        assertEquals(0, session.getAudits().getAudit().size(), printAudit(session));
        Object object = session.getTargetDocument("TargetDoc");
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        TargetFlatPrimitiveClass target = (TargetFlatPrimitiveClass)object;
        assertEquals("YES", target.getBoxedStringArrayField()[0]);
        assertEquals("YES", target.getBoxedStringArrayField()[1]);
    }

    protected void printValidation(Validation v) {
        // System.out.println("Validation n=" + v.getName() + " f=" + v.getField() + "
        // g=" + v.getGroup() + " v=" + v.getValue() + " s=" + v.getStatus() + " msg=" +
        // v.getMessage());
    }

}
