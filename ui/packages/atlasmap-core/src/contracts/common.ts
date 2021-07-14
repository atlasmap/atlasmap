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

export const FIELD_PATH_SEPARATOR = '/';

export const MODEL_PACKAGE_PREFIX = 'io.atlasmap.v2';
export const DATA_SOURCE_JSON_TYPE = MODEL_PACKAGE_PREFIX + '.DataSource';

/** SOURCE or TARGET. */
export enum DataSourceType {
  SOURCE = 'SOURCE',
  TARGET = 'TARGET',
}

/**
 * The type of collection, such as {@link ARRAY} and {@link LIST}.
 */
export enum CollectionType {
  ALL = 'ALL',
  ARRAY = 'ARRAY',
  LIST = 'LIST',
  MAP = 'MAP',
  NONE = 'NONE',
}

/**
 * The field status held by {@link IField}.
 */
export enum FieldStatus {
  SUPPORTED = 'SUPPORTED',
  UNSUPPORTED = 'UNSUPPORTED',
  CACHED = 'CACHED',
  ERROR = 'ERROR',
  NOT_FOUND = 'NOT_FOUND',
  EXCLUDED = 'EXCLUDED',
}

/**
 * The field type held by {@link IField}.
 */
export enum FieldType {
  ANY = 'ANY',
  ANY_DATE = 'ANY_DATE',
  BIG_INTEGER = 'BIG_INTEGER',
  BOOLEAN = 'BOOLEAN',
  BYTE = 'BYTE',
  BYTE_ARRAY = 'BYTE_ARRAY',
  CHAR = 'CHAR',
  COMPLEX = 'COMPLEX',
  DATE = 'DATE',
  DATE_TIME = 'DATE_TIME',
  DATE_TIME_TZ = 'DATE_TIME_TZ',
  DATE_TZ = 'DATE_TZ',
  DECIMAL = 'DECIMAL',
  DOUBLE = 'DOUBLE',
  FLOAT = 'FLOAT',
  INTEGER = 'INTEGER',
  LONG = 'LONG',
  NONE = 'NONE',
  NUMBER = 'NUMBER',
  SHORT = 'SHORT',
  STRING = 'STRING',
  TIME = 'TIME',
  TIME_TZ = 'TIME_TZ',
  UNSIGNED_BYTE = 'UNSIGNED_BYTE',
  UNSIGNED_INTEGER = 'UNSIGNED_INTEGER',
  UNSIGNED_LONG = 'UNSIGNED_LONG',
  UNSIGNED_SHORT = 'UNSIGNED_SHORT',
  UNSUPPORTED = 'UNSUPPORTED',
}

export enum DocumentType {
  JAVA = 'JAVA',
  XML = 'XML',
  XSD = 'XSD',
  JSON = 'JSON',
  CORE = 'Core',
  CSV = 'CSV',
  CONSTANT = 'Constants',
  PROPERTY = 'Property',
}

export enum InspectionType {
  JAVA_CLASS = 'JAVA_CLASS',
  SCHEMA = 'SCHEMA',
  INSTANCE = 'INSTANCE',
  UNKNOWN = 'UNKNOWN',
}

/** The serialized DataSource held by {@link IAtlasMapping}. */
export interface IDataSource {
  id: string;
  name?: string;
  description?: string;
  uri: string;
  dataSourceType: DataSourceType;
  characterEncoding?: string;
  locale?: string;
  jsonType: string;
}

/**
 * The serialized CSV document inspection result.
 */
export interface IDocument {
  fields: IFields;
}

/**
 * The container of {@link IField}.
 */
export interface IFields {
  field: IField[];
}

/**
 * The field in the mapping.
 */
export interface IField {
  actions?: IFieldAction[];
  value?: string;
  arrayDimensions?: number;
  arraySize?: number;
  collectionType?: CollectionType;
  docId?: string;
  index?: number;
  path?: string;
  required?: boolean;
  status?: FieldStatus;
  fieldType?: FieldType;
  format?: string;
  name?: string;
  jsonType: string;
}

/**
 * The field action, aka transformation in the mapping.
 */
export interface IFieldAction {
  '@type'?: string;
  displayName?: string;
  [x: string]: any;
}

export interface IStringList {
  string: string[];
}

export interface IParameterOption {
  label: string;
  value: string;
}

export interface IParameter {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  enabled?: boolean;
  required?: boolean;
}

export interface IStringContainer {
  String: string;
}
