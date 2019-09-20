/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ToolbarComponent } from './toolbar.component';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DocumentType, InspectionType } from '../common/config.types';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { InitializationService } from '../services/initialization.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { FieldActionService } from '../services/field-action.service';
import { FileManagementService } from '../services/file-management.service';
import { DocumentInitializationModel } from '../models/config.model';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

describe('Test toolbar components', () => {
  let initService = null;

  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule,
      platformBrowserDynamicTesting());

    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
      providers: [
        ToolbarComponent,
        DocumentManagementService,
        ErrorHandlerService,
        FieldActionService,
        FileManagementService,
        InitializationService,
        MappingManagementService,
        NGXLogger
      ],
    });
    jasmine.getFixtures().fixturesPath = 'base/test-resources/inspected';
  });

  beforeEach( () => {
    initService = TestBed.get(InitializationService);
    const c = initService.cfg;
    c.initCfg.baseJSONInspectionServiceUrl = 'dummy';
    c.initCfg.baseXMLInspectionServiceUrl = 'dummy';
    const sourceJson = new DocumentInitializationModel();
    sourceJson.isSource = true;
    sourceJson.type = DocumentType.JSON;
    sourceJson.inspectionType = InspectionType.SCHEMA;
    sourceJson.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-json-schema.json');
    c.addDocument(sourceJson);
    const targetXml = new DocumentInitializationModel();
    targetXml.isSource = false;
    targetXml.type = DocumentType.XML;
    targetXml.inspectionType = InspectionType.SCHEMA;
    targetXml.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-xml-schema-1.json');
    c.addDocument(targetXml);
    spyOn(c.mappingService, 'runtimeServiceActive').and.returnValues(true);
    return initService.initialize();
  });

  it(
    'should test \'Reset All\' toolbar menu dropdown selection', (done) => {
      inject([ToolbarComponent], (service: ToolbarComponent) => {

        // Verify the service is active.
        expect(service).toBeTruthy();
        const c = initService.cfg;

        // Verify that we have a source and target document loaded.
        expect(c.sourceDocs[0].fields[0].path).toEqual('/addressList<>');
        expect(c.targetDocs[0].fields[0].path).toEqual('/data');

        // Reset all.
        c.errorService.resetAll();
        c.mappings = null;
        c.clearDocs();

        // Verify all is reset.
        expect(c.sourceDocs.length).toEqual(0);
        expect(c.targetDocs.length).toEqual(0);
        expect(c.mappingFiles.length).toEqual(0);
        expect(c.propertyDoc.allFields.length).toEqual(0);
        expect(c.constantDoc.allFields.length).toEqual(0);
        done();
      })();
    });
  });
