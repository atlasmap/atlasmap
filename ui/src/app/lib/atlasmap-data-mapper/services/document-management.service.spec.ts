/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { InspectionType, DocumentType } from '../common/config.types';
import { DocumentDefinition } from '../models/document-definition.model';
import { ConfigModel } from '../models/config.model';

describe('DocumentManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DocumentManagementService,
        ErrorHandlerService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http,
          useFactory: (backend: MockBackend, options: RequestOptions) => {
            return new Http(backend, options);
          },
          deps: [MockBackend, RequestOptions],
        },
      ],
    });
  });

  it(
    'should pick up one XML root element...',
    async(inject([DocumentManagementService], (service: DocumentManagementService) => {
      service.cfg = new ConfigModel();
      service.cfg.errorService = new ErrorHandlerService();
      service.cfg.errorService.cfg = service.cfg;
      jasmine.getFixtures().fixturesPath = 'base/test-resources';
      const inspectionResult = jasmine.getFixtures().read('po-example-schema-result.json');

      const docDef = new DocumentDefinition();
      docDef.type = DocumentType.XML;
      docDef.inspectionType = InspectionType.SCHEMA;
      docDef.inspectionResult = inspectionResult;
      docDef.selectedRoot = 'purchaseOrder';
      service.fetchDocument(docDef, null).subscribe(answer => {
        expect(answer.fields.length).toBe(1);
        expect(answer.fields[0].name).toBe('purchaseOrder');
      });

      const docDef2 = new DocumentDefinition();
      docDef2.type = DocumentType.XML;
      docDef2.inspectionType = InspectionType.SCHEMA;
      docDef2.inspectionResult = inspectionResult;
      docDef2.selectedRoot = 'comment';
      service.fetchDocument(docDef2, null).subscribe(answer => {
        expect(answer.fields.length).toBe(1);
        expect(answer.fields[0].name).toBe('comment');
      });
    }),
  ));
});
