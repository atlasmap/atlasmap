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
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.v2.Validations;

public class StringLengthValidator implements AtlasValidator {

    private String violationMessage;
    private int minLength = 1;
    private int maxLength = Integer.MAX_VALUE;
    private String field;

    public StringLengthValidator(String field, String violationMessage, int minLength, int maxLength) {
        this.field = field;
        this.violationMessage = violationMessage;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean supports(Class clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Validations validations) {
        validate(target, validations, ValidationStatus.ERROR);
    }

    @Override
    public void validate(Object target, Validations validations, ValidationStatus status) {
        String value = (String) target;
        if (value.isEmpty() || value.length() > maxLength || value.length() < minLength) {
            Validation validation = new Validation();
            validation.setField(field);
            validation.setValue(target.toString());
            validation.setMessage(violationMessage);
            validation.setStatus(status);
            validations.getValidation().add(validation);
        }
    }
}
