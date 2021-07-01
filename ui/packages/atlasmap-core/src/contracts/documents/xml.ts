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

import { IDataSource, IDocument, IField } from '../common';

/**
 * The XML inspection data model contracts between frontend and backend.
 */

export const XML_MODEL_PACKAGE_PREFIX = 'io.atlasmap.xml.v2';
export const XML_DATA_SOURCE_JSON_TYPE =
  XML_MODEL_PACKAGE_PREFIX + '.XmlDataSource';
export const XML_ENUM_FIELD_JSON_TYPE =
  XML_MODEL_PACKAGE_PREFIX + '.XmlEnumField';
export const XML_INSPECTION_REQUEST_JSON_TYPE =
  XML_MODEL_PACKAGE_PREFIX + '.XmlInspectionRequest';

/**
 * The root object that carries {@link IXmlInspectionResponse}
 * when it's sent to backend.
 */
export interface IXmlInspectionResponseContainer {
  XmlInspectionResponse: IXmlInspectionResponse;
}

/**
 * The serialized XML inspection response.
 */
export interface IXmlInspectionResponse {
  xmlDocument: IXmlDocument;
  errorMessage?: string;
  executionTime?: number;
}

/**
 * The root object that carries {@link IXmlDocument}
 * when it's inspected with maven plugin.
 */
export interface IXmlDocumentContainer {
  XmlDocument: IXmlDocument;
}

/**
 * The serialized XML document in inspection result.
 */
export interface IXmlDocument extends IDocument {
  xmlNamespaces?: IXmlNamespaces;
}

/**
 * The serialized XML DataSource.
 * @see IXmlNamespaces
 */
export interface IXmlDataSource extends IDataSource {
  template?: string;
  xmlNamespaces?: IXmlNamespaces;
}

/**
 * The container of XML namespace.
 */
export interface IXmlNamespaces {
  xmlNamespace: IXmlNamespace[];
}

/**
 * The serialized XML namespace.
 */
export interface IXmlNamespace {
  alias: string;
  uri: string;
  locationUri?: string;
  targetNamespace: boolean;
}

/**
 * The serialized XML primitive field.
 */
export interface IXmlField extends IField {
  userCreated?: boolean;
  attribute?: boolean;
}

/**
 * The serialized XML complex field.
 */
export interface IXmlComplexType extends IField {
  xmlEnumFields?: IXmlEnumFields;
  xmlFields?: IXmlFields;
  annotation?: boolean;
  anonymous?: boolean;
  enumeration: boolean;
  uri?: string;
}

/**
 * The container of XML enum field.
 */
export interface IXmlEnumFields {
  xmlEnumField: IXmlEnumField[];
}

/**
 * The serialized XML enum field.
 */
export interface IXmlEnumField extends IField {
  ordinal: number;
  typeName: string;
  attribute: boolean;
  userCreated: boolean;
}

/**
 * The container of XML field.
 */
export interface IXmlFields {
  xmlField: IXmlField[];
}
