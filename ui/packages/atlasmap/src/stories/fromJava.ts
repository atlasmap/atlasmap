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
import { IAtlasmapField, IAtlasmapGroup, IAtlasmapMapping } from '../Views';

interface Modifiers {
  modifier: string[];
}

interface JavaEnumFields {
  javaEnumField: any[];
}

interface JavaField {
  jsonType: string;
  path: string;
  status: string;
  fieldType: string;
  modifiers: Modifiers;
  name: string;
  className: string;
  canonicalClassName: string;
  primitive: boolean;
  synthetic: boolean;
  arrayDimensions?: number;
  collectionType?: string;
  javaEnumFields?: JavaEnumFields;
  javaFields?: JavaFields;
  packageName?: string;
  annotation?: boolean;
  annonymous?: boolean;
  enumeration?: boolean;
  localClass?: boolean;
  memberClass?: boolean;
  uri?: string;
  interface?: boolean;
}

interface JavaFields {
  javaField: JavaField[];
}

interface JavaClass {
  jsonType: string;
  path: string;
  fieldType: string;
  modifiers: Modifiers;
  className: string;
  canonicalClassName: string;
  primitive: boolean;
  synthetic: boolean;
  javaEnumFields?: JavaEnumFields;
  javaFields?: JavaFields;
  packageName?: string;
  annotation: boolean;
  annonymous: boolean;
  enumeration: boolean;
  localClass: boolean;
  memberClass: boolean;
  uri?: string;
  interface: boolean;
}

interface ClassInspectionResponse {
  jsonType: string;
  javaClass: JavaClass;
  executionTime: number;
}

export interface JavaObject {
  ClassInspectionResponse: ClassInspectionResponse;
}

export function javaToFieldGroup(
  java: JavaObject,
  idPrefix: string,
  allMappings: IAtlasmapMapping[],
) {
  const fromElement = (jf: JavaField): IAtlasmapField => {
    const id = `${idPrefix}-${jf.path}`;
    const mappings = allMappings.filter(
      (m) =>
        m.sourceFields.find((s) => s.id === id) ||
        m.targetFields.find((t) => t.id === id),
    );

    return {
      id: `${idPrefix}-${jf.path}`,
      name: jf.name,
      type: jf.fieldType,
      path: jf.path,
      scope: 'current',
      value: '',
      mappings,
      hasTransformations:
        mappings.length > 0 &&
        (jf.name.startsWith('a') ||
          jf.name.startsWith('b') ||
          jf.name.startsWith('c')),
      isCollection: jf.collectionType === 'LIST',
      isAttribute: false,
      isInCollection: false,
      isConnected: false,
      isDisabled: jf.collectionType === 'COMPLEX',
      amField: {} as IAtlasmapField['amField'],
      enumeration: false,
    };
  };
  const fromGroup = (f: JavaField): IAtlasmapGroup => ({
    name: f.name,
    type: f.fieldType,
    id: `${idPrefix}-${f.path}`,
    fields: f.javaFields!.javaField.map((f) =>
      f.javaFields ? fromGroup(f as JavaField) : fromElement(f),
    ),
    isCollection: false,
    isInCollection: false,
    amField: {} as IAtlasmapField['amField'],
  });

  return java.ClassInspectionResponse.javaClass.javaFields!.javaField.map((f) =>
    f.javaFields ? fromGroup(f as JavaField) : fromElement(f),
  );
}
// export function javaToFieldGroup(java, idPrefix, allMappings) {
//   const fromElement = (jf) => {
//     const id = `${idPrefix}-${jf.path}`;
//     const mappings = allMappings.filter(
//       (m) =>
//         m.sourceFields.find((s) => s.id === id) ||
//         m.targetFields.find((t) => t.id === id)
//     );

//     return {
//       id: `${idPrefix}-${jf.path}`,
//       name: jf.name,
//       type: jf.fieldType,
//       value: "",
//       mappings,
//       hasTransformations:
//         mappings.length > 0 &&
//         (jf.name.startsWith("a") ||
//           jf.name.startsWith("b") ||
//           jf.name.startsWith("c")),
//       isCollection: jf.collectionType === "LIST",
//     };
//   };
//   const fromGroup = (f) => ({
//     name: f.name,
//     type: f.fieldType,
//     id: `${idPrefix}-${f.path}`,
//     fields: f.javaFields.javaField
//       .map((f) => (f.javaFields ? fromGroup(f) : fromElement(f)))
//       .sort((a, b) => a.name.localeCompare(b.name)),
//     isCollection: f.collectionType === "LIST",
//   });

//   return java.ClassInspectionResponse.javaClass.javaFields.javaField
//     .map((f) => (f.javaFields ? fromGroup(f) : fromElement(f)))
//     .sort((a, b) => a.name.localeCompare(b.name));
// }
