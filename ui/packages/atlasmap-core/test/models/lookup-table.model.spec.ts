/* tslint:disable:no-unused-variable */

import { LookupTableEntry, LookupTable } from '../../src/models/lookup-table.model';

describe('LookupTable', () => {

  test('initialize', () => {
    const entry = new LookupTableEntry;
    entry.sourceType = 'sType';
    entry.sourceValue = 'sValue';
    entry.targetType = 'tType';
    entry.targetValue = 'tValue';
    const table = new LookupTable;
    expect(table.name);
    table.entries.push(entry);
    expect(table.toString()).toContain('Entry #0: sValue => tValue');
  });

});
