export type Coords = {
  x: number;
  y: number;
};
export type Rect = { id: string, x: number, y: number, width: number, height: number };
export type Rects = Array<Rect>;
export type BrowserRect = ClientRect | DOMRect;
export type ElementId = string;
export type ElementType = 'source' | 'target';
export type GroupId = string;
export interface IFieldsNode {
  id: ElementId;
  name: string;
}
export interface IFieldsGroup {
  id: GroupId;
  title: string;
  fields: (IFieldsNode | IFieldsGroup)[];
}
export interface IMappingField {
  id: ElementId;
  name: string;
  tip: string;
}
export interface IMappings {
  id: string;
  sourceFields: Array<IMappingField>;
  targetFields: Array<IMappingField>;
}
