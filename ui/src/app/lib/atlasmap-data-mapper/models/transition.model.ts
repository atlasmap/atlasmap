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
import { FieldMappingPair } from './mapping.model';

export class FieldActionArgument {
  name: string = null;
  type = 'STRING';
  values = null;
  serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
  name: string = null;
  value: string = null;
}

export class FieldActionConfig {
  name: string;
  isCustom: boolean;
  arguments: FieldActionArgument[] = [];
  method: string;
  sourceType = 'undefined';
  targetType = 'undefined';
  serviceObject: any = new Object();

  /**
   * Return the first non-padding field in either the source or target mappings.
   *
   * @param fieldPair
   * @param isSource
   */
  private getActualField(fieldPair: FieldMappingPair, isSource: boolean): Field {
    let targetField: Field = null;
    for (targetField of fieldPair.getFields(isSource)) {
      if (targetField.name !== '<padding field>') {
        break;
      }
    }
    return targetField;
  }

  /**
   * Return true if the specified field mapping pair has multiple transformations specified, false otherwise.
   *
   * @param fieldPair
   */
  private multipleTransformations(fieldPair: FieldMappingPair): boolean {
    return fieldPair.sourceFields[0].actions.length > 1;
  }

  /**
   * Return true if the candidate type and selected type are generically a date, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesDate(candidateType: string, selectedType: string): boolean {
    return ((selectedType === '') || (candidateType === 'ANY') ||
      (candidateType === 'ANY_DATE' &&
        ['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(selectedType) !== -1));
  }

  /**
   * Return true if the candidate type and selected type are generically numeric, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesNumeric(candidateType: string, selectedType: string): boolean {
    return ((selectedType === '') || (candidateType === 'ANY') ||
      (candidateType === 'NUMBER' &&
        ['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(selectedType) !== -1));
  }

  /**
   * Return true if the action's source/target types and collection types match the respective source/target
   * field properties for source transformations, or matches the respective target field properties only for
   * a target transformation.
   *
   * Note - source-side only transformations are permitted so the target field may be undefined.
   *
   * @param fieldPair
   */
  appliesToField(fieldPair: FieldMappingPair, isSource: boolean): boolean {

    if (fieldPair == null) {
      return false;
    }
    const selectedSourceField: Field = this.getActualField(fieldPair, true);
    const selectedTargetField: Field = this.getActualField(fieldPair, false);

    if (selectedSourceField == null) {
      return false;
    }

    if (isSource) {

      // Check for matching types - date.
      if (this.matchesDate(this.sourceType, selectedSourceField.type)) {
        if ((this.multipleTransformations(fieldPair)) || (this.matchesDate(this.targetType, selectedTargetField.type)) ||
            (this.targetType === selectedTargetField.type)) {
          return true;
        }
      }

      // Check for matching types - numeric.
      if (this.matchesNumeric(this.sourceType, selectedSourceField.type)) {
        if ((this.multipleTransformations(fieldPair)) || (this.matchesNumeric(this.targetType, selectedTargetField.type)) ||
            (this.targetType === selectedTargetField.type)) {
          return true;
        }
      }

      // First check if the source types match.
     if ((this.sourceType === 'ANY') || (selectedSourceField.type === this.sourceType)) {

       // If no target type is selected then we match (source-side transformation).
       if ((this.multipleTransformations(fieldPair)) || (selectedTargetField.type === '')) {
         return true;
       }

       // Now the target types must match.
       return ((selectedTargetField.type === this.targetType) || (this.matchesNumeric(this.targetType, selectedTargetField.type)));
     }

     return false;

    // Target transformation - target type may not change
    } else {

      if (selectedTargetField == null) {
        return false;
      }

      if (this.serviceObject.sourceCollectionType !== this.serviceObject.targetCollectionType) {
        return false;
      }

      // Check for matching types - date.
      if (this.targetType === 'ANY_DATE') {
        return (this.matchesDate(this.targetType, selectedTargetField.type));
      }

      // Check for matching types - numeric.
      if (this.targetType === 'NUMBER') {
        return (this.matchesNumeric(this.targetType, selectedTargetField.type));
      }

      // All other types must match the selected field types with the candidate field action types.
      return ((this.sourceType === 'ANY' || selectedTargetField.type === this.sourceType) &&
              (this.targetType === 'ANY' || selectedTargetField.type === this.targetType));
    }
  }

