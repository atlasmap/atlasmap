/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { InitializationService } from './initialization.service';
import { MappingManagementService } from './mapping-management.service';

describe('InitializationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule ],
      providers: [
        DocumentManagementService,
        ErrorHandlerService,
        InitializationService,
        MappingManagementService,
      ],
    });
  });

  it(
    'should ...',
    inject([InitializationService], (service: InitializationService) => {
      expect(service).toBeTruthy();
    }),
  );
});
