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

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.BaseMappingTest;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;
import io.atlasmap.core.AtlasMappingUtil;
import io.atlasmap.core.DefaultAtlasMappingValidator;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public class DefaultAtlasMappingValidationTest extends BaseMappingTest {

    @Test
    public void validateAtlasMappingFile_HappyPath() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);

        //validation
        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_HappyPath2() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        assertNotNull(mapping);

        //validation
        Validations validations = new DefaultAtlasValidationsHelper();
        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping, validations);
        validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_InvalidName() throws Exception {
        AtlasMapping mapping = new AtlasMapping();
        mapping.setName("thisis in_valid.name");

        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());

    }


    @Test
    public void validateAtlasMappingFile_LookupTablesDuplicateNames() throws Exception {
        AtlasMapping mapping = getAtlasMappingWithLookupTables("duplicate_name", "duplicate_name");
        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());

    }


    @Test
    public void validateAtlasMappingFile_LookupFieldMappingRefNonExistentNames() throws Exception {
        AtlasMapping mapping = getAtlasMappingWithLookupTables("table1", "table2");

        //add one that does not exists
        Mapping lookupFieldMapping = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        lookupFieldMapping.setLookupTableName("table3");

        Field inputField = createInputJavaField("inputName");
        Field outputField = createInputJavaField("outputName");

        lookupFieldMapping.getInputField().add(inputField);
        lookupFieldMapping.getOutputField().add(outputField);

        mapping.getMappings().getMapping().add(lookupFieldMapping);

        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_LookupFieldMappingUnusedLookupTable() throws Exception {
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

        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

    @Test
    public void validateAtlasMappingFile_NoLookupFieldMappingsWithTablesDefined() throws Exception {
        AtlasMapping mapping = getAtlasMappingFullValid();
        LookupTables lookupTables = new LookupTables();
        mapping.setLookupTables(lookupTables);

        LookupTable lookupTable = new LookupTable();
        lookupTable.setName("table1");
        lookupTable.setDescription("desc_table1");
        lookupTables.getLookupTable().add(lookupTable);


        DefaultAtlasMappingValidator validator = new DefaultAtlasMappingValidator(mapping);
        Validations validations = validator.validateAtlasMappingFile();

        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
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