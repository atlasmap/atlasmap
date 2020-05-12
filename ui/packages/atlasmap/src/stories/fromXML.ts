import { IAtlasmapGroup, IAtlasmapField, IAtlasmapMapping } from "../Views";

interface Restrictions {
  restriction: any[];
}
interface XmlField {
  jsonType: string;
  path: string;
  fieldType: string;
  name: string;
  value?: any;
  status?: any;
  xmlFields?: XmlFields;
  restrictions?: Restrictions;
  collectionType?: string;
}

interface XmlFields {
  xmlField: XmlField[];
}

interface Field {
  jsonType: string;
  path: string;
  fieldType: string;
  name: string;
  xmlFields: XmlFields;
  typeName?: any;
}

interface Fields {
  field: Field[];
}

interface XmlNamespace {
  alias: string;
  uri: string;
}

interface XmlNamespaces {
  xmlNamespace: XmlNamespace[];
}

interface XmlDocument {
  jsonType: string;
  fields: Fields;
  xmlNamespaces: XmlNamespaces;
}

interface XmlInspectionResponse {
  jsonType: string;
  xmlDocument: XmlDocument;
  executionTime: number;
}

export interface XMLObject {
  XmlInspectionResponse: XmlInspectionResponse;
}

export function xmlToFieldGroup(
  xml: XMLObject,
  idPrefix: string,
  allMappings: IAtlasmapMapping[],
) {
  const fromElement = (jf: XmlField): IAtlasmapField => {
    const id = `${idPrefix}-${jf.path}`;
    const mappings = allMappings.filter(
      (m) =>
        m.sourceFields.find((s) => s.id === id) ||
        m.targetFields.find((t) => t.id === id),
    );

    return {
      id,
      name: jf.name,
      type: jf.fieldType,
      previewValue: "",
      path: jf.path,
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
  const fromGroup = (f: Field): IAtlasmapGroup => ({
    name: f.name,
    type: f.fieldType,
    id: `${idPrefix}-${f.path}`,
    fields: f.xmlFields.xmlField.map((f) =>
      f.xmlFields ? fromGroup(f as Field) : fromElement(f),
    ),
    isCollection: false,
    amField: {} as IAtlasmapField["amField"],
  });

  return xml.XmlInspectionResponse.xmlDocument.fields.field.map((f) =>
    f.xmlFields ? fromGroup(f as Field) : fromElement(f),
  );
}
// export function xmlToFieldGroup(xml, idPrefix, allMappings) {
//   const fromElement = (jf) => {
//     const id = `${idPrefix}-${jf.path}`;
//     const mappings = allMappings.filter(
//       (m) =>
//         m.sourceFields.find((s) => s.id === id) ||
//         m.targetFields.find((t) => t.id === id)
//     );

//     return {
//       id,
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
//     fields: f.xmlFields.xmlField
//       .map((f) => (f.xmlFields ? fromGroup(f) : fromElement(f)))
//       .sort((a, b) => a.name.localeCompare(b.name)),
//     isCollection: f.collectionType === "LIST",
//   });

//   return xml.XmlInspectionResponse.xmlDocument.fields.field
//     .map((f) => (f.xmlFields ? fromGroup(f) : fromElement(f)))
//     .sort((a, b) => a.name.localeCompare(b.name));
// }
