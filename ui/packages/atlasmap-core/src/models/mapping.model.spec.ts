/* tslint:disable:no-unused-variable */

import { MappingModel } from '../models/mapping.model';

describe('MappingModel', () => {
  test('initialize', () => {
    const mapping = new MappingModel();
    expect(mapping.uuid).toContain('mapping.');
    expect(mapping.cfg);
  });
});
