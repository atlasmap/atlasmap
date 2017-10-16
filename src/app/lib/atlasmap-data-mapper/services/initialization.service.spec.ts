/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { DocumentManagementService } from './document.management.service';
import { ErrorHandlerService } from './error.handler.service';
import { InitializationService } from './initialization.service';
import { MappingManagementService } from './mapping.management.service';

describe('InitializationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DocumentManagementService,
        ErrorHandlerService,
        InitializationService,
        MappingManagementService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http,
          useFactory: (backend, options) => {
            return new Http(backend, options);
          },
          deps: [MockBackend, RequestOptions],
        },
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
