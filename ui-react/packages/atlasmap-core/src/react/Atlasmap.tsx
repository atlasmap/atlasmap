import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  AtlasmapCanvasView,
  AtlasmapCanvasViewMappings,
  AtlasmapCanvasViewSource,
  AtlasmapCanvasViewTarget,
  AtlasmapUIProvider,
  GroupId,
  IAtlasmapField,
} from '@atlasmap/ui';
import { AtlasmapMappingDetails } from './AtlasmapMappingDetails';
import {
  IAtlasmapFieldWithField,
  IAtlasmapMapping,
} from '../utils/to-ui-models-util';
import {
  useAtlasmap,
  useAtlasmapSources,
  useAtlasmapTargets,
} from './AtlasmapProvider';

export interface IAtlasmapProps {
  onExportAtlasFile: (exportAtlasFile: (fileName: string) => void) => void;
  onResetAtlasmap: (resetAtlasmap: () => void) => void;
  onImportDocument: (importDocument: () => void) => void;
  onDeleteDocument: (deleteDocument: () => void) => void;
  onRemoveMappedField: (removeMappedfield: () => void) => void;
  onNewTransformation: (newTransformation: () => void) => void;
  onRemoveTransformation: (removeTransformation: () => void) => void;
  onCreateConstant: (
    createConstant: (constValue: string, constType: string) => void
  ) => void;
  onDeleteConstant: (deleteConstant: () => void) => void;
  onEditConstant: (
    constVal: string,
    editConstant: (
      origVal: string,
      constValue: string,
      constType: string
    ) => void
  ) => void;
  onCreateProperty: (
    createProperty: (
      propName: string,
      propValue: string,
      propType: string
    ) => void
  ) => void;
  onDeleteProperty: (deleteProperty: () => void) => void;
  onEditProperty: (
    selectedName: string,
    editProperty: (
      propName: string,
      propValue: string,
      propType: string
    ) => void
  ) => void;
}

