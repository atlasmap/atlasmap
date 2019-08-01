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
  Component, Input, ViewChildren, QueryList, OnDestroy, OnInit,
  ViewContainerRef, Type, ComponentFactoryResolver, AfterViewInit, ChangeDetectorRef, ElementRef
} from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { Subscription } from 'rxjs';
import { ErrorInfo, ErrorLevel } from '../models/error.model';

export interface ModalErrorWindowValidator {
  isDataValid(): boolean;
  getInitialFocusElement(): ElementRef;
}

@Component({
  selector: 'modal-error-window',
  templateUrl: './modal-error-window.component.html',
})

export class ModalErrorWindowComponent implements OnInit {
  @Input() headerText = '';
  @Input() nestedComponentType: Type<any>;
  @Input() nestedComponentInitializedCallback: Function;
  @Input() okButtonHandler: Function;
  @Input() cancelButtonHandler: Function;
  @Input() modalErrorWindow: ModalErrorWindowComponent;

  cfg: ConfigModel = null;
  message: string = null;
  nestedComponent: ModalErrorWindowValidator;
  confirmButtonDisabled = false;
  buttonText = 'Dismiss All';
  visible = false;
  fade = false;

  @ViewChildren('dyn_target', { read: ViewContainerRef }) myTarget: QueryList<ViewContainerRef>;

  private componentLoaded = false;
  private myTargetChangesSubscription: Subscription;

  constructor(private componentFactoryResolver: ComponentFactoryResolver, public detector: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.cfg = ConfigModel.getConfig();
  }

  close(): void {
    this.fade = false;
    setTimeout(() => {
      this.visible = false;
    }, 300);
  }

  somethingToShow(): boolean {
    return (this.cfg.errors.length > 0);
  }

  show(): void {
    this.headerText = 'Errors and Warnings - Current Active Mapping';
    this.message = '';
    this.visible = true;
    setTimeout(() => {
      this.fade = true;
    }, 100);
  }

  reset(): void {
    this.message = 'Hello!';
    this.headerText = '';
    this.componentLoaded = false;
    this.okButtonHandler = null;
    this.cancelButtonHandler = null;
  }

  getErrors(): ErrorInfo[] {
    return this.cfg.errors.filter(e => e.level >= ErrorLevel.ERROR);
  }

  getWarnings(): ErrorInfo[] {
    return this.cfg.errors.filter(e => e.level === ErrorLevel.WARN);
  }

  handleAlertClick(event: any) {
    const errorIdentifier = event.target.attributes.getNamedItem('errorIdentifier');
    if (errorIdentifier && errorIdentifier.value) {
      if (this.cfg.mappings.activeMapping) {
        this.cfg.mappings.activeMapping.removeValidationError(errorIdentifier.value);
      }
      this.cfg.errorService.removeError(errorIdentifier.value);
    }
    if (this.getErrors().length === 0 && this.getWarnings().length === 0) {
      this.close();
    }
  }

  dismissAll(): void {
    for (const e of this.getErrors()) {
      if (this.cfg.mappings && this.cfg.mappings.activeMapping) {
        this.cfg.mappings.activeMapping.removeValidationError(e.identifier);
      }
      this.cfg.errorService.removeError(e.identifier);
    }
    for (const w of this.getWarnings()) {
      if (this.cfg.mappings && this.cfg.mappings.activeMapping) {
        this.cfg.mappings.activeMapping.removeValidationError(w.identifier);
      }
      this.cfg.errorService.removeError(w.identifier);
    }
    this.close();
  }
}
