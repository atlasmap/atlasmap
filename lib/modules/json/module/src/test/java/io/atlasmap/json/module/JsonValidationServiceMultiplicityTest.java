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
package io.atlasmap.json.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;

public class JsonValidationServiceMultiplicityTest extends BaseJsonValidationServiceTest {

    @BeforeEach
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
        assertFalse(validationHelper.hasErrors(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasWarnings(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasInfos(), validationHelper.allValidationsToString());
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
        assertFalse(validationHelper.hasErrors(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasWarnings(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasInfos(), validationHelper.allValidationsToString());
        assertEquals(0, validationHelper.getCount());
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
        assertFalse(validationHelper.hasErrors(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasWarnings(), validationHelper.allValidationsToString());
        assertFalse(validationHelper.hasInfos(), validationHelper.allValidationsToString());
        assertEquals(0, validationHelper.getCount());
    }

}
