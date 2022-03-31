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
  CSV_INSPECTION_REQUEST_JSON_TYPE,
  ICsvComplexType,
  ICsvDocumentContainer,
  ICsvField,
  ICsvInspectionRequestContainer,
  ICsvInspectionResponse,
  ICsvInspectionResponseContainer,
} from '../../contracts/documents/csv';
import {
  DocumentInspectionModel,
  DocumentInspectionRequestModel,
  DocumentInspectionRequestOptions,
} from './document-inspection.model';
import { FieldType, IDocument } from '../../contracts/common';

import { CommonUtil } from '../../utils/common-util';
import { Field } from '../field.model';

export class CsvInspectionModel extends DocumentInspectionModel {
  documentTypeName = 'CSV';
  baseUrl = this.cfg.initCfg.baseCSVInspectionServiceUrl;
  request = new CsvInspectionRequestModel(this.cfg, this.doc, this.baseUrl);

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
  options = new CsvInspectionRequestOptions(this.cfg, this.doc);
}

export class CsvInspectionRequestOptions extends DocumentInspectionRequestOptions {
  json: ICsvInspectionRequestContainer = {
    CsvInspectionRequest: {
      jsonType: CSV_INSPECTION_REQUEST_JSON_TYPE,
      inspectionType: this.doc.inspectionType,
      csvData: this.doc.inspectionSource,
      options: this.doc.inspectionParameters,
      inspectPaths: this.doc.inspectionPaths,
    },
  };
}
