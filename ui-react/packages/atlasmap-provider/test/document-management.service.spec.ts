/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DocumentManagementService } from '../src/services/document-management.service';
import { ErrorHandlerService } from '../src/services/error-handler.service';
import { InspectionType, DocumentType } from '../src/common/config.types';
import { DocumentDefinition } from '../src/models/document-definition.model';
import { ConfigModel } from '../src/models/config.model';

describe('DocumentManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule],
      providers: [DocumentManagementService, ErrorHandlerService],
    });
  });

  beforeEach(inject(
    [DocumentManagementService],
    (service: DocumentManagementService) => {
      service.cfg = new ConfigModel();
      service.cfg.errorService = new ErrorHandlerService();
      jasmine.getFixtures().fixturesPath = 'base/test-resources/inspected';
    }
  ));

  it('should parse Java inspection', async(
    inject(
      [DocumentManagementService],
      (service: DocumentManagementService) => {
        const docDef = new DocumentDefinition();
        docDef.type = DocumentType.JAVA;
        docDef.inspectionResult = jasmine
          .getFixtures()
          .read('atlasmap-inspection-twitter4j.Status.json');
        service.fetchDocument(docDef, null).subscribe(answer => {
          expect(answer.fields.length).toBe(29);
          const text = answer.getField('/text');
          expect(text).toBeTruthy();
          expect(text.name).toBe('text');
          expect(text.type).toBe('STRING');
          expect(text.children.length).toBe(0);
          const user = answer.getField('/user');
          expect(user).toBeTruthy();
          expect(user.name).toBe('user');
          expect(user.type).toBe('COMPLEX');
          expect(user.classIdentifier).toBe('twitter4j.User');
          expect(user.children.length).toBe(57);
          const screenName = user.children.filter(
            child => child.name === 'screenName'
          );
          expect(screenName.length).toBe(1);
          expect(screenName[0].name).toBe('screenName');
          expect(screenName[0].path).toBe('/user/screenName');
          expect(screenName[0].type).toBe('STRING');
          expect(screenName[0].children.length).toBe(0);
        });
      }
    )
  ));

  it('should parse JSON inspection', async(
    inject(
      [DocumentManagementService],
      (service: DocumentManagementService) => {
        const docDef = new DocumentDefinition();
        docDef.type = DocumentType.JSON;
        docDef.inspectionType = InspectionType.SCHEMA;
        docDef.inspectionResult = jasmine
          .getFixtures()
          .read('atlasmap-inspection-complex-object-rooted.json');
        service.fetchDocument(docDef, null).subscribe(answer => {
          expect(answer.fields.length).toBe(1);
          expect(answer.fields[0].name).toBe('order');
          const order = answer.getField('/order');
          expect(order.name).toBe('order');
          expect(order.type).toBe('COMPLEX');
          expect(order.children).toBeTruthy();
          expect(order.children.length).toBe(3);
        });
      }
    )
  ));

  it('should parse XML inspection', async(
    inject(
      [DocumentManagementService],
      (service: DocumentManagementService) => {
        const docDef = new DocumentDefinition();
        docDef.type = DocumentType.XML;
        docDef.inspectionType = InspectionType.SCHEMA;
        docDef.inspectionResult = jasmine
          .getFixtures()
          .read('atlasmap-inspection-po-example-schema.json');
        service.fetchDocument(docDef, null).subscribe(answer => {
          expect(answer.fields.length).toBe(1);
          expect(answer.fields[0].name).toBe('purchaseOrder');
          const purchaseOrder = answer.getField('/tns:purchaseOrder');
          expect(purchaseOrder.name).toBe('purchaseOrder');
          expect(purchaseOrder.type).toBe('COMPLEX');
          expect(purchaseOrder.children).toBeTruthy();
          expect(purchaseOrder.children.length).toBe(5);
        });
      }
    )
  ));

  it('should pick up one XML root element', async(
    inject(
      [DocumentManagementService],
      (service: DocumentManagementService) => {
        const docDef = new DocumentDefinition();
        docDef.type = DocumentType.XML;
        docDef.inspectionType = InspectionType.SCHEMA;
        docDef.inspectionResult = jasmine
          .getFixtures()
          .read('atlasmap-inspection-po-example-schema.json');
        docDef.selectedRoot = 'purchaseOrder';
        service.fetchDocument(docDef, null).subscribe(answer => {
          expect(answer.fields.length).toBe(1);
          expect(answer.fields[0].name).toBe('purchaseOrder');
        });

        const docDef2 = new DocumentDefinition();
        docDef2.type = DocumentType.XML;
        docDef2.inspectionType = InspectionType.SCHEMA;
        docDef2.inspectionResult = docDef.inspectionResult;
        docDef2.selectedRoot = 'comment';
        service.fetchDocument(docDef2, null).subscribe(answer => {
          expect(answer.fields.length).toBe(1);
          expect(answer.fields[0].name).toBe('comment');
        });
      }
    )
  ));
});
