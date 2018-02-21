/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingFieldDetailComponent } from './mapping-field-detail.component';

describe('MappingFieldDetailComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingFieldDetailComponent],
    });
  });

  it(
    'should ...',
    inject([MappingFieldDetailComponent], (service: MappingFieldDetailComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
