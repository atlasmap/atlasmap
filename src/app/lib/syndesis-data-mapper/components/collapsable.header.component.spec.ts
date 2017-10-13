/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { CollapsableHeaderComponent } from './collapsable.header.component';

describe('CollapsableHeaderComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CollapsableHeaderComponent],
    });
  });

  it(
    'should ...',
    inject([CollapsableHeaderComponent], (service: CollapsableHeaderComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
