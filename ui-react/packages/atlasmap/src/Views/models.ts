import { Field, MappedField, MappingModel } from "@atlasmap/core";

export type ElementId = string;
export type GroupId = string;

export type AtlasmapDocumentType = "source" | "target";

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField {
  id: ElementId;
  name: string;
  type: string;
  previewValue: string;
  mappings: IAtlasmapMapping[];
  hasTransformations: boolean;
  isCollection: boolean;
  isConnected: boolean;
}

export interface IAtlasmapGroup {
  id: GroupId;
  fields: (IAtlasmapField | IAtlasmapGroup)[];
  name: string;
  type: string;
  isCollection: boolean;
}

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
}

export interface IAtlasmapGroupWithField extends IAtlasmapGroup {
  id: string;
  name: string;
  type: string;
  isCollection: boolean;
  fields: IAtlasmapFieldWithField[];
  amField: Field;
}

export interface IAtlasmapFieldWithField extends IAtlasmapField {
  id: string;
  name: string;
  type: string;
  isCollection: boolean;
  amField: Field;
  previewValue: string;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  mappedField: MappedField;
}

export interface IAtlasmapMapping {
  id: string;
  name: string;
  sourceFields: Array<IAtlasmapField>;
  targetFields: Array<IAtlasmapField>;
  mapping: MappingModel;
}
