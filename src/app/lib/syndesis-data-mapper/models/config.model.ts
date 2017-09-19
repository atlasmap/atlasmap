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
import { DocumentDefinition, DocumentTypes, DocumentType } from './document.definition.model';
import { LookupTable } from '../models/lookup.table.model';
import { Field } from '../models/field.model';

import { ErrorHandlerService } from '../services/error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { InitializationService } from '../services/initialization.service';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

export class DataMapperInitializationModel {
    public dataMapperVersion: string = "0.9.2017.07.28";
    public initialized: boolean = false;
    public loadingStatus: string = "Loading."
    public initializationErrorOccurred: boolean = false;

    public baseJavaInspectionServiceUrl: string;
    public baseXMLInspectionServiceUrl: string;
    public baseJSONInspectionServiceUrl: string;
    public baseMappingServiceUrl: string;

    /* class path fetching configuration */
    public classPathFetchTimeoutInMilliseconds: number = 30000;
    // if classPath is specified, maven call to resolve pom will be skipped
    public pomPayload: string;

    public classPath: string;

    /* inspection service filtering flags */
    public fieldNameBlacklist: string[] = [];
    public classNameBlacklist: string[] = [];
    public disablePrivateOnlyFields: boolean = false;
    public disableProtectedOnlyFields: boolean = false;
    public disablePublicOnlyFields: boolean = false;
    public disablePublicGetterSetterFields: boolean = false;

    /* mock data configuration */
    public discardNonMockSources: boolean = false;
    public addMockJSONMappings: boolean = false;
    public addMockJavaSingleSource: boolean = false;
    public addMockJavaSources: boolean = false;
    public addMockJavaCachedSource: boolean = false;
    public addMockXMLInstanceSources: boolean = false;
    public addMockXMLSchemaSources: boolean = false;    
    public addMockJSONSources: boolean = false;
    public addMockJSONInstanceSources: boolean = false;
    public addMockJSONSchemaSources: boolean = false;
    
    public addMockJavaTarget: boolean = false;
    public addMockJavaCachedTarget: boolean = false;
    public addMockXMLInstanceTarget: boolean = false;
    public addMockXMLSchemaTarget: boolean = false;
    public addMockJSONTarget: boolean = false;
    public addMockJSONInstanceTarget: boolean = false;
    public addMockJSONSchemaTarget: boolean = false;
    
    /* debug logging toggles */
    public debugDocumentServiceCalls: boolean = false;
    public debugDocumentParsing: boolean = false;
    public debugMappingServiceCalls: boolean = false;
    public debugClassPathServiceCalls: boolean = false;
    public debugValidationServiceCalls: boolean = false;
    public debugFieldActionServiceCalls: boolean = false;

    public mappingInitialized: boolean = false;
    public fieldActionsInitialized: boolean = false;
}

export class ConfigModel {
    private static cfg: ConfigModel = new ConfigModel();
    public static mappingServicesPackagePrefix: string = "io.atlasmap.v2";
    public static javaServicesPackagePrefix: string = "io.atlasmap.java.v2";

    public initCfg: DataMapperInitializationModel = new DataMapperInitializationModel;

    /* current ui state config */
    public showMappingDetailTray: boolean = false;
    public showMappingTable: boolean = false;
    public showNamespaceTable: boolean = false;
    public showLinesAlways: boolean = true;
    public showTypes: boolean = false;
    public showMappedFields: boolean = true;
    public showUnmappedFields: boolean = true;
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

    public errors : ErrorInfo[] = [];
    public validationErrors : ErrorInfo[] = [];

    constructor() {
        this.propertyDoc.initCfg.type.type = DocumentTypes.PROPERTY;
        this.propertyDoc.name = "Properties";
        this.propertyDoc.isSource = true;
        this.constantDoc.initCfg.type.type = DocumentTypes.CONSTANT;
        this.constantDoc.name = "Constants";
        this.constantDoc.isSource = true;
    }

    public static getConfig(): ConfigModel {
        return ConfigModel.cfg;
    }

    public static setConfig(cfg: ConfigModel): void {
        ConfigModel.cfg = cfg;
    }

    private createDocument(documentIdentifier: string, isSource: boolean,
        docType: DocumentTypes, documentContents: string): DocumentDefinition {
        var docDef: DocumentDefinition = new DocumentDefinition();
        docDef.isSource = isSource;
        docDef.initCfg.documentIdentifier = documentIdentifier;
        docDef.initCfg.shortIdentifier = documentIdentifier;
        docDef.uri = documentIdentifier;
        docDef.name = documentIdentifier;
        docDef.initCfg.type.type = docType;
        docDef.initCfg.documentContents = documentContents;
        docDef.initCfg.inspectionType = "INSTANCE";
        if (isSource) {
            this.sourceDocs.push(docDef);
        } else {
            this.targetDocs.push(docDef);
        }
        return docDef;

    }

    public addJavaDocument(documentIdentifier: string, isSource: boolean): DocumentDefinition {
        return this.createDocument(documentIdentifier, isSource, DocumentTypes.JAVA, null);
    }

    public addJSONDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        return this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
    }

    public addJSONInstanceDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        var docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
        docDef.initCfg.inspectionType = "INSTANCE";
        docDef.uri = "atlas:json:" + documentIdentifier;
        return docDef;
    }
    
    public addJSONSchemaDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        var docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.JSON, documentContents);
        docDef.initCfg.inspectionType = "SCHEMA";
        docDef.uri = "atlas:json:" + documentIdentifier;
        return docDef;
    }

    public addXMLInstanceDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        var docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.XML, documentContents);
        docDef.initCfg.inspectionType = "INSTANCE";
        docDef.uri = "atlas:xml:" + documentIdentifier;
        return docDef;
    }

    public addXMLSchemaDocument(documentIdentifier: string, documentContents: string, isSource: boolean): DocumentDefinition {
        var docDef: DocumentDefinition = this.createDocument(documentIdentifier, isSource, DocumentTypes.XML, documentContents);
        docDef.initCfg.inspectionType = "SCHEMA";
        docDef.uri = "atlas:xml:" + documentIdentifier;
        return docDef;
    }

    public getDocsWithoutPropertyDoc(isSource: boolean): DocumentDefinition[] {
        return [].concat(isSource ? this.sourceDocs : this.targetDocs);
    }

    public getDocs(isSource: boolean): DocumentDefinition[] {
        var docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
        return isSource ? docs.concat([this.propertyDoc, this.constantDoc]) : docs;
    }

    public hasJavaDocuments(): boolean {
        for (let doc of this.getAllDocs()) {
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
        for (let doc of this.getAllDocs()) {
            if (doc.initCfg.type.isJava() && doc.initCfg.inspectionResultContents == null) {
                return true;
            }
        }
        return false;
    }

    public getDocForShortIdentifier(shortIdentifier: string, isSource: boolean): DocumentDefinition {
        for (let d of this.getDocs(isSource)) {
            if (d.initCfg.shortIdentifier == shortIdentifier) {
                return d;
            }
        }
        return null;
    }

    public getFirstXmlDoc(isSource: boolean) {
        var docs: DocumentDefinition[] = this.getDocsWithoutPropertyDoc(isSource);
        for (let doc of docs) {
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
        for (let d of this.getAllDocs()) {
            if (!d.initCfg.initialized) {
                return false;
            }
        }
        return true;
    }
}
