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
      let concatenateFound = false;
      let dummyFound = false;
      for (const action of service.actionDefinitions) {
        if (action.name === 'io.atlasmap.maven.test.DummyOneToOne') {
          expect(action.isCustom).toBeTruthy();
          dummyFound = true;
        } else if (action.name === 'Concatenate') {
          expect(action.isCustom).toBeFalsy();
          action.arguments[0].name = 'delimiter';
          concatenateFound = true;
        }
      }
      expect(concatenateFound).toBeTruthy();
      expect(dummyFound).toBeTruthy();
    },
  ));

});
