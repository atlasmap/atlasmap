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
package io.atlasmap.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.atlasmap.spi.AtlasPropertyType;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.PropertyField;

public class DefaultAtlasPropertyStrategyTest {

    private DefaultAtlasPropertyStrategy propStrategy = null;
    
    @Before
    public void setUp() throws Exception {
        propStrategy = new DefaultAtlasPropertyStrategy();
        propStrategy.setAtlasConversionService(DefaultAtlasConversionService.getRegistry());
    }

    @After
    public void tearDown() throws Exception {
        propStrategy = null;
    }
    
    protected List<Property> generateAtlasProperties() {
        List<Property> props = new ArrayList<Property>();
        Property p = new Property();
        p.setName("prop-boolean");
        p.setValue("false");
        p.setFieldType(FieldType.BOOLEAN);
        props.add(p);
        
        p = new Property();
        p.setName("prop-byte");
        p.setValue("92");
        p.setFieldType(FieldType.BYTE);
        props.add(p);
        
        p = new Property();
        p.setName("prop-char");
        p.setValue("z");
        p.setFieldType(FieldType.CHAR);
        props.add(p);
        
        p = new Property();
        p.setName("prop-double");
        p.setValue(Double.toString(Double.MIN_VALUE));
        p.setFieldType(FieldType.DOUBLE);
        props.add(p);
        
        p = new Property();
        p.setName("prop-float");
        p.setValue(Float.toString(Float.MIN_VALUE));
        p.setFieldType(FieldType.FLOAT);
        props.add(p);
        
        p = new Property();
        p.setName("prop-int");
        p.setValue(Integer.toString(Integer.MIN_VALUE));
        p.setFieldType(FieldType.INTEGER);
        props.add(p);
        
        p = new Property();
        p.setName("prop-long");
        p.setValue(Long.toString(Long.MIN_VALUE));
        p.setFieldType(FieldType.LONG);
        props.add(p);
        
        p = new Property();
        p.setName("prop-short");
        p.setValue(Short.toString(Short.MIN_VALUE));
        p.setFieldType(FieldType.SHORT);
        props.add(p);
        
        p = new Property();
        p.setName("prop-string");
        p.setValue("helloworld");
        p.setFieldType(FieldType.STRING);
        props.add(p);
        
        p = new Property();
        p.setName("dupe-string");
        p.setValue("whatup");
        p.setFieldType(FieldType.STRING);
        props.add(p);
        
        return props;
    }
    
