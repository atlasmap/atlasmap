import { useCallback, useRef } from 'react';
import {
  useConfirmationDialog,
  useSingleInputDialog,
  useInputTextSelectDialog,
} from '@atlasmap/ui';
import { constantTypes, propertyTypes } from '../common/config.types';
import {
  getPropertyValue,
  getPropertyTypeIndex,
  getConstantTypeIndex,
} from '../components/field/field-util';

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

  const textVal1 = useRef<string>('');
  const textVal2 = useRef<string>('');
  const selectIndex = useRef<number>(0);

  const [
    createConstantDialog,
    openCreateConstantDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: 'Create Constant',
    textLabel1: 'Value',
    textValue1: textVal1,
    text1ReadOnly: false,
    textLabel2: '',
    textValue2: textVal2,
    selectLabel: 'Value Type',
    selectValues: constantTypes,
    selectDefault: selectIndex,
  });

  const [
    deleteConstantDialog,
    openDeleteConstantDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: 'Delete constant?',
    content:
      'Are you sure you want to delete the selected constant and remove any associated mapping references?',
  });

  const [editConstantDialog, openEditConstantDialog] = useInputTextSelectDialog(
    {
      modalContainer,
      title: 'Edit Constant',
      textLabel1: 'Value',
      textValue1: textVal1,
      text1ReadOnly: false,
      textLabel2: '',
      textValue2: textVal2,
      selectLabel: 'Value Type',
      selectValues: constantTypes,
      selectDefault: selectIndex,
    }
  );

  const [
    createPropertyDialog,
    openCreatePropertyDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: 'Create Property',
    textLabel1: 'Name',
    textValue1: textVal1,
    text1ReadOnly: false,
    textLabel2: 'Value',
    textValue2: textVal2,
    selectLabel: 'Value Type',
    selectValues: propertyTypes,
    selectDefault: selectIndex,
  });

  const [
    deletePropertyDialog,
    openDeletePropertyDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: 'Delete property?',
    content:
      'Are you sure you want to delete the selected property and remove any associated mapping references?',
  });

  const [editPropertyDialog, openEditPropertyDialog] = useInputTextSelectDialog(
    {
      modalContainer,
      title: 'Edit Property',
      textLabel1: 'Name',
      textValue1: textVal1,
      text1ReadOnly: true,
      textLabel2: 'Value',
      textValue2: textVal2,
      selectLabel: 'Value Type',
      selectValues: propertyTypes,
      selectDefault: selectIndex,
    }
  );

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

  const [deleteMappingDialog, openDeleteMappingDialog] = useConfirmationDialog({
    modalContainer,
    title: 'Remove Mapping?',
    content: 'Are you sure you want to remove the current mapping?',
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
      textVal1.current = '';
      selectIndex.current = 12;
      openCreateConstantDialog(
        (value: string, _value2: string, valueType: string) => {
          createConstant(value, valueType);
        }
      );
    },
    [openCreateConstantDialog]
  );

  const handleDeleteConstant = useCallback(
    (deleteConstant: () => void) => openDeleteConstantDialog(deleteConstant),
    [openDeleteConstantDialog]
  );

  const handleEditConstant = useCallback(
    (
      selectedValue: string,
      editConstant: (
        origValue: string,
        constValue: string,
        constType: string
      ) => void
    ) => {
      const originalValue = selectedValue.split(' ')[0];
      textVal1.current = originalValue;
      textVal2.current = '';
      selectIndex.current = getConstantTypeIndex(originalValue);
      openEditConstantDialog(
        (value: string, _value2: string, valueType: string) => {
          editConstant(originalValue, value, valueType);
        }
      );
    },
    [openEditConstantDialog]
  );

  const handleCreateProperty = useCallback(
    (
      createProperty: (
        propName: string,
        propValue: string,
        propType: string
      ) => void
    ) => {
      textVal1.current = '';
      textVal2.current = '';
      selectIndex.current = 13;
      openCreatePropertyDialog(
        (name: string, value: string, valueType: string) => {
          createProperty(name, value, valueType);
        }
      );
    },
    [openCreatePropertyDialog]
  );

  const handleDeleteProperty = useCallback(
    (deleteProperty: () => void) => openDeletePropertyDialog(deleteProperty),
    [openDeletePropertyDialog]
  );

  const handleEditProperty = useCallback(
    (
      selectedName: string,
      editProperty: (
        propName: string,
        propValue: string,
        propType: string
      ) => void
    ) => {
      textVal1.current = selectedName.split(' ')[0];
      textVal2.current = getPropertyValue(textVal1.current);
      selectIndex.current = getPropertyTypeIndex(textVal1.current);

      openEditPropertyDialog(
        (name: string, value: string, valueType: string) => {
          editProperty(name, value, valueType);
        }
      );
    },
    [openEditPropertyDialog]
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

  const handleDeleteMapping = useCallback(
    (removeMapping: () => void) => openDeleteMappingDialog(removeMapping),
    [openDeleteMappingDialog]
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
      onDeleteMapping: handleDeleteMapping,
      onCreateConstant: handleCreateConstant,
      onDeleteConstant: handleDeleteConstant,
      onEditConstant: handleEditConstant,
      onCreateProperty: handleCreateProperty,
      onDeleteProperty: handleDeleteProperty,
      onEditProperty: handleEditProperty,
    },
    dialogs: [
      exportDialog,
      importDialog,
      deleteDocumentDialog,
      createConstantDialog,
      deleteConstantDialog,
      editConstantDialog,
      createPropertyDialog,
      deletePropertyDialog,
      editPropertyDialog,
      resetDialog,
      removeMappedFieldDialog,
      deleteMappingDialog,
    ],
  };
}
