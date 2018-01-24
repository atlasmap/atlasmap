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

import { MappingDefinition } from './mapping.definition.model';
import { DocumentDefinition } from './document.definition.model';
import { Field } from '../models/field.model';

import { ErrorHandlerService } from '../services/error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { InitializationService } from '../services/initialization.service';
import { ErrorInfo } from '../models/error.model';

export const enum DocumentType {
    JAVA = 'Java',
    XML = 'XML',
    JSON = 'JSON',
    CORE = 'Core',
    CSV = 'CSV',
    CONSTANT = 'Constants',
    PROPERTY = 'Property'
}

export const enum InspectionType {
    JAVA_CLASS = 'JAVA_CLASS',
    SCHEMA = 'SCHEMA',
    INSTANCE = 'INSTANCE'
}

export class DataMapperInitializationModel {
    public dataMapperVersion = '0.9.2017.07.28';
    public initialized = false;
    public loadingStatus = 'Loading.';
    public initializationErrorOccurred = false;

    public baseJavaInspectionServiceUrl: string;
    public baseXMLInspectionServiceUrl: string;
    public baseJSONInspectionServiceUrl: string;
    public baseMappingServiceUrl: string;

    /* class path fetching configuration */
    public classPathFetchTimeoutInMilliseconds = 30000;
    // if classPath is specified, maven call to resolve pom will be skipped
    public pomPayload: string;

    public classPath: string;

    /* inspection service filtering flags */
    public fieldNameBlacklist: string[] = [];
    public classNameBlacklist: string[] = [];
    public disablePrivateOnlyFields = false;
    public disableProtectedOnlyFields = false;
    public disablePublicOnlyFields = false;
    public disablePublicGetterSetterFields = false;

    /* mock data configuration */
    public discardNonMockSources = false;
    public addMockJSONMappings = false;
    public addMockJavaSingleSource = false;
    public addMockJavaSources = false;
    public addMockJavaCachedSource = false;
    public addMockXMLInstanceSources = false;
    public addMockXMLSchemaSources = false;
    public addMockJSONSources = false;
    public addMockJSONInstanceSources = false;
    public addMockJSONSchemaSources = false;

    public addMockJavaTarget = false;
    public addMockJavaCachedTarget = false;
    public addMockXMLInstanceTarget = false;
    public addMockXMLSchemaTarget = false;
    public addMockJSONTarget = false;
    public addMockJSONInstanceTarget = false;
    public addMockJSONSchemaTarget = false;

    /* debug logging toggles */
    public debugDocumentServiceCalls = false;
    public debugDocumentParsing = false;
    public debugMappingServiceCalls = false;
    public debugClassPathServiceCalls = false;
    public debugValidationServiceCalls = false;
    public debugFieldActionServiceCalls = false;

    public mappingInitialized = false;
    public fieldActionsInitialized = false;
}

export class DocumentInitializationModel {
    public id: string;
    public type: DocumentType;
    public shortName: string;
    public fullName: string;
    public isSource: boolean;
    public inspectionType: InspectionType;
    public inspectionSource: string;
    public inspectionResult: string;
}

export class ConfigModel {
    public static mappingServicesPackagePrefix = 'io.atlasmap.v2';
    public static javaServicesPackagePrefix = 'io.atlasmap.java.v2';

    public initCfg: DataMapperInitializationModel = new DataMapperInitializationModel;

    /* current ui state config */
    public showMappingDetailTray = false;
    public showMappingTable = false;
    public showNamespaceTable = false;
    public showLinesAlways = true;
    public showTypes = false;
    public showMappedFields = true;
    public showUnmappedFields = true;
    public currentDraggedField: Field = null;

    public documentService: DocumentManagementService;
    public mappingService: MappingManagementService;
    public errorService: ErrorHandlerService;
    public initializationService: InitializationService;

    public sourceDocs: DocumentDefinition[] = [];
    public targetDocs: DocumentDefinition[] = [];
    public propertyDoc: DocumentDefinition = new DocumentDefinition();
    public constantDoc: DocumentDefinition = new DocumentDefinition();
    public mappingFiles: string[] = [];

    public mappings: MappingDefinition = null;

    public errors: ErrorInfo[] = [];
    public validationErrors: ErrorInfo[] = [];

    private static cfg: ConfigModel = new ConfigModel();

    constructor() {
        this.propertyDoc.type = DocumentType.PROPERTY;
        this.propertyDoc.shortName = 'Properties';
        this.propertyDoc.isSource = true;
        this.constantDoc.type = DocumentType.CONSTANT;
        this.constantDoc.shortName = 'Constants';
        this.constantDoc.isSource = true;
    }

    public static getConfig(): ConfigModel {
        return ConfigModel.cfg;
    }

    public static setConfig(cfg: ConfigModel): void {
        ConfigModel.cfg = cfg;
    }

    public addDocument(docInitModel: DocumentInitializationModel): DocumentDefinition {
        const docDef: DocumentDefinition = new DocumentDefinition();
        docDef.id = docInitModel.id;
        docDef.type = docInitModel.type;
        docDef.shortName = docInitModel.shortName;
        docDef.fullName = docInitModel.fullName;
        docDef.isSource = docInitModel.isSource;
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

    public addDocuments(docModels: DocumentInitializationModel[]): DocumentDefinition[] {
        const docDefs: DocumentDefinition[] = [];
        for (const docModel of docModels) {
            docDefs.push(this.addDocument(docModel));
        }
        return docDefs;
    }

    public getDocsWithoutPropertyDoc(isSource: boolean): DocumentDefinition[] {
        return [].concat(isSource ? this.sourceDocs : this.targetDocs);
    }

    public getDocs(isSource: boolean): DocumentDefinition[] {
        const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
        return isSource ? docs.concat([this.propertyDoc, this.constantDoc]) : docs;
    }

    public hasJavaDocuments(): boolean {
        for (const doc of this.getAllDocs()) {
            if (doc.type == DocumentType.JAVA) {
                return true;
            }
        }
        return false;
    }

    public isClassPathResolutionNeeded(): boolean {
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

    public getDocForIdentifier(documentId: string, isSource: boolean): DocumentDefinition {
        for (const d of this.getDocs(isSource)) {
            if (d.id == documentId) {
                return d;
            }
        }
        return null;
    }

    public getFirstXmlDoc(isSource: boolean) {
        const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
        for (const doc of docs) {
            if (doc.type == DocumentType.XML) {
                return doc;
            }
        }
        return null;
    }

    public getAllDocs(): DocumentDefinition[] {
        return [this.propertyDoc, this.constantDoc].concat(this.sourceDocs).concat(this.targetDocs);
    }

    public documentsAreLoaded(): boolean {
        for (const doc of this.getAllDocs()) {
            if (!doc.initialized) {
                return false;
            }
        }
        return true;
    }

}
