/* tslint:disable:no-unused-variable */

import { ChangeDetectorRef } from '@angular/core';
import { TestBed, async, inject } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { LineMachineComponent } from './line-machine.component';

describe('LineMachineComponent', () => {
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
    TestBed.configureTestingModule({
      providers: [
        ChangeDetectorRef,
        LineMachineComponent,
      ],
    });
  });

  it(
    'should ...',
    inject([LineMachineComponent], (service: LineMachineComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
