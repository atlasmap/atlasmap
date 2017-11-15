/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ToolbarComponent } from './toolbar.component';

describe('ToolbarComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToolbarComponent],
    });
  });

  it(
    'should ...',
    inject([ToolbarComponent], (service: ToolbarComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
