import { IAtlasmapDocument, IAtlasmapGroup, IAtlasmapField, IMapping, IFieldsNode } from '@atlasmap/ui';
import { MappedField, MappingModel } from "../models/mapping.model";
import { Field } from "../models/field.model";
import { MappingDefinition } from "../models/mapping-definition.model";
import { DocumentDefinition } from "../models/document-definition.model";
import { ConfigModel } from "../models/config.model";

export interface IAtlasmapGroupWithField extends IAtlasmapGroup {
  isCollection: boolean;
  amField: Field;
}

export interface IAtlasmapFieldWithField extends IAtlasmapField {
  isCollection: boolean;
  amField: Field;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  mappedField: MappedField;
}

export interface IAtlasmapMapping extends IMapping {
  mapping: MappingModel;
}

function fromFieldToIFieldsGroup(field: Field): IAtlasmapGroupWithField | null {
  const fields = field.children.map(fromFieldToIFields).filter(f => f) as IAtlasmapFieldWithField[];
  const isGroupVisible = (): boolean => {
    return field.visibleInCurrentDocumentSearch;
  };
  return fields.length > 0 ? {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    isVisible: isGroupVisible,
    name: field.name,
    type: field.type,
    isCollection: field.isCollection,
    fields: fields,
    amField: field
  } : null;
}

function fromFieldToIFieldsNode(field: Field): IAtlasmapFieldWithField | null {
  const cfg = ConfigModel.getConfig();
  const partOfMapping: boolean = field.partOfMapping;
  const shouldBeVisible = partOfMapping ? cfg.showMappedFields : cfg.showUnmappedFields;
  const isFieldVisible = (): boolean => {
    return field.visibleInCurrentDocumentSearch;
  };
  return shouldBeVisible ? {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    isVisible: isFieldVisible,
    name: field.getFieldLabel(false, false),
    type: field.type,
    isCollection: field.isCollection,
    amField: field,
    previewValue: field.value
  } : null;
}

function fromFieldToIFields(field: Field) {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

export function fromDocumentDefinitionToFieldGroup(def: DocumentDefinition): IAtlasmapDocument | null {
  const fields = def.fields.map(fromFieldToIFields).filter(f => f) as IAtlasmapFieldWithField[];
  const isGroupVisible = (): boolean => {
    return def.visibleInCurrentDocumentSearch;
  };
  return def.visibleInCurrentDocumentSearch && fields.length > 0 ? {
    id: def.id,
    isVisible: isGroupVisible,
    isCollection: false,
    fields: fields,
    name: def.name,
    type: def.type,
  } : null;
}

function fromMappedFieldToIMappingField(field: MappedField): IAtlasmapMappedField {
  if (!field.field) {
    return {id: '', name: '', type: '', previewValue: '', mappedField: field, isVisible: () => false};
  }
  const isFieldVisible = (): boolean => {
    return field.field?.visibleInCurrentDocumentSearch ?? false;
  };
  return {
    id: `${field.field!.docDef.uri}:${field.field!.docDef.isSource ? 'source' : 'target'}:${field.field!.uuid}`,
    isVisible: isFieldVisible,
    name: field.field!.getFieldLabel(false, false),
    type: field.field!.type,
    previewValue: '',
    mappedField: field
  };
}

export function fromMappingDefinitionToIMappings(def: MappingDefinition): IAtlasmapMapping[] {
  return def.mappings.map(m => {
    return {
      id: m.uuid,
      name: m.transition.getPrettyName(),
      sourceFields: m.getUserMappedFields(true).map(fromMappedFieldToIMappingField),
      targetFields: m.getUserMappedFields(false).map(fromMappedFieldToIMappingField),
      mapping: m
    }
  })
}
