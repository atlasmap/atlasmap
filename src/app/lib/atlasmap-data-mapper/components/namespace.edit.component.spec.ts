/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { NamespaceEditComponent } from './namespace.edit.component';

describe('NamespaceEditComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NamespaceEditComponent],
    });
  });

  it(
    'should ...',
    inject([NamespaceEditComponent], (service: NamespaceEditComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
