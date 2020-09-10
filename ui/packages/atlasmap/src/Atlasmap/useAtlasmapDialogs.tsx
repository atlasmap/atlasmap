import React, { useCallback, useMemo, useState } from "react";
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
import { enableCustomClass, getPropertyScopeOptions } from "./utils";
import { IAtlasmapDocument } from "../Views";

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
  const onCreateConstant = useCallback(
    (constants: IAtlasmapDocument | null) => {
      openCreateConstantDialog(({ value, valueType }) => {
        createConstant(value, valueType);
      }, constants);
    },
    [createConstant, openCreateConstantDialog],
  );

  const [editConstantDialog, openEditConstantDialog] = useConstantDialog(
    "Edit Constant",
  );
  const onEditConstant = useCallback(
    (constant: IConstant, constants: IAtlasmapDocument | null) => {
      openEditConstantDialog(
        ({ value, valueType }) => {
          editConstant(constant.value, value, valueType);
        },
        constants,
        constant,
      );
    },
    [editConstant, openEditConstantDialog],
  );
  const [deleteConstantDialog, onDeleteConstant] = useDeleteConstantDialog();
  //#endregion

  //#region property dialogs
  const [scopeOptions, setScopeOptions] = useState(
    getPropertyScopeOptions(true),
  );
  const [createPropertyDialog, openCreatePropertyDialog] = usePropertyDialog(
    "Create Property",
    scopeOptions,
  );
  const onCreateProperty = useCallback(
    (isSource: boolean, properties: IAtlasmapDocument | null) => {
      setScopeOptions(getPropertyScopeOptions(isSource));
      openCreatePropertyDialog(({ name, valueType, scope }) => {
        createProperty(name, valueType, scope, isSource);
      }, properties);
    },
    [createProperty, openCreatePropertyDialog],
  );
  const [editPropertyDialog, openEditPropertyDialog] = usePropertyDialog(
    "Edit Property",
    scopeOptions,
  );
  const onEditProperty = useCallback(
    (
      property: IProperty,
      isSource: boolean,
      properties: IAtlasmapDocument | null,
    ) => {
      setScopeOptions(getPropertyScopeOptions(isSource));
      openEditPropertyDialog(
        ({ name, valueType, scope }) => {
          editProperty(property.name, valueType, scope, name, isSource);
        },
        properties,
        property,
      );
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
  ] = useCustomClassDialog("Load Java Document From Custom Class");

  const onEnableCustomClass = useCallback(
    (isSource: boolean): void => {
      openCreateEnableCustomClassDialog(({ customClassName, collectionType }) =>
        enableCustomClass(customClassName, collectionType, isSource),
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
