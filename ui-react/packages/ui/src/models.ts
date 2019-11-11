import { ReactElement } from 'react';

export type CanvasLinkCoord = {
  x: number;
  y: number;
};

export type FieldId = string;

export type GroupId = string;

export interface FieldElement {
  id: FieldId;
  element: ReactElement;
}

export interface FieldsGroup {
  id: GroupId;
  title: ReactElement | string;
  fields: (FieldElement | FieldsGroup)[];
}

export interface Mapping {
  id: string;
  sourceFields: FieldId[];
  targetFields: FieldId[];
}

export type SourceTargetLine = {
  start: CanvasLinkCoord;
  end: CanvasLinkCoord;
  color: string;
};