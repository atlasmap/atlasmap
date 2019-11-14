import { ReactElement } from 'react';

export type Coords = {
  x: number;
  y: number;
};
export type Rect = ClientRect | DOMRect;
export type NodeId = string;
export type FieldType = 'source' | 'target';
export type GroupId = string;
export interface IFieldsNode {
  id: NodeId;
  element: ReactElement;
}
export interface IFieldsGroup {
  id: GroupId;
  title: ReactElement | string;
  fields: (IFieldsNode | IFieldsGroup)[];
}
export interface IMappings {
  id: string;
  sourceFields: NodeId[];
  targetFields: NodeId[];
}
