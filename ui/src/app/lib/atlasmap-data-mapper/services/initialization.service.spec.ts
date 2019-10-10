/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DocumentType, InspectionType } from '../common/config.types';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { InitializationService } from './initialization.service';
import { MappingManagementService } from './mapping-management.service';
import { FieldActionService } from './field-action.service';
import { FileManagementService } from './file-management.service';
import { DocumentInitializationModel } from '../models/config.model';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

describe('InitializationService', () => {
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule,
      platformBrowserDynamicTesting());

    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
      providers: [
        DocumentManagementService,
        ErrorHandlerService,
        FieldActionService,
        FileManagementService,
        InitializationService,
        MappingManagementService,
        NGXLogger,
      ],
    });
    jasmine.getFixtures().fixturesPath = 'base/test-resources/inspected';
  });

  afterAll(inject([InitializationService], (service: InitializationService) => {
      service.cfg.clearDocs();
      service.cfg.mappings.mappings = [];
  }));

  it(
    'should load document definitions', (done) => {
      inject([InitializationService], (service: InitializationService) => {
        const c = service.cfg;
        c.initCfg.baseMappingServiceUrl = 'dummy';
        c.initCfg.baseJSONInspectionServiceUrl = 'dummy';
        c.initCfg.baseXMLInspectionServiceUrl = 'dummy';
        const sourceJson = new DocumentInitializationModel();
        sourceJson.isSource = true;
        sourceJson.type = DocumentType.JSON;
        sourceJson.inspectionType = InspectionType.SCHEMA;
        sourceJson.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-json-schema.json');
        c.addDocument(sourceJson);
        const sourceXml = new DocumentInitializationModel();
        sourceXml.isSource = true;
        sourceXml.type = DocumentType.XML;
        sourceXml.inspectionType = InspectionType.INSTANCE;
        sourceXml.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-xml-instance-1.json');
        c.addDocument(sourceXml);
        const targetJson = new DocumentInitializationModel();
        targetJson.isSource = false;
        targetJson.type = DocumentType.JSON;
        targetJson.inspectionType = InspectionType.INSTANCE;
        targetJson.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-json-instance.json');
        c.addDocument(targetJson);
        const targetXml = new DocumentInitializationModel();
        targetXml.isSource = false;
        targetXml.type = DocumentType.XML;
        targetXml.inspectionType = InspectionType.SCHEMA;
        targetXml.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-xml-schema-1.json');
        c.addDocument(targetXml);
        spyOn(c.mappingService, 'runtimeServiceActive').and.returnValues(true);
        return service.initialize().then(() => {
          expect(c.sourceDocs[0].fields[0].path).toEqual('/addressList<>');
          expect(c.sourceDocs[1].fields[0].path).toEqual('/data');
          expect(c.targetDocs[0].fields[0].path).toEqual('/addressList<>');
          expect(c.targetDocs[1].fields[0].path).toEqual('/data');
          done();
        }).catch((error) => {
          fail(error);
          done();
        });
      })();
    });

  it(
    'should load mapping definition', (done) => {
      inject([InitializationService], (service: InitializationService) => {
        const cfg = service.cfg;
        cfg.clearDocs();
        cfg.initCfg.baseMappingServiceUrl = 'dummy';
        cfg.initCfg.baseJSONInspectionServiceUrl = 'dummy';
        cfg.initCfg.baseXMLInspectionServiceUrl = 'dummy';
        const fixtures = jasmine.getFixtures();

        fixtures.fixturesPath = 'base/test-resources/inspected';
        const source = new DocumentInitializationModel();
        source.isSource = true;
        source.type = DocumentType.JSON;
        source.inspectionType = InspectionType.SCHEMA;
        source.id = 'old-action-source';
        source.inspectionResult = fixtures.read('atlasmap-inspection-old-action-source.json');
        cfg.addDocument(source);
        const target = new DocumentInitializationModel();
        target.isSource = false;
        target.type = DocumentType.JSON;
        target.inspectionType = InspectionType.SCHEMA;
        target.id = 'old-action-target';
        target.inspectionResult = fixtures.read('atlasmap-inspection-old-action-target.json');
        cfg.addDocument(target);
        fixtures.fixturesPath = 'base/test-resources/mapping';
        cfg.preloadedMappingJson = fixtures.read('atlasmapping-old-action.json');

        spyOn(cfg.mappingService, 'runtimeServiceActive').and.returnValues(true);
        return service.initialize().then(() => {
          expect(cfg.sourceDocs[0].fields.length).toEqual(1);
          expect(cfg.sourceDocs[0].fields[0].path).toEqual('/<>');
          expect(cfg.targetDocs[0].fields[0].path).toEqual('/id');
          expect(cfg.mappings.mappings.length).toEqual(1);
          const mapping = cfg.mappings.mappings[0];
          expect(mapping.sourceFields.length).toEqual(1);
          const sourceField = mapping.sourceFields[0];
          expect(sourceField.field).toBeTruthy();
          expect(mapping.targetFields.length).toEqual(1);
          const targetField = mapping.targetFields[0];
          expect(targetField.field).toBeTruthy();
          expect(cfg.errors.length).toEqual(0, cfg.errors);
          done();
        }).catch((error) => {
          fail(error);
          done();
        });
      })();
    });
});
