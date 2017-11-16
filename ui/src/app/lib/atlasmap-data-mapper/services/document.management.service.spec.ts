/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { DocumentManagementService } from './document.management.service';

describe('DocumentManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DocumentManagementService,
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
    'should ...',
    inject([DocumentManagementService], (service: DocumentManagementService) => {
      expect(service).toBeTruthy();
    }),
  );
});
