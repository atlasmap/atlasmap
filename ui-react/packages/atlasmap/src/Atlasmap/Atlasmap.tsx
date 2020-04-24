import React, {
  FunctionComponent,
  useCallback,
  useMemo,
  useState,
} from "react";

import { DataMapperUtil } from "@atlasmap/core";

import { CanvasControlBar, MainLayout, ViewToolbar } from "../Layout";
import {
  CanvasProvider,
  ConditionalExpressionInput,
  FieldDragLayer,
  FieldsDndProvider,
} from "../UI";
import {
  IAtlasmapField,
  IAtlasmapFieldWithField,
  IMappingDocumentEvents,
  ISourceColumnEvents,
  ITargetsColumnEvents,
  MappingDetailsView,
  MappingTableView,
  SourceMappingTargetView,
  SourceTargetView,
  GroupId,
} from "../Views";
import { useAtlasmap } from "./AtlasmapProvider";
import { useAtlasmapDialogs } from "./useAtlasmapDialogs";
import { IUseContextToolbarData, useContextToolbar } from "./useContextToolbar";
import { addToCurrentMapping, removeFromCurrentMapping } from "./utils";

export interface IAtlasmapProps extends IUseContextToolbarData {
  modalsContainerId?: string;
}

export const Atlasmap: FunctionComponent<IAtlasmapProps> = ({
  modalsContainerId = "modals",
  ...props
}) => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();

  const {
    pending,
    // error,
    properties,
    constants,
    sources,
    targets,
    mappings,
    selectedMapping,
    selectMapping,
    deselectMapping,
    createMapping,
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

    // expression
    currentMappingExpression,
    executeFieldSearch,
    mappingExpressionAddField,
    mappingExpressionClearText,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    isMappingExpressionEmpty,
    trailerId,

    //mapping details
    getMappingActions,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleActionChange,
  } = useAtlasmap({
    sourcesSearchString: sourceFilter,
    targetsSearchString: targetFilter,
  });

  const { handlers, dialogs } = useAtlasmapDialogs({
    modalContainer: document.getElementById(modalsContainerId)!,
  });

  const handleExportAtlasFile = () => {
    handlers.onExportAtlasFile(exportAtlasFile);
  };

  const handleImportAtlasFile = (file: File) => {
    importAtlasFile(file, false);
  };

  const handleResetAtlasmap = () => {
    handlers.onResetAtlasmap(resetAtlasmap);
  };

  const {
    activeView,
    showMappingColumn,
    showMappingPreview,
    showTypes,
    contextToolbar,
  } = useContextToolbar({
    ...props,
    onImportAtlasFile: handleImportAtlasFile,
    onExportAtlasFile: handleExportAtlasFile,
    onResetAtlasmap: handleResetAtlasmap,
  });

  const handleImportDocument = useCallback(
    (selectedFile: File, isSource: boolean) => {
      if (documentExists(selectedFile, isSource)) {
        handlers.onImportDocument(() =>
          importAtlasFile(selectedFile, isSource),
        );
      } else {
        importAtlasFile(selectedFile, isSource);
      }
    },
    [documentExists, handlers, importAtlasFile],
  );

  const handleImportSourceDocument = useCallback(
    (selectedFile: File) => handleImportDocument(selectedFile, true),
    [handleImportDocument],
  );

  const handleImportTargetDocument = useCallback(
    (selectedFile: File) => handleImportDocument(selectedFile, false),
    [handleImportDocument],
  );

  const handleCreateConstant = useCallback(() => {
    handlers.onCreateConstant(createConstant);
  }, [createConstant, handlers]);

  const handleCreateProperty = useCallback(() => {
    handlers.onCreateProperty(createProperty);
  }, [createProperty, handlers]);

  const handleDeleteConstant = useCallback(
    (constValue: string) => {
      handlers.onDeleteConstant(() => deleteConstant(constValue));
    },
    [deleteConstant, handlers],
  );

  const handleEditConstant = useCallback(
    (constVal: string) => {
      handlers.onEditConstant(constVal, editConstant);
    },
    [editConstant, handlers],
  );

  const handleDeleteProperty = useCallback(
    (propName: string) => {
      handlers.onDeleteProperty(() => deleteProperty(propName));
    },
    [deleteProperty, handlers],
  );

  const handleEditProperty = useCallback(
    (field: string) => {
      handlers.onEditProperty(field, editProperty);
    },
    [editProperty, handlers],
  );

  const handleDeleteDocument = useCallback(
    (id: GroupId, isSource: boolean) => {
      handlers.onDeleteDocument(() => deleteAtlasFile(id, isSource));
    },
    [handlers, deleteAtlasFile],
  );

  const handleDeleteSourceDocument = useCallback(
    (id: GroupId) => handleDeleteDocument(id, true),
    [handleDeleteDocument],
  );

  const handleDeleteTargetDocument = useCallback(
    (id: GroupId) => handleDeleteDocument(id, false),
    [handleDeleteDocument],
  );

  // const handleFieldPreviewChange = useCallback(
  //   (field: IAtlasmapField, value: string) => {
  //     onFieldPreviewChange(field as IAtlasmapFieldWithField, value);
  //   },
  //   [onFieldPreviewChange],
  // );

  const handleAddToMapping = useCallback((node: IAtlasmapField) => {
    const field = (node as IAtlasmapFieldWithField).amField;
    addToCurrentMapping(field);
  }, []);

  const handleRemoveFromMapping = useCallback((node: IAtlasmapField) => {
    const field = (node as IAtlasmapFieldWithField).amField;
    removeFromCurrentMapping(field);
  }, []);

  const handleCreateMapping = useCallback(
    (source: IAtlasmapField, target: IAtlasmapField) => {
      const sourceField = (source as IAtlasmapFieldWithField).amField;
      const targetField = (target as IAtlasmapFieldWithField).amField;
      createMapping(sourceField, targetField);
    },
    [createMapping],
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
        trailerId={trailerId}
      />
    </ViewToolbar>
  );

  const isFieldAddableToSelection = useCallback(
    (documentType: "source" | "target", field: IAtlasmapField) => {
      if (!selectedMapping) {
        return false;
      }
      if (
        selectedMapping.sourceFields.length <= 1 &&
        selectedMapping.targetFields.length <= 1
      ) {
        if (
          documentType === "source" &&
          !selectedMapping.sourceFields.find((f) => f.id === field.id)
        ) {
          return true;
        } else if (
          !selectedMapping.targetFields.find((f) => f.id === field.id)
        ) {
          return true;
        }
      } else if (
        documentType === "source" &&
        selectedMapping.targetFields.length <= 1 &&
        !selectedMapping.sourceFields.find((f) => f.id === field.id)
      ) {
        return true;
      } else if (
        documentType === "target" &&
        selectedMapping.sourceFields.length <= 1 &&
        !selectedMapping.targetFields.find((f) => f.id === field.id)
      ) {
        return true;
      }
      return false;
    },
    [selectedMapping],
  );

  const isFieldRemovableFromSelection = useCallback(
    (documentType: "source" | "target", field: IAtlasmapField) =>
      !!selectedMapping && !isFieldAddableToSelection(documentType, field),
    [isFieldAddableToSelection, selectedMapping],
  );

  const sourceEvents = useMemo<ISourceColumnEvents>(
    () => ({
      canDrop: () => true,
      onDrop: (s, t) => handleCreateMapping(s, t.payload as IAtlasmapField),
      onShowMappingDetails: selectMapping,
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("source", f),
      onAddToSelectedMapping: handleAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("source", f),
      onRemoveFromSelectedMapping: handleRemoveFromMapping,
      onCreateConstant: handleCreateConstant,
      onEditConstant: handleEditConstant,
      onDeleteConstant: handleDeleteConstant,
      onCreateProperty: handleCreateProperty,
      onEditProperty: handleEditProperty,
      onDeleteProperty: handleDeleteProperty,
      onDeleteDocument: handleDeleteSourceDocument,
      onEnableJavaClasses: () => void 0,
      onImportDocument: handleImportSourceDocument,
      onSearch: setSourceFilter,
    }),
    [
      handleAddToMapping,
      handleCreateConstant,
      handleCreateMapping,
      handleCreateProperty,
      handleDeleteConstant,
      handleDeleteProperty,
      handleDeleteSourceDocument,
      handleEditConstant,
      handleEditProperty,
      handleImportSourceDocument,
      handleRemoveFromMapping,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
      selectMapping,
    ],
  );

  const targetEvents = useMemo<ITargetsColumnEvents>(
    () => ({
      canDrop: (f) => !f.isConnected,
      onDrop: (s, t) => handleCreateMapping(t.payload as IAtlasmapField, s),
      canAddToSelectedMapping: (f) => isFieldAddableToSelection("target", f),
      onShowMappingDetails: selectMapping,
      onAddToSelectedMapping: handleAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection("target", f),
      onRemoveFromSelectedMapping: handleRemoveFromMapping,
      onDeleteDocument: handleDeleteTargetDocument,
      onEnableJavaClasses: () => void 0,
      onImportDocument: handleImportTargetDocument,
      onSearch: setTargetFilter,
    }),
    [
      handleAddToMapping,
      handleCreateMapping,
      handleDeleteTargetDocument,
      handleImportTargetDocument,
      handleRemoveFromMapping,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
      selectMapping,
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
            onDeselectMapping={deselectMapping}
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
            onDeselectMapping={deselectMapping}
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
    deselectMapping,
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

  const renderSidebar = useCallback(() => {
    if (selectedMapping) {
      const m = selectedMapping.mapping;
      const sources = m.getMappedFields(true);
      const showSourcesIndex =
        sources.length > 1 &&
        !m.transition.enableExpression &&
        m.transition.isManyToOneMode();
      const targets = m.getMappedFields(false);
      const showTargetsIndex =
        targets.length > 1 &&
        !m.transition.enableExpression &&
        m.transition.isOneToManyMode();
      const availableActions = getMultiplicityActions(m);
      const actionsOptions = availableActions.map((a) => ({
        name: DataMapperUtil.toDisplayable(a.name),
        value: a.name,
      }));
      const availableDelimiters = getMultiplicityActionDelimiters();
      const actionDelimiters = availableDelimiters.map((a) => ({
        displayName: a.prettyName!,
        delimiterValue: a.actualDelimiter,
      }));
      const multiplicityFieldAction = m.transition.transitionFieldAction;
      const handleRemoveMapping = () => {
        handlers.onDeleteMapping(() => {
          removeMapping(m);
          deselectMapping();
        });
      };
      return (
        <MappingDetailsView
          mapping={selectedMapping}
          onClose={deselectMapping}
          onRemoveMapping={handleRemoveMapping}
          onRemoveMappedField={handleRemoveFromMapping}
          sources={sources}
          showSourcesIndex={showSourcesIndex}
          targets={targets}
          showTargetsIndex={showTargetsIndex}
          availableActions={availableActions}
          actionsOptions={actionsOptions}
          actionDelimiters={actionDelimiters}
          multiplicityFieldAction={multiplicityFieldAction}
          onActionChange={handleActionChange}
          getMappingActions={getMappingActions}
        />
      );
    }
    return <>TODO: error</>;
  }, [
    deselectMapping,
    getMappingActions,
    getMultiplicityActionDelimiters,
    getMultiplicityActions,
    handleActionChange,
    handleRemoveFromMapping,
    handlers,
    removeMapping,
    selectedMapping,
  ]);

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
        {dialogs}
      </CanvasProvider>
    </FieldsDndProvider>
  );
};
