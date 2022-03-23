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
  CollectionType,
  DataSourceType,
  FieldType,
  IDataSource,
  IField,
  MODEL_PACKAGE_PREFIX,
} from './common';

/**
 * The mapping Data model contracts between frontend and backend.
 */

export const ATLAS_MAPPING_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.AtlasMapping';
export const MAPPING_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.Mapping';
export const COLLECTION_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.Collection';
export const FIELD_GROUP_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.FieldGroup';
export const PROPERTY_FIELD_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.PropertyField';
export const CONSTANT_FIELD_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.ConstantField';

/**
 * The root object that carries {@link IAtlasMapping}
 * when it's sent/received to/from backend.
 */
export interface IAtlasMappingContainer {
  AtlasMapping: IAtlasMapping;
}

/**
 * The serialized AtlasMap mapping definition.
 */
export interface IAtlasMapping {
  dataSource?: IDataSource[];
  mappings?: IMappings;
  lookupTables?: ILookupTables;
  constants?: IConstants;
  properties?: IProperties;
  name?: string;
  jsonType: string;
  version?: string;
}

/**
 * The container of serialized {@link IBaseMapping} held by {@link IAtlasMapping}
 */
export interface IMappings {
  mapping: IBaseMapping[];
}

/**
 * The base interface of serialized mapping entry held by {@link IMappings}.
 * @see IMapping
 * @see ICollection
 */
export interface IBaseMapping {
  alias?: string;
  description?: string;
  mappingType?: MappingType;
  jsonType: string;
}

/**
 * The serialized mapping entry held by {@link IMappings}.
 * @see IBaseMapping
 */
export interface IMapping extends IBaseMapping {
  expression?: string;
  inputFieldGroup?: IFieldGroup;
  inputField?: IField[];
  outputField: IField[];
  id: string;
  delimiter?: string;
  delimiterString?: string;
  lookupTableName?: string;
  strategy?: string;
  strategyClassName?: string;
}

/**
 * The serialized collection mapping entry held by {@link IMappings}.
 * @see IBaseMapping
 * This is no longer used ATM, but the one-to-many/many-to-one for each COMPLEX collection
 * might want to resurrect it - https://github.com/atlasmap/atlasmap/issues/1236
 */
export interface ICollection extends IBaseMapping {
  mappings: IMappings;
  collectionSize: number;
  collectionType: CollectionType;
}

/**
 * The mapping mode, such as {@link LOOKUP}, {@link COLLECTION}, etc.
 * @deprecated {@link COMBINE}, {@link MAP}, {@link SEPARATE}
 */
export enum MappingType {
  ALL = 'ALL',
  COLLECTION = 'COLLECTION',
  COMBINE = 'COMBINE',
  LOOKUP = 'LOOKUP',
  MAP = 'MAP',
  SEPARATE = 'SEPARATE',
  NONE = 'NONE',
}

/**
 * The group of fields in the mapping.
 */
export interface IFieldGroup extends IField {
  field?: IField[];
}

/**
 * The container of serialized {@link ILookupTable}.
 */
export interface ILookupTables {
  lookupTable?: ILookupTable[];
}

/**
 * The container of serialized LookupTable.
 * @see ILookupEntry
 */
export interface ILookupTable {
  lookupEntry: ILookupEntry[];
  name: string;
  description?: string;
}

/**
 * The serialized lookup table entry.
 * @see {@link ILookupTable}
 */
export interface ILookupEntry {
  sourceValue: string;
  sourceType: FieldType;
  targetValue: string;
  targetType: FieldType;
}

/**
 * The container of serialized Constant.
 * @see IConstant
 */
export interface IConstants {
  constant?: IConstant[];
}

/**
 * The serialized Constant.
 */
export interface IConstant {
  name: string;
  value: string;
  fieldType: FieldType;
}

/**
 * The container of serialized Property.
 */
export interface IProperties {
  property?: IProperty[];
}

/**
 * The serialized Property.
 */
export interface IProperty {
  name: string;
  value?: string;
  fieldType: FieldType;
  scope?: string;
  dataSourceType?: DataSourceType;
}

/**
 * The serialized JSON DataSource.
 * @see IDataSource
 */
export interface IJsonDataSource extends IDataSource {
  template?: string;
}

/**
 * The serialized property field in the mapping.
 */
export interface IPropertyField extends IField {
  scope?: string;
}

/**
 * The root object that carries {@link IValidation}
 * when it's received from backend as a result of
 * mapping validation.
 */
export interface IValidationsContainer {
  Validations: IValidations;
}

/**
 * THe container of serialized validation result.
 */
export interface IValidations {
  validation: IValidation[];
}

/**
 * The validation result.
 */
export interface IValidation {
  message?: string;
  id?: string;
  docId?: string;
  docName?: string;
  scope?: ValidationScope;
  status?: ValidationStatus;
}

/**
 * The validation scope.
 */
export enum ValidationScope {
  DATA_SOURCE = 'DATA_SOURCE',
  MAPPING = 'MAPPING',
  LOOKUP_TABLE = 'LOOKUP_TABLE',
  CONSTANT = 'CONSTANT',
  PROPERTY = 'PROPERTY',
}

/**
 * The validation status.
 */
export enum ValidationStatus {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

/**
 * The root object that carries {@link IStringMap}
 * when it's received from backend.
 */
export interface IStringMapContainer {
  StringMap: IStringMap;
}

/**
 * The serialized string map, used for deliverying
 * a list of mapping name.
 */
export interface IStringMap {
  stringMapEntry: IStringMapEntry[];
}

/**
 * The string map entry with name and value.
 */
export interface IStringMapEntry {
  name: string;
  value: string;
}
