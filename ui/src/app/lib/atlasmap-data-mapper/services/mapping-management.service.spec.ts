/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { MappingManagementService } from './mapping-management.service';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MappingManagementService,
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
    inject([MappingManagementService], (service: MappingManagementService) => {
      expect(service).toBeTruthy();
    }),
  );
});
