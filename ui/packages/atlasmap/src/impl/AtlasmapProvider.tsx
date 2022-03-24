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
import * as constants from '../atlasmap.json';
import {
  DataActionPayload,
  IDataState,
  INotificationsState,
  dataReducer,
  initDataState,
  initNotificationsState,
  notificationsReducer,
} from './reducers';
import {
  DocumentInitializationModel,
  DocumentType,
  InspectionType,
  MappingSerializer,
} from '@atlasmap/core';
import { IAtlasmapDocument, IAtlasmapField } from '../Views';
import React, {
  FunctionComponent,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useReducer,
} from 'react';
import {
  addToCurrentMapping,
  changeDocumentName,
  createConstant,
  createMapping,
  createNamespace,
  createProperty,
  deleteAtlasFile,
  deleteConstant,
  deleteNamespace,
  deleteProperty,
  deselectMapping,
  documentExists,
  editConstant,
  editNamespace,
  editProperty,
  enableCustomClass,
  errorInfoToNotification,
  executeFieldSearch,
  exportADMArchiveFile,
  fromDocumentDefinitionToFieldGroup,
  fromFieldToIFieldsNode,
  fromMappedFieldToIMappingField,
  fromMappingDefinitionToIMappings,
  fromMappingModelToImapping,
  getEnumerationValues,
  getFieldEnums,
  getMappingActions,
  getMappingExpression,
  getMultiplicityActionDelimiters,
  getMultiplicityActions,
  getRuntimeVersion,
  getUIVersion,
  handleActionChange,
  handleIndexChange,
  handleMultiplicityArgumentChange,
  handleMultiplicityChange,
  handleNewTransformation,
  handleRemoveTransformation,
  handleTransformationArgumentChange,
  handleTransformationChange,
  importADMArchiveFile,
  importInstanceSchema,
  importJarFile,
  initializationService,
  isEnumerationMapping,
  mappingExpressionAddField,
  mappingExpressionClearText,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  newMapping,
  onFieldPreviewChange,
  removeFromCurrentMapping,
  removeMappedFieldFromCurrentMapping,
  removeMapping,
  resetAtlasmap,
  selectMapping,
  setSelectedEnumValue,
  toggleExpressionMode,
  toggleMappingPreview,
  toggleShowMappedFields,
  toggleShowUnmappedFields,
  trailerId,
} from './utils';

import { LogLevelDesc } from 'loglevel';
import { debounceTime } from 'rxjs/operators';

// the document payload with get from Syndesis
export interface IExternalDocumentProps {
  id: string;
  name: string;
  description: string;
  documentType: DocumentType;
  inspectionType: InspectionType;
  inspectionSource: string;
  inspectionParameters: { [key: string]: string };
  inspectionResult: string;
  showFields: boolean;
}
interface IAtlasmapContext extends IDataState, INotificationsState {
  onLoading: () => void;
  onReset: () => void;
  markNotificationRead: (id: string) => void;
}
const AtlasmapContext = createContext<IAtlasmapContext | null>(null);

export interface IAtlasmapProviderProps {
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseCSVInspectionServiceUrl: string;
  baseMappingServiceUrl: string;
  logLevel: string;

