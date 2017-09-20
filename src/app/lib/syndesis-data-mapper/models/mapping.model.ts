/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import { Field } from './field.model';
import { TransitionModel, TransitionMode, FieldAction, FieldActionConfig, FieldActionArgumentValue } from './transition.model';
import { DocumentDefinition } from '../models/document.definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { DataMapperUtil } from '../common/data.mapper.util';

export class MappedFieldParsingData {
    public parsedName: string = null;
    public parsedPath: string = null;
    public parsedValue: string = null;
    public parsedDocID: string = null;
    public parsedDocURI: string = null;
    public parsedIndex: string = null;
    public fieldIsProperty: boolean = false;
    public fieldIsConstant: boolean = false;
    public parsedValueType: string = null;
    public parsedActions: FieldAction[] = [];
    public userCreated: boolean = false;
}

export class MappedField {
    public parsedData: MappedFieldParsingData = new MappedFieldParsingData();
    public field: Field = DocumentDefinition.getNoneField();
    public actions: FieldAction[] = [];

    public updateSeparateOrCombineIndex(separateMode: boolean, combineMode: boolean,
        suggestedValue: string, isSource:boolean): void {

        //remove field action when neither combine or separate mode
        var removeField: boolean = (!separateMode && !combineMode);
        //remove field when combine and field is target
        removeField = removeField || (combineMode && !isSource);
        //remove field when separate and field is source
        removeField = removeField || (separateMode && isSource);
        if (removeField) {
            this.removeSeparateOrCombineAction();
            return;
        }

        var firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
        if (firstFieldAction == null || !firstFieldAction.isSeparateOrCombineMode) {
            //add new separate/combine field action when there isn't one
            firstFieldAction = FieldAction.createSeparateCombineFieldAction(separateMode, suggestedValue);
            this.actions = [firstFieldAction].concat(this.actions);
        }
    }

    public removeSeparateOrCombineAction(): void {
        var firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
        if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
            DataMapperUtil.removeItemFromArray(firstFieldAction, this.actions);
        }
    }

    public getSeparateOrCombineIndex(): string {
        var firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
        if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
            return firstFieldAction.argumentValues[0].value;
        }
        return null;
    }

    public removeAction(action: FieldAction): void {
        DataMapperUtil.removeItemFromArray(action, this.actions);
    }

    public static sortMappedFieldsByPath(mappedFields: MappedField[], allowNone: boolean): MappedField[] {
        if (mappedFields == null || mappedFields.length == 0) {
            return [];
        }
        var fieldsByPath: { [key:string]:MappedField; } = {};
        var fieldPaths: string[] = [];
        for (let mappedField of mappedFields) {
            if (mappedField == null || mappedField.field == null) {
                continue;
            }
            if (!allowNone && mappedField.field == DocumentDefinition.getNoneField()) {
                continue;
            }
            var path: string = mappedField.field.path;
            fieldsByPath[path] = mappedField;
            fieldPaths.push(path);
        }
        fieldPaths.sort();
        var result: MappedField[] = [];
        for (let name of fieldPaths) {
            result.push(fieldsByPath[name]);
        }
        return result;
    }

    public isMapped(): boolean {
        return (this.field != null) && (this.field != DocumentDefinition.getNoneField());
    }
}

export class FieldMappingPair {
    public sourceFields: MappedField[] = [new MappedField()];
    public targetFields: MappedField[] = [new MappedField()];
    public transition: TransitionModel = new TransitionModel();

    public constructor() { }

    public addField(field: Field, isSource: boolean): void {
        var mappedField: MappedField = new MappedField();
        mappedField.field = field;
        this.getMappedFields(isSource).push(mappedField);
    }

    public hasMappedField(isSource: boolean) {
        var mappedFields: MappedField[] = isSource ? this.sourceFields : this.targetFields;
        for (let mappedField of mappedFields) {
            if (mappedField.isMapped()) {
                return true;
            }
        }
        return false;
    }

    public isFullyMapped(): boolean {
        return this.hasMappedField(true) && this.hasMappedField(false);
    }

    public addMappedField(mappedField: MappedField, isSource: boolean): void {
        this.getMappedFields(isSource).push(mappedField);
    }

    public removeMappedField(mappedField: MappedField, isSource: boolean): void {
        DataMapperUtil.removeItemFromArray(mappedField, this.getMappedFields(isSource));
    }

    public getMappedFieldForField(field: Field, isSource: boolean): MappedField {
        for (let mappedField of this.getMappedFields(isSource)) {
            if (mappedField.field == field) {
                return mappedField;
            }
        }
        return null;
    }

    public getMappedFields(isSource: boolean): MappedField[] {
        return isSource ? this.sourceFields : this.targetFields;
    }

    public getLastMappedField(isSource: boolean): MappedField {
        var fields: MappedField[] = this.getMappedFields(isSource);
        if ((fields != null) && (fields.length > 0)) {
            return fields[fields.length - 1];
        }
        return null;
    }

