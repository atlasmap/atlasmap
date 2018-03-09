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
import { TransitionModel, TransitionMode, FieldAction, FieldActionConfig } from './transition.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { DataMapperUtil } from '../common/data-mapper-util';

export class MappedFieldParsingData {
  parsedName: string = null;
  parsedPath: string = null;
  parsedValue: string = null;
  parsedDocID: string = null;
  parsedDocURI: string = null;
  parsedIndex: string = null;
  fieldIsProperty = false;
  fieldIsConstant = false;
  parsedValueType: string = null;
  parsedActions: FieldAction[] = [];
  userCreated = false;
}

export class MappedField {
  parsedData: MappedFieldParsingData = new MappedFieldParsingData();
  field: Field = DocumentDefinition.getNoneField();
  actions: FieldAction[] = [];

  updateSeparateOrCombineIndex(
    separateMode: boolean, combineMode: boolean,
    suggestedValue: string, isSource: boolean): void {

    //remove field action when neither combine or separate mode
    let removeField: boolean = (!separateMode && !combineMode);
    //remove field when combine and field is target
    removeField = removeField || (combineMode && !isSource);
    //remove field when separate and field is source
    removeField = removeField || (separateMode && isSource);
    if (removeField) {
      this.removeSeparateOrCombineAction();
      return;
    }

    let firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction == null || !firstFieldAction.isSeparateOrCombineMode) {
      //add new separate/combine field action when there isn't one
      firstFieldAction = FieldAction.createSeparateCombineFieldAction(separateMode, suggestedValue);
      this.actions = [firstFieldAction].concat(this.actions);
    }
  }

  removeSeparateOrCombineAction(): void {
    const firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
      DataMapperUtil.removeItemFromArray(firstFieldAction, this.actions);
    }
  }

  getSeparateOrCombineIndex(): string {
    const firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
      return firstFieldAction.argumentValues[0].value;
    }
    return null;
  }

  removeAction(action: FieldAction): void {
    DataMapperUtil.removeItemFromArray(action, this.actions);
  }

  static sortMappedFieldsByPath(mappedFields: MappedField[], allowNone: boolean): MappedField[] {
    if (mappedFields == null || mappedFields.length == 0) {
      return [];
    }
    const fieldsByPath: { [key: string]: MappedField; } = {};
    const fieldPaths: string[] = [];
    for (const mappedField of mappedFields) {
      if (mappedField == null || mappedField.field == null) {
        continue;
      }
      if (!allowNone && mappedField.field == DocumentDefinition.getNoneField()) {
        continue;
      }
      const path: string = mappedField.field.path;
      fieldsByPath[path] = mappedField;
      fieldPaths.push(path);
    }
    fieldPaths.sort();
    const result: MappedField[] = [];
    for (const name of fieldPaths) {
      result.push(fieldsByPath[name]);
    }
    return result;
  }

  isMapped(): boolean {
    return (this.field != null) && (this.field != DocumentDefinition.getNoneField());
  }
}

export class FieldMappingPair {
  sourceFields: MappedField[] = [new MappedField()];
  targetFields: MappedField[] = [new MappedField()];
  transition: TransitionModel = new TransitionModel();

  constructor() {
    return;
  }

  addField(field: Field, isSource: boolean): void {
    const mappedField: MappedField = new MappedField();
    mappedField.field = field;
    this.getMappedFields(isSource).push(mappedField);
  }

  hasMappedField(isSource: boolean) {
    const mappedFields: MappedField[] = isSource ? this.sourceFields : this.targetFields;
    for (const mappedField of mappedFields) {
      if (mappedField.isMapped()) {
        return true;
      }
    }
    return false;
  }

  isFullyMapped(): boolean {
    return this.hasMappedField(true) && this.hasMappedField(false);
  }

  addMappedField(mappedField: MappedField, isSource: boolean): void {
    this.getMappedFields(isSource).push(mappedField);
  }

  removeMappedField(mappedField: MappedField, isSource: boolean): void {
    DataMapperUtil.removeItemFromArray(mappedField, this.getMappedFields(isSource));
  }

  getMappedFieldForField(field: Field, isSource: boolean): MappedField {
    for (const mappedField of this.getMappedFields(isSource)) {
      if (mappedField.field == field) {
        return mappedField;
      }
    }
    return null;
  }

  getMappedFields(isSource: boolean): MappedField[] {
    return isSource ? this.sourceFields : this.targetFields;
  }

  getLastMappedField(isSource: boolean): MappedField {
    const fields: MappedField[] = this.getMappedFields(isSource);
    if ((fields != null) && (fields.length > 0)) {
      return fields[fields.length - 1];
    }
    return null;
  }

  getFields(isSource: boolean): Field[] {
    const fields: Field[] = [];
    for (const mappedField of this.getMappedFields(isSource)) {
      if (mappedField.field != null) {
        fields.push(mappedField.field);
      }
    }
    return fields;
  }

