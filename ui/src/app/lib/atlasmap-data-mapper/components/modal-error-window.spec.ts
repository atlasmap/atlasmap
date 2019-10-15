/* tslint:disable:no-unused-variable */

import { ChangeDetectorRef, QueryList, ViewContainerRef } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ModalErrorWindowComponent } from './modal-error-window.component';
import { DataMapperErrorComponent } from './data-mapper-error.component';
import { AlertModule } from 'ngx-bootstrap';
import { ConfigModel } from '../models/config.model';
import { ErrorHandlerService } from '../services/error-handler.service';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { ConstantFieldEditComponent } from './constant-field-edit.component';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';

describe('ModalErrorWindowComponent', () => {
  let fixture: ComponentFixture<ModalErrorWindowComponent>;
  let comp: ModalErrorWindowComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AlertModule.forRoot(), FormsModule],
      providers: [
        ChangeDetectorRef,
        ModalErrorWindowComponent,
      ],
      declarations: [
        ConstantFieldEditComponent,
        DataMapperErrorComponent,
        ModalErrorWindowComponent
      ],
    }).overrideModule(BrowserDynamicTestingModule, {
      set: {
        entryComponents: [
          ConstantFieldEditComponent
        ],
      }
    });
    fixture = TestBed.createComponent(ModalErrorWindowComponent);
    comp = fixture.componentInstance;
    comp.cfg = new ConfigModel();
    comp.cfg.errorService = new ErrorHandlerService();
    fixture.detectChanges();
  });
});
