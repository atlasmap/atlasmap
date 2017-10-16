/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { FieldEditComponent } from './field.edit.component';

describe('FieldEditComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FieldEditComponent],
    });
  });

  it(
    'should ...',
    inject([FieldEditComponent], (service: FieldEditComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
