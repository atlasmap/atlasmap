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
package io.atlasmap.reference.xmlToJava;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Ignore;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.Validation;

@Ignore // TODO: Complete implementation and normalization of test to AtlasTestUtil
public class XmlJavaAutoConversionTest extends AtlasMappingBaseTest {
    
    protected BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz) throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass)targetClazz.newInstance();
        
        newObject.setBooleanField(false);
        newObject.setByteField((byte) 99);
        newObject.setCharField((char)'a');
        newObject.setDoubleField(50000000d);
        newObject.setFloatField(40000000f);
        newObject.setIntField(2);
        newObject.setLongField(30000L);        
        newObject.setShortField((short)1);
        return newObject;
    }
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion1(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(40000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Long(50000000L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 30000), new Short(targetObject.getShortField()));
        assertEquals(new Character('2'), new Character(targetObject.getCharField()));
        
        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        
        // Unused by mapping
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
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion2(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(0.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(97f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(30000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(40000000L), new Long(targetObject.getLongField()));
        assertTrue(Character.valueOf((char)1) == targetObject.getCharField());
        
        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

        // Unused by mapping
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
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion3(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Double(97d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(50000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(0L), new Long(targetObject.getLongField()));
        assertTrue(Character.valueOf((char)30000) == targetObject.getCharField());
        
        // Primitive auto-initialized values
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

        // Unused by mapping
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
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion4(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(1.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(40000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(97L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

        // Unused by mapping
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
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion5(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(30000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(0), new Integer(targetObject.getIntField()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 97), new Short(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

        // Unused by mapping
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
    
    protected void validateFlatPrimitiveClassPrimitiveFieldAutoConversion6(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(30000.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(50000000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(97), new Integer(targetObject.getIntField()));
        assertEquals(new Long(1L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));

        // Unused by mapping
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
        assertEquals(new Boolean(Boolean.TRUE), targetObject.getBoxedBooleanField());
        assertEquals(new Byte((byte) 87), new Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField()));
        assertNull(targetObject.getBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(false == targetObject.isBooleanField());
        assertNull(targetObject.getBoxedByteArrayField());
        assertTrue((byte)0 == targetObject.getByteField());
        assertNull(targetObject.getBoxedCharArrayField());
        assertTrue((char)'\u0000' == targetObject.getCharField());
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
    public void testProcessJavaJavaFlatFieldMappingAutoConversion1() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-1.xml").toURI());
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion1((TargetFlatPrimitiveClass)object);
    }
    
    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion2() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-2.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion2((TargetFlatPrimitiveClass)object);
    }
    
    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion3() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-3.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion3((TargetFlatPrimitiveClass)object);
    }
    
    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion4() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-4.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion4((TargetFlatPrimitiveClass)object);
    }
    
    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion5() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-autoconversion-5.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion5((TargetFlatPrimitiveClass)object);
    }
    
    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion6() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-6.xml"));
        ((DefaultAtlasContext)context).setNewProcessFlow(true);

        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceClass = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceClass);
        context.process(session);
        
        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFieldAutoConversion6((TargetFlatPrimitiveClass)object);
    }
    
    protected void printValidation(Validation v) {
        //System.out.println("Validation n=" + v.getName() + " f=" + v.getField() + " g=" + v.getGroup() + " v=" + v.getValue() + " s=" + v.getStatus() + " msg=" + v.getMessage());
    }

}