  externalDocument?: {
    documentId: string;
    inputDocuments: IExternalDocumentProps[];
    outputDocument: IExternalDocumentProps;
    initialMappings?: string;
  };
  onMappingChange?: (serializedMappings: string) => void;
}
export const AtlasmapProvider: FunctionComponent<IAtlasmapProviderProps> = ({
  baseJavaInspectionServiceUrl,
  baseXMLInspectionServiceUrl,
  baseJSONInspectionServiceUrl,
  baseCSVInspectionServiceUrl,
  baseMappingServiceUrl,
  externalDocument,
  onMappingChange,
  logLevel,
  children,
}) => {
  const [data, dispatchData] = useReducer(dataReducer, {}, initDataState);
  const [notifications, dispatchNotifications] = useReducer(
    notificationsReducer,
    {},
    initNotificationsState,
  );

  const onReset = () => {
    dispatchData({ type: 'reset' });
    dispatchNotifications({
      type: 'reset',
    });
  };

  const onLoading = () => {
    dispatchData({ type: 'loading' });
  };

  const onUpdates = (payload: DataActionPayload) => {
    dispatchData({
      type: 'update',
      payload,
    });
  };

  const markNotificationRead = (id: string) =>
    dispatchNotifications({ type: 'dismiss', payload: { id } });

  useEffect(
    function onInitializationCb() {
      onReset();
      initializationService.resetConfig();
      const cfg = initializationService.cfg;
      cfg.logger?.setLevel(logLevel as LogLevelDesc);
      cfg.initCfg.dataMapperVersion = constants.version;

      cfg.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;
      cfg.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
      cfg.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
      cfg.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;
      cfg.initCfg.baseCSVInspectionServiceUrl = baseCSVInspectionServiceUrl;

      if (externalDocument) {
        externalDocument.inputDocuments.forEach((d) => {
          const inputDoc: DocumentInitializationModel =
            new DocumentInitializationModel();
          inputDoc.type = d.documentType;
          inputDoc.inspectionType = d.inspectionType;
          inputDoc.inspectionSource = d.inspectionSource;
          inputDoc.inspectionParameters = d.inspectionParameters;
          inputDoc.inspectionResult = d.inspectionResult;
          inputDoc.id = d.id;
          inputDoc.name = d.name;
          inputDoc.description = d.description;
          inputDoc.isSource = true;
          inputDoc.showFields = d.showFields;
          cfg.addDocument(inputDoc);
        });

        const outputDoc: DocumentInitializationModel =
          new DocumentInitializationModel();
        outputDoc.type = externalDocument.outputDocument.documentType;
        outputDoc.inspectionType =
          externalDocument.outputDocument.inspectionType;
        outputDoc.inspectionSource =
          externalDocument.outputDocument.inspectionSource;
        outputDoc.inspectionParameters =
          externalDocument.outputDocument.inspectionParameters;
        outputDoc.inspectionResult =
          externalDocument.outputDocument.inspectionResult;
        outputDoc.id = externalDocument.outputDocument.id;
        outputDoc.name = externalDocument.outputDocument.name;
        outputDoc.description = externalDocument.outputDocument.description;
        outputDoc.isSource = false;
        outputDoc.showFields = externalDocument.outputDocument.showFields;
        cfg.addDocument(outputDoc);

        if (externalDocument.initialMappings) {
          cfg.preloadedMappingJson = externalDocument.initialMappings;
        }
      }

      initializationService.initialize();

      onLoading();
    },
    [
      baseCSVInspectionServiceUrl,
      baseJSONInspectionServiceUrl,
      baseJavaInspectionServiceUrl,
      baseMappingServiceUrl,
      baseXMLInspectionServiceUrl,
      externalDocument,
      logLevel,
    ],
  );

  const configModel = initializationService.cfg;

  const convertSources = useCallback(
    function convertSourcesCb() {
      return configModel.sourceDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter((d: any) => d) as IAtlasmapDocument[];
    },
    [configModel],
  );

  const convertConstants = useCallback(
    function convertConstantsCb() {
      return fromDocumentDefinitionToFieldGroup(configModel.constantDoc);
    },
    [configModel],
  );

  const convertSourceProperties = useCallback(
    function convertPropertiesCb() {
      return fromDocumentDefinitionToFieldGroup(configModel.sourcePropertyDoc);
    },
    [configModel],
  );

  const convertTargetProperties = useCallback(
    function convertPropertiesCb() {
      return fromDocumentDefinitionToFieldGroup(configModel.targetPropertyDoc);
    },
    [configModel],
  );

  const convertTargets = useCallback(
    function convertTargetsCb() {
      return configModel.targetDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter((d) => d) as IAtlasmapDocument[];
    },
    [configModel],
  );

  const convertMappings = useCallback(
    function convertMappingsCb() {
      return fromMappingDefinitionToIMappings(configModel.mappings);
    },
    [configModel],
  );

  const convertSelectedMapping = useCallback(
    function convertSelectedMappingCb() {
      return fromMappingModelToImapping(configModel.mappings?.activeMapping);
    },
    [configModel],
  );

  const convertSourcesToFlatArray = useCallback(
    function convertSourcesToFlatArrayCb(): IAtlasmapField[] {
      return configModel.sourceDocs.flatMap((s) =>
        s.getAllFields().flatMap((f) => {
          const af = fromFieldToIFieldsNode(f);
          return af ? [af] : [];
        }),
      );
    },
    [configModel],
  );
  const convertTargetsToFlatArray = useCallback(
    function convertTargetsToFlatArrayCb() {
      return configModel.targetDocs.flatMap((t) =>
        t.getAllFields().flatMap((f) => {
          const af = fromFieldToIFieldsNode(f);
          return af ? [af] : [];
        }),
      );
    },
    [configModel],
  );

  const onSubUpdate = useCallback(
    function onSubUpdateCb(_caller: string) {
      onUpdates({
        pending: !configModel.initCfg.initialized,
        error: configModel.initCfg.initializationErrorOccurred,
        sources: convertSources(),
        constants: convertConstants(),
        sourceProperties: convertSourceProperties(),
        targets: convertTargets(),
        targetProperties: convertTargetProperties(),
        mappings: convertMappings(),
        selectedMapping: convertSelectedMapping(),
        flatSources: convertSourcesToFlatArray(),
        flatTargets: convertTargetsToFlatArray(),
      });
      dispatchNotifications({
        type: 'update',
        payload: {
          notifications: configModel.errorService
            .getErrors()
            .reverse()
            .filter((e) => e.level !== 'DEBUG')
            .map(errorInfoToNotification),
        },
      });
    },
    [
      configModel,
      convertConstants,
      convertMappings,
      convertSelectedMapping,
      convertSources,
      convertSourceProperties,
      convertSourcesToFlatArray,
      convertTargets,
      convertTargetProperties,
      convertTargetsToFlatArray,
    ],
  );

  useEffect(
    function subscriptionListener() {
      const debounceTimeWindow = data.pending ? 1000 : 50;
      const initializationObservable =
        initializationService.systemInitializedSource.pipe(
          debounceTime(debounceTimeWindow),
        );
      const lineRefreshObservable =
        configModel.mappingService.lineRefreshSource.pipe(
          debounceTime(debounceTimeWindow),
        );
      const mappingUpdatedSource =
        configModel.mappingService.mappingUpdatedSource.pipe(
          debounceTime(debounceTimeWindow),
        );
      const mappingPreview =
        configModel.previewService.mappingPreviewOutput$.pipe(
          debounceTime(debounceTimeWindow),
        );

      const subscriptions = [
        initializationObservable.subscribe(() =>
          onSubUpdate('initializationObservable'),
        ),
        mappingUpdatedSource.subscribe(() =>
          onSubUpdate('mappingUpdatedSource'),
        ),
        mappingPreview.subscribe(() => onSubUpdate('mappingPreviewOutput$')),
        lineRefreshObservable.subscribe(() =>
          onSubUpdate('lineRefreshObservable'),
        ),
        configModel.errorService.subscribe(() => onSubUpdate('errorService')),
      ];

      return () => {
        subscriptions.forEach((s) => s.unsubscribe());
      };
    },
    [
      baseJavaInspectionServiceUrl,
      baseXMLInspectionServiceUrl,
      baseJSONInspectionServiceUrl,
      baseCSVInspectionServiceUrl,
      baseMappingServiceUrl,
      configModel,
      data.pending,
      data.selectedMapping,
      onSubUpdate,
    ],
  );

  useEffect(
    function onMappingChangeListenerCb() {
      if (onMappingChange) {
        configModel.mappingService.mappingUpdatedSource.subscribe(
          function onMappingChangeListenerSubCb() {
            if (configModel.initCfg.initialized) {
              onMappingChange(
                JSON.stringify(
                  MappingSerializer.serializeMappings(configModel),
                ),
              );
            }
          },
        );
      }
    },
    [configModel, onMappingChange],
  );

  return (
    <AtlasmapContext.Provider
      value={{
        ...data,
        ...notifications,
        onLoading,
        onReset,
        markNotificationRead,
      }}
    >
      {children}
    </AtlasmapContext.Provider>
  );
};

