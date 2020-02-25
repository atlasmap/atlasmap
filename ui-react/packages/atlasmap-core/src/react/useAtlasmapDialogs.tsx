import { useCallback } from 'react';
import {
  useConfirmationDialog,
  useSingleInputDialog,
  useInputTextSelectDialog,
} from '@atlasmap/ui';
import { constantTypes, propertyTypes } from '../common/config.types';

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

  const [
    createConstantDialog,
    openCreateConstantDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: 'Create Constant',
    textLabel1: 'Value',
    textLabel2: '',
    selectLabel: 'Value Type',
    selectValues: constantTypes,
    selectDefault: 12,
  });

  const [
    createPropertyDialog,
    openCreatePropertyDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: 'Create Property',
    textLabel1: 'Name',
    textLabel2: 'Value',
    selectLabel: 'Value Type',
    selectValues: propertyTypes,
    selectDefault: 13,
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
      openExportDialog((value: string) => {
        if (value.length === 0) {
          value = defaultCatalogName;
        }
        exportAtlasFile(value);
      });
    },
    [openExportDialog]
  );

  const handleCreateConstant = useCallback(
    (createConstant: (constValue: string, constType: string) => void) => {
      openCreateConstantDialog(
        (value: string, _value2: string, valueType: string) => {
          createConstant(value, valueType);
        }
      );
    },
    [openCreateConstantDialog]
  );

  const handleCreateProperty = useCallback(
    (
      createProperty: (
        propName: string,
        propValue: string,
        propType: string
      ) => void
    ) => {
      openCreatePropertyDialog(
        (name: string, value: string, valueType: string) => {
          createProperty(name, value, valueType);
        }
      );
    },
    [openCreatePropertyDialog]
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
      onCreateConstant: handleCreateConstant,
      onCreateProperty: handleCreateProperty,
    },
    dialogs: [
      exportDialog,
      importDialog,
      deleteDocumentDialog,
      createConstantDialog,
      createPropertyDialog,
      resetDialog,
      removeMappedFieldDialog,
    ],
  };
}
