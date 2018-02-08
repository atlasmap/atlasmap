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

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { ModalWindowValidator } from './modal.window.component';

@Component({
    selector: 'property-field-edit',
    template: `
        <div class="DataMapperEditComponent">
            <div class="form-group">
                <label>Name:</label>
                <input name="name" type="text" [(ngModel)]="field.name"/>
            </div>
            <div class="form-group">
                <label>Value:</label>
                <input name="value" type="text" [(ngModel)]="field.value"/>
            </div>
            <div class="form-group">
                <label>Value Type</label>
                <select (change)="valueTypeSelectionChanged($event);" [ngModel]="valueType">
                    <option value="BOOLEAN">Boolean</option>
                    <option value="BYTE">Byte</option>
                    <option value="BYTE_ARRAY">ByteArray</option>
                    <option value="CHAR">Char</option>
                    <option value="COMPLEX">Complex</option>
                    <option value="DECIMAL">Decimal</option>
                    <option value="DOUBLE">Double</option>
                    <option value="FLOAT">Float</option>
                    <option value="INTEGER">Integer</option>
                    <option value="LONG">Long</option>
                    <option value="SHORT">Short</option>
                    <option value="STRING">String</option>
                    <option value="TIME">Time</option>
                    <option value="DATE">Date</option>
                    <option value="DATE_TIME">DateTime</option>
                    <option value="DATE_TZ">DateTZ</option>
                    <option value="TIME_TZ">TimeTZ</option>
                    <option value="DATE_TIME_TZ">DateTimeTZ</option>
                    <option value="UNSIGNED_BYTE">Unsigned Byte</option>
                    <option value="UNSIGNED_INTEGER">Unsigned Integer</option>
                    <option value="UNSIGNED_LONG">Unsigned Long</option>
                    <option value="UNSIGNED_SHORT">Unsigned Short</option>
                </select>
            </div>
        </div>
    `,
})

export class PropertyFieldEditComponent implements ModalWindowValidator {
    field: Field = new Field();
    valueType: any = 'STRING';

    initialize(field: Field): void {
        if (field != null) {
            this.valueType = field.type;
        }
        this.field = field == null ? new Field() : field.copy();
    }

    valueTypeSelectionChanged(event: any): void {
        this.valueType = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
    }

    getField(): Field {
        this.field.displayName = this.field.name;
        this.field.path = this.field.name;
        this.field.type = this.valueType;
        this.field.userCreated = true;
        return this.field;
    }

    isDataValid(): boolean {
        return ConfigModel.getConfig().isRequiredFieldValid(this.field.name, 'Name');
    }
}