  populateFieldAction(action: FieldAction): void {
    action.name = this.name;
    action.config = this;

    // Use the parsed values if present, otherwise set to '0'.
    if (action.argumentValues == null || action.argumentValues.length === 0) {
      action.argumentValues = [];
      for (const arg of this.arguments) {

        // Default the input field to 0 for numerics
        if (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(arg.type) !== -1) {
          action.setArgumentValue(arg.name, '0');
        } else {
          action.setArgumentValue(arg.name, '');
        }
      }
    }
  }

  getArgumentForName(name: string): FieldActionArgument {
    for (const argument of this.arguments) {
      if (argument.name === name) {
        return argument;
      }
    }
    return null;
  }
}

export class FieldAction {
  static combineActionConfig: FieldActionConfig = null;
  static separateActionConfig: FieldActionConfig = null;

  isSeparateOrCombineMode = false;
  name: string;
  config: FieldActionConfig = null;
  argumentValues: FieldActionArgumentValue[] = [];

  static createSeparateCombineFieldAction(separateMode: boolean, value: string) {
    if (FieldAction.combineActionConfig == null) {
      const argument: FieldActionArgument = new FieldActionArgument();
      argument.name = 'Index';
      argument.type = 'NUMBER';
      FieldAction.combineActionConfig = new FieldActionConfig();
      FieldAction.combineActionConfig.name = 'Combine';
      FieldAction.combineActionConfig.arguments.push(argument);
      FieldAction.separateActionConfig = new FieldActionConfig();
      FieldAction.separateActionConfig.name = 'Separate';
      FieldAction.separateActionConfig.arguments.push(argument);
    }

    const fieldAction: FieldAction = new FieldAction();
    FieldAction.combineActionConfig.populateFieldAction(fieldAction);
    if (separateMode) {
      FieldAction.separateActionConfig.populateFieldAction(fieldAction);
    }
    fieldAction.isSeparateOrCombineMode = true;

    fieldAction.setArgumentValue('Index', (value == null) ? '1' : value);
    return fieldAction;
  }

  getArgumentValue(argumentName: string): FieldActionArgumentValue {
    for (const argValue of this.argumentValues) {
      if (argValue.name === argumentName) {
        return argValue;
      }
    }
    const newArgValue: FieldActionArgumentValue = new FieldActionArgumentValue();
    newArgValue.name = argumentName;
    newArgValue.value = '0';
    this.argumentValues.push(newArgValue);
    return newArgValue;
  }

  setArgumentValue(argumentName: string, value: string): void {
    this.getArgumentValue(argumentName).value = value;
  }

}

export enum TransitionMode { MAP, SEPARATE, ENUM, COMBINE }
export enum TransitionDelimiter {
  NONE, AMPERSAND, AT_SIGN, BACKSLASH, COLON, COMMA, DASH, EQUAL, HASH,
  MULTI_SPACE, PERIOD, PIPE, SEMICOLON, SLASH, SPACE, UNDERSCORE
}

export class TransitionDelimiterModel {
  delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
  serializedValue: string = null;
  prettyName: string = null;

  constructor(
    delimiter: TransitionDelimiter, serializedValue: string, prettyName: string) {
    this.delimiter = delimiter;
    this.serializedValue = serializedValue;
    this.prettyName = prettyName;
  }
}

export class TransitionModel {
  static delimiterModels: TransitionDelimiterModel[] = [];
  static actionConfigs: FieldActionConfig[] = [];

