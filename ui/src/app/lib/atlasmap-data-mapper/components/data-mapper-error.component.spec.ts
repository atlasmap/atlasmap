/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { DataMapperErrorComponent } from './data-mapper-error.component';

describe('DataMapperErrorComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataMapperErrorComponent],
    });
  });

  it(
    'should ...',
    inject([DataMapperErrorComponent], (service: DataMapperErrorComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
