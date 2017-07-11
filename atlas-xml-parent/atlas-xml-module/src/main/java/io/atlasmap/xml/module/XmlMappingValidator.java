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
package io.atlasmap.xml.module;

import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
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

public class XmlMappingValidator {

    private AtlasMapping mapping;
    private Validations validations;
    private AtlasConversionService conversionService;
    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();

    static {
        NonNullValidator javaFileNameNonNullValidator = new NonNullValidator("XmlField.Name", "The name element must not be null nor empty");
        NonNullValidator javaFilePathNonNullValidator = new NonNullValidator("XmlField.Path", "The path element must not be null nor empty");
        NonNullValidator inputFieldTypeNonNullValidator = new NonNullValidator("Input.Field.Type", "Field type should not be null nor empty");
        NonNullValidator outputFieldTypeNonNullValidator = new NonNullValidator("Output.Field.Type", "Field type should not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator("Field.Type", "Filed type should not be null nor empty");

        validatorMap.put("xml.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("xml.field.name.not.null", javaFileNameNonNullValidator);
        validatorMap.put("xml.field.path.not.null", javaFilePathNonNullValidator);
        validatorMap.put("input.field.type.not.null", inputFieldTypeNonNullValidator);
        validatorMap.put("output.field.type.not.null", outputFieldTypeNonNullValidator);
    }

    public XmlMappingValidator(AtlasMapping mapping, Validations validations) {
        this.mapping = mapping;
        this.validations = validations;
        this.conversionService = DefaultAtlasConversionService.getRegistry();
    }

    public XmlMappingValidator(AtlasMapping mapping) {
        this.mapping = mapping;
        this.validations = new DefaultAtlasValidationsHelper();
        this.conversionService = DefaultAtlasConversionService.getRegistry();
    }

    public XmlMappingValidator(AtlasMapping mapping, Validations validations, AtlasConversionService conversionService) {
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
                for (BaseMapping baseMapping : mapping.getMappings().getMapping()) {
                    if (baseMapping.getClass().isAssignableFrom(Mapping.class)) {
                        if(MappingType.MAP.equals(((Mapping)baseMapping).getMappingType())) {
                            evaluateMapMapping((Mapping) baseMapping);
                        } else if(MappingType.SEPARATE.equals(((Mapping)baseMapping).getMappingType())) {
                            evaluateSeparateMapping((Mapping) baseMapping);
                        }
                    }
                }
            }
        }
    }

    private void evaluateMapMapping(Mapping fieldMapping) {
        XmlField inputField = null;
        XmlField outField = null;

        if (inputField != null && outField != null) {
            validateXmlField(inputField, "input");
            validateXmlField(outField, "output");
            validateSourceAndTargetTypes(inputField, outField);
        }
    }

    private void validateSourceAndTargetTypes(XmlField inputField, XmlField outField) {
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

    private void validateConversion(XmlField inputField, XmlField outField) {
        FieldType inFieldType = inputField.getFieldType();
        FieldType outFieldType = outField.getFieldType();
        String rejectedValue;
        Optional<AtlasConverter> atlasConverter;
        //do we have a converter for this?
        atlasConverter = conversionService.findMatchingConverter(inFieldType, outFieldType);

        if (!atlasConverter.isPresent()) {
            rejectedValue = inputField.getName();
            Validation validation = new Validation();
            validation.setField("Field.Input/Output.conversion");
            validation.setMessage("A conversion between the input and output fields is required but no converter is available");
            validation.setStatus(ValidationStatus.ERROR);
            validation.setValue(rejectedValue.toString());
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
        XmlField inputField = null;
        XmlField outField = null;

        // check that the input field is of type String else error
        if (inputField != null && inputField.getFieldType().compareTo(FieldType.STRING) != 0) {
            Validation validation = new Validation();
            validation.setField("Input.Field");
            validation.setMessage("Input field must be of type " + FieldType.STRING + " for a Separate Mapping");
            validation.setStatus(ValidationStatus.ERROR);
            validation.setValue(inputField.getFieldType().toString());
            validations.getValidation().add(validation);
        }

        validateXmlField(inputField, "input");
        //TODO call JavaModule.isSupported() on field  (false = ERROR)
        // output
        for (Field mappedField : fieldMapping.getOutputField()) {
            if (mappedField.getClass().isAssignableFrom(XmlField.class)) {
                outField = (XmlField) mappedField;
                validateXmlField(outField, "output");
                //TODO call JavaModule.isSupported() on field  (false = ERROR)
            } else {
                Validation validation = new Validation();
                validation.setField("Output.Field");
                validation.setMessage("Output field " + mappedField.getClass().getName() + " is not supported by Xml Module");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue((outField != null ? outField.toString() : null));
                validations.getValidation().add(validation);
            }
        }
    }

    private void validateXmlField(XmlField field, String direction) {
        //TODO check that it is a valid type on the AtlasContext

        validatorMap.get("xml.field.type.not.null").validate(field, validations, ValidationStatus.WARN);
        if ("input".equalsIgnoreCase(direction)) {
            // assert that source uri contains java module
            if (!mapping.getDataSource().get(0).getUri().contains("xml")) {
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
            if (!mapping.getDataSource().get(1).getUri().contains("xml")) {
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
        if (field != null) {
            if ((field.getName() == null && field.getPath() == null)) {
                Validation validation = new Validation();
                validation.setField(String.format("Field.%s.Name/Path", direction));
                validation.setMessage("One of path or name must be specified");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue(field.toString());
                validations.getValidation().add(validation);
            } else if (field.getName() != null && field.getPath() == null) {
                validatorMap.get("xml.field.name.not.null").validate(field.getName(), validations);
            } else if (field.getName() == null && field.getPath() != null) {
                validatorMap.get("xml.field.path.not.null").validate(field.getPath(), validations);
            }
        }
    }
}
