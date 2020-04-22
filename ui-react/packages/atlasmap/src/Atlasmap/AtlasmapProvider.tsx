import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useReducer,
} from "react";
import { interval } from "rxjs";
import { debounce } from "rxjs/operators";

import { MappingUtil, search } from "@atlasmap/core";

import { IAtlasmapDocument, IAtlasmapMapping } from "../Views";
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
} from "./utils";

interface IAtlasmapContext extends State {
  selectMapping: typeof selectMapping;
  deselectMapping: typeof deselectMapping;
  deleteAtlasFile: typeof deleteAtlasFile;
  exportAtlasFile: typeof exportAtlasFile;
  importAtlasFile: typeof importAtlasFile;
  resetAtlasmap: typeof resetAtlasmap;
  mappingExpressionClearText: typeof mappingExpressionClearText;
  isMappingExpressionEmpty: boolean;
  executeFieldSearch: typeof executeFieldSearch;
  mappingExpressionAddField: typeof mappingExpressionAddField;
  mappingExpressionInit: typeof mappingExpressionInit;
  mappingExpressionInsertText: typeof mappingExpressionInsertText;
  mappingExpressionObservable: typeof mappingExpressionObservable;
  mappingExpressionRemoveField: typeof mappingExpressionRemoveField;
  onConditionalMappingExpressionEnabled: typeof onConditionalMappingExpressionEnabled;
  currentMappingExpression: string | undefined;
  getMappingExpression: typeof getMappingExpression;
  onToggleExpressionMode: typeof onToggleExpressionMode;
  toggleMappingPreview: typeof toggleMappingPreview;
  toggleShowMappedFields: typeof toggleShowMappedFields;
  toggleShowUnmappedFields: typeof toggleShowUnmappedFields;
  onFieldPreviewChange: typeof onFieldPreviewChange;
  addToCurrentMapping: typeof addToCurrentMapping;
  createMapping: typeof createMapping;
  newMapping: typeof newMapping;
  removeMapping: typeof removeMapping;
  documentExists: typeof documentExists;
  getMappingActions: typeof getMappingActions;
  getMultiplicityActions: typeof getMultiplicityActions;
  getMultiplicityActionDelimiters: typeof getMultiplicityActionDelimiters;
  handleActionChange: typeof handleActionChange;
  createConstant: typeof createConstant;
  deleteConstant: typeof deleteConstant;
  editConstant: typeof editConstant;
  createProperty: typeof createProperty;
  deleteProperty: typeof deleteProperty;
  editProperty: typeof editProperty;
  trailerId: typeof trailerId;
}

const AtlasmapContext = createContext<IAtlasmapContext | null>(null);

export interface IAtlasmapProviderProps {
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;
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
  children,
}) => {
  const [state, dispatch] = useReducer(reducer, {}, init);

  useEffect(() => {
    initializationService.cfg.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
    initializationService.cfg.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
    initializationService.cfg.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;
    initializationService.cfg.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;
    initializationService.initialize();
    dispatch({ type: "loading" });
  }, [
    baseJSONInspectionServiceUrl,
    baseJavaInspectionServiceUrl,
    baseMappingServiceUrl,
    baseXMLInspectionServiceUrl,
  ]);

  useEffect(() => {
    const onUpdates = (caller: string) => {
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
          sources: initializationService.cfg.sourceDocs
            .map(fromDocumentDefinitionToFieldGroup)
            .filter((d) => d) as IAtlasmapDocument[],
          constants: fromDocumentDefinitionToFieldGroup(
            initializationService.cfg.constantDoc,
          ),
          properties: fromDocumentDefinitionToFieldGroup(
            initializationService.cfg.propertyDoc,
          ),
          targets: initializationService.cfg.targetDocs
            .map(fromDocumentDefinitionToFieldGroup)
            .filter((d) => d) as IAtlasmapDocument[],
          mappings: fromMappingDefinitionToIMappings(
            initializationService.cfg.mappings,
          ),
          selectedMapping: fromMappingModelToImapping(
            initializationService.cfg.mappings?.activeMapping,
          ),
        },
      });
    };
    const initializationObservable = initializationService.systemInitializedSource.pipe(
      debounce(() => interval(500)),
    );
    const lineRefreshObservable = initializationService.cfg.mappingService.lineRefreshSource.pipe(
      debounce(() => interval(500)),
    );
    const mappingUpdatedSource = initializationService.cfg.mappingService.mappingUpdatedSource.pipe(
      debounce(() => interval(500)),
    );

    const subscriptions = [
      initializationObservable.subscribe(() =>
        onUpdates("initializationObservable"),
      ),
      mappingUpdatedSource.subscribe(() => onUpdates("mappingUpdatedSource")),
      initializationService.cfg.mappingService.mappingPreviewOutput$.subscribe(
        () => onUpdates("mappingPreviewOutput$"),
      ),
      lineRefreshObservable.subscribe(() => onUpdates("lineRefreshObservable")),
    ];

    return () => {
      initializationService.resetConfig();
      subscriptions.forEach((s) => s.unsubscribe());
    };
  }, [
    baseJavaInspectionServiceUrl,
    baseXMLInspectionServiceUrl,
    baseJSONInspectionServiceUrl,
    baseMappingServiceUrl,
  ]);

  const handleImportAtlasFile = useCallback((file: File, isSource: boolean) => {
    dispatch({ type: "loading" });
    importAtlasFile(file, isSource);
  }, []);

  const handleResetAtlasmap = useCallback(() => {
    dispatch({ type: "reset" });
    resetAtlasmap();
  }, []);

  const isMappingExpressionEmpty =
    initializationService.cfg.mappings?.activeMapping?.transition.expression
      ?.nodes.length === 0;

  return (
    <AtlasmapContext.Provider
      value={{
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
          initializationService.cfg.mappings?.activeMapping,
        ),
        getMappingExpression,
        onToggleExpressionMode,
        toggleMappingPreview,
        toggleShowMappedFields,
        toggleShowUnmappedFields,
        onFieldPreviewChange,
        addToCurrentMapping,
        createMapping,
        newMapping,
        removeMapping,
        documentExists,
        getMappingActions,
        getMultiplicityActions,
        getMultiplicityActionDelimiters,
        handleActionChange,
        createConstant,
        deleteConstant,
        editConstant,
        createProperty,
        deleteProperty,
        editProperty,
        trailerId,
      }}
    >
      {children}
    </AtlasmapContext.Provider>
  );
};

export interface IUseAtlasmapProps {
  sourcesSearchString?: string;
  targetsSearchString?: string;
}

export function useAtlasmap({
  sourcesSearchString,
  targetsSearchString,
}: IUseAtlasmapProps = {}) {
  const context = useContext(AtlasmapContext);

  useEffect(() => search(sourcesSearchString, true), [sourcesSearchString]);
  useEffect(() => search(targetsSearchString, false), [targetsSearchString]);

  if (!context) {
    throw new Error(
      `useAtlasmap must be used inside an AtlasmapProvider component`,
    );
  }

  return context;
}
