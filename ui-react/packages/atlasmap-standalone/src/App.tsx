import { useAtlasmap, IAtlasmapFieldWithField } from '@atlasmap/provider';
import { Atlasmap, GroupId, IAtlasmapField } from "@atlasmap/ui";
import React, { useCallback, useRef, useState } from 'react';
import "./App.css";
import { useConfirmationDialog } from './useConfirmationDialog';
import { useSingleInputDialog } from './useSingleInputDialog';

const App: React.FC = () => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const {
    sources,
    targets,
    mappings,
    pending,
    error,
    importAtlasFile,
    resetAtlasmap,
    exportAtlasFile,
    deleteAtlasFile,
    changeActiveMapping,
    enableMappingPreview,
    onFieldPreviewChange
  } = useAtlasmap({
    sourceFilter,
    targetFilter
  });

  const handleImportAtlasFile = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, false),
    [importAtlasFile]
  );
  const handleImportSourceDocument = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, true),
    [importAtlasFile]
  );
  const handleImportTargetDocument = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, false),
    [importAtlasFile]
  );
  const defaultCatalogName = 'atlasmap-mapping.adm';
  const [exportDialog, openExportDialog] = useSingleInputDialog({
    title: 'Export Mappings and Documents.',
    content: 'Please enter a name for your exported catalog file',
    placeholder: defaultCatalogName,
    onConfirm: (closeDialog, value) => {
      closeDialog();
      if (value.length === 0) {
        value = defaultCatalogName;
      }
      exportAtlasFile(value);
    },
    onCancel: (closeDialog) => {
      closeDialog();
    },
  });

  const [resetDialog, openResetDialog] = useConfirmationDialog({
    title: 'Reset All Mappings and Imports?',
    content: 'Are you sure you want to reset all mappings and clear all imported documents?',
    onConfirm: (closeDialog) => {
      closeDialog();
      resetAtlasmap();
    },
    onCancel: (closeDialog) => {
      closeDialog();
    }
  });

  const documentToDelete = useRef<GroupId | undefined>();
  let documentIsSource = useRef<boolean>();

  const [deleteDocumentDialog, openDeleteDocumentDialog] = useConfirmationDialog({
    title: 'Remove selected document?',
    content: 'Are you sure you want to remove the selected document and any associated mappings?',
    onConfirm: (closeDialog) => {
      if (documentToDelete.current === undefined || documentIsSource.current === undefined) {
        throw new Error(
          `Fatal internal error: Could not remove the specified file.`
        );
      }
      closeDialog();
      deleteAtlasFile(documentToDelete.current!, documentIsSource.current!);
    },
    onCancel: (closeDialog) => {
      closeDialog();
    }
  });

  const handleDeleteSourceDocumentDialog = useCallback((id: GroupId) => {
    documentToDelete.current = id;
    documentIsSource.current = true;
    openDeleteDocumentDialog();
  }, [openDeleteDocumentDialog]);

  const handleDeleteTargetDocumentDialog = useCallback((id: GroupId) => {
    documentToDelete.current = id;
    documentIsSource.current = false;
    openDeleteDocumentDialog();
  }, [openDeleteDocumentDialog]);

  const handleFieldPreviewChange = useCallback((field: IAtlasmapField, value: string) => {
    onFieldPreviewChange(field as IAtlasmapFieldWithField, value);
  }, [onFieldPreviewChange]);

  return (
    <>
      <Atlasmap
        sources={sources}
        targets={targets}
        mappings={mappings}
        pending={pending}
        error={error}
        onImportAtlasFile={handleImportAtlasFile}
        onImportSourceDocument={handleImportSourceDocument}
        onImportTargetDocument={handleImportTargetDocument}
        onDeleteSourceDocument={handleDeleteSourceDocumentDialog}
        onDeleteTargetDocument={handleDeleteTargetDocumentDialog}
        onResetAtlasmap={openResetDialog}
        onSourceSearch={setSourceFilter}
        onTargetSearch={setTargetFilter}
        onExportAtlasFile={openExportDialog}
        onActiveMappingChange={changeActiveMapping}
        onShowMappingPreview={enableMappingPreview}
        onFieldPreviewChange={handleFieldPreviewChange}
        onAddToMapping={() => void 0}
        onCreateMapping={() => void 0}
      />
      {exportDialog}
      {deleteDocumentDialog}
      {resetDialog}
    </>
  );
};

export default App;
