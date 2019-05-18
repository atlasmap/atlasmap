import { TestBed } from '@angular/core/testing';

import { MappingIdentifierService } from './mapping-identifier.service';

describe('MappingIdentifierService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MappingIdentifierService = TestBed.get(MappingIdentifierService);
    expect(service).toBeTruthy();
  });
});
