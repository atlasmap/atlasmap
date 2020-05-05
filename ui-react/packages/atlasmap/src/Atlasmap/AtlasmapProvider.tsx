import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useReducer,
} from "react";
import { debounceTime } from "rxjs/operators";

import {
  MappingUtil,
  search,
  MappingSerializer,
  InspectionType,
  DocumentType,
  DocumentInitializationModel,
} from "@atlasmap/core";

import { IAtlasmapDocument, IAtlasmapField } from "../Views";
import {
  addToCurrentMapping,
  createConstant,
  createMapping,
  createProperty,
  deleteAtlasFile,
  deleteConstant,
  deleteProperty,
  deselectMapping,
  documentExists,
  editConstant,
  editProperty,
  executeFieldSearch,
  exportAtlasFile,
  fromDocumentDefinitionToFieldGroup,
  fromMappingDefinitionToIMappings,
  fromMappingModelToImapping,
  getMappingActions,
  getMappingExpression,
  getMultiplicityActionDelimiters,
  getMultiplicityActions,
  handleActionChange,
  handleIndexChange,
  handleNewTransformation,
  handleTransformationChange,
  handleTransformationArgumentChange,
  handleRemoveTransformation,
  handleMultiplicityChange,
  handleMultiplicityArgumentChange,
  importAtlasFile,
  initializationService,
  mappingExpressionAddField,
  mappingExpressionClearText,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  newMapping,
  onConditionalMappingExpressionEnabled,
  onFieldPreviewChange,
  onToggleExpressionMode,
  removeMapping,
  resetAtlasmap,
  selectMapping,
  toggleMappingPreview,
  toggleShowMappedFields,
  toggleShowUnmappedFields,
  trailerId,
  removeFromCurrentMapping,
  removeMappedFieldFromCurrentMapping,
  fromMappedFieldToIMappingField,
  errorLevelToVariant,
} from "./utils";
import {
  INotificationsState,
  IDataState,
  initDataState,
  initNotificationsState,
  dataReducer,
  notificationsReducer,
  DataActionPayload,
} from "./reducers";

