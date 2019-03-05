/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import {
  Component, Input, ViewChildren, QueryList, OnDestroy,
  ViewContainerRef, Type, ComponentFactoryResolver, AfterViewInit, ChangeDetectorRef, ElementRef
} from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { Subscription } from 'rxjs';

// source: http://www.w3schools.com/howto/howto_css_modals.asp

export interface ModalWindowValidator {
  isDataValid(): boolean;
  getInitialFocusElement(): ElementRef;
}

@Component({
  selector: 'empty-modal-body',
  template: '',
})

export class EmptyModalBodyComponent { }

@Component({
  selector: 'modal-window',
  templateUrl: './modal-window.component.html',
})

export class ModalWindowComponent implements AfterViewInit, OnDestroy {
  @Input() headerText = '';
  @Input() nestedComponentType: Type<any>;
  @Input() nestedComponentInitializedCallback: Function;
  @Input() okButtonHandler: Function;
  @Input() cancelButtonHandler: Function;
  @Input() cfg: ConfigModel;

  message: string = null;
  nestedComponent: ModalWindowValidator;
  confirmButtonDisabled = false;
  confirmButtonText = 'OK';
  visible = false;
  fade = false;

  @ViewChildren('dyn_target', { read: ViewContainerRef }) myTarget: QueryList<ViewContainerRef>;

  private componentLoaded = false;
  private myTargetChangesSubscription: Subscription;

  constructor(private componentFactoryResolver: ComponentFactoryResolver, public detector: ChangeDetectorRef) { }

  ngAfterViewInit() {
    if (this.myTargetChangesSubscription) {
      this.myTargetChangesSubscription.unsubscribe();
    }

    // from: http://stackoverflow.com/questions/40811809/add-component-dynamically-inside-an-ngif
    this.myTargetChangesSubscription = this.myTarget.changes.subscribe(changes => {
      setTimeout(() => {
        if (!this.componentLoaded && this.visible && this.myTarget && (this.myTarget.toArray().length)) {
          this.loadComponent();
        }
        setTimeout(() => {
          this.detector.detectChanges();
        }, 10);
      }, 10);
    });
  }

  ngOnDestroy() {
    this.myTargetChangesSubscription.unsubscribe();
  }

  loadComponent(): void {
    const viewContainerRef: ViewContainerRef = this.myTarget.toArray()[0];
    viewContainerRef.clear();
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.nestedComponentType);
    this.nestedComponent = viewContainerRef.createComponent(componentFactory).instance as ModalWindowValidator;
    if (this.nestedComponentInitializedCallback != null) {
      this.nestedComponentInitializedCallback(this);
    }
    const initialFocusElement = this.nestedComponent.getInitialFocusElement();
    if (initialFocusElement) {
      initialFocusElement.nativeElement.focus();
    }
  }

  closeClicked(event: MouseEvent): void { this.buttonClicked(false); }
  close(): void {
    this.fade = false;
    setTimeout(() => {
      this.visible = false;
    }, 300);
  }
  show(): void {
    this.visible = true;
    setTimeout(() => {
      this.fade = true;
    }, 100);
  }

  reset(): void {
    this.cfg.errorService.clearValidationErrors();
    this.nestedComponentInitializedCallback = null;
    this.confirmButtonDisabled = false;
    this.confirmButtonText = 'OK';
    this.message = '';
    this.headerText = '';
    this.componentLoaded = false;
    this.nestedComponentType = EmptyModalBodyComponent;
    this.okButtonHandler = null;
    this.cancelButtonHandler = null;
  }

  private buttonClicked(okClicked: boolean): void {
    if (okClicked) {
      if (this.nestedComponent != null) {
        this.cfg.errorService.clearValidationErrors();
        if (!(this.nestedComponent.isDataValid())) {
          return;
        }
      }
      if (this.okButtonHandler) {
        this.okButtonHandler(this);
      }
    } else { // cancel clicked
      if (this.cancelButtonHandler) {
        this.cancelButtonHandler(this);
      }
    }
    this.close();
  }

}
