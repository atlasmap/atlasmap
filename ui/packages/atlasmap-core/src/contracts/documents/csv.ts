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

import { IDocument, IField } from '../common';

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
