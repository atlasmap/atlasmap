/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingDetailComponent } from './mapping.detail.component';

describe('MappingDetailComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingDetailComponent],
    });
  });

  it(
    'should ...',
    inject([MappingDetailComponent], (service: MappingDetailComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
