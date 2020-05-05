/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { FunctionComponent, useCallback, useMemo } from "react";

import { CanvasControlBar, MainLayout, ViewToolbar } from "../Layout";
import {
  CanvasProvider,
  ConditionalExpressionInput,
  FieldDragLayer,
  FieldsDndProvider,
} from "../UI";
import {
  IAtlasmapField,
  IMappingDocumentEvents,
  ISourceColumnCallbacks,
  ITargetsColumnCallbacks,
  MappingTableView,
  SourceMappingTargetView,
  SourceTargetView,
} from "../Views";
import { useAtlasmap } from "./AtlasmapProvider";
import { useAtlasmapDialogs } from "./useAtlasmapDialogs";
import { IUseContextToolbarData, useContextToolbar } from "./useContextToolbar";
import { useSidebar } from "./useSidebar";
import {
  AlertGroup,
  Alert,
  AlertActionCloseButton,
} from "@patternfly/react-core";

export interface IAtlasmapProps extends IUseContextToolbarData {
  modalsContainerId?: string;
}

export const Atlasmap: FunctionComponent<IAtlasmapProps> = ({
  modalsContainerId = "modals",
  ...props
}) => {
  const {
    pending,
    // error,
    notifications,
    markNotificationRead,
    properties,
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
    // expression
    currentMappingExpression,
    executeFieldSearch,
    mappingExpressionAddField,
    mappingExpressionClearText,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    onConditionalMappingExpressionEnabled,
    onToggleExpressionMode,
    isMappingExpressionEmpty,
    trailerId,
    isFieldAddableToSelection,
    isFieldRemovableFromSelection,
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
    ...props,
    onImportAtlasFile: (file) => handlers.onImportDocument(file, false),
    onExportAtlasFile: handlers.onExportAtlasFile,
    onResetAtlasmap: handlers.onResetAtlasmap,
  });

  const shouldShowMappingPreviewForField = useCallback(
    (field: IAtlasmapField) =>
      showMappingPreview &&
      !!selectedMapping &&
      !!(
        selectedMapping.sourceFields.find((s) => s.id === field.id) ||
        selectedMapping.targetFields.find((t) => t.id === field.id)
      ),
    [selectedMapping, showMappingPreview],
  );

  const viewToolbar = (
    <ViewToolbar>
      <ConditionalExpressionInput
        mappingExpression={currentMappingExpression}
        executeFieldSearch={executeFieldSearch}
        mappingExpressionAddField={mappingExpressionAddField}
        mappingExpressionClearText={mappingExpressionClearText}
        isMappingExpressionEmpty={isMappingExpressionEmpty}
        mappingExpressionInit={mappingExpressionInit}
        mappingExpressionInsertText={mappingExpressionInsertText}
        mappingExpressionObservable={mappingExpressionObservable}
        mappingExpressionRemoveField={mappingExpressionRemoveField}
        onConditionalMappingExpressionEnabled={
          onConditionalMappingExpressionEnabled
        }
        onToggleExpressionMode={onToggleExpressionMode}
        trailerId={trailerId}
      />
    </ViewToolbar>
  );

  const sourceEvents = useMemo<ISourceColumnCallbacks>(
    () => ({
      canDrop: () => true,
      onDrop: (s, t) =>
        handlers.onCreateMapping(s, t.payload as IAtlasmapField),
      onShowMappingDetails: selectMapping,
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("source", f),
      onAddToSelectedMapping: handlers.onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("source", f),
      onRemoveFromSelectedMapping: handlers.onRemoveFromMapping,
      onCreateConstant: handlers.onCreateConstant,
      onEditConstant: handlers.onEditConstant,
      onDeleteConstant: handlers.onDeleteConstant,
      onCreateProperty: handlers.onCreateProperty,
      onEditProperty: handlers.onEditProperty,
      onDeleteProperty: handlers.onDeleteProperty,
      onDeleteDocument: (id) => handlers.onDeleteDocument(id, true),
      onEnableJavaClasses: () => void 0,
      onImportDocument: (id) => handlers.onImportDocument(id, true),
      onSearch: searchSources,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: () => true, // TODO: check that there is at least one target field unmapped and compatible
      onStartMapping: (field) => handlers.onCreateMapping(field, undefined),
    }),
    [
      selectMapping,
      handlers,
      searchSources,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
    ],
  );

  const targetEvents = useMemo<ITargetsColumnCallbacks>(
    () => ({
      canDrop: (f) => !f.isConnected,
      onDrop: (s, t) =>
        handlers.onCreateMapping(t.payload as IAtlasmapField, s),
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("target", f),
      onShowMappingDetails: selectMapping,
      onAddToSelectedMapping: handlers.onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("target", f),
      onRemoveFromSelectedMapping: handlers.onRemoveFromMapping,
      onDeleteDocument: (id) => handlers.onDeleteDocument(id, false),
      onEnableJavaClasses: () => void 0,
      onImportDocument: (id) => handlers.onImportDocument(id, false),
      onSearch: searchTargets,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: (field) => !field.isConnected,
      onStartMapping: (field) => handlers.onCreateMapping(undefined, field),
    }),
    [
      selectMapping,
      handlers,
      searchTargets,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
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
            properties={properties}
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
            properties={properties}
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
        return <MappingTableView mappings={mappings} />;
      default:
        return <>TODO</>;
    }
  }, [
    activeView,
    constants,
    mappingEvents,
    mappings,
    properties,
    selectMapping,
    selectedMapping,
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
            .filter((n) => !n.isRead)
            .map(({ id, variant, message }) => (
              <Alert
                isLiveRegion
                variant={variant}
                title={message}
                key={id}
                action={
                  <AlertActionCloseButton
                    title={message}
                    variantLabel={`${variant} alert`}
                    onClose={() => markNotificationRead(id)}
                  />
                }
              />
            ))}
        </AlertGroup>
        {dialogs}
      </CanvasProvider>
    </FieldsDndProvider>
  );
};
