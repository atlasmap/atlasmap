import { Field, MappingModel } from "@atlasmap/core";
import { ITransformationArgument, ITransformationSelectOption } from "../UI";
import { AlertProps } from "@patternfly/react-core";

export type ElementId = string;
export type GroupId = string;

export type AtlasmapDocumentType = "source" | "target";

export type AtlasmapFields = Array<IAtlasmapGroup | IAtlasmapField>;

export interface IAtlasmapField {
  id: ElementId;
  name: string;
  type: string;
  scope: string;
  path: string;
  previewValue: string;
  mappings: IAtlasmapMapping[];
  hasTransformations: boolean;
  isAttribute: boolean;
  isCollection: boolean;
  isConnected: boolean;
  isInCollection: boolean;
  isDisabled: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapGroup {
  id: GroupId;
  fields: (IAtlasmapField | IAtlasmapGroup)[];
  name: string;
  type: string;
  isCollection: boolean;
  isInCollection: boolean;

  // TODO: find a way to remove this maybe?
  amField: Field;
}

export interface IAtlasmapNamespace {
  alias: string;
  uri: string;
  locationUri: string;
  isTarget: boolean;
}

export type AtlasmapNamespaces = Array<IAtlasmapNamespace>;

export interface IAtlasmapDocument {
  id: string;
  name: string;
  type: string;
  fields: AtlasmapFields;
  namespaces?: AtlasmapNamespaces;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  transformations: Array<{
    name: string;
    options: Array<ITransformationSelectOption>;
    arguments: Array<ITransformationArgument>;
  }>;
}

export interface IAtlasmapMapping {
  id: string;
  name: string;
  sourceFields: Array<IAtlasmapMappedField>;
  targetFields: Array<IAtlasmapMappedField>;
  mapping: MappingModel;
}

export interface INotification {
  id: string;
  variant: AlertProps["variant"];
  title: string;
  description: string;
  isRead?: boolean;
  mappingId?: string;
}
