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

import { DocumentType, FieldMode } from '../common/config.types';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { Field } from '../models/field.model';
import { ModalWindowComponent, ModalWindowValidator } from './modal-window.component';

@Component({
  selector: 'property-field-edit',
  templateUrl: './property-field-edit.component.html',
})

export class PropertyFieldEditComponent implements ModalWindowValidator {
  field: Field = new Field();
  fieldMode: FieldMode;
  valueType: any = 'STRING';
  docDef: DocumentDefinition;
  modalWindowComponent: ModalWindowComponent;

  initialize(field: Field, docDef: DocumentDefinition, mwc: ModalWindowComponent): void {
    if (field != null) {
      this.valueType = field.type;
      this.fieldMode = FieldMode.EDIT;
    } else { this.fieldMode = FieldMode.CREATE; }

    this.field = field == null ? new Field() : field.copy();
    this.docDef = docDef;
    this.modalWindowComponent = mwc;
  }

  valueTypeSelectionChanged(event: any): void {
    this.valueType = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
  }

  getField(): Field {
    this.field.displayName = this.field.name;
    this.field.path = this.field.name;
    this.field.type = this.valueType;
    this.field.userCreated = true;
    this.field.docDef = this.docDef;
    return this.field;
  }

  isDataValid(): boolean {
    return ConfigModel.getConfig().isRequiredFieldValid(this.field.name, 'Name');
  }

  /**
   * Return true and disable the save button if the candidate name already exists on creation, return false
   * and enable the save button otherwise.
   */
  nameExistsOnCreation(): boolean {
    if (this.fieldMode === FieldMode.CREATE && this.docDef != null &&
        this.docDef.fieldExists(this.getField(), DocumentType.PROPERTY)) {
      this.modalWindowComponent.confirmButtonDisabled = true;
      return true;
    }
    this.modalWindowComponent.confirmButtonDisabled = false;
    return false;
  }
}
