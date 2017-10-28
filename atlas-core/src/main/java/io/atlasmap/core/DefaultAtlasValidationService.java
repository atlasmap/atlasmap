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
package io.atlasmap.core;

import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.spi.AtlasValidator;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.LookupTables;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.CompositeValidator;
import io.atlasmap.validators.LookupTableNameValidator;
import io.atlasmap.validators.NonNullValidator;
import io.atlasmap.validators.NotEmptyValidator;
import io.atlasmap.validators.PositiveIntegerValidator;
import io.atlasmap.validators.StringPatternValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultAtlasValidationService implements AtlasValidationService {

    enum Validators {
        MAPPING_NAME (() -> {
            StringPatternValidator namePattern = new StringPatternValidator(
                "Mapping.Name",
                "Mapping name must not contain spaces nor special characters other than period (.) and underscore (_)",
                "[^A-Za-z0-9_.]");
            NonNullValidator nameNotNull = new NonNullValidator(
                "Mapping.Name", "Mapping name must not be null nor empty");
            return new CompositeValidator(namePattern, nameNotNull);
        }),
        DATASOURCE_TARGET_URI (() ->
            new NonNullValidator("DataSource.target.uri", "DataSource target uri must not be null nor empty")
        ),
        DATASOURCE_SOURCE_URI (() ->
            new NonNullValidator("DataSource.source.uri", "DataSource source uri must not be null nor empty")
        ),
        FIELD_NAMES_NOT_EMPTY (() ->
            new NotEmptyValidator("Field.Mappings", "Field mappings should not be empty")
        ),
        FIELD_NAMES_NOT_NULL (() ->
            new NonNullValidator("Field.Mappings", "Field mappings must not be null")
        ),

        COMBINE_INPUT_NOT_NULL (() ->
            new NonNullValidator("CombineFieldMapping.Input", "Input element should not be null")
        ),
        COMBINE_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator("CombineFieldMapping.Input.Field", "Input field element should not be empty")
        ),
        COMBINE_OUTPUT_NOT_NULL (() ->
            new NonNullValidator("CombineFieldMapping.Output", "Output element must not be null")
        ),
        COMBINE_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator("CombineFieldMapping.Output.Field", "Output field element must not be empty")
        ),
        COMBINE_INPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator(
                "CombineFieldMapping.Input.Fields", "Input field elements should not be null")
        ),
        COMBINE_INPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE (() ->
            new PositiveIntegerValidator(
                "CombineFieldMapping.Input.Fields.FieldActions.MapAction.Index",
                "MapAction index must exists and be greater than or equal to zero (0)")
        ),

        MAP_INPUT_NOT_NULL (() ->
            new NonNullValidator("MapFieldMapping.Input", "Input element must not be null")
        ),
        MAP_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator("MapFieldMapping.Input.Field", "Input field element must not be empty")
        ),
        MAP_OUTPUT_NOT_NULL (() ->
            new NonNullValidator("MapFieldMapping.Output", "Output element should not be null")
        ),
        MAP_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator("MapFieldMapping.Output.Field", "Output field element should not be empty")
        ),

        SEPARATE_INPUT_NOT_NULL (() ->
            new NonNullValidator("SeparateFieldMapping.Input", "Input element must not be null")
        ),
        SEPARATE_INPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator("SeparateFieldMapping.Input.Field", "Input field element must not be null")
        ),
        SEPARATE_INPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator("SeparateFieldMapping.Input.Field", "Input field element must not be empty")
        ),
        SEPARATE_OUTPUT_NOT_NULL (() ->
            new NonNullValidator("SeparateFieldMapping.Output", "Output element should not be null")
        ),
        SEPARATE_OUTPUT_FIELD_NOT_NULL (() ->
            new NonNullValidator(
                "SeparateFieldMapping.Output.Fields", "Output field elements should not be null")
        ),
        SEPARATE_OUTPUT_FIELD_NOT_EMPTY (() ->
            new NotEmptyValidator(
                "SeparateFieldMapping.Output.Fields", "Output field elements should not be empty")
        ),
        SEPARATE_OUTPUT_FIELD_FIELD_ACTION_NOT_EMPTY (() ->
            new NotEmptyValidator(
                "SeparateFieldMapping.Output.FieldActions", "Field actions cannot be null or empty")
        ),
        SEPARATE_OUTPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE (() ->
            new PositiveIntegerValidator(
                "SeparateFieldMapping.Output.Fields.FieldActions.MapAction.Index",
                "MapAction index must exists and be greater than or equal to zero (0)")
        ),

        LOOKUPTABLE_NAME_CHECK_FOR_DUPLICATE (() ->
            new LookupTableNameValidator(
                "lookuptables.lookuptable.name",
                "LookupTables contain duplicated LookupTable names.")
        );

        private final AtlasValidator validator;
        private Validators(Supplier<AtlasValidator> s) {
            validator = s.get();
        }

        public AtlasValidator get() {
            return validator;
        };
    }

    @Override
    public List<Validation> validateMapping(AtlasMapping mapping) {
        List<Validation> validations = new ArrayList<Validation>();
        Validators.MAPPING_NAME.get().validate(mapping.getName(), validations);

        List<DataSource> dataSources = mapping.getDataSource();
        for (DataSource ds : dataSources) {
            switch (ds.getDataSourceType()) {
            case SOURCE:
                Validators.DATASOURCE_SOURCE_URI.get().validate(ds.getUri(), validations);
                break;
            case TARGET:
                Validators.DATASOURCE_TARGET_URI.get().validate(ds.getUri(), validations);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown DataSource type '%s'", ds.getDataSourceType()));
            }
        }
        validateFieldMappings(mapping.getMappings(), mapping.getLookupTables(), validations);
        return validations;
    }

    private void validateFieldMappings(Mappings mappings, LookupTables lookupTables, List<Validation> validations) {
        Validators.FIELD_NAMES_NOT_NULL.get().validate(mappings, validations);
        if (mappings != null) {
            Validators.FIELD_NAMES_NOT_EMPTY.get().validate(mappings, validations, ValidationStatus.WARN);

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
                validateMapMapping(mapFieldMappings, validations);
                validateCombineMapping(combineFieldMappings, validations);
                validateSeparateMapping(separateFieldMappings, validations);
                validateLookupTables(lookupFieldMappings, lookupTables, validations);
            }
        }
    }

    private void validateLookupTables(List<Mapping> lookupFieldMappings, LookupTables lookupTables,
            List<Validation> validations) {
        if (lookupTables != null && lookupTables.getLookupTable() != null && !lookupTables.getLookupTable().isEmpty()) {
            // check for duplicate names
            Validators.LOOKUPTABLE_NAME_CHECK_FOR_DUPLICATE.get().validate(lookupTables, validations);
            if (lookupFieldMappings.isEmpty()) {
                Validation validation = new Validation();
                validation.setField("lookup.fields.missing");
                validation.setMessage("LookupTables are defined but no LookupFields are utilized.");
                validation.setStatus(ValidationStatus.WARN);
                validations.add(validation);
            } else {
                validateLookupFieldMapping(lookupFieldMappings, lookupTables, validations);
            }
        }
    }

    // mapping field validations
    private void validateLookupFieldMapping(List<Mapping> fieldMappings, LookupTables lookupTables,
            List<Validation> validations) {
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
                    validation.setField("lookupfield.tablename");
                    validation.setMessage(
                            "One ore more LookupFieldMapping references a non existent LookupTable name in the mapping");
                    validation.setStatus(ValidationStatus.ERROR);
                    validation.setValue(disjoint.toString());
                    validations.add(validation);
                }

                // check that if a name exists in table names that at least one field mapping
                // uses it, else WARN
                if (isInTableNameList) {
                    Validation validation = new Validation();
                    validation.setField("lookupfield.tablename");
                    validation.setMessage("A LookupTable is defined but not used by any LookupField");
                    validation.setStatus(ValidationStatus.WARN);
                    validation.setValue(disjoint.toString());
                    validations.add(validation);
                }
            }
        }

        for (Mapping fieldMapping : fieldMappings) {
            if (fieldMapping.getInputField() != null) {
                Validators.MAP_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations);
            }
            Validators.MAP_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                Validators.MAP_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                        ValidationStatus.WARN);
            }
        }

    }

    private void validateMapMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            Validators.MAP_INPUT_NOT_NULL.get().validate(fieldMapping.getInputField(), validations);
            if (fieldMapping.getInputField() != null) {
                Validators.MAP_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations);
            }
            Validators.MAP_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                Validators.MAP_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                        ValidationStatus.WARN);
            }
        }
    }

    private void validateSeparateMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            Validators.SEPARATE_INPUT_NOT_NULL.get().validate(fieldMapping.getInputField(), validations);
            if (fieldMapping.getInputField() != null) {
                Validators.SEPARATE_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations);
                // source must be a String type
            }

            Validators.SEPARATE_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            Validators.SEPARATE_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);

            if (fieldMapping.getOutputField() != null) {
                for (Field field : fieldMapping.getOutputField()) {
                    Validators.SEPARATE_OUTPUT_FIELD_NOT_NULL.get().validate(field, validations);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        Validators.SEPARATE_OUTPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE.get().validate(field.getIndex(),
                                validations);
                    }
                }
            }
        }
    }

    private void validateCombineMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            Validators.COMBINE_OUTPUT_NOT_NULL.get().validate(fieldMapping.getOutputField(), validations);
            if (fieldMapping.getOutputField() != null) {
                Validators.COMBINE_OUTPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getOutputField(), validations);
                // source must be a String type
            }

            Validators.COMBINE_INPUT_NOT_NULL.get().validate(fieldMapping.getInputField(), validations,
                    ValidationStatus.WARN);
            Validators.COMBINE_INPUT_FIELD_NOT_EMPTY.get().validate(fieldMapping.getInputField(), validations,
                    ValidationStatus.WARN);

            if (fieldMapping.getInputField() != null) {
                for (Field field : fieldMapping.getInputField()) {
                    Validators.COMBINE_INPUT_FIELD_NOT_NULL.get().validate(field, validations);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        Validators.COMBINE_INPUT_FIELD_FIELD_ACTION_INDEX_POSITIVE.get().validate(field.getIndex(),
                                validations);
                    }
                }
            }
        }
    }
}
