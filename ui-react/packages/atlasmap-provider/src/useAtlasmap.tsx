import ky from 'ky';
import { useEffect, useMemo, useState } from 'react';
import { DocumentDefinition } from './models/document-definition.model';
import { DocumentManagementService } from './services/document-management.service';
import { ErrorHandlerService } from './services/error-handler.service';
import { FieldActionService } from './services/field-action.service';
import { FileManagementService } from './services/file-management.service';
import { InitializationService } from './services/initialization.service';
import { MappingManagementService } from './services/mapping-management.service';


export function importAtlasFile(selectedFile: File) {
  console.log('importAtlasFile: ' + selectedFile.name);

  const userFileComps = selectedFile.name.split('.');
  const userFileSuffix: string = userFileComps[userFileComps.length - 1].toUpperCase();

  if (userFileSuffix === 'ADM') {
        /*
    const error: any = null;
    new ErrorInfo({
      message: 'This is a test of the AtlasMap error service.',
      level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, 
      object: error});

      this.cfg.errorService.resetAll();

      // Clear out current user documents from the runtime service before processing the imported ADM.
      this.cfg.fileService.resetMappings().toPromise().then( async() => {
        this.cfg.fileService.resetLibs().toPromise().then( async() => {
          await this.processMappingsCatalog(selectedFile);
        });
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Fatal network error: Could not connect to AtlasMap design runtime service.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        } else {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Could not reset document definitions before import.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        }
      });
      */
  } else if (userFileSuffix === 'JAR') {
    // this.cfg.documentService.processDocument(selectedFile, InspectionType.JAVA_CLASS, false);
  }
}

export interface IUseAtlasmapArgs {
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;
}

export function useAtlasmap({
  baseJavaInspectionServiceUrl,
  baseXMLInspectionServiceUrl,
  baseJSONInspectionServiceUrl,
  baseMappingServiceUrl,
}: IUseAtlasmapArgs) {
  const [sourceDocs, setSourceDocs] = useState<DocumentDefinition[]>([]);
  const [targetDocs, setTargetDocs] = useState<DocumentDefinition[]>([]);

  const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });

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

  const c = initializationService.cfg;

  c.initCfg.baseJavaInspectionServiceUrl = baseJavaInspectionServiceUrl;
  c.initCfg.baseXMLInspectionServiceUrl = baseXMLInspectionServiceUrl;
  c.initCfg.baseJSONInspectionServiceUrl = baseJSONInspectionServiceUrl;
  c.initCfg.baseMappingServiceUrl = baseMappingServiceUrl;

  useEffect(() => {
    initializationService.systemInitialized$.subscribe(() => {
      setSourceDocs(initializationService.cfg.sourceDocs);
      setTargetDocs(initializationService.cfg.targetDocs);
    });

    initializationService.initialize();

    return () => {
      initializationService.resetConfig();
    };
  }, [
    initializationService,
    baseJavaInspectionServiceUrl,
    baseXMLInspectionServiceUrl,
    baseJSONInspectionServiceUrl,
    baseMappingServiceUrl,
  ]);

  return { sourceDocs, targetDocs, importAtlasFile };
}
