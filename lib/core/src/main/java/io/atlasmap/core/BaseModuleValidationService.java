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
package io.atlasmap.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public abstract class BaseModuleValidationService<T extends Field> implements AtlasValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(BaseModuleValidationService.class);

    private AtlasConversionService conversionService;
    private AtlasFieldActionService fieldActionService;
    private AtlasModuleMode mode;
    private String docId;

    public BaseModuleValidationService() {
        this.conversionService = DefaultAtlasConversionService.getInstance();
        this.fieldActionService = new DefaultAtlasFieldActionService(this.conversionService);
    }

    public BaseModuleValidationService(AtlasConversionService conversionService, AtlasFieldActionService fieldActionService) {
        this.conversionService = conversionService;
        this.fieldActionService = fieldActionService;
    }

    public void setMode(AtlasModuleMode mode) {
        this.mode = mode;
    }

    public AtlasModuleMode getMode() {
        return mode;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocId() {
        return this.docId;
    }

    protected abstract AtlasModuleDetail getModuleDetail();

    @Override
    public List<Validation> validateMapping(AtlasMapping mapping) {
        List<Validation> validations = new ArrayList<>();
        if (getMode() == AtlasModuleMode.UNSET) {
            Validation validation = new Validation();
            validation.setMessage(String.format(
                    "No mode specified for %s/%s, skipping module validations",
                    this.getModuleDetail().name(), this.getClass().getSimpleName()));
        }

        if (mapping != null && mapping.getMappings() != null && mapping.getMappings().getMapping() != null
                && !mapping.getMappings().getMapping().isEmpty()) {
            validateMappingEntries(mapping.getMappings().getMapping(), validations);
        }

        boolean found = false;
        for (DataSource ds : mapping.getDataSource()) {
            if (ds.getUri() != null && ds.getUri().startsWith(getModuleDetail().uri())) {
                found = true;
                break;
            }
        }

        if (!found) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.DATA_SOURCE);
            validation.setMessage(String.format("No DataSource with '%s' uri specified", getModuleDetail().uri()));
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }

        return validations;
    }

    protected void validateCombineMapping(Mapping mapping, List<Validation> validations) {
        if (mapping == null) {
            return;
        }

        List<Field> sourceFields = mapping.getInputField();

        final List<Field> targetFields = mapping.getOutputField();
        final Field targetField = (targetFields != null && !targetFields.isEmpty()) ? targetFields.get(0) : null;
        if (targetField == null) {
            return;
        }

        String mappingId = mapping.getId();

        if (getMode() == AtlasModuleMode.TARGET && matchDocIdOrNull(targetField.getDocId())) {
            if (sourceFields != null) {
                // FIXME Run only for TARGET to avoid duplicate validation...
                // we should convert per module validations to plugin style
                for (Field sourceField : sourceFields) {
                    validateSourceAndTargetTypes(mappingId, sourceField, targetField, validations);
                }
            }

            // check that the output field is of type String else error
            if (targetField.getFieldType() != FieldType.STRING) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(String.format(
                        "Target field '%s' must be of type '%s' for a Combine Mapping",
                        getFieldName(targetField), FieldType.STRING));
                validation.setStatus(ValidationStatus.ERROR);
                validations.add(validation);
            }
            validateField(mappingId, targetField, FieldDirection.TARGET, validations);
        } else if (sourceFields != null) { // SOURCE
            for (Field sourceField : sourceFields) {
                if (matchDocIdOrNull(sourceField.getDocId())) {
                    validateField(mappingId, sourceField, FieldDirection.SOURCE, validations);
                }
            }
        }
    }

    protected void validateMappingEntries(List<BaseMapping> mappings, List<Validation> validations) {
        for (BaseMapping fieldMapping : mappings) {
            if (fieldMapping.getClass().isAssignableFrom(Mapping.class)
                    && MappingType.MAP.equals(((Mapping) fieldMapping).getMappingType())) {
                validateMapMapping((Mapping) fieldMapping, validations);
            } else if (fieldMapping.getClass().isAssignableFrom(Mapping.class)
                    && MappingType.SEPARATE.equals(((Mapping) fieldMapping).getMappingType())) {
                validateSeparateMapping((Mapping) fieldMapping, validations);
            } else if (fieldMapping.getClass().isAssignableFrom(Mapping.class)
                    && MappingType.COMBINE.equals(((Mapping) fieldMapping).getMappingType())) {
                validateCombineMapping((Mapping) fieldMapping, validations);
            }
        }
    }

    protected void validateMapMapping(Mapping mapping, List<Validation> validations) {
        Field sourceField = null;
        Field targetField = null;
        String mappingId = mapping.getId();

        if (mapping != null && mapping.getInputField() != null && mapping.getInputField().size() > 0) {
            sourceField = mapping.getInputField().get(0);
            if (getMode() == AtlasModuleMode.SOURCE && matchDocIdOrNull(sourceField.getDocId())) {
                validateField(mappingId, sourceField, FieldDirection.SOURCE, validations);
            }
        }

        if (mapping != null && mapping.getOutputField() != null && mapping.getOutputField().size() > 0) {
            targetField = mapping.getOutputField().get(0);
            if (getMode() == AtlasModuleMode.TARGET && matchDocIdOrNull(targetField.getDocId())) {
                validateField(mappingId, targetField, FieldDirection.TARGET, validations);
            }
        }

        if (sourceField != null && targetField != null && getMode() == AtlasModuleMode.SOURCE
                && matchDocIdOrNull(sourceField.getDocId())) {
            // FIXME Run only for SOURCE to avoid duplicate validation...
            // we should convert per module validations to plugin style
            validateSourceAndTargetTypes(mappingId, sourceField, targetField, validations);
        }
    }

    protected void validateSeparateMapping(Mapping mapping, List<Validation> validations) {
        if (mapping == null) {
            return;
        }

        final List<Field> sourceFields = mapping.getInputField();
        final Field sourceField = (sourceFields != null && !sourceFields.isEmpty()) ? sourceFields.get(0) : null;
        if (sourceField == null) {
            return;
        }
        List<Field> targetFields = mapping.getOutputField();
        String mappingId = mapping.getId();

        if (getMode() == AtlasModuleMode.SOURCE && matchDocIdOrNull(sourceField.getDocId())) {
            // check that the source field is of type String else error
            if (sourceField.getFieldType() != FieldType.STRING) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mapping.getId());
                validation.setMessage(String.format(
                        "Source field '%s' must be of type '%s' for a Separate Mapping",
                        getFieldName(sourceField), FieldType.STRING));
                validation.setStatus(ValidationStatus.ERROR);
                validations.add(validation);
            }
            validateField(mappingId, sourceField, FieldDirection.SOURCE, validations);

            if (targetFields != null) {
                // FIXME Run only for SOURCE to avoid duplicate validation...
                // we should convert per module validations to plugin style
                for (Field targetField : targetFields) {
                    validateSourceAndTargetTypes(mappingId, sourceField, targetField, validations);
                }
            }
        } else if (targetFields != null) { // TARGET
            for (Field targetField : targetFields) {
                if (matchDocIdOrNull(targetField.getDocId())) {
                    validateField(mappingId, targetField, FieldDirection.TARGET, validations);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void validateField(String mappingId, Field field, FieldDirection direction, List<Validation> validations) {
        if (field == null) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setId(mappingId);
            validation.setMessage(String.format("%s field %s is null",
                    direction.value(), getFieldName(field)));
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        } else if (getFieldType().isAssignableFrom(field.getClass())){
            validateModuleField(mappingId, (T)field, direction, validations);
        }
    }

    protected abstract Class<T> getFieldType();

    protected abstract void validateModuleField(String mappingId, T field, FieldDirection direction, List<Validation> validation);

    protected void validateSourceAndTargetTypes(String mappingId, Field sourceField, Field targetField, List<Validation> validations) {
        if (sourceField.getFieldType() != targetField.getFieldType()) {
            // is this check superseded by the further checks using the AtlasConversionInfo
            // annotations?

            // errors.getAllErrors().add(new AtlasMappingError("Field.Input/Output",
            // inputField.getType().value() + " --> " + outField.getType().value(), "Output
            // field type does not match input field type, may require a converter.",
            // AtlasMappingError.Level.WARN));
            validateFieldTypeConversion(mappingId, sourceField, targetField, validations);
        }
    }

    protected void validateFieldTypeConversion(String mappingId, Field sourceField, Field targetField, List<Validation> validations) {
        FieldType sourceFieldType = sourceField.getFieldType();
        if (sourceField.getActions() != null && sourceField.getActions().getActions().size() > 0) {
            Action lastAction = sourceField.getActions().getActions().get(sourceField.getActions().getActions().size()-1);
            ActionDetail detail = null;
            try {
                detail = this.fieldActionService.findActionDetail(lastAction, sourceField.getFieldType());
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
                return;
            }
            sourceFieldType = detail.getTargetType();
        }
        FieldType targetFieldType = targetField.getFieldType();
        Optional<AtlasConverter<?>> atlasConverter = conversionService.findMatchingConverter(sourceFieldType, targetFieldType);
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
                populateConversionConcerns(mappingId, conversionInfo, getFieldName(sourceField), getFieldName(targetField), validations);
            }
        }
    }

    protected void populateConversionConcerns(String mappingId, AtlasConversionInfo converterAnno,
            String sourceFieldName, String targetFieldName, List<Validation> validations) {
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

    private boolean matchDocIdOrNull(String docId) {
        return docId == null || getDocId().equals(docId);
    }

    @SuppressWarnings("unchecked")
    protected String getFieldName(Field field) {
        if (field == null) {
            return "null";
        }
        if (field.getClass().isAssignableFrom(getFieldType())) {
            return getModuleFieldName((T)field);
        }
        if (field.getFieldType() != null) {
            return field.getFieldType().name();
        }
        return field.getClass().getName();
    }

    protected abstract String getModuleFieldName(T field);

    protected AtlasConversionService getConversionService() {
        return conversionService;
    }

    protected void setConversionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

}
