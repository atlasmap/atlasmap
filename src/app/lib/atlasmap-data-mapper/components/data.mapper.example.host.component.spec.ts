/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { DataMapperAppExampleHostComponent } from './data.mapper.example.host.component';
import { DocumentManagementService } from '../services/document.management.service';
import { ErrorHandlerService } from '../services/error.handler.service';
import { InitializationService } from '../services/initialization.service';
import { MappingManagementService } from '../services/mapping.management.service';

describe('DataMapperAppExampleHostComponent', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DataMapperAppExampleHostComponent,
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
        },],
    });
  });

  it(
    'should ...',
    inject([DataMapperAppExampleHostComponent], (service: DataMapperAppExampleHostComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
