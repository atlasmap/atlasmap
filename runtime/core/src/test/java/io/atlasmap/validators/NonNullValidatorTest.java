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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;

public class NonNullValidatorTest extends BaseValidatorTest {

    @Override
    @Before
    public void setUp() {
        super.setUp();
        validator = new NonNullValidator(ValidationScope.MAPPING, "Cannot be null");
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupports() throws Exception {
        assertTrue(validator.supports(String.class));
        assertTrue(validator.supports(Integer.class));
        assertTrue(validator.supports(Double.class));
    }

    @Test
    public void testValidate() throws Exception {
        String notNull = "notNull";
        validator.validate(notNull, validations, null);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() throws Exception {
        validator.validate(null, validations, null);
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));

        Validation validation = validationHelper.getAllValidations().get(0);
        assertNotNull(validation);

        assertTrue("Cannot be null".equals(validation.getMessage()));
        assertEquals(ValidationScope.MAPPING, validation.getScope());
        assertNull(validation.getId());

        String empty = "";
        validationHelper.getAllValidations().clear();

        validator.validate(empty, validations, "testValidateInvalid-2");

        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertEquals(ValidationScope.MAPPING, validationHelper.getValidation().get(0).getScope());
        assertEquals("testValidateInvalid-2", validationHelper.getValidation().get(0).getId());
    }

}
