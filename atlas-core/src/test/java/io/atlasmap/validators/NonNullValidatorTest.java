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
import io.atlasmap.validators.NonNullValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;

public class NonNullValidatorTest {

    private NonNullValidator validator;
    private Validations validations;

    @Before
    public void setUp() {
        validator = new NonNullValidator("qwerty", "Cannot be null");
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
        assertTrue(validator.supports(Integer.class));
        assertTrue(validator.supports(Double.class));
    }

    @Test
    public void validate() throws Exception {
        String notNull = "notNull";
        validator.validate(notNull, validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

    @Test
    public void validate_invalid() throws Exception {
        validator.validate(null, validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));

        Validation validation = ((AtlasValidationHelper)validations).getAllValidations().get(0);
        assertNotNull(validation);

        // TODO: Support rejected value assertNull(validation.getRejectedValue());
        assertTrue("Cannot be null".equals(validation.getMessage()));
        assertTrue("qwerty".equals(validation.getField()));

        String empty = "";
        ((AtlasValidationHelper)validations).getAllValidations().clear();

        validator.validate(empty, validations);

        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));
    }

}