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

import java.util.List;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class MultipleFieldSelectionValidator {

    private BaseModuleValidationService<?> service;

    public MultipleFieldSelectionValidator(BaseModuleValidationService<?> service) {
        this.service = service;
    }

    public void validate(List<Validation> validations, String mappingId, FieldDirection direction, List<Field> fields) {
        if (fields.size() <= 1) {
            return;
        }
        for (Field f : fields) {
            if (!service.matchDocIdOrNull(f.getDocId())) {
                continue;
            }
            AtlasPath path = new AtlasPath(f.getPath());
            if (path.hasCollection()) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.MAPPING);
                validation.setId(mappingId);
                validation.setMessage(String.format(
                    "A %s field contained in a collection can not be selected with other %s field: ['%s']",
                    direction.value(), direction.value(), f.getPath()));
                validation.setStatus(ValidationStatus.ERROR);
                validations.add(validation);
            }
        }
    }

}