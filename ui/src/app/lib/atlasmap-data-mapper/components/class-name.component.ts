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

import { Component } from '@angular/core';

import { DocumentDefinition, NamespaceModel } from '../models/document-definition.model';
import { Field } from '../models/field.model';
import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { Observable } from 'rxjs';
import { ModalWindowValidator } from './modal-window.component';

@Component({
  selector: 'class-name',
  templateUrl: './class-name.component.html',
})

export class ClassNameComponent implements ModalWindowValidator {
  cfg: ConfigModel = ConfigModel.getConfig();
  isSource: boolean;
  userClassName: string = null;
  docDef: DocumentDefinition = null;

  constructor() {
  }

  initialize(field: Field, docDef: DocumentDefinition, isAdd: boolean): void {
    this.docDef = docDef;
    this.userClassName = '';
  }

  parentSelectionChanged(event: any): void {
  }

  isDataValid(): boolean {
    return ConfigModel.getConfig().isRequiredFieldValid(this.userClassName, 'Class name');
  }

  valueExistsOnCreation(): boolean {
    return false;
  }
}
