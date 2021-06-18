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
import { Field, MappingModel } from '@atlasmap/core';
import { ITransformationArgument, ITransformationSelectOption } from '../UI';
import { AlertProps } from '@patternfly/react-core';

export type ElementId = string;
export type GroupId = string;

export type AtlasmapDocumentType = 'source' | 'target';

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField {
  id: ElementId;
  name: string;
  type: string;
  scope: string;
  value: string;
  path: string;
  mappings: IAtlasmapMapping[];
  hasTransformations: boolean;
  isAttribute: boolean;
  isCollection: boolean;
  isConnected: boolean;
  isInCollection: boolean;
  isDisabled: boolean;
  enumeration: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapGroup {
  id: GroupId;
  fields: (IAtlasmapField | IAtlasmapGroup)[];
  name: string;
  type: string;
  isCollection: boolean;
  isInCollection: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapNamespace {
  alias: string;
  uri: string;
  locationUri: string;
  isTarget: boolean;
}

export type AtlasmapNamespaces = Array<IAtlasmapNamespace>;

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
  namespaces?: AtlasmapNamespaces;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  transformations: Array<{
    name: string;
    options: Array<ITransformationSelectOption>;
    arguments: Array<ITransformationArgument>;
  }>;
}

export interface IAtlasmapMapping {
  id: string;
  name: string;
  sourceFields: Array<IAtlasmapMappedField>;
  targetFields: Array<IAtlasmapMappedField>;
  mapping: MappingModel;
}

export interface INotification {
  id: string;
  variant: AlertProps['variant'];
  title: string;
  description: string;
  isRead?: boolean;
  mappingId?: string;
}
