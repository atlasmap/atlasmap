import { Field, MappingModel } from "@atlasmap/core";
import { ITransformationArgument } from "../UI";
import { AlertProps } from "@patternfly/react-core";

export type ElementId = string;
export type GroupId = string;

export type AtlasmapDocumentType = "source" | "target";

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField {
  id: ElementId;
  name: string;
  type: string;
  path: string;
  previewValue: string;
  mappings: IAtlasmapMapping[];
  hasTransformations: boolean;
  isCollection: boolean;
  isConnected: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapGroup {
  id: GroupId;
  fields: (IAtlasmapField | IAtlasmapGroup)[];
  name: string;
  type: string;
  isCollection: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  transformations: Array<{
    name: string;
    arguments: Array<ITransformationArgument>;
  }>;
}

export interface IAtlasmapMapping {
  id: string;
  name: string;
  sourceFields: Array<IAtlasmapMappedField>;
  targetFields: Array<IAtlasmapMappedField>;
  mapping: MappingModel;
  notifications: INotification[];
}

export interface INotification {
  id: string;
  variant: AlertProps["variant"];
  message: string;
  isRead?: boolean;
}
