/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { DataMapperUtil } from './data.mapper.util';

describe('DataMapperUtil', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataMapperUtil],
    });
  });

  it(
    'should ...',
    inject([DataMapperUtil], (service: DataMapperUtil) => {
      expect(service).toBeTruthy();
    }),
  );
});
