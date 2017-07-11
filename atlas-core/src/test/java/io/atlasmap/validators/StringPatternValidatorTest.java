/**
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

import org.junit.Test;

import io.atlasmap.v2.Validations;
import io.atlasmap.validators.StringPatternValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;

public class StringPatternValidatorTest {

    private StringPatternValidator validator;
    private Validations validations;

    @Before
    public void setUp() {
        validations = new DefaultAtlasValidationsHelper();
    }
    
    @After
    public void tearDown() {
        validator = null;
        validations = null;
    }

    @Test
    public void supports() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match .*", ".*");
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void not_Supports() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [0-9_.]", "[0-9_.]");
        assertFalse(validator.supports(Double.class));
    }

    @Test
    public void validate() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [^A-Za-z0-9_.]", "[^A-Za-z0-9_.]");
        validator.validate("This. &* should result in an error", validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        validations.getValidation().clear();
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        validator.validate("This_isafineexample.whatever1223", validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

    @Test
    public void validate_UsingMatch() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [0-9]+", "[0-9]+", true);
        validator.validate("0333", validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());

        validator = new StringPatternValidator("qwerty", "Must match [0-9]", "[0-9]", true);
        validator.validate("This_isafineexample.whatever", validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
    }

}