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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;

public class StringLengthValidatorTest extends BaseValidatorTest {

    private StringLengthValidator validator;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        validator = new StringLengthValidator(ValidationScope.MAPPING, "Must be of this length", 1, 10);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupported() {
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() {
        assertFalse(validator.supports(Integer.class));
    }

    @Test
    public void testValidate() {
        String pass = "1112332";
        validator.validate(pass, validations, "testValidate");
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() {
        String pass = "";
        validator.validate(pass, validations, "testValidateInvalid");
        assertTrue(validationHelper.hasErrors());
        assertEquals(Integer.valueOf(1), Integer.valueOf(validationHelper.getAllValidations().size()));

        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertEquals("testValidateInvalid", validation.getId());
        assertTrue("Must be of this length".equals(validation.getMessage()));
    }

}
