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
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/observable/forkJoin';
import { Subject } from 'rxjs/Subject';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document.definition.model';
import { MappingDefinition } from '../models/mapping.definition.model';

import { ErrorHandlerService } from './error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { MappingSerializer } from '../services/mapping.serializer';

import { TransitionModel, FieldActionConfig } from '../models/transition.model';

@Injectable()
export class InitializationService {
    public cfg: ConfigModel = ConfigModel.getConfig();

    systemInitializedSource = new Subject<void>();
    systemInitialized$ = this.systemInitializedSource.asObservable();

    initializationStatusChangedSource = new Subject<void>();
    initializationStatusChanged$ = this.initializationStatusChangedSource.asObservable();

    constructor(private documentService: DocumentManagementService,
                private mappingService: MappingManagementService,
                private errorService: ErrorHandlerService) {
        this.resetConfig();

        this.cfg.documentService.initialize();
        this.cfg.mappingService.initialize();
    }

    public resetConfig(): void {
        this.cfg = new ConfigModel();
        this.cfg.documentService = this.documentService;
        this.cfg.documentService.cfg = this.cfg;
        this.cfg.mappingService = this.mappingService;
        this.cfg.mappingService.cfg = this.cfg;
        this.cfg.errorService = this.errorService;
        this.cfg.errorService.cfg = this.cfg;
        this.cfg.initializationService = this;
        ConfigModel.setConfig(this.cfg);
    }

    public initialize(): void {
        if (this.cfg.mappingService == null) {
            this.cfg.errorService.warn('Mapping service is not configured, validation service will not be used.', null);
        } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
            this.cfg.errorService.warn('Mapping service URL is not configured, validation service will not be used.', null);
        }

        if (this.cfg.initCfg.discardNonMockSources) {
            this.cfg.sourceDocs = [];
            this.cfg.targetDocs = [];
        }

        if (this.cfg.initCfg.addMockJSONMappings) {
            const mappingDefinition: MappingDefinition = new MappingDefinition();
            const mappingJSON: any = InitializationService.createExampleMappingsJSON();
            MappingSerializer.deserializeMappingServiceJSON(mappingJSON, mappingDefinition, this.cfg);
            this.cfg.mappings = mappingDefinition;
        }

        if (this.cfg.initCfg.addMockJavaSources || this.cfg.initCfg.addMockJavaSingleSource) {
            this.cfg.addJavaDocument('io.atlasmap.java.test.TargetTestClass', true);
            if (this.cfg.initCfg.addMockJavaSources) {
                this.cfg.addJavaDocument('io.atlasmap.java.test.SourceContact', true);
                this.cfg.addJavaDocument('io.atlasmap.java.test.SourceAddress', true);
                this.cfg.addJavaDocument('io.atlasmap.java.test.TestListOrders', true);
                this.cfg.addJavaDocument('io.atlasmap.java.test.TargetOrderArray', true);
                this.cfg.addJavaDocument('io.atlasmap.java.test.SourceFlatPrimitiveClass', true);
                this.cfg.addJavaDocument('io.atlasmap.java.test.SourceOrder', true);
            }
        }

        if (this.cfg.initCfg.addMockJavaCachedSource) {
            const docDef: DocumentDefinition = this.cfg.addJavaDocument('io.atlasmap.java.test.Name', true);
            docDef.initCfg.inspectionResultContents = DocumentManagementService.generateMockJavaDoc();
        }

        if (this.cfg.initCfg.addMockXMLInstanceSources) {
            this.cfg.addXMLInstanceDocument('XMLInstanceSource', DocumentManagementService.generateMockInstanceXMLDoc(), true);
        }

        if (this.cfg.initCfg.addMockXMLSchemaSources) {
            this.cfg.addXMLSchemaDocument('XMLSchemaSource', DocumentManagementService.generateMockSchemaXMLDoc(), true);
        }

        if (this.cfg.initCfg.addMockJSONSources || this.cfg.initCfg.addMockJSONInstanceSources) {
            this.cfg.addJSONInstanceDocument('JSONInstanceSource', DocumentManagementService.generateMockJSONInstanceDoc(), true);
        }

