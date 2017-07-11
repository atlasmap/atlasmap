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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotEmptyValidator implements AtlasValidator {

    private String violationMessage;
    private String field;

    public NotEmptyValidator(String field, String violationMessage) {
        this.violationMessage = violationMessage;
        this.field = field;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {

        return clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Map.class)
                || clazz.isAssignableFrom(Set.class) || clazz.isAssignableFrom(Collection.class);

    }

    @Override
    public void validate(Object target, Validations validations) {
        validate(target, validations, ValidationStatus.ERROR);
    }

    @Override
    public void validate(Object target, Validations validations, ValidationStatus status) {

        if (((Collection) target).isEmpty()) {
            Validation validation = new Validation();
            validation.setField(field);
            validation.setMessage(this.violationMessage);
            validation.setStatus(status);
            validation.setValue(target.toString());
            validations.getValidation().add(validation);
        }
    }
}
