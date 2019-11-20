/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { MappingManagementService } from '../src/services/mapping-management.service';
import { ErrorHandlerService } from '../src/services/error-handler.service';
import { Field } from '../src/models/field.model';

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
    'should check banned fields',
    inject([MappingManagementService], (service: MappingManagementService) => {
      const f = new Field();
      f.isCollection = true;
      f.parentField = new Field();
      f.parentField.isCollection = true;
      f.isPrimitive = true;
      expect(service.getFieldSelectionExclusionReason(null, f.parentField)).toContain('parent');
    }),
  );
});
