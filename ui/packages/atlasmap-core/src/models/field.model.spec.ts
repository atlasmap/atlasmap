/* tslint:disable:no-unused-variable */

import { Field } from '../models/field.model';

describe('Field', () => {
  test('initialize', () => {
    const f = new Field();
    expect(f.uuid);
  });
});
