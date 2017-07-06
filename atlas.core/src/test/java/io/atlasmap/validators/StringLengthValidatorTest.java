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

import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.StringLengthValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class StringLengthValidatorTest {

    private StringLengthValidator validator;
    private Validations validations;

    @Before
    public void setUp() {
        validator = new StringLengthValidator("qwerty", "Must be of this length", 1, 10);
        validations = new DefaultAtlasValidationsHelper();
    }
    
    @After
    public void tearDown() {
        validator = null;
        validations = null;
    }
    
    @Test
    public void supports() throws Exception {
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void doesnt_supports() throws Exception {
        assertFalse(validator.supports(Integer.class));
    }

    @Test
    public void validate() throws Exception {
        String pass = "1112332";
        validator.validate(pass, validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

    @Test
    public void validate_invalid() throws Exception {
        String pass = "";
        validator.validate(pass, validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertThat(((AtlasValidationHelper)validations).getAllValidations().size(), is(1));

        Validation validation = validations.getValidation().get(0);
        assertNotNull(validation);
        assertTrue("".equals(validation.getValue()));
        assertTrue("Must be of this length".equals(validation.getMessage()));
        assertTrue("qwerty".equals(validation.getField()));
    }

}