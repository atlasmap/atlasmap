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
  MappingUtil,
  TransitionMode,
  search,
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
  exportAtlasFile,
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
  importAtlasFile,
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

      const c = initializationService.cfg;
      c.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;
      c.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
      c.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
      c.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;
      c.initCfg.baseCSVInspectionServiceUrl = baseCSVInspectionServiceUrl;

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
          c.addDocument(inputDoc);
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
        c.addDocument(outputDoc);

        if (externalDocument.initialMappings) {
          c.preloadedMappingJson = externalDocument.initialMappings;
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
    ],
  );

  const convertSources = useCallback(function convertSourcesCb() {
    return initializationService.cfg.sourceDocs
      .map(fromDocumentDefinitionToFieldGroup)
      .filter((d) => d) as IAtlasmapDocument[];
  }, []);

  const convertConstants = useCallback(function convertConstantsCb() {
    return fromDocumentDefinitionToFieldGroup(
      initializationService.cfg.constantDoc,
    );
  }, []);

  const convertSourceProperties = useCallback(function convertPropertiesCb() {
    return fromDocumentDefinitionToFieldGroup(
      initializationService.cfg.sourcePropertyDoc,
    );
  }, []);

  const convertTargetProperties = useCallback(function convertPropertiesCb() {
    return fromDocumentDefinitionToFieldGroup(
      initializationService.cfg.targetPropertyDoc,
    );
  }, []);

  const convertTargets = useCallback(function convertTargetsCb() {
    return initializationService.cfg.targetDocs
      .map(fromDocumentDefinitionToFieldGroup)
      .filter((d) => d) as IAtlasmapDocument[];
  }, []);

  const convertMappings = useCallback(function convertMappingsCb() {
    return fromMappingDefinitionToIMappings(initializationService.cfg.mappings);
  }, []);

  const convertSelectedMapping = useCallback(
    function convertSelectedMappingCb() {
      return fromMappingModelToImapping(
        initializationService.cfg.mappings?.activeMapping,
      );
    },
    [],
  );

  const convertSourcesToFlatArray = useCallback(
    function convertSourcesToFlatArrayCb(): IAtlasmapField[] {
      return initializationService.cfg.sourceDocs.flatMap((s) =>
        s.getAllFields().flatMap((f) => {
          const af = fromFieldToIFieldsNode(f);
          return af ? [af] : [];
        }),
      );
    },
    [],
  );
  const convertTargetsToFlatArray = useCallback(
    function convertTargetsToFlatArrayCb() {
      return initializationService.cfg.targetDocs.flatMap((t) =>
        t.getAllFields().flatMap((f) => {
          const af = fromFieldToIFieldsNode(f);
          return af ? [af] : [];
        }),
      );
    },
    [],
  );

  const onSubUpdate = useCallback(
    function onSubUpdateCb(caller: string) {
      console.log(
        'onUpdates',
        caller,
        'initialized',
        initializationService.cfg.initCfg.initialized,
        'errors',
        initializationService.cfg.initCfg.initializationErrorOccurred,
      );
      onUpdates({
        pending: !initializationService.cfg.initCfg.initialized,
        error: initializationService.cfg.initCfg.initializationErrorOccurred,
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
          notifications: initializationService.cfg.errorService
            .getErrors()
            .reverse()
            .filter((e) => e.level !== 'DEBUG')
            .map(errorInfoToNotification),
        },
      });
    },
    [
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
        initializationService.cfg.mappingService.lineRefreshSource.pipe(
          debounceTime(debounceTimeWindow),
        );
      const mappingUpdatedSource =
        initializationService.cfg.mappingService.mappingUpdatedSource.pipe(
          debounceTime(debounceTimeWindow),
        );
      const mappingPreview =
        initializationService.cfg.mappingService.mappingPreviewOutput$.pipe(
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
        initializationService.cfg.errorService.subscribe(() =>
          onSubUpdate('errorService'),
        ),
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
      data.pending,
      data.selectedMapping,
      onSubUpdate,
    ],
  );

  useEffect(
    function onMappingChangeListenerCb() {
      if (onMappingChange) {
        initializationService.cfg.mappingService.mappingUpdatedSource.subscribe(
          function onMappingChangeListenerSubCb() {
            if (initializationService.cfg.initCfg.initialized) {
              onMappingChange(
                JSON.stringify(
                  MappingSerializer.serializeMappings(
                    initializationService.cfg,
                  ),
                ),
              );
            }
          },
        );
      }
    },
    [onMappingChange],
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

  const { onLoading, onReset, ...state } = context;

  const searchSources = useCallback((term: string) => search(term, true), []);
  const searchTargets = useCallback((term: string) => search(term, false), []);

  const handleImportAtlasFile = useCallback(
    (
      file: File,
      isSource: boolean,
      isSchema: boolean,
      parameters?: { [key: string]: string },
    ) => {
      if (!isSource) {
        onLoading();
      }
      importAtlasFile(file, isSource, isSchema, parameters);
    },
    [onLoading],
  );

  const handleResetAtlasmap = useCallback(() => {
    onReset();
    resetAtlasmap();
  }, [onReset]);

  const onAddToMapping = useCallback((node: IAtlasmapField) => {
    const field = (node as IAtlasmapField).amField;
    addToCurrentMapping(field);
  }, []);

  const onRemoveFromMapping = useCallback((node: IAtlasmapField) => {
    const field = (node as IAtlasmapField).amField;
    removeFromCurrentMapping(field);
  }, []);

  const onCreateMapping = useCallback(
    (
      source: IAtlasmapField | undefined,
      target: IAtlasmapField | undefined,
    ) => {
      const sourceField = (source as IAtlasmapField | undefined)?.amField;
      const targetField = (target as IAtlasmapField | undefined)?.amField;
      createMapping(sourceField, targetField);
    },
    [],
  );

  const isMappingExpressionEmpty =
    initializationService.cfg.mappings?.activeMapping?.transition?.expression
      ?.nodes.length === 0;

  const mappingHasSourceCollection = useCallback(() => {
    return initializationService.cfg.mappingService.willClearOutSourceFieldsOnTogglingExpression();
  }, []);

  /**
   * Return true if it's possible to add a source or target field to the current
   * mapping from the specified panel, false otherwise.
   */
  const canAddToSelectedMapping = useCallback(
    (isSource: boolean): boolean => {
      const { selectedMapping } = context;
      if (
        !selectedMapping ||
        (selectedMapping.mapping.transition.mode === TransitionMode.ENUM &&
          selectedMapping.sourceFields.length > 0 &&
          selectedMapping.targetFields.length > 0)
      ) {
        return false;
      }
      if (
        selectedMapping.sourceFields.length <= 1 &&
        selectedMapping.targetFields.length <= 1
      ) {
        return true;
      } else if (
        isSource &&
        (selectedMapping.targetFields.length <= 1 ||
          selectedMapping.sourceFields.length === 0)
      ) {
        return true;
      } else if (
        !isSource &&
        (selectedMapping.sourceFields.length <= 1 ||
          selectedMapping.targetFields.length === 0)
      ) {
        return true;
      }
      return false;
    },
    [context],
  );

  /**
   * Return true if it's possible to add the specified source field to the current mapping
   * from the specified panel, false otherwise.
   */
  const isFieldAddableToSelection = useCallback(
    (
      documentType: 'source' | 'target',
      field: IAtlasmapField,
      dropTarget?: IAtlasmapField,
    ): boolean => {
      const { selectedMapping } = context;
      const isSource = documentType === 'source';
      if (
        !field ||
        !field.amField.isTerminal() ||
        dropTarget?.type === 'UNSUPPORTED' ||
        (selectedMapping &&
          selectedMapping.mapping.transition.mode === TransitionMode.ENUM &&
          selectedMapping.sourceFields.length > 0 &&
          selectedMapping.targetFields.length > 0)
      ) {
        return false;
      }
      if (!selectedMapping || (dropTarget && !dropTarget.isConnected)) {
        return true;
      }
      if (
        selectedMapping.sourceFields.length <= 1 &&
        selectedMapping.targetFields.length <= 1
      ) {
        if (
          isSource &&
          !selectedMapping.sourceFields.find((f) => f.id === field.id)
        ) {
          return true;
        } else if (
          field.isCollection ||
          field.isInCollection ||
          (!field.isConnected &&
            !selectedMapping.targetFields.find((f) => f.id === field.id))
        ) {
          return true;
        }
      } else if (
        isSource &&
        (selectedMapping.targetFields.length <= 1 ||
          selectedMapping.sourceFields.length === 0) &&
        !selectedMapping.sourceFields.find((f) => f.id === field.id)
      ) {
        return true;
      } else if (
        !isSource &&
        (field.isCollection ||
          field.isInCollection ||
          (!field.isConnected &&
            (selectedMapping.sourceFields.length <= 1 ||
              selectedMapping.targetFields.length === 0) &&
            !selectedMapping.targetFields.find((f) => f.id === field.id)))
      ) {
        return true;
      }
      return false;
    },
    [context],
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
    exportAtlasFile,
    importAtlasFile: handleImportAtlasFile,
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
      initializationService.cfg.mappingService.conditionalMappingExpressionEnabled(),
    currentMappingExpression: MappingUtil.getMappingExpressionStr(
      true,
      initializationService.cfg.mappings?.activeMapping,
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
    isFieldRemovableFromSelection,
    searchSources,
    searchTargets,
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
  };
}
