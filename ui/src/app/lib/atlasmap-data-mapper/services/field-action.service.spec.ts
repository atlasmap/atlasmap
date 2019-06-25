/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { FieldActionService } from './field-action.service';
import { ErrorHandlerService } from './error-handler.service';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG}) ],
      providers: [
        ErrorHandlerService,
        FieldActionService,
        NGXLogger,
      ],
    });
  });

  beforeEach(() => {
    jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
  });

  it(
    'should parse field action metadata',
    inject([FieldActionService], (service: FieldActionService) => {
      service.cfg.preloadedFieldActionMetadata = JSON.parse(jasmine.getFixtures().read('atlasmap-field-action.json'));
      service.fetchFieldActions();
      const dummy = service.actionDefinitions[service.actionDefinitions.length - 1];
      expect(dummy.name).toBe('DummyFieldAction');
      expect(dummy.isCustom).toBe(true);
    },
  ));

});
