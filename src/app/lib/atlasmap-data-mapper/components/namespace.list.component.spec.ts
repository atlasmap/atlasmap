/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { NamespaceListComponent } from './namespace.list.component';

describe('NamespaceListComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NamespaceListComponent],
    });
  });

  it(
    'should ...',
    inject([NamespaceListComponent], (service: NamespaceListComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
