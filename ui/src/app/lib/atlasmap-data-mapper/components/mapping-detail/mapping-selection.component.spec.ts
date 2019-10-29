/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingSelectionComponent } from './mapping-selection.component';

describe('MappingSelectionComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingSelectionComponent],
    });
  });

  it(
    'should ...',
    inject([MappingSelectionComponent], (service: MappingSelectionComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
