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
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';

import { ConfigModel } from '../config.model';
import { DocumentDefinition } from '../document-definition.model';
import { Field } from '../field.model';
import { IField } from '../../contracts/common';
import { Input } from 'ky/distribution/types/options';
import { Options } from 'ky';

/**
 * Encapsulates Document inspection context.
 */
export abstract class DocumentInspectionModel {
  request: DocumentInspectionRequestModel;

  constructor(public cfg: ConfigModel, public doc: DocumentDefinition) {}

  /**
   * Validates if the online inspection is available for this type of Document.
   */
  abstract isOnlineInspectionCapable(): boolean;

  /**
   * Parse inspection response returned from backend.
   *
   * @param responseJson
   */
  abstract parseResponse(responseJson: any): void;

  protected parseFieldFromDocument(
    field: IField,
    parentField: Field | null
  ): Field | null {
    if (field != null && field.status === 'NOT_FOUND') {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `Ignoring unknown field: ${field.name} (${field.path}), document: ${this.doc.name}`,
          level: ErrorLevel.WARN,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.USER,
        })
      );
      return null;
    } else if (field != null && field.status === 'EXCLUDED') {
      return null;
    }

    const parsedField: Field = new Field();
    parsedField.name = field.name!;
    parsedField.type = field.fieldType!;
    parsedField.path = field.path!;
    parsedField.isPrimitive = field.fieldType !== 'COMPLEX';
    parsedField.documentField = field;

    if ('LIST' === field.collectionType || 'ARRAY' === field.collectionType) {
      parsedField.isCollection = true;
      if ('ARRAY' === field.collectionType) {
        parsedField.isArray = true;
      }
    }

    if (parentField != null) {
      parsedField.parentField = parentField;
      parentField.children.push(parsedField);
    } else {
      this.doc.fields.push(parsedField);
    }

    return parsedField;
  }
}

export abstract class DocumentInspectionRequestModel {
  constructor(protected cfg: ConfigModel, protected doc: DocumentDefinition) {}
  url: Input;
  options: DocumentInspectionRequestOptions;
}

export abstract class DocumentInspectionRequestOptions implements Options {
  constructor(protected cfg: ConfigModel, protected doc: DocumentDefinition) {}
  json: any;
  headers: { [key: string]: string } = { 'Content-Type': 'application/json' };
  searchParams: { [key: string]: string } = {};
}
