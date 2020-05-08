/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ConfigModel } from './config.model';

describe('ConfigModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConfigModel],
    });
  });

  it(
    'should ...',
    inject([ConfigModel], (service: ConfigModel) => {
      expect(service).toBeTruthy();
    }),
  );
});
