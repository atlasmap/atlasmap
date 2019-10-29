/* tslint:disable:no-unused-variable */

import { ChangeDetectorRef } from '@angular/core';
import { TestBed, async, inject } from '@angular/core/testing';
import { DataMapperAppComponent } from './data-mapper-app.component';

describe('DataMapperAppComponent', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChangeDetectorRef,
        DataMapperAppComponent,
      ],
    });
  });

  it(
    'should ...',
    inject([DataMapperAppComponent], (service: DataMapperAppComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
