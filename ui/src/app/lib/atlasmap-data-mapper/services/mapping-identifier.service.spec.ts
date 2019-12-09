import { TestBed } from '@angular/core/testing';

import { MappingIdentifierService } from './mapping-identifier.service';
import {ActivatedRoute} from '@angular/router';

describe('MappingIdentifierService', () => {
  const testActivatedRoute = ({
    paramMap: ({
      id: 0
    })
  } as any) as ActivatedRoute;

  const testMappingIdentifierService = new MappingIdentifierService(testActivatedRoute);

  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      {provide: MappingIdentifierService, useValue: testMappingIdentifierService},
      {provide: ActivatedRoute, useValue: testActivatedRoute}
    ],
  }));

  it('should be created', () => {

    expect(testMappingIdentifierService).toBeTruthy();
  });
});
