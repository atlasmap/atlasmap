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

import { Component, ViewChild, OnInit } from '@angular/core';
import { ConfigModel } from '../models/config.model';

import { ErrorHandlerService } from '../services/error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { InitializationService } from '../services/initialization.service';

import { DataMapperAppComponent } from './data-mapper-app.component';

@Component({
  selector: 'data-mapper-example-host',
  template: `
        <data-mapper #dataMapperComponent></data-mapper>
    `,
  providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService],
})

export class DataMapperAppExampleHostComponent implements OnInit {

  @ViewChild('dataMapperComponent')
  dataMapperComponent: DataMapperAppComponent;

  constructor(private initializationService: InitializationService) { }

  ngOnInit() {
    // initialize config information before initializing services
    const c: ConfigModel = this.initializationService.cfg;

    //store references to our services in our config model

    //initialize base urls for our service calls
    c.initCfg.baseJavaInspectionServiceUrl = 'http://localhost:8585/v2/atlas/java/';
    c.initCfg.baseXMLInspectionServiceUrl = 'http://localhost:8585/v2/atlas/xml/';
    c.initCfg.baseJSONInspectionServiceUrl = 'http://localhost:8585/v2/atlas/json/';
    c.initCfg.baseMappingServiceUrl = 'http://localhost:8585/v2/atlas/';

    //initialize data for our class path service call
    //note that quotes, newlines, and tabs are escaped
    c.initCfg.pomPayload = InitializationService.createExamplePom();
    c.initCfg.classPathFetchTimeoutInMilliseconds = 30000;
    // if classPath is specified, maven call to resolve pom will be skipped
    c.initCfg.classPath = null;

    /*
     * The following examples demonstrate adding source/target documents to the Data Mapper's configuration.
     * Note that multiple source documents are supported, but multiple target documents are not supported.
     *
     * example java source document configuration:
     *
     * var documentIsSourceDocument: boolean = true;
     * c.addJavaDocument("io.atlasmap.java.test.SourceOrder", documentIsSourceDocument);
     *
     * example xml instance document:
     *
     * c.addXMLInstanceDocument("XMLInstanceSource", DocumentManagementService.generateMockInstanceXML(), documentIsSourceDocument);
     *
     * example xml schema document:
     *
     * c.addXMLSchemaDocument("XMLSchemaSource", DocumentManagementService.generateMockSchemaXML(), documentIsSourceDocument);
     *
     * example json document:
     *
     * c.addJSONDocument("JSONTarget", DocumentManagementService.generateMockJSON(), documentIsSourceDocument);
     *
     */

    //enable debug logging options as needed
    c.initCfg.debugDocumentServiceCalls = true;
    c.initCfg.debugDocumentParsing = false;
    c.initCfg.debugMappingServiceCalls = false;
    c.initCfg.debugClassPathServiceCalls = false;
    c.initCfg.debugValidationServiceCalls = false;
    c.initCfg.debugFieldActionServiceCalls = false;

    //enable mock mappings loading, example code is shown in the InitializationService for this
    c.initCfg.addMockJSONMappings = false;

    //enable mock source/target documents as needed
    c.initCfg.addMockJavaSingleSource = false;
    c.initCfg.addMockJavaSources = true;
    c.initCfg.addMockJavaCachedSource = false;
    c.initCfg.addMockXMLInstanceSources = false;
    c.initCfg.addMockXMLSchemaSources = true;
    c.initCfg.addMockJSONSources = false;
    c.initCfg.addMockJSONInstanceSources = false;
    c.initCfg.addMockJSONSchemaSources = true;

    c.initCfg.addMockJavaTarget = true;
    c.initCfg.addMockJavaCachedTarget = false;
    c.initCfg.addMockXMLInstanceTarget = false;
    c.initCfg.addMockXMLSchemaTarget = true;
    c.initCfg.addMockJSONTarget = false;
    c.initCfg.addMockJSONInstanceTarget = false;
    c.initCfg.addMockJSONSchemaTarget = true;

    //initialize system
    this.initializationService.initialize();

    //save the mappings when the ui calls us back asking for save
    c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
      //NOTE: the mapping definition being saved is currently stored in "this.cfg.mappings" until further notice.

      //This is an example callout to save the mapping to the mock java service
      c.mappingService.saveMappingToService();

      //After you've sucessfully saved you *MUST* call this (don't call on error)
      c.mappingService.handleMappingSaveSuccess(saveHandler);
    });

    if (!c.sourceDocs || c.sourceDocs.length == 0) {
      c.errorService.error('No source document was found', '');
    }
    if (!c.targetDocs || c.targetDocs.length == 0) {
      c.errorService.error('No target document was found', '');
    }
  }
}
