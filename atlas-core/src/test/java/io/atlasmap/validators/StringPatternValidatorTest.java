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
import io.atlasmap.validators.StringPatternValidator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;

public class StringPatternValidatorTest extends BaseValidatorTest {

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }

    @Test
    public void testSupported() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match .*", ".*");
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [0-9_.]", "[0-9_.]");
        assertFalse(validator.supports(Double.class));
    }

    @Test
    public void testValidate() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [^A-Za-z0-9_.]", "[^A-Za-z0-9_.]");
        validator.validate("This. &* should result in an error", validations);
        assertTrue(validationHelper.hasErrors());
        validations.clear();
        assertFalse(validationHelper.hasErrors());
        validator.validate("This_isafineexample.whatever1223", validations);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateUsingMatch() throws Exception {
        validator = new StringPatternValidator("qwerty", "Must match [0-9]+", "[0-9]+", true);
        validator.validate("0333", validations);
        assertFalse(validationHelper.hasErrors());

        validator = new StringPatternValidator("qwerty", "Must match [0-9]", "[0-9]", true);
        validator.validate("This_isafineexample.whatever", validations);
        assertTrue(validationHelper.hasErrors());
    }

}