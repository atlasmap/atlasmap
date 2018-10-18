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

import { ConfigModel } from '../../models/config.model';
import { MappedField } from '../../models/mapping.model';
import { DocumentDefinition } from '../../models/document-definition.model';

@Component({
  selector: 'mapping-list-field',
  templateUrl: './mapping-list-field.component.html',
})

export class MappingListFieldComponent {
  @Input() mappedField: MappedField;
  @Input() isSource: boolean;
  @Input() isActive: boolean;
  @Input() cfg: ConfigModel;

  getSourceIconCSSClass(): string {
    return this.isSource ? 'fa fa-hdd-o' : 'fa fa-download';
  }

  getFieldPath(): string {
    if (this.mappedField == null || this.mappedField.field == null
      || (this.mappedField.field === DocumentDefinition.getNoneField())) {
      return '[None]';
    }
    return this.mappedField.field.getFieldLabel(ConfigModel.getConfig().showTypes, true);
  }

  displayParentObject(): boolean {
    if (this.mappedField == null || this.mappedField.field == null
      || this.mappedField.field.docDef == null
      || (this.mappedField.field === DocumentDefinition.getNoneField())) {
      return false;
    }
    return true;
  }

  getParentObjectName() {
    if (this.mappedField == null || this.mappedField.field == null || this.mappedField.field.docDef == null) {
      return '';
    }
    return this.mappedField.field.docDef.getName(ConfigModel.getConfig().showTypes);
  }
}
