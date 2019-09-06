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
import { FieldAction } from './field-action.model';

export enum TransitionMode { ONE_TO_ONE, ONE_TO_MANY, ENUM, MANY_TO_ONE, FOR_EACH }
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

  mode: TransitionMode = TransitionMode.ONE_TO_ONE;
  delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
  userDelimiter = '';
  lookupTableName: string = null;
  expression: ExpressionModel;
  enableExpression = false;
  transitionFieldAction: FieldAction;

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
   * Translate a mapping mode number into a string.
   * @param mode
   */
  static getMappingModeName(mode: TransitionMode): string {
    let actionName: string;

    switch (mode) {
      case TransitionMode.ONE_TO_ONE: {
        actionName = 'One to One';
        break;
      }
      case TransitionMode.MANY_TO_ONE: {
        actionName = 'Many to One';
        break;
      }
      case TransitionMode.ONE_TO_MANY: {
        actionName = 'One to Many';
        break;
      }
      case TransitionMode.ENUM: {
        actionName = 'ENUM';
        break;
      }
      case TransitionMode.FOR_EACH: {
        actionName = 'For Each';
        break;
      }
      default: {
         actionName = '';
         break;
      }
    }
    return actionName;
  }

  getPrettyName() {
    if (this.mode === TransitionMode.ONE_TO_MANY) {
      return TransitionModel.getMappingModeName(this.mode) + ' (' + this.transitionFieldAction.name + ')';
    } else if (this.mode === TransitionMode.MANY_TO_ONE) {
      return TransitionModel.getMappingModeName(this.mode) + ' (' + this.transitionFieldAction.name + ')';
    } else if (this.mode === TransitionMode.ENUM) {
      return 'Enum (table: ' + this.lookupTableName + ')';
    }
    return TransitionModel.getMappingModeName(this.mode);
  }

  isOneToManyMode(): boolean {
    return this.mode === TransitionMode.ONE_TO_MANY;
  }

  isOneToOneMode(): boolean {
    return this.mode === TransitionMode.ONE_TO_ONE;
  }

  isManyToOneMode(): boolean {
    return this.mode === TransitionMode.MANY_TO_ONE;
  }

  isForEachMode(): boolean {
    return this.mode === TransitionMode.FOR_EACH;
  }

  isEnumerationMode(): boolean {
    return this.mode === TransitionMode.ENUM;
  }

}
