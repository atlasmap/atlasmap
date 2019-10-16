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

import { Component, Input, OnInit, OnDestroy } from '@angular/core';

import { ModalErrorWindowComponent } from './modal-error-window.component';

import { ErrorInfo, ErrorLevel } from '../models/error.model';
import { ErrorHandlerService } from '../services/error-handler.service';
import { ConfigModel } from '../models/config.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'data-mapper-error',
  templateUrl: './data-mapper-error.component.html',
})

export class DataMapperErrorComponent implements OnInit, OnDestroy {
  @Input() errorService: ErrorHandlerService;
  @Input() modalErrorWindow: ModalErrorWindowComponent;

  isOpen = true;
  cfg: ConfigModel = null;

  private elem = null;
  private mouseEventTimer = null;
  private errors: ErrorInfo[] = [];
  private errorSubscription: Subscription;

  ngOnInit() {
    this.cfg = ConfigModel.getConfig();
    this.errorSubscription = this.errorService.subscribe((errors: ErrorInfo[]) => {
      this.errors = errors;
    });
  }

  ngOnDestroy() {
    if (this.errorSubscription) {
      this.errorSubscription.unsubscribe();
    }
  }

  /**
   * Return true if an error window is necessary, false otherwise.
   */
  errorServiceRequired(): boolean {
    return (this.errorService
      && (ErrorHandlerService.filterWith(this.errors, this.cfg.mappings ? this.cfg.mappings.activeMapping : null).length > 0));
  }

  getErrors(): ErrorInfo[] {
    return ErrorHandlerService.filterWith(this.errors, this.cfg.mappings.activeMapping, ErrorLevel.ERROR);
  }

  getWarnings(): ErrorInfo[] {
    return ErrorHandlerService.filterWith(this.errors, this.cfg.mappings.activeMapping, ErrorLevel.WARN);
  }

  getInfos(): ErrorInfo[] {
    return ErrorHandlerService.filterWith(this.errors, this.cfg.mappings.activeMapping, ErrorLevel.INFO);
  }

  handleAlertClose(e: ErrorInfo): void {
    this.isOpen = true;
    this.errorService.removeError(e.identifier, e.scope);
  }

  /**
   * Handle the event of a user mousing over the error window.  If they stay within the window
   * for a half-second then the active errors modal will show all of the errors/ warnings.
   *
   * @param evt1
   */
  handleMouseEnter(evt1: MouseEvent): void {
    this.mouseEventTimer =  setTimeout(() => {
        if (this.elem != null) {
            evt1.stopPropagation();
            evt1.preventDefault();
          }
      this.showActiveErrors();
    }, 500);
  }

  /**
   * Handle the event of a user mousing out of the error window.  Disarm the event timer if that case.
   *
   * @param evt1
   */
  handleMouseLeave(evt1: MouseEvent): void {
    if (this.mouseEventTimer) {
      clearTimeout(this.mouseEventTimer);
    }
    this.mouseEventTimer = null;
  }

  /**
   * Show all errors/ warnings in a separate modal window.
   */
  private showActiveErrors(): void {
    this.modalErrorWindow.reset();
    this.modalErrorWindow.setErrors(ErrorHandlerService.filterWith(this.errors, this.cfg.mappings.activeMapping));
    this.modalErrorWindow.show();
  }

  /**
   * The fixed error window only needs to show one error.  The full collection of errors is
   * available from the error modal window.
   */
  getFirstError(): ErrorInfo {
    return this.getErrors()[0];
  }

  /**
   * The fixed error window only needs to show one warning.  The full collection of warnings is
   * available from the error modal window.
   */
  getFirstWarning(): ErrorInfo {
    return this.getWarnings()[0];
  }

}
