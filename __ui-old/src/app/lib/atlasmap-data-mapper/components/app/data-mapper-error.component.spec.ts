/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { DataMapperErrorComponent } from './data-mapper-error.component';

describe('DataMapperErrorComponent', () => {
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
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
