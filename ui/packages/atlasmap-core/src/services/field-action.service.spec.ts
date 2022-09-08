/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import { Field } from '../models/field.model';
import { FieldActionDefinition } from '../models/field-action.model';
import { FieldActionService } from '../services/field-action.service';
import { FieldType } from '../contracts/common';
import { MappingModel } from '../models/mapping.model';
import { Multiplicity } from '../contracts/field-action';
import atlasmapFieldActionJson from '../../../../test-resources/fieldActions/atlasmap-field-action.json';
import ky from 'ky';
import log from 'loglevel';

describe('FieldActionService', () => {
  let service: FieldActionService;
  beforeEach(() => {
    service = new FieldActionService(ky);
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
    service = new FieldActionService(ky);
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
    action.sourceType = FieldType.NUMBER;
    action.targetType = FieldType.NUMBER;
    target.type = FieldType.SHORT;
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = FieldType.NUMBER;
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = FieldType.STRING;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return if action target type is ANY_DATE and target field type is a date/time', () => {
    action.sourceType = FieldType.ANY_DATE;
    action.targetType = FieldType.ANY_DATE;
    target.type = FieldType.DATE;
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = FieldType.DATE_TIME_TZ;
    expect(service.appliesToField(action, mapping, false)).toBe(true);
    target.type = FieldType.STRING;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return true if action source type STRING matches target field type STRING, and action target type matches target type', () => {
    source.type = FieldType.STRING;
    target.type = FieldType.STRING;
    action.targetType = FieldType.STRING;
    action.sourceType = FieldType.STRING;
    expect(service.appliesToField(action, mapping, false)).toBe(true);
  });

  test('should return false if action source type CHAR matches target field type STRING, and action target type matches target type', () => {
    target.type = FieldType.STRING;
    action.targetType = FieldType.STRING;
    action.sourceType = FieldType.CHAR;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });

  test('should return false if action source type STRING matches target field type CHAR, and action target type does not match target type', () => {
    target.type = FieldType.CHAR;
    action.targetType = FieldType.STRING;
    action.sourceType = FieldType.STRING;
    expect(service.appliesToField(action, mapping, false)).toBe(false);
  });
});
