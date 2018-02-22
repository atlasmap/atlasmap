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
import { Field } from '../../models/field.model';
import { FieldMappingPair } from '../../models/mapping.model';
import { DocumentDefinition } from '../../models/document-definition.model';

@Component({
  selector: 'collection-mapping',
  templateUrl: './collection-mapping.component.html',
})

export class CollectionMappingComponent {
  @Input() cfg: ConfigModel;

  fieldPairForEditing: FieldMappingPair = null;
  private animateLeft = false;
  private animateRight = false;

  getAnimationCSSClass(): string {
    if (this.animateLeft) {
      return 'dm-swipe-left collectionSectionLeft';
    } else if (this.animateRight) {
      return 'dm-swipe-right';
    }
    return '';
  }

  getFields(fieldPair: FieldMappingPair, isSource: boolean): Field[] {
    const fields: Field[] = fieldPair.getFields(isSource);
    return (fields.length > 0) ? fields : [DocumentDefinition.getNoneField()];
  }

  addClicked(): void {
    this.cfg.mappingService.addMappedPair();
  }

  editPair(fieldPair: FieldMappingPair): void {
    this.fieldPairForEditing = fieldPair;
    this.cfg.mappings.activeMapping.currentFieldMapping = fieldPair;
    this.animateLeft = true;
  }

  exitEditMode(): void {
    this.fieldPairForEditing = null;
    this.animateLeft = false;
    this.animateRight = true;
    this.cfg.mappings.activeMapping.currentFieldMapping = null;
  }

  removePair(fieldPair: FieldMappingPair): void {
    this.cfg.mappingService.removeMappedPair(fieldPair);
  }
}
