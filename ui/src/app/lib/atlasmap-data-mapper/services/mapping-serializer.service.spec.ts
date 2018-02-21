/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingSerializer } from './mapping-serializer.service';

describe('MappingSerializer', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingSerializer],
    });
  });

  it(
    'should ...',
    inject([MappingSerializer], (service: MappingSerializer) => {
      expect(service).toBeTruthy();
    }),
  );
});
