import React, { useCallback, useMemo } from "react";
import { createPortal } from "react-dom";

import { IConstant, INamespace, IProperty } from "../UI";
import { useAtlasmap } from "./AtlasmapProvider";
import {
  useConstantDialog,
  useDeleteConstantDialog,
  useDeleteDocumentDialog,
  useDeleteMappingDialog,
  useDeletePropertyDialog,
  useExportCatalogDialog,
  useImportCatalogDialog,
  useImportDocumentDialog,
  useNamespaceDialog,
  usePropertyDialog,
  useRemoveMappedFieldDialog,
  useResetAtlasmapDialog,
  useToggleExpressionModeDialog,
  useCustomClassDialog,
} from "./dialogs";
import { enableCustomClass } from "./utils";

export interface IUseAtlasmapDialogsProps {
  modalContainer: HTMLElement;
}
export function useAtlasmapDialogs({
  modalContainer,
}: IUseAtlasmapDialogsProps) {
  const {
    selectedMapping,
    createConstant,
    editConstant,
    createProperty,
    editProperty,
    createNamespace,
    editNamespace,
    deleteNamespace,
  } = useAtlasmap();

  //#region constant dialogs
  const [createConstantDialog, openCreateConstantDialog] = useConstantDialog(
    "Create Constant",
  );
  const onCreateConstant = useCallback(() => {
    openCreateConstantDialog(({ value, valueType }) => {
      createConstant(value, valueType);
    });
  }, [createConstant, openCreateConstantDialog]);

  const [editConstantDialog, openEditConstantDialog] = useConstantDialog(
    "Edit Constant",
  );
  const onEditConstant = useCallback(
    (constant: IConstant) => {
      openEditConstantDialog(({ value, valueType }) => {
        editConstant(constant.value, value, valueType);
      }, constant);
    },
    [editConstant, openEditConstantDialog],
  );
  const [deleteConstantDialog, onDeleteConstant] = useDeleteConstantDialog();
  //#endregion

  //#region property dialogs
  const [createPropertyDialog, openCreatePropertyDialog] = usePropertyDialog(
    "Create Property",
  );
  const onCreateProperty = useCallback(() => {
    openCreatePropertyDialog(({ name, value, valueType }) => {
      createProperty(name, value, valueType);
    });
  }, [createProperty, openCreatePropertyDialog]);

  const [editPropertyDialog, openEditPropertyDialog] = usePropertyDialog(
    "Edit Property",
  );
  const onEditProperty = useCallback(
    (property: IProperty) => {
      openEditPropertyDialog(({ name, value, valueType }) => {
        editProperty(property.name, value, valueType, name);
      }, property);
    },
    [editProperty, openEditPropertyDialog],
  );
  const [deletePropertyDialog, onDeleteProperty] = useDeletePropertyDialog();
  //#endregion

  //#region atlasmap catalog
  const [importCatalogDialog, onImportAtlasCatalog] = useImportCatalogDialog();
  const [exportCatalogDialog, onExportAtlasCatalog] = useExportCatalogDialog();
  const [resetDialog, onResetAtlasmap] = useResetAtlasmapDialog();
  const [
    toggleExpressionModeDialog,
    onToggleExpressionMode,
  ] = useToggleExpressionModeDialog();
  //#endregion

  //#region editor dialogs
  const [importDocumentDialog, onImportDocument] = useImportDocumentDialog();
  const [deleteDocumentDialog, onDeleteDocument] = useDeleteDocumentDialog();
  const [
    removeMappedFieldDialog,
    onRemoveMappedField,
  ] = useRemoveMappedFieldDialog();
  const [deleteMappingDialog, onDeleteMapping] = useDeleteMappingDialog();
  const onDeleteSelectedMapping = useCallback(() => {
    if (selectedMapping) {
      onDeleteMapping(selectedMapping);
    }
  }, [onDeleteMapping, selectedMapping]);
  //#endregion

  //#region namespace table dialogs
  const [createNamespaceDialog, openCreateNamespaceDialog] = useNamespaceDialog(
    "Create namespace",
  );
  const onCreateNamespace = useCallback(
    (docName: string) => {
      openCreateNamespaceDialog(
        ({ alias, uri, locationUri, targetNamespace }) =>
          createNamespace(docName, alias, uri, locationUri, targetNamespace),
      );
    },
    [createNamespace, openCreateNamespaceDialog],
  );
  const [editNamespaceDialog, openEditNamespaceDialog] = useNamespaceDialog(
    "Edit namespace",
  );
  const onEditNamespace = useCallback(
    (docName: string, namespace: INamespace) => {
      openEditNamespaceDialog(
        ({ alias, uri, locationUri, targetNamespace }) =>
          editNamespace(
            docName,
            namespace.alias,
            alias,
            uri,
            locationUri,
            targetNamespace,
          ),
        namespace,
      );
    },
    [editNamespace, openEditNamespaceDialog],
  );
  //#endregion

  const [
    createEnableCustomClassDialog,
    openCreateEnableCustomClassDialog,
  ] = useCustomClassDialog("Enable Custom Class");

  const onEnableCustomClass = useCallback(
    (isSource: boolean): void => {
      openCreateEnableCustomClassDialog(({ value, collectionType }) =>
        enableCustomClass(value, collectionType, isSource),
      );
    },
    [openCreateEnableCustomClassDialog],
  );

  const portal = useMemo(
    () =>
      createPortal(
        <>
          {importCatalogDialog}
          {exportCatalogDialog}
          {importDocumentDialog}
          {deleteDocumentDialog}
          {createConstantDialog}
          {deleteConstantDialog}
          {editConstantDialog}
          {createPropertyDialog}
          {deletePropertyDialog}
          {editPropertyDialog}
          {resetDialog}
          {removeMappedFieldDialog}
          {deleteMappingDialog}
          {createEnableCustomClassDialog}
          {createNamespaceDialog}
          {editNamespaceDialog}
          {toggleExpressionModeDialog}
        </>,
        modalContainer,
      ),
    [
      createConstantDialog,
      createEnableCustomClassDialog,
      createNamespaceDialog,
      createPropertyDialog,
      deleteConstantDialog,
      deleteDocumentDialog,
      deleteMappingDialog,
      deletePropertyDialog,
      editConstantDialog,
      editNamespaceDialog,
      editPropertyDialog,
      exportCatalogDialog,
      importCatalogDialog,
      importDocumentDialog,
      modalContainer,
      removeMappedFieldDialog,
      resetDialog,
      toggleExpressionModeDialog,
    ],
  );

  return {
    handlers: {
      onImportAtlasCatalog,
      onExportAtlasCatalog,
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
      onDeleteMapping,
      onDeleteSelectedMapping,
      onEnableCustomClass,
      onCreateNamespace,
      onEditNamespace,
      deleteNamespace,
      onToggleExpressionMode,
    },
    dialogs: portal,
  };
}
