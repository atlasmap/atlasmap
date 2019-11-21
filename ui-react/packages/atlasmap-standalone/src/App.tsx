import { useAtlasmap, DocumentDefinition, Field, MappingDefinition, MappedField } from '@atlasmap/provider';
import { Mapper, IFieldsGroup, IFieldsNode, IMappings, IMappingField } from '@atlasmap/ui';
import React from 'react';
import './App.css';

function fromFieldToIFieldsGroup(field: Field): IFieldsGroup {
  return {
    id: field.uuid,
    title: field.name,
    fields: field.children.map(fromFieldToIFields)
  }
}

function fromFieldToIFieldsNode(field: Field): IFieldsNode {
  return {
    id: field.uuid,
    element: <>{field.name}</>
  }
}

function fromFieldToIFields(field: Field): IFieldsGroup | IFieldsNode {
  return field.children.length > 0
    ? fromFieldToIFieldsGroup(field)
    : fromFieldToIFieldsNode(field);
}

function fromDocumentDefinitionToFieldGroup(def: DocumentDefinition): IFieldsGroup {
  return {
    id: def.id,
    fields: def.fields.map(fromFieldToIFields),
    title: def.name
  };
}

function fromMappedFieldToIMappingField(field: MappedField): IMappingField {
  return {
    id: field.field!.uuid,
    name: field.field!.name,
    tip: field.field!.path
  }
}

function fromMappingDefinitionToIMappings(def: MappingDefinition): IMappings[] {
  return def.mappings.map(m => {
    return {
      id: m.uuid,
      sourceFields: m.getMappedFields(true).map(fromMappedFieldToIMappingField),
      targetFields: m.getMappedFields(false).map(fromMappedFieldToIMappingField),
    }
  })
}

const App: React.FC = () => {
  const { sourceDocs, targetDocs, mappingDefinition } = useAtlasmap({
    baseJavaInspectionServiceUrl: '/v2/atlas/java/',
    baseXMLInspectionServiceUrl: '/v2/atlas/xml/',
    baseJSONInspectionServiceUrl: '/v2/atlas/json/',
    baseMappingServiceUrl: '/v2/atlas/',
  });
  console.log('Source docs', sourceDocs);
  console.log('Target docs', targetDocs);
  console.log('Mapping definition', mappingDefinition);
  return (
    <Mapper
      sources={sourceDocs.map(fromDocumentDefinitionToFieldGroup)}
      targets={targetDocs.map(fromDocumentDefinitionToFieldGroup)}
      mappings={fromMappingDefinitionToIMappings(mappingDefinition)}
      addToMapping={() => void(0)}
    />
  );
};

export default App;
