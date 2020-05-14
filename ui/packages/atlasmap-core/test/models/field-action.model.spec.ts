import { FieldActionDefinition, FieldAction } from '../../src/models/field-action.model';

describe('FieldActionModel', () => {

  test('FieldAction.create()', () => {
    const actionDefinition = new FieldActionDefinition;
    actionDefinition.name = 'dummy';
    const action = FieldAction.create(actionDefinition);
    expect(action).toBeTruthy();
    expect(action.name).toEqual(actionDefinition.name);
    expect(action.definition).toEqual(actionDefinition);
  });

});
