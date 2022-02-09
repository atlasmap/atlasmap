/*
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
package io.atlasmap.kafkaconnect.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.core.validate.BaseModuleValidationService;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.NonNullValidator;

/**
 * The module validation service for Kafka Connect.
 */
public class KafkaConnectValidationService extends BaseModuleValidationService<KafkaConnectField> {

    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();
    private AtlasModuleDetail moduleDetail = KafkaConnectModule.class.getAnnotation(AtlasModuleDetail.class);

    /**
     * A constructor.
     * @param conversionService conversion service
     * @param fieldActionService field action service
     */
    public KafkaConnectValidationService(AtlasConversionService conversionService,
            AtlasFieldActionService fieldActionService) {
        super(conversionService, fieldActionService);
        init();
    }

    /**
     * Initializes.
     */
    public void init() {
        NonNullValidator pathNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
                "The path element must not be null nor empty");
        NonNullValidator fieldTypeNonNullValidator = new NonNullValidator(ValidationScope.MAPPING,
                "Filed type should not be null nor empty");

        validatorMap.put("kafkaconnect.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("kafkaconnect.field.path.not.null", pathNonNullValidator);
        validatorMap.put("input.field.type.not.null", fieldTypeNonNullValidator);
        validatorMap.put("output.field.type.not.null", fieldTypeNonNullValidator);
    }

    protected AtlasModuleDetail getModuleDetail() {
        return moduleDetail;
    }

    @Override
    protected Class<KafkaConnectField> getFieldType() {
        return KafkaConnectField.class;
    }

    @Override
    protected void validateModuleField(String mappingId, KafkaConnectField field, FieldDirection direction,
            List<Validation> validations) {
        validatorMap.get("kafkaconnect.field.type.not.null").validate(field, validations, mappingId, ValidationStatus.WARN);

        if (field.getPath() == null) {
            validatorMap.get("kafkaconnect.field.path.not.null").validate(field.getPath(), validations, mappingId);
        }
    }

    @Override
    protected String getModuleFieldName(KafkaConnectField field) {
        return field.getName() != null ? field.getName() : field.getPath();
    }

}
