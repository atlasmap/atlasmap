import ky from 'ky';
import { useEffect, useMemo, useState } from 'react';
import { DocumentDefinition } from './models/document-definition.model';
import { MappingDefinition } from './models/mapping-definition.model';
import { DocumentManagementService } from './services/document-management.service';
import { ErrorHandlerService } from './services/error-handler.service';
import { FieldActionService } from './services/field-action.service';
import { FileManagementService } from './services/file-management.service';
import { InitializationService } from './services/initialization.service';
import { MappingManagementService } from './services/mapping-management.service';

const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });

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
  const [mappingDefinition, setMappingDefinition] = useState<MappingDefinition>(new MappingDefinition());

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
      setMappingDefinition(initializationService.cfg.mappings || new MappingDefinition());
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

  return { sourceDocs, targetDocs, mappingDefinition };
}
