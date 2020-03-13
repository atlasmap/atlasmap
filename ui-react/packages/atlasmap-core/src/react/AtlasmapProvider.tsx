import { IAtlasmapDocument } from '@atlasmap/ui';
import ky from 'ky';
import React, {
  createContext,
  FunctionComponent,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
} from 'react';
import { timer } from 'rxjs';
import { debounce } from 'rxjs/operators';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { FieldActionService } from '../services/field-action.service';
import { FileManagementService } from '../services/file-management.service';
import { InitializationService } from '../services/initialization.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { search } from '../utils/filter-fields';
import {
  fromDocumentDefinitionToFieldGroup,
  fromMappingDefinitionToIMappings,
  IAtlasmapFieldWithField,
} from '../utils/to-ui-models-util';
import {
  addToCurrentMapping,
  createMapping,
  createConstant,
  deleteConstant,
  editConstant,
  createProperty,
  deleteProperty,
  editProperty,
} from '../components/field/field-util';
import { getExpressionStr } from '../components/expression/expression-util';
import {
  deleteAtlasFile,
  toggleMappingPreview,
  exportAtlasFile,
  importAtlasFile,
  resetAtlasmap,
  documentExists,
  toggleShowUnmappedFields,
  toggleShowMappedFields,
} from '../components/toolbar/toolbar-util';
import {
  FieldAction,
  FieldActionDefinition,
  Multiplicity,
} from '../models/field-action.model';
import { TransitionMode, TransitionModel } from '../models/transition.model';

const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });

interface IAtlasmapContext extends State {
  dispatch: (value: Action) => void;
  initializationService: InitializationService;
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
  sourceDocs: DocumentDefinition[];
  targetDocs: DocumentDefinition[];
  mappingDefinition: MappingDefinition;
}

interface Action {
  type: 'reset' | 'loading' | 'loaded' | 'error';
  payload?: ActionPayload;
}

interface ActionPayload {
  sourceDocs?: DocumentDefinition[];
  targetDocs?: DocumentDefinition[];
  mappingDefinition?: MappingDefinition;
}

const init = (): State => ({
  pending: false,
  error: false,
  sourceDocs: [] as DocumentDefinition[],
  targetDocs: [] as DocumentDefinition[],
  mappingDefinition: new MappingDefinition(),
});

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'reset':
      return {
        ...state,
        pending: false,
        error: false,
      };
    case 'loading':
      return {
        ...state,
        pending: true,
        error: false,
      };
    case 'loaded':
      return {
        ...state,
        ...action.payload,
        pending: false,
        error: false,
      };
    case 'error':
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

  const initializationService = useMemo(
    () =>
      new InitializationService(
        new DocumentManagementService(api),
        new MappingManagementService(api),
        new ErrorHandlerService(),
        new FieldActionService(api),
        new FileManagementService(api)
      ),
    []
  );

  initializationService.cfg.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
  initializationService.cfg.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
  initializationService.cfg.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;
  initializationService.cfg.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;

  const onUpdates = useCallback(() => {
    if (initializationService.cfg.initCfg.initialized) {
      if (!initializationService.cfg.initCfg.initializationErrorOccurred) {
        dispatch({
          type: 'loaded',
          payload: {
            sourceDocs: [
              ...initializationService.cfg.sourceDocs,
              initializationService.cfg.constantDoc,
              initializationService.cfg.propertyDoc,
            ],
            targetDocs: [...initializationService.cfg.targetDocs],
            mappingDefinition:
              initializationService.cfg.mappings || new MappingDefinition(),
          },
        });
      } else {
        dispatch({ type: 'error' });
      }
    }
  }, [initializationService]);

  useEffect(() => {
    initializationService.initialize();

    const initializationObservable = initializationService.systemInitializedSource.pipe(
      debounce(() => timer(500))
    );

    const subscriptions = [
      initializationObservable.subscribe(onUpdates),
      initializationService.systemInitializedSource.subscribe(onUpdates),
      initializationService.cfg.mappingService.mappingUpdatedSource.subscribe(
        onUpdates
      ),
      initializationService.cfg.mappingService.mappingPreviewOutput$.subscribe(
        onUpdates
      ),
    ];

    dispatch({ type: 'loading' });

    return () => {
      initializationService.resetConfig();
      subscriptions.forEach(s => s.unsubscribe());
    };
  }, [
    initializationService,
    onUpdates,
    baseJavaInspectionServiceUrl,
    baseXMLInspectionServiceUrl,
    baseJSONInspectionServiceUrl,
    baseMappingServiceUrl,
  ]);

  return (
    <AtlasmapContext.Provider
      value={{
        ...state,
        dispatch,
        initializationService,
      }}
    >
      {children}
    </AtlasmapContext.Provider>
  );
};

export function useAtlasmapSources(filter?: string) {
  const context = useContext(AtlasmapContext);
  if (!context) {
    throw new Error(
      `useAtlasmapSources must be used inside an AtlasmapProvider component`
    );
  }

  const { sourceDocs } = context;

  useEffect(() => search(filter, true), [filter]);

  return useMemo(
    () =>
      sourceDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter(d => d) as IAtlasmapDocument[],
    [sourceDocs]
  );
}

