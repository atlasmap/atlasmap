/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LookupTableEntry, LookupTable } from './lookup.table.model';

describe('LookupTable', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LookupTableEntry, LookupTable],
    });
  });

  it(
    'should ...',
    inject([LookupTableEntry, LookupTable], (entry : LookupTableEntry, table: LookupTable) => {
      expect(entry).toBeTruthy();
      expect(table).toBeTruthy();
    }),
  );
});
