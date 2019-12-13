import { IFieldsGroup, IFieldsNode } from '../CanvasView';

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField extends IFieldsNode {
  name: string;
  type: string;
  previewValue: string;
}

export interface IAtlasmapGroup extends IFieldsGroup {
  name: string;
  type: string;
}

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
}
