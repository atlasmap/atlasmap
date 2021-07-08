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

import { IDocument, IField, IParameter } from '../common';

/**
 * The CSV inspection data model contracts between frontend and backend.
 */

/**
 * The root object that carries {@link ICsvInspectionResponse}
 * when it's received from backend.
 */
export interface ICsvInspectionResponseContainer {
  CsvInspectionResponse: ICsvInspectionResponse;
}

/**
 * The serialized CSV inspection response.
 */
export interface ICsvInspectionResponse {
  csvDocument: IDocument;
  errorMessage?: string;
  executionTime?: number;
}

/**
 * The root object that carries {@link ICsvDocument}
 * when it's inspected with maven plugin.
 */
export interface ICsvDocumentContainer {
  CsvDocument: IDocument;
}

/**
 * The serialized CSV complex field.
 */
export interface ICsvComplexType extends ICsvField {
  csvFields: ICsvFields;
  uri?: string;
}

/**
 * The serialized CSV primitive field.
 */
export interface ICsvField extends IField {
  column: number;
}

/**
 * The container of {@link ICsvField}.
 */
export interface ICsvFields {
  csvField: ICsvField[];
}

// TODO: Retrieve from the backend CSV module
export function getCsvParameterOptions(): IParameter[] {
  return [
    {
      name: 'format',
      label: 'CSV File Format',
      value: 'Default',
      options: [
        { label: 'Default', value: 'Default' },
        { label: 'Excel', value: 'Excel' },
        { label: 'InformixUnload', value: 'InformixUnload' },
        { label: 'InformixUnloadCsv', value: 'InformixUnloadCsv' },
        { label: 'MongoDBCsv', value: 'MongoDBCsv' },
        { label: 'MongoDBTsv', value: 'MongoDBTsv' },
        { label: 'MySQL', value: 'MySQL' },
        { label: 'Oracle', value: 'Oracle' },
        { label: 'PostgreSQLCsv', value: 'PostgreSQLCsv' },
        { label: 'PostgreSQLText', value: 'PostgreSQLText' },
        { label: 'RFC4180', value: 'RFC4180' },
        { label: 'TDF', value: 'TDF' },
      ],
      required: true,
    },
    {
      name: 'allowDuplicateHeaderNames',
      label: 'Allow Duplicate Header Names',
      value: 'true',
      boolean: true,
      required: false,
    },
    {
      name: 'allowMissingColumnNames',
      label: 'Allow Missing Column Names',
      value: 'true',
      boolean: true,
      required: false,
    },
    {
      name: 'commentMarker',
      label: 'Comment Marker',
      value: '',
      required: false,
    },
    {
      name: 'delimiter',
      label: 'Delimiter',
      value: '',
      required: false,
    },
    { name: 'escape', label: 'Escape', value: '', required: false },
    {
      name: 'firstRecordAsHeader',
      label: 'First Record As Header',
      value: 'true',
      boolean: true,
      required: false,
    },
    { name: 'headers', label: 'Headers', value: '', required: false },
    {
      name: 'ignoreEmptyLines',
      label: 'Ignore Empty Lines',
      value: 'true',
      boolean: true,
      required: false,
    },
    {
      name: 'ignoreHeaderCase',
      label: 'Ignore Header Case',
      value: 'true',
      boolean: true,
      required: false,
    },
    {
      name: 'ignoreSurroundingSpaces',
      label: 'Ignore Surrounding Spaces',
      value: 'true',
      boolean: true,
      required: false,
    },
    {
      name: 'nullString',
      label: 'Null String',
      value: '',
      required: false,
    },
    {
      name: 'skipHeaderRecord',
      label: 'Skip Header Record',
      value: 'true',
      boolean: true,
      required: false,
    },
  ];
}
