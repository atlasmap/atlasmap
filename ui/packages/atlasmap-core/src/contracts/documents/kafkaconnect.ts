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

import { IDocument, IField, IStringList } from '../common';
/**
 * The KafkaConnect inspection data model contracts between frontend and backend.
 */
export const KAFKACONNECT_MODEL_PACKAGE_PREFIX = 'io.atlasmap.kafkaconnect.v2';
export const KAFKACONNECT_INSPECTION_REQUEST_JSON_TYPE =
  KAFKACONNECT_MODEL_PACKAGE_PREFIX + '.KafkaConnectInspectionRequest';

/**
 * The root object that carries {@link IKafkaConnectInspectionRequest}
 * when it's sent to backend.
 */
export interface IKafkaConnectInspectionRequestContainer {
  KafkaConnectInspectionRequest: IKafkaConnectInspectionRequest;
}

/**
 * The serialized Kafka Connect inspection request.
 */
export interface IKafkaConnectInspectionRequest {
  fieldNameExclusions?: IStringList;
  typeNameExclusions?: IStringList;
  namespaceExclusions?: IStringList;
  uri?: string;
  schemaData?: string;
  options?: Map<string, string>;
}

/**
 * The root object that carries {@link IKafkaConnectInspectionResponse}
 * when it's received from backend.
 */
export interface IKafkaConnectInspectionResponseContainer {
  KafkaConnectInspectionResponse: IKafkaConnectInspectionResponse;
}

/**
 * The serialized Kafka Connect inspection response.
 */
export interface IKafkaConnectInspectionResponse {
  kafkaConnectDocument: IKafkaConnectDocument;
  errorMessage?: string;
  executionTime?: number;
}

/**
 * The root object that carries {@link IKafkaConnectDocument}
 * when it's inspected with maven plugin.
 */
export interface IKafkaConnectDocumentContainer {
  KafkaConnectDocument: IKafkaConnectDocument;
}

/**
 * The serialized Kafka Connect document inspection result.
 */
export interface IKafkaConnectDocument extends IDocument {
  /** True if it's an enum. */
  enumeration?: boolean;
  /** Enum fields. */
  enumFields?: IKafkaConnectEnumFields;
}

/**
 * The serialized Kafka Connect primitive field in inspection result.
 */
export interface IKafkaConnectField extends IField {
  primitive: boolean;
  typeName: string;
  userCreated?: boolean;
}

/**
 * The serialized Kafka Connect complex field in inspection result.
 */
export interface IKafkaConnectComplexType extends IKafkaConnectField {
  kafkaConnectFields?: IKafkaConnectFields;
  kafkaConnectEnumFields?: IKafkaConnectEnumFields;
  enumeration: boolean;
  uri?: string;
}

/**
 * The container of Kafka Connect field.
 */
export interface IKafkaConnectFields {
  kafkaConnectField: IKafkaConnectField[];
}

/**
 * The container of Kafka Connect enum field.
 */
export interface IKafkaConnectEnumFields {
  kafkaConnectEnumField: IKafkaConnectEnumField[];
}

/**
 * The serialized Kafka Connect enum field.
 */
export interface IKafkaConnectEnumField extends IField {
  ordinal: number;
  typeName: string;
  userCreated?: boolean;
}
