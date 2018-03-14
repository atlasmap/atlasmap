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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import io.atlasmap.v2.ValidationScope;

public class StringPatternValidatorTest extends BaseValidatorTest {

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupported() {
        validator = new StringPatternValidator(ValidationScope.ALL, "Must match .*", ".*");
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() {
        validator = new StringPatternValidator(ValidationScope.DATA_SOURCE, "Must match [0-9_.]", "[0-9_.]");
        assertFalse(validator.supports(Double.class));
    }

    @Test
    public void testValidate() {
        validator = new StringPatternValidator(ValidationScope.MAPPING, "Must match [^A-Za-z0-9_.]", "[^A-Za-z0-9_.]");
        validator.validate("This. &* should result in an error", validations, "testValidate");
        assertTrue(validationHelper.hasErrors());
        validations.clear();
        assertFalse(validationHelper.hasErrors());
        validator.validate("This_isafineexample.whatever1223", validations, "testValidate-2");
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateUsingMatch() {
        validator = new StringPatternValidator(ValidationScope.LOOKUP_TABLE, "Must match [0-9]+", "[0-9]+", true);
        validator.validate("0333", validations, "testValidateUsingMatch");
        assertFalse(validationHelper.hasErrors());

        validator = new StringPatternValidator(ValidationScope.PROPERTY, "Must match [0-9]", "[0-9]", true);
        validator.validate("This_isafineexample.whatever", validations, "testValidateUsingMatch-2");
        assertTrue(validationHelper.hasErrors());
    }

}
