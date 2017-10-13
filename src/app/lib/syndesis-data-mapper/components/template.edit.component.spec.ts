/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { TemplateEditComponent } from './template.edit.component';

describe('TemplateEditComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TemplateEditComponent],
    });
  });

  it(
    'should ...',
    inject([TemplateEditComponent], (service: TemplateEditComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
