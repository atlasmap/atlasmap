/* tslint:disable:no-unused-variable */

import { ChangeDetectorRef } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { ModalWindowComponent, EmptyModalBodyComponent } from './modal-window.component';
import { ModalErrorWindowComponent } from './modal-error-window.component';
import { DataMapperErrorComponent } from '../app/data-mapper-error.component';
import { AlertModule } from 'ngx-bootstrap';
import { ConfigModel } from '../../models/config.model';
import { ErrorHandlerService } from '../../services/error-handler.service';
import { ConstantFieldEditComponent } from '../app/constant-field-edit.component';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { ModalErrorDetailComponent } from './modal-error-detail.component';

describe('ModalWindowComponent', () => {
  let fixture: ComponentFixture<ModalWindowComponent>;
  let comp: ModalWindowComponent;

  beforeEach(() => {
    TestBed.resetTestEnvironment();
    TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
    TestBed.configureTestingModule({
      imports: [AlertModule.forRoot(), FormsModule],
      providers: [
        ChangeDetectorRef,
        ModalWindowComponent,
      ],
      declarations: [
        ConstantFieldEditComponent,
        DataMapperErrorComponent,
        EmptyModalBodyComponent,
        ModalErrorDetailComponent,
        ModalErrorWindowComponent,
        ModalWindowComponent
      ],
    }).overrideModule(BrowserDynamicTestingModule, {
      set: {
        entryComponents: [
          ConstantFieldEditComponent,
          EmptyModalBodyComponent
        ],
      }
    });
    fixture = TestBed.createComponent(ModalWindowComponent);
    comp = fixture.componentInstance;
    comp.cfg = ConfigModel.getConfig();
    comp.cfg.errorService = new ErrorHandlerService();
    fixture.detectChanges();
  });

  it('should be initialized with EmptyModalBodyComponent', (done) => {
    comp.reset();
    comp.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      expect(comp.nestedComponent instanceof EmptyModalBodyComponent).toBeTruthy();
      expect(comp.nestedComponent.isDataValid()).toBeTruthy();
      expect(comp.nestedComponent.getInitialFocusElement()).toBe(undefined);
      done();
    };
    comp.show();
    comp.ngAfterViewInit();
    fixture.detectChanges();
  });

  it('should load ConstantFieldEditComponent', (done) => {
    comp.reset();
    comp.nestedComponentType = ConstantFieldEditComponent;
    comp.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      expect(comp.nestedComponent instanceof ConstantFieldEditComponent).toBeTruthy();
      expect(comp.nestedComponent.isDataValid()).toBeFalsy();
      expect(comp.nestedComponent.getInitialFocusElement().nativeElement.name).toBe('value');
      done();
    };
    comp.show();
    comp.ngAfterViewInit();
    fixture.detectChanges();
  });

  it('should invoke OK button handler', (done) => {
    comp.reset();
    comp.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const okEl = fixture.debugElement.query(By.css('.btn-primary')).nativeElement;
      okEl.click();
    };
    comp.okButtonHandler = (mw: ModalWindowComponent) => {
      expect(comp.nestedComponent instanceof EmptyModalBodyComponent).toBeTruthy();
      expect(comp.nestedComponent.isDataValid()).toBeTruthy();
      expect(comp.nestedComponent.getInitialFocusElement()).toBe(undefined);
      done();
    };
    comp.show();
    comp.ngAfterViewInit();
    fixture.detectChanges();
  });

  it('should invoke cancel button handler', (done) => {
    comp.reset();
    comp.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const cancelEl = fixture.debugElement.query(By.css('.btn-default')).nativeElement;
      cancelEl.click();
    };
    comp.cancelButtonHandler = (mw: ModalWindowComponent) => {
      expect(comp.nestedComponent instanceof EmptyModalBodyComponent).toBeTruthy();
      expect(comp.nestedComponent.isDataValid()).toBeTruthy();
      expect(comp.nestedComponent.getInitialFocusElement()).toBe(undefined);
      done();
    };
    comp.show();
    comp.ngAfterViewInit();
    fixture.detectChanges();
  });

});
