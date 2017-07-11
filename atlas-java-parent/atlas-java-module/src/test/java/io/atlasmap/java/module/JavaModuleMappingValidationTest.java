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

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.module.JavaMappingValidator;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaField;
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
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.BaseMappingTest;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@Ignore // TODO: Update validators for model refactor
public class JavaModuleMappingValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaModuleMappingValidationTest.class);
    protected io.atlasmap.java.v2.ObjectFactory javaModelFactory = null;
    protected AtlasMappingUtil mappingUtil = null;
    
    @Before
    public void setUp() {
        javaModelFactory = new io.atlasmap.java.v2.ObjectFactory();
        mappingUtil = new AtlasMappingUtil("io.atlasmap.v2:io.atlasmap.java.v2");
    }
    
    protected AtlasMapping getAtlasMappingFullValid() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");

        mapping.getDataSource().add(generateDataSource("atlas:java?2", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:java?3", DataSourceType.TARGET));

        Mapping mapMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        // MappedField
        JavaField inputField = AtlasJavaModelFactory.createJavaField();
        inputField.setFieldType(FieldType.STRING);
        inputField.setClassName("java.lang.String");
        inputField.setName("inputName");

        JavaField outputField = AtlasJavaModelFactory.createJavaField();
        outputField.setFieldType(FieldType.STRING);
        outputField.setClassName("java.lang.String");
        outputField.setName("outputName");

        mapMapping.getInputField().add(inputField);
        mapMapping.getOutputField().add(outputField);
        
        JavaField sIJavaField = AtlasJavaModelFactory.createJavaField();
        sIJavaField.setFieldType(FieldType.STRING);
        sIJavaField.setClassName("java.lang.String");
        sIJavaField.setName("inputName");
        sepMapping.getInputField().add(sIJavaField);

        JavaField sOJavaField = AtlasJavaModelFactory.createJavaField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setClassName("java.lang.String");
        sOJavaField.setName("outputName");
        sOJavaField.setIndex(0);
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
    
    protected void createMockMappedFields(AtlasMapping mapping, Mapping mapFieldMapping) {
        // Mock MappedField
        MockField inputField = new MockField();
        inputField.setName("input.name");
        MockField outputField = new MockField();
        outputField.setName("out.name");

        mapFieldMapping.getInputField().add(inputField);
        mapFieldMapping.getOutputField().add(outputField);
        mapping.getMappings().getMapping().add(mapFieldMapping);
    }
    
    protected void debugErrors(Validations validations) {
        for (Validation validation : validations.getValidation()) {
            logger.debug(DefaultAtlasValidationsHelper.validationToString(validation));
        }
    }
    
    @Test
    public void validateAtlasMappingFile_HappyPath() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_HappyPath2() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        Validations validations = new DefaultAtlasValidationsHelper();
        JavaMappingValidator validator = new JavaMappingValidator(mapping, validations);
        validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_LoadFromFile() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);
        Validations validations = new DefaultAtlasValidationsHelper();
        JavaMappingValidator validator = new JavaMappingValidator(mapping, validations);
        validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_LoadWithRegistry() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);
        Validations validations = new DefaultAtlasValidationsHelper();
        JavaMappingValidator validator = new JavaMappingValidator(mapping, validations, DefaultAtlasConversionService.getRegistry());
        validator.validateAtlasMappingFile();
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_InvalidValidatorType() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");

        mapping.getDataSource().add(generateDataSource("atlas:java::2", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:java::3", DataSourceType.TARGET));

        Mapping mapMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        // Mock MappedField
        createMockMappedFields(mapping, mapMapping);

        MockField mockInField = new MockField();
        MockField mockOutField = new MockField();
        mockOutField.setIndex(0);

        sepMapping.getInputField().add(mockInField);
        sepMapping.getOutputField().add(mockOutField);

        mapping.getMappings().getMapping().add(mapMapping);
        mapping.getMappings().getMapping().add(sepMapping);

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());

        mappingUtil.marshallMapping(mapping, "src/test/resources/mappings/MisMatchedModuleTypes.xml");
    }

    @Test
    public void validateAtlasMappingFile_InvalidModuleType() throws Exception {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");
        mapping.getDataSource().add(generateDataSource("atlas:xml::2", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:xml::3", DataSourceType.TARGET));

        Mapping mapFieldMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        createMockMappedFields(mapping, mapFieldMapping);

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_InvalidTypeInSeparateField() throws Exception {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();

        atlasMapping.setName("thisis a_valid.name");
        atlasMapping.getDataSource().add(generateDataSource("atlas:java::2", DataSourceType.SOURCE));
        atlasMapping.getDataSource().add(generateDataSource("atlas:java::3", DataSourceType.TARGET));

        Mapping separateFieldMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        JavaField bIJavaField = javaModelFactory.createJavaField();
        bIJavaField.setFieldType(FieldType.BOOLEAN);
        bIJavaField.setClassName("java.lang.Boolean");
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setName("inputName");

        separateFieldMapping.getInputField().add(bIJavaField);
        
        JavaField sOJavaField = javaModelFactory.createJavaField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setClassName("java.lang.String");
        sOJavaField.setName("outputName");
        sOJavaField.setIndex(0);
        separateFieldMapping.getOutputField().add(sOJavaField);

        atlasMapping.getMappings().getMapping().add(separateFieldMapping);

        
        JavaMappingValidator validator = new JavaMappingValidator(atlasMapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());

        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));

        Validation validation = validations.getValidation().get(0);
        assertNotNull(validation);
        assertThat("Input.Field", is(validation.getField()));
        assertThat("BOOLEAN", is(validation.getValue().toString()));
        assertThat("Input field must be of type STRING for a Separate Mapping", is(validation.getMessage()));
        assertThat(ValidationStatus.ERROR, is(validation.getStatus()));
    }

    @Test
    public void validateAtlasMappingFile_MisMatchedSourceToTarget() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.CHAR);

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertTrue(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_SourceToTargetNoConversion() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.COMPLEX);
        in.setClassName("io.atlasmap.java.module.MockJavaClass");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.STRING);
        out.setClassName("java.lang.String");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
        assertThat(1, is(((AtlasValidationHelper)validations).getCount()));
        assertTrue(validations.getValidation().stream()
                .anyMatch(me -> me.getField().equals("Field.Input/Output.conversion")));

        Long errorCount = validations.getValidation().stream()
                .filter(atlasMappingError -> atlasMappingError.getStatus().compareTo(ValidationStatus.ERROR) == 0).count();
        assertNotNull(errorCount);
        assertEquals(1L, errorCount.longValue());
    }


    @Test
    public void validateAtlasMappingFile_SourceToTargetCustomUsingClassNames() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DATE);
        in.setClassName("java.util.Date");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.COMPLEX);
        out.setClassName("java.time.ZonedDateTime");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertTrue(((AtlasValidationHelper)validations).hasInfos());
        assertThat(1, is(((AtlasValidationHelper)validations).getCount()));
        assertTrue(validations.getValidation().stream()
                .anyMatch(me -> me.getField().equals("Field.Input/Output.conversion")));
        Long errorCount = validations.getValidation().stream()
                .filter(atlasMappingError -> atlasMappingError.getStatus().compareTo(ValidationStatus.INFO) == 0).count();
        assertNotNull(errorCount);
        assertEquals(1L, errorCount.longValue());
    }

    @Test
    public void validateAtlasMappingFile_SourceToTargetRangeConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DOUBLE);
        in.setClassName("java.lang.Double");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
        assertThat(1, is(((AtlasValidationHelper)validations).getCount()));

        assertTrue(validations.getValidation().stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("range")));
    }

    @Test
    public void validateAtlasMappingFile_SourceToTargetFormatConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);
        in.setClassName("java.lang.String");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);
        out.setClassName("java.lang.Long");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
        assertThat(2, is(((AtlasValidationHelper)validations).getCount()));

        assertTrue(validations.getValidation().stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("range")));
        assertTrue(validations.getValidation().stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("format")));
    }

    @Test
    public void validateAtlasMappingFile_SourceToTargetUnsupported() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);
        in.setClassName("java.lang.String");

        JavaField out = (JavaField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.BYTE);
        out.setClassName("java.lang.Byte");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        if (logger.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));

        assertTrue(validations.getValidation().stream()
                .anyMatch(atlasMappingError -> atlasMappingError.getMessage().contains("not supported")));
    }

    @Test
    public void validateAtlasMappingFile_NoClassOnClassPath() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setClassName("java.lang.String3");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }


    @Test
    public void validateAtlasMappingFile_WrongModule() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        mapping.getDataSource().add(generateDataSource("atlas:xml:qwerty", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:json:qwerty", DataSourceType.TARGET));
        
        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_PathAndNameNull() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setName(null);

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_PathNotNull() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.xml");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JavaField in = (JavaField) fieldMapping.getInputField().get(0);
        in.setName(null);
        in.setPath("path");

        JavaMappingValidator validator = new JavaMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    public static <T> Collector<T, ?, T> singletonCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }
}
