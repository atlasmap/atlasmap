export type BrowserRect = ClientRect | DOMRect;
export type ElementId = string;
export type GroupId = string;

export interface IFieldsNode {
  id: ElementId;
}
export interface IFieldsGroup {
  id: GroupId;
  fields: (IFieldsNode | IFieldsGroup)[];
}
export interface IMappingField {
  id: ElementId;
  name: string;
  tip: string;
}
export interface IMappings {
  id: string;
  name: string;
  sourceFields: Array<IMappingField>;
  targetFields: Array<IMappingField>;
}
