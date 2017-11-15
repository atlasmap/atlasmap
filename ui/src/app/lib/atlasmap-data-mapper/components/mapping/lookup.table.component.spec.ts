/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { LookupTableComponent } from './lookup.table.component';

describe('LookupTableComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LookupTableComponent],
    });
  });

  it(
    'should ...',
    inject([LookupTableComponent], (service: LookupTableComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
