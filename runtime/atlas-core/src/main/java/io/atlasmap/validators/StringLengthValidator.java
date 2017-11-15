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

public class StringLengthValidator implements AtlasValidator {

    private String violationMessage;
    private int minLength = 1;
    private int maxLength = Integer.MAX_VALUE;
    private ValidationScope scope;

    public StringLengthValidator(ValidationScope scope, String violationMessage, int minLength, int maxLength) {
        this.scope = scope;
        this.violationMessage = violationMessage;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, List<Validation> validations, String id) {
        validate(target, validations, id, ValidationStatus.ERROR);
    }

    @Override
    public void validate(Object target, List<Validation> validations, String id, ValidationStatus status) {
        String value = (String) target;
        if (value.isEmpty() || value.length() > maxLength || value.length() < minLength) {
            Validation validation = new Validation();
            validation.setScope(scope);
            validation.setId(id);
            validation.setMessage(String.format(violationMessage, target.toString()));
            validation.setStatus(status);
            validations.add(validation);
        }
    }
}
