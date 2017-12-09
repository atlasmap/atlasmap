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
import { DocumentDefinition, DocumentTypes } from './document.definition.model';
import { Field } from '../models/field.model';

import { ErrorHandlerService } from '../services/error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { InitializationService } from '../services/initialization.service';
import { ErrorInfo } from '../models/error.model';

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
        this.propertyDoc.initCfg.type.type = DocumentTypes.PROPERTY;
        this.propertyDoc.name = 'Properties';
        this.propertyDoc.isSource = true;
        this.constantDoc.initCfg.type.type = DocumentTypes.CONSTANT;
        this.constantDoc.name = 'Constants';
        this.constantDoc.isSource = true;
    }

    public static getConfig(): ConfigModel {
        return ConfigModel.cfg;
    }

    public static setConfig(cfg: ConfigModel): void {
        ConfigModel.cfg = cfg;
    }

    public addJavaDocument(documentIdentifier: string, isSource: boolean): DocumentDefinition {
        const docDef = this.createDocument(documentIdentifier, isSource, DocumentTypes.JAVA, null);
        docDef.uri = 'atlas:java?className=' + documentIdentifier;
        return docDef;
    }

    public addJSONDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        return this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
    }

    public addJSONInstanceDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        const docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
        docDef.initCfg.inspectionType = 'INSTANCE';
        docDef.uri = 'atlas:json:' + documentIdentifier;
        return docDef;
    }

    public addJSONSchemaDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        const docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
        docDef.initCfg.inspectionType = 'SCHEMA';
        docDef.uri = 'atlas:json:' + documentIdentifier;
        return docDef;
    }

    public addXMLInstanceDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        const docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.XML, documentContents);
        docDef.initCfg.inspectionType = 'INSTANCE';
        docDef.uri = 'atlas:xml:' + documentIdentifier;
        return docDef;
    }

    public addXMLSchemaDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        const docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.XML, documentContents);
        docDef.initCfg.inspectionType = 'SCHEMA';
        docDef.uri = 'atlas:xml:' + documentIdentifier;
        return docDef;
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
            if (doc.initCfg.type.isJava()) {
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
            if (doc.initCfg.type.isJava() && doc.initCfg.inspectionResultContents == null) {
                return true;
            }
        }
        return false;
    }

    public getDocForShortIdentifier(shortIdentifier: string, isSource: boolean): DocumentDefinition {
        for (const d of this.getDocs(isSource)) {
            if (d.initCfg.shortIdentifier == shortIdentifier) {
                return d;
            }
        }
        return null;
    }

    public getFirstXmlDoc(isSource: boolean) {
        const docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
        for (const doc of docs) {
            if (doc.initCfg.type.isXML()) {
                return doc;
            }
        }
        return null;
    }

    public getAllDocs(): DocumentDefinition[] {
        return [this.propertyDoc, this.constantDoc].concat(this.sourceDocs).concat(this.targetDocs);
    }

    public documentsAreLoaded(): boolean {
        for (const d of this.getAllDocs()) {
            if (!d.initCfg.initialized) {
                return false;
            }
        }
        return true;
    }

    private createDocument(documentIdentifier: string, isSource: boolean,
                           docType: DocumentTypes, documentContents: string): DocumentDefinition {
        const docDef: DocumentDefinition = new DocumentDefinition();
        docDef.isSource = isSource;
        docDef.initCfg.documentIdentifier = documentIdentifier;
        docDef.initCfg.shortIdentifier = documentIdentifier;
        docDef.uri = documentIdentifier;
        docDef.name = documentIdentifier;
        docDef.initCfg.type.type = docType;
        docDef.initCfg.documentContents = documentContents;
        docDef.initCfg.inspectionType = 'INSTANCE';
        if (isSource) {
            this.sourceDocs.push(docDef);
        } else {
            this.targetDocs.push(docDef);
        }
        return docDef;

    }

}
