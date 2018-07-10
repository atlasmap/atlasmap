/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { FieldActionConfig, TransitionModel } from './transition.model';
import { FieldMappingPair } from './mapping.model';
import { Field } from './field.model';

describe('TransitionModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TransitionModel],
    });
  });

  it(
    'should ...',
    inject([TransitionModel], (service: TransitionModel) => {
      expect(service).toBeTruthy();
    }),
  );
});

describe('FieldActionConfig.appliesToField()', () => {
    let action: FieldActionConfig;
    let pair: FieldMappingPair;
    let source: Field;
    let target: Field;
    beforeEach(() => {
      action = new FieldActionConfig();
      pair = new FieldMappingPair();
      pair.sourceFields.splice(0);
      source = new Field();
      pair.addField(source, true);
      pair.targetFields.splice(0);
      target = new Field();
      pair.addField(target, false);
    });

    it('should return false if FieldMappingPair is null', () => {
      expect(action.appliesToField(null, false)).toBe(false);
    });

    it('should return false if source or target is null', () => {
      pair.targetFields.splice(0);
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return false if action collection type is ALL, target collection type is not ARRAY or LIST, and type type is not \
       STRING', () => {
      action.serviceObject.sourceCollectionType = 'ALL';
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return false if action collection type is NONE and target field collection type is not null', () => {
      action.serviceObject.sourceCollectionType = 'NONE';
      target.isCollection = true;
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return false if action source collection type does not match target collection type', () => {
      action.serviceObject.sourceCollectionType = 'LIST';
      action.serviceObject.targetCollectionType = 'NONE';
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return if action target type is NUMBER and target field type is numeric', () => {
      action.targetType = 'NUMBER';
      target.type = 'SHORT';
      expect(action.appliesToField(pair, false)).toBe(true);
      target.type = 'NUMBER';
      expect(action.appliesToField(pair, false)).toBe(true);
      target.type = 'STRING';
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return if action target type is ANY_DATE and target field type is a date/time', () => {
      action.targetType = 'ANY_DATE';
      target.type = 'DATE';
      expect(action.appliesToField(pair, false)).toBe(true);
      target.type = 'DATE_TIME_TZ';
      expect(action.appliesToField(pair, false)).toBe(true);
      target.type = 'STRING';
      expect(action.appliesToField(pair, false)).toBe(false);
    });

    it('should return if action source type matches target field type, and action target type matches target type',
       () => {
      target.type = 'STRING';
      action.targetType = 'STRING';
      action.sourceType = 'STRING';
      expect(action.appliesToField(pair, false)).toBe(true);
      action.sourceType = 'CHAR';
      expect(action.appliesToField(pair, false)).toBe(false);
      action.sourceType = 'STRING';
      target.type = 'CHAR';
      expect(action.appliesToField(pair, false)).toBe(false);
    });
});