  mode: TransitionMode = TransitionMode.MAP;
  delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
  lookupTableName: string = null;

  constructor() {
    TransitionModel.initialize();
  }

  static initialize() {
    if (TransitionModel.delimiterModels.length === 0) {
      const models: TransitionDelimiterModel[] = [];
      models.push(new TransitionDelimiterModel(TransitionDelimiter.NONE, null, '[None]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.AMPERSAND, 'Ampersand', 'Ampersand [&]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.AT_SIGN, 'AtSign', 'At Sign [@]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.BACKSLASH, 'Backslash', 'Backslash [\\]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.COLON, 'Colon', 'Colon [:]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.COMMA, 'Comma', 'Comma [,]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.DASH, 'Dash', 'Dash [-]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.EQUAL, 'Equal', 'Equal [=]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.HASH, 'Hash', 'Hash [#]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.MULTI_SPACE, 'MultiSpace', 'Multi Spaces'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.PERIOD, 'Period', 'Period [.]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.PIPE, 'Pipe', 'Pipe [|]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SEMICOLON, 'Semicolon', 'Semicolon [;]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SLASH, 'Slash', 'Slash [/]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SPACE, 'Space', 'Space [ ]'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.UNDERSCORE, 'Underscore', 'Underscore [_]'));
      TransitionModel.delimiterModels = models;
    }
  }

  static getActionConfigForName(actionName: string): FieldActionConfig {
    if (actionName == null) {
      return null;
    }
    for (const actionConfig of TransitionModel.actionConfigs) {
      if (actionName === actionConfig.name) {
        return actionConfig;
      }
    }
    return null;
  }

  /**
   * Translate an action mode number into a string.
   * @param mode
   */
  static getActionName(mode: TransitionMode): string {
    let actionName: string;

    switch (mode) {
      case TransitionMode.MAP: {
         actionName = 'MAP';
         break;
      }
      case TransitionMode.COMBINE: {
         actionName = 'COMBINE';
         break;
      }
      case TransitionMode.SEPARATE: {
          actionName = 'SEPARATE';
          break;
      }
      case TransitionMode.ENUM: {
          actionName = 'ENUM';
          break;
      }
      default: {
         actionName = '';
         break;
      }
    }
    return actionName;
  }

  static getTransitionDelimiterPrettyName(delimiter: TransitionDelimiter): string {
    for (const m of TransitionModel.delimiterModels) {
      if (m.delimiter === delimiter) {
        return m.prettyName;
      }
    }
    return null;
  }

  getSerializedDelimeter(): string {
    for (const m of TransitionModel.delimiterModels) {
      if (m.delimiter === this.delimiter) {
        return m.serializedValue;
      }
    }
    return null;
  }

  setSerializedDelimeterFromSerializedValue(value: string): void {
    for (const m of TransitionModel.delimiterModels) {
      if (m.serializedValue === value) {
        this.delimiter = m.delimiter;
      }
    }
  }

  getPrettyName() {
    const delimiterDesc: string = TransitionModel.getTransitionDelimiterPrettyName(this.delimiter);
    if (this.mode === TransitionMode.SEPARATE) {
      return 'Separate (' + delimiterDesc + ')';
    } else if (this.mode === TransitionMode.COMBINE) {
      return 'Combine (' + delimiterDesc + ')';
    } else if (this.mode === TransitionMode.ENUM) {
      return 'Enum (table: ' + this.lookupTableName + ')';
    }
    return 'Map';
  }

  isSeparateMode(): boolean {
    return this.mode === TransitionMode.SEPARATE;
  }

  isMapMode(): boolean {
    return this.mode === TransitionMode.MAP;
  }

  isCombineMode(): boolean {
    return this.mode === TransitionMode.COMBINE;
  }

  isEnumerationMode(): boolean {
    return this.mode === TransitionMode.ENUM;
  }
}
