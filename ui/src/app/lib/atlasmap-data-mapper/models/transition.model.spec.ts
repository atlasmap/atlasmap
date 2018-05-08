/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { TransitionModel, FieldActionConfig } from './transition.model';

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

describe('FieldActionConfig.appliesToField', () => {
    let cfg: FieldActionConfig;
    beforeEach(() => {
      cfg = new FieldActionConfig();
    });

    it('#appliesToField should return false', () => {
      expect(cfg.appliesToField(null)).toBe(false);
    });
});
