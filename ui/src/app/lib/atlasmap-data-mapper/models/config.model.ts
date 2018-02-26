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

import { MappingDefinition } from './mapping-definition.model';
import { DocumentDefinition } from './document-definition.model';
import { Field } from '../models/field.model';

import { ErrorHandlerService } from '../services/error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { InitializationService } from '../services/initialization.service';
import { ErrorInfo } from '../models/error.model';

import { DocumentType, InspectionType } from '../common/config.types';

export class DataMapperInitializationModel {
  dataMapperVersion = '0.9.2017.07.28';
  initialized = false;
  loadingStatus = 'Loading.';
  initializationErrorOccurred = false;

  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;

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

  /* mock data configuration */
  discardNonMockSources = false;
  addMockJSONMappings = false;
  addMockJavaSingleSource = false;
  addMockJavaSources = false;
  addMockJavaCachedSource = false;
  addMockXMLInstanceSources = false;
  addMockXMLSchemaSources = false;
  addMockJSONSources = false;
  addMockJSONInstanceSources = false;
  addMockJSONSchemaSources = false;

  addMockJavaTarget = false;
  addMockJavaCachedTarget = false;
  addMockXMLInstanceTarget = false;
  addMockXMLSchemaTarget = false;
  addMockJSONTarget = false;
  addMockJSONInstanceTarget = false;
  addMockJSONSchemaTarget = false;

  /* debug logging toggles */
  debugDocumentServiceCalls = false;
  debugDocumentParsing = false;
  debugMappingServiceCalls = false;
  debugClassPathServiceCalls = false;
  debugValidationServiceCalls = false;
  debugFieldActionServiceCalls = false;

  mappingInitialized = false;
  fieldActionsInitialized = false;
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
}

export class ConfigModel {
  static mappingServicesPackagePrefix = 'io.atlasmap.v2';
  static javaServicesPackagePrefix = 'io.atlasmap.java.v2';

  initCfg: DataMapperInitializationModel = new DataMapperInitializationModel;

  /* current ui state config */
  showMappingDetailTray = false;
  showMappingTable = false;
  showNamespaceTable = false;
  showLinesAlways = true;
  showTypes = false;
  showMappedFields = true;
  showUnmappedFields = true;
  currentDraggedField: Field = null;

  documentService: DocumentManagementService;
  mappingService: MappingManagementService;
  errorService: ErrorHandlerService;
  initializationService: InitializationService;

  sourceDocs: DocumentDefinition[] = [];
  targetDocs: DocumentDefinition[] = [];
  propertyDoc: DocumentDefinition = new DocumentDefinition();
  constantDoc: DocumentDefinition = new DocumentDefinition();
  mappingFiles: string[] = [];

  mappings: MappingDefinition = null;

  errors: ErrorInfo[] = [];
  validationErrors: ErrorInfo[] = [];

  private static cfg: ConfigModel = new ConfigModel();

  constructor() {
    this.propertyDoc.type = DocumentType.PROPERTY;
    this.propertyDoc.name = 'Properties';
    this.propertyDoc.isSource = true;
    this.constantDoc.type = DocumentType.CONSTANT;
    this.constantDoc.name = 'Constants';
    this.constantDoc.isSource = true;
  }

  static getConfig(): ConfigModel {
    return ConfigModel.cfg;
  }

  static setConfig(cfg: ConfigModel): void {
    ConfigModel.cfg = cfg;
  }

  addDocument(docInitModel: DocumentInitializationModel): DocumentDefinition {
    const docDef: DocumentDefinition = new DocumentDefinition();
    docDef.id = docInitModel.id;
    docDef.type = docInitModel.type;
    docDef.name = docInitModel.name;
    docDef.description = docInitModel.description;
    docDef.isSource = docInitModel.isSource;
    docDef.showFields = docInitModel.showFields;
    docDef.inspectionType = docInitModel.inspectionType;
    docDef.inspectionSource = docInitModel.inspectionSource;
    docDef.inspectionResult = docInitModel.inspectionResult;
    docDef.uri = 'atlas:' + docDef.type.toLowerCase() + ':' + docDef.id;
    if (docDef.type == DocumentType.JAVA) {
      docDef.uri += '?className=' + docDef.inspectionSource;
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
    return isSource ? docs.concat([this.propertyDoc, this.constantDoc]) : docs;
  }

  hasJavaDocuments(): boolean {
    for (const doc of this.getAllDocs()) {
      if (doc.type == DocumentType.JAVA) {
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
      if (doc.type == DocumentType.JAVA && doc.inspectionResult == null) {
        return true;
      }
    }
    return false;
  }

  getDocForIdentifier(documentId: string, isSource: boolean): DocumentDefinition {
    for (const d of this.getDocs(isSource)) {
      if (d.id == documentId) {
        return d;
      }
    }
    return null;
  }

  getFirstXmlDoc(isSource: boolean) {
    const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
    for (const doc of docs) {
      if (doc.type == DocumentType.XML) {
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

  /**
   * Validate the specified field value, generating a validation error if not defined.
   * @param value - field to validate
   * @param fieldDescription - used in error diagnostic
   */
  isRequiredFieldValid(value: string, fieldDescription: string): boolean {

    if (value == null || '' == value) {
      const errorMessage: string = fieldDescription + ' is required.';
      this.errorService.validationError(errorMessage, null);
      return false;
    }
    return true;
  }

}
