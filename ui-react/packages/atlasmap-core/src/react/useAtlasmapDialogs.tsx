import { useCallback } from 'react';
import { useConfirmationDialog, useSingleInputDialog } from '@atlasmap/ui';

export interface IUseAtlasmapDialogsProps {
  modalContainer: HTMLElement;
}
export function useAtlasmapDialogs({
  modalContainer,
}: IUseAtlasmapDialogsProps) {
  const [importDialog, openImportDialog] = useConfirmationDialog({
    modalContainer,
    title: 'Overwrite selected document?',
    content:
      'Are you sure you want to overwrite the selected document and remove any associated mappings?',
  });

  const defaultCatalogName = 'atlasmap-mapping.adm';
  const [exportDialog, openExportDialog] = useSingleInputDialog({
    modalContainer,
    title: 'Export Mappings and Documents.',
    content: 'Please enter a name for your exported catalog file',
    placeholder: defaultCatalogName,
  });

  const [resetDialog, openResetDialog] = useConfirmationDialog({
    modalContainer,
    title: 'Reset All Mappings and Imports?',
    content:
      'Are you sure you want to reset all mappings and clear all imported documents?',
  });

  const [
    deleteDocumentDialog,
    openDeleteDocumentDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: 'Remove selected document?',
    content:
      'Are you sure you want to remove the selected document and any associated mappings?',
  });

  const [
    removeMappedFieldDialog,
    openRemoveMappedFieldDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: 'Remove field?',
    content: 'Are you sure you want to remove this field?',
  });

  const handleExportAtlasFile = useCallback(
    (exportAtlasFile: (fileName: string) => void) => {
      openExportDialog(value => {
        if (value.length === 0) {
          value = defaultCatalogName;
        }
        exportAtlasFile(value);
      });
    },
    [openExportDialog]
  );

  const handleResetAtlasmap = useCallback(
    (resetAtlasmap: () => void) => openResetDialog(resetAtlasmap),
    [openResetDialog]
  );
  const handleImportDocument = useCallback(
    (importDocument: () => void) => openImportDialog(importDocument),
    [openImportDialog]
  );
  const handleDeleteDocument = useCallback(
    (deleteDocument: () => void) => openDeleteDocumentDialog(deleteDocument),
    [openDeleteDocumentDialog]
  );
  const handleRemoveMappedField = useCallback(
    (removeMappedField: () => void) =>
      openRemoveMappedFieldDialog(removeMappedField),
    [openRemoveMappedFieldDialog]
  );
  const handleNewTransformation = useCallback(() => void 0, []);
  const handleRemoveTransformation = useCallback(() => void 0, []);

  return {
    handlers: {
      onExportAtlasFile: handleExportAtlasFile,
      onResetAtlasmap: handleResetAtlasmap,
      onImportDocument: handleImportDocument,
      onDeleteDocument: handleDeleteDocument,
      onRemoveMappedField: handleRemoveMappedField,
      onNewTransformation: handleNewTransformation,
      onRemoveTransformation: handleRemoveTransformation,
    },
    dialogs: [
      exportDialog,
      importDialog,
      deleteDocumentDialog,
      resetDialog,
      removeMappedFieldDialog,
    ],
  };
}