    protected AtlasMapping generateAtlasMapping() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();
        mapping.getProperties().getProperty().addAll(generateAtlasProperties());
        return mapping;
    }

    protected Map<String, Object> generateRuntimeProperties() {
        Map<String, Object> runtimeProps = new HashMap<String, Object>();
        runtimeProps.put("key-boolean", true);
        runtimeProps.put("key-byte", new String("b").getBytes()[0]);
        runtimeProps.put("key-char", new String("a").charAt(0));
        runtimeProps.put("key-double", Double.MAX_VALUE);
        runtimeProps.put("key-float", Float.MAX_VALUE);
        runtimeProps.put("key-int", Integer.MAX_VALUE);
        runtimeProps.put("key-long", Long.MAX_VALUE);
        runtimeProps.put("key-short", Short.MAX_VALUE);
        runtimeProps.put("key-string", "foobar");
        runtimeProps.put("dupe-string", "uh oh");
        return runtimeProps;
    }
    
    @Test
    public void testProcessPropertyFieldEnvironment() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("PATH");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertTrue(((String)propField.getValue()).contains(File.pathSeparator));
    }
    
    @Test
    public void testProcessPropertyFieldNotFound() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("XXXXXXXXXXXXXXXXXXXXX");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNull(propField.getValue());
    }
        
    @Test
    public void testProcessPropertyFieldJavaSystem() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("java.specification.version");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("1.8", (String)propField.getValue());
    }
    
    @Test
    public void testProcessPropertyFieldDuplicateDefaultOrdering() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("dupe-string");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("uh oh", (String)propField.getValue());
    }
    
    @Test
    public void testProcessPropertyFieldDuplicateCustomOrdering() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("dupe-string");
        
        propStrategy.setPropertyOrderValue(Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES.value(), AtlasPropertyType.MAPPING_DEFINED_PROPERTIES.value()));
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("whatup", (String)propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldNull() throws Exception {
        propStrategy.processPropertyField(generateAtlasMapping(), null, generateRuntimeProperties());        
    }
    
    @Test
    public void testProcessPropertyFieldNullName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());        
    }
    
    @Test
    public void testProcessPropertyFieldEmptyName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("");
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());        
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefined() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer)propField.getValue()));
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefinedNullMapping() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        AtlasMapping mapping = generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.processPropertyField(null, propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNull(propField.getValue());
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefinedNullProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        AtlasMapping mapping = generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.processPropertyField(mapping, propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNull(propField.getValue());
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        AtlasMapping mapping = generateAtlasMapping();
        mapping.getProperties().getProperty().clear();
        propStrategy.processPropertyField(mapping, propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNull(propField.getValue());
    }
        
    @Test
    public void testProcessPropertyFieldMappingDefinedNullRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, null);
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer)propField.getValue()));
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, new HashMap<String, Object>());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer)propField.getValue()));
    }
    
    @Test
    public void testProcessPropertyFieldMappingDefinedNoConversionService() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");
        
        propStrategy.setAtlasConversionService(null);
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals(new Integer(Integer.MIN_VALUE).toString(), (String)propField.getValue());
    }
    
    @Test
    public void testProcessPropertyFieldRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("key-float");
        
        propStrategy.processPropertyField(generateAtlasMapping(), propField, generateRuntimeProperties());
        
        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Float);
        assertEquals(new Float(Float.MAX_VALUE), new Float((Float)propField.getValue()));
    }

    @Test
    public void testGetSetPropertyOrderValue() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 4);
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(2));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(3));
        
        propStrategy.setPropertyOrderValue(Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES.value(), AtlasPropertyType.MAPPING_DEFINED_PROPERTIES.value()));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 2);
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(1));        
    }
    
    @Test
    public void testGetSetPropertyOrderValueIllegalValue() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 4);
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(2));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(3));
        
        propStrategy.setPropertyOrderValue(Arrays.asList("foo", AtlasPropertyType.RUNTIME_PROPERTIES.value()));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 1);
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
    }

    @Test
    public void testGetSetEnvironmentPropertiesEnabled() {
        assertTrue(propStrategy.isEnvironmentPropertiesEnabled());
        propStrategy.setEnvironmentPropertiesEnabled(false);
        assertFalse(propStrategy.isEnvironmentPropertiesEnabled());      
    }

    @Test
    public void testGetSetSystemPropertiesEnabled() {
        assertTrue(propStrategy.isSystemPropertiesEnabled());
        propStrategy.setSystemPropertiesEnabled(false);
        assertFalse(propStrategy.isSystemPropertiesEnabled());     
    }

    @Test
    public void testGetSetMappingDefinedPropertiesEnabled() {
        assertTrue(propStrategy.isMappingDefinedPropertiesEnabled());
        propStrategy.setMappingDefinedPropertiesEnabled(false);
        assertFalse(propStrategy.isMappingDefinedPropertiesEnabled());    
    }

    @Test
    public void testGetSetRuntimePropertiesEnabled() {
        assertTrue(propStrategy.isRuntimePropertiesEnabled());
        propStrategy.setRuntimePropertiesEnabled(false);
        assertFalse(propStrategy.isRuntimePropertiesEnabled());
    }

    @Test
    public void testGetSetPropertyOrder() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 4);
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(2));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(3));
        
        propStrategy.setPropertyOrder(Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES, AtlasPropertyType.MAPPING_DEFINED_PROPERTIES));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 2);
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(1));        
    }


    @Test
    public void testGetSetAtlasConversionService() {
        propStrategy.setAtlasConversionService(DefaultAtlasConversionService.getRegistry());
        assertNotNull(propStrategy.getAtlasConversionService());
    }


}
