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
package io.atlasmap.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.validators.BaseValidatorTest;

public class DefaultAtlasValidationServiceTest extends BaseValidatorTest {

    private DefaultAtlasValidationService validationService = null;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        validationService = new DefaultAtlasValidationService();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        validationService = null;
    }

    @Test
    public void testValidateAtlasMappingFileHappyPath() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);

        // validation
        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileHappyPath2() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);

        // validation
        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileInvalidName() throws Exception {
        AtlasMapping mapping = new AtlasMapping();
        mapping.setName("thisis in_valid.name");

        validations.addAll(validationService.validateMapping(mapping));
        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

    }

    @Test
    public void testValidateAtlasMappingFileLookupTablesDuplicateNames() throws Exception {
        AtlasMapping mapping = getAtlasMappingWithLookupTables("duplicate_name", "duplicate_name");
        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());

    }

    @Test
    public void testValidateAtlasMappingFileLookupFieldMappingRefNonExistentNames() throws Exception {
        AtlasMapping mapping = getAtlasMappingWithLookupTables("table1", "table2");

        // add one that does not exists
        Mapping lookupFieldMapping = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        lookupFieldMapping.setLookupTableName("table3");

        Field inputField = createInputJavaField("inputName");
        Field outputField = createInputJavaField("outputName");

        lookupFieldMapping.getInputField().add(inputField);
        lookupFieldMapping.getOutputField().add(outputField);

        mapping.getMappings().getMapping().add(lookupFieldMapping);

        validations.addAll(validationService.validateMapping(mapping));

        assertTrue(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileLookupFieldMappingUnusedLookupTable() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        LookupTables lookupTables = new LookupTables();
        mapping.setLookupTables(lookupTables);

        LookupTable lookupTable = new LookupTable();
        lookupTable.setName("table1");
        lookupTable.setDescription("desc_table1");

        LookupTable lookupTable2 = new LookupTable();
        lookupTable2.setName("table2");
        lookupTable2.setDescription("desc_table2");

        lookupTables.getLookupTable().add(lookupTable);
        lookupTables.getLookupTable().add(lookupTable2);

        Mapping lookupFieldMapping = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        lookupFieldMapping.setLookupTableName("table1");

        Field inputField = createInputJavaField("inputName");
        Field outputField = createInputJavaField("outputName");

        lookupFieldMapping.getInputField().add(inputField);
        lookupFieldMapping.getOutputField().add(outputField);

        mapping.getMappings().getMapping().add(lookupFieldMapping);

        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testValidateAtlasMappingFileNoLookupFieldMappingsWithTablesDefined() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        LookupTables lookupTables = new LookupTables();
        mapping.setLookupTables(lookupTables);

        LookupTable lookupTable = new LookupTable();
        lookupTable.setName("table1");
        lookupTable.setDescription("desc_table1");
        lookupTables.getLookupTable().add(lookupTable);

        validations.addAll(validationService.validateMapping(mapping));

        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

    @Test
    public void testAtlasMappingUtil() throws Exception {
        Files.createDirectories(Paths.get("target/mappings"));
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);
        AtlasMappingUtil atlasMappingUtil = new AtlasMappingUtil("io.atlasmap.v2");
        final String fileName = "target/mappings/HappyPathMapping.xml";
        atlasMappingUtil.marshallMapping(mapping, fileName);
        assertTrue(Files.exists(Paths.get(fileName)));
        AtlasMapping atlasMapping = atlasMappingUtil.loadMapping(fileName);
        assertNotNull(atlasMapping);
    }

}