export function useAtlasmapTargets(filter?: string) {
  const context = useContext(AtlasmapContext);
  if (!context) {
    throw new Error(
      `useAtlasmapTargets must be used inside an AtlasmapProvider component`
    );
  }

  const { targetDocs } = context;

  useEffect(() => search(filter, false), [filter]);

  return useMemo(
    () =>
      targetDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter(d => d) as IAtlasmapDocument[],
    [targetDocs]
  );
}

export function useAtlasmap() {
  const context = useContext(AtlasmapContext);
  if (!context) {
    throw new Error(
      `useAtlasmap must be used inside an AtlasmapProvider component`
    );
  }

  const {
    dispatch,
    pending,
    error,
    mappingDefinition,
    initializationService,
  } = context;

  const handleImportAtlasFile = useCallback(
    (file: File, isSource: boolean) => {
      dispatch({ type: 'loading' });
      importAtlasFile(file, isSource);
    },
    [dispatch]
  );

  const handleResetAtlasmap = useCallback(() => {
    dispatch({ type: 'reset' });
    resetAtlasmap();
  }, [dispatch]);

  const mappings = fromMappingDefinitionToIMappings(mappingDefinition);

  const onFieldPreviewChange = useCallback(
    (field: IAtlasmapFieldWithField, value: string) => {
      field.amField.value = value;
      initializationService.cfg.mappingService.notifyMappingUpdated();
    },
    [initializationService]
  );

  const onConditionalMappingExpressionEnabled = useCallback(() => {
    return initializationService.cfg.mappingService.conditionalMappingExpressionEnabled();
  }, [initializationService]);

  const onGetMappingExpressionStr = useCallback(() => {
    return getExpressionStr();
  }, []);

  const onToggleExpressionMode = useCallback(() => {
    initializationService.cfg.mappingService.toggleExpressionMode();
  }, [initializationService]);

  const changeActiveMapping = useCallback(
    (mappingId: string) => {
      const mapping = mappingDefinition.mappings.find(
        m => m.uuid === mappingId
      );
      if (mapping) {
        initializationService.cfg.mappingService.selectMapping(mapping);
      } else {
        initializationService.cfg.mappingService.deselectMapping();
      }
    },
    [initializationService, mappingDefinition]
  );

  const getMappingActions = useCallback(
    (isSource: boolean) => {
      return initializationService.cfg.fieldActionService.getActionsAppliesToField(
        initializationService.cfg.mappings!.activeMapping!,
        isSource,
        Multiplicity.ONE_TO_ONE
      );
    },
    [initializationService]
  );

  const getMultiplicityActions = useCallback(
    (mapping: any) => {
      if (mapping.transition.mode === TransitionMode.ONE_TO_MANY) {
        return initializationService.cfg.fieldActionService.getActionsAppliesToField(
          mapping,
          true,
          Multiplicity.ONE_TO_MANY
        );
      } else if (mapping.transition.mode === TransitionMode.MANY_TO_ONE) {
        return initializationService.cfg.fieldActionService.getActionsAppliesToField(
          mapping,
          true,
          Multiplicity.MANY_TO_ONE
        );
      } else {
        return [];
      }
    },
    [initializationService]
  );

  const getMultiplicityActionDelimiters = useCallback(() => {
    return TransitionModel.delimiterModels;
  }, []);

  const handleActionChange = useCallback(
    (action: FieldAction, definition: FieldActionDefinition) => {
      action.argumentValues = []; // Invalidate the previously selected field action arguments.
      definition.populateFieldAction(action);

      // If the field action configuration predefines argument values then populate the fields with
      // default values.  Needed to support pull-down menus in action argument definitions.
      if (
        action.argumentValues.values &&
        action.argumentValues.length > 0 &&
        definition.arguments[0] &&
        definition.arguments[0].values &&
        definition.arguments[0].values.length > 0
      ) {
        for (let i = 0; i < action.argumentValues.length; i++) {
          action.argumentValues[i].value = definition.arguments[i].values![i];
        }
      }
      initializationService.cfg.mappingService.notifyMappingUpdated();
    },
    [initializationService]
  );

  return useMemo(
    () => ({
      pending,
      error,
      mappings,
      deleteAtlasFile,
      exportAtlasFile,
      importAtlasFile: handleImportAtlasFile,
      resetAtlasmap: handleResetAtlasmap,
      changeActiveMapping,
      onConditionalMappingExpressionEnabled,
      onGetMappingExpressionStr,
      onToggleExpressionMode,
      toggleMappingPreview,
      toggleShowMappedFields,
      toggleShowUnmappedFields,
      onFieldPreviewChange,
      addToCurrentMapping,
      createMapping,
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
    }),
    [
      pending,
      error,
      mappings,
      handleImportAtlasFile,
      handleResetAtlasmap,
      changeActiveMapping,
      onFieldPreviewChange,
      onConditionalMappingExpressionEnabled,
      onGetMappingExpressionStr,
      onToggleExpressionMode,
      getMappingActions,
      getMultiplicityActions,
      getMultiplicityActionDelimiters,
      handleActionChange,
    ]
  );
}
