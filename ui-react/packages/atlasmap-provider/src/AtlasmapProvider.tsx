import { IAtlasmapDocument } from '@atlasmap/ui';
import { IAtlasmapField } from '@atlasmap/ui/src';
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
import { DocumentDefinition } from './models/document-definition.model';
import { MappingDefinition } from './models/mapping-definition.model';
import { DocumentManagementService } from './services/document-management.service';
import { ErrorHandlerService } from './services/error-handler.service';
import { FieldActionService } from './services/field-action.service';
import { FileManagementService } from './services/file-management.service';
import { InitializationService } from './services/initialization.service';
import { MappingManagementService } from './services/mapping-management.service';
import { search } from './utils/filter-fields';
import {
  fromDocumentDefinitionToFieldGroup,
  fromMappingDefinitionToIMappings,
  IAtlasmapFieldWithField,
} from './utils/to-ui-models-util';
import {
  addToMapping,
} from './components/field/field-util';
import {
  deleteAtlasFile,
  enableMappingPreview,
  exportAtlasFile,
  importAtlasFile,
  resetAtlasmap,
  documentExists,
} from './components/toolbar/toolbar-util';

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
            sourceDocs: [...initializationService.cfg.sourceDocs],
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

export interface IUseAtlasmapArgs {
  sourceFilter?: string;
  targetFilter?: string;
}

export function useAtlasmap({
  sourceFilter,
  targetFilter,
}: IUseAtlasmapArgs = {}) {
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
    sourceDocs,
    targetDocs,
    mappingDefinition,
    initializationService,
  } = context;

  search(sourceFilter, true);
  search(targetFilter, false);

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

  const sources = useMemo(
    () =>
      sourceDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter(d => d) as IAtlasmapDocument[],
    [sourceDocs]
  );

  const targets = useMemo(
    () =>
      targetDocs
        .map(fromDocumentDefinitionToFieldGroup)
        .filter(d => d) as IAtlasmapDocument[],
    [targetDocs]
  );

  const mappings = fromMappingDefinitionToIMappings(mappingDefinition);

  const onFieldPreviewChange = useCallback((
    field: IAtlasmapFieldWithField,
    value: string
  ) => {
    field.amField.value = value;
    initializationService.cfg.mappingService.notifyMappingUpdated();
  }, [initializationService]);

  const changeActiveMapping = useCallback((mappingId: string) => {
    const mapping = mappingDefinition.mappings.find(m => m.uuid === mappingId);
    if (mapping) {
      initializationService.cfg.mappingService.selectMapping(mapping);
    } else {
      initializationService.cfg.mappingService.deselectMapping();
    }
  }, [initializationService, mappingDefinition]);

  return useMemo(
    () => ({
      pending,
      error,
      sources,
      targets,
      mappings,
      deleteAtlasFile,
      exportAtlasFile,
      importAtlasFile: handleImportAtlasFile,
      resetAtlasmap: handleResetAtlasmap,
      changeActiveMapping,
      enableMappingPreview,
      onFieldPreviewChange,
      documentExists,
      addToMapping,
    }),
    [
      pending,
      error,
      sources,
      targets,
      mappings,
      handleImportAtlasFile,
      handleResetAtlasmap,
      changeActiveMapping,
      onFieldPreviewChange,
    ]
  );
}
