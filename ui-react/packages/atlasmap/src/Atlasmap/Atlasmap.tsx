/* eslint-disable @typescript-eslint/no-unused-vars */
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
  IMappingDocumentEvents,
  ISourceColumnCallbacks,
  ITargetsColumnCallbacks,
  MappingDetailsView,
  MappingTableView,
  SourceMappingTargetView,
  SourceTargetView,
  GroupId,
  IMappingDetailsViewProps,
} from "../Views";
import { useAtlasmap } from "./AtlasmapProvider";
import { useAtlasmapDialogs } from "./useAtlasmapDialogs";
import { IUseContextToolbarData, useContextToolbar } from "./useContextToolbar";

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
    onFieldPreviewChange,
    addToCurrentMapping,
    removeFromCurrentMapping,
    removeMappedFieldFromCurrentMapping,
    fromMappedFieldToIMappingField,

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

    //mapping details
    getMappingActions,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
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

  const handleAddToMapping = useCallback(
    (node: IAtlasmapField) => {
      const field = (node as IAtlasmapField).amField;
      addToCurrentMapping(field);
    },
    [addToCurrentMapping],
  );

  const handleRemoveFromMapping = useCallback(
    (node: IAtlasmapField) => {
      const field = (node as IAtlasmapField).amField;
      removeFromCurrentMapping(field);
    },
    [removeFromCurrentMapping],
  );

  const handleCreateMapping = useCallback(
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

  const sourceEvents = useMemo<ISourceColumnCallbacks>(
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
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: () => true, // TODO: check that there is at least one target field unmapped and compatible
      onStartMapping: (field) => handleCreateMapping(field, undefined),
    }),
    [
      selectMapping,
      handleAddToMapping,
      handleRemoveFromMapping,
      handleCreateConstant,
      handleEditConstant,
      handleDeleteConstant,
      handleCreateProperty,
      handleEditProperty,
      handleDeleteProperty,
      handleDeleteSourceDocument,
      handleImportSourceDocument,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      handleCreateMapping,
      isFieldAddableToSelection,
      isFieldRemovableFromSelection,
    ],
  );

  const targetEvents = useMemo<ITargetsColumnCallbacks>(
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
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      canStartMapping: (field) => !field.isConnected,
      onStartMapping: (field) => handleCreateMapping(undefined, field),
    }),
    [
      selectMapping,
      handleAddToMapping,
      handleRemoveFromMapping,
      handleDeleteTargetDocument,
      handleImportTargetDocument,
      shouldShowMappingPreviewForField,
      onFieldPreviewChange,
      handleCreateMapping,
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
      const sources = m
        .getMappedFields(true)
        .map(fromMappedFieldToIMappingField);
      const targets = m
        .getMappedFields(false)
        .map(fromMappedFieldToIMappingField);
      const showSourcesIndex =
        sources.length > 1 &&
        m.transition.isManyToOneMode() &&
        !m.transition.enableExpression;
      const showTargetsIndex =
        targets.length > 1 &&
        m.transition.isOneToManyMode() &&
        !m.transition.enableExpression;

      const multiplicityFieldAction = m.transition.transitionFieldAction;

      let multiplicity: IMappingDetailsViewProps["multiplicity"] = undefined;
      if (multiplicityFieldAction) {
        const transformations = getMultiplicityActions(m);
        const transformationsOptions = transformations.map((a) => ({
          label: DataMapperUtil.toDisplayable(a.name),
          name: a.name,
          value: a.name,
        }));
        const delimiters = getMultiplicityActionDelimiters();
        const delimitersOptions = delimiters.map((a) => ({
          name: a.prettyName!,
          value: a.actualDelimiter,
        }));

        multiplicity = {
          name: multiplicityFieldAction.name,
          transformationsOptions,
          transformationsArguments: multiplicityFieldAction.argumentValues.map(
            (a) => ({
              label: DataMapperUtil.toDisplayable(a.name),
              name: a.name,
              value: a.value,
              options: a.name === "delimiter" ? delimitersOptions : undefined,
            }),
          ),
          onChange: (name) =>
            handleMultiplicityChange(multiplicityFieldAction, name),
          onArgumentChange: (argumentName, arguemntValue) =>
            handleMultiplicityArgumentChange(
              multiplicityFieldAction,
              argumentName,
              arguemntValue,
            ),
        };
      }
      const sourceTransformations = getMappingActions(true);
      const sourceTransformationsOptions = sourceTransformations.map((a) => ({
        name: DataMapperUtil.toDisplayable(a.name),
        value: a.name,
      }));
      const targetTransformations = getMappingActions(false);
      const targetTransformationsOptions = targetTransformations.map((a) => ({
        name: DataMapperUtil.toDisplayable(a.name),
        value: a.name,
      }));
      const handleRemoveMapping = () => {
        handlers.onDeleteMapping(() => {
          removeMapping(m);
          deselectMapping();
        });
      };

      const handleRemoveMappedField = (isSource: boolean, index: number) => {
        const amField = selectedMapping.mapping.getMappedFieldForIndex(
          "" + (index + 1),
          isSource,
        );
        console.log(amField);
        if (amField) {
          removeMappedFieldFromCurrentMapping(amField);
        }
      };

      return (
        <MappingDetailsView
          sources={sources}
          targets={targets}
          onClose={deselectMapping}
          onRemoveMapping={handleRemoveMapping}
          onRemoveMappedField={handleRemoveMappedField}
          showSourcesIndex={showSourcesIndex}
          showTargetsIndex={showTargetsIndex}
          multiplicity={multiplicity}
          sourceTransformationsOptions={sourceTransformationsOptions}
          targetTransformationsOptions={targetTransformationsOptions}
          onIndexChange={handleIndexChange}
          onNewTransformation={handleNewTransformation}
          onRemoveTransformation={handleRemoveTransformation}
          onTransformationChange={handleTransformationChange}
          onTransformationArgumentChange={handleTransformationArgumentChange}
        />
      );
    }
    return <>TODO: error</>;
  }, [
    selectedMapping,
    getMappingActions,
    fromMappedFieldToIMappingField,
    deselectMapping,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
    handlers,
    removeMapping,
    removeMappedFieldFromCurrentMapping,
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
