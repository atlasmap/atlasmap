/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { NamespaceModel, DocumentType, DocumentInitializationConfig, DocumentDefinition  } from './document.definition.model';

describe('DocumentDefinitionModel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NamespaceModel,
        DocumentType,
        DocumentInitializationConfig,
        DocumentDefinition,
     ],
    });
  });

  it(
    'should ...',
    inject([NamespaceModel, DocumentType, DocumentInitializationConfig, DocumentDefinition],
       (ns: NamespaceModel, docType: DocumentType, initCfg: DocumentInitializationConfig, doc: DocumentDefinition) => {
      expect(ns).toBeTruthy();
      expect(docType).toBeTruthy();
      expect(initCfg).toBeTruthy();
      expect(doc).toBeTruthy();
    }),
  );
});
