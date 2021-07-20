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
package io.atlasmap.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.ValidationScope;

public class LookupTableNameValidatorTest extends BaseValidatorTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        validator = new LookupTableNameValidator("LookupTables contain duplicated LookupTable names.");
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupportsLookupTables() {
        LookupTables lookupTables = makeLookupTables();
        assertTrue(validator.supports(lookupTables.getClass()));
    }

    @Test
    public void testValidateDuplicatedNames() {
        LookupTables lookupTables = makeLookupTables();
        validator.validate(lookupTables, validations, null);
        assertTrue(validationHelper.hasErrors());
        assertEquals(ValidationScope.LOOKUP_TABLE, validationHelper.getValidation().get(0).getScope());
        assertNull(validationHelper.getValidation().get(0).getId());
        debugErrors(validationHelper);
    }

    @Test
    public void testValidateNoDuplicateNames() {
        LookupTables lookupTables = makeLookupTables();
        lookupTables.getLookupTable().remove(2);
        validator.validate(lookupTables, validations, null);
        assertFalse(validationHelper.hasErrors());
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
