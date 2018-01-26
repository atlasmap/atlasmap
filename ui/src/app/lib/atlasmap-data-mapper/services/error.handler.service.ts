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

import { Injectable } from '@angular/core';

import { ErrorInfo, ErrorLevel } from '../models/error.model';
import { ConfigModel } from '../models/config.model';

@Injectable()
export class ErrorHandlerService {
    cfg: ConfigModel = null;

    debug(message: string, error: any) { this.addError(message, ErrorLevel.DEBUG, error); }
    info(message: string, error: any) { this.addError(message, ErrorLevel.INFO, error); }
    warn(message: string, error: any) { this.addError(message, ErrorLevel.WARN, error); }
    error(message: string, error: any) { this.addError(message, ErrorLevel.ERROR, error); }
    validationError(message: string, error: any) { this.addValidationError(message, error); }

    removeError(identifier: string): void {
        this.cfg.errors = this.cfg.errors.filter(e => e.identifier !== identifier);
        this.cfg.validationErrors = this.cfg.validationErrors.filter(e => e.identifier !== identifier);
    }

    clearValidationErrors(): void {
        this.cfg.validationErrors = [];
    }

    private addError(message: string, level: ErrorLevel, error: any): void {
        if (this.arrayDoesNotContainError(message)) {
            const e = new ErrorInfo(message, level, error);
            this.cfg.errors.push(e);
        }
    }

    private arrayDoesNotContainError(message: string) {
        return this.cfg.errors.filter(e => e.message === message).length === 0;
    }

    private addValidationError(message: string, error: any): void {
        const e = new ErrorInfo(message, ErrorLevel.VALIDATION_ERROR, error);
        this.cfg.validationErrors.push(e);
    }

}
