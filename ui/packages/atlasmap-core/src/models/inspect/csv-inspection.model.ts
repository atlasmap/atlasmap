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
import {
  DocumentInspectionModel,
  DocumentInspectionRequestModel,
  DocumentInspectionRequestOptions,
} from './document-inspection.model';
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';
import { FieldType, IDocument } from '../../contracts/common';
import {
  ICsvComplexType,
  ICsvDocumentContainer,
  ICsvField,
  ICsvInspectionResponse,
  ICsvInspectionResponseContainer,
} from '../../contracts/documents/csv';

import { CommonUtil } from '../../utils/common-util';
import { Field } from '../field.model';

export class CsvInspectionModel extends DocumentInspectionModel {
  request = new CsvInspectionRequestModel(this.cfg, this.doc);

  isOnlineInspectionCapable(): boolean {
    if (this.cfg.initCfg.baseCSVInspectionServiceUrl == null) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `CSV inspection service is not configured. Document will not be loaded: ${this.doc.name}`,
          level: ErrorLevel.WARN,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.INTERNAL,
          object: this.doc,
        })
      );
      return false;
    }
    return true;
  }

  parseResponse(responseJson: any): void {
    if (typeof responseJson.CsvInspectionResponse !== 'undefined') {
      this.extractCSVDocumentDefinitionFromInspectionResponse(
        (responseJson as ICsvInspectionResponseContainer).CsvInspectionResponse
      );
    } else if (typeof responseJson.CsvDocument !== 'undefined') {
      this.extractCSVDocumentDefinition(
        (responseJson as ICsvDocumentContainer).CsvDocument
      );
    } else {
      throw new Error(`Unknown CSV inspection result format: ${responseJson}`);
    }
  }

  private extractCSVDocumentDefinitionFromInspectionResponse(
    body: ICsvInspectionResponse
  ): void {
    if (body.errorMessage) {
      this.doc.errorOccurred = true;
      throw new Error(
        `Could not load JSON document, error: ${body.errorMessage}`
      );
    }

    this.extractCSVDocumentDefinition(body.csvDocument);
  }

  private extractCSVDocumentDefinition(csvDocument: IDocument): void {
    if (!this.doc.description) {
      this.doc.description = this.doc.id;
    }
    if (!this.doc.name) {
      this.doc.name = this.doc.id;
    }
    if (this.doc.inspectionParameters) {
      this.doc.uri = CommonUtil.urlWithParameters(
        this.doc.uri,
        this.doc.inspectionParameters
      );
    }

    for (const field of csvDocument.fields.field) {
      this.parseCSVFieldFromDocument(field as ICsvField, null);
    }
  }

  private parseCSVFieldFromDocument(
    field: ICsvField,
    parentField: Field | null
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField);
    if (parsedField == null) {
      return;
    }
    parsedField.column = field.column;
    if (field.fieldType !== FieldType.COMPLEX) {
      return;
    }
    const csvComplexType = field as ICsvComplexType;
    if (csvComplexType.csvFields?.csvField?.length) {
      for (const childField of csvComplexType.csvFields.csvField) {
        this.parseCSVFieldFromDocument(childField, parsedField);
      }
    }
  }
}

export class CsvInspectionRequestModel extends DocumentInspectionRequestModel {
  url = this.cfg.initCfg.baseCSVInspectionServiceUrl + 'inspect';
  options = new CsvInspectionRequestOptions(this.cfg, this.doc);
}

export class CsvInspectionRequestOptions extends DocumentInspectionRequestOptions {
  body = this.doc.inspectionSource;
  searchParams: { [key: string]: string } = this.doc.inspectionParameters;
}
