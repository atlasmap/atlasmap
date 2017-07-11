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

import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.PositiveIntegerValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;

public class PositiveIntegerValidatorTest {

    private PositiveIntegerValidator validator;
    private Validations validations;

    @Before
    public void setUp() {
        validator = new PositiveIntegerValidator("test.integer", "Integer must be >= 0");
        validations = new DefaultAtlasValidationsHelper();
    }
    
    @After
    public void tearDown() {
        validator = null;
        validations = null;
    }

    @Test
    public void supports() throws Exception {
        assertTrue(validator.supports(Integer.class));
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void doesnot_supports() throws Exception {
        assertFalse(validator.supports(Boolean.class));
    }

    @Test
    public void validate() throws Exception {
        validator.validate(0, validations);
        validator.validate(1222, validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

    @Test
    public void validate_Invalid() throws Exception {
        validator.validate(-1, validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));
    }

    @Test
    public void validate_Invalid_Warn() throws Exception {
        validator.validate(-1, validations, ValidationStatus.WARN);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertTrue(((AtlasValidationHelper)validations).hasWarnings());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));
    }

    @Test
    public void validate_Invalid_Info() throws Exception {
        validator.validate(-1, validations, ValidationStatus.INFO);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertTrue(((AtlasValidationHelper)validations).hasInfos());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));
    }

    @Test
    public void validateWithErrorLevel() throws Exception {
        validator.validate(0, validations, ValidationStatus.WARN);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

}