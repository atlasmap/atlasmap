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
import { Injectable } from '@angular/core';
import { inflate } from 'pako';
import { Subject, Observable } from 'rxjs';
import { NGXLogger } from 'ngx-logger';

import { DocumentType, InspectionType, CollectionType } from '../common/config.types';
import { DataMapperUtil } from '../common/data-mapper-util';
import { DocumentInitializationModel, ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingDefinition } from '../models/mapping-definition.model';

import { ErrorHandlerService } from './error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { FieldActionService } from './field-action.service';
import { FileManagementService } from './file-management.service';
import { LookupTableUtil } from '../utils/lookup-table-util';
import { MappingSerializer } from '../utils/mapping-serializer';
import { MappingUtil } from '../utils/mapping-util';
import { ErrorScope, ErrorType, ErrorInfo, ErrorLevel } from '../models/error.model';

@Injectable()
export class InitializationService {
  cfg: ConfigModel = ConfigModel.getConfig();

  systemInitializedSource = new Subject<void>();
  systemInitialized$: Observable<void> = this.systemInitializedSource.asObservable();

  initializationStatusChangedSource = new Subject<void>();
  initializationStatusChanged$: Observable<void> = this.initializationStatusChangedSource.asObservable();

  constructor(
    private documentService: DocumentManagementService,
    private mappingService: MappingManagementService,
    private errorService: ErrorHandlerService,
    private fieldActionService: FieldActionService,
    private fileService: FileManagementService,
    private logger: NGXLogger) {
    this.resetConfig();

    this.cfg.documentService.initialize();
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
    this.cfg.initializationService = this;
    this.cfg.logger = this.logger;
    ConfigModel.setConfig(this.cfg);
  }

