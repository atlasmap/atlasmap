/* tslint:disable:no-unused-variable */

import { MappingDefinition } from './mapping-definition.model';

describe('MappingDefinition', () => {
  let mappingDefinition: MappingDefinition;
  const expectedMappingName = 'UI.0.default';
  beforeEach(() => {
    mappingDefinition = new MappingDefinition(0);
  });

  it(
    'should have name UI.0.default',
    () => {
      expect(mappingDefinition.name).toEqual(expectedMappingName);
    }

  );
});
