/* tslint:disable:no-unused-variable */

import ky from 'ky/umd';
import log from 'loglevel';

import { FieldActionService } from '../../src/services/field-action.service';
import {
  Multiplicity,
  FieldActionDefinition,
} from '../../src/models/field-action.model';
import { MappingModel } from '../../src/models/mapping.model';
import { Field } from '../../src/models/field.model';

import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';

describe('FieldActionService', () => {
  let service: FieldActionService;
  beforeEach(() => {
    const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });
    service = new FieldActionService(api);
    service.cfg.logger = log.getLogger('copnfig');
  });

  test('parse field action metadata', (done) => {
    service.cfg.preloadedFieldActionMetadata = atlasmapFieldActionJson;
    service.fetchFieldActions().then(() => {
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
      done();
    });
  });
});

describe('FieldActionService.appliesToField()', () => {
  let action: FieldActionDefinition;
  let mapping: MappingModel;
  let source: Field;
  let target: Field;
  let service: FieldActionService;

  beforeEach(() => {
    action = new FieldActionDefinition();
    mapping = new MappingModel();
    mapping.sourceFields.splice(0);
    source = new Field();
    mapping.addField(source, false);
    mapping.targetFields.splice(0);
    target = new Field();
    mapping.addField(target, false);
    const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });
    service = new FieldActionService(api);
    service.cfg.logger = log.getLogger('copnfig');
  });

  test('return false if FieldMappingPair is null', () => {
    expect(service.appliesToField(action, new MappingModel(), false)).toBe(
      false
    );
  });

  test('should return false if source or target is null', () => {
    mapping.targetFields.splice(0);
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return false if action multiplicity is ONE_TO_MANY', () => {
    action.multiplicity = Multiplicity.ONE_TO_MANY;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return false if action multiplicity is MANY_TO_ONE', () => {
    action.multiplicity = Multiplicity.MANY_TO_ONE;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return if action target type is NUMBER and target field type is numeric', () => {
    action.sourceType = 'NUMBER';
    action.targetType = 'NUMBER';
    target.type = 'SHORT';
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = 'NUMBER';
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = 'STRING';
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return if action target type is ANY_DATE and target field type is a date/time', () => {
    action.sourceType = 'ANY_DATE';
    action.targetType = 'ANY_DATE';
    target.type = 'DATE';
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = 'DATE_TIME_TZ';
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = 'STRING';
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return true if action source type STRING matches target field type STRING, and action target type matches target type', () => {
    source.type = 'STRING';
    target.type = 'STRING';
    action.targetType = 'STRING';
    action.sourceType = 'STRING';
    expect(service.appliesToField(action, mapping, false)).toBe(true);
  });

  test('should return false if action source type CHAR matches target field type STRING, and action target type matches target type', () => {
    target.type = 'STRING';
    action.targetType = 'STRING';
    action.sourceType = 'CHAR';
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return false if action source type STRING matches target field type CHAR, and action target type does not match target type', () => {
    target.type = 'CHAR';
    action.targetType = 'STRING';
    action.sourceType = 'STRING';
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });
});
