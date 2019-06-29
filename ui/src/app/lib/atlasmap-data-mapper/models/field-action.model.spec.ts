import { FieldActionDefinition, Multiplicity } from './field-action.model';
import { MappingModel } from './mapping.model';
import { Field } from './field.model';

describe('FieldActionDefinition.appliesToField()', () => {
    let action: FieldActionDefinition;
    let mapping: MappingModel;
    let source: Field;
    let target: Field;
    beforeEach(() => {
      action = new FieldActionDefinition();
      mapping = new MappingModel();
      mapping.sourceFields.splice(0);
      source = new Field();
      mapping.addField(source, true, false);
      mapping.targetFields.splice(0);
      target = new Field();
      mapping.addField(target, false, false);
    });

    it('should return false if FieldMappingPair is null', () => {
      expect(action.appliesToField(null, false)).toBe(false);
    });

    it('should return false if source or target is null', () => {
      mapping.targetFields.splice(0);
      expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return false if action multiplicity is ONE_TO_MANY', () => {
      action.multiplicity = Multiplicity.ONE_TO_MANY;
      expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return false if action multiplicity is MANY_TO_ONE', () => {
      action.multiplicity = Multiplicity.MANY_TO_ONE;
      expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return if action target type is NUMBER and target field type is numeric', () => {
      action.sourceType = 'NUMBER';
      action.targetType = 'NUMBER';
      target.type = 'SHORT';
      expect(action.appliesToField(mapping, false)).toBe(true);
      target.type = 'NUMBER';
      expect(action.appliesToField(mapping, false)).toBe(true);
      target.type = 'STRING';
      expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return if action target type is ANY_DATE and target field type is a date/time', () => {
      action.sourceType = 'ANY_DATE';
      action.targetType = 'ANY_DATE';
      target.type = 'DATE';
      expect(action.appliesToField(mapping, false)).toBe(true);
      target.type = 'DATE_TIME_TZ';
      expect(action.appliesToField(mapping, false)).toBe(true);
      target.type = 'STRING';
      expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return true if action source type STRING matches target field type STRING, and action target type matches target type',
       () => {
      source.type = 'STRING';
      target.type = 'STRING';
      action.targetType = 'STRING';
      action.sourceType = 'STRING';
      expect(action.appliesToField(mapping, false)).toBe(true);
    });

    it('should return false if action source type CHAR matches target field type STRING, and action target type matches target type',
        () => {
       target.type = 'STRING';
       action.targetType = 'STRING';
       action.sourceType = 'CHAR';
       expect(action.appliesToField(mapping, false)).toBe(false);
    });

    it('should return false if action source type STRING matches target field type CHAR, and action target type does not match target type',
        () => {
       target.type = 'CHAR';
       action.targetType = 'STRING';
       action.sourceType = 'STRING';
       expect(action.appliesToField(mapping, false)).toBe(false);
    });

});
