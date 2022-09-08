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
import { DocumentType, IStringContainer } from '../contracts/common';
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import { Observable, Subject } from 'rxjs';

import { ADMDigest } from '../contracts/adm-digest';
import { CommonUtil } from '../utils/common-util';
import { ConfigModel } from '../models/config.model';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { FieldActionService } from './field-action.service';
import { FileManagementService } from './file-management.service';
import { LookupTableUtil } from '../utils/lookup-table-util';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingExpressionService } from './mapping-expression.service';
import { MappingManagementService } from './mapping-management.service';
import { MappingPreviewService } from './mapping-preview.service';
import { MappingSerializer } from '../utils/mapping-serializer';
import { MappingUtil } from '../utils/mapping-util';
import { first } from 'rxjs/operators';
import ky from 'ky';
import log from 'loglevel';

log.setDefaultLevel(log.levels.WARN);

/**
 * Initialize AtlasMap UI core library. It initializes {@link ConfigModel} and core services.
 */
export class InitializationService {
  cfg: ConfigModel = ConfigModel.getConfig();

  systemInitializedSource = new Subject<void>();
  systemInitialized$: Observable<void> =
    this.systemInitializedSource.asObservable();

  initializationStatusChangedSource = new Subject<void>();
  initializationStatusChanged$: Observable<void> =
    this.initializationStatusChangedSource.asObservable();

  private documentService: DocumentManagementService;
  private mappingService: MappingManagementService;
  private errorService: ErrorHandlerService;
  private fieldActionService: FieldActionService;
  private fileService: FileManagementService;
  private previewService: MappingPreviewService;
  private expressionService: MappingExpressionService;

  constructor(private api: typeof ky) {
    this.documentService = new DocumentManagementService(this.api);
    this.mappingService = new MappingManagementService(this.api);
    this.errorService = new ErrorHandlerService();
    this.fieldActionService = new FieldActionService(this.api);
    this.fileService = new FileManagementService(this.api);
    this.previewService = new MappingPreviewService(this.api);
    this.expressionService = new MappingExpressionService();
    this.resetConfig();
    this.documentService.initialize();
  }

  resetConfig(): void {
    this.cfg = new ConfigModel();
    this.cfg.documentService = this.documentService;
    this.cfg.documentService.cfg = this.cfg;
    this.cfg.mappingService = this.mappingService;
    this.cfg.mappingService.cfg = this.cfg;
    this.cfg.errorService = this.errorService;
    this.cfg.fieldActionService = this.fieldActionService;
    this.cfg.fieldActionService.cfg = this.cfg;
    this.cfg.fileService = this.fileService;
    this.cfg.fileService.cfg = this.cfg;
    this.cfg.previewService = this.previewService;
    this.cfg.previewService.cfg = this.cfg;
    this.cfg.expressionService = this.expressionService;
    this.cfg.expressionService.cfg = this.cfg;
    this.cfg.initializationService = this;
    this.cfg.logger = log.getLogger('config');
    ConfigModel.setConfig(this.cfg);
  }

