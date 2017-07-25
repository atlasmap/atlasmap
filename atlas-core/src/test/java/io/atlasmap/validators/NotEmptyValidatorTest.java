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
import io.atlasmap.validators.NotEmptyValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

public class NotEmptyValidatorTest extends BaseValidatorTest {

    private NotEmptyValidator validator;

    @Before
    public void setUp() {
        super.setUp();
        validator = new NotEmptyValidator("test.field", "Collection should not be empty");
    }
    
    @After
    public void tearDown() {
        super.setUp();
        validator = null;
    }
    
    @Test
    public void testSupported() throws Exception {
        assertTrue(validator.supports(Map.class));
        assertTrue(validator.supports(List.class));
        assertTrue(validator.supports(Set.class));
        assertTrue(validator.supports(Collection.class));
    }

    @Test
    public void testUnsupported() throws Exception {
        assertFalse(validator.supports(HashMap.class));
    }

    @Test
    public void testValidate() throws Exception {
        List<String> stuff = new ArrayList<>();
        stuff.add("one");
        stuff.add("two");

        validator.validate(stuff, validations);
        assertFalse(validationHelper.hasErrors());

        validator.validate(stuff, validations, ValidationStatus.WARN);
        assertFalse(validationHelper.hasErrors());
    }

    @Test
    public void testValidateInvalid() throws Exception {
        List<String> stuff = new ArrayList<>();
        validator.validate(stuff, validations);
        assertTrue(validationHelper.hasErrors());
        assertEquals(new Integer(1), new Integer(validationHelper.getCount()));
        assertFalse(validationHelper.hasWarnings());
        assertFalse(validationHelper.hasInfos());
    }

}