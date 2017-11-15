/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ExampleAppModule } from './example.app.module';

describe('ExampleAppModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExampleAppModule],
    });
  });

  it(
    'should ...',
    inject([ExampleAppModule], (service: ExampleAppModule) => {
      expect(service).toBeTruthy();
    }),
  );
});
