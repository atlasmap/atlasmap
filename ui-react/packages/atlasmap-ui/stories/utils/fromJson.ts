import { IAtlasmapDocument } from '../../src/AtlasmapUI';

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
  idPrefix: string
): IAtlasmapDocument[] {
  const fromElement = (jf: JsonField) => ({
    id: `${idPrefix}-${jf.path}`,
    name: jf.name,
    type: jf.fieldType,
    previewValue: '',
  });
  const fromGroup = (f: Field): IAtlasmapDocument => ({
    name: f.name,
    type: f.fieldType,
    id: `${idPrefix}-${f.path}`,
    fields: f.jsonFields.jsonField.map(f =>
      f.jsonFields ? fromGroup(f as Field) : fromElement(f)
    ),
  });

  return json.JsonInspectionResponse.jsonDocument.fields.field.map(fromGroup);
}
