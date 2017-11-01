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
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPatternValidator implements AtlasValidator {

    private String violationMessage;
    private String pattern;
    private ValidationScope scope;
    private boolean useMatch;

    public StringPatternValidator(ValidationScope scope, String violationMessage, String pattern) {
        this(scope, violationMessage, pattern, false);
    }

    public StringPatternValidator(ValidationScope scope, String violationMessage, String pattern, boolean useMatch) {
        this.violationMessage = violationMessage;
        this.pattern = pattern;
        this.scope = scope;
        this.useMatch = useMatch;
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
        Pattern regEx = Pattern.compile(pattern);

        if (target != null && supports(target.getClass())) {
            String value = (String) target;
            Matcher m = regEx.matcher(value);
            if (useMatch) {
                if (!m.matches()) {
                    Validation validation = new Validation();
                    validation.setScope(scope);
                    validation.setId(id);
                    validation.setMessage(String.format(violationMessage, target.toString()));
                    validation.setStatus(status);
                    validations.add(validation);
                }
            } else {
                if (m.find()) {
                    Validation validation = new Validation();
                    validation.setScope(scope);
                    validation.setId(id);
                    validation.setMessage(String.format(violationMessage, target.toString()));
                    validation.setStatus(status);
                    validations.add(validation);
                }
            }
        }
    }
}
