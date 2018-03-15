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
  Component, Input, ViewChildren, QueryList,
  ViewContainerRef, Type, ComponentFactoryResolver, AfterViewInit, ChangeDetectorRef
} from '@angular/core';
import { ConfigModel } from '../models/config.model';

// source: http://www.w3schools.com/howto/howto_css_modals.asp

export interface ModalWindowValidator {
  isDataValid(): boolean;
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

export class ModalWindowComponent implements AfterViewInit {
  @Input() headerText = '';
  @Input() nestedComponentType: Type<any>;
  @Input() nestedComponentInitializedCallback: Function;
  @Input() okButtonHandler: Function;
  @Input() cancelButtonHandler: Function;
  @Input() cfg: ConfigModel;

  message: string = null;
  nestedComponent: Component;
  confirmButtonDisabled = false;
  confirmButtonText = 'OK';
  visible = false;

  @ViewChildren('dyn_target', { read: ViewContainerRef }) myTarget: QueryList<ViewContainerRef>;

  private componentLoaded = false;

  constructor(private componentFactoryResolver: ComponentFactoryResolver, public detector: ChangeDetectorRef) { }

  ngAfterViewInit() {
    //from: http://stackoverflow.com/questions/40811809/add-component-dynamically-inside-an-ngif
    this.myTarget.changes.subscribe(changes => {
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

  loadComponent(): void {
    const viewContainerRef: ViewContainerRef = this.myTarget.toArray()[0];
    viewContainerRef.clear();
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.nestedComponentType);
    this.nestedComponent = viewContainerRef.createComponent(componentFactory).instance;
    if (this.nestedComponentInitializedCallback != null) {
      this.nestedComponentInitializedCallback(this);
    }
  }

  closeClicked(event: MouseEvent): void { this.buttonClicked(false); }
  close(): void { this.visible = false; }
  show(): void {
    this.visible = true;
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
      const anyComponent: any = this.nestedComponent;
      if ((anyComponent != null) && (anyComponent.isDataValid)) {
        this.cfg.errorService.clearValidationErrors();
        if (!(anyComponent.isDataValid())) {
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
