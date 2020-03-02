import { IAtlasmapDocument } from '../../src/AtlasmapUI';

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

export function javaToFieldGroup(java: JavaObject, idPrefix: string) {
  const fromElement = (jf: JavaField) => ({
    id: `${idPrefix}-${jf.path}`,
    name: jf.name,
    type: jf.fieldType,
    previewValue: '',
    isVisible: () => true,
  });
  const fromGroup = (f: JavaField): IAtlasmapDocument => ({
    name: f.name,
    type: f.fieldType,
    id: `${idPrefix}-${f.path}`,
    fields: f.javaFields!.javaField.map(f =>
      f.javaFields ? fromGroup(f as JavaField) : fromElement(f)
    ),
    isCollection: false,
    isVisible: () => true,
  });

  return java.ClassInspectionResponse.javaClass.javaFields!.javaField.map(f =>
    f.javaFields ? fromGroup(f as JavaField) : fromElement(f)
  );
}
