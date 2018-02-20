/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { NamespaceModel, DocumentDefinition  } from './document.definition.model';
import { DocumentType } from '../common/config.types';

describe('DocumentDefinitionModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NamespaceModel,
        DocumentDefinition,
     ],
    });
  });

  it(
    'should ...',
    inject([NamespaceModel, DocumentDefinition],
       (ns: NamespaceModel, doc: DocumentDefinition) => {
      expect(ns).toBeTruthy();
      expect(doc).toBeTruthy();
    }),
  );
});
