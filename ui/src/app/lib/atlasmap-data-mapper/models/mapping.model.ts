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
import { TransitionModel, TransitionMode, FieldAction, FieldActionConfig, FieldActionArgumentValue } from './transition.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

import { DataMapperUtil } from '../common/data-mapper-util';
import { transition } from 'd3';

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
  private padField = false;
  private transformationCount = 0;
  static sortMappedFieldsByPath(mappedFields: MappedField[], allowNone: boolean): MappedField[] {
    if (mappedFields == null || mappedFields.length === 0) {
      return [];
    }
    const fieldsByPath: { [key: string]: MappedField; } = {};
    const fieldPaths: string[] = [];
    for (const mappedField of mappedFields) {
      if (mappedField == null || mappedField.field == null) {
        continue;
      }
      if (!allowNone && mappedField.isNoneField()) {
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

  isNoneField(): boolean {
    return this.field === DocumentDefinition.getNoneField();
  }

  isPadField(): boolean {
    return this.padField;
  }

  setIsPadField(): void {
    this.padField = true;
  }

  incTransformationCount(): void {
    this.transformationCount++;
  }

  reduceTransformationCount(): void {
    if (this.transformationCount > 0) {
      this.transformationCount--;
    }
  }

  getTransformationCount(): number {
    return this.transformationCount;
  }

  /**
   * Given an index range, fill in the field-pair mappings gap with place-holder fields.
   *
   * @param lowIndex
   * @param highIndex
   * @param fieldPair
   */
  addPlaceholders(lowIndex: number, highIndex: number, fieldPair: FieldMappingPair) {
    let padField = null;
    for (let i = lowIndex; i < highIndex; i++) {
      padField = new MappedField;
      padField.field = DocumentDefinition.getPadField();
      padField.field.docDef = this.field.docDef;
      padField.setIsPadField();
      padField.updateSeparateOrCombineFieldAction(fieldPair.transition.mode === TransitionMode.SEPARATE,
        fieldPair.transition.mode === TransitionMode.COMBINE, i.toString(10), this.isSource(), true, false);
      if (this.isSource()) {
          fieldPair.sourceFields.push(padField);
      } else {
          fieldPair.targetFields.push(padField);
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
  updateSeparateOrCombineFieldAction(separateMode: boolean, combineMode: boolean, suggestedValue: string,
                               isSource: boolean, compoundSelection: boolean, fieldRemoved: boolean): void {

    // Remove field actions where appropriate.
    if ((!separateMode && !combineMode) || (separateMode && isSource && compoundSelection)) {
      this.removeSeparateOrCombineAction();
      return;
    }

    let firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction == null || !firstFieldAction.isSeparateOrCombineMode) {

      // Create a new separate/combine field action if there isn't one.
      firstFieldAction = FieldAction.createSeparateCombineFieldAction(separateMode, suggestedValue);
      this.actions = [firstFieldAction].concat(this.actions);
      return;
    }

    // Given a compound selection (ctrl/cmd-M1) create a new field action based on the suggested value.
    if (compoundSelection && !fieldRemoved) {
      const currentFieldAction: FieldAction = FieldAction.createSeparateCombineFieldAction(separateMode, suggestedValue);
      this.actions = [currentFieldAction];
    }
  }

  removeSeparateOrCombineAction(): void {
    const firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
      DataMapperUtil.removeItemFromArray(firstFieldAction, this.actions);
    }
  }

  /**
   * Return the field action index value of this mapped field in separate or combine mode.
   * @deprecated Is there any corner case that it has to check other than argumentValues[0]?
   * other part in the codebase always uses MappedField.actions[0].argumentValues[0]
   * If not, just use getFieldIndex() instead
   */
  getSeparateOrCombineIndex(): string {
    const firstFieldAction: FieldAction = (this.actions.length > 0) ? this.actions[0] : null;
    if (firstFieldAction != null && firstFieldAction.isSeparateOrCombineMode) {
      let maxIndex = 0;
      for (const indexValue of firstFieldAction.argumentValues) {
        const indexAsNumber = (indexValue.value == null) ? 0 : parseInt(indexValue.value, 10);
        if (indexAsNumber > maxIndex) {
          maxIndex = indexAsNumber;
          break;
        }
      }
      return maxIndex.toString();
    }
    return null;
  }

  removeAction(action: FieldAction): void {
    DataMapperUtil.removeItemFromArray(action, this.actions);
  }

  isMapped(): boolean {
    return (this.field != null) && (this.field !== DocumentDefinition.getNoneField());
  }

  getFieldIndex() {
    if (this.actions && this.actions[0] && this.actions[0].argumentValues && this.actions[0].argumentValues[0]) {
      return this.actions[0].argumentValues[0].value;
    }
    return null;
  }

  setFieldIndex(index: string) {
    this.actions[0].argumentValues[0].value = index;
  }

}

export class FieldMappingPair {
  sourceFields: MappedField[] = [new MappedField()];
  targetFields: MappedField[] = [new MappedField()];
  transition: TransitionModel = new TransitionModel();

  constructor() {
    return;
  }

  /**
   * Add a field to this field mapping pair.
   *
   * @param field - field to add to the mapping
   * @param isSource - if true sources panel field, target panels field otherwise.
   * @param first - if true add the field to the beginning of the array, last otherwise.
   */
  addField(field: Field, isSource: boolean, first: boolean): MappedField {
    const mappedField: MappedField = new MappedField();
    mappedField.field = field;
    if (first) {
      this.getMappedFields(isSource).unshift(mappedField);
    } else {
      this.getMappedFields(isSource).push(mappedField);
    }
    return mappedField;
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

  hasNoneField(isSource: boolean) {
    const mappedFields: MappedField[] = isSource ? this.sourceFields : this.targetFields;
    for (const mappedField of mappedFields) {
      if (mappedField.isNoneField()) {
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
      if (index === mappedField.getFieldIndex()) {
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
      if (!mappedField.isPadField() && !mappedField.isNoneField()) {
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
      if (field === DocumentDefinition.getNoneField()) {
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
      if (field === DocumentDefinition.getNoneField()) {
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
      if (mappedField.getTransformationCount() > 0) {
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
      if (!mField.isNoneField() && mField.actions != null && mField.actions.length > 0) {
        if (+mField.getFieldIndex() > maxIndex) {
          maxIndex = +mField.getFieldIndex();
        }
      }
    }
    return maxIndex;
  }

  /**
   * Given an array of mapped fields, fill the gaps in the action indices by adding place-holders.
   * If a field was removed just re-sequence the indices based on their existing position.
   * @param mappedFields
   * @param fieldRemoved
   */
  resequenceRemovalsAndGaps(mappedFields: MappedField[], fieldRemoved: boolean): number {
      let lastIndex = 0;
      let tempIndex = 0;
      for (const mField of mappedFields) {
        if (mField.isNoneField()) {
          continue;
        }
        if (mField.actions != null && mField.actions.length > 0) {
          if (fieldRemoved) {
            const newIndex = (++lastIndex).toString(10);
            mField.setFieldIndex(newIndex);
            continue;
          }
          tempIndex = +mField.getFieldIndex();
          if (tempIndex > lastIndex + 1) {
            mField.addPlaceholders(++lastIndex, tempIndex, this);
            break;
          } else if (tempIndex === lastIndex) {
            const newIndex = ++tempIndex;
            mField.setFieldIndex(newIndex.toString(10));
          }
          lastIndex = +mField.getFieldIndex();
        } else {
          lastIndex++;
        }
      }
      this.sortFieldActionFields(mappedFields);
      return lastIndex;
  }

  /**
   * Given an array of mapped fields, re-sequence the field action indices accounting for any gaps
   * due to user index editing or element removal.  An optional mapped field may be specified to be
   * inserted at a designated index.
   *
   * @param mappedFields
   * @param insertedMappedField - optional user-selected mapped field which was just dragged/dropped
   * @param inIndex - drop index location
   * @param fieldRemoved
   */
  resequenceFieldActionIndices(mappedFields: MappedField[], insertedMappedField: MappedField,
                               inIndex: string, fieldRemoved: boolean): number {
    let index = 0;
    if (insertedMappedField != null) {
      let startIndex = +insertedMappedField.getFieldIndex();
      mappedFields.splice(startIndex, 1);
      startIndex = +inIndex;
      mappedFields.splice(startIndex, 0, insertedMappedField);

      // Now re-sequence the index on the ordinal position within the mapped fields array.
      for (const mField of mappedFields) {
        if (!mField.isNoneField() && mField.actions != null && mField.actions.length > 0) {
          const oldIndex = mField.getFieldIndex();
          const newIndex = index.toString(10);
          mField.setFieldIndex(newIndex);
        }
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
   * Given an array of mapped fields, sort the field action fields themselves. based on their index.
   *
   * @param mappedFields
   */
  sortFieldActionFields(mappedFields: MappedField[]): void {
    let done = false;

    while (!done) {
      let tempField: MappedField = null;
      let lastField: MappedField = null;
      let index = 0;
      done = true;

      for (const mField of mappedFields) {
        if (mField.isNoneField()) {
          continue;
        }

        if (mField.actions != null && mField.actions.length > 0 && lastField != null) {
          if (mField.getFieldIndex() == null) {
            break;
          }

          if (lastField.actions.length > 0 && +mField.getFieldIndex() < +lastField.getFieldIndex()) {
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

  /**
   * Normalize index fields for combine/ separate modes.
   * @param combineMode
   */
  processIndices(combineMode: boolean, fieldRemoved: boolean): number {

    // Remove indices from target fields in combine-mode if they exist or remove indices from
    // source fields in separate-mode if they exist.
    for (const mField of this.getMappedFields(!combineMode)) {
      if (mField.isNoneField()) {
        continue;
      }
      mField.removeSeparateOrCombineAction();
    }

    // Gather mapped fields.
    const mappedFields = this.getMappedFields(combineMode);
    return this.resequenceFieldActionIndices(mappedFields, null, '', fieldRemoved);
  }

  clearAllCombineSeparateActions(): void {
    for (const mappedField of this.getAllMappedFields()) {
      mappedField.removeSeparateOrCombineAction();
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
      if (mappedField.isNoneField()) {
        continue;
      }
      const actionsToRemove: FieldAction[] = [];
      for (const action of mappedField.actions) {
        const actionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(action.name);
        if (actionConfig != null && !actionConfig.appliesToField(this, false)) {
          actionsToRemove.push(action);
        }
      }
      for (const action of actionsToRemove) {
        mappedField.removeAction(action);
      }
    }

    let separateMode: boolean = (this.transition.mode === TransitionMode.SEPARATE);
    let combineMode: boolean = (this.transition.mode === TransitionMode.COMBINE);
    let maxIndex = 0;

    if (combineMode || separateMode) {

      // Set a none field as the base of a compound mapping.
      if (!this.hasNoneField(isSource)) {
        this.addField(DocumentDefinition.getNoneField(), isSource, true);
      }

      maxIndex = this.processIndices(combineMode, fieldRemoved);

      if (maxIndex <= 1 && fieldRemoved) {
        this.transition.mode = TransitionMode.MAP;
        combineMode = false;
        separateMode = false;
        this.clearAllCombineSeparateActions();
      } else {
        mappedFields = this.getMappedFields(combineMode);
        if (mappedFields != null && mappedFields.length > 1) {
          let mappedField: MappedField = mappedFields[1];

          // In case the user made a compound mapping, then reverted back to single mapping, then back to compound.
          if (mappedField.actions.length === 0) {
            mappedField.updateSeparateOrCombineFieldAction(separateMode, combineMode, '1', isSource,
              compoundSelection, fieldRemoved);
          }
          mappedField = mappedFields[mappedFields.length - 1];
          mappedField.updateSeparateOrCombineFieldAction(separateMode, combineMode, maxIndex.toString(), isSource,
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
}

export class MappingModel {
  cfg: ConfigModel;
  uuid: string;
  fieldMappings: FieldMappingPair[] = [];
  currentFieldMapping: FieldMappingPair = null;
  validationErrors: ErrorInfo[] = []; // must be immutable
  previewErrors: ErrorInfo[] = []; // must be immutable
  brandNewMapping = true;

  constructor() {
    this.uuid = 'mapping.' + Math.floor((Math.random() * 1000000) + 1).toString();
    this.fieldMappings.push(new FieldMappingPair());
    this.cfg = ConfigModel.getConfig();
    Object.freeze(this.validationErrors);
  }

  getFirstFieldMapping(): FieldMappingPair {
    if (this.fieldMappings == null || (this.fieldMappings.length === 0)) {
      return null;
    }
    return this.fieldMappings[0];
  }

  getLastFieldMapping(): FieldMappingPair {
    if (this.fieldMappings == null || (this.fieldMappings.length === 0)) {
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
    for (const f of this.getFields(isSource)) {
      if (f.isInCollection()) {
        return f;
      }
    }
    return null;
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

  /**
   * Walk all target field mappings and return true if the specified field is already the target
   * of a previous mapping, false otherwise.
   *
   * @param field
   */
  getMappedTarget(field: Field): string {
    const mappings: MappingModel[] = this.cfg.mappings.mappings;

    if (!field.isSource()) {
      for (const m of mappings) {
        for (const fieldPair of m.fieldMappings) {
          if (fieldPair.targetFields.length === 0) {
            continue;
          }

          for (const mappedOutputField of fieldPair.targetFields) {
             if (mappedOutputField.field.name === field.name) {
               const currentFieldMapping = this.getCurrentFieldMapping();
               if (currentFieldMapping != null && !currentFieldMapping.isFieldMapped(field) && field.partOfMapping) {
                 return fieldPair.sourceFields[0].field.name;
               }
             }
          }
        }
      }
    }
    return null;
  }

  getFieldSelectionExclusionReason(field: Field ): string {
    if (this.brandNewMapping) { // if mapping hasn't had a field selected yet, allow it
      return null;
    }

    if (!field.isTerminal()) {
      return 'field is a parent field';
    }

    // Target fields may only be mapped once.
    const existingMappedField = this.getMappedTarget(field);
    if (existingMappedField != null) {
      const macPlatform: boolean = /(MacPPC|MacIntel|Mac_PowerPC|Macintosh|Mac OS X)/.test(navigator.userAgent);
      return 'it is already the target of another mapping (' + existingMappedField + '). ' +
        'Use ' + (macPlatform ? 'CMD' : 'CTRL') + '-M1 to select multiple elements for \'Combine\' or \'Separate\' actions.';
    }

    const lookupMode: boolean = this.isLookupMode();
    let mapMode = false;
    let separateMode = false;
    let combineMode = false;

    if (!lookupMode) {
      for (const fieldPair of this.fieldMappings) {
        mapMode = mapMode || fieldPair.transition.isMapMode();
        separateMode = separateMode || fieldPair.transition.isSeparateMode();
        combineMode = combineMode || fieldPair.transition.isCombineMode();
      }
    }
    if (mapMode || separateMode || combineMode) {
      // enums are not selectable in these modes
      if (field.enumeration) {
        return 'Enumeration fields are not valid for this mapping';
      }

      // separate mode sources must be string
      if (separateMode && !field.isStringField() && field.isSource()) {
        return 'source fields for this mapping must be type String';
      }
    } else if (lookupMode) {
      if (!field.enumeration) {
        return 'only Enumeration fields are valid for this mapping';
      }
    }
    return null;
  }

  isFieldMapped(field: Field, isSource: boolean): boolean {
    return this.getFields(isSource).indexOf(field) !== -1;
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

  /**
   * Return true if the current mapping instance has any field action/transformation, false otherwise.
   */
  hasFieldAction(): boolean {
    for (const pair of this.fieldMappings) {
      if (pair.hasFieldActions()) {
        return true;
      }
    }
    return false;
  }

}
