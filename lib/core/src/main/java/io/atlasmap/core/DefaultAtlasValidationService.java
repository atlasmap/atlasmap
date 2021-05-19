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
package io.atlasmap.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.CompositeValidator;
import io.atlasmap.validators.LookupTableNameValidator;
import io.atlasmap.validators.NonNullValidator;
import io.atlasmap.validators.NotEmptyValidator;
import io.atlasmap.validators.PositiveIntegerValidator;
import io.atlasmap.validators.StringPatternValidator;

public class DefaultAtlasValidationService implements AtlasValidationService {

    enum Validators {
        MAPPING_NAME (() -> {
            StringPatternValidator namePattern = new StringPatternValidator(
                ValidationScope.ALL,
                "Mapping name must not contain spaces nor special characters other than period (.) and underscore (_), but was '%s'",
                "[^A-Za-z0-9_.]");
            NonNullValidator nameNotNull = new NonNullValidator(
                ValidationScope.ALL, "Mapping name must not be null nor empty");
            return new CompositeValidator(namePattern, nameNotNull);
        }),
        DATASOURCE_TARGET_URI (() ->
            new NonNullValidator(ValidationScope.DATA_SOURCE, "DataSource target uri must not be null nor empty")
        ),
        DATASOURCE_SOURCE_URI (() ->
            new NonNullValidator(ValidationScope.DATA_SOURCE, "DataSource source uri must not be null nor empty")
        ),

        MAPPINGS_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Field mappings must not be null")
        ),

