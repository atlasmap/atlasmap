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
  Component, ViewChildren, QueryList, OnInit,
  ViewContainerRef, ChangeDetectorRef, ElementRef
} from '@angular/core';
import { ConfigModel } from '../models/config.model';
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
  cfg: ConfigModel = null;
  message: string = null;
  nestedComponent: ModalErrorWindowValidator;
  confirmButtonDisabled = false;
  headerText = 'Errors and Warnings - Current Active Mapping';
  buttonText = 'Dismiss All';
  visible = false;
  fade = false;
  errors: ErrorInfo[];

  @ViewChildren('dyn_target', { read: ViewContainerRef }) myTarget: QueryList<ViewContainerRef>;

  constructor(public detector: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.cfg = ConfigModel.getConfig();
  }

  setErrors(errors: ErrorInfo[]) {
    this.errors = errors;
    if (!this.errors || this.errors.length === 0) {
      this.close();
    }
  }

  close(): void {
    this.fade = false;
    setTimeout(() => {
      this.visible = false;
    }, 300);
  }

  somethingToShow(): boolean {
    return (this.errors && this.errors.length > 0);
  }

  show(): void {
    this.message = '';
    this.visible = true;
    setTimeout(() => {
      this.fade = true;
    }, 100);
  }

  reset(): void {
    this.message = 'Hello!';
  }

  getErrors(): ErrorInfo[] {
    return this.errors.filter(e => e.level === ErrorLevel.ERROR);
  }

  getWarnings(): ErrorInfo[] {
    return this.errors.filter(e => e.level === ErrorLevel.WARN);
  }

  dismissAll(): void {
    this.cfg.errorService.clearAllErrors();
    this.close();
  }
}
