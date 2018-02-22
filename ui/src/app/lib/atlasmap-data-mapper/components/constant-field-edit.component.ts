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
import { ModalWindowValidator } from './modal-window.component';

@Component({
  selector: 'constant-field-edit',
  templateUrl: './constant-field-edit.component.html',
})

export class ConstantFieldEditComponent implements ModalWindowValidator {
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
    this.field.displayName = this.field.value;
    this.field.name = this.field.value;
    this.field.path = this.field.value;
    this.field.type = this.valueType;
    this.field.userCreated = true;
    return this.field;
  }

  isDataValid(): boolean {
    return ConfigModel.getConfig().isRequiredFieldValid(this.field.value, 'Value');
  }
}
