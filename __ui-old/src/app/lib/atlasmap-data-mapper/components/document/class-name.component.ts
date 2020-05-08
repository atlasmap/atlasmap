/*
    Copyright (C) 2018 Red Hat, Inc.

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

import { Component, ViewChild, ElementRef } from '@angular/core';

import { DocumentDefinition } from '../../models/document-definition.model';
import { Field } from '../../models/field.model';
import { ConfigModel } from '../../models/config.model';
import { ModalWindowValidator } from '../modal/modal-window.component';
import { CollectionType } from '../../common/config.types';

@Component({
  selector: 'class-name',
  templateUrl: './class-name.component.html',
})

export class ClassNameComponent implements ModalWindowValidator {
  cfg: ConfigModel = ConfigModel.getConfig();
  isSource: boolean;
  userClassName: string = null;
  userCollectionType = CollectionType.NONE;
  userCollectionClassName = null;
  docDef: DocumentDefinition = null;
  @ViewChild('class') private focusEl: ElementRef;

  constructor() {
  }

  initialize(field: Field, docDef: DocumentDefinition, isAdd: boolean): void {
    this.docDef = docDef;
    this.userClassName = '';
  }

  parentSelectionChanged(event: any): void {
  }

  isDataValid(): boolean {
    return this.cfg.errorService.isRequiredFieldValid(this.userClassName, 'Class name');
  }

  getInitialFocusElement(): ElementRef {
    return this.focusEl;
  }

  valueExistsOnCreation(): boolean {
    return false;
  }

  collectionTypeSelectionChanged(event: any) {
      this.userCollectionType = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
  }
}
