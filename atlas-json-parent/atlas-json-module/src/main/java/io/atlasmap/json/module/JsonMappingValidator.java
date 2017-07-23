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
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.v2.Validations;
import io.atlasmap.validators.DefaultAtlasValidationsHelper;
import io.atlasmap.validators.NonNullValidator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonMappingValidator {

    private AtlasMapping mapping;
    private Validations validations;
    private AtlasConversionService conversionService;
    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();

    static {
        NonNullValidator javaFileNameNonNullValidator = new NonNullValidator("JsonField.Name", "The name element must not be null nor empty");
        NonNullValidator javaFilePathNonNullValidator = new NonNullValidator("JsonField.Path", "The path element must not be null nor empty");
        NonNullValidator inputFieldTypeNonNullValidator = new NonNullValidator("Input.Field.Type", "Field type should not be null nor empty");
        NonNullValidator outputFieldTypeNonNullValidator = new NonNullValidator("Output.Field.Type", "Field type should not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator("Field.Type", "Filed type should not be null nor empty");

        validatorMap.put("json.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("json.field.name.not.null", javaFileNameNonNullValidator);
        validatorMap.put("json.field.path.not.null", javaFilePathNonNullValidator);
        validatorMap.put("input.field.type.not.null", inputFieldTypeNonNullValidator);
        validatorMap.put("output.field.type.not.null", outputFieldTypeNonNullValidator);
    }

    public JsonMappingValidator(AtlasMapping mapping, Validations validations) {
        this.mapping = mapping;
        this.validations = validations;
        this.conversionService = DefaultAtlasConversionService.getRegistry();
    }

    public JsonMappingValidator(AtlasMapping mapping) {
        this.mapping = mapping;
        this.validations = new DefaultAtlasValidationsHelper();
        this.conversionService = DefaultAtlasConversionService.getRegistry();
    }

    public JsonMappingValidator(AtlasMapping mapping, Validations validations, AtlasConversionService conversionService) {
        this.mapping = mapping;
        this.validations = validations;
        this.conversionService = conversionService;
    }

    public Validations validateAtlasMappingFile() {
        validateFieldMappings();
        return validations;
    }

    private void validateFieldMappings() {
        if (mapping.getMappings() != null) {
            if (mapping.getMappings().getMapping() != null && !mapping.getMappings().getMapping().isEmpty()) {
                for (BaseMapping mapping : mapping.getMappings().getMapping()) {
                    if (mapping.getClass().isAssignableFrom(Mapping.class)) {
                        switch(mapping.getMappingType()) {
                        case MAP: evaluateMapMapping((Mapping) mapping); break;
                        case SEPARATE: evaluateSeparateMapping((Mapping) mapping); break;
                        }
                    }
                }
            }
        }
    }

    private void evaluateMapMapping(Mapping fieldMapping) {
        Field input = fieldMapping.getInputField().get(0);
        Field output = fieldMapping.getOutputField().get(0);

        if (input != null && output != null) {
            if(input instanceof JsonField && output instanceof JsonField) {
                validateJsonField((JsonField)input, "input");
                validateJsonField((JsonField)output, "output");
                validateSourceAndTargetTypes((JsonField)input, (JsonField)output);
            }
        }
    }

    private void validateSourceAndTargetTypes(JsonField inputField, JsonField outField) {
        // making an assumption that anything marked as COMPLEX would require the use of the class name to find a validator.
        if ((inputField.getFieldType() == null || outField.getFieldType() == null) ||
                (inputField.getFieldType().compareTo(FieldType.COMPLEX) == 0 || outField.getFieldType().compareTo(FieldType.COMPLEX) == 0)) {
            validateConversion(inputField, outField);
        } else if (inputField.getFieldType().compareTo(outField.getFieldType()) != 0) {
            // is this check superseded by the further checks using the AtlasConversionInfo annotations?

//            errors.getAllErrors().add(new AtlasMappingError("Field.Input/Output", inputField.getType().value() + " --> " + outField.getType().value(), "Output field type does not match input field type, may require a converter.", AtlasMappingError.Level.WARN));
            validateConversion(inputField, outField);
        }
    }

    private void validateConversion(JsonField inputField, JsonField outField) {
        FieldType inFieldType = inputField.getFieldType();
        FieldType outFieldType = outField.getFieldType();
        String rejectedValue;
        Optional<AtlasConverter> atlasConverter;
        //do we have a converter for this?
        
        if(inFieldType == null && outFieldType == null) {
            if(inputField.getActions() == null || inputField.getActions().getActions() == null || inputField.getActions().getActions().isEmpty()) {
                Validation inVal = new Validation();
                inVal.setField("inputPath=" + inputField.getPath());
                inVal.setMessage("Auto-detection required due to unspecified input fieldType and no transformation fieldAction specified");
                inVal.setStatus(ValidationStatus.WARN);
                validations.getValidation().add(inVal);
            }
            if(outField.getActions() == null || outField.getActions().getActions() == null || outField.getActions().getActions().isEmpty()) {
                Validation outVal = new Validation();
                outVal.setField("outputPath=" + outField.getPath());
                outVal.setMessage("Auto-detection required due to unspecified output fieldType");
                outVal.setStatus(ValidationStatus.WARN);
                validations.getValidation().add(outVal);
            }
            return;
        }
        
        atlasConverter = conversionService.findMatchingConverter(inFieldType, outFieldType);

        if (!atlasConverter.isPresent()) {
            rejectedValue = inputField.getName();
            Validation validation = new Validation();
            validation.setField("Field.Input/Output.conversion");
            validation.setMessage("A conversion between the input and output fields is required but no converter is available");
            validation.setStatus(ValidationStatus.ERROR);
            validation.setValue((rejectedValue != null ? rejectedValue.toString() : null));
            validations.getValidation().add(validation);
        } else {
            AtlasConversionInfo conversionInfo;
            //find the method that does the conversion
            Method[] methods = atlasConverter.get().getClass().getMethods();
            conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                    .filter(atlasConversionInfo -> atlasConversionInfo != null)
                    .filter(atlasConversionInfo -> (atlasConversionInfo.sourceType().compareTo(inFieldType) == 0
                            && atlasConversionInfo.targetType().compareTo(outFieldType) == 0)).findFirst().orElse(null);

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
                        validations.getValidation().add(validation);
                    }
                    if (atlasConversionConcern.equals(AtlasConversionConcern.RANGE) || atlasConversionConcern.equals(AtlasConversionConcern.FORMAT)) {
                        Validation validation = new Validation();
                        validation.setField("Field.Input/Output.conversion");
                        validation.setMessage(atlasConversionConcern.getMessage());
                        validation.setStatus(ValidationStatus.WARN);
                        validation.setValue(rejectedValue.toString());
                        validations.getValidation().add(validation);
                    }
                    if (atlasConversionConcern.equals(AtlasConversionConcern.UNSUPPORTED)) {
                        Validation validation = new Validation();
                        validation.setField("Field.Input/Output.conversion");
                        validation.setMessage(atlasConversionConcern.getMessage());
                        validation.setStatus(ValidationStatus.ERROR);
                        validation.setValue(rejectedValue.toString());
                        validations.getValidation().add(validation);
                    }
                }
            }
        }
    }

    private void evaluateSeparateMapping(Mapping fieldMapping) {
        // input
        Field input = fieldMapping.getInputField().get(0);
        JsonField jsonInput = null;
        JsonField jsonOutput = null;

        if(input.getClass().isAssignableFrom(JsonField.class)) {
            jsonInput = (JsonField)input;
            
            // check that the input field is of type String else error
            if (jsonInput != null &&jsonInput.getFieldType().compareTo(FieldType.STRING) != 0) {
                Validation validation = new Validation();
                validation.setField("Input.Field");
                validation.setMessage("Input field must be of type " + FieldType.STRING + " for a Separate Mapping");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue(jsonInput.getFieldType().toString());
                validations.getValidation().add(validation);
            }
            validateJsonField(jsonInput, "input");
        } 
       
        
        //TODO call JavaModule.isSupported() on field  (false = ERROR)
        // output
        for (Field mappedField : fieldMapping.getOutputField()) {
            if (mappedField.getClass().isAssignableFrom(JsonField.class)) {
                jsonOutput = (JsonField) mappedField;
                validateJsonField(jsonOutput, "output");
                //TODO call JavaModule.isSupported() on field  (false = ERROR)
            } else {
                Validation validation = new Validation();
                validation.setField("Output.Field");
                validation.setMessage("Output field " + mappedField.getClass().getName() + " is not supported by Json Module");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue((jsonOutput != null ? jsonOutput.toString() : null));
                validations.getValidation().add(validation);
            }
        }
    }

    private void validateJsonField(JsonField field, String direction) {
        //TODO check that it is a valid type on the AtlasContext

        validatorMap.get("json.field.type.not.null").validate(field, validations, ValidationStatus.WARN);
        
        for(DataSource ds : mapping.getDataSource()) {
            if ("input".equalsIgnoreCase(direction) && DataSourceType.SOURCE.equals(ds.getDataSourceType())) {
                // assert that source uri contains java module
                if (!ds.getUri().contains("json")) {
                    Validation validation = new Validation();
                    validation.setField(String.format("Field.%s.Name/Path", direction));
                    validation.setMessage("Source module does not support source field");
                    validation.setStatus(ValidationStatus.ERROR);
                    validation.setValue(field.toString());
                    validations.getValidation().add(validation);
                }
                if (field != null) {
                    validatorMap.get("input.field.type.not.null").validate(field.getFieldType(), validations, ValidationStatus.WARN);
                }
            } else {
                // assert that target uri contains java module
                if (!ds.getUri().contains("json") ) {
                    Validation validation = new Validation();
                    validation.setField(String.format("Field.%s.Name/Path", direction));
                    validation.setMessage("Target module does not support target field");
                    validation.setStatus(ValidationStatus.ERROR);
                    validation.setValue(field.toString());
                    validations.getValidation().add(validation);
                }
                if (field != null) {
                    validatorMap.get("output.field.type.not.null").validate(field.getFieldType(), validations, ValidationStatus.WARN);
                }
            }
        }
        if (field != null) {
            if ((field.getName() == null && field.getPath() == null)) {
                Validation validation = new Validation();
                validation.setField(String.format("Field.%s.Name/Path", direction));
                validation.setMessage("One of path or name must be specified");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue(field.toString());
                validations.getValidation().add(validation);
            } else if (field.getName() != null && field.getPath() == null) {
                validatorMap.get("json.field.name.not.null").validate(field.getName(), validations);
            } else if (field.getName() == null && field.getPath() != null) {
                validatorMap.get("json.field.path.not.null").validate(field.getPath(), validations);
            }
        }
    }
}
