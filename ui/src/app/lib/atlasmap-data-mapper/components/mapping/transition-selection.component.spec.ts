/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { TransitionSelectionComponent } from './transition-selection.component';

describe('TransitionSelectionComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TransitionSelectionComponent],
    });
  });

  it(
    'should ...',
    inject([TransitionSelectionComponent], (service: TransitionSelectionComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
