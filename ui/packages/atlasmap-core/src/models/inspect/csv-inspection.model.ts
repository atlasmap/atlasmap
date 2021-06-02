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

import { DocumentDefinition } from '../document-definition.model';
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
        responseJson,
        this.doc
      );
    } else if (typeof responseJson.csvDocument !== 'undefined') {
      this.extractCSVDocumentDefinition(responseJson, this.doc);
    } else {
      throw new Error(`Unknown CSV inspection result format: ${responseJson}`);
    }
  }

  private extractCSVDocumentDefinitionFromInspectionResponse(
    responseJson: any,
    docDef: DocumentDefinition
  ): void {
    const body: any = responseJson.CsvInspectionResponse;
    if (body.errorMessage) {
      docDef.errorOccurred = true;
      throw new Error(
        `Could not load JSON document, error: ${body.errorMessage}`
      );
    }

    this.extractCSVDocumentDefinition(body, docDef);
  }

  private extractCSVDocumentDefinition(
    body: any,
    docDef: DocumentDefinition
  ): void {
    let csvDocument: any;
    if (typeof body.csvDocument !== 'undefined') {
      csvDocument = body.csvDocument;
    } else {
      csvDocument = body.CsvDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.id;
    }

    docDef.characterEncoding = csvDocument.characterEncoding;
    docDef.locale = csvDocument.locale;

    for (const field of csvDocument.fields.field) {
      this.parseCSVFieldFromDocument(field, null, docDef);
    }
  }

  private parseCSVFieldFromDocument(
    field: any,
    parentField: Field | null,
    docDef: DocumentDefinition
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (
      field.csvFields &&
      field.csvFields.csvField &&
      field.csvFields.csvField.length
    ) {
      for (const childField of field.csvFields.csvField) {
        this.parseCSVFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }
}

export class CsvInspectionRequestModel extends DocumentInspectionRequestModel {
  url = this.cfg.initCfg.baseCSVInspectionServiceUrl + 'inspect';
  options = new CsvInspectionRequestOptions(this.cfg, this.doc);
}

export class CsvInspectionRequestOptions extends DocumentInspectionRequestOptions {
  json = this.doc.inspectionSource;
  searchParams: { [key: string]: string } = this.doc.inspectionParameters;
}
