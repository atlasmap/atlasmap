/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AppComponent],
    });
  });

  it(
    'should ...',
    inject([AppComponent], (service: AppComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});