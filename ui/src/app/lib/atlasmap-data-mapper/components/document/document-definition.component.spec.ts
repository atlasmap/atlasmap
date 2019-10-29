/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { DocumentDefinitionComponent } from './document-definition.component';

describe('DocumentDefinitionComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DocumentDefinitionComponent],
    });
  });

  it(
    'should ...',
    inject([DocumentDefinitionComponent], (service: DocumentDefinitionComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
