/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ClassNameComponent } from './class-name.component';

describe('ClassNameComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClassNameComponent],
    });
  });

  it(
    'should ...',
    inject([ClassNameComponent], (service: ClassNameComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
