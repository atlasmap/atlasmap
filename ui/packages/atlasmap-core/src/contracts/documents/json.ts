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

import { IDocument, IField, IStringList, InspectionType } from '../common';

/**
 * The JSON inspection data model contracts between frontend and backend.
 */
export const JSON_MODEL_PACKAGE_PREFIX = 'io.atlasmap.json.v2';
export const JSON_DATA_SOURCE_JSON_TYPE =
  JSON_MODEL_PACKAGE_PREFIX + '.JsonDataSource';
export const JSON_ENUM_FIELD_JSON_TYPE =
  JSON_MODEL_PACKAGE_PREFIX + '.JsonEnumField';
export const JSON_INSPECTION_REQUEST_JSON_TYPE =
  JSON_MODEL_PACKAGE_PREFIX + '.JsonInspectionRequest';

/**
 * The root object that carries {@link IJsonInspectionRequest}
 * when it's sent to backend.
 */
export interface IJsonInspectionRequestContainer {
  JsonInspectionRequest: IJsonInspectionRequest;
}

/**
 * The serialized JSON inspection request.
 */
export interface IJsonInspectionRequest {
  fieldNameExclusions?: IStringList;
  typeNameExclusions?: IStringList;
  namespaceExclusions?: IStringList;
  jsonData?: string;
  uri?: string;
  type: InspectionType;
}

/**
 * The root object that carries {@link IJsonInspectionResponse}
 * when it's received from backend.
 */
export interface IJsonInspectionResponseContainer {
  JsonInspectionResponse: IJsonInspectionResponse;
}

/**
 * The serialized JSON inspection response.
 */
export interface IJsonInspectionResponse {
  jsonDocument: IJsonDocument;
  errorMessage?: string;
  executionTime?: number;
}

/**
 * The root object that carries {@link IJsonDocument}
 * when it's inspected with maven plugin.
 */
export interface IJsonDocumentContainer {
  JsonDocument: IJsonDocument;
}

/**
 * The serialized JSON document inspection result.
 */
export interface IJsonDocument extends IDocument {}

/**
 * The serialized JSON primitive field in inspection result.
 */
export interface IJsonField extends IField {
  primitive: boolean;
  typeName: string;
  userCreated?: boolean;
}

/**
 * The serialized JSON complex field in inspection result.
 */
export interface IJsonComplexType extends IJsonField {
  jsonFields?: IJsonFields;
  jsonEnumFields?: IJsonEnumFields;
  enumeration: boolean;
  uri?: string;
}

/**
 * The container of JSON field.
 */
export interface IJsonFields {
  jsonField: IJsonField[];
}

/**
 * The container of JSON enum field.
 */
export interface IJsonEnumFields {
  jsonEnumField: IJsonEnumField[];
}

/**
 * The serialized JSON enum field.
 */
export interface IJsonEnumField extends IField {
  ordinal: number;
  typeName: string;
  userCreated?: boolean;
}