// the document payload with get from Syndesis
export interface IExternalDocumentProps {
  id: string;
  name: string;
  description: string;
  documentType: DocumentType;
  inspectionType: InspectionType;
  inspectionSource: string;
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
    dispatchData({ type: "reset" });
    dispatchNotifications({
      type: "reset",
    });
  };

  const onLoading = () => {
    dispatchData({ type: "loading" });
  };

  const onUpdates = (payload: DataActionPayload) => {
    dispatchData({
      type: "update",
      payload,
    });
  };

  const markNotificationRead = (id: string) =>
    dispatchNotifications({ type: "dismiss", payload: { id } });

  useEffect(
    function onInitializationCb() {
      onReset();
      initializationService.resetConfig();

      const c = initializationService.cfg;
      c.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;
      c.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
      c.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
      c.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;

      if (externalDocument) {
        externalDocument.inputDocuments.forEach((d) => {
          const inputDoc: DocumentInitializationModel = new DocumentInitializationModel();
          inputDoc.type = d.documentType;
          inputDoc.inspectionType = d.inspectionType;
          inputDoc.inspectionSource = d.inspectionSource;
          inputDoc.inspectionResult = d.inspectionResult;
          inputDoc.id = d.id;
          inputDoc.name = d.name;
          inputDoc.description = d.description;
          inputDoc.isSource = true;
          inputDoc.showFields = d.showFields;
          c.addDocument(inputDoc);
        });

        const outputDoc: DocumentInitializationModel = new DocumentInitializationModel();
        outputDoc.type = externalDocument.outputDocument.documentType;
        outputDoc.inspectionType =
          externalDocument.outputDocument.inspectionType;
        outputDoc.inspectionSource =
          externalDocument.outputDocument.inspectionSource;
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

  const convertProperties = useCallback(function convertPropertiesCb() {
    return fromDocumentDefinitionToFieldGroup(
      initializationService.cfg.propertyDoc,
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

  const onSubUpdate = useCallback(
    function onSubUpdateCb(caller: string) {
      console.log(
        "onUpdates",
        caller,
        "initialized",
        initializationService.cfg.initCfg.initialized,
        "errors",
        initializationService.cfg.initCfg.initializationErrorOccurred,
      );
      onUpdates({
        pending: !initializationService.cfg.initCfg.initialized,
        error: initializationService.cfg.initCfg.initializationErrorOccurred,
        sources: convertSources(),
        constants: convertConstants(),
        properties: convertProperties(),
        targets: convertTargets(),
        mappings: convertMappings(),
        selectedMapping: convertSelectedMapping(),
      });
    },
    [
      convertConstants,
      convertMappings,
      convertProperties,
      convertSelectedMapping,
      convertSources,
      convertTargets,
    ],
  );

  useEffect(
    function subscriptionListener() {
      const debounceTimeWindow = data.pending ? 1000 : 50;
      const initializationObservable = initializationService.systemInitializedSource.pipe(
        debounceTime(debounceTimeWindow),
      );
      const lineRefreshObservable = initializationService.cfg.mappingService.lineRefreshSource.pipe(
        debounceTime(debounceTimeWindow),
      );
      const mappingUpdatedSource = initializationService.cfg.mappingService.mappingUpdatedSource.pipe(
        debounceTime(debounceTimeWindow),
      );

      const subscriptions = [
        initializationObservable.subscribe(() =>
          onSubUpdate("initializationObservable"),
        ),
        mappingUpdatedSource.subscribe(() =>
          onSubUpdate("mappingUpdatedSource"),
        ),
        initializationService.cfg.mappingService.mappingPreviewOutput$.subscribe(
          () => onSubUpdate("mappingPreviewOutput$"),
        ),
        lineRefreshObservable.subscribe(() =>
          onSubUpdate("lineRefreshObservable"),
        ),
        initializationService.cfg.errorService.subscribe(() => {
          dispatchNotifications({
            type: "update",
            payload: {
              notifications: initializationService.cfg.errorService
                .getErrors()
                .filter((e) => !e.mapping && e.level !== "DEBUG")
                .map((e) => ({
                  variant: errorLevelToVariant(e.level),
                  message: e.message,
                  id: e.identifier,
                })),
            },
          });
        }),
      ];

      return () => {
        subscriptions.forEach((s) => s.unsubscribe());
      };
    },
    [
      baseJavaInspectionServiceUrl,
      baseXMLInspectionServiceUrl,
      baseJSONInspectionServiceUrl,
      baseMappingServiceUrl,
      data.pending,
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
    (file: File, isSource: boolean) => {
      if (!isSource) {
        onLoading();
      }
      importAtlasFile(file, isSource);
    },
    [onLoading],
  );

  const handleResetAtlasmap = useCallback(() => {
    onReset();
    resetAtlasmap();
  }, [onReset]);

  const isMappingExpressionEmpty =
    initializationService.cfg.mappings?.activeMapping?.transition?.expression
      ?.nodes.length === 0;

  const isFieldAddableToSelection = useCallback(
    (documentType: "source" | "target", field: IAtlasmapField) => {
      const { selectedMapping } = context;
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
    [context],
  );

  const isFieldRemovableFromSelection = useCallback(
    (documentType: "source" | "target", field: IAtlasmapField) =>
      !!context.selectedMapping &&
      !isFieldAddableToSelection(documentType, field),
    [context.selectedMapping, isFieldAddableToSelection],
  );

  return {
    ...state,
    selectMapping,
    deselectMapping,
    deleteAtlasFile,
    exportAtlasFile,
    importAtlasFile: handleImportAtlasFile,
    resetAtlasmap: handleResetAtlasmap,
    mappingExpressionClearText,
    isMappingExpressionEmpty,
    executeFieldSearch,
    mappingExpressionAddField,
    mappingExpressionInit,
    mappingExpressionInsertText,
    mappingExpressionObservable,
    mappingExpressionRemoveField,
    onConditionalMappingExpressionEnabled,
    currentMappingExpression: MappingUtil.getMappingExpressionStr(
      true,
      initializationService.cfg.mappings?.activeMapping,
    ),
    getMappingExpression,
    onToggleExpressionMode,
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
    isFieldAddableToSelection,
    isFieldRemovableFromSelection,
    searchSources,
    searchTargets,
  };
}
