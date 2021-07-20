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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.ValidationScope;

public class CompositeValidatorTest {

    @Test
    public void testSupports() {
        NotEmptyValidator notEmptyValidator = new NotEmptyValidator(ValidationScope.ALL, "violationMessage");
        assertTrue(notEmptyValidator.supports(new HashSet<String>()));
        assertTrue(notEmptyValidator.supports(new ArrayList<String>()));
        assertTrue(notEmptyValidator.supports(new HashMap<String, String>()));
        assertFalse(notEmptyValidator.supports(new String[1]));
        assertTrue(notEmptyValidator.supports(new ArrayDeque<String>()));

        List<AtlasValidator> validators = new ArrayList<>();
        validators.add(notEmptyValidator);
        CompositeValidator compositeValidator = new CompositeValidator(validators);
        assertFalse(compositeValidator.supports(NotEmptyValidator.class));
        assertTrue(compositeValidator.supports(List.class));
    }

}
