/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MappingDefinition } from '../src/models/mapping-definition.model';

describe('MappingDefinition', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingDefinition],
    });
  });

  it(
    'should ...',
    inject([MappingDefinition], (service: MappingDefinition) => {
      expect(service).toBeTruthy();
    }),
  );
});