        COMBINE_INPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Source field should not be null")
        ),
        COMBINE_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Source field should not be empty")
        ),
        COMBINE_OUTPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Target element must not be null")
        ),
        COMBINE_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Target field must not be empty")
        ),
        COMBINE_INPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator(
                ValidationScope.MAPPING, "Source fields should not be null")
        ),
        COMBINE_INPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE (() ->
            new PositiveIntegerValidator(
                ValidationScope.MAPPING, "MapAction index must exists and be greater than or equal to zero (0), but was '%s'")
        ),

        MAP_INPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Source field must not be null")
        ),
        MAP_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Source field must not be empty")
        ),
        MAP_OUTPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Target field should not be null")
        ),
        MAP_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Target field should not be empty")
        ),

        SEPARATE_INPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Source field must not be null")
        ),
        SEPARATE_INPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Source field must not be null")
        ),
        SEPARATE_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Source field must not be empty")
        ),
        SEPARATE_OUTPUT_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Target field should not be null")
        ),
        SEPARATE_OUTPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator(ValidationScope.MAPPING, "Target fields should not be null")
        ),
        SEPARATE_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Target fields should not be empty")
        ),
        SEPARATE_OUTPUT_FIELD_FIELD_ACTION_NOT_EMPTY (() ->
            new NotEmptyValidator(ValidationScope.MAPPING, "Field actions cannot be null or empty")
        ),
        SEPARATE_OUTPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE (() ->
            new PositiveIntegerValidator(ValidationScope.MAPPING, "MapAction index must exists and be greater than or equal to zero (0), but was '%s'")
        ),

        LOOKUPTABLE_NAME_CHECK_FOR_DUPLICATE (() ->
            new LookupTableNameValidator("LookupTables contain duplicated LookupTable names '%s'.")
        );

        private final AtlasValidator validator;
        private Validators(Supplier<AtlasValidator> s) {
            validator = s.get();
        }

        public AtlasValidator get() {
            return validator;
        }
    }

    @Override
    public List<Validation> validateMapping(AtlasMapping mapping) {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping definition must not be null");
        }
        List<Validation> validations = new ArrayList<>();
        Validators.MAPPING_NAME.get().validate(mapping.getName(), validations, null);

        List<DataSource> dataSources = mapping.getDataSource();
        for (DataSource ds : dataSources) {
            switch (ds.getDataSourceType()) {
            case SOURCE:
                Validators.DATASOURCE_SOURCE_URI.get().validate(ds.getUri(), validations, ds.getId());
                break;
            case TARGET:
                Validators.DATASOURCE_TARGET_URI.get().validate(ds.getUri(), validations, ds.getId());
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown DataSource type '%s'", ds.getDataSourceType()));
            }
        }
        validateFieldMappings(mapping.getMappings(), mapping.getLookupTables(), validations);
        return validations;
    }

    private void validateFieldMappings(Mappings mappings, LookupTables lookupTables, List<Validation> validations) {
        Validators.MAPPINGS_NOT_NULL.get().validate(mappings, validations, null);
        if (mappings != null) {
            List<BaseMapping> fieldMappings = mappings.getMapping();
            if (fieldMappings != null && !fieldMappings.isEmpty()) {
                List<Mapping> mapFieldMappings = fieldMappings.stream()
                        .filter(p -> p.getMappingType() == MappingType.MAP).map(p -> (Mapping) p)
                        .collect(Collectors.toList());
                List<Mapping> combineFieldMappings = fieldMappings.stream()
                        .filter(p -> p.getMappingType() == MappingType.COMBINE).map(p -> (Mapping) p)
                        .collect(Collectors.toList());
                List<Mapping> separateFieldMappings = fieldMappings.stream()
                        .filter(p -> p.getMappingType() == MappingType.SEPARATE).map(p -> (Mapping) p)
                        .collect(Collectors.toList());
                List<Mapping> lookupFieldMappings = fieldMappings.stream()
                        .filter(p -> p.getMappingType() == MappingType.LOOKUP).map(p -> (Mapping) p)
                        .collect(Collectors.toList());
                Set<String> usedIds = new HashSet<>();
                validateMapMapping(mapFieldMappings, validations, usedIds);
                validateCombineMapping(combineFieldMappings, validations, usedIds);
                validateSeparateMapping(separateFieldMappings, validations, usedIds);
                validateLookupTables(lookupFieldMappings, lookupTables, validations, usedIds);
            }
        }
    }

    private void validateLookupTables(List<Mapping> lookupFieldMappings, LookupTables lookupTables,
            List<Validation> validations, Set<String> usedIds) {
        if (lookupTables != null && lookupTables.getLookupTable() != null && !lookupTables.getLookupTable().isEmpty()) {
            // check for duplicate names
            Validators.LOOKUPTABLE_NAME_CHECK_FOR_DUPLICATE.get().validate(lookupTables, validations, null);
            if (lookupFieldMappings.isEmpty()) {
                Validation validation = new Validation();
                validation.setScope(ValidationScope.LOOKUP_TABLE);
                validation.setMessage("LookupTables are defined but no LookupFields are utilized.");
                validation.setStatus(ValidationStatus.WARN);
                validations.add(validation);
            } else {
                validateLookupFieldMapping(lookupFieldMappings, lookupTables, validations, usedIds);
            }
        }
    }

    // mapping field validations
    private void validateLookupFieldMapping(List<Mapping> fieldMappings, LookupTables lookupTables,
            List<Validation> validations, Set<String> usedIds) {
        Set<String> lookupFieldMappingTableNameRefs = fieldMappings.stream().map(Mapping::getLookupTableName)
                .collect(Collectors.toSet());

        Set<String> tableNames = lookupTables.getLookupTable().stream().map(LookupTable::getName)
                .collect(Collectors.toSet());

        if (!lookupFieldMappingTableNameRefs.isEmpty() && !tableNames.isEmpty()) {
            Set<String> disjoint = Stream.concat(lookupFieldMappingTableNameRefs.stream(), tableNames.stream())
                    .collect(Collectors.toMap(Function.identity(), t -> true, (a, b) -> null)).keySet();
            if (!disjoint.isEmpty()) {

                boolean isInFieldList = !lookupFieldMappingTableNameRefs.stream().filter(disjoint::contains)
                        .collect(Collectors.toList()).isEmpty();
                boolean isInTableNameList = !tableNames.stream().filter(disjoint::contains).collect(Collectors.toList())
                        .isEmpty();
                // which list has the disjoin.... if its the lookup fields then ERROR
                if (isInFieldList) {
                    Validation validation = new Validation();
                    validation.setScope(ValidationScope.LOOKUP_TABLE);
                    validation.setMessage(
                            "One ore more LookupFieldMapping references a non existent LookupTable name in the mapping: " + disjoint.toString());
                    validation.setStatus(ValidationStatus.ERROR);
                    validations.add(validation);
                }

                // check that if a name exists in table names that at least one field mapping
                // uses it, else WARN
                if (isInTableNameList) {
                    Validation validation = new Validation();
                    validation.setScope(ValidationScope.LOOKUP_TABLE);
                    validation.setMessage("A LookupTable is defined but not used by any LookupField: " + disjoint.toString());
                    validation.setStatus(ValidationStatus.WARN);
                    validations.add(validation);
                }
            }
        }

        for (Mapping fieldMapping : fieldMappings) {
            String mappingId = fieldMapping.getId();
            validateMappingId(mappingId, usedIds, validations);
            if (fieldMapping.getInputField() != null) {
                Validators.MAP_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations, mappingId);
            }
            Validators.MAP_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    mappingId, ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                Validators.MAP_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                        mappingId, ValidationStatus.WARN);
            }
        }

    }

    private void validateMapMapping(List<Mapping> fieldMappings, List<Validation> validations, Set<String> usedIds) {
        for (Mapping fieldMapping : fieldMappings) {
            String mappingId = fieldMapping.getId();
            FieldGroup sourceFieldGroup = fieldMapping.getInputFieldGroup();
            List<Field> sourceFields = sourceFieldGroup != null ? sourceFieldGroup.getField() : fieldMapping.getInputField();
            validateMappingId(mappingId, usedIds, validations);
            Validators.MAP_INPUT_NOT_NULL.get().validate(sourceFields, validations, mappingId);
            if (fieldMapping.getInputField() != null) {
                Validators.MAP_INPUT_FIELD_NOT_EMPTY.get().validate(sourceFields, validations, mappingId);
            }
            Validators.MAP_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    mappingId, ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                Validators.MAP_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                        mappingId, ValidationStatus.WARN);
            }
        }
    }

    private void validateSeparateMapping(List<Mapping> fieldMappings, List<Validation> validations, Set<String> usedIds) {
        for (Mapping fieldMapping : fieldMappings) {
            String mappingId = fieldMapping.getId();
            validateMappingId(mappingId, usedIds, validations);
            Validators.SEPARATE_INPUT_NOT_NULL.get().validate(fieldMapping.getInputField(), validations, mappingId);
            if (fieldMapping.getInputField() != null) {
                Validators.SEPARATE_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations, mappingId);
                // source must be a String type
            }

            Validators.SEPARATE_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    mappingId, ValidationStatus.WARN);
            Validators.SEPARATE_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                    mappingId, ValidationStatus.WARN);

            if (fieldMapping.getOutputField() != null) {
                for (Field field : fieldMapping.getOutputField()) {
                    Validators.SEPARATE_OUTPUT_FIELD_NOT_NULL.get().validate(field, validations, mappingId);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        Validators.SEPARATE_OUTPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE.get().validate(field.getIndex(),
                                validations, mappingId);
                    }
                }
            }
        }
    }

    private void validateCombineMapping(List<Mapping> fieldMappings, List<Validation> validations, Set<String> usedIds) {
        for (Mapping fieldMapping : fieldMappings) {
            String mappingId = fieldMapping.getId();
            validateMappingId(mappingId, usedIds, validations);
            Validators.COMBINE_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations, mappingId);
            if (fieldMapping.getOutputField() != null) {
                Validators.COMBINE_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations, mappingId);
                // source must be a String type
            }

            Validators.COMBINE_INPUT_NOT_NULL.get().validate(fieldMapping.getInputField(), validations,
                    mappingId, ValidationStatus.WARN);
            Validators.COMBINE_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations,
                    mappingId, ValidationStatus.WARN);

            if (fieldMapping.getInputField() != null) {
                for (Field field : fieldMapping.getInputField()) {
                    Validators.COMBINE_INPUT_FIELD_NOT_NULL.get().validate(field, validations, mappingId);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        Validators.COMBINE_INPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE.get().validate(field.getIndex(),
                                validations, mappingId);
                    }
                }
            }
        }
    }

    private void validateMappingId(String id, Set<String> usedIds, List<Validation> validations) {
        if (id == null) {
            return;
        }
        if (usedIds.contains(id)) {
            Validation validation = new Validation();
            validation.setScope(ValidationScope.MAPPING);
            validation.setMessage(String.format("Duplicated mapping ID '%s' is found", id));
            validation.setStatus(ValidationStatus.WARN);
            validations.add(validation);
        } else {
            usedIds.add(id);
        }
    }

}
