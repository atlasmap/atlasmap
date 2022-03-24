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
import {
  CollectionType,
  DocumentType,
  InspectionType,
} from '../contracts/common';
import { DocumentDefaultName } from '../common/config.types';
import { DocumentDefinition } from './document-definition.model';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { FieldActionService } from '../services/field-action.service';
import { FileManagementService } from '../services/file-management.service';
import { InitializationService } from '../services/initialization.service';
import { Logger } from 'loglevel';
import { MappingDefinition } from './mapping-definition.model';
import { MappingExpressionService } from '../services/mapping-expression.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { MappingPreviewService } from '../services/mapping-preview.service';

export class DataMapperInitializationModel {
  dataMapperVersion = '';
  initialized = false;
  loadingStatus = 'Loading.';
  admHttpTimeout = 30000; // 30 seconds
  initializationErrorOccurred = false;

  baseJavaInspectionServiceUrl?: string;
  baseXMLInspectionServiceUrl?: string;
  baseJSONInspectionServiceUrl?: string;
  baseCSVInspectionServiceUrl?: string;
  baseMappingServiceUrl?: string;

  xsrfHeaderName?: string;
  xsrfCookieName?: string;
  xsrfDefaultTokenValue?: string;

  /* class path fetching configuration */
  classPathFetchTimeoutInMilliseconds = 30000;
  // if classPath is specified, maven call to resolve pom will be skipped
  pomPayload?: string;

  classPath?: string;

  /* inspection service filtering flags */
  fieldNameExclusions: string[] = [];
  classNameExclusions: string[] = [];
  disablePrivateOnlyFields = false;
  disableProtectedOnlyFields = false;
  disablePublicOnlyFields = false;
  disablePublicGetterSetterFields = false;

  disableMappingPreviewMode = false;

  /* enable the navigation bar and import/export */
  disableNavbar = true;

  mappingInitialized = false;
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
  inspectionParameters: { [key: string]: string };
  inspectionResult: string;
  selectedRoot: string;
  collectionType?: CollectionType;
  collectionClassName?: string;
}

/**
 * The central store of the AtlasMap UI core library. It is expected to be initialized through
 * {@link InitializationService}.
 */
export class ConfigModel {
  private static cfg: ConfigModel = new ConfigModel();

  initCfg: DataMapperInitializationModel = new DataMapperInitializationModel();

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
  previewService: MappingPreviewService;
  expressionService: MappingExpressionService;

  sourceDocs: DocumentDefinition[] = [];
  targetDocs: DocumentDefinition[] = [];
  sourcePropertyDoc: DocumentDefinition = new DocumentDefinition();
  targetPropertyDoc: DocumentDefinition = new DocumentDefinition();
  constantDoc: DocumentDefinition = new DocumentDefinition();
  mappingFiles: string[] = [];
  mappingDefinitionId = 0;
  mappings: MappingDefinition | null = null;

  preloadedMappingJson: string | null = null;
  preloadedFieldActionMetadata: any;
  logger?: Logger;

  constructor() {
    this.setConstantPropertyDocs();
  }

  static getConfig(): ConfigModel {
    return ConfigModel.cfg;
  }

  static setConfig(cfg: ConfigModel): void {
    ConfigModel.cfg = cfg;
  }

  initializePropertyDoc(propertyDoc: DocumentDefinition, isSource: boolean) {
    propertyDoc.clearFields();
    propertyDoc.type = DocumentType.PROPERTY;
    propertyDoc.name = DocumentDefaultName.PROPERTIES;
    propertyDoc.id =
      'DOC.' +
      propertyDoc.name +
      '.' +
      Math.floor(Math.random() * 1000000 + 1).toString();
    propertyDoc.isSource = isSource;
    propertyDoc.showFields = false;
    propertyDoc.isPropertyOrConstant = true;
  }

  setConstantPropertyDocs(): void {
    this.initializePropertyDoc(this.sourcePropertyDoc, true);
    this.initializePropertyDoc(this.targetPropertyDoc, false);
    this.constantDoc.clearFields();
    this.constantDoc.type = DocumentType.CONSTANT;
    this.constantDoc.name = DocumentDefaultName.CONSTANTS;
    this.constantDoc.id =
      'DOC.' +
      this.constantDoc.name +
      '.' +
      Math.floor(Math.random() * 1000000 + 1).toString();
    this.constantDoc.isSource = true;
    this.constantDoc.showFields = false;
    this.constantDoc.isPropertyOrConstant = true;
  }

  set showMappingPreview(show: boolean) {
    this._showMappingPreview = show;
  }

  get showMappingPreview(): boolean {
    return this._showMappingPreview;
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
    docDef.inspectionParameters = docInitModel.inspectionParameters;
    docDef.selectedRoot = docInitModel.selectedRoot;

    if (docDef.type === DocumentType.XSD) {
      docDef.uri = 'atlas:xml:' + docDef.id;
    } else if (docDef.type === DocumentType.JAVA) {
      docDef.uri = `atlas:java:${docDef.id}?className=${docDef.inspectionSource}`;
      if (
        docInitModel.collectionType &&
        docInitModel.collectionType !== CollectionType.NONE
      ) {
        docDef.uri += '&collectionType=' + docInitModel.collectionType;
        if (docInitModel.collectionClassName) {
          docDef.uri +=
            '&collectionClassName=' + docInitModel.collectionClassName;
        }
      }
    } else {
      docDef.uri = 'atlas:' + docDef.type.toLowerCase() + ':' + docDef.id;
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
    return isSource ? [...this.sourceDocs] : [...this.targetDocs];
  }

  getDocs(isSource: boolean): DocumentDefinition[] {
    const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
    return isSource
      ? [this.sourcePropertyDoc, this.constantDoc].concat(docs)
      : [this.targetPropertyDoc].concat(docs);
  }

  /**
   * Clear source/target/mapping documents from the model.  Reset constant and property document definitions.
   */
  clearDocs(): void {
    this.sourceDocs = [];
    this.targetDocs = [];
    this.sourcePropertyDoc.clearFields();
    this.targetPropertyDoc.clearFields();
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

  getDocForIdentifier(
    documentId: string,
    isSource: boolean
  ): DocumentDefinition | null {
    // TODO: check this non null operator
    return this.getDocs(isSource).find((d) => d.id === documentId)!;
  }

  getFirstXmlDoc(isSource: boolean): DocumentDefinition {
    const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
    // TODO: check this non null operator
    return docs.find((doc) => doc.type === DocumentType.XML)!;
  }

  getAllDocs(): DocumentDefinition[] {
    return [this.sourcePropertyDoc, this.constantDoc]
      .concat(this.sourceDocs)
      .concat(this.targetPropertyDoc)
      .concat(this.targetDocs);
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
