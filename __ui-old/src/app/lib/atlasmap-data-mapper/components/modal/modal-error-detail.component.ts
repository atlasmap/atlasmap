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
import { Component, Input } from '@angular/core';
import { ErrorInfo, ErrorLevel } from '../../models/error.model';
import { ErrorHandlerService } from '../../services/error-handler.service';

@Component({
  selector: 'modal-error-detail',
  templateUrl: './modal-error-detail.component.html',
})
export class ModalErrorDetailComponent {
  @Input() errors: ErrorInfo[];
  @Input() errorService: ErrorHandlerService;

  getErrors(): ErrorInfo[] {
    return this.errors.filter(e => e.level === ErrorLevel.ERROR);
  }

  getWarnings(): ErrorInfo[] {
    return this.errors.filter(e => e.level === ErrorLevel.WARN);
  }

  getInfos(): ErrorInfo[] {
    return this.errors.filter(e => e.level === ErrorLevel.INFO);
  }

  handleAlertClick(error: ErrorInfo) {
    if (error && error.identifier) {
      this.errorService.removeError(error.identifier, error.scope);
    }
  }

}
