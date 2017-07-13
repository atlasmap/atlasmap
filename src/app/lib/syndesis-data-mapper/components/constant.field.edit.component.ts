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

import { Field } from '../models/field.model';
import { ModalWindowValidator } from './modal.window.component';
import { ConfigModel } from '../models/config.model';
import { DataMapperUtil } from '../common/data.mapper.util';

@Component({
    selector: 'constant-field-edit',
    template: `
        <div class="DataMapperEditComponent">
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
    `
})

export class ConstantFieldEditComponent implements ModalWindowValidator {
    public field: Field = new Field();
    public valueType: any = "STRING";

    public initialize(field: Field): void {
        if (field != null) {
            this.valueType = field.type;
        }
        this.field = field == null ? new Field() : field.copy();
    }

    public valueTypeSelectionChanged(event: MouseEvent): void {
        var eventTarget: any = event.target; //extract this to avoid compiler error about 'selectedOptions' not existing.
        this.valueType = eventTarget.selectedOptions.item(0).attributes.getNamedItem("value").value;
    }

    public getField(): Field {
        this.field.displayName = this.field.value;
        this.field.name = this.field.value;
        this.field.path = this.field.value;
        this.field.type = this.valueType;
        return this.field;
    }

    isDataValid(): boolean {
        return DataMapperUtil.isRequiredFieldValid(this.field.value, "Value");
    }
}
