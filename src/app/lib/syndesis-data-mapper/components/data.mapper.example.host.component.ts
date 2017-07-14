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

import { Component, ViewChild } from '@angular/core';

import { DocumentDefinition } from '../models/document.definition.model';
import { MappingDefinition } from '../models/mapping.definition.model';
import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';

import { ErrorHandlerService } from '../services/error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { InitializationService } from '../services/initialization.service';

import { DataMapperAppComponent } from './data.mapper.app.component';

@Component({
    selector: 'data-mapper-example-host',
    template: `
        <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    `,
    providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService]
})

export class DataMapperAppExampleHostComponent {

    @ViewChild('dataMapperComponent')
    public dataMapperComponent: DataMapperAppComponent;

    public cfg: ConfigModel = null;

    constructor(private initializationService: InitializationService) {
        console.log("Host component being created.");
        // initialize config information before initializing services
        var c: ConfigModel = initializationService.cfg

        //store references to our services in our config model

        //initialize base urls for our service calls
        c.initCfg.baseJavaInspectionServiceUrl = "http://localhost:8585/v2/atlas/java/";
        c.initCfg.baseXMLInspectionServiceUrl = "http://localhost:8585/v2/atlas/xml/";
        c.initCfg.baseJSONInspectionServiceUrl = "http://localhost:8585/v2/atlas/json/";
        c.initCfg.baseMappingServiceUrl = "http://localhost:8585/v2/atlas/";

        //initialize data for our class path service call
        //note that quotes, newlines, and tabs are escaped
        c.initCfg.pomPayload = InitializationService.createExamplePom();
        c.initCfg.classPathFetchTimeoutInMilliseconds = 30000;
        // if classPath is specified, maven call to resolve pom will be skipped
        c.initCfg.classPath = null;

        //specify source documents
        c.addJavaDocument("io.atlasmap.java.test.SourceOrder", true);
        c.addJavaDocument("io.atlasmap.java.test.SourceContact", true);
        c.addJavaDocument("io.atlasmap.java.test.SourceAddress", true);
        c.addJavaDocument("io.atlasmap.java.test.TestListOrders", true);
        c.addJavaDocument("io.atlasmap.java.test.TargetOrderArray", true);
        c.addJavaDocument("io.atlasmap.java.test.SourceFlatPrimitiveClass", true);
        c.addJavaDocument("io.atlasmap.java.test.TargetTestClass", true);
        c.addXMLInstanceDocument("XMLInstanceSource", DocumentManagementService.generateMockInstanceXML(), true);
        c.addXMLSchemaDocument("XMLSchemaSource", DocumentManagementService.generateMockSchemaXML(), true);
        c.addJSONDocument("JSONSource", DocumentManagementService.generateMockJSON(), true);

        //specify target document (only one allowed at a time)
        c.addJavaDocument("io.atlasmap.java.test.TargetTestClass", false);
        c.addXMLInstanceDocument("XMLInstanceTarget", DocumentManagementService.generateMockInstanceXML(), false);
        c.addXMLSchemaDocument("XMLSchemaTarget", DocumentManagementService.generateMockSchemaXML(), false);
        c.addJSONDocument("JSONTarget", DocumentManagementService.generateMockJSON(), false);

        //turn on debug logging options as needed
        c.debugDocumentJSON: boolean = false;
        c.debugDocumentParsing: boolean = false;
        c.debugMappingJSON: boolean = false;
        c.debugClassPathJSON: boolean = false;
        c.debugValidationJSON: boolean = false;
        c.debugFieldActionJSON: boolean = false;

        console.log("Example config after host component configuration.", c);

        //initialize system
        this.cfg = c;
        c.initializationService.initialize();

        //save the mappings when the ui calls us back asking for save
        c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
            //NOTE: the mapping definition being saved is currently stored in "this.cfg.mappings" until further notice.

            console.log("Host component saving mappings.");
            console.log("Mappings to save.", this.cfg.mappings);

            //turn this on to print out example json
            var makeExampleJSON: boolean = false;
            if (makeExampleJSON) {
                var jsonObject: any = c.mappingService.serializeMappingsToJSON();
                var jsonVersion = JSON.stringify(jsonObject);
                var jsonPretty = JSON.stringify(JSON.parse(jsonVersion),null,2);
                console.log("Mappings as JSON: " + jsonPretty);
            }

            //This is an example callout to save the mapping to the mock java service
            c.mappingService.saveMappingToService();

            //After you've sucessfully saved you *MUST* call this (don't call on error)
            c.mappingService.handleMappingSaveSuccess(saveHandler);
        });
    }
}
