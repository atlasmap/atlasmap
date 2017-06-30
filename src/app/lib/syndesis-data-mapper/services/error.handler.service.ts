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

@Injectable()
export class ErrorHandlerService {
    public errors : ErrorInfo[] = [];
    public validationErrors : ErrorInfo[] = [];

    public debug(message: string, error: any) { this.addError(message, ErrorLevel.DEBUG, error); }
    public info(message: string, error: any) { this.addError(message, ErrorLevel.INFO, error); }
    public warn(message: string, error: any) { this.addError(message, ErrorLevel.WARN, error); }
    public error(message: string, error: any) { this.addError(message, ErrorLevel.ERROR, error); }
    public validationError(message: string, error: any) { this.addError(message, ErrorLevel.VALIDATION_ERROR, error); }

    private addError(message: string, level: ErrorLevel, error:any): void {
        if (level == ErrorLevel.ERROR) {
            console.error(message, error);
        }
        var e: ErrorInfo = new ErrorInfo();
        e.message = message;
        e.level = level;
        e.error = error;
        if (level == ErrorLevel.VALIDATION_ERROR) {
            this.validationErrors.push(e);
        } else {
            this.errors.push(e);
        }
    }

    public removeError(identifier: string): void {
        for (var i = 0; i < this.errors.length; i++) {
            if (this.errors[i].identifier == identifier) {
                this.errors.splice(i, 1);
                return;
            }
        }
        for (var i = 0; i < this.validationErrors.length; i++) {
            if (this.validationErrors[i].identifier == identifier) {
                this.validationErrors.splice(i, 1);
                return;
            }
        }
    }

    public clearValidationErrors(): void {
        this.validationErrors = [];
    }
}
