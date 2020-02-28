export type BrowserRect = ClientRect | DOMRect;
export type ElementId = string;
export type GroupId = string;

export interface IFieldsNode {
  id: ElementId;
  isVisible?: () => boolean;
}
export interface IFieldsGroup {
  id: GroupId;
  isVisible?: () => boolean;
  isCollection: boolean;
  fields: (IFieldsNode | IFieldsGroup)[];
}
export interface IMappingField {
  id: ElementId;
}
export interface IMapping {
  id: string;
  name: string;
  sourceFields: Array<IMappingField>;
  targetFields: Array<IMappingField>;
}
