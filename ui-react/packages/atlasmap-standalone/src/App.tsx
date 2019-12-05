import { useAtlasmap } from "@atlasmap/provider";
import { Atlasmap, GroupId } from "@atlasmap/ui";
import React, { useCallback, useRef, useState } from 'react';
import "./App.css";
import { useConfirmationDialog } from './useConfirmationDialog';

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
    deleteAtlasFile
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
      closeDialog();
      deleteAtlasFile(documentToDelete.current, documentIsSource.current);
      console.log(`TODO: delete document id ${documentToDelete.current}`);
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

  return (
    <>
      <Atlasmap
        sources={sources}
        targets={targets}
        mappings={mappings}
        addToMapping={() => void 0}
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
        onExportAtlasFile={exportAtlasFile}
      />
      {resetDialog}
      {deleteDocumentDialog}
    </>
  );
};

export default App;
