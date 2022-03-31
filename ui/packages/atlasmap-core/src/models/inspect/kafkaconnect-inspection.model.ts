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
import { DocumentType, FieldType } from '../../contracts/common';
import { EnumValue, Field } from '../field.model';
import {
  IKafkaConnectComplexType,
  IKafkaConnectDocument,
  IKafkaConnectDocumentContainer,
  IKafkaConnectField,
  IKafkaConnectInspectionRequestContainer,
  IKafkaConnectInspectionResponse,
  IKafkaConnectInspectionResponseContainer,
  KAFKACONNECT_INSPECTION_REQUEST_JSON_TYPE,
} from '../../contracts/documents/kafkaconnect';
/**
 * Encapsulates Kafka Connect inspection context.
 */
export class KafkaConnectInspectionModel extends DocumentInspectionModel {
  documentTypeName = 'Kafka Connect';
  baseUrl = this.cfg.initCfg.baseKafkaConnectInspectionServiceUrl;
  request = new KafkaConnectInspectionRequestModel(
    this.cfg,
    this.doc,
    this.baseUrl
  );

  parseResponse(responseKafkaConnect: any): void {
    if (
      typeof responseKafkaConnect.KafkaConnectInspectionResponse !== 'undefined'
    ) {
      this.extractKafkaConnectDocumentDefinitionFromInspectionResponse(
        (responseKafkaConnect as IKafkaConnectInspectionResponseContainer)
          .KafkaConnectInspectionResponse
      );
    } else if (
      typeof responseKafkaConnect.KafkaConnectDocument !== 'undefined'
    ) {
      this.extractKafkaConnectDocumentDefinition(
        (responseKafkaConnect as IKafkaConnectDocumentContainer)
          .KafkaConnectDocument
      );
    } else {
      throw new Error(
        `Unknown Kafka Connect inspection result format: ${responseKafkaConnect}`
      );
    }
  }

  private extractKafkaConnectDocumentDefinitionFromInspectionResponse(
    body: IKafkaConnectInspectionResponse
  ): void {
    if (body.errorMessage) {
      this.doc.errorOccurred = true;
      throw new Error(
        `Could not load Kafka Connect document, error: ${body.errorMessage}`
      );
    }

    this.extractKafkaConnectDocumentDefinition(body.kafkaConnectDocument);
  }

  private parseKafkaConnectFieldFromDocument(
    field: IKafkaConnectField,
    parentField: Field | null
  ): void {
    const parsedField = this.parseFieldFromDocument(field, parentField);
    if (parsedField == null) {
      return;
    }
    if (field.fieldType !== FieldType.COMPLEX) {
      return;
    }
    const complex = field as IKafkaConnectComplexType;
    parsedField.enumeration = complex.enumeration;
    if (
      parsedField.enumeration &&
      complex.kafkaConnectEnumFields?.kafkaConnectEnumField
    ) {
      for (const enumValue of complex.kafkaConnectEnumFields
        .kafkaConnectEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name!;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }
    if (complex.kafkaConnectFields?.kafkaConnectField.length) {
      for (const childField of complex.kafkaConnectFields.kafkaConnectField) {
        this.parseKafkaConnectFieldFromDocument(childField, parsedField);
      }
    }
  }

  private extractKafkaConnectDocumentDefinition(
    kafkaConnectDocument: IKafkaConnectDocument
  ): void {
    if (!this.doc.description) {
      this.doc.description = this.doc.id;
    }
    if (!this.doc.name) {
      this.doc.name = this.doc.id;
    }
    if (kafkaConnectDocument.fields.field.length === 0) {
      let primitiveField: IKafkaConnectField =
        kafkaConnectDocument as unknown as IKafkaConnectField;
      primitiveField.name = '/'; // dont use the primitive field subfield name
      this.parseKafkaConnectFieldFromDocument(primitiveField, null);
      return;
    }
    for (const field of kafkaConnectDocument.fields.field) {
      this.parseKafkaConnectFieldFromDocument(
        field as IKafkaConnectField,
        null
      );
    }
  }
}

export class KafkaConnectInspectionRequestModel extends DocumentInspectionRequestModel {
  options = new KafkaConnectInspectionRequestOptions(this.cfg, this.doc);
}

export class KafkaConnectInspectionRequestOptions extends DocumentInspectionRequestOptions {
  documentType = this?.doc.type === DocumentType.KAFKA_AVRO ? 'AVRO' : 'JSON';
  json: IKafkaConnectInspectionRequestContainer = {
    KafkaConnectInspectionRequest: {
      jsonType: KAFKACONNECT_INSPECTION_REQUEST_JSON_TYPE,
      inspectionType: this.doc.inspectionType,
      schemaData: this.doc.inspectionSource,
      options: { schemaType: this.documentType },
    },
  };
}
