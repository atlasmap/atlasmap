import React from 'react';
import { IFieldsGroup, IFieldsNode, IMappingField, IMappings } from '@atlasmap/ui';
import { DocumentDefinition, Field, MappedField, MappingDefinition } from '..';

function fromFieldToIFieldsGroup(field: Field): IFieldsGroup {
  return {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    title: field.name,
    fields: field.children.map(fromFieldToIFields)
  }
}

function fromFieldToIFieldsNode(field: Field): IFieldsNode {
  return {
    id: `${field.docDef.uri}:${field.docDef.isSource ? 'source' : 'target'}:${field.uuid}`,
    name: field.getFieldLabel(false, false)
  }
}

function fromFieldToIFields(field: Field): IFieldsGroup | IFieldsNode {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

export function fromDocumentDefinitionToFieldGroup(def: DocumentDefinition): IFieldsGroup | null {
  return def.visibleInCurrentDocumentSearch ? {
    id: def.id,
    fields: def.fields.map(fromFieldToIFields),
    title: def.name
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
