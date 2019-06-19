/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DataMapperAppExampleHostComponent } from './data-mapper-example-host.component';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { InitializationService } from '../services/initialization.service';
import { MappingManagementService } from '../services/mapping-management.service';

describe('DataMapperAppExampleHostComponent', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG})],
      providers: [
        DataMapperAppExampleHostComponent,
        DocumentManagementService,
        ErrorHandlerService,
        InitializationService,
        MappingManagementService,
        NGXLogger,
      ],
    });
  });

  it(
    'should ...',
    inject([DataMapperAppExampleHostComponent], (service: DataMapperAppExampleHostComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
