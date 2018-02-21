/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ErrorHandlerService } from './error-handler.service';

describe('ErrorHandlerService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ErrorHandlerService],
    });
  });

  it(
    'should ...',
    inject([ErrorHandlerService], (service: ErrorHandlerService) => {
      expect(service).toBeTruthy();
    }),
  );
});
