/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { MappingManagementService } from './mapping-management.service';
import { ErrorHandlerService } from './error-handler.service';
import { Field } from '../models/field.model';

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
      expect(service.getFieldSelectionExclusionReason(null, f)).toContain('Nested');
    }),
  );
});
