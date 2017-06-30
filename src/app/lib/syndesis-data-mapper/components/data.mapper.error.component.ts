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
        </div>
    `
})

export class DataMapperErrorComponent {
    @Input() public errorService: ErrorHandlerService;
    @Input() public isValidation: boolean = false;

    private getErrors(): ErrorInfo[] {
        return this.isValidation ? this.errorService.validationErrors : this.errorService.errors;
    }

    private handleClick(event: MouseEvent) {
        // need to extract this so typescript doesnt throw compiler error
        var eventTarget: any = event.target;
        var errorIdentifier: string = eventTarget.attributes.getNamedItem("errorIdentifier").value;
        this.errorService.removeError(errorIdentifier);
    }
}

