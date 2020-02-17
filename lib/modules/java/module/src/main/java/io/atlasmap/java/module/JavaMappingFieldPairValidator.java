/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.java.module;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.core.validate.MappingFieldPairValidator;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class JavaMappingFieldPairValidator extends MappingFieldPairValidator {

    private JavaValidationService service;
    private AtlasConversionService conversionService;
    
    JavaMappingFieldPairValidator(JavaValidationService service, AtlasConversionService conversion) {
        super(service);
        this.service = service;
        this.conversionService = conversion;
    }

    @Override
    protected void doValidateFieldTypes(List<Validation> validations, String mappingId,
            Field sourceField, Field targetField, FieldType sourceFieldType) {
        if ((sourceField instanceof JavaField && targetField instanceof JavaField) && ((sourceFieldType == null
                || targetField.getFieldType() == null)
                || (sourceFieldType == FieldType.COMPLEX || targetField.getFieldType() == FieldType.COMPLEX))) {
            // making an assumption that anything marked as COMPLEX would require the use of
            // the class name to find a validator.
            if (((JavaField)sourceField).getClassName() != null && ((JavaField)targetField).getClassName() != null) {
                validateClassConversion(mappingId, (JavaField) sourceField, (JavaField) targetField, validations);
                return;
            }
        }

        if (sourceField.getFieldType() != targetField.getFieldType()) {
            super.doValidateFieldTypes(validations, mappingId, sourceField, targetField, sourceFieldType);
        }
    }

    private void validateClassConversion(String mappingId, JavaField inputField, JavaField outField,
            List<Validation> validations) {
        // skip converter check for COMPLEX source field (possible for conditional mapping)
        if (inputField.getFieldType() == FieldType.COMPLEX) {
            return;
        }

        Optional<AtlasConverter<?>> atlasConverter = conversionService
                .findMatchingConverter(inputField.getClassName(), outField.getClassName());

        if (!atlasConverter.isPresent()) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setId(mappingId);
            validation
                    .setMessage(String.format("Conversion from '%s' to '%s' is required but no converter is available",
                            inputField.getClassName(), outField.getClassName()));
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        } else {
            AtlasConversionInfo conversionInfo;
            // find the method that does the conversion
            Method[] methods = atlasConverter.get().getClass().getMethods();
            conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                    .filter(atlasConversionInfo -> atlasConversionInfo != null)
                    .filter(atlasConversionInfo -> atlasConversionInfo.sourceType()
                            .compareTo(inputField.getFieldType()) == 0
                            && atlasConversionInfo.targetType().compareTo(outField.getFieldType()) == 0)
                    .findFirst().orElse(null);

            if (conversionInfo != null) {
                populateConversionConcerns(validations, mappingId,
                     conversionInfo, service.getModuleFieldName(inputField), service.getModuleFieldName(outField));
            }
        }
    }

}