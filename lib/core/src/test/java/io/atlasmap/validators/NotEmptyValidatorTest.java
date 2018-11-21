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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class NotEmptyValidatorTest extends BaseValidatorTest {

    private NotEmptyValidator validator;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        validator = new NotEmptyValidator(ValidationScope.MAPPING, "Collection should not be empty");
    }

    @Override
    @After
    public void tearDown() {
        super.setUp();
        validator = null;
    }

    @Test
    public void testSupported() {
        assertTrue(validator.supports(Map.class));
        assertTrue(validator.supports(List.class));
        assertTrue(validator.supports(Set.class));
        assertTrue(validator.supports(Collection.class));
    }

    @Test
    public void testUnsupported() {
        assertFalse(validator.supports(HashMap.class));
    }

    @Test
    public void testValidate() {
        List<String> stuff = new ArrayList<>();
        stuff.add("one");
        stuff.add("two");

        validator.validate(stuff, validations, "testValidate-1");
        assertFalse(validationHelper.hasErrors());

        validator.validate(stuff, validations, "testValidate-2", ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() {
        List<String> stuff = new ArrayList<>();
        validator.validate(stuff, validations, "testValidateInvalid");
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertEquals(ValidationScope.MAPPING, validationHelper.getValidation().get(0).getScope());
        assertEquals("testValidateInvalid", validationHelper.getValidation().get(0).getId());
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

}