  getFieldNames(isSource: boolean): string[] {
    const fields: Field[] = this.getFields(isSource);
    Field.alphabetizeFields(fields);
    const names: string[] = [];
    for (const field of fields) {
      if (field == DocumentDefinition.getNoneField()) {
        continue;
      }
      names.push(field.name);
    }
    return names;
  }

  getFieldPaths(isSource: boolean): string[] {
    const fields: Field[] = this.getFields(isSource);
    Field.alphabetizeFields(fields);
    const paths: string[] = [];
    for (const field of fields) {
      if (field == DocumentDefinition.getNoneField()) {
        continue;
      }
      paths.push(field.path);
    }
    return paths;
  }

  hasFieldActions(): boolean {
    for (const mappedField of this.getAllMappedFields()) {
      if (mappedField.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  getAllFields(): Field[] {
    return this.getFields(true).concat(this.getFields(false));
  }

  getAllMappedFields(): MappedField[] {
    return this.getMappedFields(true).concat(this.getMappedFields(false));
  }

  isFieldMapped(field: Field): boolean {
    return this.getMappedFieldForField(field, field.isSource()) != null;
  }

  hasTransition(): boolean {
    const mappedFields: MappedField[] = this.getAllMappedFields();
    for (const mappedField of mappedFields) {
      if (mappedField.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  updateTransition(): void {
    for (const field of this.getAllFields()) {
      if (field.enumeration) {
        this.transition.mode = TransitionMode.ENUM;
        break;
      }
    }

    let mappedFields: MappedField[] = this.getMappedFields(false);
    for (const mappedField of mappedFields) {
      const actionsToRemove: FieldAction[] = [];
      for (const action of mappedField.actions) {
        const actionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(action.name);
        if (actionConfig != null && !actionConfig.appliesToField(this)) {
          actionsToRemove.push(action);
        }
      }
      for (const action of actionsToRemove) {
        mappedField.removeAction(action);
      }
    }

    const separateMode: boolean = (this.transition.mode == TransitionMode.SEPARATE);
    const combineMode: boolean = (this.transition.mode == TransitionMode.COMBINE);

    if (separateMode || combineMode) {
      const isSource: boolean = combineMode;
      mappedFields = this.getMappedFields(isSource);
      //remove indexes from targets in combine mode, from sources in seperate mode
      for (const mappedField of this.getMappedFields(!isSource)) {
        mappedField.removeSeparateOrCombineAction();
      }
      //find max seperator index from existing fields
      let maxIndex = 0;
      for (const mappedField of mappedFields) {
        const index: string = mappedField.getSeparateOrCombineIndex();
        const indexAsNumber = (index == null) ? 0 : parseInt(index, 10);
        maxIndex = Math.max(maxIndex, indexAsNumber);
      }

      maxIndex += 1; //we want our next index to be one larger than previously found indexes
      for (const mappedField of mappedFields) {
        mappedField.updateSeparateOrCombineIndex(separateMode, combineMode, maxIndex.toString(), isSource);
        //see if this field used the new index, if so, increment
        const index: string = mappedField.getSeparateOrCombineIndex();
        if (index == maxIndex.toString()) {
          maxIndex += 1;
        }
      }
    } else { //not separate mode
      for (const mappedField of this.getAllMappedFields()) {
        mappedField.removeSeparateOrCombineAction();
      }
    }
  }
}

export class MappingModel {
  uuid: string;
  fieldMappings: FieldMappingPair[] = [];
  currentFieldMapping: FieldMappingPair = null;
  validationErrors: ErrorInfo[] = []; // must be immutable
  brandNewMapping = true;

  constructor() {
    this.uuid = 'mapping.' + Math.floor((Math.random() * 1000000) + 1).toString();
    this.fieldMappings.push(new FieldMappingPair());
    Object.freeze(this.validationErrors);
  }

  getFirstFieldMapping(): FieldMappingPair {
    if (this.fieldMappings == null || (this.fieldMappings.length == 0)) {
      return null;
    }
    return this.fieldMappings[0];
  }

  getLastFieldMapping(): FieldMappingPair {
    if (this.fieldMappings == null || (this.fieldMappings.length == 0)) {
      return null;
    }
    return this.fieldMappings[this.fieldMappings.length - 1];
  }

  getCurrentFieldMapping(): FieldMappingPair {
    return (this.currentFieldMapping == null) ? this.getLastFieldMapping() : this.currentFieldMapping;
  }

  addValidationError(message: string) {
    const e = new ErrorInfo(message, ErrorLevel.VALIDATION_ERROR);
    this.validationErrors = [...this.validationErrors, e];
    Object.freeze(this.validationErrors);
  }

  clearValidationErrors(): void {
    this.validationErrors = [];
    Object.freeze(this.validationErrors);
  }

  getValidationErrors(): ErrorInfo[] {
    return this.validationErrors.filter(e => e.level >= ErrorLevel.ERROR);
  }

  getValidationWarnings(): ErrorInfo[] {
    return this.validationErrors.filter(e => e.level == ErrorLevel.WARN);
  }

  removeError(identifier: string) {
    this.validationErrors = this.validationErrors.filter(e => e.identifier !== identifier);
    Object.freeze(this.validationErrors);
  }

  getFirstCollectionField(isSource: boolean): Field {
    for (const f of this.getFields(isSource)) {
      if (f.isInCollection()) {
        return f;
      }
    }
    return null;
  }

  isCollectionMode(): boolean {
    return (this.getFirstCollectionField(true) != null)
      || (this.getFirstCollectionField(false) != null);
  }

  isLookupMode(): boolean {
    for (const f of this.getAllFields()) {
      if (f.enumeration) {
        return true;
      }
    }
    return false;
  }

  removeMappedPair(fieldPair: FieldMappingPair): void {
    DataMapperUtil.removeItemFromArray(fieldPair, this.fieldMappings);
  }

  getMappedFields(isSource: boolean): MappedField[] {
    let fields: MappedField[] = [];
    for (const fieldPair of this.fieldMappings) {
      fields = fields.concat(fieldPair.getMappedFields(isSource));
    }
    return fields;
  }

  isFieldSelectable(field: Field): boolean {
    return this.getFieldSelectionExclusionReason(field) == null;
  }

  getFieldSelectionExclusionReason(field: Field): string {
    if (this.brandNewMapping) { // if mapping hasnt had a field selected yet, allow it
      return null;
    }

    if (!field.isTerminal()) {
      return 'field is a parent field';
    }

    const repeatedMode: boolean = this.isCollectionMode();
    const lookupMode: boolean = this.isLookupMode();
    let mapMode = false;
    let separateMode = false;
    let combineMode = false;

    if (!repeatedMode && !lookupMode) {
      for (const fieldPair of this.fieldMappings) {
        mapMode = mapMode || fieldPair.transition.isMapMode();
        separateMode = separateMode || fieldPair.transition.isSeparateMode();
        combineMode = combineMode || fieldPair.transition.isCombineMode();
      }
    }
    if (mapMode || separateMode || combineMode) {
      //enums are not selectable in these modes
      if (field.enumeration) {
        return 'Enumeration fields are not valid for this mapping';
      }

      //separate mode sources must be string
      if (separateMode && !field.isStringField() && field.isSource()) {
        return 'source fields for this mapping must be type String';
      }
    } else if (lookupMode) {
      if (!field.enumeration) {
        return 'only Enumeration fields are valid for this mapping';
      }
    } else if (repeatedMode) {
      //enumeration fields are not allowed in repeated mappings
      if (field.enumeration) {
        return 'Enumeration fields are not valid for this mapping';
      }

      //if no fields for this isSource has been selected yet, everything is open to selection
      if (!this.hasMappedFields(field.isSource())) {
        return null;
      }

      const collectionField: Field = this.getFirstCollectionField(field.isSource());
      if (collectionField == null) {
        //only primitive fields (not in collections) are selectable
        if (field.isInCollection()) {
          const fieldTypeDesc: string = field.isSource ? 'source' : 'target';
          return fieldTypeDesc + ' fields cannot be repeated fields for this mapping.';
        }
      } else { //collection field exists in this mapping for isSource
        const parentCollectionField: Field = collectionField.getCollectionParentField();
        //primitive fields are not selectable when collection field is already selected
        if (!field.isInCollection()) {
          return 'field is not selectable, it is not a child of ' + parentCollectionField.displayName;
        }

        //children of collections are only selectable if this field is in the same collection
        if (field.getCollectionParentField() != parentCollectionField) {
          return 'field is not selectable, it is not a child of ' + parentCollectionField.displayName;
        }
      }
    }
    return null;
  }

  isFieldMapped(field: Field, isSource: boolean): boolean {
    return this.getFields(isSource).indexOf(field) != -1;
  }

  getAllMappedFields(): MappedField[] {
    return this.getMappedFields(true).concat(this.getMappedFields(false));
  }

  getAllFields(): Field[] {
    return this.getFields(true).concat(this.getFields(false));
  }

  getFields(isSource: boolean): Field[] {
    let fields: Field[] = [];
    for (const fieldPair of this.fieldMappings) {
      fields = fields.concat(fieldPair.getFields(isSource));
    }
    return fields;
  }

  hasMappedFields(isSource: boolean): boolean {
    for (const mappedField of this.getMappedFields(isSource)) {
      if (mappedField.isMapped()) {
        return true;
      }
    }
    return false;
  }

  hasFullyMappedPair(): boolean {
    for (const pair of this.fieldMappings) {
      if (pair.isFullyMapped()) {
        return true;
      }
    }
    return false;
  }
}
