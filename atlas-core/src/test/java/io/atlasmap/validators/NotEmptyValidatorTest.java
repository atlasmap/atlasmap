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
import io.atlasmap.validators.NotEmptyValidator;
import io.atlasmap.validators.AtlasValidationHelper;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class NotEmptyValidatorTest {

    private NotEmptyValidator validator;
    private Validations validations;

    @Before
    public void setUp() {
        validator = new NotEmptyValidator("test.field", "Collection should not be empty");
        validations = new DefaultAtlasValidationsHelper();
    }
    
    @After
    public void tearDown() {
        validator = null;
        validations = null;
    }
    
    @Test
    public void supports() throws Exception {
        assertTrue(validator.supports(Map.class));
        assertTrue(validator.supports(List.class));
        assertTrue(validator.supports(Set.class));
        assertTrue(validator.supports(Collection.class));
    }

    @Test
    public void not_supports() throws Exception {
        assertFalse(validator.supports(HashMap.class));
    }

    @Test
    public void validate() throws Exception {
        List<String> stuff = new ArrayList<>();
        stuff.add("one");
        stuff.add("two");

        validator.validate(stuff, validations);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());

        validator.validate(stuff, validations, ValidationStatus.WARN);
        assertFalse(((AtlasValidationHelper)validations).hasErrors());
    }

    @Test
    public void invalid_validate() throws Exception {
        List<String> stuff = new ArrayList<>();
        validator.validate(stuff, validations);
        assertTrue(((AtlasValidationHelper)validations).hasErrors());
        assertThat(((AtlasValidationHelper)validations).getCount(), is(1));
        assertFalse(((AtlasValidationHelper)validations).hasWarnings());
        assertFalse(((AtlasValidationHelper)validations).hasInfos());
    }

}