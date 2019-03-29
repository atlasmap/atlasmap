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
package io.atlasmap.itests.reference.java_to_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
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

        newObject.setBooleanField(Boolean.valueOf(Boolean.FALSE));
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
        return newObject;
    }

    protected void validateFlatPrimitiveClassPrimitiveFields(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(50000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(40000000f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(2), new Integer(targetObject.getIntField()));
        assertEquals(new Long(30000L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField()));
        assertEquals(Boolean.FALSE, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(new Character('a'), new Character(targetObject.getCharField()));
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
        assertEquals(new Double(90000000d), new Double(targetObject.getBoxedDoubleField()));
        assertEquals(new Float(70000000f), new Float(targetObject.getBoxedFloatField()));
        assertEquals(new Integer(5), new Integer(targetObject.getBoxedIntField()));
        assertEquals(new Long(20000L), new Long(targetObject.getBoxedLongField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getBoxedShortField()));
        assertEquals(Boolean.TRUE, targetObject.getBoxedBooleanField());
        assertEquals(new Byte((byte) 87), new Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField()));
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
        AtlasMappingService atlasMappingService = new AtlasMappingService();
        File path = new File("target/reference-mappings/javaToJava");
        path.mkdirs();
        atlasMappingService.saveMappingAsFile(atlasMapping,
                new File(path, "atlasmapping-flatprimitive.json"));
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

    protected void printValidation(Validation v) {
        // System.out.println("Validation n=" + v.getName() + " f=" + v.getField() + "
        // g=" + v.getGroup() + " v=" + v.getValue() + " s=" + v.getStatus() + " msg=" +
        // v.getMessage());
    }

}
