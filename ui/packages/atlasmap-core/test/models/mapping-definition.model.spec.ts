/* tslint:disable:no-unused-variable */

import { MappingDefinition } from '../../src/models/mapping-definition.model';

describe('MappingDefinition', () => {

  test('initialize', () => {
    const mappingDef = new MappingDefinition;
    expect(mappingDef.name).toEqual('UI.0');
  });

});
