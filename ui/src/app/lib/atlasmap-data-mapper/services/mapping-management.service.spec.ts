/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { MappingManagementService } from './mapping-management.service';
import { FieldActionConfig } from '../models/transition.model';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG}) ],
      providers: [
        MappingManagementService,
        NGXLogger,
      ],
    });
  });

  beforeEach(() => {
    jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
  });

  it(
    'should parse field action metadata',
    () => {
      const fieldActionMetadata = JSON.parse(jasmine.getFixtures().read('atlasmap-field-action.json'));
      const actionConfigs: FieldActionConfig[] = [];
      for (const actionDetail of fieldActionMetadata.ActionDetails.actionDetail) {
        const fieldActionConfig = MappingManagementService.extractFieldActionConfig(actionDetail);
        actionConfigs.push(fieldActionConfig);
      }
      MappingManagementService.sortFieldActionConfigs(actionConfigs);
      const dummy = actionConfigs[actionConfigs.length - 1];
      expect(dummy.name).toBe('DummyFieldAction');
      expect(dummy.isCustom).toBe(true);
    },
  );

  it(
    'should ...',
    inject([MappingManagementService], (service: MappingManagementService) => {
      expect(service).toBeTruthy();
    }),
  );
});
