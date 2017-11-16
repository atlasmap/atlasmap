/* tslint:disable:no-unused-variable */

import { ChangeDetectorRef } from '@angular/core';
import { TestBed, async, inject } from '@angular/core/testing';
import { ModalWindowComponent } from './modal.window.component';

describe('ModalWindowComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChangeDetectorRef,
        ModalWindowComponent,
      ],
    });
  });

  it(
    'should ...',
    inject([ModalWindowComponent], (service: ModalWindowComponent) => {
      expect(service).toBeTruthy();
    }),
  );
});
