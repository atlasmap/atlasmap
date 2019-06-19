/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { NGXLogger} from 'ngx-logger';
import { ConstantFieldEditComponent } from './constant-field-edit.component';

describe('ConstantFieldEditComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConstantFieldEditComponent, NGXLogger],
    });
  });

  it(
    'should ...',
    inject([ConstantFieldEditComponent], (service: ConstantFieldEditComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