  initialize(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      this.cfg.setConstantPropertyDocs();
      this.cfg.errorService.resetAll();
      this.cfg.fieldActionService.isInitialized = false;
      this.cfg.initCfg.initialized = false;
      this.cfg.initCfg.mappingInitialized = false;

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Mapping service is not configured, validation service will not be used.',
            level: ErrorLevel.WARN,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
          })
        );
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Mapping service URL is not configured, validation service will not be used.',
            level: ErrorLevel.WARN,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
          })
        );
      }

      if (!this.cfg.fieldActionService) {
        this.handleError('FieldActionService is not configured');
        resolve(false);
        return;
      }

      // Verify the runtime service is out there.
      try {
        if (!(await this.runtimeServiceActive())) {
          this.handleError('The AtlasMap runtime service is not available.');
          resolve(false);
          return;
        }
      } catch (error) {
        this.handleError('The AtlasMap runtime service is not available.');
        resolve(false);
        return;
      }

      // load documents from initialization parameters in embedded mode
      this.updateLoadingStatus('Loading source/target documents.');
      const allDocs = this.cfg.getAllDocs();
      // assumption is that there will be at least one document present
      const lastDoc = allDocs[allDocs.length - 1];
      this.cfg.documentService
        .inspectDocuments()
        // inspectedDocuments notifies for all documents, wait till the last document
        .pipe(first((d) => d === lastDoc))
        .subscribe({
          next: () => {
            // updateStatus() will nullify this.cfg.preloadedMappingJson
            // let's store it for comparisson below
            const preloadedMappingJson = this.cfg.preloadedMappingJson;
            this.updateStatus();
            this.systemInitializedSource.pipe(first()).subscribe(() => {
              if (preloadedMappingJson) {
                const maybeUpdatedMappingJson = JSON.stringify(
                  MappingSerializer.serializeMappings(this.cfg)
                );
                // inspection might change the mapping, for example the preloaded
                // mapping could have a document URI that is no longer the same
                // after inspection, e.g. when parameters change on the provided
                // documents and differ from the parameters embedded in the
                // provided mapping; in that case we need to notify that mapping
                // has been changed
                if (preloadedMappingJson !== maybeUpdatedMappingJson) {
                  this.cfg.mappingService.notifyMappingUpdated();
                }
              }
            });
          },
        });

      this.initializeWithMappingDigest().finally(() => {
        this.updateStatus();
      });
      resolve(true);
    });
  }

  /**
   * Return true if the runtime service is available, false otherwise.
   */
  runtimeServiceActive(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      const url: string = this.cfg.initCfg.baseMappingServiceUrl + 'ping';
      this.cfg.logger!.debug('Runtime Service Ping Request');
      this.api
        .get(url)
        .json<IStringContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Runtime Service Ping Response: ${body.String}`
          );
          resolve(body?.String === 'pong');
        })
        .catch((error: any) => {
          reject(error);
        });
    });
  }

  /**
   * Retrieve AtlasMap design time backend runtime version.
   * @returns
   */
  getRuntimeVersion(): Promise<string> {
    const url = this.cfg.initCfg.baseMappingServiceUrl + 'version';
    return new Promise<string>((resolve, reject) => {
      this.api
        .get(url)
        .json<IStringContainer>()
        .then((body) => {
          this.cfg.logger!.debug(
            `Runtime Service Version Response: ${body.String}`
          );
          resolve(body.String);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  /**
   * Return the UI version as a string.
   *
   * @returns UI version
   */
  getUIVersion(): string {
    return this.cfg.initCfg.dataMapperVersion;
  }

  /**
   * Set the UI version.
   *
   * @param uiVersion - version to set
   */
  setUIVersion(uiVersion: string) {
    this.cfg.initCfg.dataMapperVersion = uiVersion;
  }

  /**
   * Initialize with the {@link ADMDigest} mapping digest from either an imported ADM archive
   * file or from the DM runtime digest file is presented to update the canvas.
   *
   * @param mappingDigest - {@link ADMDigest} mapping digest
   */
  private initializeWithMappingDigest(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.cfg.fileService
        .getCurrentMappingDigest()
        .then(async (mappingDigest: ADMDigest | null) => {
          // If digest is null then no compressed mappings digest file is available on the server.
          if (!mappingDigest) {
            if (this.cfg.mappings === null) {
              this.cfg.mappings = new MappingDefinition();
            }

            // load field actions - do this even with no documents so the default field actions are loaded.
            await this.cfg.fieldActionService.fetchFieldActions();
            this.updateStatus();
            resolve(true);
            return;
          }

          await this.addDocumentsFromMappingDigest(mappingDigest);

          if (!mappingDigest || !mappingDigest.exportMappings) {
            resolve(false);
            return;
          }
          // Reinitialize the model mappings.
          const digestMappingsName =
            MappingSerializer.deserializeAtlasMappingName(
              CommonUtil.objectize(mappingDigest.exportMappings.value)
            );

          // If the live UI mappings name does not match the UI mappings name extracted from the
          // catalog file then use the mappings from the catalog file. Otherwise use the live
          // UI file.
          this.cfg.fileService
            .findMappingFiles('UI')
            .then(async (files: string[]) => {
              await this.cfg.fileService.setMappingDigestToService(
                mappingDigest
              );
              await this.cfg.fieldActionService.fetchFieldActions();
              if (digestMappingsName !== files[0]) {
                await this.cfg.fileService.setMappingStringToService(
                  mappingDigest.exportMappings.value
                );
              }
              // load both default and custom field actions
              await this.cfg.fieldActionService.fetchFieldActions();

              // load mappings
              this.fetchMappings().then((value) => {
                resolve(value);
              });
            })
            .catch(() => {
              resolve(false);
            });
        })
        .catch(() => {
          resolve(false);
        });
    });
  }

  private addDocumentsFromMappingDigest(
    mappingDigest: ADMDigest
  ): Promise<boolean> {
    return new Promise<any>(async (resolve) => {
      this.cfg.errorService.resetAll();

      let fragIndex = 0;

      // Reinitialize the model documents.
      for (let metaFragment of mappingDigest.exportMeta) {
        const fragData = mappingDigest.exportBlockData[fragIndex].value;
        const docID = metaFragment.id ? metaFragment.id : metaFragment.name;
        const docType = metaFragment.dataSourceType
          ? (metaFragment.dataSourceType.toUpperCase() as DocumentType)
          : (metaFragment.documentType?.toUpperCase() as DocumentType);
        const isSource =
          typeof metaFragment.isSource === 'string'
            ? (metaFragment.isSource as string).toLowerCase() === 'true'
            : metaFragment.isSource;
        await this.cfg.documentService.addDocument(
          fragData,
          docID,
          metaFragment.name,
          docType,
          metaFragment.inspectionType,
          isSource,
          metaFragment.inspectionParameters
        );
        this.updateStatus();
        fragIndex++;
      }
      this.cfg.mappingService.notifyMappingUpdated();
      resolve(true);
    });
  }

  /**
   * Fetch mapping files and initialize user mappings in the canvas.
   *
   * @param mappingFiles
   */
  private fetchMappings(): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      if (this.cfg.mappings != null) {
        resolve(true);
        return;
      }

      this.cfg.mappings = new MappingDefinition();
      let mappingFiles = this.cfg.mappingFiles;
      if (!this.cfg.mappingFiles || this.cfg.mappingFiles.length === 0) {
        mappingFiles = await this.cfg.fileService.findMappingFiles('UI');
      }
      if (mappingFiles.length === 0) {
        resolve(false);
      }

      this.cfg.mappingService
        .fetchMappings(mappingFiles, this.cfg.mappings)
        .then(() => {
          this.cfg.initCfg.mappingInitialized = true;
          this.updateStatus();
          this.cfg.mappingService
            .notifyMappingUpdated()
            .then(() => resolve(true));
        });
    });
  }

  updateStatus(): void {
    const documentCount: number = this.cfg.getAllDocs().length;
    let finishedDocCount = 0;
    for (const docDef of this.cfg.getAllDocs()) {
      if (docDef.initialized || docDef.errorOccurred) {
        finishedDocCount++;
      }
    }

    if (
      documentCount === finishedDocCount &&
      this.cfg.fieldActionService.isInitialized
    ) {
      if (this.cfg.preloadedMappingJson) {
        MappingSerializer.deserializeMappingServiceJSON(
          JSON.parse(this.cfg.preloadedMappingJson),
          this.cfg
        );
        this.cfg.preloadedMappingJson = null;
      }
      if (this.cfg.mappings) {
        LookupTableUtil.updateLookupTables(this.cfg.mappings);
        MappingUtil.updateDocumentNamespacesFromMappings(this.cfg);
        MappingUtil.updateMappingsFromDocuments(this.cfg);
        for (const d of this.cfg.getAllDocs()) {
          d.updateFromMappings(this.cfg.mappings);
        }
        MappingUtil.removeStaleMappings(this.cfg);
        this.cfg.mappingService.updateMappingsTransition();
      }
      this.updateInitComplete();
    }
  }

  private handleError(message: string, error?: any) {
    message = 'Data Mapper UI Initialization Error: ' + message;
    this.cfg.errorService.addError(
      new ErrorInfo({
        message: message,
        scope: ErrorScope.APPLICATION,
        level: ErrorLevel.ERROR,
        type: ErrorType.INTERNAL,
        object: error,
      })
    );
    this.updateLoadingStatus(message);
    this.cfg.initCfg.initializationErrorOccurred = true;
    this.cfg.initCfg.initialized = true;
    this.systemInitializedSource.next();
  }

  updateInitComplete(): void {
    this.updateLoadingStatus('Initialization complete.');
    this.cfg.initCfg.initialized = true;
    this.systemInitializedSource.next();
  }

  updateLoadingStatus(status: string): void {
    this.cfg.initCfg.loadingStatus = status;
    this.initializationStatusChangedSource.next();
  }

  initializeWithADMArchiveFile(file: File): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.updateLoadingStatus('Importing ADM archive file');
      this.cfg.fileService.importADMArchive(file).then(() => {
        this.initialize().then((value) => {
          resolve(value);
        });
      });
    });
  }

  resetAtlasMap(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.cfg.fileService.resetAll().then((value) => {
        this.initialize().then((value2) => {
          resolve(value && value2);
        });
      });
    });
  }
}
