/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { DataMapperModule } from './data-mapper.module';

describe('DataMapperModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DataMapperModule],
    });
  });

  it(
    'should ...',
    inject([DataMapperModule], (service: DataMapperModule) => {
      expect(service).toBeTruthy();
    }),
  );
});