export function useAtlasmap() {
  const context = useContext(AtlasmapContext);

  if (!context) {
    throw new Error(
      `useAtlasmap must be used inside an AtlasmapProvider component`,
    );
  }

  const configModel = initializationService.cfg;

  const { onLoading, onReset, ...state } = context;

  const searchSources = useCallback(
    (term: string) =>
      configModel.documentService.filterDocumentFields(term, true),
    [configModel],
  );
  const searchTargets = useCallback(
    (term: string) =>
      configModel.documentService.filterDocumentFields(term, false),
    [configModel],
  );

  const handleImportADMArchiveFile = useCallback(
    (file: File) => {
      importADMArchiveFile(file, configModel);
    },
    [configModel],
  );

  const handleImportJarFile = useCallback(
    (file: File) => {
      importJarFile(file, configModel);
    },
    [configModel],
  );

  const handleResetAtlasmap = useCallback(() => {
    onReset();
    resetAtlasmap();
  }, [onReset]);

  const onAddToMapping = useCallback((node: IAtlasmapField) => {
    const field = node.amField;
    addToCurrentMapping(field);
  }, []);

  const onRemoveFromMapping = useCallback((node: IAtlasmapField) => {
    const field = node.amField;
    removeFromCurrentMapping(field);
  }, []);

  const onCreateMapping = useCallback(
    (
      source: IAtlasmapField | undefined,
      target: IAtlasmapField | undefined,
    ) => {
      const sourceField = source?.amField;
      const targetField = target?.amField;
      createMapping(sourceField, targetField);
    },
    [],
  );

  const isMappingExpressionEmpty =
    configModel.mappings?.activeMapping?.transition?.expression?.nodes
      .length === 0;

  const mappingHasSourceCollection = useCallback(() => {
    return configModel.expressionService.willClearOutSourceFieldsOnTogglingExpression();
  }, [configModel]);

  /**
   * Return true if it's possible to add a source or target field to the current
   * mapping from the specified panel, false otherwise.
   */
  const canAddToSelectedMapping = useCallback(
    (isSource: boolean): boolean => {
      return configModel.mappingService.canAddToActiveMapping(isSource);
    },
    [configModel],
  );

  /**
   * Return true if it's possible to add the specified source field to the current mapping
   * from the specified panel, false otherwise.
   */
  const isFieldAddableToSelection = useCallback(
    (_documentType: 'source' | 'target', field: IAtlasmapField): boolean => {
      return configModel.mappingService.isFieldAddableToActiveMapping(
        field.amField,
      );
    },
    [configModel],
  );

  const isFieldDragAndDropAllowed = useCallback(
    (field: IAtlasmapField, dropTarget: IAtlasmapField): boolean => {
      return configModel.mappingService.isFieldDragAndDropAllowed(
        field.amField,
        dropTarget.amField,
      );
    },
    [configModel],
  );

  const isFieldRemovableFromSelection = useCallback(
    (documentType: 'source' | 'target', field: IAtlasmapField): boolean =>
      !!context.selectedMapping &&
      !!context.selectedMapping[
        documentType === 'source' ? 'sourceFields' : 'targetFields'
      ].find((f) => f.id === field.id),
    [context.selectedMapping],
  );

  return {
    ...state,
    selectMapping,
    deselectMapping,
    deleteAtlasFile,
    exportADMArchiveFile: exportADMArchiveFile,
    importADMArchiveFile: handleImportADMArchiveFile,
    importJarFile: handleImportJarFile,
    resetAtlasmap: handleResetAtlasmap,
    getUIVersion: getUIVersion,
    getRuntimeVersion: getRuntimeVersion,
    mappingExpressionClearText,
    isMappingExpressionEmpty,
    executeFieldSearch,
    getFieldEnums,
    setSelectedEnumValue,
    mappingExpressionAddField,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    mappingHasSourceCollection,
    mappingExpressionEnabled:
      configModel.expressionService.isExpressionEnabledForActiveMapping(),
    currentMappingExpression:
      configModel.expressionService.getMappingExpressionStr(
        true,
        configModel.mappings?.activeMapping,
      ),
    getMappingExpression,
    toggleExpressionMode,
    toggleMappingPreview,
    toggleShowMappedFields,
    toggleShowUnmappedFields,
    onFieldPreviewChange,
    addToCurrentMapping,
    removeFromCurrentMapping,
    removeMappedFieldFromCurrentMapping,
    fromMappedFieldToIMappingField,
    createMapping,
    newMapping,
    removeMapping,
    documentExists,
    getMappingActions,
    getMultiplicityActions,
    getMultiplicityActionDelimiters,
    handleActionChange,
    handleIndexChange,
    handleNewTransformation,
    handleRemoveTransformation,
    handleTransformationChange,
    handleTransformationArgumentChange,
    handleMultiplicityChange,
    handleMultiplicityArgumentChange,
    createConstant,
    deleteConstant,
    editConstant,
    createProperty,
    deleteProperty,
    editProperty,
    trailerId,
    canAddToSelectedMapping,
    isFieldAddableToSelection,
    isFieldDragAndDropAllowed,
    isFieldRemovableFromSelection,
    searchSources,
    searchTargets,
    importInstanceSchema,
    enableCustomClass,
    createNamespace,
    editNamespace,
    deleteNamespace,
    onAddToMapping,
    onRemoveFromMapping,
    onCreateMapping,
    changeDocumentName,
    getEnumerationValues,
    isEnumerationMapping,
    configModel,
  };
}
