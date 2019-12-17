/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.atlasmap.java.module;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.core.DefaultAtlasFieldActionService;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.MockField;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.AtlasValidationTestHelper;

public class JavaValidationServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JavaValidationServiceTest.class);
    protected AtlasMappingUtil mappingUtil = null;
    protected DefaultAtlasFieldActionService fieldActionService;
    protected JavaValidationService sourceValidationService = null;
    protected JavaValidationService targetValidationService = null;
    protected AtlasValidationTestHelper validationHelper = null;
    protected List<Validation> validations = null;
    protected AtlasModuleDetail moduleDetail = null;

    @Before
    public void setUp() {
        mappingUtil = new AtlasMappingUtil();
        moduleDetail = JavaModule.class.getAnnotation(AtlasModuleDetail.class);

        fieldActionService = DefaultAtlasFieldActionService.getInstance();
        fieldActionService.init();
        sourceValidationService = new JavaValidationService(DefaultAtlasConversionService.getInstance(), fieldActionService);
        sourceValidationService.setMode(AtlasModuleMode.SOURCE);
        sourceValidationService.setDocId(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        targetValidationService = new JavaValidationService(DefaultAtlasConversionService.getInstance(), fieldActionService);
        targetValidationService.setMode(AtlasModuleMode.TARGET);
        targetValidationService.setDocId(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
        validationHelper = new AtlasValidationTestHelper();
        validations = validationHelper.getValidation();
    }

    @After
    public void tearDown() {
        mappingUtil = null;
        sourceValidationService = null;
        targetValidationService = null;
        validationHelper = null;
        validations = null;
    }

    protected AtlasMapping getAtlasMappingFullValid() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");

        mapping.getDataSource().add(generateDataSource("atlas:java?className=io.atlasmap.java.module.MockJavaClass",
                DataSourceType.SOURCE, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID));
        mapping.getDataSource().add(generateDataSource("atlas:java?className=io.atlasmap.java.module.MockJavaClass",
                DataSourceType.TARGET, AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID));

        Mapping mapMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        Mapping combineMapping = AtlasModelFactory.createMapping(MappingType.COMBINE);

        // MappedField
        JavaField inputField = AtlasJavaModelFactory.createJavaField();
        inputField.setFieldType(FieldType.STRING);
        inputField.setPath("firstName");

        JavaField outputField = AtlasJavaModelFactory.createJavaField();
        outputField.setFieldType(FieldType.STRING);
        outputField.setPath("firstName");

        mapMapping.getInputField().add(inputField);
        mapMapping.getOutputField().add(outputField);

        JavaField sIJavaField = AtlasJavaModelFactory.createJavaField();
        sIJavaField.setFieldType(FieldType.STRING);
        sIJavaField.setPath("displayName");
        sepMapping.getInputField().add(sIJavaField);

        JavaField sOJavaField = AtlasJavaModelFactory.createJavaField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setPath("lastName");
        sOJavaField.setIndex(1);
        sepMapping.getOutputField().add(sOJavaField);

        JavaField cIJavaField = AtlasJavaModelFactory.createJavaField();
        cIJavaField.setFieldType(FieldType.STRING);
        cIJavaField.setPath("displayName");
        combineMapping.getInputField().add(cIJavaField);

        JavaField cOJavaField = AtlasJavaModelFactory.createJavaField();
        cOJavaField.setFieldType(FieldType.STRING);
        cOJavaField.setPath("lastName");
        cOJavaField.setIndex(1);
        combineMapping.getOutputField().add(cOJavaField);

        mapping.getMappings().getMapping().add(mapMapping);
        mapping.getMappings().getMapping().add(sepMapping);
        mapping.getMappings().getMapping().add(combineMapping);
        return mapping;
    }

    protected DataSource generateDataSource(String uri, DataSourceType type, String docId) {
        DataSource ds = new DataSource();
        ds.setUri(uri);
        ds.setDataSourceType(type);
        ds.setId(docId);
        return ds;
    }

    protected Mapping createMockMapping() {
        // Mock MappedField
        MockField inputField = new MockField();
        inputField.setName("input.name");
        MockField outputField = new MockField();
        outputField.setName("out.name");

        Mapping mapping = new Mapping();
        mapping.setMappingType(MappingType.MAP);
        mapping.getInputField().add(inputField);
        mapping.getOutputField().add(inputField);
        return mapping;
    }

    protected void debugErrors(List<Validation> validations) {
        for (Validation validation : validations) {
            LOG.debug(AtlasValidationTestHelper.validationToString(validation));
        }
    }

    @Test
    public void testValidateMappingHappyPath() {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingHappyPathFromFile() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingMismatchedFieldType() {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        mapping.getMappings().getMapping().clear();

        // Mock MappedField
        mapping.getMappings().getMapping().add(createMockMapping());

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingInvalidCombineSourceFieldType() {
        AtlasMapping atlasMapping = getAtlasMappingFullValid();

        Mapping combineFieldMapping = AtlasModelFactory.createMapping(MappingType.COMBINE);
        combineFieldMapping.setId("combine.firstName.lastName");

        JavaField bIJavaField = new JavaField();
        bIJavaField.setFieldType(FieldType.STRING);
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setPath("firstName");
        combineFieldMapping.getInputField().add(bIJavaField);

        JavaField sOJavaField = new JavaField();
        sOJavaField.setFieldType(FieldType.BOOLEAN);
        sOJavaField.setPath("lastName");
        sOJavaField.setIndex(0);
        combineFieldMapping.getOutputField().add(sOJavaField);

        atlasMapping.getMappings().getMapping().add(combineFieldMapping);

        validations.addAll(sourceValidationService.validateMapping(atlasMapping));
        validations.addAll(targetValidationService.validateMapping(atlasMapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));

        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertEquals("combine.firstName.lastName", validation.getId());
        assertEquals("Target field 'lastName' must be of type 'STRING' for a Combine Mapping", validation.getMessage());
        assertEquals(ValidationStatus.ERROR, validation.getStatus());
    }

    @Test
    public void testValidateMappingInvalidModuleType() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");
        mapping.getDataSource()
                .add(generateDataSource("atlas:xml", DataSourceType.SOURCE, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID));
        mapping.getDataSource()
                .add(generateDataSource("atlas:xml", DataSourceType.TARGET, AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID));

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingInvalidSeparateSourceFieldType() {
        AtlasMapping atlasMapping = getAtlasMappingFullValid();

        Mapping separateFieldMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        separateFieldMapping.setId("separate.firstName.lastName");

        JavaField bIJavaField = new JavaField();
        bIJavaField.setFieldType(FieldType.BOOLEAN);
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setPath("firstName");

        separateFieldMapping.getInputField().add(bIJavaField);

        JavaField sOJavaField = new JavaField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setPath("lastName");
        sOJavaField.setIndex(0);
        separateFieldMapping.getOutputField().add(sOJavaField);

        atlasMapping.getMappings().getMapping().add(separateFieldMapping);

        validations.addAll(sourceValidationService.validateMapping(atlasMapping));
        validations.addAll(targetValidationService.validateMapping(atlasMapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));

        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertEquals("separate.firstName.lastName", validation.getId());
        assertEquals("Source field 'firstName' must be of type 'STRING' for a Separate Mapping",
                validation.getMessage());
        assertEquals(ValidationStatus.ERROR, validation.getStatus());
    }

    @Test
    public void testValidateMappingSupportedSourceToTargetConversion() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.CHAR);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileConversionRequired() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.COMPLEX);
        in.setClassName("io.atlasmap.java.module.MockJavaClass");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.STRING);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(1, is(validationHelper.getCount()));
        Validation v = validations.get(0);
        assertEquals(ValidationScope.MAPPING, v.getScope());
        assertEquals("map.firstName.firstName", v.getId());
        assertEquals(ValidationStatus.ERROR, v.getStatus());
    }

    @Test
    public void testValidateMappingSourceToTargetCustomUsingClassNames() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DATE_TIME_TZ);
        in.setClassName("java.util.Date");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.DATE_TIME_TZ);
        out.setClassName("java.time.ZonedDateTime");

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(validationHelper.getCount(), is(0));
    }

    @Test
    public void testValidateMappingSourceToTargetRangeConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DOUBLE);
        in.setClassName("java.lang.Double");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(1, is(validationHelper.getCount()));

        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("range")));
    }

    @Test
    public void testValidateMappingSourceToTargetFormatConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);
        in.setClassName("java.lang.String");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(3, is(validationHelper.getCount()));

        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("range")));
        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("format")));
        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("fractional part")));
    }

    @Test
    public void testValidateMappingSourceToTargetUnsupported() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DATE);
        in.setClassName("java.util.Date");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.BYTE);
        out.setClassName("java.lang.Byte");

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(validationHelper.getCount(), is(1));

        assertTrue(validations.stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("out of range")));
    }

    @Test
    public void testValidateMappingClassNotFound() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setClassName("java.lang.String3");

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

        assertEquals(1, validations.size());
        Validation v = validations.get(0);
        assertEquals(ValidationScope.MAPPING, v.getScope());
        assertEquals("map.firstName.firstName", v.getId());
        assertEquals(ValidationStatus.ERROR, v.getStatus());
    }

    @Test
    public void testValidateMappingPathNull() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setPath(null);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingTransformation() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMappingTransformation.json");
        assertNotNull(mapping);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testIssue127() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/Issue127Mapping.json");
        assertNotNull(mapping);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertEquals(3, validations.size());

        Validation v = validations.get(0);
        assertEquals(ValidationScope.MAPPING, v.getScope());
        assertEquals("issue127-1", v.getId());
        assertEquals("Conversion from 'STRING' to 'INTEGER' can cause numeric format exceptions", v.getMessage());
        assertEquals(ValidationStatus.WARN, v.getStatus());

        v = validations.get(1);
        assertEquals(ValidationScope.MAPPING, v.getScope());
        assertEquals("issue127-1", v.getId());
        assertEquals("Conversion from 'STRING' to 'INTEGER' can cause out of range exceptions", v.getMessage());
        assertEquals(ValidationStatus.WARN, v.getStatus());

        v = validations.get(2);
        assertEquals(ValidationScope.MAPPING, v.getScope());
        assertEquals("issue127-1", v.getId());
        assertEquals("Conversion from 'STRING' to 'INTEGER' can cause fractional part to be lost", v.getMessage());
        assertEquals(ValidationStatus.WARN, v.getStatus());
    }

    public static <T> Collector<T, ?, T> singletonCollector() {
        return Collectors.collectingAndThen(Collectors.toList(), list -> {
            if (list.size() != 1) {
                throw new IllegalStateException();
            }
            return list.get(0);
        });
    }
}
