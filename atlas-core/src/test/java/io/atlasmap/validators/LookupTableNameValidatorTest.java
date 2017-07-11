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

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.LookupTableNameValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;

/**
 */
public class LookupTableNameValidatorTest extends BaseMappingTest {
    private LookupTableNameValidator validator;
    private Validations validations;
    
    @Before
    public void setUp() throws Exception {
        validator = new LookupTableNameValidator("lookuptables.lookuptable.name", "LookupTables contain duplicated LookupTable names.");
        validations = new DefaultAtlasValidationsHelper();
    }
    
    @After
    public void tearDown() throws Exception {
        validator = null;
        validations = null;
    }

    @Test
    public void supports() throws Exception {
        LookupTables lookupTables = makeLookupTables();
        assertTrue(validator.supports(lookupTables.getClass()));
    }

    @Test
    public void validate_DuplicatedNames() throws Exception {
        LookupTables lookupTables = makeLookupTables();
        validator.validate(lookupTables, validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        debugErrors(validations);
    }

    @Test
    public void validate_NoDuplicateNames() throws Exception {
        LookupTables lookupTables = makeLookupTables();
        lookupTables.getLookupTable().remove(2);
        validator.validate(lookupTables, validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }


    private LookupTables makeLookupTables() {
        LookupTables lookupTables = new LookupTables();
        LookupTable lookupTable = new LookupTable();
        LookupTable lookupTable2 = new LookupTable();
        LookupTable lookupTableDup = new LookupTable();
        lookupTable.setName("qwerty");
        lookupTable2.setName("anotherName");
        lookupTableDup.setName("qwerty");
        lookupTables.getLookupTable().add(lookupTable);
        lookupTables.getLookupTable().add(lookupTable2);
        lookupTables.getLookupTable().add(lookupTableDup);
        return lookupTables;
    }

}