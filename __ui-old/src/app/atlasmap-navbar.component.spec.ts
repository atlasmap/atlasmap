/* tslint:disable:no-unused-variable */

import { Component, Input } from '@angular/core';
import { TestBed, async, inject } from '@angular/core/testing';
import { AtlasmapNavbarComponent } from './atlasmap-navbar.component';

describe('AtlasmapNavbarComponent', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [AtlasmapNavbarComponent],
      });
    });

    it(
      'should ...',
      inject([AtlasmapNavbarComponent], (service: AtlasmapNavbarComponent) => {
        expect(service).toBeTruthy();
      }),
    );
  });
