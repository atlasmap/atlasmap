import { Component, ViewChild } from '@angular/core';
import { ElectronService } from './providers/electron.service';

import { DocumentDefinition } from 'syndesis.data.mapper';
import { MappingDefinition } from 'syndesis.data.mapper';
import { ConfigModel } from 'syndesis.data.mapper';
import { MappingModel } from 'syndesis.data.mapper';
import { ErrorHandlerService } from 'syndesis.data.mapper';
import { DocumentManagementService } from 'syndesis.data.mapper';
import { MappingManagementService } from 'syndesis.data.mapper';
import { InitializationService } from 'syndesis.data.mapper';
import { DataMapperAppComponent } from 'syndesis.data.mapper';

@Component({
  selector: 'app-root',
  template: `<data-mapper #dataMapperComponent></data-mapper>`,
  providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService]
})
export class AppComponent {

  @ViewChild('dataMapperComponent')
  public dataMapperComponent: DataMapperAppComponent;

    constructor(private initializationService: InitializationService, public electronService: ElectronService) {

        console.log("window.location.hash", window.location.hash);
        var options = JSON.parse(decodeURIComponent(window.location.hash.substring(1)));
        console.log("options", options);

        if (electronService.isElectron()) {
          console.log('Mode electron');
          // Check if electron is correctly injected (see externals in webpack.config.js)
          console.log('c', electronService.ipcRenderer);
          // Check if nodeJs childProcess is correctly injected (see externals in webpack.config.js)
          console.log('c', electronService.childProcess);
        } else {
          console.log('Mode web');
        }

        console.log("Host component being created.");
        // initialize config information before initializing services
        var c: ConfigModel = initializationService.cfg;

        //store references to our services in our config model

        //initialize base urls for our service calls
        c.initCfg.baseJavaInspectionServiceUrl = "http://localhost:"+options.atlasMapServicePort+"/v2/atlas/java/";
        c.initCfg.baseXMLInspectionServiceUrl = "http://localhost:"+options.atlasMapServicePort+"/v2/atlas/xml/";
        c.initCfg.baseJSONInspectionServiceUrl = "http://localhost:"+options.atlasMapServicePort+"/v2/atlas/json/";
        c.initCfg.baseMappingServiceUrl = "http://localhost:"+options.atlasMapServicePort+"/v2/atlas/";

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
        c.initCfg.addMockJavaSources = false;
        c.initCfg.addMockJavaCachedSource = false;
        c.initCfg.addMockXMLInstanceSources = false;
        c.initCfg.addMockXMLSchemaSources = false;
        c.initCfg.addMockJSONSources = false;
        c.initCfg.addMockJSONInstanceSources = false;
        c.initCfg.addMockJSONSchemaSources = true;
        
        c.initCfg.addMockJavaTarget = false;
        c.initCfg.addMockJavaCachedTarget = false;
        c.initCfg.addMockXMLInstanceTarget = false;
        c.initCfg.addMockXMLSchemaTarget = false;
        c.initCfg.addMockJSONTarget = false;
        c.initCfg.addMockJSONInstanceTarget = false;
        c.initCfg.addMockJSONSchemaTarget = true;
        
        console.log("Example config after host component configuration.", c);

        //initialize system
        initializationService.initialize();

        //save the mappings when the ui calls us back asking for save
        c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
            //NOTE: the mapping definition being saved is currently stored in "this.cfg.mappings" until further notice.

            console.log("Host component saving mappings.");
            console.log("Mappings to save.", ConfigModel.getConfig().mappings);

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
