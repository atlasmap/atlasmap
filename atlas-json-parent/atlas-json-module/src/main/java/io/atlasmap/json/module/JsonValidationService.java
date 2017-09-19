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
import io.atlasmap.core.BaseModuleValidationService;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.NonNullValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonValidationService extends BaseModuleValidationService<JsonField> {

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
        super(conversionService);
        init();
    }

    @Override
    protected AtlasModuleDetail getModuleDetail() {
        return moduleDetail;
    }

    @Override
    protected Class<JsonField> getFieldType() {
        return JsonField.class;
    }

    @Override
    protected String getModuleFieldName(JsonField field) {
        StringBuilder buf = new StringBuilder();
        if (field.getName() != null) {
            buf.append(field.getName());
        }
        if (field.getFieldType() != null) {
            buf.append("(").append(field.getFieldType().name()).append(")");
        }
        return buf.toString();
    }

    @Override
    protected void validateModuleField(JsonField field, FieldDirection direction, List<Validation> validations) {
        // TODO check that it is a valid type on the AtlasContext

        validatorMap.get("json.field.type.not.null").validate(field, validations, ValidationStatus.WARN);

        if (field.getPath() == null) {
            validatorMap.get("json.field.path.not.null").validate(field.getPath(), validations);
        }
    }
}
