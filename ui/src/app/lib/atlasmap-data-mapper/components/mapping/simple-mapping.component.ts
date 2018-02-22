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
import { FieldMappingPair, MappedField } from '../../models/mapping.model';
import { DocumentDefinition } from '../../models/document-definition.model';

@Component({
  selector: 'simple-mapping',
  templateUrl: './simple-mapping.component.html',
})

export class SimpleMappingComponent {
  @Input() cfg: ConfigModel;
  @Input() isSource = false;
  @Input() fieldPair: FieldMappingPair;

  isAddButtonVisible(): boolean {
    if (this.isSource && this.fieldPair.transition.isCombineMode()) {
      return true;
    } else if (!this.isSource && this.fieldPair.transition.isSeparateMode()) {
      return true;
    }
    return false;
  }

  getTopFieldTypeLabel(): string {
    return this.isSource ? 'Source' : 'Target';
  }

  getAddButtonLabel(): string {
    return this.isSource ? 'Add Source' : 'Add Target';
  }

  addClicked(): void {
    this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource);
    this.cfg.mappingService.updateMappedField(this.fieldPair);
  }

  removePair(): void {
    this.cfg.mappingService.removeMappedPair(this.fieldPair);
  }

  removeMappedField(mappedField: MappedField): void {
    this.fieldPair.removeMappedField(mappedField, this.isSource);
    if (this.fieldPair.getMappedFields(this.isSource).length == 0) {
      this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource);
    }
    this.cfg.mappingService.updateMappedField(this.fieldPair);
  }
}