    public getFields(isSource: boolean): Field[] {
        var fields: Field[] = [];
        for (let mappedField of this.getMappedFields(isSource)) {
            if (mappedField.field != null) {
                fields.push(mappedField.field);
            }
        }
        return fields;
    }

    public getFieldNames(isSource: boolean): string[] {
        var fields: Field[] = this.getFields(isSource);
        Field.alphabetizeFields(fields);
        var names: string[] = [];
        for (let field of fields) {
            if (field == DocumentDefinition.getNoneField()) {
                continue;
            }
            names.push(field.name);
        }
        return names;
    }

    public getFieldPaths(isSource: boolean): string[] {
        var fields: Field[] = this.getFields(isSource);
        Field.alphabetizeFields(fields);
        var paths: string[] = [];
        for (let field of fields) {
            if (field == DocumentDefinition.getNoneField()) {
                continue;
            }
            paths.push(field.path);
        }
        return paths;
    }

    public hasFieldActions(): boolean {
        for (let mappedField of this.getAllMappedFields()) {
            if (mappedField.actions.length > 0) {
                return true;
            }
        }
        return false;
    }

    public getAllFields(): Field[] {
        return this.getFields(true).concat(this.getFields(false));
    }

    public getAllMappedFields(): MappedField[] {
        return this.getMappedFields(true).concat(this.getMappedFields(false));
    }

    public isFieldMapped(field: Field): boolean {
        return this.getMappedFieldForField(field, field.isSource()) != null;
    }

    public hasTransition(): boolean {
        var mappedFields: MappedField[] = this.getAllMappedFields();
        for (let mappedField of mappedFields) {
            if (mappedField.actions.length > 0) {
                return true;
            }
        }
        return false;
    }

    public updateTransition(): void {
        for (let field of this.getAllFields()) {
            if (field.enumeration) {
                this.transition.mode = TransitionMode.ENUM;
                break;
            }
        }

        var mappedFields: MappedField[] = this.getMappedFields(false);
        for (let mappedField of mappedFields) {
            var actionsToRemove: FieldAction[] = [];
            for (let action of mappedField.actions) {
                var actionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(action.name);
                if (actionConfig != null && !actionConfig.appliesToField(mappedField.field, this)) {
                    actionsToRemove.push(action);
                }
            }
            for (let action of actionsToRemove) {
                mappedField.removeAction(action);
            }
        }

        var separateMode: boolean = (this.transition.mode == TransitionMode.SEPARATE);
        var combineMode: boolean = (this.transition.mode == TransitionMode.COMBINE);

        if (separateMode || combineMode) {
            var isSource: boolean = combineMode;
            mappedFields = this.getMappedFields(isSource);
            //remove indexes from targets in combine mode, from sources in seperate mode
            for (let mappedField of this.getMappedFields(!isSource)) {
                mappedField.removeSeparateOrCombineAction();
            }
            //find max seperator index from existing fields
            var maxIndex: number = 0;
            for (let mappedField of mappedFields) {
                var index: string = mappedField.getSeparateOrCombineIndex();
                var indexAsNumber = (index == null) ? 0 : parseInt(index);
                maxIndex = Math.max(maxIndex, indexAsNumber);
            }

            maxIndex += 1; //we want our next index to be one larger than previously found indexes
            for (let mappedField of mappedFields) {
                mappedField.updateSeparateOrCombineIndex(separateMode, combineMode, maxIndex.toString(), isSource);
                //see if this field used the new index, if so, increment
                var index: string = mappedField.getSeparateOrCombineIndex();
                if (index == maxIndex.toString()) {
                    maxIndex += 1;
                }
            }
        } else { //not separate mode
            for (let mappedField of this.getAllMappedFields()) {
                mappedField.removeSeparateOrCombineAction();
            }
        }
    }
}

export class MappingModel {
    public uuid: string;
    public fieldMappings: FieldMappingPair[] = [];
    public currentFieldMapping: FieldMappingPair = null;
    public validationErrors: ErrorInfo[] = [];
    public brandNewMapping: boolean = true;

    public constructor() {
        this.uuid = "mapping." + Math.floor((Math.random() * 1000000) + 1).toString();
        this.fieldMappings.push(new FieldMappingPair());
    }

    public getFirstFieldMapping(): FieldMappingPair {
        if (this.fieldMappings == null || (this.fieldMappings.length == 0)) {
            return null;
        }
        return this.fieldMappings[0];
    }

    public getLastFieldMapping(): FieldMappingPair {
        if (this.fieldMappings == null || (this.fieldMappings.length == 0)) {
            return null;
        }
        return this.fieldMappings[this.fieldMappings.length - 1];
    }

    public getCurrentFieldMapping(): FieldMappingPair {
        return (this.currentFieldMapping == null) ? this.getLastFieldMapping() : this.currentFieldMapping;
    }

    public addValidationError(message: string) {
        const e = new ErrorInfo(message, ErrorLevel.VALIDATION_ERROR);
        this.validationErrors.push(e);
    }

