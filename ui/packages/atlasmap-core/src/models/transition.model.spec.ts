/* tslint:disable:no-unused-variable */

import {
  TransitionMode,
  TransitionModel,
} from '../../src/models/transition.model';

describe('TransitionModel', () => {
  test('initialize', () => {
    const transition = new TransitionModel();
    expect(TransitionModel.delimiterModels.length > 0);
    expect(transition.mode === TransitionMode.ONE_TO_ONE);
    expect(transition.getPrettyName() === 'One to One');
  });
});
