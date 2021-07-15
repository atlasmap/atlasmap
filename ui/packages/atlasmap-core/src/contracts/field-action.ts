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

import { CollectionType, FieldType } from './common';

/**
 * The field action Data model contracts between frontend and backend.
 */

/**
 * The root object that carries {@link IAActionDetails}
 * when it's received from backend.
 */
export interface IActionDetailsContainer {
  ActionDetails: IActionDetails;
}

/**
 * The container of serialized {@link IActionDetail}.
 */
export interface IActionDetails {
  actionDetail: IActionDetail[];
}

/**
 * The serialized field action detail.
 */
export interface IActionDetail {
  parameters?: IActionParameters;
  name: string;
  custom: boolean;
  className: string;
  method: string;
  sourceType: FieldType;
  targetType: FieldType;
  multiplicity: Multiplicity;
  actionSchema: IActionSchema;
}

/**
 * The container of serialized field action parameter in old style.
 */
export interface IActionParameters {
  actionParameter: IActionParameter[];
}

/**
 * The serialized field action parameter in old style.
 */
export interface IActionParameter {
  values: string[];
  name: string;
  displayName: string;
  description: string;
  fieldType: FieldType;
}

/**
 * The multiplicity of the field action.
 */
export enum Multiplicity {
  ONE_TO_ONE = 'ONE_TO_ONE',
  ONE_TO_MANY = 'ONE_TO_MANY',
  MANY_TO_ONE = 'MANY_TO_ONE',
  ZERO_TO_ONE = 'ZERO_TO_ONE',
  MANY_TO_MANY = 'MANY_TO_MANY',
}

/**
 * The newer style of field action metadata.
 */
export interface IActionSchema {
  type: string;
  id: string;
  properties: IActionSchemaProperties;
}

/**
 * The newer style of field action parameter metadata.
 */
export interface IActionSchemaProperties {
  [key: string]: {
    type: string;
    const: string;
    description: string;
    title: string;
    enum?: string[];
    'atlas-field-type'?: FieldType;
    'atlas-collection-type'?: CollectionType;
  };
}
