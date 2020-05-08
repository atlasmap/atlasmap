/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { Field } from './field.model';

describe('Field', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Field],
    });
  });

  it(
    'should ...',
    inject([Field], (service: Field) => {
      expect(service).toBeTruthy();
    }),
  );
});
