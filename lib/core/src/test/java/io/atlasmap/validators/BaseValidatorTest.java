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
 */
package io.atlasmap.validators;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.MockField;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

public abstract class BaseValidatorTest {

    protected static Logger logger = LoggerFactory.getLogger(BaseValidatorTest.class);

    // protected io.atlasmap.java.v2.ObjectFactory javaModelFactory = new
    // io.atlasmap.java.v2.ObjectFactory();
    protected AtlasMappingUtil mappingUtil = new AtlasMappingUtil("io.atlasmap.v2");
    protected AtlasValidationTestHelper validationHelper = null;
    protected List<Validation> validations = null;
    protected AtlasValidator validator = null;

    @Before
    public void setUp() {
        validationHelper = new AtlasValidationTestHelper();
        validations = validationHelper.getValidation();
    }

    @After
    public void tearDown() {
        validationHelper = null;
        validations = null;
    }

    protected AtlasMapping getAtlasMappingFullValid() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();

        mapping.setName("thisis_a_valid.name");

        DataSource src = new DataSource();
        src.setDataSourceType(DataSourceType.SOURCE);
        src.setUri("atlas:java?2");

        DataSource tgt = new DataSource();
        tgt.setDataSourceType(DataSourceType.TARGET);
        tgt.setUri("atlas:java?3");

        mapping.getDataSource().add(src);
        mapping.getDataSource().add(tgt);

        Mapping mapFieldMapping = AtlasModelFactory.createMapping(MappingType.MAP);

        MockField inputField = AtlasModelFactory.createMockField();
        inputField.setFieldType(FieldType.STRING);
        inputField.setCustom("java.lang.String");
        inputField.setName("inputName");
        mapFieldMapping.getInputField().add(inputField);

        MockField outputField = AtlasModelFactory.createMockField();
        outputField.setFieldType(FieldType.STRING);
        outputField.setCustom("java.lang.String");
        outputField.setName("outputName");
        mapFieldMapping.getOutputField().add(outputField);

        Mapping separateMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        MockField sIJavaField = AtlasModelFactory.createMockField();
        sIJavaField.setFieldType(FieldType.STRING);
        sIJavaField.setCustom("java.lang.String");
        sIJavaField.setName("inputName");
        separateMapping.getInputField().add(sIJavaField);

        MockField sOJavaField = AtlasModelFactory.createMockField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setCustom("java.lang.String");
        sOJavaField.setName("outputName");
        sOJavaField.setIndex(0);
        separateMapping.getOutputField().add(sOJavaField);

        mapping.getMappings().getMapping().add(mapFieldMapping);
        mapping.getMappings().getMapping().add(separateMapping);
        return mapping;
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

    protected AtlasMapping getAtlasMappingWithLookupTables(String... names) {
        AtlasMapping mapping = this.getAtlasMappingFullValid();
        LookupTables lookupTables = new LookupTables();
        mapping.setLookupTables(lookupTables);
        for (String name : names) {
            LookupTable lookupTable = new LookupTable();
            lookupTable.setName(name);
            lookupTable.setDescription("desc_".concat(name));
            lookupTables.getLookupTable().add(lookupTable);

            Mapping lookupFieldMapping = AtlasModelFactory.createMapping(MappingType.LOOKUP);
            lookupFieldMapping.setDescription("field_desc_".concat(name));
            lookupFieldMapping.setLookupTableName(name);

            Field inputField = createInputJavaField("inputName");
            Field outputField = createInputJavaField("outputName");

            lookupFieldMapping.getInputField().add(inputField);
            lookupFieldMapping.getOutputField().add(outputField);
            mapping.getMappings().getMapping().add(lookupFieldMapping);
        }

        return mapping;
    }

    protected Field createInputJavaField(String inputName) {
        MockField inputJavaField = AtlasModelFactory.createMockField();
        inputJavaField.setFieldType(FieldType.STRING);
        inputJavaField.setCustom("java.lang.String");
        inputJavaField.setName(inputName);
        return inputJavaField;
    }

    protected void debugErrors(Validations validations) {
        for (Validation validation : validations.getValidation()) {
            logger.debug(AtlasValidationTestHelper.validationToString(validation));
        }
    }
}
