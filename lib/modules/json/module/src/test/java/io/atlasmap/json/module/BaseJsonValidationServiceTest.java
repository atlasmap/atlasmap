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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.core.DefaultAtlasFieldActionService;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
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
import io.atlasmap.validators.AtlasValidationTestHelper;

public abstract class BaseJsonValidationServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidationServiceTest.class);

    protected AtlasMappingUtil mappingUtil = null;
    protected DefaultAtlasFieldActionService fieldActionService;
    protected JsonValidationService sourceValidationService = null;
    protected JsonValidationService targetValidationService = null;
    protected AtlasValidationTestHelper validationHelper = null;
    protected List<Validation> validations = null;
    protected AtlasModuleDetail moduleDetail = null;

    @Before
    public void setUp() {
        mappingUtil = new AtlasMappingUtil();
        moduleDetail = JsonModule.class.getAnnotation(AtlasModuleDetail.class);

        fieldActionService = DefaultAtlasFieldActionService.getInstance();
        fieldActionService.init();
        sourceValidationService = new JsonValidationService(DefaultAtlasConversionService.getInstance(), fieldActionService);
        sourceValidationService.setMode(AtlasModuleMode.SOURCE);
        targetValidationService = new JsonValidationService(DefaultAtlasConversionService.getInstance(), fieldActionService);
        targetValidationService.setMode(AtlasModuleMode.TARGET);
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

        mapping.getDataSource().add(generateDataSource("atlas:json:MockJson", DataSourceType.SOURCE));
        mapping.getDataSource().add(generateDataSource("atlas:json:MockJson", DataSourceType.TARGET));

        Mapping mapMapping = AtlasModelFactory.createMapping(MappingType.MAP);
        Mapping sepMapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        Mapping combineMapping = AtlasModelFactory.createMapping(MappingType.COMBINE);

        // MappedField
        JsonField inputField = AtlasJsonModelFactory.createJsonField();
        inputField.setFieldType(FieldType.STRING);
        inputField.setPath("firstName");

        JsonField outputField = AtlasJsonModelFactory.createJsonField();
        outputField.setFieldType(FieldType.STRING);
        outputField.setPath("firstName");

        mapMapping.getInputField().add(inputField);
        mapMapping.getOutputField().add(outputField);

        JsonField sIJavaField = AtlasJsonModelFactory.createJsonField();
        sIJavaField.setFieldType(FieldType.STRING);
        sIJavaField.setPath("displayName");
        sepMapping.getInputField().add(sIJavaField);

        JsonField sOJavaField = AtlasJsonModelFactory.createJsonField();
        sOJavaField.setFieldType(FieldType.STRING);
        sOJavaField.setPath("lastName");
        sOJavaField.setIndex(1);
        sepMapping.getOutputField().add(sOJavaField);

        JsonField cIJavaField = AtlasJsonModelFactory.createJsonField();
        cIJavaField.setFieldType(FieldType.STRING);
        cIJavaField.setPath("displayName");
        combineMapping.getInputField().add(cIJavaField);

        JsonField cOJavaField = AtlasJsonModelFactory.createJsonField();
        cOJavaField.setFieldType(FieldType.STRING);
        cOJavaField.setPath("lastName");
        cOJavaField.setIndex(1);
        combineMapping.getOutputField().add(cOJavaField);

        mapping.getMappings().getMapping().add(mapMapping);
        mapping.getMappings().getMapping().add(sepMapping);
        mapping.getMappings().getMapping().add(combineMapping);
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
            LOG.debug(AtlasValidationTestHelper.validationToString(validation));
        }
    }

}
