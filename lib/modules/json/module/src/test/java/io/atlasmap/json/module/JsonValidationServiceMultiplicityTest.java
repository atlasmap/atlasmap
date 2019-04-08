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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class JsonValidationServiceMultiplicityTest extends BaseJsonValidationServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidationServiceMultiplicityTest.class);

    @Before
    public void setUp() {
        super.setUp();
        sourceValidationService.setDocId("source");
        targetValidationService.setDocId("target");
    }

    @Test
    public void testHappyPath() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/atlasmapping-multiplicity-transformation-concatenate-split.json");
        assertNotNull(mapping);
        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasErrors());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasWarnings());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasInfos());
    }

    @Test
    public void testCollectionInFieldGroup() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/atlasmapping-multiplicity-transformation-concatenate-split.json");
        for (BaseMapping m : mapping.getMappings().getMapping()) {
            Mapping entry = (Mapping)m;
            if ("concatenate-from-field-group".equals(entry.getId())) {
                JsonField f = new JsonField();
                f.setCollectionType(CollectionType.LIST);
                f.setFieldType(FieldType.STRING);
                f.setPath("/sourceStringList<>");
                entry.getInputFieldGroup().getField().add(f);
                break;
            }
        }
        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));
        assertTrue(validationHelper.allValidationsToString(), validationHelper.hasErrors());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasWarnings());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasInfos());
        assertEquals(1, validationHelper.getCount());
        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertEquals("concatenate-from-field-group", validation.getId());
        assertEquals("A Source field contained in a collection can not be selected with other Source field: ['/sourceStringList<>']",
                validation.getMessage());
        assertEquals(ValidationStatus.ERROR, validation.getStatus());
    }

    @Test
    public void testCollectionInTargetFields() throws Exception {
        AtlasMapping mapping = mappingUtil.loadMapping("src/test/resources/mappings/atlasmapping-multiplicity-transformation-concatenate-split.json");
        for (BaseMapping m : mapping.getMappings().getMapping()) {
            Mapping entry = (Mapping)m;
            if ("split-into-multiple-fields".equals(entry.getId())) {
                JsonField f = new JsonField();
                f.setCollectionType(CollectionType.LIST);
                f.setFieldType(FieldType.STRING);
                f.setPath("/targetStringList<>");
                entry.getOutputField().add(f);
                break;
            }
        }
        validations.addAll(sourceValidationService.validateMapping(mapping));
        validations.addAll(targetValidationService.validateMapping(mapping));
        assertTrue(validationHelper.allValidationsToString(), validationHelper.hasErrors());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasWarnings());
        assertFalse(validationHelper.allValidationsToString(), validationHelper.hasInfos());
        assertEquals(1, validationHelper.getCount());
        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertEquals("split-into-multiple-fields", validation.getId());
        assertEquals("A Target field contained in a collection can not be selected with other Target field: ['/targetStringList<>']",
                validation.getMessage());
        assertEquals(ValidationStatus.ERROR, validation.getStatus());
    }

}