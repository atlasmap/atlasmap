/*
    Copyright (C) 2018 Red Hat, Inc.

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

export enum DocumentType {
  JAVA = 'JAVA',
  JAVA_ARCHIVE = 'JAR',
  XML = 'XML',
  XSD = 'XSD',
  JSON = 'JSON',
  CORE = 'Core',
  CSV = 'CSV',
  CONSTANT = 'Constants',
  PROPERTY = 'Property'
}

export enum InspectionType {
  JAVA_CLASS = 'JAVA_CLASS',
  SCHEMA = 'SCHEMA',
  INSTANCE = 'INSTANCE',
  UNKNOWN = 'UNKNOWN'
}

export const enum FieldMode {
  CREATE,
  EDIT
}

export enum CollectionType {
  ARRAY = 'ARRAY',
  LIST = 'LIST',
  MAP = 'MAP',
  NONE = 'NONE'
}

export const collectionTypes = [
  [CollectionType[CollectionType.ARRAY], 'Array'],
  [CollectionType[CollectionType.LIST], 'List'],
  [CollectionType[CollectionType.MAP], 'Map'],
  [CollectionType[CollectionType.NONE], 'None'],
]

export const constantTypes = [
  ['BOOLEAN', 'Boolean'],
  ['BIG_INTEGER', 'Big Integer'],
  ['BYTE', 'Byte'],
  ['BYTE_ARRAY', 'ByteArray'],
  ['CHAR', 'Char'],
  ['COMPLEX', 'Complex'],
  ['DECIMAL', 'Decimal'],
  ['DOUBLE', 'Double'],
  ['FLOAT', 'Float'],
  ['INTEGER', 'Integer'],
  ['LONG', 'Long'],
  ['SHORT', 'Short'],
  ['STRING', 'String'],
  ['TIME', 'Time'],
  ['DATE', 'Date'],
  ['DATE_TIME', 'DateTime'],
  ['DATE_TZ', 'DateTZ'],
  ['TIME_TZ', 'TimeTZ'],
  ['DATE_TIME_TZ', 'DateTimeTZ'],
  ['UNSIGNED_BYTE', 'Unsigned Byte'],
  ['UNSIGNED_INTEGER', 'Unsigned Integer'],
  ['UNSIGNED_LONG', 'Unsigned Long'],
  ['UNSIGNED_SHORT', 'Unsigned Short'],
]

export const propertyTypes = [
  ['ANY', 'Any'],
  ['BOOLEAN', 'Boolean'],
  ['BIG_INTEGER', 'Big Integer'],
  ['BYTE', 'Byte'],
  ['BYTE_ARRAY', 'ByteArray'],
  ['CHAR', 'Char'],
  ['COMPLEX', 'Complex'],
  ['DECIMAL', 'Decimal'],
  ['DOUBLE', 'Double'],
  ['FLOAT', 'Float'],
  ['INTEGER', 'Integer'],
  ['LONG', 'Long'],
  ['SHORT', 'Short'],
  ['STRING', 'String'],
  ['TIME', 'Time'],
  ['DATE', 'Date'],
  ['DATE_TIME', 'DateTime'],
  ['DATE_TZ', 'DateTZ'],
  ['TIME_TZ', 'TimeTZ'],
  ['DATE_TIME_TZ', 'DateTimeTZ'],
  ['UNSIGNED_BYTE', 'Unsigned Byte'],
  ['UNSIGNED_INTEGER', 'Unsigned Integer'],
  ['UNSIGNED_LONG', 'Unsigned Long'],
  ['UNSIGNED_SHORT', 'Unsigned Short'],
]
