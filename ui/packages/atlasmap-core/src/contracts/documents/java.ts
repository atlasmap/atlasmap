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
import { CollectionType, IField, IStringList } from '../common';

/**
 * The Java class inspection data model contracts between frontend and backend.
 */
export const JAVA_MODEL_PACKAGE_PREFIX = 'io.atlasmap.java.v2';
export const JAVA_INSPECTION_REQUEST_JSON_TYPE =
  JAVA_MODEL_PACKAGE_PREFIX + '.ClassInspectionRequest';
export const JAVA_CLASS_JSON_TYPE = JAVA_MODEL_PACKAGE_PREFIX + '.JavaClass';
export const JAVA_ENUM_FIELD_JSON_TYPE =
  JAVA_MODEL_PACKAGE_PREFIX + '.JavaEnumField';

/**
 * The root object that carries {@link IClassInspectionRequest}
 * when it's sent to backend.
 */
export interface IClassInspectionRequestContainer {
  ClassInspectionRequest: IClassInspectionRequest;
}

/**
 * The serialized Java class inspection request.
 */
export interface IClassInspectionRequest {
  jsonType: string;
  fieldNameExclusions?: IStringList;
  classNameExclusions?: IStringList;
  classpath?: string;
  className: string;
  collectionType?: CollectionType;
  collectionClassName?: string;
  disablePrivateOnlyFields?: boolean;
  disableProtectedOnlyFields?: boolean;
  disablePublicOnlyFields?: boolean;
  disablePublicGetterSetterFields?: boolean;
}

/**
 * The root object that carries {@link IClassInspectionResponse}
 * when it's received from backend.
 */
export interface IClassInspectionResponseContainer {
  ClassInspectionResponse: IClassInspectionResponse;
}

/**
 * The serialized Java class inspection response.
 */
export interface IClassInspectionResponse {
  javaClass: IJavaClass;
  errorMessage: string;
  executionTime: number;
}

/**
 * The root object that carries {@link IJavaClass}
 * when it's read from Java offline inspection (maven plugin).
 */
export interface IJavaClassContainer {
  JavaClass: IJavaClass;
}

/**
 * The serialized Java class inspection result.
 */
export interface IJavaClass extends IJavaField {
  javaEnumFields: IJavaEnumFields;
  javaFields: IJavaFields;
  packageName: string;
  annotation: boolean;
  annonymous: boolean;
  enumeration: boolean;
  isInterface: boolean;
  localClass: boolean;
  memberClass: boolean;
  uri: string;
}

/**
 * The serialized Java field.
 */
export interface IJavaField extends IField {
  annotations?: IStringList;
  modifiers?: { modifier: Modifier[] };
  parameterizedTypes?: IStringList;
  className?: string;
  canonicalClassName?: string;
  collectionClassName?: string;
  getMethod?: string;
  setMethod?: string;
  primitive?: boolean;
  synthetic?: boolean;
}

/**
 * The serialized Java modifier.
 */
export enum Modifier {
  ALL = 'ALL',
  ABSTRACT = 'ABSTRACT',
  FINAL = 'FINAL',
  INTERFACE = 'INTERFACE',
  NATIVE = 'NATIVE',
  PACKAGE_PRIVATE = 'Package Private',
  PUBLIC = 'PUBLIC',
  PROTECTED = 'PROTECTED',
  PRIVATE = 'PRIVATE',
  STATIC = 'STATIC',
  STRICT = 'STRICT',
  SYNCHRONIZED = 'SYNCHRONIZED',
  TRANSIENT = 'TRANSIENT',
  VOLATILE = 'VOLATILE',
  NONE = 'NONE',
}

/**
 * The container of serialized {@link IJavaEnumField}.
 */
export interface IJavaEnumFields {
  javaEnumField: IJavaEnumField[];
}

/**
 * The serialized Java enum field.
 */
export interface IJavaEnumField extends IField {
  name: string;
  ordinal: number;
  className: string;
}

/**
 * The container of serialized {@link IJavaField}.
 */
export interface IJavaFields {
  javaField: IJavaField[];
}
