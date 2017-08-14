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

import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.module.JavaValidationService;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.MockField;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.AtlasValidationTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JavaValidationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaValidationServiceTest.class);
    protected io.atlasmap.java.v2.ObjectFactory javaModelFactory = null;
    protected AtlasMappingUtil mappingUtil = null;
    protected JavaValidationService validationService = null;
    protected AtlasValidationTestHelper validationHelper = null;
    protected List<Validation> validations = null;
    protected AtlasModuleDetail moduleDetail = null;

    @Before
    public void setUp() {
        javaModelFactory = new io.atlasmap.java.v2.ObjectFactory();
        mappingUtil = new AtlasMappingUtil("io.atlasmap.v2:io.atlasmap.java.v2");
        moduleDetail = JavaModule.class.getAnnotation(AtlasModuleDetail.class);

        validationService = new JavaValidationService(DefaultAtlasConversionService.getInstance());
        validationHelper = new AtlasValidationTestHelper();
        validations = validationHelper.getValidation();
    }

    @After
    public void tearDown() {
        javaModelFactory = null;
        mappingUtil = null;
        validationService = null;
        validationHelper = null;
        validations = null;
    }

    protected AtlasMapping getAtlasMappingFullValid() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");

        mapping.getDataSource().add(generateDataSource("atlas:java?className=io.atlasmap.java.module.MockJavaClass",
                DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:java?className=io.atlasmap.java.module.MockJavaClass",
                DataSourceType.TARGET));

        Mapping mapMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

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

        mapping.getMappings().getMapping().add(mapMapping);
        mapping.getMappings().getMapping().add(sepMapping);
        return mapping;
    }

    protected DataSource generateDataSource(String uri, DataSourceType type) {
        DataSource ds = new DataSource();
        ds.setUri(uri);
        ds.setDataSourceType(type);
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
            logger.debug(AtlasValidationTestHelper.validationToString(validation));
        }
    }

    // @Test
    // @Ignore // Note: manual utility to assist in creating files
    // public void saveSampleFile() throws Exception {
    // AtlasMappingUtil util = new
    // AtlasMappingUtil("io.atlasmap.v2:io.atlasmap.java.v2");
    //
    // AtlasMapping mapping = getAtlasMappingFullValid();
    // util.marshallMapping(mapping,
    // "src/test/resources/mappings/HappyPathMapping.xml");
    //
    // mapping = getAtlasMappingFullValid();
    // mapping.getMappings().getMapping().clear();
    // mapping.getMappings().getMapping().add(createMockMapping());
    // mappingUtil.marshallMapping(mapping,
    // "src/test/resources/mappings/MisMatchedFieldTypes.xml");
    //
    // }

    @Test
    public void testValidateMappingHappyPath() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        validations.addAll(validationService.validateMapping(mapping));
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingHappyPathFromFile() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingMismatchedFieldType() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        mapping.getMappings().getMapping().clear();

        // Mock MappedField
        mapping.getMappings().getMapping().add(createMockMapping());

        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingInvalidModuleType() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");
        mapping.getDataSource().add(generateDataSource("atlas:xml", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:xml", DataSourceType.TARGET));

        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingInvalidSeparateInputFieldType() throws Exception {
        AtlasMapping atlasMapping = getAtlasMappingFullValid();

        Mapping separateFieldMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        JavaField bIJavaField = javaModelFactory.createJavaField();
        bIJavaField.setFieldType(FieldType.BOOLEAN);
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setPath("firstName");

        separateFieldMapping.getInputField().add(bIJavaField);

        JavaField sOJavaField = javaModelFactory.createJavaField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setPath("lastName");
        sOJavaField.setIndex(0);
        separateFieldMapping.getOutputField().add(sOJavaField);

        atlasMapping.getMappings().getMapping().add(separateFieldMapping);

        validations.addAll(validationService.validateMapping(atlasMapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));

        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertThat("Input.Field", is(validation.getField()));
        assertThat("BOOLEAN", is(validation.getValue().toString()));
        assertThat("Input field must be of type STRING for a Separate Mapping", is(validation.getMessage()));
        assertThat(ValidationStatus.ERROR, is(validation.getStatus()));
    }

    @Test
    public void testValidateMappingSupportedSourceToTargetConversion() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.CHAR);

        validations.addAll(validationService.validateMapping(mapping));

        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertTrue(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileConversionRequired() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.COMPLEX);
        in.setClassName("io.atlasmap.java.module.MockJavaClass");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.STRING);

        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(1, is(validationHelper.getCount()));
        assertTrue(validations.stream().anyMatch(me -> me.getField().equals("Field.Input/Output.conversion")));

        Long errorCount = validations.stream()
                .filter(atlasMappingError -> atlasMappingError.getStatus().compareTo(ValidationStatus.WARN) == 0)
                .count();
        assertNotNull(errorCount);
        assertEquals(1L, errorCount.longValue());
    }

    @Test
    public void testValidateMappingSourceToTargetCustomUsingClassNames() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DATE);
        in.setClassName("java.util.Date");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.COMPLEX);
        out.setClassName("java.time.ZonedDateTime");

        validations.addAll(validationService.validateMapping(mapping));

        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertTrue(validationHelper.hasInfos());
        assertThat(1, is(validationHelper.getCount()));
        assertTrue(validations.stream().anyMatch(me -> me.getField().equals("Field.Input/Output.conversion")));
        Long errorCount = validations.stream()
                .filter(atlasMappingError -> atlasMappingError.getStatus().compareTo(ValidationStatus.INFO) == 0)
                .count();
        assertNotNull(errorCount);
        assertEquals(1L, errorCount.longValue());
    }

    @Test
    public void testValidateMappingSourceToTargetRangeConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DOUBLE);
        in.setClassName("java.lang.Double");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        validations.addAll(validationService.validateMapping(mapping));

        if (logger.isDebugEnabled()) {
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
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);
        in.setClassName("java.lang.String");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        validations.addAll(validationService.validateMapping(mapping));

        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(2, is(validationHelper.getCount()));

        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("range")));
        assertTrue(
                validations.stream().anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("format")));
    }

    @Test
    public void testValidateMappingSourceToTargetUnsupported() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);
        in.setClassName("java.lang.String");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.BYTE);
        out.setClassName("java.lang.Byte");

        validations.addAll(validationService.validateMapping(mapping));

        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
        assertThat(validationHelper.getCount(), is(1));

        assertTrue(validations.stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("not supported")));
    }

    @Test
    public void testValidateMappingClassNotFound() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setClassName("java.lang.String3");

        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

        boolean found = false;
        for (Validation v : validations) {
            if ("Field.Classname".equals(v.getField())) {
                assertEquals("Class for field is not found on the classpath", v.getMessage());
                assertEquals(ValidationStatus.ERROR, v.getStatus());
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testValidateMappingPathNull() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setPath(null);

        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testDetectJavaCompiledVersion() throws Exception {
        validationService.detectClassVersion("java.lang.String");
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
