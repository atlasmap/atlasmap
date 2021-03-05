/* eslint-disable @typescript-eslint/no-unused-vars */
import { AlertGroup } from "@patternfly/react-core";
import React, { FunctionComponent, useCallback, useMemo } from "react";
import { CanvasControlBar, MainLayout, ViewToolbar } from "../Layout";
import {
  CanvasProvider,
  ConditionalExpressionInput,
  FieldDragLayer,
  FieldsDndProvider,
  TimedToast,
} from "../UI";
import {
  IAtlasmapField,
  IMappingDocumentEvents,
  ISourceColumnCallbacks,
  ITargetsColumnCallbacks,
  MappingTableView,
  NamespaceTableView,
  SourceMappingTargetView,
  SourceTargetView,
  IAtlasmapMapping,
} from "../Views";
import { useAtlasmap } from "./AtlasmapProvider";
import { useAtlasmapDialogs } from "./useAtlasmapDialogs";
import { IUseContextToolbarData, useContextToolbar } from "./useContextToolbar";
import { useSidebar } from "./useSidebar";
import { getPropertyType, getConstantType } from "./utils";
export interface IAtlasmapProps {
  allowImport?: boolean;
  allowExport?: boolean;
  allowReset?: boolean;
  allowDelete?: boolean;
  allowCustomJavaClasses?: boolean;
  modalsContainerId?: string;
  toolbarOptions?: IUseContextToolbarData;
}

