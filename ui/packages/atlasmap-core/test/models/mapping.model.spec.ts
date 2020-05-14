/* tslint:disable:no-unused-variable */

import { MappingModel } from '../../src/models/mapping.model';

describe('MappingModel', () => {

  test('initialize', () => {
    const mapping = new MappingModel;
    expect(mapping.uuid).toContain('mapping.')
    expect(mapping.cfg);
  });

});
