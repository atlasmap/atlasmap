/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DocumentType } from '../common/config.types';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { InitializationService } from './initialization.service';
import { MappingManagementService } from './mapping-management.service';
import { FieldActionService } from './field-action.service';
import { FileManagementService } from './file-management.service';
import { DocumentInitializationModel } from '../models/config.model';

describe('InitializationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG}) ],
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

  it(
    'should load document definitions',
     async(inject([InitializationService], (service: InitializationService) => {
      const c = service.cfg;
      const sourceJson = new DocumentInitializationModel();
      sourceJson.isSource = true;
      sourceJson.type = DocumentType.JSON;
      sourceJson.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-json-schema.json');
      c.addDocument(sourceJson);
      const sourceXml = new DocumentInitializationModel();
      sourceXml.isSource = true;
      sourceJson.type = DocumentType.XML;
      sourceXml.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-xml-instance-1.json');
      c.addDocument(sourceJson);
      const targetJson = new DocumentInitializationModel();
      targetJson.isSource = false;
      sourceJson.type = DocumentType.JSON;
      targetJson.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-json-instance.json');
      c.addDocument(sourceJson);
      const targetXml = new DocumentInitializationModel();
      targetJson.isSource = false;
      sourceJson.type = DocumentType.XML;
      targetXml.inspectionResult = jasmine.getFixtures().read('atlasmap-inspection-mock-xml-schema-1.json');
      c.addDocument(sourceJson);
      service.initialize().then(() => {
        expect(c.sourceDocs[0].fields[0].path).toEqual('/order');
        expect(c.sourceDocs[1].fields[0].path).toEqual('/data');
        expect(c.targetDocs[0].fields[0].path).toEqual('/order');
        expect(c.targetDocs[1].fields[0].path).toEqual('/data');
      });
    }),
  ));
});
