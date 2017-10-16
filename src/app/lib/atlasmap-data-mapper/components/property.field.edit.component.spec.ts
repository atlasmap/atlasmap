/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { PropertyFieldEditComponent } from './property.field.edit.component';

describe('PropertyFieldEditComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PropertyFieldEditComponent],
    });
  });

  it(
    'should ...',
    inject([PropertyFieldEditComponent], (service: PropertyFieldEditComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
