/* tslint:disable:no-unused-variable */

import { TestBed, async, inject, ComponentFixture } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { ClassNameComponent } from './class-name.component';

describe('ClassNameComponent', () => {
  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
    TestBed.configureTestingModule({
      providers: [ClassNameComponent],
    });
  });

  it(
    'should ...',
    inject([ClassNameComponent], (component: ClassNameComponent) => {
      expect(component).toBeTruthy();
    }));
});
