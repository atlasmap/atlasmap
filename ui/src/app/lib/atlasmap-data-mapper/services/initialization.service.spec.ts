/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DocumentManagementService } from './document-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { InitializationService } from './initialization.service';
import { MappingManagementService } from './mapping-management.service';
import { FieldActionService } from './field-action.service';

describe('InitializationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG}) ],
      providers: [
        DocumentManagementService,
        ErrorHandlerService,
        FieldActionService,
        InitializationService,
        MappingManagementService,
        NGXLogger,
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
