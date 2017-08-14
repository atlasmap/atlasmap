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

import io.atlasmap.java.v2.JavaField;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.core.DefaultAtlasConversionService;
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
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.NonNullValidator;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaValidationService implements AtlasValidationService {

    private static final Logger logger = LoggerFactory.getLogger(JavaValidationService.class);
    private AtlasConversionService conversionService;
    private AtlasModuleDetail moduleDetail = JavaModule.class.getAnnotation(AtlasModuleDetail.class);
    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();
    private static Map<String, Integer> versionMap = new HashMap<>();

    public JavaValidationService() {
        conversionService = DefaultAtlasConversionService.getInstance();
        init();
    }

    public JavaValidationService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
        init();
    }

    public void init() {
        NonNullValidator javaFilePathNonNullValidator = new NonNullValidator("JavaField.Path",
                "Field path must not be null nor empty");
        NonNullValidator inputFieldTypeNonNullValidator = new NonNullValidator("Input.Field.Type",
                "FieldType should not be null nor empty");
        NonNullValidator outputFieldTypeNonNullValidator = new NonNullValidator("Output.Field.Type",
                "FieldType should not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator("Field.Type",
                "Filed type should not be null nor empty");

        validatorMap.put("java.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("java.field.path.not.null", javaFilePathNonNullValidator);
        validatorMap.put("input.field.type.not.null", inputFieldTypeNonNullValidator);
        validatorMap.put("output.field.type.not.null", outputFieldTypeNonNullValidator);

        versionMap.put("1.9", 53);
        versionMap.put("1.8", 52);
        versionMap.put("1.7", 51);
        versionMap.put("1.6", 50);
        versionMap.put("1.5", 49);
        versionMap.put("1.4", 48);
        versionMap.put("1.3", 47);
        versionMap.put("1.2", 46);
        versionMap.put("1.1", 45);
    }

    public void destroy() {
        validatorMap.clear();
        versionMap.clear();
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
            validation.setMessage("No DataSource with atlas:java uri specified");
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }

        return validations;
    }

    private void validateMappings(List<BaseMapping> mappings, List<Validation> validations) {
        for (BaseMapping fieldMapping : mappings) {
            if (fieldMapping.getClass().isAssignableFrom(Mapping.class)
                    && MappingType.MAP.equals(((Mapping) fieldMapping).getMappingType())) {
                validateMapMapping((Mapping) fieldMapping, validations);
            } else if (fieldMapping.getClass().isAssignableFrom(Mapping.class)
                    && MappingType.SEPARATE.equals(((Mapping) fieldMapping).getMappingType())) {
                validateSeparateMapping((Mapping) fieldMapping, validations);
            }
        }
    }

    private void validateMapMapping(Mapping mapping, List<Validation> validations) {
        JavaField inputField = null;
        JavaField outField = null;

        if (mapping != null && mapping.getInputField() != null && mapping.getInputField().size() > 0
                && mapping.getInputField().get(0).getClass().isAssignableFrom(JavaField.class)) {
            inputField = (JavaField) mapping.getInputField().get(0);
        } else {
            Validation validation = new Validation();
            validation.setField("Input.Field");
            validation.setMessage("Input field " + generateBestFieldName(mapping.getInputField().get(0))
                    + " is not supported by the Java Module");
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }

        if (mapping != null && mapping.getOutputField() != null && mapping.getOutputField().size() > 0
                && mapping.getOutputField().get(0).getClass().isAssignableFrom(JavaField.class)) {
            outField = (JavaField) mapping.getOutputField().get(0);
        } else {
            Validation validation = new Validation();
            validation.setField("Output.Field");
            validation.setMessage("Output field " + generateBestFieldName(mapping.getOutputField().get(0))
                    + " is not supported by the Java Module");
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }

        if (inputField != null && outField != null) {
            validateJavaField(inputField, "input", validations);
            validateJavaField(outField, "output", validations);
            validateSourceAndTargetTypes(inputField, outField, validations);
        }
    }

    private void validateSourceAndTargetTypes(JavaField inputField, JavaField outField, List<Validation> validations) {
        // making an assumption that anything marked as COMPLEX would require the use of
        // the class name to find a validator.
        if ((inputField.getFieldType() == null || outField.getFieldType() == null)
                || (inputField.getFieldType().compareTo(FieldType.COMPLEX) == 0
                        || outField.getFieldType().compareTo(FieldType.COMPLEX) == 0)) {
            validateConversion(inputField, outField, true, validations);
        } else if (inputField.getFieldType().compareTo(outField.getFieldType()) != 0) {
            // is this check superseded by the further checks using the AtlasConversionInfo
            // annotations?

            // errors.getAllErrors().add(new AtlasMappingError("Field.Input/Output",
            // inputField.getType().value() + " --> " + outField.getType().value(), "Output
            // field type does not match input field type, may require a converter.",
            // AtlasMappingError.Level.WARN));
            validateConversion(inputField, outField, false, validations);
        }
    }

    private void validateConversion(JavaField inputField, JavaField outField, boolean useClassNames,
            List<Validation> validations) {
        FieldType inFieldType = inputField.getFieldType();
        FieldType outFieldType = outField.getFieldType();
        String rejectedValue;
        Optional<AtlasConverter> atlasConverter;
        // do we have a converter for this?
        if (useClassNames) {
            atlasConverter = conversionService.findMatchingConverter(inputField.getClassName(),
                    outField.getClassName());
        } else {
            atlasConverter = conversionService.findMatchingConverter(inFieldType, outFieldType);
        }
        if (!atlasConverter.isPresent()) {
            rejectedValue = inputField.getClassName() + " --> " + outField.getClassName();
            Validation validation = new Validation();
            validation.setField("Field.Input/Output.conversion");
            validation.setMessage(
                    "A conversion between the input and output fields is required but no converter is available");
            validation.setStatus(ValidationStatus.WARN);
            validation.setValue(rejectedValue.toString());
            validations.add(validation);
        } else {
            AtlasConversionInfo conversionInfo;
            // find the method that does the conversion
            Method[] methods = atlasConverter.get().getClass().getMethods();
            if (useClassNames) {
                conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                        .filter(atlasConversionInfo -> atlasConversionInfo != null)
                        .filter(atlasConversionInfo -> atlasConversionInfo.sourceClassName()
                                .equals(inputField.getClassName())
                                && atlasConversionInfo.targetClassName().equals(outField.getClassName()))
                        .findFirst().orElse(null);
            } else {
                conversionInfo = Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                        .filter(atlasConversionInfo -> atlasConversionInfo != null)
                        .filter(atlasConversionInfo -> (atlasConversionInfo.sourceType().compareTo(inFieldType) == 0
                                && atlasConversionInfo.targetType().compareTo(outFieldType) == 0))
                        .findFirst().orElse(null);
            }

            if (conversionInfo != null) {
                if (useClassNames) {
                    rejectedValue = inputField.getClassName().concat(" --> ").concat(outField.getClassName());
                } else {
                    rejectedValue = inFieldType.value().concat(" --> ").concat(outFieldType.value());
                }

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
        JavaField inputField = null;
        JavaField outField = null;
        // input
        if (fieldMapping != null && fieldMapping.getInputField() != null && fieldMapping.getInputField().get(0) != null
                && fieldMapping.getInputField().get(0).getClass().isAssignableFrom(JavaField.class)) {
            inputField = (JavaField) fieldMapping.getInputField().get(0);
        } else {
            Validation validation = new Validation();
            validation.setField("Input.Field");
            validation.setMessage("Input field " + fieldMapping.getInputField().get(0).getClass().getName()
                    + " + is not supported by Java Module");
            validation.setStatus(ValidationStatus.ERROR);
            validations.add(validation);
        }
        // check that the input field is of type String else error
        if (inputField != null && inputField.getFieldType() != null
                && !FieldType.STRING.equals(inputField.getFieldType())
                || (inputField.getValue() != null && inputField.getValue().getClass().isAssignableFrom(String.class))) {
            Validation validation = new Validation();
            validation.setField("Input.Field");
            validation.setMessage(
                    String.format("Input field must be of type %s for a Separate Mapping", FieldType.STRING));
            validation.setStatus(ValidationStatus.ERROR);
            validation.setValue(inputField.getFieldType().toString());
            validations.add(validation);
        }

        validateJavaField(inputField, "input", validations);
        // TODO call JavaModule.isSupported() on field (false = ERROR)
        // output
        if (fieldMapping != null && fieldMapping.getOutputField() != null && fieldMapping.getOutputField().size() > 0)
            for (Field outputField : fieldMapping.getOutputField()) {
                if (outputField.getClass().isAssignableFrom(JavaField.class)) {
                    validateJavaField((JavaField) outputField, "output", validations);
                    // TODO call JavaModule.isSupported() on field (false = ERROR)
                } else {
                    Validation validation = new Validation();
                    validation.setField("Output.Field");
                    validation.setMessage(String.format("Output field %s is not supported by Java Module",
                            outputField.getClass().getName()));
                    validation.setStatus(ValidationStatus.ERROR);
                    validation.setValue((outField != null ? outField.toString() : null));
                    validations.add(validation);
                }
            }
    }

    private void validateJavaField(JavaField field, String direction, List<Validation> validations) {
        // TODO check that it is a valid type on the AtlasContext

        validatorMap.get("java.field.type.not.null").validate(field, validations, ValidationStatus.WARN);
        if ("input".equalsIgnoreCase(direction)) {
            if (field != null) {
                validatorMap.get("input.field.type.not.null").validate(field.getFieldType(), validations,
                        ValidationStatus.WARN);
            }
        } else {
            if (field != null) {
                validatorMap.get("output.field.type.not.null").validate(field.getFieldType(), validations,
                        ValidationStatus.WARN);
            }
        }
        if (field != null) {
            if (field.getPath() == null) {
                validatorMap.get("java.field.path.not.null").validate(field.getPath(), validations);
            }
            validateClass(field, validations);
        }
    }

    private void validateClass(JavaField field, List<Validation> validations) {
        String clazzName = field.getClassName();
        if (clazzName != null && !clazzName.isEmpty()) {
            Integer major = detectClassVersion(clazzName);
            if (major != null) {
                if (major > versionMap.get(System.getProperty("java.vm.specification.version"))) {
                    Validation validation = new Validation();
                    validation.setField("Field.Classname");
                    validation.setMessage(String.format(
                            "Class for field is compiled against unsupported JDK version: %d current JDK: %d", major,
                            versionMap.get(System.getProperty("java.vm.specification.version"))));
                    validation.setStatus(ValidationStatus.ERROR);
                    validation.setValue(clazzName);
                    validations.add(validation);
                }
            } else {
                Validation validation = new Validation();
                validation.setField("Field.Classname");
                validation.setMessage("Class for field is not found on the classpath");
                validation.setStatus(ValidationStatus.ERROR);
                validation.setValue(clazzName);
                validations.add(validation);
            }
        }
    }

    private String generateBestFieldName(Field field) {
        if (field == null) {
            return "null";
        }

        return field.getClass().getName();
    }

    protected Integer detectClassVersion(String className) {
        Integer major = null;
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(className.replace('.', '/') + ".class");
            if (in == null) {
                in = ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class");
                if (in == null) {
                    return null;
                }
            }
            DataInputStream data = new DataInputStream(in);
            int magic = data.readInt();
            if (magic != 0xCAFEBABE) {
                logger.error(String.format("Invalid Java class: %s magic value: %s", className, magic));
            }

            int minor = 0xFFFF & data.readShort();
            major = new Integer(0xFFFF & data.readShort());

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Detected class: %s version major: %s minor: %s", className, magic, minor));
            }
        } catch (IOException e) {
            logger.error(String.format("Error detected version for class: %s msg: %s", className, e.getMessage()), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ie) {
                    logger.error(String.format("Error closing input stream msg: %s", ie.getMessage()), ie);
                }
            }
        }
        return major;
    }
}
