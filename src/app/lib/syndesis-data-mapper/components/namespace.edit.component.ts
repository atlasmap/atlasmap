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

import { NamespaceModel } from '../models/document.definition.model';
import { ConfigModel } from '../models/config.model';
import { ModalWindowValidator } from './modal.window.component';
import { DataMapperUtil } from '../common/data.mapper.util';

@Component({
    selector: 'namespace-edit',
    template: `
        <div class="PropertyEditFieldComponent">
            <div class="form-group">
                <label>Alias</label>
                <input type="text" [(ngModel)]="namespace.alias" disabled="{{namespace.isTarget || !namespace.createdByUser}}">
            </div>
            <div class="form-group">
                <label>URI</label>
                <input type="text" [(ngModel)]="namespace.uri"/>
            </div>
            <div class="form-group">
                <label>Location URI</label>
                <input type="text" [(ngModel)]="namespace.locationUri"/>
            </div>
            <div class="form-group">
                <label>Type</label>
                <input type="checkbox" [ngModel]="namespace.isTarget" style="width:20px; vertical-align:middle;"
                    disabled="{{!targetEnabled}}" (click)="targetToggled()" />
                <label [attr.class]="(targetEnabled ? '' : 'disabled')" style="width:105px; ">Target Namespace</label>
                <div class="clear"></div>
            </div>
        </div>
    `
})

export class NamespaceEditComponent implements ModalWindowValidator {
    public namespace: NamespaceModel = new NamespaceModel();
    public targetEnabled: boolean = true;

    public initialize(namespace: NamespaceModel, namespaces: NamespaceModel[]): void {
        this.namespace = (namespace == null) ? new NamespaceModel() : namespace.copy();
        if (!namespace.isTarget) {
            for (let ns of namespaces) {
                if (ns.isTarget) {
                    this.targetEnabled = false;
                    break;
                }
            }
        }
        console.log("Namespaces enabled: " + this.targetEnabled);
    }

    targetToggled(): void {
        this.namespace.isTarget = !this.namespace.isTarget;
        this.namespace.alias = this.namespace.isTarget ? "tns" : "";
    }

    isDataValid(): boolean {
        var dataIsValid: boolean = DataMapperUtil.isRequiredFieldValid(this.namespace.alias, "Alias");
        dataIsValid = DataMapperUtil.isRequiredFieldValid(this.namespace.uri, "URI") && dataIsValid;
        return dataIsValid;
    }
}
