/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingListComponent } from './mapping-list.component';

describe('MappingListComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingListComponent],
    });
  });

  it(
    'should ...',
    inject([MappingListComponent], (service: MappingListComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
