import { IConstant, IDocumentName, INamespace, IProperty } from "../UI";
import React, { useCallback, useMemo, useState } from "react";
import { enableCustomClass, getPropertyScopeOptions } from "./utils";
import {
  useAboutDialog,
  useCaptureDocumentNameToast,
  useChangeDocumentNameDialog,
  useConstantDialog,
  useCustomClassDialog,
  useDeleteConstantDialog,
  useDeleteDocumentDialog,
  useDeleteMappingDialog,
  useDeletePropertyDialog,
  useEditMappingEnumerationDialog,
  useExportCatalogDialog,
  useImportCatalogDialog,
  useImportDocumentDialog,
  useNamespaceDialog,
  usePropertyDialog,
  useRemoveMappedFieldDialog,
  useResetAtlasmapDialog,
  useSpecifyInstanceSchemaDialog,
  useToggleExpressionModeDialog,
} from "./dialogs";

import { IAtlasmapDocument } from "../Views";
import { createPortal } from "react-dom";
import { useAtlasmap } from "./AtlasmapProvider";

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
    changeDocumentName,
  } = useAtlasmap();

  //#region constant dialogs
  const [createConstantDialog, openCreateConstantDialog] =
    useConstantDialog("Create Constant");
  const onCreateConstant = useCallback(
    (constants: IAtlasmapDocument | null, addToActiveMapping?: boolean) => {
      openCreateConstantDialog(({ value, valueType }) => {
        createConstant(value, valueType, addToActiveMapping);
      }, constants);
    },
    [createConstant, openCreateConstantDialog],
  );

  const [editConstantDialog, openEditConstantDialog] =
    useConstantDialog("Edit Constant");
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
    (
      isSource: boolean,
      properties: IAtlasmapDocument | null,
      addToActiveMapping?: boolean,
    ) => {
      setScopeOptions(getPropertyScopeOptions(isSource));
      openCreatePropertyDialog(({ name, valueType, scope }) => {
        createProperty(name, valueType, scope, isSource, addToActiveMapping);
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
          editProperty(
            property.name,
            valueType,
            property.scope,
            isSource,
            name,
            scope,
          );
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
  const [aboutDialog, onAbout] = useAboutDialog();
  const [toggleExpressionModeDialog, onToggleExpressionMode] =
    useToggleExpressionModeDialog();
  //#endregion

  //#region document dialogs
  const [importDocumentDialog, onImportDocument] = useImportDocumentDialog();
  const [deleteDocumentDialog, onDeleteDocument] = useDeleteDocumentDialog();
  const [captureDocumentNameToast, onCaptureDocumentName] =
    useCaptureDocumentNameToast();

  const [changeDocumentNameDialog, openChangeDocumentNameDialog] =
    useChangeDocumentNameDialog();
  const onChangeDocumentName = useCallback(
    (docNameInfo: IDocumentName) => {
      openChangeDocumentNameDialog(({ id, name, isSource }) => {
        changeDocumentName(id, name, isSource);
      }, docNameInfo);
    },
    [changeDocumentName, openChangeDocumentNameDialog],
  );

  const [removeMappedFieldDialog, onRemoveMappedField] =
    useRemoveMappedFieldDialog();
  const [deleteMappingDialog, onDeleteMapping] = useDeleteMappingDialog();
  const onDeleteSelectedMapping = useCallback(() => {
    if (selectedMapping) {
      onDeleteMapping(selectedMapping);
    }
  }, [onDeleteMapping, selectedMapping]);

  const [editMappingEnumerationDialog, onEditMappingEnumeration] =
    useEditMappingEnumerationDialog();

  const [specifyInstanceSchemaDialog, onSpecifyInstanceSchema] =
    useSpecifyInstanceSchemaDialog(false);
  //#endregion

  //#region namespace table dialogs
  const [createNamespaceDialog, openCreateNamespaceDialog] =
    useNamespaceDialog("Create namespace");
  const onCreateNamespace = useCallback(
    (docName: string) => {
      openCreateNamespaceDialog(
        ({ alias, uri, locationUri, targetNamespace }) =>
          createNamespace(docName, alias, uri, locationUri, targetNamespace),
      );
    },
    [createNamespace, openCreateNamespaceDialog],
  );
  const [editNamespaceDialog, openEditNamespaceDialog] =
    useNamespaceDialog("Edit namespace");
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

  const [createEnableCustomClassDialog, openCreateEnableCustomClassDialog] =
    useCustomClassDialog("Load Java Document From Custom Class");

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
          {specifyInstanceSchemaDialog}
          {captureDocumentNameToast}
          {changeDocumentNameDialog}
          {createConstantDialog}
          {deleteConstantDialog}
          {editConstantDialog}
          {createPropertyDialog}
          {deletePropertyDialog}
          {editPropertyDialog}
          {resetDialog}
          {aboutDialog}
          {removeMappedFieldDialog}
          {deleteMappingDialog}
          {createEnableCustomClassDialog}
          {createNamespaceDialog}
          {editNamespaceDialog}
          {toggleExpressionModeDialog}
          {editMappingEnumerationDialog}
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
      captureDocumentNameToast,
      changeDocumentNameDialog,
      deleteMappingDialog,
      deletePropertyDialog,
      editConstantDialog,
      editNamespaceDialog,
      editPropertyDialog,
      exportCatalogDialog,
      importCatalogDialog,
      importDocumentDialog,
      specifyInstanceSchemaDialog,
      modalContainer,
      removeMappedFieldDialog,
      resetDialog,
      aboutDialog,
      toggleExpressionModeDialog,
      editMappingEnumerationDialog,
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
      onAbout,
      onImportDocument,
      onDeleteDocument,
      onSpecifyInstanceSchema,
      onCaptureDocumentName,
      onChangeDocumentName,
      onRemoveMappedField,
      onDeleteMapping,
      onDeleteSelectedMapping,
      onEnableCustomClass,
      onCreateNamespace,
      onEditNamespace,
      deleteNamespace,
      onToggleExpressionMode,
      onEditMappingEnumeration,
    },
    dialogs: portal,
  };
}
