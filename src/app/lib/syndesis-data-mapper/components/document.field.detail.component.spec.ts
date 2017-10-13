/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { DocumentFieldDetailComponent } from './document.field.detail.component';

describe('DocumentFieldDetailComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DocumentFieldDetailComponent],
    });
  });

  it(
    'should ...',
    inject([DocumentFieldDetailComponent], (service: DocumentFieldDetailComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
