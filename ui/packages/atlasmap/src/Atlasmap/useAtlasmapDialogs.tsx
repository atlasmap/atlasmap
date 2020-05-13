import { useCallback, useRef } from "react";

import { collectionTypes, constantTypes, propertyTypes } from "@atlasmap/core";

import {
  useConfirmationDialog,
  useInputTextSelectDialog,
  useSingleInputDialog,
  useNamespaceDialog,
} from "../UI";
import { GroupId, IAtlasmapMapping, IAtlasmapField } from "../Views";
import {
  getConstantTypeIndex,
  getPropertyTypeIndex,
  getPropertyValue,
  enableCustomClass,
} from "./utils";
import { useAtlasmap } from "./AtlasmapProvider";

export interface IUseAtlasmapDialogsProps {
  modalContainer: HTMLElement;
}
export function useAtlasmapDialogs({
  modalContainer,
}: IUseAtlasmapDialogsProps) {
  const {
    selectedMapping,
    deselectMapping,
    removeMapping,
    deleteAtlasFile,
    exportAtlasFile,
    importAtlasFile,
    resetAtlasmap,
    createConstant,
    deleteConstant,
    editConstant,
    createProperty,
    deleteProperty,
    editProperty,
    documentExists,
    addToCurrentMapping,
    createMapping,
    removeFromCurrentMapping,
    createNamespace,
    editNamespace,
    deleteNamespace,
  } = useAtlasmap();

  const [importDialog, openImportDialog] = useConfirmationDialog({
    modalContainer,
    title: "Overwrite selected document?",
    content:
      "Are you sure you want to overwrite the selected document and remove any associated mappings?",
  });

  const defaultCatalogName = "atlasmap-mapping.adm";
  const [exportDialog, openExportDialog] = useSingleInputDialog({
    modalContainer,
    title: "Export Mappings and Documents.",
    content: "Please enter a name for your exported catalog file",
    placeholder: defaultCatalogName,
  });

  const title = useRef<string>("");
  const textVal1 = useRef<string>("");
  const textVal2 = useRef<string>("");
  const textVal3 = useRef<string>("");
  const booleanVal = useRef<boolean>(false);
  const selectIndex = useRef<number>(0);

  const [createNamespaceDialog, openCreateNamespaceDialog] = useNamespaceDialog(
    {
      docName: title.current,
      initAlias: textVal1,
      initUri: textVal2,
      initLocationUri: textVal3,
      initIsTarget: booleanVal,
      modalContainer,
    },
  );

  const [editNamespaceDialog, openEditNamespaceDialog] = useNamespaceDialog({
    docName: title.current,
    initAlias: textVal1,
    initUri: textVal2,
    initLocationUri: textVal3,
    initIsTarget: booleanVal,
    modalContainer,
  });

  const [
    createConstantDialog,
    openCreateConstantDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: "Create Constant",
    textLabel1: "Value",
    textValue1: textVal1,
    text1ReadOnly: false,
    textLabel2: "",
    textValue2: textVal2,
    selectLabel: "Value Type",
    selectValues: constantTypes,
    selectDefault: selectIndex,
  });

  const [
    deleteConstantDialog,
    openDeleteConstantDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: "Delete constant?",
    content:
      "Are you sure you want to delete the selected constant and remove any associated mapping references?",
  });

  const [editConstantDialog, openEditConstantDialog] = useInputTextSelectDialog(
    {
      modalContainer,
      title: "Edit Constant",
      textLabel1: "Value",
      textValue1: textVal1,
      text1ReadOnly: false,
      textLabel2: "",
      textValue2: textVal2,
      selectLabel: "Value Type",
      selectValues: constantTypes,
      selectDefault: selectIndex,
    },
  );

  const [
    createPropertyDialog,
    openCreatePropertyDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: "Create Property",
    textLabel1: "Name",
    textValue1: textVal1,
    text1ReadOnly: false,
    textLabel2: "Value",
    textValue2: textVal2,
    selectLabel: "Value Type",
    selectValues: propertyTypes,
    selectDefault: selectIndex,
  });

  const [
    deletePropertyDialog,
    openDeletePropertyDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: "Delete property?",
    content:
      "Are you sure you want to delete the selected property and remove any associated mapping references?",
  });

  const [editPropertyDialog, openEditPropertyDialog] = useInputTextSelectDialog(
    {
      modalContainer,
      title: "Edit Property",
      textLabel1: "Name",
      textValue1: textVal1,
      text1ReadOnly: true,
      textLabel2: "Value",
      textValue2: textVal2,
      selectLabel: "Value Type",
      selectValues: propertyTypes,
      selectDefault: selectIndex,
    },
  );

  const [resetDialog, openResetDialog] = useConfirmationDialog({
    modalContainer,
    title: "Reset All Mappings and Imports?",
    content:
      "Are you sure you want to reset all mappings and clear all imported documents?",
  });

  const [deleteDocumentDialog] = useConfirmationDialog({
    modalContainer,
    title: "Remove selected document?",
    content:
      "Are you sure you want to remove the selected document and any associated mappings?",
  });

  const [
    removeMappedFieldDialog,
    openRemoveMappedFieldDialog,
  ] = useConfirmationDialog({
    modalContainer,
    title: "Remove field?",
    content: "Are you sure you want to remove this field?",
  });

  const [deleteMappingDialog, openDeleteMappingDialog] = useConfirmationDialog({
    modalContainer,
    title: "Remove Mapping?",
    content: "Are you sure you want to remove the current mapping?",
  });

  const [
    createEnableCustomClassDialog,
    openCreateEnableCustomClassDialog,
  ] = useInputTextSelectDialog({
    modalContainer,
    title: "Enable Custom Class",
    textLabel1: "Custom Class Package Name",
    textValue1: textVal1,
    text1ReadOnly: false,
    textLabel2: "",
    textValue2: textVal2,
    selectLabel: "Collection Type",
    selectValues: collectionTypes,
    selectDefault: selectIndex,
  });

  const onExportAtlasFile = useCallback(() => {
    openExportDialog((value: string) => {
      if (value.length === 0) {
        value = defaultCatalogName;
      }
      exportAtlasFile(value);
    });
  }, [exportAtlasFile, openExportDialog]);

  const onCreateNamespace = (docName: string) => {
    title.current = docName;
    textVal1.current = "";
    textVal2.current = "";
    textVal3.current = "";
    booleanVal.current = false;
    openCreateNamespaceDialog(
      (
        docName: string,
        _initAlias: string,
        alias: string,
        uri: string,
        locationUri: string,
        isTarget: boolean,
      ) => {
        createNamespace(docName, alias, uri, locationUri, isTarget);
      },
    );
  };

  const onEditNamespace = (
    docName: string,
    alias: string,
    uri: string,
    locationUri: string,
    isTarget: boolean,
  ) => {
    title.current = docName;
    textVal1.current = alias;
    textVal2.current = uri;
    textVal3.current = locationUri;
    booleanVal.current = isTarget;
    openEditNamespaceDialog(
      (
        docName: string,
        initAlias: string,
        alias: string,
        uri: string,
        locationUri: string,
        isTarget: boolean,
      ) => {
        editNamespace(docName, initAlias, alias, uri, locationUri, isTarget);
      },
    );
  };

  const onCreateConstant = useCallback(() => {
    textVal1.current = "";
    selectIndex.current = 12;
    openCreateConstantDialog(
      (value: string, _value2: string, valueType: string) => {
        createConstant(value, valueType);
      },
    );
  }, [createConstant, openCreateConstantDialog]);

  const onDeleteConstant = useCallback(
    (constValue: string) =>
      openDeleteConstantDialog(() => deleteConstant(constValue)),
    [deleteConstant, openDeleteConstantDialog],
  );

  const onEditConstant = useCallback(
    (selectedValue: string) => {
      const originalValue = selectedValue.split(" ")[0];
      textVal1.current = originalValue;
      textVal2.current = "";
      selectIndex.current = getConstantTypeIndex(originalValue);
      openEditConstantDialog(
        (value: string, _value2: string, valueType: string) => {
          editConstant(originalValue, value, valueType);
        },
      );
    },
    [editConstant, openEditConstantDialog],
  );

  const onCreateProperty = useCallback(() => {
    textVal1.current = "";
    textVal2.current = "";
    selectIndex.current = 13;
    openCreatePropertyDialog(
      (name: string, value: string, valueType: string) => {
        createProperty(name, value, valueType);
      },
    );
  }, [createProperty, openCreatePropertyDialog]);

  const onDeleteProperty = useCallback(
    (propName: string) =>
      openDeletePropertyDialog(() => deleteProperty(propName)),
    [deleteProperty, openDeletePropertyDialog],
  );

  const onEditProperty = useCallback(
    (selectedName: string) => {
      textVal1.current = selectedName.split(" ")[0];
      textVal2.current = getPropertyValue(textVal1.current);
      selectIndex.current = getPropertyTypeIndex(textVal1.current);

      openEditPropertyDialog(
        (name: string, value: string, valueType: string) => {
          editProperty(name, value, valueType);
        },
      );
    },
    [editProperty, openEditPropertyDialog],
  );

  const getCustomClass = useCallback(
    (
      getCustomClassSelections: (
        selectClass: string,
        selectCollection: string,
      ) => void,
    ) => {
      textVal1.current = "";
      textVal2.current = "";
      selectIndex.current = 3;
      openCreateEnableCustomClassDialog(getCustomClassSelections);
    },
    [openCreateEnableCustomClassDialog],
  );

  const onEnableCustomClass = useCallback(
    (isSource: boolean): void => {
      getCustomClass((selectedClass: string, selectedCollection: string) =>
        enableCustomClass(selectedClass, selectedCollection, isSource),
      );
    },
    [getCustomClass],
  );

  const onResetAtlasmap = useCallback(() => openResetDialog(resetAtlasmap), [
    openResetDialog,
    resetAtlasmap,
  ]);
  const onImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        openImportDialog(() => importAtlasFile(selectedFile, isSource));
      } else {
        importAtlasFile(selectedFile, isSource);
      }
    },
    [documentExists, importAtlasFile, openImportDialog],
  );
  const onDeleteDocument = useCallback(
    (id: GroupId, isSource: boolean) => deleteAtlasFile(id, isSource),
    [deleteAtlasFile],
  );
  const onRemoveMappedField = useCallback(
    (removeMappedField: () => void) =>
      openRemoveMappedFieldDialog(removeMappedField),
    [openRemoveMappedFieldDialog],
  );

  const onNewTransformation = useCallback(() => void 0, []);
  const onRemoveTransformation = useCallback(() => void 0, []);

  const onDeleteMapping = useCallback(
    (mapping: IAtlasmapMapping) => {
      openDeleteMappingDialog(() => {
        removeMapping(mapping.mapping);
        deselectMapping();
      });
    },
    [deselectMapping, openDeleteMappingDialog, removeMapping],
  );

  const onDeleteSelectedMapping = useCallback(() => {
    if (selectedMapping) {
      onDeleteMapping(selectedMapping);
    }
  }, [onDeleteMapping, selectedMapping]);

  const onAddToMapping = useCallback(
    (node: IAtlasmapField) => {
      const field = (node as IAtlasmapField).amField;
      addToCurrentMapping(field);
    },
    [addToCurrentMapping],
  );

  const onRemoveFromMapping = useCallback(
    (node: IAtlasmapField) => {
      const field = (node as IAtlasmapField).amField;
      removeFromCurrentMapping(field);
    },
    [removeFromCurrentMapping],
  );

  const onCreateMapping = useCallback(
    (
      source: IAtlasmapField | undefined,
      target: IAtlasmapField | undefined,
    ) => {
      const sourceField = (source as IAtlasmapField | undefined)?.amField;
      const targetField = (target as IAtlasmapField | undefined)?.amField;
      createMapping(sourceField, targetField);
    },
    [createMapping],
  );

  return {
    handlers: {
      onExportAtlasFile,
      onCreateConstant,
      onDeleteConstant,
      onEditConstant,
      onCreateProperty,
      onDeleteProperty,
      onEditProperty,
      onResetAtlasmap,
      onImportDocument,
      onDeleteDocument,
      onRemoveMappedField,
      onNewTransformation,
      onRemoveTransformation,
      onDeleteMapping,
      onDeleteSelectedMapping,
      onAddToMapping,
      onRemoveFromMapping,
      onCreateMapping,
      onEnableCustomClass,
      onCreateNamespace,
      onEditNamespace,
      deleteNamespace,
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
      createEnableCustomClassDialog,
      createNamespaceDialog,
      editNamespaceDialog,
    ],
  };
}
