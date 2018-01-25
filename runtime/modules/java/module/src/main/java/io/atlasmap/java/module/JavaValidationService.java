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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.core.BaseModuleValidationService;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.NonNullValidator;

public class JavaValidationService extends BaseModuleValidationService<JavaField> {

    private static final Logger LOG = LoggerFactory.getLogger(JavaValidationService.class);
    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();
    private static Map<String, Integer> versionMap = new HashMap<>();
    private AtlasModuleDetail moduleDetail = JavaModule.class.getAnnotation(AtlasModuleDetail.class);

    public JavaValidationService() {
        super();
        init();
    }

    public JavaValidationService(AtlasConversionService conversionService) {
        super(conversionService);
        init();
    }

    public void init() {
        NonNullValidator javaFilePathNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
                "Field path must not be null nor empty");
        NonNullValidator inputFieldTypeNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
                "FieldType should not be null nor empty");
        NonNullValidator outputFieldTypeNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
                "FieldType should not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
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
    protected AtlasModuleDetail getModuleDetail() {
        return moduleDetail;
    }

    @Override
    protected Class<JavaField> getFieldType() {
        return JavaField.class;
    }

    @Override
    protected String getModuleFieldName(JavaField field) {
        return field.getName() != null ? field.getName() : field.getPath();
    }

    protected void validateSourceAndTargetTypes(String mappingId, Field inputField, Field outField,
            List<Validation> validations) {
        if ((inputField instanceof JavaField && outField instanceof JavaField) && ((inputField.getFieldType() == null
                || outField.getFieldType() == null)
                || (inputField.getFieldType() == FieldType.COMPLEX || outField.getFieldType() == FieldType.COMPLEX))) {
            // making an assumption that anything marked as COMPLEX would require the use of
            // the class name to find a validator.
            validateClassConversion(mappingId, (JavaField) inputField, (JavaField) outField, validations);
            return;
        }

        if (inputField.getFieldType() != outField.getFieldType()) {
            // is this check superseded by the further checks using the AtlasConversionInfo
            // annotations?

            // errors.getAllErrors().add(new AtlasMappingError("Field.Input/Output",
            // inputField.getType().value() + " --> " + outField.getType().value(), "Output
            // field type does not match input field type, may require a converter.",
            // AtlasMappingError.Level.WARN));
            validateFieldTypeConversion(mappingId, inputField, outField, validations);
        }
    }

    private void validateClassConversion(String mappingId, JavaField inputField, JavaField outField,
            List<Validation> validations) {
        Optional<AtlasConverter<?>> atlasConverter = getConversionService()
                .findMatchingConverter(inputField.getClassName(), outField.getClassName());
        if (!atlasConverter.isPresent()) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setId(mappingId);
            validation
                    .setMessage(String.format("Conversion from '%s' to '%s' is required but no converter is available",
                            inputField.getClassName(), outField.getClassName()));
            validation.setStatus(ValidationStatus.WARN);
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
                populateConversionConcerns(mappingId, conversionInfo, getFieldName(inputField), getFieldName(outField),
                        validations);
            }
        }
    }

    @Override
    protected void validateModuleField(String mappingId, JavaField field, FieldDirection direction,
            List<Validation> validations) {
        validatorMap.get("java.field.type.not.null").validate(field, validations, mappingId, ValidationStatus.WARN);
        if (direction == FieldDirection.SOURCE) {
            validatorMap.get("input.field.type.not.null").validate(field.getFieldType(), validations, mappingId,
                    ValidationStatus.WARN);
        } else {
            validatorMap.get("output.field.type.not.null").validate(field.getFieldType(), validations, mappingId,
                    ValidationStatus.WARN);
        }
        if (field.getPath() == null) {
            validatorMap.get("java.field.path.not.null").validate(field.getPath(), validations, mappingId);
        }

        validateClass(mappingId, field, validations);
    }

    private void validateClass(String mappingId, JavaField field, List<Validation> validations) {
        String clazzName = field.getClassName();
        if (clazzName != null && !clazzName.isEmpty()) {
            Integer major = detectClassVersion(clazzName);
            if (major != null) {
                if (major > versionMap.get(System.getProperty("java.vm.specification.version"))) {
                    Validation validation = new Validation();
                    validation.setScope(ValidationScope.MAPPING);
                    validation.setId(mappingId);
                    validation.setMessage(String.format(
                            "Class '%s' for field is compiled against unsupported JDK version: %d current JDK: %d",
                            clazzName, major, versionMap.get(System.getProperty("java.vm.specification.version"))));
                    validation.setStatus(ValidationStatus.ERROR);
                    validations.add(validation);
                }
            } else {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(String.format("Class '%s' for field is not found on the classpath", clazzName));
                validation.setStatus(ValidationStatus.ERROR);
                validations.add(validation);
            }
        }
    }

    private Integer detectClassVersion(String className) {
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
                LOG.error(String.format("Invalid Java class: %s magic value: %s", className, magic));
            }

            int minor = 0xFFFF & data.readShort();
            major = new Integer(0xFFFF & data.readShort());

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Detected class: %s version major: %s minor: %s", className, magic, minor));
            }
        } catch (IOException e) {
            LOG.error(String.format("Error detected version for class: %s msg: %s", className, e.getMessage()), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ie) {
                    LOG.error(String.format("Error closing input stream msg: %s", ie.getMessage()), ie);
                }
            }
        }
        return major;
    }

}
