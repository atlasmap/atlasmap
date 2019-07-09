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
import { ConfigModel } from '../models/config.model';
import { Field } from './field.model';
import { TransitionModel, TransitionMode } from './transition.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { DataMapperUtil } from '../common/data-mapper-util';
import { FieldAction } from './field-action.model';

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
  field: Field;
  index = -1;
  actions: FieldAction[] = [];
  private padField = false;
  private transformationCount = 0;

  static sortMappedFieldsByPath(mappedFields: MappedField[]): MappedField[] {
    if (mappedFields == null || mappedFields.length === 0) {
      return [];
    }
    const fieldsByPath: { [key: string]: MappedField; } = {};
    const fieldPaths: string[] = [];
    for (const mappedField of mappedFields) {
      if (mappedField == null || mappedField.field == null) {
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

  isPadField(): boolean {
    return this.padField;
  }

  setIsPadField(): void {
    this.padField = true;
  }

  /**
   * Given an index range, fill in the field-pair mappings gap with place-holder fields.
   *
   * @param lowIndex
   * @param highIndex
   * @param fieldPair
   */
  addPlaceholders(lowIndex: number, highIndex: number, mapping: MappingModel) {
    let padField = null;
    for (let i = lowIndex; i < highIndex; i++) {
      padField = new MappedField;
      padField.field = DocumentDefinition.getPadField();
      padField.field.docDef = this.field.docDef;
      padField.setIsPadField();
      padField.updateSeparateOrCombineFieldAction(mapping.transition.mode === TransitionMode.ONE_TO_MANY,
        mapping.transition.mode === TransitionMode.MANY_TO_ONE, i.toString(10), this.isSource(), true, false);
      if (this.isSource()) {
          mapping.sourceFields.push(padField);
      } else {
          mapping.targetFields.push(padField);
      }
    }
  }

  isSource(): boolean {
    return this.field.isSource();
  }

  /**
   * Given a selection or de-selection of a component, update the appropriate field action.
   *
   * @param separateMode
   * @param combineMode
   * @param suggestedValue
   * @param isSource
   * @param compoundSelection
   * @param fieldRemoved
   */
  updateSeparateOrCombineFieldAction(separateMode: boolean, combineMode: boolean,
                               isSource: boolean, compoundSelection: boolean, fieldRemoved: boolean): void {

    // Remove field actions where appropriate.
    if ((!separateMode && !combineMode) || (separateMode && isSource && compoundSelection)) {
      this.removeSeparateOrCombineAction();
      return;
    }

    let firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction == null || !firstFieldAction.isSeparateOrCombineMode) {

      // Create a new separate/combine field action if there isn't one.
      firstFieldAction = FieldAction.createSeparateCombineFieldAction(separateMode);
      this.actions = [firstFieldAction].concat(this.actions);
      return;
    }

    // Given a compound selection (ctrl/cmd-M1) create a new field action based on the suggested value.
    if (compoundSelection && !fieldRemoved) {
      const currentFieldAction: FieldAction = FieldAction.createSeparateCombineFieldAction(separateMode);
      this.actions = [currentFieldAction];
    }
  }

  removeSeparateOrCombineAction(): void {
    const firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
      DataMapperUtil.removeItemFromArray(firstFieldAction, this.actions);
    }
  }

  removeAction(action: FieldAction): void {
    DataMapperUtil.removeItemFromArray(action, this.actions);
  }

  hasIndex() {
    return this.index != null && this.index >= 0;
  }

}

export class MappingModel {
  cfg: ConfigModel;
  uuid: string;

  sourceFields: MappedField[] = [];
  targetFields: MappedField[] = [];
  transition: TransitionModel = new TransitionModel();

  validationErrors: ErrorInfo[] = []; // must be immutable
  previewErrors: ErrorInfo[] = []; // must be immutable

  constructor() {
    this.uuid = 'mapping.' + Math.floor((Math.random() * 1000000) + 1).toString();
    this.cfg = ConfigModel.getConfig();
    Object.freeze(this.validationErrors);
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
    return this.validationErrors.filter(e => e.level === ErrorLevel.WARN);
  }

  removeValidationError(identifier: string) {
    this.validationErrors = this.validationErrors.filter(e => e.identifier !== identifier);
    Object.freeze(this.validationErrors);
  }

  clearPreviewErrors(): void {
    this.previewErrors = [];
    Object.freeze(this.previewErrors);
  }

  getPreviewErrors(): ErrorInfo[] {
    return this.previewErrors.filter(e => e.level >= ErrorLevel.ERROR);
  }

  getPreviewWarnings(): ErrorInfo[] {
    return this.previewErrors.filter(e => e.level === ErrorLevel.WARN);
  }

  removePreviewError(identifier: string) {
    this.previewErrors = this.previewErrors.filter(e => e.identifier !== identifier);
    Object.freeze(this.previewErrors);
  }

  getFirstCollectionField(isSource: boolean): Field {
    for (const f of isSource ? this.sourceFields : this.targetFields) {
      if (f.field.isInCollection()) {
        return f.field;
      }
    }
    return null;
  }