        if (this.cfg.initCfg.addMockJSONSchemaSources) {
            this.cfg.addJSONSchemaDocument('JSONSchemaSource', DocumentManagementService.generateMockJSONSchemaDoc(), true);
        }

        if (this.cfg.initCfg.addMockJavaTarget) {
            this.cfg.addJavaDocument('io.atlasmap.java.test.TargetTestClass', false);
        }

        if (this.cfg.initCfg.addMockJavaCachedTarget) {
            const docDef: DocumentDefinition = this.cfg.addJavaDocument('io.atlasmap.java.test.Name', false);
            docDef.initCfg.inspectionResultContents = DocumentManagementService.generateMockJavaDoc();
        }

        if (this.cfg.initCfg.addMockXMLInstanceTarget) {
            this.cfg.addXMLInstanceDocument('XMLInstanceTarget', DocumentManagementService.generateMockInstanceXMLDoc(), false);
        }

        if (this.cfg.initCfg.addMockXMLSchemaTarget) {
            this.cfg.addXMLSchemaDocument('XMLSchemaTarget', DocumentManagementService.generateMockSchemaXMLDoc(), false);
        }

        if (this.cfg.initCfg.addMockJSONTarget || this.cfg.initCfg.addMockJSONInstanceTarget) {
            this.cfg.addJSONInstanceDocument('JSONInstanceTarget', DocumentManagementService.generateMockJSONInstanceDoc(), false);
        }

        if (this.cfg.initCfg.addMockJSONSchemaTarget) {
            this.cfg.addJSONSchemaDocument('JSONSchemaTarget', DocumentManagementService.generateMockJSONSchemaDoc(), false);
        }

        //load field actions
        this.fetchFieldActions();

        //load documents
        if (!this.cfg.isClassPathResolutionNeeded()) {
            this.fetchDocuments();
        } else {
            this.updateLoadingStatus('Loading Maven class path.');
            //fetch class path
            this.cfg.documentService.fetchClassPath().subscribe(
                (classPath: string) => {
                    this.cfg.initCfg.classPath = classPath;
                    this.fetchDocuments();
                    this.updateStatus();
                },
                (error: any) => { this.handleError('could not load Maven class path.', error); },
            );
        }

