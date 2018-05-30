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

import { ErrorInfo, ErrorLevel } from '../models/error.model';
import { ErrorHandlerService } from '../services/error-handler.service';
import { ConfigModel } from '../models/config.model';

@Component({
  selector: 'data-mapper-error',
  templateUrl: './data-mapper-error.component.html',
})

export class DataMapperErrorComponent {
  @Input() errorService: ErrorHandlerService;
  @Input() isValidation = false;

  isOpen = true;

  getErrors(): ErrorInfo[] {
    return this.isValidation ? ConfigModel.getConfig().validationErrors.filter(e => e.level >= ErrorLevel.ERROR)
      : ConfigModel.getConfig().errors.filter(e => e.level >= ErrorLevel.ERROR);
  }

  getWarnings(): ErrorInfo[] {
    return this.isValidation ? ConfigModel.getConfig().validationErrors.filter(e => e.level == ErrorLevel.WARN)
      : ConfigModel.getConfig().errors.filter(e => e.level == ErrorLevel.WARN);
  }

  getInfos(): ErrorInfo[] {
    return this.isValidation ? ConfigModel.getConfig().validationErrors.filter(e => e.level == ErrorLevel.INFO)
      : ConfigModel.getConfig().errors.filter(e => e.level == ErrorLevel.INFO);
  }

  handleClick(event: any) {
    const errorIdentifier: string = event.target.attributes.getNamedItem('errorIdentifier').value;
    this.errorService.removeError(errorIdentifier);
  }

  handleAlertClose(info: ErrorInfo): void {
    this.isOpen = true;
    this.errorService.removeError(info.identifier);
  }
}
