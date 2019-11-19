/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingModel } from '../src/models/mapping.model';

describe('MappingModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingModel],
    });
  });

  it(
    'should ...',
    inject([MappingModel], (service: MappingModel) => {
      expect(service).toBeTruthy();
    }),
  );
});
