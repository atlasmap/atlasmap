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
import { ExpressionModel } from './expression.model';
import { FieldActionDefinition } from './field-action.model';

export enum TransitionMode { MAP, SEPARATE, ENUM, COMBINE }
export enum TransitionDelimiter {
  NONE, AMPERSAND, AT_SIGN, BACKSLASH, COLON, COMMA, DASH, EQUAL, HASH,
  MULTI_SPACE, PERIOD, PIPE, SEMICOLON, SLASH, SPACE, UNDERSCORE, USER_DEFINED
}

export class TransitionDelimiterModel {
  delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
  serializedValue: string = null;
  prettyName: string = null;
  actualDelimiter = '';

  constructor(
    delimiter: TransitionDelimiter, serializedValue: string, prettyName: string, actualDelimiter) {
    this.delimiter = delimiter;
    this.serializedValue = serializedValue;
    this.prettyName = prettyName;
    this.actualDelimiter = actualDelimiter;
  }
}

export class TransitionModel {
  static delimiterModels: TransitionDelimiterModel[] = [];

  mode: TransitionMode = TransitionMode.MAP;
  delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
  userDelimiter = '';
  lookupTableName: string = null;
  expression: ExpressionModel;
  enableExpression = false;

  constructor() {
    TransitionModel.initialize();
  }

  static initialize() {
    if (TransitionModel.delimiterModels.length === 0) {
      const models: TransitionDelimiterModel[] = [];
      models.push(new TransitionDelimiterModel(TransitionDelimiter.NONE, null, '[None]', ''));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.AMPERSAND, 'Ampersand', 'Ampersand [&]', '\&'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.AT_SIGN, 'AtSign', 'At Sign [@]', '\@'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.BACKSLASH, 'Backslash', 'Backslash [\\]', '\\'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.COLON, 'Colon', 'Colon [:]', '\:'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.COMMA, 'Comma', 'Comma [,]', '\,'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.DASH, 'Dash', 'Dash [-]', '\-'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.EQUAL, 'Equal', 'Equal [=]', '\='));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.HASH, 'Hash', 'Hash [#]', '\#'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.MULTI_SPACE, 'MultiSpace', 'Multi Spaces', '  '));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.PERIOD, 'Period', 'Period [.]', '\.'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.PIPE, 'Pipe', 'Pipe [|]', '\|'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SEMICOLON, 'Semicolon', 'Semicolon [;]', '\;'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SLASH, 'Slash', 'Slash [/]', '\/'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.SPACE, 'Space', 'Space [ ]', ' '));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.UNDERSCORE, 'Underscore', 'Underscore [_]', '\_'));
      models.push(new TransitionDelimiterModel(TransitionDelimiter.USER_DEFINED, 'User defined', 'User defined', ''));
      TransitionModel.delimiterModels = models;
    }
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

  static getTransitionDelimiterFromActual(actualDelimiter: string): TransitionDelimiter {
    for (const m of TransitionModel.delimiterModels) {
      if (m.actualDelimiter === actualDelimiter) {
        return m.delimiter;
      }
    }
    return TransitionDelimiter.USER_DEFINED;
  }

  getSerializedDelimeter(): string {
    if (this.delimiter === TransitionDelimiter.USER_DEFINED) {
      return this.userDelimiter;
    }
    for (const m of TransitionModel.delimiterModels) {
      if (m.delimiter === this.delimiter) {
        return m.serializedValue;
      }
    }
    return null;
  }

  getActualDelimiter(): string {
    if (this.delimiter === TransitionDelimiter.USER_DEFINED) {
      return this.userDelimiter;
    }
    for (const m of TransitionModel.delimiterModels) {
      if (m.delimiter === this.delimiter) {
        return m.actualDelimiter;
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
