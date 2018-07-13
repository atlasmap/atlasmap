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
  arguments: FieldActionArgument[] = [];
  method: string;
  sourceType = 'undefined';
  targetType = 'undefined';
  serviceObject: any = new Object();

  /**
   * Return true if the action's source/target types and collection types matches the respective source/target field properties
   *             for source transformations, or matches the respective target field properties only if for a target transformation
   * @param fieldPair
   */
  appliesToField(fieldPair: FieldMappingPair, isSource: boolean): boolean {

    if (fieldPair == null) {
      return false;
    }
    const sourceField: Field = fieldPair.getFields(true)[0];
    const targetField: Field = fieldPair.getFields(false)[0];

    if (sourceField == null || targetField == null) {
      return false;
    }

    if (isSource) {
      if (this.serviceObject.sourceCollectionType === 'NONE' && sourceField.getCollectionType() != null) {
        return false;
      }
      if (this.serviceObject.sourceCollectionType === 'ALL'
          && this.sourceType !== 'ANY'
          && ['ARRAY', 'LIST', 'MAP'].indexOf(sourceField.getCollectionType()) === -1
          && sourceField.type !== 'STRING') {
        return false;
      }
      if (this.serviceObject.targetCollectionType === 'NONE' && targetField.getCollectionType() != null) {
        return false;
      }
      if (this.serviceObject.targetCollectionType === 'ALL'
          && ['ARRAY', 'LIST', 'MAP'].indexOf(targetField.getCollectionType()) === -1
          && targetField.type !== 'STRING') {
        return false;
      }

      // Check for matching types.
      if (this.sourceType === 'NUMBER'
          && ['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(sourceField.type) === -1) {
        return false;
      }
      if (this.sourceType === 'ANY_DATE' && ['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(sourceField.type) === -1) {
        return false;
      }
      if (this.targetType === 'NUMBER') {
        return ['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(targetField.type) !== -1;
      }
      if (this.targetType === 'ANY_DATE') {
        return ['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(targetField.type) !== -1;
      }

      // All other types must match the mapped field types with the field action types.
      if (this.sourceType !== 'ANY' && sourceField.type !== this.sourceType) {
        return false;
      }
      return targetField.type === this.targetType;
    } else { // target transformation
      if (this.serviceObject.sourceCollectionType !== this.serviceObject.targetCollectionType) {
        return false;
      }

      // Check for matching types.
      if (this.targetType === 'NUMBER') {
        return (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(targetField.type) !== -1);
      }
      if (this.targetType === 'ANY_DATE') {
        return (['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(targetField.type) !== -1);
      }

      // All other types must match the mapped field types with the field action types.
      return targetField.type === this.sourceType && targetField.type === this.targetType;
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
