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
import io.atlasmap.validators.StringLengthValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringLengthValidatorTest extends BaseValidatorTest {

    private StringLengthValidator validator;

    @Override
	@Before
    public void setUp() {
        super.setUp();
        validator = new StringLengthValidator("qwerty", "Must be of this length", 1, 10);
    }
    
    @Override
	@After
    public void tearDown() {
        super.tearDown();
        validator = null;
    }
    
    @Test
    public void testSupported() throws Exception {
        assertTrue(validator.supports(String.class));
    }

    @Test
    public void testUnsupported() throws Exception {
        assertFalse(validator.supports(Integer.class));
    }

    @Test
    public void testValidate() throws Exception {
        String pass = "1112332";
        validator.validate(pass, validations);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() throws Exception {
        String pass = "";
        validator.validate(pass, validations);
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getAllValidations().size()));

        Validation validation = validations.get(0);
        assertNotNull(validation);
        assertTrue("".equals(validation.getValue()));
        assertTrue("Must be of this length".equals(validation.getMessage()));
        assertTrue("qwerty".equals(validation.getField()));
    }

}