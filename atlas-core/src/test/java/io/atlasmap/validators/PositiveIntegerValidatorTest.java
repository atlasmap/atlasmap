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
import io.atlasmap.validators.AtlasValidationTestHelper;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;

public class PositiveIntegerValidatorTest extends BaseValidatorTest {

    private PositiveIntegerValidator validator;

    @Before
    public void setUp() {
        super.setUp();
        validator = new PositiveIntegerValidator("test.integer", "Integer must be >= 0");
    }
    
    @After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupported() throws Exception {
        assertTrue(validator.supports(Integer.class));
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() throws Exception {
        assertFalse(validator.supports(Boolean.class));
    }

    @Test
    public void testValidate() throws Exception {
        validator.validate(0, validations);
        validator.validate(1222, validations);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() throws Exception {
        validator.validate(-1, validations);
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
    }

    @Test
    public void testValidateInvalidWarn() throws Exception {
        validator.validate(-1, validations, ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
        assertTrue(validationHelper.hasWarnings());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
    }

    @Test
    public void testValidateInvalidInfo() throws Exception {
        validator.validate(-1, validations, ValidationStatus.INFO);
        assertFalse(validationHelper.hasErrors());
        assertFalse(validationHelper.hasWarnings());
        assertTrue(validationHelper.hasInfos());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
    }

    @Test
    public void testValidateWithErrorLevel() throws Exception {
        validator.validate(0, validations, ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
    }

}