  isLookupMode(): boolean {
    for (const f of this.sourceFields.concat(this.targetFields)) {
      if (f.field.enumeration) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add a field to this field mapping pair.
   *
   * @param field - field to add to the mapping
   * @param first - if true add the field to the beginning of the array, last otherwise.
   */
  addField(field: Field, first: boolean): MappedField {
    const mappedField: MappedField = new MappedField();
    mappedField.field = field;
    if (first) {
      this.getMappedFields(field.isSource()).unshift(mappedField);
    } else {
      this.getMappedFields(field.isSource()).push(mappedField);
    }
    return mappedField;
  }

  removeField(field: Field) {
    const mappedFields = this.getMappedFields(field.isSource());
    DataMapperUtil.removeItemFromArray(mappedFields.find(mf => mf.field === field), mappedFields);
  }

  hasMappedField(isSource: boolean) {
    return isSource ? this.sourceFields.length > 0 : this.targetFields.length > 0;
  }

  isEmpty() {
    return this.sourceFields.length === 0 && this.targetFields.length === 0;
  }

  isFullyMapped(): boolean {
    return this.sourceFields.length > 0 && this.targetFields.length > 0;
  }

  addMappedField(mappedField: MappedField, isSource: boolean): void {
    this.getMappedFields(isSource).push(mappedField);
  }

  removeMappedField(mappedField: MappedField, isSource: boolean): void {
    DataMapperUtil.removeItemFromArray(mappedField, this.getMappedFields(isSource));
  }

  getMappedFieldForField(field: Field): MappedField {
    for (const mappedField of this.getMappedFields(field.isSource())) {
      if (mappedField.field === field) {
        return mappedField;
      }
    }
    return null;
  }

  getMappedFieldForIndex(index: string, isSource: boolean): MappedField {
    if (!index) {
      return null;
    }
    for (const mappedField of this.getMappedFields(isSource)) {
      if (+index === mappedField.index) {
        return mappedField;
      }
    }
    return null;
  }

  /**
   * Return an array of user mapped fields for the specified panel in this field pair instance.  No
   * data-mapper generated padding fields will be included.
   *
   * @param isSource - true source panel, false target panel
   */
  getUserMappedFields(isSource: boolean): MappedField[] {
    const workingFields = isSource ? this.sourceFields : this.targetFields;
    const resultFields: MappedField[] = [new MappedField()];

    for (const mappedField of workingFields) {
      if (!mappedField.isPadField()) {
        resultFields.push(mappedField);
      }
    }
    resultFields.shift();
    return resultFields;
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
      names.push(field.name);
    }
    return names;
  }

  getFieldPaths(isSource: boolean): string[] {
    const fields: Field[] = this.getFields(isSource);
    Field.alphabetizeFields(fields);
    const paths: string[] = [];
    for (const field of fields) {
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
    return this.getMappedFieldForField(field) != null;
  }

  hasTransformation(): boolean {
    const mappedFields: MappedField[] = this.getAllMappedFields();
    for (const mappedField of mappedFields) {
      if (mappedField.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return the maximum assigned index value in the specified mapped fields.
   *
   * @param mappedFields
   */
  getMaxIndex(mappedFields: MappedField[]): number {
    let maxIndex = 0;
    for (const mField of mappedFields) {
      if (mField.actions != null && mField.actions.length > 0) {
        if (+mField.index > maxIndex) {
          maxIndex = +mField.index;
        }
      }
    }
    return maxIndex;
  }

  /**
   * Given an array of mapped fields, re-sequence the field indices accounting for any gaps
   * due to user index editing or element removal.  An optional mapped field may be specified to be
   * inserted at a designated index.
   *
   * @param mappedFields
   * @param insertedMappedField - optional user-selected mapped field which was just dragged/dropped
   * @param inIndex - drop index location
   * @param fieldRemoved
   */
  resequenceFieldIndices(mappedFields: MappedField[], insertedMappedField: MappedField,
                               inIndex: number, fieldRemoved: boolean): number {
    if (insertedMappedField != null) {
      let startIndex = insertedMappedField.index;
      mappedFields.splice(startIndex - 1, 1);
      startIndex = inIndex;
      mappedFields.splice(startIndex - 1, 0, insertedMappedField);

      // Now re-sequence the index on the ordinal position within the mapped fields array.
      let index = 1;
      for (const mField of mappedFields) {
        mField.index = index;
        index++;
      }
      return(index - 1);
    }

    const maxIndex = this.getMaxIndex(mappedFields);
    let lastIndex = 0;
    while (lastIndex < maxIndex) {
      lastIndex = this.resequenceRemovalsAndGaps(mappedFields, fieldRemoved);
      if (fieldRemoved) {
        break;
      }
    }

    return(lastIndex);
  }

  /**
   * Given an array of mapped fields, sort the fields themselves. based on their index.
   *
   * @param mappedFields
   */
  sortFieldsByIndex(mappedFields: MappedField[]): void {
    let done = false;

    while (!done) {
      let tempField: MappedField = null;
      let lastField: MappedField = null;
      let index = 0;
      done = true;

      for (const mField of mappedFields) {
        if (lastField != null) {
          if (!mField.hasIndex()) {
            break;
          }

          if (mField.index < lastField.index) {
            tempField = mappedFields[index - 1];
            mappedFields[index - 1] = mField;
            mappedFields[index] = tempField;
            done = false;
          }
        }
        index++;
        lastField = mField;
      }
    }
  }

  updateTransition(isSource: boolean, compoundSelection: boolean, fieldRemoved: boolean, position?: string, offset?: number): void {
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
        const actionConfig = this.cfg.fieldActionService.getActionDefinitionForName(action.name);
        if (actionConfig != null && !actionConfig.appliesToField(this, false)) {
          actionsToRemove.push(action);
        }
      }
      for (const action of actionsToRemove) {
        mappedField.removeAction(action);
      }
    }

    let separateMode: boolean = (this.transition.mode === TransitionMode.ONE_TO_MANY);
    let combineMode: boolean = (this.transition.mode === TransitionMode.MANY_TO_ONE);
    let maxIndex = 0;

    if (combineMode || separateMode) {

      maxIndex = this.processIndices(combineMode, fieldRemoved);

      if (maxIndex <= 1 && fieldRemoved) {
        this.transition.mode = TransitionMode.ONE_TO_ONE;
        combineMode = false;
        separateMode = false;
        this.clearAllCombineSeparateActions();
        this.getMappedFields(isSource).forEach(mf => mf.index = null);
      } else {
        mappedFields = this.getMappedFields(combineMode);
        if (mappedFields != null && mappedFields.length > 1) {
          let mappedField: MappedField = mappedFields[1];

          // In case the user made a compound mapping, then reverted back to single mapping, then back to compound.
          if (mappedField.actions.length === 0) {
            mappedField.index = 1;
            mappedField.updateSeparateOrCombineFieldAction(separateMode, combineMode, isSource,
              compoundSelection, fieldRemoved);
          }
          mappedField = mappedFields[mappedFields.length - 1];
          mappedField.index = maxIndex;
          mappedField.updateSeparateOrCombineFieldAction(separateMode, combineMode, isSource,
            compoundSelection, fieldRemoved);
        }
      }
    } else {
      this.clearAllCombineSeparateActions();
    }

    // Update conditional expression field references if enabled.
    if (this.transition.enableExpression && this.transition.expression) {
      this.transition.expression.updateFieldReference(this, position, offset);
    }
  }

  /**
   * Walk all target field mappings and return one of corresponding source field name
   * if the specified field is already the target of a previous mapping, null otherwise.
   *
   * @param field
   */
  public getMappedTarget(field: Field): string {
    const mappings: MappingModel[] = this.cfg.mappings.mappings;

    if (field.isSource()) {
      return null;
    }
    for (const m of mappings) {
      if (m.targetFields.length === 0) {
        continue;
      }

      for (const mappedOutputField of m.targetFields) {
        if (mappedOutputField.field.docDef === field.docDef
          && mappedOutputField.field.path === field.path) {
          if (m.isFieldMapped(field)) {
            return m.sourceFields[0].field.name;
          }
        }
      }
    }
  }

  /**
   * Normalize index fields for combine/ separate modes.
   * @param combineMode
   */
  private processIndices(combineMode: boolean, fieldRemoved: boolean): number {

    // Remove indices from target fields in combine-mode if they exist or remove indices from
    // source fields in separate-mode if they exist.
    for (const mField of this.getMappedFields(!combineMode)) {
      mField.removeSeparateOrCombineAction();
    }

    // Gather mapped fields.
    const mappedFields = this.getMappedFields(combineMode);
    return this.resequenceFieldIndices(mappedFields, null, -1, fieldRemoved);
  }

  private clearAllCombineSeparateActions(): void {
    for (const mappedField of this.getAllMappedFields()) {
      mappedField.removeSeparateOrCombineAction();
    }
  }

  /**
   * Given an array of mapped fields, fill the gaps in the action indices by adding place-holders.
   * If a field was removed just re-sequence the indices based on their existing position.
   * @param mappedFields
   * @param fieldRemoved
   */
  private resequenceRemovalsAndGaps(mappedFields: MappedField[], fieldRemoved: boolean): number {
      let lastIndex = 0;
      let tempIndex = 0;
      for (const mField of mappedFields) {
        if (mField.actions != null && mField.actions.length > 0) {
          if (fieldRemoved) {
            mField.index = ++lastIndex;
            continue;
          }
          tempIndex = mField.index;
          if (tempIndex > lastIndex + 1) {
            mField.addPlaceholders(++lastIndex, tempIndex, this);
            break;
          } else if (tempIndex === lastIndex) {
            const newIndex = ++tempIndex;
            mField.index = newIndex;
          }
          lastIndex = mField.index;
        } else {
          lastIndex++;
        }
      }
      this.sortFieldsByIndex(mappedFields);
      return lastIndex;
  }

}
