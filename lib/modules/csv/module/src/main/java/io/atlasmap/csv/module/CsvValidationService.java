/**
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
package io.atlasmap.csv.module;

import io.atlasmap.core.validate.BaseModuleValidationService;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.Validation;

import java.util.List;

public class CsvValidationService extends BaseModuleValidationService<CsvField> {

    private AtlasModuleDetail moduleDetail = CsvModule.class.getAnnotation(AtlasModuleDetail.class);

    public CsvValidationService(AtlasConversionService conversionService, AtlasFieldActionService fieldActionService) {
        super(conversionService, fieldActionService);
    }

    @Override
    protected AtlasModuleDetail getModuleDetail() {
        return moduleDetail;
    }

    @Override
    protected Class<CsvField> getFieldType() {
        return CsvField.class;
    }

    @Override
    protected void validateModuleField(String mappingId, CsvField field, FieldDirection direction, List<Validation> validation) {

    }

    @Override
    protected String getModuleFieldName(CsvField field) {
        return field.getName() != null ? field.getName() : field.getPath();
    }

}
