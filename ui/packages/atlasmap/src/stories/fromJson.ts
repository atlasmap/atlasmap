import { IAtlasmapGroup, IAtlasmapField, IAtlasmapMapping } from "../Views";

interface JsonField {
  jsonType: string;
  path: string;
  status: string;
  fieldType: string;
  name: string;
  jsonFields?: JsonFields;
  collectionType?: string;
  value?: any;
}

interface JsonFields {
  jsonField: JsonField[];
}

interface Field {
  jsonType: string;
  path: string;
  status: string;
  fieldType: string;
  name: string;
  jsonFields: JsonFields;
  collectionType?: string;
}

interface Fields {
  field: Field[];
}

interface JsonDocument {
  jsonType: string;
  fields: Fields;
}

interface JsonInspectionResponse {
  jsonType: string;
  jsonDocument: JsonDocument;
  executionTime: number;
}

export interface JsonObject {
  JsonInspectionResponse: JsonInspectionResponse;
}

export function jsonToFieldGroup(
  json: JsonObject,
  idPrefix: string,
  allMappings: IAtlasmapMapping[],
) {
  const fromElement = (jf: JsonField): IAtlasmapField => {
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
  const fromGroup = (f: Field): IAtlasmapGroup => ({
    name: f.name,
    type: f.fieldType,
    id: `${idPrefix}-${f.path}`,
    fields: f.jsonFields.jsonField.map((f) =>
      f.jsonFields ? fromGroup(f as Field) : fromElement(f),
    ),
    isCollection: false,
    amField: {} as IAtlasmapField["amField"],
  });

  return json.JsonInspectionResponse.jsonDocument.fields.field.map(fromGroup);
}
// export function jsonToFieldGroup(json, idPrefix, allMappings) {
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
//     fields: f.jsonFields.jsonField
//       .map((f) => (f.jsonFields ? fromGroup(f) : fromElement(f)))
//       .sort((a, b) => a.name.localeCompare(b.name)),
//     isCollection: f.collectionType === "LIST",
//   });

//   return json.JsonInspectionResponse.jsonDocument.fields.field
//     .map(fromGroup)
//     .sort((a, b) => a.name.localeCompare(b.name));
// }
