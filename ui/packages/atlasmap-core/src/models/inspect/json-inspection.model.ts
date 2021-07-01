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
import {
  IJsonComplexType,
  IJsonDocument,
  IJsonDocumentContainer,
  IJsonField,
  IJsonInspectionResponse,
  IJsonInspectionResponseContainer,
  JSON_INSPECTION_REQUEST_JSON_TYPE,
} from '../../contracts/documents/json';
import { FieldType } from '../../contracts/common';

/**
 * Encapsulates JSON inspection context.
 */
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
        (responseJson as IJsonInspectionResponseContainer)
          .JsonInspectionResponse
      );
    } else if (typeof responseJson.JsonDocument !== 'undefined') {
      this.extractJSONDocumentDefinition(
        (responseJson as IJsonDocumentContainer).JsonDocument
      );
    } else {
      throw new Error(`Unknown JSON inspection result format: ${responseJson}`);
    }
  }

  private extractJSONDocumentDefinitionFromInspectionResponse(
    body: IJsonInspectionResponse
  ): void {
    if (body.errorMessage) {
      this.doc.errorOccurred = true;
      throw new Error(
        `Could not load JSON document, error: ${body.errorMessage}`
      );
    }

    this.extractJSONDocumentDefinition(body.jsonDocument);
  }

  private extractJSONDocumentDefinition(jsonDocument: IJsonDocument): void {
    if (!this.doc.description) {
      this.doc.description = this.doc.id;
    }
    if (!this.doc.name) {
      this.doc.name = this.doc.id;
    }

    for (const field of jsonDocument.fields.field) {
      this.parseJSONFieldFromDocument(field as IJsonField, null);
    }
  }

  private parseJSONFieldFromDocument(
    field: IJsonField,
    parentField: Field | null
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField);
    if (parsedField == null) {
      return;
    }
    if (field.fieldType !== FieldType.COMPLEX) {
      return;
    }
    const complex = field as IJsonComplexType;
    parsedField.enumeration = complex.enumeration;
    /** FIXME enumIndexValue doesn't exist on JsonField/JsonComplexType
    parsedField.enumIndexValue = complex.enumIndexValue
      ? complex.enumIndexValue
      : 0;
    */
    if (parsedField.enumeration && complex.jsonEnumFields?.jsonEnumField) {
      for (const enumValue of complex.jsonEnumFields.jsonEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name!;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }
    if (complex.jsonFields?.jsonField.length) {
      for (const childField of complex.jsonFields.jsonField) {
        this.parseJSONFieldFromDocument(childField, parsedField);
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
      jsonType: JSON_INSPECTION_REQUEST_JSON_TYPE,
      type: this.doc.inspectionType,
      jsonData: this.doc.inspectionSource,
    },
  };
}
