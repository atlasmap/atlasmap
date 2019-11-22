import ky from 'ky';
import { useCallback, useEffect, useMemo, useReducer } from 'react';
import { timer } from 'rxjs';
import { debounce } from 'rxjs/operators';
import { InspectionType } from './common/config.types';
import { ConfigModel } from './models/config.model';
import { DocumentDefinition } from './models/document-definition.model';
import {
  ErrorScope,
  ErrorType,
  ErrorInfo,
  ErrorLevel,
} from './models/error.model';
import { MappingDefinition } from './models/mapping-definition.model';
import { DocumentManagementService } from './services/document-management.service';
import { ErrorHandlerService } from './services/error-handler.service';
import { FieldActionService } from './services/field-action.service';
import { FileManagementService } from './services/file-management.service';
import { InitializationService } from './services/initialization.service';
import { MappingManagementService } from './services/mapping-management.service';
import {
  fromDocumentDefinitionToFieldGroup,
  fromMappingDefinitionToIMappings,
} from './utils/to-ui-models-util';

const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });

/**
 * A user has selected a compressed mappings catalog file to be imported into the canvas.
 *
 * @param selectedFile
 */
async function processMappingsCatalog(selectedFile: any, cfg: ConfigModel) {
  cfg.initializationService.updateLoadingStatus('Importing AtlasMap Catalog');
  await cfg.fileService.importADMCatalog(selectedFile);
}

/**
 * The user has imported a file (mapping catalog or Java archive).
 *
 * @param event
 */
export function importAtlasFile(selectedFile: File) {
  const cfg = ConfigModel.getConfig();
  const userFileComps = selectedFile.name.split('.');
  const userFileSuffix: string = userFileComps[
    userFileComps.length - 1
  ].toUpperCase();

  if (userFileSuffix === 'ADM') {
    cfg.errorService.resetAll();

    // Clear out current user documents from the backend service before processing the
    // imported ADM.
    cfg.fileService
      .resetMappings()
      .toPromise()
      .then(async () => {
        cfg.fileService
          .resetLibs()
          .toPromise()
          .then(async () => {
            await processMappingsCatalog(selectedFile, cfg);
          });
      })
      .catch((error: any) => {
        if (error.status === 0) {
          cfg.errorService.addError(
            new ErrorInfo({
              message:
                'Fatal network error: Could not connect to AtlasMap design runtime service.',
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
              object: error,
            })
          );
        } else {
          cfg.errorService.addError(
            new ErrorInfo({
              message: 'Could not reset document definitions before import.',
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
              object: error,
            })
          );
        }
      });
  } else if (userFileSuffix === 'JAR') {
    cfg.documentService.processDocument(
      selectedFile,
      InspectionType.JAVA_CLASS,
      false
    );
  }
}

export interface IUseAtlasmapArgs {
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
  type: 'loading' | 'loaded' | 'error';
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

export function useAtlasmap({
  baseJavaInspectionServiceUrl,
  baseXMLInspectionServiceUrl,
  baseJSONInspectionServiceUrl,
  baseMappingServiceUrl,
}: IUseAtlasmapArgs) {
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

  useEffect(() => {
    initializationService.initialize();

    const initializationObservable = initializationService.systemInitializedSource.pipe(
      debounce(() => timer(500))
    );

    const subscription = initializationObservable.subscribe(() => {
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
    });

    dispatch({ type: 'loading' });

    return () => {
      initializationService.resetConfig();
      subscription.unsubscribe();
    };
  }, [initializationService]);

  const handleImportAtlasFile = useCallback(
    (file: File) => {
      dispatch({ type: 'loading' });
      importAtlasFile(file);
    },
    [dispatch]
  );

  return useMemo(
    () => ({
      pending: state.pending,
      error: state.error,
      sources: state.sourceDocs.map(fromDocumentDefinitionToFieldGroup),
      targets: state.targetDocs.map(fromDocumentDefinitionToFieldGroup),
      mappings: fromMappingDefinitionToIMappings(state.mappingDefinition),
      importAtlasFile: handleImportAtlasFile,
    }),
    [state]
  );
}
