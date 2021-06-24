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

import { MappedField, MappingModel } from './mapping.model';

export enum ErrorLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

export enum ErrorScope {
  APPLICATION = 'APPLICATION',
  DATA_SOURCE = 'DATA_SOURCE',
  LOOKUP_TABLE = 'LOOKUP_TABLE',
  MAPPING = 'MAPPING',
  FIELD = 'FIELD',
  FORM = 'FORM',
  CONSTANT = 'CONSTANT',
  PROPERTY = 'PROPERTY',
}

export enum ErrorType {
  INTERNAL = 'INTERNAL',
  USER = 'USER',
  VALIDATION = 'VALIDATION',
  PREVIEW = 'PREVIEW',
  FORM = 'FORM',
}

export class ErrorInfo {
  private static errorIdentifierCounter = 0;
  private _identifier: string;

  message: string;
  level: ErrorLevel;
  scope: ErrorScope;
  type: ErrorType;
  mapping: MappingModel;
  field: MappedField;
  object: any;

  constructor(init: Partial<ErrorInfo>) {
    this._identifier = ErrorInfo.errorIdentifierCounter.toString();
    ErrorInfo.errorIdentifierCounter++;
    Object.assign(this, init);
  }

  get identifier() {
    return this._identifier;
  }
}
