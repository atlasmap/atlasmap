/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoggerModule, NGXLogger, NgxLoggerLevel } from 'ngx-logger';
import { DataMapperAppExampleHostComponent } from './data-mapper-example-host.component';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { InitializationService } from '../services/initialization.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { FieldActionService } from '../services/field-action.service';
import { FileManagementService } from '../services/file-management.service';
import {MappingIdentifierService} from '../services/mapping-identifier.service';
import {ActivatedRoute} from '@angular/router';

describe('DataMapperAppExampleHostComponent', () => {
  const testActivatedRoute = ({
    data: ({
      id: 0
    })
  } as any) as ActivatedRoute;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule, LoggerModule.forRoot({level: NgxLoggerLevel.DEBUG})],
      providers: [
        DataMapperAppExampleHostComponent,
        DocumentManagementService,
        ErrorHandlerService,
        FieldActionService,
        FileManagementService,
        InitializationService,
        MappingManagementService,
        NGXLogger,
        MappingIdentifierService,
        {provide: ActivatedRoute, useValue: testActivatedRoute}
      ],
    });
  });

  it(
    'should ...',
    inject([DataMapperAppExampleHostComponent], (service: DataMapperAppExampleHostComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
