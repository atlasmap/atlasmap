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

import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.v2.Validations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeValidator implements AtlasValidator {

    private List<AtlasValidator> validators;

    public CompositeValidator(List<AtlasValidator> validators) {
        this.validators = new ArrayList<>(validators);
    }

    public CompositeValidator(AtlasValidator... validators) {
        this.validators = new ArrayList<>();
        Collections.addAll(this.validators, validators);
    }

    @Override
    public boolean supports(Class clazz) {
        for (AtlasValidator validator : validators) {
            if (validator.supports(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate(Object target, Validations validations) {
        validate(target, validations, ValidationStatus.ERROR);
    }

    @Override
    public void validate(Object target, Validations validations, ValidationStatus status) {
        for (AtlasValidator validator : validators) {
            validator.validate(target, validations, status);
        }
    }
}
