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
package io.atlasmap.json.module;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class JsonValidationServiceTest extends BaseJsonValidationServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidationServiceTest.class);

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
    public void testValidateMappingInvalidCombineInputFieldType() {
        AtlasMapping atlasMapping = getAtlasMappingFullValid();

        Mapping combineFieldMapping = AtlasModelFactory.createMapping(MappingType.COMBINE);
        combineFieldMapping.setId("combine.firstName.lastName");

        JsonField bIJavaField = new JsonField();
        bIJavaField.setFieldType(FieldType.STRING);
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setPath("firstName");
        combineFieldMapping.getInputField().add(bIJavaField);

        JsonField sOJavaField = new JsonField();
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
        mapping.getDataSource().add(generateDataSource("atlas:xml", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:xml", DataSourceType.TARGET));

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingInvalidSeparateInputFieldType() {
        AtlasMapping atlasMapping = getAtlasMappingFullValid();

        Mapping separateFieldMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        separateFieldMapping.setId("separate.firstName.lastName");

        JsonField bIJavaField = new JsonField();
        bIJavaField.setFieldType(FieldType.BOOLEAN);
        bIJavaField.setValue(Boolean.TRUE);
        bIJavaField.setPath("firstName");

        separateFieldMapping.getInputField().add(bIJavaField);

        JsonField sOJavaField = new JsonField();
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

        JsonField in = (JsonField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.CHAR);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        if (LOG.isDebugEnabled()) {
            debugErrors(validations);
        }
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertTrue(validationHelper.hasInfos());
    }

    @Test
    public void testValidateMappingSourceToTargetRangeConcerns() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JsonField in = (JsonField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.DOUBLE);

        JsonField out = (JsonField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);

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

        JsonField in = (JsonField) fieldMapping.getInputField().get(0);
        in.setFieldType(FieldType.STRING);

        JsonField out = (JsonField) fieldMapping.getOutputField().get(0);
        out.setFieldType(FieldType.LONG);

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
    public void testValidateMappingPathNull() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/HappyPathMapping.json");
        assertNotNull(mapping);

        Mapping fieldMapping = (Mapping) mapping.getMappings().getMapping().get(0);

        JsonField in = (JsonField) fieldMapping.getInputField().get(0);
        in.setPath(null);

        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
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
