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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class PositiveIntegerValidatorTest extends BaseValidatorTest {

    private PositiveIntegerValidator validator;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        validator = new PositiveIntegerValidator(ValidationScope.MAPPING, "Integer must be >= 0");
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupported() {
        assertTrue(validator.supports(Integer.class));
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() {
        assertFalse(validator.supports(Boolean.class));
    }

    @Test
    public void testValidate() {
        validator.validate(0, validations, null);
        validator.validate(1222, validations, null);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() {
        validator.validate(-1, validations, "testValidateInvalid");
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertEquals("testValidateInvalid", validationHelper.getValidation().get(0).getId());
    }

    @Test
    public void testValidateInvalidWarn() {
        validator.validate(-1, validations, "testValidateInvalidWarn", ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertEquals("testValidateInvalidWarn", validationHelper.getValidation().get(0).getId());
    }

    @Test
    public void testValidateInvalidInfo() {
        validator.validate(-1, validations, "testValidateInvalidInfo", ValidationStatus.INFO);
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertTrue(validationHelper.hasInfos());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertEquals("testValidateInvalidInfo", validationHelper.getValidation().get(0).getId());
    }

    @Test
    public void testValidateWithErrorLevel() {
        validator.validate(0, validations, "testValidateWithErrorLevel", ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

}