  /**
   * Initialize a user-import schema or schema-instance document.
   *
   * @param docBody
   * @param docName
   * @param docType
   * @param inspectionType
   * @param isSource
   */
  async initializeUserDoc(docBody: any, docName: string, docType: DocumentType, inspectionType: InspectionType, isSource: boolean) {
    let docdef: DocumentDefinition = null;
    const javaArchive = (docType === DocumentType.JAVA_ARCHIVE);
    if (this.cfg.mappingService == null) {
      this.cfg.errorService.addError(new ErrorInfo({
        message: 'Mapping service is not configured, validation service will not be used.',
        level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
    } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      this.cfg.errorService.addError(new ErrorInfo({
        message: 'Mapping service URL is not configured, validation service will not be used.',
        level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
    }

    // Clear out the existing document if importing the same name.
    if (docdef = this.cfg.getDocForIdentifier(docName, isSource)) {
      if (isSource) {
        DataMapperUtil.removeItemFromArray(docdef, this.cfg.sourceDocs);
      } else {
        DataMapperUtil.removeItemFromArray(docdef, this.cfg.targetDocs);
      }
    }

    if (!javaArchive) {
      if (docType === DocumentType.JAVA) {
        docdef = this.addJavaDocument(docName, isSource);
      } else {
        docdef = this.addNonJavaDocument(docName, docType, inspectionType, docBody, isSource);
      }
      docdef.name = docName;
      docdef.updateFromMappings(this.cfg.mappings);
    }

    this.cfg.documentService.fetchClassPath().toPromise()
      .then((classPath: string) => {
        this.cfg.initCfg.classPath = classPath;

        // Push the user-defined java archive file to the runtime service.
        if (javaArchive) {
          this.cfg.documentService.setLibraryToService(docBody, async(success, res) => {
            if (success) {
              await this.cfg.fieldActionService.fetchFieldActions()
              .catch((error: any) => {
                this.handleError(error);
              });
            }
          });
        } else {
          this.logger.trace(`Fetching user document: name=${docdef.name}, id=${docdef.id},\
isSource=${docdef.initModel.isSource}, inspection=${docdef.initModel.inspectionType}`);
          this.cfg.documentService.fetchDocument(docdef, this.cfg.initCfg.classPath).toPromise()
          .then(async(doc: DocumentDefinition) => {

            if (doc.fields.length === 0) {
              if (isSource) {
                DataMapperUtil.removeItemFromArray(docdef, this.cfg.sourceDocs);
              } else {
                DataMapperUtil.removeItemFromArray(docdef, this.cfg.targetDocs);
              }
            }
            this.logger.trace(`Fetched user document: name=${docdef.name}, id=${docdef.id},\
isSource=${docdef.initModel.isSource}, inspection=${docdef.initModel.inspectionType}`);
            this.updateStatus();
          })
          .catch((error: any) => {
            if (error.status === 0) {
              this.handleError('Unable to fetch document ' + docName + ' from the runtime service.', error);
            } else {
              this.handleError('Could not load document \'' + docdef.id + '\': ' + error.status + ' ' + error.statusText, error);
            }
          });
        }
      })
      .catch((error: any) => {
        if (error.status === 0) {
          this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
        } else {
          this.handleError('Could not load Maven class path: ' + error.status + ' ' + error.statusText, error);
        }
      });
  }

  async initialize(): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      this.cfg.errorService.resetAll();
      this.cfg.fieldActionService.isInitialized = false;
      this.cfg.initCfg.initialized = false;
      this.cfg.initCfg.mappingInitialized = false;

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: 'Mapping service is not configured, validation service will not be used.',
          level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: 'Mapping service URL is not configured, validation service will not be used.',
          level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
      }

      if (!this.cfg.fieldActionService) {
        this.handleError('FieldActionService is not configured');
        reject();
        return;
      }

      // Verify the runtime service is out there.
      try {
        if (!await this.cfg.mappingService.runtimeServiceActive()) {
          this.handleError('The AtlasMap runtime service is not available.');
          reject();
          return;
        }
      } catch (error) {
        this.handleError('The AtlasMap runtime service is not available.');
        reject(error);
        return;
      }

      // load documents
      if (!this.cfg.isClassPathResolutionNeeded()) {
        this.fetchDocuments();
      } else {
        this.updateLoadingStatus('Loading Maven class path.');
        // fetch class path
        this.cfg.documentService.fetchClassPath().toPromise()
          .then((classPath: string) => {
            this.cfg.initCfg.classPath = classPath;
            this.fetchDocuments();
            this.updateStatus();
          })
          .catch((error: any) => {
            if (error.status === 0) {
              this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
            } else {
              this.handleError('Could not load Maven class path: ' + error.status + ' ' + error.statusText, error);
            }
            reject(error);
          });
      }

      // Fetch adm-catalog-files.gz if it exists.
      this.cfg.fileService.getCurrentMappingCatalog().subscribe( async(catalog: Uint8Array) => {

        // If catalog is null then no compressed mappings catalog is available on the server.
        if (catalog === null) {
          if (this.cfg.mappings === null) {
            this.cfg.mappings = new MappingDefinition(this.cfg.mappingId);
          }

          // load field actions - do this even with no documents so the default field actions are loaded.
          await this.cfg.fieldActionService.fetchFieldActions()
          .catch((error: any) => {
            this.handleError('Failure to load field actions on initialization.', error);
            reject(error);
            return;
          });
          this.updateStatus();
          resolve(true);
          return;
        }

        await this.processMappingsCatalogFiles(catalog);

        // load both default and custom field actions
        await this.cfg.fieldActionService.fetchFieldActions()
        .catch((error: any) => {
          this.handleError('Failure to load field actions on initialization.', error);
          reject(error);
          return;
        });

        // load mappings
        if (this.cfg.mappings == null) {
          this.cfg.mappings = new MappingDefinition(this.cfg.mappingId);
          if (this.cfg.mappingFiles.length > 0) {
            await this.fetchMappings(this.cfg.mappingFiles);
          } else {
            // filter according to mappingId
            // The postfix to differentiate btw UI.1 and UI.11
            var filter = 'UI.' + this.cfg.mappingId + MappingDefinition.MAPPING_NAME_POSTFIX;
            this.cfg.fileService.findMappingFiles(filter).toPromise()
              .then( async(files: string[]) => {
                // It's okay if no mapping files are found - resolve false so the caller will know.
                if (!await this.fetchMappings(files)) {
                  resolve(false);
                }
              },
              (error: any) => {
                if (error.status === 0) {
                  this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
                  reject(error);
                }
              }
            );
          }
        }
        this.updateStatus();
        resolve(true);
      });
      resolve(true);
    });
  }

  processMappingsDocuments(mappingsSchemaAggregate: string): any {
    let mInfo: any = null;
    try {
      mInfo = DocumentManagementService.getMappingsInfo(mappingsSchemaAggregate);
    } catch (error) {
      this.cfg.errorService.addError(new ErrorInfo({
        message: 'Unable to process mapping information from the data mappings file. ' + '\n' + error.message,
        level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
      return null;
    }

    this.cfg.errorService.resetAll();

    let metaFragment: any = null;
    let fragData = '';
    let fragIndex = 0;

    // Reinitialize the model documents.
    for (metaFragment of mInfo.exportMeta) {
      fragData = mInfo.exportBlockData[fragIndex].value;
      this.initializeUserDoc(fragData, metaFragment.name, metaFragment.documentType,
        metaFragment.inspectionType, (metaFragment.isSource === 'true'));
      fragIndex++;
    }
    this.cfg.mappingService.notifyMappingUpdated();
    return mInfo;
  }

  /**
   * Update .../target/mappings/atlasmapping-UI.nnnnnn.json in the runtime service.
   *
   * @param mInfo
   */
  async updateMappings(mInfo: any): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      try {
        this.cfg.fileService.setMappingToService(mInfo.exportMappings.value).toPromise()
          .then(async(result: boolean) => {
            resolve(true);
        }).catch((error: any) => {
          if (error.status === 0) {
            this.cfg.errorService.addError(new ErrorInfo({
              message: 'Fatal network error: Unable to connect to the AtlasMap design runtime service.',
              level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
          } else {
            this.cfg.errorService.addError(new ErrorInfo({
              message: `Unable to update the mappings file to the AtlasMap design runtime service. ${error.status} ${error.statusText}`,
              level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
          }
          reject(error);
        });
      } catch (error) {
        this.cfg.errorService.addError(new ErrorInfo({message: 'Unable to decompress the aggregate mappings catalog buffer.',
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        reject(error);
      }
    });
  }

  /**
   * Update the GZIP catalog file in the runtime service.
   *
   * @param compressedCatalog
   */
  async updateCatalog(compressedCatalog: Uint8Array): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      // Update .../target/mappings/adm-catalog-files.gz
      const fileContent: Blob = new Blob([compressedCatalog], {type: 'application/octet-stream'});
      this.cfg.fileService.setBinaryFileToService(fileContent, this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/0').toPromise()
        .then(async(result: boolean) => {
        resolve(true);
      }).catch((error: any) => {
        if (error.status === 0) {
          this.cfg.errorService.addError(new ErrorInfo({
            message: 'Fatal network error: Unable to connect to the AtlasMap design runtime service.',
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        } else {
          this.cfg.errorService.addError(new ErrorInfo({
            message: `Unable to update the catalog mappings file to the AtlasMap design runtime service. \
${error.status} ${error.statusText}`,
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
          resolve(false);
        }
      });
    });
  }

  /**
   * The compressed binary content (gzip) from either an imported ADM catalog file or from
   * the DM runtime catalog is presented to update the canvas.
   *
   * @param compressedCatalog - gzip binary buffer
   */
  async processMappingsCatalogFiles(compressedCatalog: Uint8Array): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      try {

        // Inflate the compressed content.
        const decompress = inflate(compressedCatalog);
        const mappingsDocuments =
          new Uint8Array(decompress).reduce((data, byte) => data + String.fromCharCode(byte), '');
        const mInfo = this.processMappingsDocuments(mappingsDocuments);

        // Reinitialize the model mappings.
        if (mInfo && mInfo.exportMappings) {
          const catalogMappingsName = MappingSerializer.deserializeAtlasMappingName(
            DocumentManagementService.getMappingsInfo(mInfo.exportMappings.value));

            // If the live UI mappings name does not match the UI mappings name extracted from the
            // catalog file then use the mappings from the catalog file.  Otherwise use the live
            // UI file.
            this.cfg.fileService.findMappingFiles('UI').toPromise()
              .then( async(files: string[]) => {

              await this.updateCatalog(compressedCatalog);
              await this.cfg.fieldActionService.fetchFieldActions()
              .catch((error: any) => {
                this.handleError('Failure to load field actions.', error);
              });
              if (catalogMappingsName !== files[0]) {
                await this.updateMappings(mInfo);
              }
              resolve(true);
            },
            (error: any) => {
              if (error.status === 0) {
                this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
              }
              reject(error);
            }
          );
        } else {
          resolve(true);
        }
      } catch (error) {
        this.cfg.errorService.addError(new ErrorInfo({message: 'Unable to decompress the aggregate mappings catalog buffer.',
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: error}));
        reject(error);
      }
    });
  }

  addJavaDocument(className: string, isSource: boolean,
    collectionType?: CollectionType, collectionClassName?: string): DocumentDefinition {
    const model: DocumentInitializationModel = new DocumentInitializationModel();
    model.id = className;
    model.type = DocumentType.JAVA;
    model.inspectionType = InspectionType.JAVA_CLASS;
    model.inspectionSource = className;
    model.isSource = isSource;
    model.collectionType = collectionType;
    model.collectionClassName = collectionClassName;
    return this.cfg.addDocument(model);
  }

  private addNonJavaDocument(
    name: string, documentType: DocumentType, inspectionType: InspectionType,
    inspectionSource: string, isSource: boolean): DocumentDefinition {
    const model: DocumentInitializationModel = new DocumentInitializationModel();
    model.id = name;
    model.type = documentType;
    model.inspectionType = inspectionType;
    model.inspectionSource = inspectionSource;
    model.isSource = isSource;
    return this.cfg.addDocument(model);
  }

  private fetchDocuments(): void {
    this.updateLoadingStatus('Loading source/target documents.');
    for (const docDef of this.cfg.getAllDocs()) {
      if (docDef === this.cfg.propertyDoc || docDef === this.cfg.constantDoc) {
        docDef.initialized = true;
        continue;
      }

      const docName: string = docDef.name;

      if (docDef.type === DocumentType.JAVA_ARCHIVE && this.cfg.initCfg.baseJavaInspectionServiceUrl == null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: `Java inspection service is not configured. Document will not be loaded: ${docName}`,
          level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: docDef}));
        docDef.initialized = true;
        this.updateStatus();
        continue;
      } else if (docDef.type === DocumentType.XML && this.cfg.initCfg.baseXMLInspectionServiceUrl == null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: `XML inspection service is not configured. Document will not be loaded: ${docName}`,
          level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: docDef}));
        docDef.initialized = true;
        this.updateStatus();
        continue;
      } else if (docDef.type === DocumentType.JSON && this.cfg.initCfg.baseJSONInspectionServiceUrl == null) {
        this.cfg.errorService.addError(new ErrorInfo({
          message: `JSON inspection service is not configured. Document will not be loaded: ${docName}`,
          level: ErrorLevel.WARN, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: docDef}));
        docDef.initialized = true;
        this.updateStatus();
        continue;
      }

      this.cfg.documentService.fetchDocument(docDef, this.cfg.initCfg.classPath).toPromise()
        .then((doc: DocumentDefinition) => {
          this.updateStatus();
        })
        .catch((error: any) => {
          if (error.status === 0) {
            this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
          } else {
            const errDetail = error.status ? error.status + ':' + error.statusText : error.toString();
            this.handleError(`Could not load Document \'${docDef.name}\'(${docDef.id}): ${errDetail}`, error);
          }
        });
    }
  }

  /**
   * Fetch mapping files and initialize user mappings in the canvas.
   *
   * @param mappingFiles
   */
  async fetchMappings(mappingFiles: string[]): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (mappingFiles == null || mappingFiles.length === 0) {
        resolve(false);
      }

      this.cfg.mappingService.fetchMappings(mappingFiles, this.cfg.mappings).toPromise()
        .then((result: boolean) => {
        this.cfg.initCfg.mappingInitialized = true;
        this.updateStatus();
        this.cfg.mappingService.notifyMappingUpdated().then(() => resolve(true));
      }).catch((error: any) => {
        if (error.status === 0) {
          this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
        } else {
          this.handleError('Could not load mapping definitions.', error);
        }
        reject(error);
      });
    });
  }

  private updateStatus(): void {
    const documentCount: number = this.cfg.getAllDocs().length;
    let finishedDocCount = 0;
    for (const docDef of this.cfg.getAllDocs()) {
      if (docDef.initialized || docDef.errorOccurred) {
        finishedDocCount++;
      }
    }

    if ((documentCount === finishedDocCount) && this.cfg.fieldActionService.isInitialized) {
      if (this.cfg.preloadedMappingJson) {
        MappingSerializer.deserializeMappingServiceJSON(JSON.parse(this.cfg.preloadedMappingJson), this.cfg);
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
      }
      this.updateLoadingStatus('Initialization complete.');
      this.cfg.initCfg.initialized = true;
      this.systemInitializedSource.next();
    }
  }

  private handleError(message: string, error?: any) {
    message = 'Data Mapper UI Initialization Error: ' + message;
    this.cfg.errorService.addError(new ErrorInfo({message: message, scope: ErrorScope.APPLICATION,
      level: ErrorLevel.ERROR, type: ErrorType.INTERNAL, object: error}));
    this.updateLoadingStatus(message);
    this.cfg.initCfg.initializationErrorOccurred = true;
    this.cfg.initCfg.initialized = true;
    this.systemInitializedSource.next();
  }

  updateLoadingStatus(status: string): void {
    this.cfg.initCfg.loadingStatus = status;
    this.initializationStatusChangedSource.next();
  }

}