export const Atlasmap: FunctionComponent<IAtlasmapProps> = ({
  onExportAtlasFile,
  onResetAtlasmap,
  onImportDocument,
  onDeleteDocument,
  onRemoveMappedField,
  onNewTransformation,
  onRemoveTransformation,
  onCreateConstant,
  onDeleteConstant,
  onEditConstant,
  onCreateProperty,
  onDeleteProperty,
  onEditProperty,
}) => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const {
    mappings,
    pending,
    error,
    importAtlasFile,
    resetAtlasmap,
    exportAtlasFile,
    deleteAtlasFile,
    changeActiveMapping,
    documentExists,
    toggleMappingPreview,
    toggleShowMappedFields,
    toggleShowUnmappedFields,
    onFieldPreviewChange,
    addToCurrentMapping,
    createMapping,
    createConstant,
    deleteConstant,
    editConstant,
    createProperty,
    deleteProperty,
    editProperty,
  } = useAtlasmap();

  const sources = useAtlasmapSources(sourceFilter);
  const targets = useAtlasmapTargets(targetFilter);

  const handleExportAtlasFile = () => {
    onExportAtlasFile(exportAtlasFile);
  };

  const handleCreateConstant = () => {
    onCreateConstant(createConstant);
  };

  const handleCreateProperty = () => {
    onCreateProperty(createProperty);
  };

  const handleImportAtlasFile = (file: File) => {
    importAtlasFile(file, false);
  };

  const handleResetAtlasmap = () => {
    onResetAtlasmap(resetAtlasmap);
  };

  const handleImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        onImportDocument(() => importAtlasFile(selectedFile, isSource));
      } else {
        importAtlasFile(selectedFile, isSource);
      }
    },
    [importAtlasFile, documentExists, onImportDocument]
  );

  const handleImportSourceDocument = useCallback(
    (selectedFile: File) => handleImportDocument(selectedFile, true),
    [handleImportDocument]
  );

  const handleImportTargetDocument = useCallback(
    (selectedFile: File) => handleImportDocument(selectedFile, false),
    [handleImportDocument]
  );

  const handleDeleteConstant = useCallback(
    (constValue: string) => {
      onDeleteConstant(() => deleteConstant(constValue));
    },
    [onDeleteConstant, deleteConstant]
  );

  const handleEditConstant = useCallback(
    (constVal: string) => {
      onEditConstant(constVal, editConstant);
    },
    [onEditConstant, editConstant]
  );

  const handleDeleteProperty = useCallback(
    (propName: string) => {
      onDeleteProperty(() => deleteProperty(propName));
    },
    [onDeleteProperty, deleteProperty]
  );

  const handleEditProperty = useCallback(
    (field: string) => {
      onEditProperty(field, editProperty);
    },
    [onEditProperty, editProperty]
  );

  const handleDeleteDocument = useCallback(
    (id: GroupId, isSource: boolean) => {
      onDeleteDocument(() => deleteAtlasFile(id, isSource));
    },
    [onDeleteDocument, deleteAtlasFile]
  );

  const handleDeleteSourceDocument = useCallback(
    (id: GroupId) => handleDeleteDocument(id, true),
    [handleDeleteDocument]
  );

  const handleDeleteTargetDocument = useCallback(
    (id: GroupId) => handleDeleteDocument(id, false),
    [handleDeleteDocument]
  );

  const handleFieldPreviewChange = useCallback(
    (field: IAtlasmapField, value: string) => {
      onFieldPreviewChange(field as IAtlasmapFieldWithField, value);
    },
    [onFieldPreviewChange]
  );

  const handleAddToMapping = useCallback(
    (node: IAtlasmapField) => {
      const field = (node as IAtlasmapFieldWithField).amField;
      addToCurrentMapping(field);
    },
    [addToCurrentMapping]
  );

  const handleCreateMapping = useCallback(
    (source: IAtlasmapField, target: IAtlasmapField) => {
      const sourceField = (source as IAtlasmapFieldWithField).amField;
      const targetField = (target as IAtlasmapFieldWithField).amField;
      createMapping(sourceField, targetField);
    },
    [createMapping]
  );

  return (
    <AtlasmapUIProvider
      error={error}
      pending={pending}
      sources={sources}
      targets={targets}
      mappings={mappings}
      onActiveMappingChange={changeActiveMapping}
      renderMappingDetails={({ mapping, closeDetails }) =>
        mapping && (
          <AtlasmapMappingDetails
            mapping={(mapping as IAtlasmapMapping).mapping}
            closeDetails={closeDetails}
            onRemoveMappedField={onRemoveMappedField}
            onNewTransformation={onNewTransformation}
            onRemoveTransformation={onRemoveTransformation}
          />
        )
      }
    >
      <AtlasmapCanvasView
        onShowMappingPreview={toggleMappingPreview}
        onShowMappedFields={toggleShowMappedFields}
        onShowUnmappedFields={toggleShowUnmappedFields}
        onExportAtlasFile={handleExportAtlasFile}
        onImportAtlasFile={(file: File) => handleImportAtlasFile(file)}
        onResetAtlasmap={handleResetAtlasmap}
      >
        {({ showTypes, showMappingPreview }) => (
          <>
            <AtlasmapCanvasViewSource
              onAddToMapping={handleAddToMapping}
              onCreateMapping={handleCreateMapping}
              onDeleteDocument={handleDeleteSourceDocument}
              onFieldPreviewChange={handleFieldPreviewChange}
              onImportDocument={handleImportSourceDocument}
              onCreateConstant={handleCreateConstant}
              onDeleteConstant={handleDeleteConstant}
              onEditConstant={handleEditConstant}
              onCreateProperty={handleCreateProperty}
              onDeleteProperty={handleDeleteProperty}
              onEditProperty={handleEditProperty}
              onSearch={setSourceFilter}
              showMappingPreview={showMappingPreview}
              showTypes={showTypes}
              sources={sources}
            />

            <AtlasmapCanvasViewMappings />

            <AtlasmapCanvasViewTarget
              onAddToMapping={handleAddToMapping}
              onCreateMapping={handleCreateMapping}
              onDeleteDocument={handleDeleteTargetDocument}
              onImportDocument={handleImportTargetDocument}
              onSearch={setTargetFilter}
              showMappingPreview={showMappingPreview}
              showTypes={showTypes}
              targets={targets}
            />
          </>
        )}
      </AtlasmapCanvasView>
    </AtlasmapUIProvider>
  );
};
