/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MappingManagementService } from './mapping-management.service';

describe('MappingManagementService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientModule, HttpClientTestingModule ],
      providers: [
        MappingManagementService,
      ],
    });
  });

  it(
    'should ...',
    inject([MappingManagementService], (service: MappingManagementService) => {
      expect(service).toBeTruthy();
    }),
  );
});
