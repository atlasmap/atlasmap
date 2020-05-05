import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useReducer,
  Dispatch,
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

import { IAtlasmapDocument, IAtlasmapMapping, IAtlasmapField } from "../Views";
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
} from "./utils";

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
interface IAtlasmapContext extends State {
  dispatch: Dispatch<Action>;
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

interface State {
  pending: boolean;
  error: boolean;
  sources: IAtlasmapDocument[];
  targets: IAtlasmapDocument[];
  properties: IAtlasmapDocument | null;
  constants: IAtlasmapDocument | null;
  mappings: IAtlasmapMapping[];
  selectedMapping: IAtlasmapMapping | null;
}

interface Action {
  type: "reset" | "loading" | "update" | "error";
  payload?: ActionPayload;
}

interface ActionPayload {
  pending?: boolean;
  error?: boolean;
  sources?: IAtlasmapDocument[];
  targets?: IAtlasmapDocument[];
  properties?: IAtlasmapDocument | null;
  constants?: IAtlasmapDocument | null;
  mappings?: IAtlasmapMapping[];
  selectedMapping?: IAtlasmapMapping | null;
  sourcesFilter?: string;
  targetsFilter?: string;
}

const init = (): State => ({
  pending: false,
  error: false,
  properties: null,
  constants: null,
  sources: [],
  targets: [],
  mappings: [],
  selectedMapping: null,
});

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case "reset":
      return {
        ...state,
        pending: false,
        error: false,
      };
    case "loading":
      return {
        ...state,
        pending: true,
        error: false,
      };
    case "update":
      return {
        ...state,
        ...action.payload,
      };
    case "error":
      return init();
    default:
      throw new Error();
  }
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
  const [state, dispatch] = useReducer(reducer, {}, init);

  useEffect(
    function onInitializationCb() {
      dispatch({ type: "reset" });
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

      dispatch({ type: "loading" });
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

  const onUpdates = useCallback(
    function onUpdatesCb(caller: string) {
      console.log(
        "onUpdates",
        caller,
        "initialized",
        initializationService.cfg.initCfg.initialized,
        "errors",
        initializationService.cfg.initCfg.initializationErrorOccurred,
      );
      dispatch({
        type: "update",
        payload: {
          pending: !initializationService.cfg.initCfg.initialized,
          error: initializationService.cfg.initCfg.initializationErrorOccurred,
          sources: convertSources(),
          constants: convertConstants(),
          properties: convertProperties(),
          targets: convertTargets(),
          mappings: convertMappings(),
          selectedMapping: convertSelectedMapping(),
        },
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
      const debounceTimeWindow = state.pending ? 1000 : 50;
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
          onUpdates("initializationObservable"),
        ),
        mappingUpdatedSource.subscribe(() => onUpdates("mappingUpdatedSource")),
        initializationService.cfg.mappingService.mappingPreviewOutput$.subscribe(
          () => onUpdates("mappingPreviewOutput$"),
        ),
        lineRefreshObservable.subscribe(() =>
          onUpdates("lineRefreshObservable"),
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
      baseMappingServiceUrl,
      state.pending,
      onUpdates,
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
    <AtlasmapContext.Provider value={{ ...state, dispatch }}>
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

  const { dispatch, ...state } = context;

  const searchSources = useCallback((term: string) => search(term, true), []);
  const searchTargets = useCallback((term: string) => search(term, false), []);

  const handleImportAtlasFile = useCallback(
    (file: File, isSource: boolean) => {
      dispatch({ type: "loading" });
      importAtlasFile(file, isSource);
    },
    [dispatch],
  );

  const handleResetAtlasmap = useCallback(() => {
    dispatch({ type: "reset" });
    resetAtlasmap();
  }, [dispatch]);

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
