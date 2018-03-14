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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.spi.AtlasPropertyType;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.PropertyField;

public class DefaultAtlasPropertyStrategyTest {

    private DefaultAtlasPropertyStrategy propStrategy = null;

    @Before
    public void setUp() {
        propStrategy = new DefaultAtlasPropertyStrategy();
        propStrategy.setAtlasConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        propStrategy = null;
    }

    @Test
    public void testProcessPropertyFieldEnvironment() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("PATH");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertTrue(((String) propField.getValue()).contains(File.pathSeparator));
    }

    @Test
    public void testProcessPropertyFieldEnvironmentDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("PATH");

        propStrategy.setEnvironmentPropertiesEnabled(false);
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldNotFound() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("XXXXXXXXXXXXXXXXXXXXX");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldJavaSystem() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("java.specification.version");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("1.8", propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldJavaSystemDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("java.specification.version");

        propStrategy.setSystemPropertiesEnabled(false);
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldDuplicateDefaultOrdering() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("dupe-string");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("uh oh", propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldDuplicateCustomOrdering() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("dupe-string");

        propStrategy.setPropertyOrderValue(Arrays.asList(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES.value(),
                AtlasPropertyType.RUNTIME_PROPERTIES.value()));
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("whatup", propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldNull() throws Exception {
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), null,
                AtlasTestData.generateRuntimeProperties());
    }

    @Test
    public void testProcessPropertyFieldNullName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());
    }

    @Test
    public void testProcessPropertyFieldEmptyName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("");
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());
    }

    @Test
    public void testProcessPropertyFieldMappingDefined() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.setMappingDefinedPropertiesEnabled(false);
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullMapping() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.processPropertyField(null, propField, AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.processPropertyField(mapping, propField, AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.getProperties().getProperty().clear();
        propStrategy.processPropertyField(mapping, propField, AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField, null);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                new HashMap<String, Object>());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(new Integer(Integer.MIN_VALUE), new Integer((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNoConversionService() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.setAtlasConversionService(null);
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals(new Integer(Integer.MIN_VALUE).toString(), propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("key-float");

        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Float);
        assertEquals(new Float(Float.MAX_VALUE), new Float((Float) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldRuntimeDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("key-float");

        propStrategy.setRuntimePropertiesEnabled(false);
        propStrategy.processPropertyField(AtlasTestData.generateAtlasMapping(), propField,
                AtlasTestData.generateRuntimeProperties());

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testGetSetPropertyOrderValue() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertEquals(new Integer(4), new Integer(propTypes.size()));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(2));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(3));

        propStrategy.setPropertyOrderValue(Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES.value(),
                AtlasPropertyType.MAPPING_DEFINED_PROPERTIES.value()));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertEquals(new Integer(2), new Integer(propTypes.size()));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(1));
    }

    @Test
    public void testGetSetPropertyOrderValueIllegalValue() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 4);
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(2));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(3));

        propStrategy.setPropertyOrderValue(Arrays.asList("foo", AtlasPropertyType.RUNTIME_PROPERTIES.value()));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertEquals(new Integer(1), new Integer(propTypes.size()));
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
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(2));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(3));

        propStrategy.setPropertyOrder(
                Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES, AtlasPropertyType.MAPPING_DEFINED_PROPERTIES));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertTrue(propTypes.size() == 2);
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(1));
    }

    @Test
    public void testGetSetAtlasConversionService() {
        propStrategy.setAtlasConversionService(DefaultAtlasConversionService.getInstance());
        assertNotNull(propStrategy.getAtlasConversionService());
    }

}
