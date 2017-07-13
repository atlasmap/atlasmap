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

import { Component } from '@angular/core';

import { ModalWindowValidator } from './modal.window.component';
import { DataMapperUtil } from '../common/data.mapper.util';

@Component({
    selector: 'template-edit',
    template: `
        <div class="DataMapperEditComponent">
            <div class="form-group">
                <textarea [(ngModel)]="templateText" rows="16" cols="100"></textarea>
            </div>
        </div>
    `
})

export class TemplateEditComponent implements ModalWindowValidator {
    public templateText: string = null;

    isDataValid(): boolean { return true; }
}