        //load mappings
        if (this.cfg.mappings != null) {
            this.cfg.initCfg.mappingInitialized = true;
            this.updateStatus();
        } else {
            this.cfg.mappings = new MappingDefinition();
            if (this.cfg.mappingFiles.length > 0) {
                this.fetchMappings(this.cfg.mappingFiles);
            } else {
                this.cfg.mappingService.findMappingFiles('UI').subscribe(
                    (files: string[]) => { this.fetchMappings(files); },
                    (error: any) => { this.handleError('could not load mapping files.', error); },
                );
            }
        }
    }

    private fetchDocuments(): void {
        this.updateLoadingStatus('Loading source/target documents.');
        for (const docDef of this.cfg.getAllDocs()) {
            if (docDef == this.cfg.propertyDoc || docDef == this.cfg.constantDoc) {
                docDef.initCfg.initialized = true;
                continue;
            }

            const docName: string = docDef.initCfg.shortIdentifier;

            if (docDef.initCfg.type.isJava() && this.cfg.initCfg.baseJavaInspectionServiceUrl == null) {
                this.cfg.errorService.warn('Java inspection service is not configured. Document will not be loaded: ' + docName, docDef);
                docDef.initCfg.initialized = true;
                this.updateStatus();
                continue;
            } else if (docDef.initCfg.type.isXML() && this.cfg.initCfg.baseXMLInspectionServiceUrl == null) {
                this.cfg.errorService.warn('XML inspection service is not configured. Document will not be loaded: ' + docName, docDef);
                docDef.initCfg.initialized = true;
                this.updateStatus();
                continue;
            } else if (docDef.initCfg.type.isJSON() && this.cfg.initCfg.baseJSONInspectionServiceUrl == null) {
                this.cfg.errorService.warn('JSON inspection service is not configured. Document will not be loaded: ' + docName, docDef);
                docDef.initCfg.initialized = true;
                this.updateStatus();
                continue;
            }

            this.cfg.documentService.fetchDocument(docDef, this.cfg.initCfg.classPath).subscribe(
                (doc: DocumentDefinition) => {
                    this.updateStatus();
                },
                (error: any) => { this.handleError("Could not load document '"
                    + docDef.initCfg.documentIdentifier + "'.", error); },
            );
        }
    }

    private fetchMappings(mappingFiles: string[]): void {
        if (mappingFiles.length == 0) {
            this.cfg.initCfg.mappingInitialized = true;
            this.updateStatus();
            return;
        }
        this.cfg.mappingService.fetchMappings(mappingFiles, this.cfg.mappings).subscribe(
            (result: boolean) => {
                this.cfg.initCfg.mappingInitialized = true;
                this.updateStatus();
            },
            (error: any) => { this.handleError('could not load mapping definitions.', error); },
        );
    }

    private fetchFieldActions(): void {
        if (this.cfg.mappingService == null) {
            this.cfg.errorService.warn('Mapping service is not provided. Field Actions will not be used.', null);
            this.cfg.initCfg.fieldActionsInitialized = true;
            this.updateStatus();
            return;
        } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
            this.cfg.errorService.warn('Mapping service URL is not provided. Field Actions will not be used.', null);
            this.cfg.initCfg.fieldActionsInitialized = true;
            this.updateStatus();
            return;
        }
        this.cfg.mappingService.fetchFieldActions().subscribe(
            (actionConfigs: FieldActionConfig[]) => {
                TransitionModel.actionConfigs = actionConfigs;
                this.cfg.initCfg.fieldActionsInitialized = true;
                this.updateStatus();
            },
            (error: any) => { this.handleError('Could not load field action configs.', error); },
        );
    }

    private updateStatus(): void {
        const documentCount: number = this.cfg.getAllDocs().length;
        let finishedDocCount = 0;
        for (const docDef of this.cfg.getAllDocs()) {
            if (docDef.initCfg.initialized || docDef.initCfg.errorOccurred) {
                finishedDocCount++;
            }
        }

        if ((documentCount == finishedDocCount) && this.cfg.initCfg.mappingInitialized && this.cfg.initCfg.fieldActionsInitialized) {
            this.cfg.mappings.detectTableIdentifiers();
            this.cfg.mappings.updateDocumentNamespacesFromMappings(this.cfg);
            this.cfg.mappings.updateMappingsFromDocuments(this.cfg);
            for (const d of this.cfg.getAllDocs()) {
                d.updateFromMappings(this.cfg.mappings, this.cfg);
            }
            this.cfg.mappings.removeStaleMappings(this.cfg);
            this.updateLoadingStatus('Initialization complete.');
            this.cfg.initCfg.initialized = true;
            this.systemInitializedSource.next();
        }
    }

    private handleError(message: string, error: any ) {
        message = 'Data Mapper UI Initialization Error: ' + message;
        this.cfg.errorService.error(message, error);
        this.updateLoadingStatus(message);
        this.cfg.initCfg.initializationErrorOccurred = true;
        this.updateStatus();
    }

    private updateLoadingStatus(status: string): void {
        this.cfg.initCfg.loadingStatus = status;
        this.initializationStatusChangedSource.next();
    }

    public static createExamplePom(): string {
        const pom = `
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

                <modelVersion>4.0.0</modelVersion>
                <groupId>foo.bar</groupId>
                <artifactId>test.model</artifactId>
                <version>1.10.0</version>
                <packaging>jar</packaging>
                <name>Test :: Model</name>

                <dependencies>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-annotations</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-databind</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-core</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                </dependencies>
            </project>
        `;

        //pom = pom.replace(/\"/g, "\\\"");
        /*
        pom = pom.replace(/\n/g, "\\n");
        pom = pom.replace(/\t/g, "\\t");
        */
        return pom;
    }

    public static createExampleMappingsJSON(): any {
        const json: any = {
            'AtlasMapping': {
                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.AtlasMapping',
                'fieldMappings': {
                    'fieldMapping': [
                        {
                            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MapFieldMapping',
                            'inputField': {
                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                'field': {
                                    'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                    'status': 'SUPPORTED',
                                    'modifiers': { 'modifier': [] },
                                    'name': 'text',
                                    'className': 'java.lang.String',
                                    'type': 'STRING',
                                    'getMethod': 'getText',
                                    'primitive': true,
                                    'array': false,
                                    'synthetic': false,
                                    'path': 'Text',
                                },
                                'fieldActions': null,
                            },
                            'outputField': {
                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                'field': {
                                    'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                    'status': 'SUPPORTED',
                                    'modifiers': { 'modifier': [ 'PRIVATE' ] },
                                    'name': 'Description',
                                    'className': 'java.lang.String',
                                    'type': 'STRING',
                                    'getMethod': 'getDescription',
                                    'setMethod': 'setDescription',
                                    'primitive': true,
                                    'array': false,
                                    'synthetic': false,
                                    'path': 'Description',
                                },
                                'fieldActions': null,
                            },
                        },
                        {
                            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.SeparateFieldMapping',
                            'inputField': {
                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                'field': {
                                    'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                    'status': 'SUPPORTED',
                                    'modifiers': { 'modifier': [] },
                                    'name': 'name',
                                    'className': 'java.lang.String',
                                    'type': 'STRING',
                                    'getMethod': 'getName',
                                    'primitive': true,
                                    'array': false,
                                    'synthetic': false,
                                    'path': 'User.Name',
                                },
                                'fieldActions': null,
                            },
                            'outputFields': {
                                'mappedField': [
                                    {
                                        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                        'field': {
                                            'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                            'status': 'SUPPORTED',
                                            'modifiers': { 'modifier': [ 'PRIVATE' ] },
                                            'name': 'FirstName',
                                            'className': 'java.lang.String',
                                            'type': 'STRING',
                                            'getMethod': 'getFirstName',
                                            'setMethod': 'setFirstName',
                                            'primitive': true,
                                            'array': false,
                                            'synthetic': false,
                                            'path': 'FirstName',
                                        },
                                        'fieldActions': {
                                            'fieldAction': [ {
                                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MapAction',
                                                'index': 0,
                                            } ],
                                        },
                                    },
                                    {
                                        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                        'field': {
                                            'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                            'status': 'SUPPORTED',
                                            'modifiers': {
                                            'modifier': [ 'PRIVATE' ] },
                                            'name': 'LastName',
                                            'className': 'java.lang.String',
                                            'type': 'STRING',
                                            'getMethod': 'getLastName',
                                            'setMethod': 'setLastName',
                                            'primitive': true,
                                            'array': false,
                                            'synthetic': false,
                                            'path': 'LastName',
                                        },
                                        'fieldActions': {
                                            'fieldAction': [ {
                                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MapAction',
                                                'index': 1,
                                            } ],
                                        },
                                    },
                                ],
                            },
                            'strategy': 'SPACE',
                        },
                        {
                            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MapFieldMapping',
                            'inputField': {
                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                'field': {
                                    'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                    'status': 'SUPPORTED',
                                    'modifiers': { 'modifier': [] },
                                    'name': 'screenName',
                                    'className': 'java.lang.String',
                                    'type': 'STRING',
                                    'getMethod': 'getScreenName',
                                    'primitive': true,
                                    'array': false,
                                    'synthetic': false,
                                    'path': 'User.ScreenName',
                                },
                                'fieldActions': null,
                            },
                            'outputField': {
                                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.MappedField',
                                'field': {
                                    'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                                    'status': 'SUPPORTED',
                                    'modifiers': {
                                    'modifier': [ 'PRIVATE' ] },
                                    'name': 'Title',
                                    'className': 'java.lang.String',
                                    'type': 'STRING',
                                    'getMethod': 'getTitle',
                                    'setMethod': 'setTitle',
                                    'primitive': true,
                                    'array': false,
                                    'synthetic': false,
                                    'path': 'Title',
                                },
                                'fieldActions': null,
                            },
                        },
                    ],
                },
                'name': 'UI.867332',
                'sourceUri': 'atlas:java?className=twitter4j.Status',
                'targetUri': 'atlas:java?className=org.apache.camel.salesforce.dto.Contact',
                'lookupTables': { 'lookupTable': [] },
            },
        };
        return json;
    }
}
