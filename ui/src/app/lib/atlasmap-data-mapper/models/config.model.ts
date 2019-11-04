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

import { EventEmitter } from '@angular/core';
import { NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { environment } from '../../../../environments/environment';
import { MappingDefinition } from './mapping-definition.model';
import { DocumentDefinition } from './document-definition.model';

import { ErrorHandlerService } from '../services/error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { InitializationService } from '../services/initialization.service';
import { ErrorInfo } from '../models/error.model';

import { DocumentType, InspectionType, CollectionType } from '../common/config.types';
import { FieldActionService } from '../services/field-action.service';
import { FileManagementService } from '../services/file-management.service';

export class DataMapperInitializationModel {
  dataMapperVersion = '0.9.2017.07.28';
  initialized = false;
  loadingStatus = 'Loading.';
  admHttpTimeout = 30000;  // 30 seconds
  initializationErrorOccurred = false;

  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;

  xsrfHeaderName: string;
  xsrfCookieName: string;
  xsrfDefaultTokenValue: string;

  /* class path fetching configuration */
  classPathFetchTimeoutInMilliseconds = 30000;
  // if classPath is specified, maven call to resolve pom will be skipped
  pomPayload: string;

  classPath: string;

  /* inspection service filtering flags */
  fieldNameBlacklist: string[] = [];
  classNameBlacklist: string[] = [];
  disablePrivateOnlyFields = false;
  disableProtectedOnlyFields = false;
  disablePublicOnlyFields = false;
  disablePublicGetterSetterFields = false;

  disableMappingPreviewMode = false;

  /* enable the navigation bar and import/export */
  disableNavbar = true;

  mappingInitialized = false;

  constructor() {
    if (environment.xsrf) {
      this.xsrfHeaderName = environment.xsrf.headerName;
      this.xsrfCookieName = environment.xsrf.cookieName;
      this.xsrfDefaultTokenValue = environment.xsrf.defaultTokenValue;
    }
  }

}

export class DocumentInitializationModel {
  id: string;
  type: DocumentType;
  name: string;
  description: string;
  isSource: boolean;
  showFields = true;
  inspectionType: InspectionType;
  inspectionSource: string;
  inspectionResult: string;
  selectedRoot: string;
  collectionType: CollectionType;
  collectionClassName: string;
}

export class ConfigModel {
  static mappingServicesPackagePrefix = 'io.atlasmap.v2';
  static javaServicesPackagePrefix = 'io.atlasmap.java.v2';
  static jsonServicesPackagePrefix = 'io.atlasmap.json.v2';
  static xmlServicesPackagePrefix = 'io.atlasmap.xml.v2';
  private static cfg: ConfigModel = new ConfigModel();

  initCfg: DataMapperInitializationModel = new DataMapperInitializationModel;

  /* current ui state config */
  showMappingDetailTray = false;
  showMappingTable = false;
  showNamespaceTable = false;
  showLinesAlways = true;
  showTypes = false;
  showMappedFields = true;
  showUnmappedFields = true;
  _showMappingPreview = false;
  currentDraggedField: any = null;

  documentService: DocumentManagementService;
  mappingService: MappingManagementService;
  errorService: ErrorHandlerService;
  initializationService: InitializationService;
  fieldActionService: FieldActionService;
  fileService: FileManagementService;

  sourceDocs: DocumentDefinition[] = [];
  targetDocs: DocumentDefinition[] = [];
  propertyDoc: DocumentDefinition = new DocumentDefinition();
  constantDoc: DocumentDefinition = new DocumentDefinition();
  mappingFiles: string[] = [];

  mappings: MappingDefinition = null;

  preloadedMappingJson: string;
  preloadedFieldActionMetadata: any;
  logger: NGXLogger;

  constructor() {
    this.propertyDoc.type = DocumentType.PROPERTY;
    this.propertyDoc.name = 'Properties';
    this.propertyDoc.isSource = true;
    this.propertyDoc.showFields = false;
    this.constantDoc.type = DocumentType.CONSTANT;
    this.constantDoc.name = 'Constants';
    this.constantDoc.isSource = true;
    this.constantDoc.showFields = false;
  }

  static getConfig(): ConfigModel {
    return ConfigModel.cfg;
  }

  static setConfig(cfg: ConfigModel): void {
    ConfigModel.cfg = cfg;
  }

  set showMappingPreview(show: boolean) {
    if (show && !this._showMappingPreview) {
      this.mappingService.enableMappingPreview();
    } else if (!show && this._showMappingPreview) {
      this.mappingService.disableMappingPreview();
    }
    this._showMappingPreview = show;
  }

  get showMappingPreview(): boolean {
    return this._showMappingPreview;
  }

  isDebugEnabled(): boolean {
    return [NgxLoggerLevel.DEBUG, NgxLoggerLevel.TRACE].includes(this.logger.getConfigSnapshot().level);
  }

  isTraceEnabled(): boolean {
    return this.logger.getConfigSnapshot().level === NgxLoggerLevel.TRACE;
  }

  addDocument(docInitModel: DocumentInitializationModel): DocumentDefinition {
    const docDef: DocumentDefinition = new DocumentDefinition();
    docDef.initModel = docInitModel;
    docDef.id = docInitModel.id;
    docDef.type = docInitModel.type;
    docDef.name = docInitModel.name;
    docDef.description = docInitModel.description;
    docDef.isSource = docInitModel.isSource;
    docDef.showFields = docInitModel.showFields;
    docDef.inspectionType = docInitModel.inspectionType;
    docDef.inspectionSource = docInitModel.inspectionSource;
    docDef.inspectionResult = docInitModel.inspectionResult;
    docDef.selectedRoot = docInitModel.selectedRoot;

    if (docDef.type === DocumentType.XSD) {
      docDef.uri = 'atlas:' + 'xml' + ':' + docDef.id;
    } else if (docDef.type === DocumentType.JAVA_ARCHIVE) {
      docDef.uri = 'atlas:' + 'java' + ':' + docDef.id;
    } else {
      docDef.uri = 'atlas:' + docDef.type.toLowerCase() + ':' + docDef.id;
    }

    if (docDef.type === DocumentType.JAVA || docDef.type === DocumentType.JAVA_ARCHIVE) {
      docDef.uri += '?className=' + docDef.inspectionSource;
      if (docInitModel.collectionType && docInitModel.collectionType !== CollectionType.NONE) {
        docDef.uri += '&collectionType=' + docInitModel.collectionType;
        if (docInitModel.collectionClassName) {
          docDef.uri += '&collectionClassName=' + docInitModel.collectionClassName;
        }
      }
    }

    if (docInitModel.isSource) {
      this.sourceDocs.push(docDef);
    } else {
      this.targetDocs.push(docDef);
    }
    return docDef;
  }

  addDocuments(docModels: DocumentInitializationModel[]): DocumentDefinition[] {
    const docDefs: DocumentDefinition[] = [];
    for (const docModel of docModels) {
      docDefs.push(this.addDocument(docModel));
    }
    return docDefs;
  }

  getDocsWithoutPropertyDoc(isSource: boolean): DocumentDefinition[] {
    return [].concat(isSource ? this.sourceDocs : this.targetDocs);
  }

  getDocs(isSource: boolean): DocumentDefinition[] {
    const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
    return isSource ? [this.propertyDoc, this.constantDoc].concat(docs) : docs;
  }

  /**
   * Return a uri:DocumentDefinition document map for either the sources or targets panel contents.
   *
   * @param cfg
   * @param isSource
   */
  getDocUriMap(cfg: ConfigModel, isSource: boolean): {[key: string]: DocumentDefinition} {
    const docMap: {[key: string]: DocumentDefinition} = {};
    for (const doc of cfg.getDocs(isSource)) {
      docMap[doc.uri] = doc;
    }
    return docMap;
  }

  /**
   * Clear source/target/mapping documents from the model.  Reset constant and property document definitions.
   */
  clearDocs(): void {
    this.sourceDocs = [];
    this.targetDocs = [];
    this.propertyDoc.clearFields();
    this.constantDoc.clearFields();
    this.mappingFiles = [];
  }

  hasJavaDocuments(): boolean {
    for (const doc of this.getAllDocs()) {
      if (doc.type === DocumentType.JAVA) {
        return true;
      }
    }
    return false;
  }

  isClassPathResolutionNeeded(): boolean {
    if (this.initCfg.classPath) {
      return false;
    }
    for (const doc of this.getAllDocs()) {
      if (doc.type === DocumentType.JAVA && doc.inspectionResult == null) {
        return true;
      }
    }
    return false;
  }

  getDocForIdentifier(documentId: string, isSource: boolean): DocumentDefinition {
    for (const d of this.getDocs(isSource)) {
      if (d.id === documentId) {
        return d;
      }
    }
    return null;
  }

  getFirstXmlDoc(isSource: boolean) {
    const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
    for (const doc of docs) {
      if (doc.type === DocumentType.XML) {
        return doc;
      }
    }
    return null;
  }

  getAllDocs(): DocumentDefinition[] {
    return [this.propertyDoc, this.constantDoc].concat(this.sourceDocs).concat(this.targetDocs);
  }

  documentsAreLoaded(): boolean {
    for (const doc of this.getAllDocs()) {
      if (!doc.initialized) {
        return false;
      }
    }
    return true;
  }

}
