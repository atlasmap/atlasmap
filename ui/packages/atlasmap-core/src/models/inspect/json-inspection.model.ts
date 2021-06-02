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
import { EnumValue, Field } from '../field.model';
import { ErrorInfo, ErrorLevel, ErrorScope, ErrorType } from '../error.model';

import { DocumentDefinition } from '../document-definition.model';

export class JsonInspectionModel extends DocumentInspectionModel {
  request = new JsonInspectionRequestModel(this.cfg, this.doc);

  isOnlineInspectionCapable(): boolean {
    if (this.cfg.initCfg.baseJSONInspectionServiceUrl == null) {
      this.cfg.errorService.addError(
        new ErrorInfo({
          message: `JSON inspection service is not configured. Document will not be loaded: ${this.doc.name}`,
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
    if (typeof responseJson.JsonInspectionResponse !== 'undefined') {
      this.extractJSONDocumentDefinitionFromInspectionResponse(
        responseJson,
        this.doc
      );
    } else if (typeof responseJson.JsonDocument !== 'undefined') {
      this.extractJSONDocumentDefinition(responseJson, this.doc);
    } else {
      throw new Error(`Unknown JSON inspection result format: ${responseJson}`);
    }
  }

  private extractJSONDocumentDefinitionFromInspectionResponse(
    responseJson: any,
    docDef: DocumentDefinition
  ): void {
    const body: any = responseJson.JsonInspectionResponse;
    if (body.errorMessage) {
      docDef.errorOccurred = true;
      throw new Error(
        `Could not load JSON document, error: ${body.errorMessage}`
      );
    }

    this.extractJSONDocumentDefinition(body, docDef);
  }

  private extractJSONDocumentDefinition(
    body: any,
    docDef: DocumentDefinition
  ): void {
    let jsonDocument: any;
    if (typeof body.jsonDocument !== 'undefined') {
      jsonDocument = body.jsonDocument;
    } else {
      jsonDocument = body.JsonDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.id;
    }

    docDef.characterEncoding = jsonDocument.characterEncoding;
    docDef.locale = jsonDocument.locale;

    for (const field of jsonDocument.fields.field) {
      this.parseJSONFieldFromDocument(field, null, docDef);
    }
  }

  private parseJSONFieldFromDocument(
    field: any,
    parentField: Field | null,
    docDef: DocumentDefinition
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }
    parsedField.enumeration = field.enumeration;
    parsedField.enumIndexValue = field.enumIndexValue
      ? field.enumIndexValue
      : 0;

    if (
      parsedField.enumeration &&
      field.jsonEnumFields &&
      field.jsonEnumFields.jsonEnumField
    ) {
      for (const enumValue of field.jsonEnumFields.jsonEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }
    if (
      field.jsonFields &&
      field.jsonFields.jsonField &&
      field.jsonFields.jsonField.length
    ) {
      for (const childField of field.jsonFields.jsonField) {
        this.parseJSONFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }
}

export class JsonInspectionRequestModel extends DocumentInspectionRequestModel {
  url = this.cfg.initCfg.baseJSONInspectionServiceUrl + 'inspect';
  options = new JsonInspectionRequestOptions(this.cfg, this.doc);
}

export class JsonInspectionRequestOptions extends DocumentInspectionRequestOptions {
  json = {
    JsonInspectionRequest: {
      jsonType: 'io.atlasmap.json.v2.JsonInspectionRequest',
      type: this.doc.inspectionType,
      jsonData: this.doc.inspectionSource,
    },
  };
}
