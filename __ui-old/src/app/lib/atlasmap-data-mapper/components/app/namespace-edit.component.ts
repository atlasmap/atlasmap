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

import { Component, ViewChild, ElementRef } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { NamespaceModel } from '../../models/document-definition.model';
import { ModalWindowValidator } from '../modal/modal-window.component';

@Component({
  selector: 'namespace-edit',
  templateUrl: './namespace-edit.component.html',
})

export class NamespaceEditComponent implements ModalWindowValidator {
  namespace: NamespaceModel = new NamespaceModel();
  targetEnabled = true;
  @ViewChild('namespace') private focusEl: ElementRef;

  initialize(namespace: NamespaceModel, namespaces: NamespaceModel[]): void {
    this.namespace = (namespace == null) ? new NamespaceModel() : namespace.copy();
    if (!namespace.isTarget) {
      for (const ns of namespaces) {
        if (ns.isTarget) {
          this.targetEnabled = false;
          break;
        }
      }
    }
  }

  targetToggled(): void {
    this.namespace.isTarget = !this.namespace.isTarget;
    this.namespace.alias = this.namespace.isTarget ? 'tns' : '';
  }

  isDataValid(): boolean {
    const configModel: ConfigModel = ConfigModel.getConfig();
    let dataIsValid: boolean = configModel.errorService.isRequiredFieldValid(this.namespace.alias, 'Alias');
    dataIsValid = configModel.errorService.isRequiredFieldValid(this.namespace.uri, 'URI') && dataIsValid;
    return dataIsValid;
  }

  getInitialFocusElement(): ElementRef {
    return this.focusEl;
  }

}
