import { IAtlasmapDocument, IAtlasmapGroup, IAtlasmapField, IMapping } from '@atlasmap/ui';
import { MappedField, MappingModel } from "../models/mapping.model";
import { Field } from "../models/field.model";
import { MappingDefinition } from "../models/mapping-definition.model";
import { DocumentDefinition } from "../models/document-definition.model";

export interface IAtlasmapGroupWithField extends IAtlasmapGroup {
  amField: Field;
}

export interface IAtlasmapFieldWithField extends IAtlasmapField {
  amField: Field;
}

export interface IAtlasmapMappedField extends IAtlasmapField {
  mappedField: MappedField;
}

export interface IAtlasmapMapping extends IMapping {
  mapping: MappingModel;
}

function fromFieldToIFieldsGroup(field: Field): IAtlasmapGroupWithField {
  return {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    name: field.name,
    type: field.type,
    fields: field.children.map(fromFieldToIFields),
    amField: field
  }
}

function fromFieldToIFieldsNode(field: Field): IAtlasmapFieldWithField {
  return {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    name: field.getFieldLabel(false, false),
    type: field.type,
    amField: field,
    previewValue: field.value
  }
}

function fromFieldToIFields(field: Field) {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

export function fromDocumentDefinitionToFieldGroup(def: DocumentDefinition): IAtlasmapDocument | null {
  return def.visibleInCurrentDocumentSearch ? {
    id: def.id,
    fields: def.fields.map(fromFieldToIFields),
    name: def.name,
    type: def.type,
  } : null;
}

function fromMappedFieldToIMappingField(field: MappedField): IAtlasmapMappedField {
  return {
    id: `${field.field!.docDef.uri}:${field.field!.docDef.isSource ? 'source' : 'target'}:${field.field!.uuid}`,
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
