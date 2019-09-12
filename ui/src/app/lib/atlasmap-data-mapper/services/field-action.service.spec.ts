/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { FieldActionService } from './field-action.service';
import { ErrorHandlerService } from './error-handler.service';
import { Multiplicity, FieldActionDefinition } from '../models/field-action.model';
import { MappingModel } from '../models/mapping.model';
import { Field } from '../models/field.model';

describe('FieldActionService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
      providers: [
        ErrorHandlerService,
        FieldActionService,
        NGXLogger,
      ],
    });
    jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
  });

  it(
    'should parse field action metadata',
    inject([FieldActionService], (service: FieldActionService) => {
      service.cfg.preloadedFieldActionMetadata = JSON.parse(jasmine.getFixtures().read('atlasmap-field-action.json'));
      service.fetchFieldActions();
      let concatenateFound = false;
      let dummyFound = false;
      for (const action of service.actions[Multiplicity.ONE_TO_ONE]) {
        if (action.name === 'io.atlasmap.maven.test.DummyOneToOne') {
          expect(action.isCustom).toBeTruthy();
          dummyFound = true;
        } else if (action.name === 'Concatenate') {
          expect(action.isCustom).toBeFalsy();
          action.arguments[0].name = 'delimiter';
          concatenateFound = true;
        }
      }
      expect(dummyFound).toBeTruthy();
      expect(concatenateFound).toBeFalsy();
      dummyFound = false;
      concatenateFound = false;
      for (const action of service.actions[Multiplicity.MANY_TO_ONE]) {
        if (action.name === 'io.atlasmap.maven.test.DummyOneToOne') {
          expect(action.isCustom).toBeTruthy();
          dummyFound = true;
        } else if (action.name === 'Concatenate') {
          expect(action.isCustom).toBeFalsy();
          action.arguments[0].name = 'delimiter';
          concatenateFound = true;
        }
      }
      expect(dummyFound).toBeFalsy();
      expect(concatenateFound).toBeTruthy();
    },
    ));

});

describe('FieldActionService.appliesToField()', () => {
    let action: FieldActionDefinition;
    let mapping: MappingModel;
    let source: Field;
    let target: Field;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({ level: NgxLoggerLevel.DEBUG })],
        providers: [
          ErrorHandlerService,
          FieldActionService,
          NGXLogger,
        ],
      });
      jasmine.getFixtures().fixturesPath = 'base/test-resources/fieldActions';
      action = new FieldActionDefinition();
      mapping = new MappingModel();
      mapping.sourceFields.splice(0);
      source = new Field();
      mapping.addField(source, false);
      mapping.targetFields.splice(0);
      target = new Field();
      mapping.addField(target, false);
    });

    it('should return false if FieldMappingPair is null',
      inject([FieldActionService], (service: FieldActionService) => {
        expect(service.appliesToField(action, null, false)).toBe(false);
    }));

    it('should return false if source or target is null',
      inject([FieldActionService], (service: FieldActionService) => {
        mapping.targetFields.splice(0);
        expect(service.appliesToField(action, mapping, false)).toBe(false);
    }));

    it('should return false if action multiplicity is ONE_TO_MANY',
      inject([FieldActionService], (service: FieldActionService) => {
        action.multiplicity = Multiplicity.ONE_TO_MANY;
        expect(service.appliesToField(action, mapping, false)).toBe(false);
    }));

    it('should return false if action multiplicity is MANY_TO_ONE',
      inject([FieldActionService], (service: FieldActionService) => {
        action.multiplicity = Multiplicity.MANY_TO_ONE;
        expect(service.appliesToField(action, mapping, false)).toBe(false);
    }));

    it('should return if action target type is NUMBER and target field type is numeric',
      inject([FieldActionService], (service: FieldActionService) => {
        action.sourceType = 'NUMBER';
        action.targetType = 'NUMBER';
        target.type = 'SHORT';
        expect(service.appliesToField(action, mapping, false)).toBe(true);
        target.type = 'NUMBER';
        expect(service.appliesToField(action, mapping, false)).toBe(true);
        target.type = 'STRING';
        expect(service.appliesToField(action, mapping, false)).toBe(false);
    }));

    it('should return if action target type is ANY_DATE and target field type is a date/time',
      inject([FieldActionService], (service: FieldActionService) => {
        action.sourceType = 'ANY_DATE';
        action.targetType = 'ANY_DATE';
        target.type = 'DATE';
        expect(service.appliesToField(action, mapping, false)).toBe(true);
        target.type = 'DATE_TIME_TZ';
        expect(service.appliesToField(action, mapping, false)).toBe(true);
        target.type = 'STRING';
        expect(service.appliesToField(action, mapping, false)).toBe(false);
    }));

    it('should return true if action source type STRING matches target field type STRING, and action target type matches target type',
      inject([FieldActionService], (service: FieldActionService) => {
        source.type = 'STRING';
        target.type = 'STRING';
        action.targetType = 'STRING';
        action.sourceType = 'STRING';
        expect(service.appliesToField(action, mapping, false)).toBe(true);
      }));

    it('should return false if action source type CHAR matches target field type STRING, and action target type matches target type',
      inject([FieldActionService], (service: FieldActionService) => {
        target.type = 'STRING';
        action.targetType = 'STRING';
        action.sourceType = 'CHAR';
        expect(service.appliesToField(action, mapping, false)).toBe(false);
      }));

    it('should return false if action source type STRING matches target field type CHAR, and action target type does not match target type',
      inject([FieldActionService], (service: FieldActionService) => {
        target.type = 'CHAR';
        action.targetType = 'STRING';
        action.sourceType = 'STRING';
        expect(service.appliesToField(action, mapping, false)).toBe(false);
      }));

  });