export const Atlasmap: FunctionComponent<IAtlasmapProps> = ({
  allowImport = true,
  allowExport = true,
  allowReset = true,
  allowDelete = true,
  allowCustomJavaClasses = true,
  modalsContainerId = "modals",
  toolbarOptions,
}) => {
  const {
    pending,
    // error,
    notifications,
    markNotificationRead,
    sourceProperties,
    targetProperties,
    constants,
    sources,
    targets,
    mappings,
    selectedMapping,
    selectMapping,
    deselectMapping,
    onFieldPreviewChange,
    searchSources,
    searchTargets,
    importAtlasFile,
    // expression
    currentMappingExpression,
    executeFieldSearch,
    mappingExpressionAddField,
    mappingExpressionClearText,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    mappingExpressionEnabled,
    isMappingExpressionEmpty,
    trailerId,
    isFieldAddableToSelection,
    isFieldRemovableFromSelection,
    onAddToMapping,
    onRemoveFromMapping,
    onCreateMapping,
    isEnumerationMapping,
  } = useAtlasmap();

  const { handlers, dialogs } = useAtlasmapDialogs({
    modalContainer: document.getElementById(modalsContainerId)!,
  });
  const {
    activeView,
    showMappingColumn,
    showMappingPreview,
    showTypes,
    contextToolbar,
  } = useContextToolbar({
    showImportAtlasFileToolbarItem: allowImport,
    showImportJarFileToolbarItem: allowImport,
    showExportAtlasFileToolbarItem: allowExport,
    showResetToolbarItem: allowReset,
    ...toolbarOptions,
    onImportAtlasFile: handlers.onImportAtlasCatalog,
    onImportJarFile: (file) => importAtlasFile(file, false, false),
    onExportAtlasFile: handlers.onExportAtlasCatalog,
    onResetAtlasmap: handlers.onResetAtlasmap,
  });

  const shouldShowMappingPreviewForField = useCallback(
    (field: IAtlasmapField) =>
      showMappingPreview &&
      !!selectedMapping &&
      field.isConnected &&
      !!field.mappings.find((m) => m.id === selectedMapping.id),
    [selectedMapping, showMappingPreview],
  );

  const shouldShowMappingPreview = useCallback(
    (mapping: IAtlasmapMapping) =>
      showMappingPreview &&
      !!selectedMapping &&
      mapping.id === selectedMapping.id,
    [selectedMapping, showMappingPreview],
  );

  const viewToolbar = (
    <ViewToolbar>
      <ConditionalExpressionInput
        mappingExpression={
          mappingExpressionEnabled ? currentMappingExpression : undefined
        }
        executeFieldSearch={executeFieldSearch}
        mappingExpressionAddField={mappingExpressionAddField}
        mappingExpressionClearText={mappingExpressionClearText}
        isMappingExpressionEmpty={isMappingExpressionEmpty}
        mappingExpressionInit={mappingExpressionInit}
        mappingExpressionInsertText={mappingExpressionInsertText}
        mappingExpressionObservable={mappingExpressionObservable}
        mappingExpressionRemoveField={mappingExpressionRemoveField}
        trailerId={trailerId}
        disabled={!selectedMapping}
        onToggle={handlers.onToggleExpressionMode}
      />
    </ViewToolbar>
  );

  const sourceEvents = useMemo<ISourceColumnCallbacks>(
    () => ({
      isSource: true,
      acceptDropType: "target",
      draggableType: "source",
      canDrop: (dt, i) => {
        return isFieldAddableToSelection(
          "target",
          i.payload as IAtlasmapField,
          dt,
        );
      },
      onDrop: (s, t) => onCreateMapping(s, t.payload as IAtlasmapField),
      onShowMappingDetails: selectMapping,
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("source", f),
      onAddToSelectedMapping: onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("source", f),
      onRemoveFromSelectedMapping: onRemoveFromMapping,
      onCreateConstant: () => handlers.onCreateConstant(constants),
      onEditConstant: (constant) => {
        const [value] = constant.split(" ");
        const valueType = getConstantType(value);

        handlers.onEditConstant({ value, valueType }, constants);
      },
      onDeleteConstant: handlers.onDeleteConstant,
      onCreateProperty: (isSource: boolean) => {
        handlers.onCreateProperty(isSource, sourceProperties);
      },
      onEditProperty: (property, scope, isSource) => {
        const [leftPart] = property.split(" ");
        const valueType = getPropertyType(leftPart, scope, isSource);
        handlers.onEditProperty(
          {
            name: leftPart,
            valueType,
            scope,
          },
          true,
          sourceProperties,
        );
      },
      onDeleteProperty: handlers.onDeleteProperty,
      onDeleteDocument: allowDelete
        ? (id) => handlers.onDeleteDocument(id, true)
        : undefined,
      onCaptureDocumentName: (id) => handlers.onCaptureDocumentName(id),
      onChangeDocumentName: (id, name) =>
        handlers.onChangeDocumentName({
          id: id,
          name: name,
          isSource: true,
        }),
      onCustomClassSearch: allowCustomJavaClasses
        ? (isSource: boolean) => handlers.onEnableCustomClass(isSource)
        : undefined,
      onImportDocument: allowImport
        ? (id) => handlers.onImportDocument(id, true)
        : undefined,
      onSearch: searchSources,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: () => true, // TODO: check that there is at least one target field unmapped and compatible
      onStartMapping: (field) => onCreateMapping(field, undefined),
    }),
    [
      selectMapping,
      onAddToMapping,
      onRemoveFromMapping,
      handlers,
      constants,
      allowDelete,
      allowCustomJavaClasses,
      allowImport,
      searchSources,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      onCreateMapping,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
      sourceProperties,
    ],
  );

  const targetEvents = useMemo<ITargetsColumnCallbacks>(
    () => ({
      isSource: false,
      acceptDropType: "source",
      draggableType: "target",
      canDrop: (dt, i) => {
        return isFieldAddableToSelection(
          "source",
          i.payload as IAtlasmapField,
          dt,
        );
      },
      onDrop: (s, t) => onCreateMapping(t.payload as IAtlasmapField, s),
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("target", f),
      onShowMappingDetails: selectMapping,
      onAddToSelectedMapping: onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("target", f),
      onRemoveFromSelectedMapping: onRemoveFromMapping,
      onCreateProperty: (isSource: boolean) => {
        handlers.onCreateProperty(isSource, targetProperties);
      },
      onEditProperty: (property, scope, isSource) => {
        const [leftPart] = property.split(" ");
        const valueType = getPropertyType(leftPart, scope, isSource);
        handlers.onEditProperty(
          {
            name: leftPart,
            valueType,
            scope,
          },
          false,
          targetProperties,
        );
      },
      onDeleteProperty: handlers.onDeleteProperty,
      onDeleteDocument: allowDelete
        ? (id) => handlers.onDeleteDocument(id, false)
        : undefined,
      onCaptureDocumentName: (id) => handlers.onCaptureDocumentName(id),
      onChangeDocumentName: (id, name) =>
        handlers.onChangeDocumentName({
          id: id,
          name: name,
          isSource: false,
        }),
      onCustomClassSearch: allowCustomJavaClasses
        ? (isSource: boolean) => handlers.onEnableCustomClass(isSource)
        : undefined,
      onImportDocument: allowImport
        ? (id) => handlers.onImportDocument(id, false)
        : undefined,
      onSearch: searchTargets,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: (field) => !field.isConnected,
      onStartMapping: (field) => onCreateMapping(undefined, field),
    }),
    [
      selectMapping,
      onAddToMapping,
      onRemoveFromMapping,
      handlers,
      allowDelete,
      allowCustomJavaClasses,
      allowImport,
      searchTargets,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      onCreateMapping,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
      targetProperties,
    ],
  );

  const mappingEvents: IMappingDocumentEvents = useMemo(
    () => ({
      onSelectMapping: selectMapping,
      onDeselectMapping: deselectMapping,
      onEditMapping: () => void 0,
      onFieldPreviewChange: () => void 0,
      onMouseOver: () => void 0,
      onMouseOut: () => void 0,
      canDrop: (f, m) =>
        !m.sourceFields.find((s) => s.id === f.id) &&
        !m.targetFields.find((t) => t.id === f.id),
    }),
    [deselectMapping, selectMapping],
  );

  const currentView = useMemo(() => {
    switch (activeView) {
      case "ColumnMapper":
        return showMappingColumn ? (
          <SourceMappingTargetView
            sourceProperties={sourceProperties}
            targetProperties={targetProperties}
            constants={constants}
            sources={sources}
            mappings={mappings}
            targets={targets}
            selectedMappingId={selectedMapping?.id}
            onSelectMapping={selectMapping}
            showMappingPreview={showMappingPreview}
            showTypes={showTypes}
            sourceEvents={sourceEvents}
            mappingEvents={mappingEvents}
            targetEvents={targetEvents}
          />
        ) : (
          <SourceTargetView
            sourceProperties={sourceProperties}
            targetProperties={targetProperties}
            constants={constants}
            sources={sources}
            mappings={mappings}
            targets={targets}
            selectedMappingId={selectedMapping?.id}
            onSelectMapping={selectMapping}
            showMappingPreview={showMappingPreview}
            showTypes={showTypes}
            sourceEvents={sourceEvents}
            targetEvents={targetEvents}
          />
        );
      case "MappingTable":
        return (
          <MappingTableView
            mappings={mappings}
            onSelectMapping={selectMapping}
            shouldShowMappingPreview={shouldShowMappingPreview}
            onFieldPreviewChange={onFieldPreviewChange}
          />
        );
      case "NamespaceTable":
        return (
          <NamespaceTableView
            sources={sources}
            onCreateNamespace={handlers.onCreateNamespace}
            onEditNamespace={(
              docName: string,
              alias: string,
              uri: string,
              locationUri: string,
              targetNamespace: boolean,
            ) =>
              handlers.onEditNamespace(docName, {
                alias,
                uri,
                locationUri,
                targetNamespace,
              })
            }
            onDeleteNamespace={handlers.deleteNamespace}
          />
        );
      default:
        return <>TODO</>;
    }
  }, [
    activeView,
    constants,
    handlers,
    mappingEvents,
    mappings,
    onFieldPreviewChange,
    sourceProperties,
    targetProperties,
    selectMapping,
    selectedMapping,
    shouldShowMappingPreview,
    showMappingColumn,
    showMappingPreview,
    showTypes,
    sourceEvents,
    sources,
    targetEvents,
    targets,
  ]);

  const renderSidebar = useSidebar({
    onRemoveMapping: handlers.onDeleteSelectedMapping,
    onEditEnum: handlers.onEditMappingEnumeration,
    isEnumMapping: isEnumerationMapping,
  });

  return (
    <FieldsDndProvider>
      <CanvasProvider>
        <MainLayout
          loading={pending}
          contextToolbar={contextToolbar}
          viewToolbar={activeView !== "NamespaceTable" && viewToolbar}
          controlBar={activeView === "FreeView" && <CanvasControlBar />}
          showSidebar={!!selectedMapping}
          renderSidebar={renderSidebar}
        >
          {currentView}
        </MainLayout>
        <FieldDragLayer />
        <AlertGroup isToast>
          {notifications
            .filter((n) => !n.isRead && !n.mappingId)
            .slice(0, 5)
            .map(({ id, variant, title, description }) => (
              <TimedToast
                variant={variant}
                title={title}
                key={id}
                onClose={() => markNotificationRead(id)}
                onTimeout={() => markNotificationRead(id)}
              >
                {description}
              </TimedToast>
            ))}
        </AlertGroup>
        {dialogs}
      </CanvasProvider>
    </FieldsDndProvider>
  );
};
