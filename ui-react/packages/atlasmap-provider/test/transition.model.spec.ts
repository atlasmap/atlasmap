/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { TransitionModel } from '../src/models/transition.model';

describe('TransitionModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TransitionModel],
    });
  });

  it(
    'should ...',
    inject([TransitionModel], (service: TransitionModel) => {
      expect(service).toBeTruthy();
    }),
  );
});
