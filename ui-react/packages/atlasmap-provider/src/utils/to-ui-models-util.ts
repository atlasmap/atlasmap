import { IAtlasmapDocument, IAtlasmapGroup, IAtlasmapField, IMappingField, IMappings } from '@atlasmap/ui';
import { DocumentDefinition, Field, MappedField, MappingDefinition } from '..';

export interface IAtlasmapGroupWithField extends IAtlasmapGroup {
  amField: Field;
}

export interface IAtlasmapFieldWithField extends IAtlasmapField {
  amField: Field;
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

function fromMappedFieldToIMappingField(isSource: boolean, field: MappedField): IMappingField {
  return {
    id: `${field.field!.docDef.uri}:${isSource ? 'source' : 'target'}:${field.field!.uuid}`,
    name: field.field!.getFieldLabel(false, false),
    tip: field.field!.path
  }
}

export function fromMappingDefinitionToIMappings(def: MappingDefinition): IMappings[] {
  return def.mappings.map(m => {
    return {
      id: m.uuid,
      sourceFields: m.getUserMappedFields(true).map(fromMappedFieldToIMappingField.bind(null, true)),
      targetFields: m.getUserMappedFields(false).map(fromMappedFieldToIMappingField.bind(null, false)),
    }
  })
}
