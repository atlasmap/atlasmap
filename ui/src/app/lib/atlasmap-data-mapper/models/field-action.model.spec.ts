import { FieldActionDefinition, FieldAction } from './field-action.model';

describe('FieldAction.create()', () => {
  let actionDefinition: FieldActionDefinition;
  beforeEach(() => {
    actionDefinition = new FieldActionDefinition();
    actionDefinition.name = 'dummy';
  });

  it('should return a FieldAction', () => {
    const action = FieldAction.create(actionDefinition);
    expect(action).toBeTruthy();
    expect(action.name).toEqual(actionDefinition.name);
    expect(action.definition).toEqual(actionDefinition);
  });

});
