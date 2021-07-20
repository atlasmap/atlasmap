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
package io.atlasmap.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.spi.AtlasPropertyType;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.PropertyField;

public class DefaultAtlasPropertyStrategyTest {

    private DefaultAtlasPropertyStrategy propStrategy = null;

    @BeforeEach
    public void setUp() {
        propStrategy = new DefaultAtlasPropertyStrategy();
        propStrategy.setAtlasConversionService(DefaultAtlasConversionService.getInstance());
    }

    @AfterEach
    public void tearDown() {
        propStrategy = null;
    }

    @Test
    public void testProcessPropertyFieldEnvironment() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("PATH");

        propStrategy.readProperty(null, propField);

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
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldNotFound() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("XXXXXXXXXXXXXXXXXXXXX");

        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldJavaSystem() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("java.specification.version");

        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertTrue(Double.parseDouble((String)propField.getValue()) >= Double.parseDouble("1.8"),
                (String)propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldJavaSystemDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("java.specification.version");

        propStrategy.setSystemPropertiesEnabled(false);
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldDuplicateDefaultOrdering() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("dupe-string");

        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

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
        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals("whatup", propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldNull() throws Exception {
        propStrategy.readProperty(null, null);
    }

    @Test
    public void testProcessPropertyFieldNullName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propStrategy.readProperty(null, propField);
    }

    @Test
    public void testProcessPropertyFieldEmptyName() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("");
        propStrategy.readProperty(null, propField);
    }

    @Test
    public void testProcessPropertyFieldMappingDefined() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.setMappingDefinedPropertiesEnabled(false);
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullMapping() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.setProperties(null);
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyProperties() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        AtlasMapping mapping = AtlasTestData.generateAtlasMapping();
        mapping.getProperties().getProperty().clear();
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNullRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedEmptyRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Integer);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf((Integer) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldMappingDefinedNoConversionService() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("prop-int");

        propStrategy.setAtlasConversionService(null);
        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof String);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE).toString(), propField.getValue());
    }

    @Test
    public void testProcessPropertyFieldRuntime() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("key-float");

        propStrategy.readProperty(AtlasTestData.generateAtlasSession(), propField);

        assertNotNull(propField);
        assertNotNull(propField.getValue());
        assertTrue(propField.getValue() instanceof Float);
        assertEquals(Float.valueOf(Float.MAX_VALUE), Float.valueOf((Float) propField.getValue()));
    }

    @Test
    public void testProcessPropertyFieldRuntimeDisabled() throws Exception {
        PropertyField propField = AtlasModelFactory.createPropertyField();
        propField.setName("key-float");

        propStrategy.setRuntimePropertiesEnabled(false);
        propStrategy.readProperty(null, propField);

        assertNotNull(propField);
        assertNull(propField.getValue());
    }

    @Test
    public void testGetSetPropertyOrderValue() {
        List<AtlasPropertyType> propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertEquals(Integer.valueOf(4), Integer.valueOf(propTypes.size()));
        assertEquals(AtlasPropertyType.RUNTIME_PROPERTIES, propTypes.get(0));
        assertEquals(AtlasPropertyType.JAVA_SYSTEM_PROPERTIES, propTypes.get(1));
        assertEquals(AtlasPropertyType.ENVIRONMENT_VARIABLES, propTypes.get(2));
        assertEquals(AtlasPropertyType.MAPPING_DEFINED_PROPERTIES, propTypes.get(3));

        propStrategy.setPropertyOrderValue(Arrays.asList(AtlasPropertyType.RUNTIME_PROPERTIES.value(),
                AtlasPropertyType.MAPPING_DEFINED_PROPERTIES.value()));
        propTypes = propStrategy.getPropertyOrder();
        assertNotNull(propTypes);
        assertEquals(Integer.valueOf(2), Integer.valueOf(propTypes.size()));
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
        assertEquals(Integer.valueOf(1), Integer.valueOf(propTypes.size()));
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
