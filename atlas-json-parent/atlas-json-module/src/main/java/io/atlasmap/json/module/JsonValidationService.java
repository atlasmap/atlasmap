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
package io.atlasmap.json.module;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.NonNullValidator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonValidationService implements AtlasValidationService {

    private AtlasConversionService conversionService;
    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();
    private AtlasModuleDetail moduleDetail = JsonModule.class.getAnnotation(AtlasModuleDetail.class);

    public void init() {
        NonNullValidator javaFilePathNonNullValidator = new NonNullValidator("JsonField.Path",
                "The path element must not be null nor empty");
        NonNullValidator inputFieldTypeNonNullValidator = new NonNullValidator("Input.Field.Type",
                "Field type should not be null nor empty");
        NonNullValidator outputFieldTypeNonNullValidator = new NonNullValidator("Output.Field.Type",
                "Field type should not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator("Field.Type",
                "Filed type should not be null nor empty");

        validatorMap.put("json.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("json.field.path.not.null", javaFilePathNonNullValidator);
        validatorMap.put("input.field.type.not.null", inputFieldTypeNonNullValidator);
        validatorMap.put("output.field.type.not.null", outputFieldTypeNonNullValidator);
    }

    public void destroy() {
        validatorMap.clear();
    }

    public JsonValidationService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
        init();
    }

    @Override
    public List<Validation> validateMapping(AtlasMapping mapping) {
        List<Validation> validations = new ArrayList<Validation>();
        if (mapping != null && mapping.getMappings() != null && mapping.getMappings().getMapping() != null
                && !mapping.getMappings().getMapping().isEmpty()) {
            validateMappings(mapping.getMappings().getMapping(), validations);
        }

        boolean found = false;
        for (DataSource ds : mapping.getDataSource()) {
            if (ds.getUri() != null && ds.getUri().startsWith(moduleDetail.uri())) {
                found = true;
            }
        }

        if (!found) {
            Validation validation = new Validation();
            validation.setField(String.format("DataSource.uri"));
            validation.setMessage("No DataSource with atlas:json uri specified");
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }

        return validations;
    }

    private void validateMappings(List<BaseMapping> mappings, List<Validation> validations) {
        for (BaseMapping mapping : mappings) {
            if (mapping.getClass().isAssignableFrom(Mapping.class)) {
                switch (mapping.getMappingType()) {
                case MAP:
                    validateMapMapping((Mapping) mapping, validations);
                    break;
                case SEPARATE:
                    validateSeparateMapping((Mapping) mapping, validations);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private void validateMapMapping(Mapping fieldMapping, List<Validation> validations) {
        Field input = fieldMapping.getInputField().get(0);
        Field output = fieldMapping.getOutputField().get(0);

        if (input != null && output != null) {
            if (input instanceof JsonField && output instanceof JsonField) {
                validateJsonField((JsonField) input, "input", validations);
                validateJsonField((JsonField) output, "output", validations);
                validateSourceAndTargetTypes((JsonField) input, (JsonField) output, validations);
            }
        }
    }

    private void validateSourceAndTargetTypes(JsonField inputField, JsonField outField, List<Validation> validations) {
        // making an assumption that anything marked as COMPLEX would require the use of
        // the class name to find a validator.
        if ((inputField.getFieldType() == null || outField.getFieldType() == null)
                || (inputField.getFieldType().compareTo(FieldType.COMPLEX) == 0
                        || outField.getFieldType().compareTo(FieldType.COMPLEX) == 0)) {
            validateConversion(inputField, outField, validations);
        } else if (inputField.getFieldType().compareTo(outField.getFieldType()) != 0) {
            // is this check superseded by the further checks using the AtlasConversionInfo
            // annotations?

            // errors.getAllErrors().add(new AtlasMappingError("Field.Input/Output",
            // inputField.getType().value() + " --> " + outField.getType().value(), "Output
            // field type does not match input field type, may require a converter.",
            // AtlasMappingError.Level.WARN));
            validateConversion(inputField, outField, validations);
        }
    }

    private void validateConversion(JsonField inputField, JsonField outField, List<Validation> validations) {
        FieldType inFieldType = inputField.getFieldType();
        FieldType outFieldType = outField.getFieldType();
        String rejectedValue;
        Optional<AtlasConverter> atlasConverter;
        // do we have a converter for this?

        if (inFieldType == null && outFieldType == null) {
            if (inputField.getActions() == null || inputField.getActions().getActions() == null
                    || inputField.getActions().getActions().isEmpty()) {
                Validation inVal = new Validation();
                inVal.setField("inputPath=" + inputField.getPath());
                inVal.setMessage(
                        "Auto-detection required due to unspecified input fieldType and no transformation fieldAction specified");
                inVal.setStatus(ValidationStatus.WARN);
                validations.add(inVal);
            }
            if (outField.getActions() == null || outField.getActions().getActions() == null
                    || outField.getActions().getActions().isEmpty()) {
                Validation outVal = new Validation();
                outVal.setField("outputPath=" + outField.getPath());
                outVal.setMessage("Auto-detection required due to unspecified output fieldType");
                outVal.setStatus(ValidationStatus.WARN);
                validations.add(outVal);
            }
            return;
        }

        atlasConverter = conversionService.findMatchingConverter(inFieldType, outFieldType);

        if (!atlasConverter.isPresent()) {
            rejectedValue = inputField.getName();
            Validation validation = new Validation();
            validation.setField("Field.Input/Output.conversion");
            validation.setMessage(
                    "A conversion between the input and output fields is required but no converter is available");
            validation.setStatus(ValidationStatus.ERROR);
            validation.setValue((rejectedValue != null ? rejectedValue.toString() : null));
            validations.add(validation);
        } else {
            AtlasConversionInfo conversionInfo;
            // find the method that does the conversion
            Method[] methods = atlasConverter.get().getClass().getMethods();
            conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                    .filter(atlasConversionInfo -> atlasConversionInfo != null)
                    .filter(atlasConversionInfo -> (atlasConversionInfo.sourceType().compareTo(inFieldType) == 0
                            && atlasConversionInfo.targetType().compareTo(outFieldType) == 0))
                    .findFirst().orElse(null);

            if (conversionInfo != null) {
                rejectedValue = inFieldType.value().concat(" --> ").concat(outFieldType.value());

                AtlasConversionConcern[] atlasConversionConcerns = conversionInfo.concerns();
                for (AtlasConversionConcern atlasConversionConcern : atlasConversionConcerns) {
                    if (AtlasConversionConcern.NONE.equals(atlasConversionConcern)) {
                        Validation validation = new Validation();
                        validation.setField("Field.Input/Output.conversion");
                        validation.setMessage(atlasConversionConcern.getMessage());
                        validation.setStatus(ValidationStatus.INFO);
                        validation.setValue(rejectedValue.toString());
                        validations.add(validation);
                    }
                    if (atlasConversionConcern.equals(AtlasConversionConcern.RANGE)
                            || atlasConversionConcern.equals(AtlasConversionConcern.FORMAT)) {
                        Validation validation = new Validation();
                        validation.setField("Field.Input/Output.conversion");
                        validation.setMessage(atlasConversionConcern.getMessage());
                        validation.setStatus(ValidationStatus.WARN);
                        validation.setValue(rejectedValue.toString());
                        validations.add(validation);
                    }
                    if (atlasConversionConcern.equals(AtlasConversionConcern.UNSUPPORTED)) {
                        Validation validation = new Validation();
                        validation.setField("Field.Input/Output.conversion");
                        validation.setMessage(atlasConversionConcern.getMessage());
                        validation.setStatus(ValidationStatus.ERROR);
                        validation.setValue(rejectedValue.toString());
                        validations.add(validation);
                    }
                }
            }
        }
    }

    private void validateSeparateMapping(Mapping fieldMapping, List<Validation> validations) {
        // input
        Field input = fieldMapping.getInputField().get(0);
        JsonField jsonInput = null;
        JsonField jsonOutput = null;

        if (input.getClass().isAssignableFrom(JsonField.class)) {
            jsonInput = (JsonField) input;

            // check that the input field is of type String else error
            if (jsonInput != null && jsonInput.getFieldType().compareTo(FieldType.STRING) != 0) {
                Validation validation = new Validation();
                validation.setField("Input.Field");
                validation.setMessage("Input field must be of type " + FieldType.STRING + " for a Separate Mapping");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue(jsonInput.getFieldType().toString());
                validations.add(validation);
            }
            validateJsonField(jsonInput, "input", validations);
        }

        // TODO call JsonModule.isSupported() on field (false = ERROR)
        // output
        for (Field mappedField : fieldMapping.getOutputField()) {
            if (mappedField.getClass().isAssignableFrom(JsonField.class)) {
                jsonOutput = (JsonField) mappedField;
                validateJsonField(jsonOutput, "output", validations);
                // TODO call JavaModule.isSupported() on field (false = ERROR)
            } else {
                Validation validation = new Validation();
                validation.setField("Output.Field");
                validation.setMessage(
                        "Output field " + mappedField.getClass().getName() + " is not supported by Json Module");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue((jsonOutput != null ? jsonOutput.toString() : null));
                validations.add(validation);
            }
        }
    }

    private void validateJsonField(JsonField field, String direction, List<Validation> validations) {
        // TODO check that it is a valid type on the AtlasContext

        validatorMap.get("json.field.type.not.null").validate(field, validations, ValidationStatus.WARN);

        if (field != null) {
            if (field.getPath() != null) {
                validatorMap.get("json.field.path.not.null").validate(field.getPath(), validations);
            }
        }
    }
}
