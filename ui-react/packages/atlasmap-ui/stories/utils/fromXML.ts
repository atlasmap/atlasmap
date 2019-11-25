import { IFieldsGroup } from '../../src/models';

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

export function xmlToFieldGroup(xml: XMLObject, idPrefix: string) {
  const fromElement = (jf: XmlField) => ({
    id: `${idPrefix}-${jf.path}`,
    name: jf.name
  });

  const fromGroup = (f: Field): IFieldsGroup => ({
    title: f.name,
    id: `${idPrefix}-${f.path}`,
    fields: f.xmlFields.xmlField.map(
      f => f.xmlFields ? fromGroup(f as Field) : fromElement(f)
    )
  });

  return xml.XmlInspectionResponse.xmlDocument.fields.field.map(
    f => f.xmlFields ? fromGroup(f as Field) : fromElement(f)
  )
}