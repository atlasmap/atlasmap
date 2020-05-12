import { IAtlasmapGroup, IAtlasmapField, IAtlasmapMapping } from "../Views";

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
      previewValue: "",
      mappings,
      hasTransformations:
        mappings.length > 0 &&
        (jf.name.startsWith("a") ||
          jf.name.startsWith("b") ||
          jf.name.startsWith("c")),
      isCollection: jf.collectionType === "LIST",
      isConnected: false,
      isDisabled: jf.collectionType === "COMPLEX",
      amField: {} as IAtlasmapField["amField"],
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
    amField: {} as IAtlasmapField["amField"],
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
//       previewValue: "",
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
