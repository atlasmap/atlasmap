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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultAtlasValidationService implements AtlasValidationService {

    private static Map<String, AtlasValidator> validatorMap = new HashMap<>();

    public DefaultAtlasValidationService() {
        init();
    }

    public void init() {
        NonNullValidator sourceURINotNullOrEmptyValidator = new NonNullValidator("DataSource.source.uri",
                "DataSource source uri must not be null nor empty");
        NonNullValidator targetURINotNullOrEmptyValidator = new NonNullValidator("DataSource.target.uri",
                "DataSource target uri must not be null nor empty");
        StringPatternValidator namePatternValidator = new StringPatternValidator("Mapping.Name",
                "Mapping name must not contain spaces nor special characters other than period (.) and underscore (_)",
                "[^A-Za-z0-9_.]");
        NonNullValidator nameNotNullOrEmptyValidator = new NonNullValidator("Mapping.Name",
                "Mapping name must not be null nor empty");
        CompositeValidator nameValidator = new CompositeValidator(nameNotNullOrEmptyValidator, namePatternValidator);
        NonNullValidator fieldNamesNotNullValidator = new NonNullValidator("Field.Mappings",
                "Field mappings must not be null");
        NotEmptyValidator fieldNamesNotEmptyValidator = new NotEmptyValidator("Field.Mappings",
                "Field mappings should not be empty");
        NonNullValidator inputNonNullValidator = new NonNullValidator("MapFieldMapping.Input",
                "Input element must not be null");
        NonNullValidator inputFieldNonNullValidator = new NonNullValidator("MapFieldMapping.Input.Field",
                "Input field element must not be null");
        NonNullValidator outputNonNullValidator = new NonNullValidator("MapFieldMapping.Output",
                "Output element should not be null");
        NonNullValidator outputFieldNonNullValidator = new NonNullValidator("MapFieldMapping.Output.Field",
                "Output field element should not be null");
        NonNullValidator separateInputNonNullValidator = new NonNullValidator("SeparateFieldMapping.Input",
                "Input element must not be null");
        NonNullValidator separateInputFieldNonNullValidator = new NonNullValidator("SeparateFieldMapping.Input.Field",
                "Input field element must not be null");

        NonNullValidator separateOutputNonNullValidator = new NonNullValidator("SeparateFieldMapping.Output",
                "Output element should not be null");
        NotEmptyValidator separateOutputNotEmptyValidator = new NotEmptyValidator("SeparateFieldMapping.Output.Fields",
                "Output elements should not be empty");
        NonNullValidator separateOutputFieldNonNullValidator = new NonNullValidator(
                "SeparateFieldMapping.Output.FieldActions", "Output field actions cannot not be null");
        NotEmptyValidator separateOutputNotEmptyFieldActionValidator = new NotEmptyValidator(
                "SeparateFieldMapping.Output.Fields.FieldActions", "Field actions cannot be null or empty");
        PositiveIntegerValidator separateOutputMapActionPositiveIntegerValidator = new PositiveIntegerValidator(
                "SeparateFieldMapping.Output.Fields.FieldActions.MapAction.Index",
                "MapAction index must exists and be greater than or equal to zero (0)");

        LookupTableNameValidator lookupTableNameValidator = new LookupTableNameValidator(
                "lookuptables.lookuptable.name", "LookupTables contain duplicated LookupTable names.");

        validatorMap.put("datasource.source.uri", sourceURINotNullOrEmptyValidator);
        validatorMap.put("datasource.target.uri", targetURINotNullOrEmptyValidator);
        validatorMap.put("mapping.name", nameValidator);
        validatorMap.put("field.names.not.null", fieldNamesNotNullValidator);
        validatorMap.put("field.names.not.empty", fieldNamesNotEmptyValidator);
        validatorMap.put("input.not.null", inputNonNullValidator);
        validatorMap.put("input.field.not.null", inputFieldNonNullValidator);
        validatorMap.put("output.not.null", outputNonNullValidator);
        validatorMap.put("output.field.not.null", outputFieldNonNullValidator);

        validatorMap.put("separate.input.not.null", separateInputNonNullValidator);
        validatorMap.put("separate.input.field.not.null", separateInputFieldNonNullValidator);
        validatorMap.put("separate.output.not.null", separateOutputNonNullValidator);
        validatorMap.put("separate.output.not.empty", separateOutputNotEmptyValidator);
        validatorMap.put("separate.output.field.not.null", separateOutputFieldNonNullValidator);
        validatorMap.put("separate.output.field.field.action.not.empty", separateOutputNotEmptyFieldActionValidator);
        validatorMap.put("separate.output.field.field.action.index.positive",
                separateOutputMapActionPositiveIntegerValidator);
        validatorMap.put("lookuptable.name.check.for.duplicate", lookupTableNameValidator);
    }

    public void destroy() {
        validatorMap.clear();
    }

    @Override
    public List<Validation> validateMapping(AtlasMapping mapping) {
        List<Validation> validations = new ArrayList<Validation>();
        validatorMap.get("mapping.name").validate(mapping.getName(), validations);

        List<DataSource> dataSources = mapping.getDataSource();
        for (DataSource ds : dataSources) {
            switch (ds.getDataSourceType()) {
            case SOURCE:
                validatorMap.get("datasource.source.uri").validate(ds.getUri(), validations);
                break;
            case TARGET:
                validatorMap.get("datasource.target.uri").validate(ds.getUri(), validations);
                break;
            }
        }
        validateFieldMappings(mapping.getMappings(), mapping.getLookupTables(), validations);
        return validations;
    }

    private void validateFieldMappings(Mappings mappings, LookupTables lookupTables, List<Validation> validations) {
        validatorMap.get("field.names.not.null").validate(mappings, validations);
        if (mappings != null) {
            validatorMap.get("field.names.not.empty").validate(mappings, validations, ValidationStatus.WARN);

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
            validatorMap.get("lookuptable.name.check.for.duplicate").validate(lookupTables, validations);
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
                validatorMap.get("input.field.not.null").validate(fieldMapping.getInputField(), validations);
            }
            validatorMap.get("output.not.null").validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                validatorMap.get("output.field.not.null").validate(fieldMapping.getOutputField(), validations,
                        ValidationStatus.WARN);
            }
        }

    }

    private void validateMapMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            validatorMap.get("input.not.null").validate(fieldMapping.getInputField(), validations);
            if (fieldMapping.getInputField() != null) {
                validatorMap.get("input.field.not.null").validate(fieldMapping.getInputField(), validations);
            }
            validatorMap.get("output.not.null").validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            if (fieldMapping.getOutputField() != null) {
                validatorMap.get("output.field.not.null").validate(fieldMapping.getOutputField(), validations,
                        ValidationStatus.WARN);
            }
        }
    }

    private void validateSeparateMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            validatorMap.get("separate.input.not.null").validate(fieldMapping.getInputField(), validations);
            if (fieldMapping.getInputField() != null) {
                validatorMap.get("separate.input.field.not.null").validate(fieldMapping.getInputField(), validations);
                // source must be a String type
            }

            validatorMap.get("separate.output.not.null").validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);
            validatorMap.get("separate.output.not.empty").validate(fieldMapping.getOutputField(), validations,
                    ValidationStatus.WARN);

            if (fieldMapping.getOutputField() != null) {
                for (Field field : fieldMapping.getOutputField()) {
                    validatorMap.get("separate.output.field.not.null").validate(field, validations);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        validatorMap.get("separate.output.field.field.action.index.positive").validate(field.getIndex(),
                                validations);
                    }
                }
            }
        }
    }

    private void validateCombineMapping(List<Mapping> fieldMappings, List<Validation> validations) {
        for (Mapping fieldMapping : fieldMappings) {
            validatorMap.get("combine.output.not.null").validate(fieldMapping.getOutputField(), validations);
            if (fieldMapping.getOutputField() != null) {
                validatorMap.get("combine.putput.field.not.null").validate(fieldMapping.getOutputField(), validations);
                // source must be a String type
            }

            validatorMap.get("combine.input.not.null").validate(fieldMapping.getInputField(), validations,
                    ValidationStatus.WARN);
            validatorMap.get("combine.input.not.empty").validate(fieldMapping.getInputField(), validations,
                    ValidationStatus.WARN);

            if (fieldMapping.getInputField() != null) {
                for (Field field : fieldMapping.getInputField()) {
                    validatorMap.get("combine.output.field.not.null").validate(field, validations);
                    if (field.getIndex() == null || field.getIndex() < 0) {
                        validatorMap.get("combine.output.field.field.action.index.positive").validate(field.getIndex(),
                                validations);
                    }
                }
            }
        }
    }
}
