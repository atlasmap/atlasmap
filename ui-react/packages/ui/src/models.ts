import { ReactElement } from 'react';

export type Coords = {
  x: number;
  y: number;
};
export type Rect = ClientRect | DOMRect;
export type MappingNodeID = string;
export type MappingNodeType = 'source' | 'target';
export type MappingGroupId = string;
export interface MappingNode {
  id: MappingNodeID;
  element: ReactElement;
}
export interface MappingGroup {
  id: MappingGroupId;
  title: ReactElement | string;
  fields: (MappingNode | MappingGroup)[];
}
export interface Mapping {
  id: string;
  sourceFields: MappingNodeID[];
  targetFields: MappingNodeID[];
}
