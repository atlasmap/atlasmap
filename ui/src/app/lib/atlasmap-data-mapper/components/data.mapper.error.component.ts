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
import { ErrorHandlerService } from '../services/error.handler.service';
import { ConfigModel } from '../models/config.model';

@Component({
    selector: 'data-mapper-error',
    template: `
        <div class="DataMapperErrorComponent" *ngIf="errorService && getErrors().length">
            <div class="alert alert-danger" *ngFor="let e of getErrors()">
                <a class="close" (click)="handleClick($event)">
                    <i class="fa fa-close" attr.errorIdentifier="{{e.identifier}}"></i>
                </a>
                <span class="pficon pficon-error-circle-o"></span>
                {{e.message}}
            </div>
            <div class="alert alert-warning" *ngFor="let w of getWarnings()">
                <a class="close" (click)="handleClick($event)">
                    <i class="fa fa-close" attr.errorIdentifier="{{w.identifier}}"></i>
                </a>
                <span class="pficon pficon-warning-triangle-o"></span>
                {{w.message}}
            </div>
        </div>
    `
})

export class DataMapperErrorComponent {
    @Input() public errorService: ErrorHandlerService;
    @Input() public isValidation: boolean = false;

    public getErrors(): ErrorInfo[] {
        let test : ErrorInfo[] = ConfigModel.getConfig().validationErrors;
        return this.isValidation ? ConfigModel.getConfig().validationErrors.filter(e => e.level >= ErrorLevel.ERROR) : ConfigModel.getConfig().errors;
    }

    public getWarnings(): ErrorInfo[] {
        return this.isValidation ? ConfigModel.getConfig().validationErrors.filter(e => e.level === ErrorLevel.WARN) : ErrorInfo[0];
    }

    public handleClick(event: any) {
        var errorIdentifier: string = event.target.attributes.getNamedItem("errorIdentifier").value;
        this.errorService.removeError(errorIdentifier);
    }
}

