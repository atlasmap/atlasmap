/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { MappingManagementService } from './mapping-management.service';
import { ErrorHandlerService } from './error-handler.service';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG}) ],
      providers: [
        ErrorHandlerService,
        MappingManagementService,
        NGXLogger,
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
