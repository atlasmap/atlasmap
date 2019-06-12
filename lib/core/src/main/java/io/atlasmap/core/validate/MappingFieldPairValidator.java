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
package io.atlasmap.core.validate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class MappingFieldPairValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MappingFieldPairValidator.class);

    private BaseModuleValidationService<?> service;

    public MappingFieldPairValidator(BaseModuleValidationService<?> service) {
        this.service = service;
    }

    public void validateFieldTypes(List<Validation> validations, String mappingId, FieldGroup sourceFieldGroup, Field targetField) {
        FieldType actionOutputType = getActionOutputFieldType(validations, mappingId, sourceFieldGroup);
        for (Field sourceField : sourceFieldGroup.getField()) {
            if (!service.matchDocIdOrNull(sourceField.getDocId())) {
                return;
            }
            doValidateFieldTypes(validations, mappingId, sourceField, targetField,
                    actionOutputType != null ? actionOutputType : sourceField.getFieldType());
        }
    }

    public void validateFieldTypes(List<Validation> validations, String mappingId, List<Field> sourceFields, Field targetField) {
        for (Field sourceField : sourceFields) {
            if (!service.matchDocIdOrNull(sourceField.getDocId())) {
                return;
            }
            FieldType actionOutputType = getActionOutputFieldType(validations, mappingId, sourceField);
            doValidateFieldTypes(validations, mappingId, sourceField, targetField,
                    actionOutputType != null ? actionOutputType : sourceField.getFieldType());
        }
    }

    public void validateFieldTypes(List<Validation> validations, String mappingId, Field sourceField, List<Field> targetFields) {
        if (!service.matchDocIdOrNull(sourceField.getDocId())) {
            return;
        }
        FieldType actionOutputType = getActionOutputFieldType(validations, mappingId, sourceField);
        for (Field targetField : targetFields) {
            doValidateFieldTypes(validations, mappingId, sourceField, targetField,
                    actionOutputType != null ? actionOutputType : sourceField.getFieldType());
        }
    }

    public void validateFieldTypes(List<Validation> validations, String mappingId, Field sourceField, Field targetField) {
        FieldType actionOutputType = getActionOutputFieldType(validations, mappingId, sourceField);
        doValidateFieldTypes(validations, mappingId, sourceField, targetField,
                actionOutputType != null ? actionOutputType : sourceField.getFieldType());
    }

    protected  void doValidateFieldTypes(List<Validation> validations, String mappingId, Field sourceField, Field targetField, FieldType sourceFieldType) {
        if (sourceField == null && targetField == null || sourceField.getFieldType() == targetField.getFieldType()) {
            return;
        }
        FieldType targetFieldType = targetField.getFieldType();
        if (sourceFieldType == null || targetFieldType == null) {
            return;
        }
        if (sourceFieldType == FieldType.ANY || targetFieldType == FieldType.ANY) {
            return;
        }

        Optional<AtlasConverter<?>> atlasConverter = service.getConversionService().findMatchingConverter(sourceFieldType, targetFieldType);
        if (!atlasConverter.isPresent()) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setId(mappingId);
            validation.setMessage(String.format(
                    "Conversion from '%s' to '%s' is required but no converter is available",
                    sourceField.getFieldType(), targetField.getFieldType()));
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        } else {
            AtlasConversionInfo conversionInfo;
            // find the method that does the conversion
            FieldType sft = sourceFieldType;
            Method[] methods = atlasConverter.get().getClass().getMethods();
            conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                    .filter(atlasConversionInfo -> atlasConversionInfo != null)
                    .filter(atlasConversionInfo -> (atlasConversionInfo.sourceType().compareTo(sft) == 0
                    && atlasConversionInfo.targetType().compareTo(targetFieldType) == 0))
                    .findFirst().orElse(null);
            if (conversionInfo != null) {
                populateConversionConcerns(validations, mappingId, conversionInfo, service.getFieldName(sourceField), service.getFieldName(targetField));
            }
        }
    }

    private FieldType getActionOutputFieldType(List<Validation> validations, String mappingId, Field f) {
        if (f.getActions() == null || f.getActions().size() == 0) {
            return null;
        }

        Action lastAction = f.getActions().get(f.getActions().size()-1);
        ActionDetail detail = null;
        try {
            detail = service.getFieldActionService().findActionDetail(lastAction, f.getFieldType());
        } catch (AtlasException e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("ActionDetail not found", e);
            }
        }
        if (detail == null) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setId(mappingId);
            validation.setMessage(String.format(
                    "Couldn't find a metadata for transformation '%s'", lastAction.getDisplayName()));
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
            return null;
        }
        return detail.getTargetType();
    }

    public void populateConversionConcerns(List<Validation> validations, String mappingId, AtlasConversionInfo converterAnno,
            String sourceFieldName, String targetFieldName) {
        if (converterAnno == null || converterAnno.concerns() == null) {
            return;
        }

        for (AtlasConversionConcern atlasConversionConcern : converterAnno.concerns()) {
            String message = atlasConversionConcern.getMessage(converterAnno);
            if (AtlasConversionConcern.NONE.equals(atlasConversionConcern)) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(message);
                validation.setStatus(ValidationStatus.INFO);
                validations.add(validation);
            } else  if (atlasConversionConcern.equals(AtlasConversionConcern.RANGE)
                    || atlasConversionConcern.equals(AtlasConversionConcern.FORMAT)
                    || atlasConversionConcern.equals(AtlasConversionConcern.FRACTIONAL_PART)
                    || atlasConversionConcern.equals(AtlasConversionConcern.TIMEZONE)) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(message);
                validation.setStatus(ValidationStatus.WARN);
                validations.add(validation);
            } else if (atlasConversionConcern.equals(AtlasConversionConcern.UNSUPPORTED)) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(message);
                validation.setStatus(ValidationStatus.ERROR);
                validations.add(validation);
            }
        }
    }

}
