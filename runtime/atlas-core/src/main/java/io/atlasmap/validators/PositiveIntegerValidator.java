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

import java.util.List;

import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class PositiveIntegerValidator implements AtlasValidator {

    private String violationMessage;
    private ValidationScope scope;

    public PositiveIntegerValidator(ValidationScope scope, String violationMessage) {
        this.violationMessage = violationMessage;
        this.scope = scope;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Integer.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, List<Validation> validations, String id) {
        this.validate(target, validations, id, ValidationStatus.ERROR);
    }

    @Override
    public void validate(Object target, List<Validation> validations, String id, ValidationStatus status) {
        Integer value = (Integer) target;
        if (value == null || value < 0) {
            Validation validation = new Validation();
            validation.setScope(scope);
            validation.setId(id);
            validation.setMessage(String.format(violationMessage, target != null ? target.toString() : null));
            validation.setStatus(status);
            validations.add(validation);
        }
    }
}
