/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import {
  CanvasProvider,
  ConditionalExpressionInput,
  FieldDragLayer,
  FieldsDndProvider,
  TimedToast,
} from '../UI';
import { ExpressionToolbar, MainLayout } from '../Layout';
import {
  IAtlasmapField,
  IAtlasmapMapping,
  ISourceColumnCallbacks,
  ITargetsColumnCallbacks,
  MappingTableView,
  NamespaceTableView,
  SourceTargetView,
} from '../Views';
import { IUseContextToolbarData, useContextToolbar } from './useContextToolbar';
import React, { FunctionComponent, useCallback, useMemo } from 'react';
import { getConstantType, getPropertyType } from './utils';

import { AlertGroup } from '@patternfly/react-core';
import { useAtlasmap } from './AtlasmapProvider';
import { useAtlasmapDialogs } from './useAtlasmapDialogs';
import { useSidebar } from './useSidebar';

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
  modalsContainerId = 'modals',
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
    onFieldPreviewChange,
    searchSources,
    searchTargets,
    importJarFile,
    // expression
    currentMappingExpression,
    executeFieldSearch,
    getFieldEnums,
    setSelectedEnumValue,
    mappingExpressionAddField,
    mappingExpressionClearText,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    mappingExpressionEnabled,
    isMappingExpressionEmpty,
    trailerId,
    canAddToSelectedMapping,
    isFieldAddableToSelection,
    isFieldDragAndDropAllowed,
    isFieldRemovableFromSelection,
    onAddToMapping,
    onRemoveFromMapping,
    onCreateMapping,
    isEnumerationMapping,
  } = useAtlasmap();

  const { handlers, dialogs } = useAtlasmapDialogs({
    modalContainer: document.getElementById(modalsContainerId)!,
  });
  const { activeView, showMappingPreview, showTypes, contextToolbar } =
    useContextToolbar({
      showImportAtlasFileToolbarItem: allowImport,
      showImportJarFileToolbarItem: allowImport,
      showExportAtlasFileToolbarItem: allowExport,
      showResetToolbarItem: allowReset,
      ...toolbarOptions,
      onImportADMArchiveFile: handlers.onImportADMArchive,
      onImportJarFile: (file) => importJarFile(file),
      onExportAtlasFile: handlers.onExportADMArchive,
      onResetAtlasmap: handlers.onResetAtlasmap,
      onAbout: handlers.onAbout,
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

  const expressionToolbar = (
    <ExpressionToolbar>
      <ConditionalExpressionInput
        mappingExpression={
          mappingExpressionEnabled ? currentMappingExpression : undefined
        }
        executeFieldSearch={executeFieldSearch}
        getFieldEnums={getFieldEnums}
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
        setSelectedEnumValue={setSelectedEnumValue}
      />
    </ExpressionToolbar>
  );

  const sourceEvents = useMemo<ISourceColumnCallbacks>(
    () => ({
      isSource: true,
      acceptDropType: 'target',
      draggableType: 'source',
      canDrop: (dt, i) => {
        return isFieldDragAndDropAllowed(i.payload as IAtlasmapField, dt);
      },
      onDrop: (s, t) => onCreateMapping(s, t?.payload as IAtlasmapField),
      onShowMappingDetails: selectMapping,
      onAddToSelectedMapping: onAddToMapping,
      canAddToSelectedMapping: canAddToSelectedMapping,
      canAddFieldToSelectedMapping: (f) =>
        isFieldAddableToSelection('source', f),
      onAddFieldToSelectedMapping: onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection('source', f),
      onRemoveFromSelectedMapping: onRemoveFromMapping,
      onCreateConstant: () => handlers.onCreateConstant(constants),
      onEditConstant: (constName, constValue) => {
        const name = constName;
        const value = constValue;
        const valueType = getConstantType(name);

        handlers.onEditConstant({ name, value, valueType }, constants);
      },
      onDeleteConstant: handlers.onDeleteConstant,
      onCreateProperty: (isSource: boolean) => {
        handlers.onCreateProperty(isSource, sourceProperties);
      },
      onEditProperty: (property, scope, isSource) => {
        const [leftPart] = property.split(' ');
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
      onCaptureDocumentID: (id) => handlers.onCaptureDocumentID(id),
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
      onEditCSVParams: (docId, isSource) => {
        handlers.onEditCSVParams(docId, isSource);
      },
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
      canAddToSelectedMapping,
      isFieldAddableToSelection,
      isFieldDragAndDropAllowed,
      isFieldRemovableFromSelection,
      sourceProperties,
    ],
  );

  const targetEvents = useMemo<ITargetsColumnCallbacks>(
    () => ({
      isSource: false,
      acceptDropType: 'source',
      draggableType: 'target',
      canDrop: (dt, i) => {
        return isFieldDragAndDropAllowed(i.payload as IAtlasmapField, dt);
      },
      onDrop: (s, t) => onCreateMapping(t?.payload as IAtlasmapField, s),
      canAddToSelectedMapping: canAddToSelectedMapping,
      canAddFieldToSelectedMapping: (f) =>
        isFieldAddableToSelection('target', f),
      onShowMappingDetails: selectMapping,
      onAddToSelectedMapping: onAddToMapping,
      canRemoveFromSelectedMapping: (f) =>
        isFieldRemovableFromSelection('target', f),
      onRemoveFromSelectedMapping: onRemoveFromMapping,
      onCreateProperty: (isSource: boolean) => {
        handlers.onCreateProperty(isSource, targetProperties);
      },
      onEditProperty: (property, scope, isSource) => {
        const [leftPart] = property.split(' ');
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
      onCaptureDocumentID: (id) => handlers.onCaptureDocumentID(id),
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
      onEditCSVParams: (docId, isSource) => {
        handlers.onEditCSVParams(docId, isSource);
      },
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
      canAddToSelectedMapping,
      isFieldAddableToSelection,
      isFieldDragAndDropAllowed,
      isFieldRemovableFromSelection,
      targetProperties,
    ],
  );

  const currentView = useMemo(() => {
    switch (activeView) {
      case 'ColumnMapper':
        return (
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
      case 'MappingTable':
        return (
          <MappingTableView
            mappings={mappings}
            onSelectMapping={selectMapping}
            shouldShowMappingPreview={shouldShowMappingPreview}
            onFieldPreviewChange={onFieldPreviewChange}
          />
        );
      case 'NamespaceTable':
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
    mappings,
    onFieldPreviewChange,
    sourceProperties,
    targetProperties,
    selectMapping,
    selectedMapping,
    shouldShowMappingPreview,
    showMappingPreview,
    showTypes,
    sourceEvents,
    sources,
    targetEvents,
    targets,
  ]);

  const renderSidebar = useSidebar({
    onCreateConstant: () => {
      handlers.onCreateConstant(constants, true);
    },
    onCreateProperty: (isSource: boolean) => {
      if (isSource) {
        handlers.onCreateProperty(isSource, sourceProperties, true);
      } else {
        handlers.onCreateProperty(isSource, targetProperties, true);
      }
    },
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
          expressionToolbar={
            activeView !== 'NamespaceTable' && expressionToolbar
          }
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