    public clearValidationErrors(): void {
        this.validationErrors = [];
    }

    public removeError(identifier: string) {
        for (var i = 0; i < this.validationErrors.length; i++) {
            if (this.validationErrors[i].identifier == identifier) {
                this.validationErrors.splice(i, 1);
                return;
            }
        }
    }

    public getFirstCollectionField(isSource: boolean): Field {
        for (let f of this.getFields(isSource)) {
            if (f.isInCollection()) {
                return f;
            }
        }
        return null;
    }

    public isCollectionMode(): boolean {
        return (this.getFirstCollectionField(true) != null)
            || (this.getFirstCollectionField(false) != null);
    }

    public isLookupMode(): boolean {
        for (let f of this.getAllFields()) {
            if (f.enumeration) {
                return true;
            }
        }
        return false;
    }

    public removeMappedPair(fieldPair: FieldMappingPair): void {
        DataMapperUtil.removeItemFromArray(fieldPair, this.fieldMappings);
    }

    public getMappedFields(isSource: boolean): MappedField[] {
        var fields: MappedField[] = [];
        for (let fieldPair of this.fieldMappings) {
            fields = fields.concat(fieldPair.getMappedFields(isSource));
        }
        return fields;
    }

    public isFieldSelectable(field: Field): boolean {
        return this.getFieldSelectionExclusionReason(field) == null;
    }

    public getFieldSelectionExclusionReason(field: Field): string {
        if (this.brandNewMapping) { // if mapping hasnt had a field selected yet, allow it
            return null;
        }

        if (!field.isTerminal()) {
            return "field is a parent field";
        }

        var repeatedMode: boolean = this.isCollectionMode();
        var lookupMode: boolean = this.isLookupMode();
        var mapMode: boolean = false;
        var separateMode: boolean = false;
        var combineMode: boolean = false;

        if (!repeatedMode && !lookupMode) {
            for (let fieldPair of this.fieldMappings) {
                mapMode = mapMode || fieldPair.transition.isMapMode();
                separateMode = separateMode || fieldPair.transition.isSeparateMode();
                combineMode = combineMode || fieldPair.transition.isCombineMode();
            }
        }
        if (mapMode || separateMode || combineMode) {
            //repeated fields and enums are not selectable in these modes
            if (field.isInCollection()) {
                return "Repeated fields are not valid for this mapping";
            }
            if (field.enumeration) {
                return "Enumeration fields are not valid for this mapping";
            }

            //separate mode sources must be string
            if (separateMode && !field.isStringField() && field.isSource()) {
                return "source fields for this mapping must be type String"
            }
        } else if (lookupMode) {
            if (!field.enumeration) {
                return "only Enumeration fields are valid for this mapping";
            }
        } else if (repeatedMode) {
            //enumeration fields are not allowed in repeated mappings
            if (field.enumeration) {
                return "Enumeration fields are not valid for this mapping";
            }

            //if no fields for this isSource has been selected yet, everything is open to selection
            if (!this.hasMappedFields(field.isSource())) {
                return null;
            }

            var collectionField: Field = this.getFirstCollectionField(field.isSource());
            if (collectionField == null) {
                //only primitive fields (not in collections) are selectable
                if (field.isInCollection()) {
                    var fieldTypeDesc: string = field.isSource ? "source" : "target"
                    return fieldTypeDesc + " fields cannot be repeated fields for this mapping.";
                }
            } else { //collection field exists in this mapping for isSource
                var parentCollectionField: Field = collectionField.getCollectionParentField();
                //primitive fields are not selectable when collection field is already selected
                if (!field.isInCollection()) {
                    return "field is not selectable, it is not a child of " + parentCollectionField.displayName;
                }

                //children of collections are only selectable if this field is in the same collection
                if (field.getCollectionParentField() != parentCollectionField) {
                    return "field is not selectable, it is not a child of " + parentCollectionField.displayName;
                }
            }
        }
        return null;
    }

    public isFieldMapped(field:Field, isSource:boolean): boolean {
        return this.getFields(isSource).indexOf(field) != -1;
    }

    public getAllMappedFields(): MappedField[] {
        return this.getMappedFields(true).concat(this.getMappedFields(false));
    }

    public getAllFields(): Field[] {
        return this.getFields(true).concat(this.getFields(false));
    }

    public getFields(isSource: boolean): Field[] {
        var fields: Field[] = [];
        for (let fieldPair of this.fieldMappings) {
            fields = fields.concat(fieldPair.getFields(isSource));
        }
        return fields;
    }

    public hasMappedFields(isSource: boolean): boolean {
        for (let mappedField of this.getMappedFields(isSource)) {
            if (mappedField.isMapped()) {
                return true;
            }
        }
        return false;
    }

    public hasFullyMappedPair(): boolean {
        for (let pair of this.fieldMappings) {
            if (pair.isFullyMapped()) {
                return true;
            }
        }
        return false;
    }
}
