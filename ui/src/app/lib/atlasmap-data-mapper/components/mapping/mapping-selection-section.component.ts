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

import { MappingModel, FieldMappingPair } from '../../models/mapping.model';

@Component({
  selector: 'mapping-selection-section',
  templateUrl: './mapping-selection-section.component.html',
})

export class MappingSelectionSectionComponent {
  @Input() outputNumber: number;
  @Input() mapping: MappingModel;
  @Input() selectedCallback: Function;
  @Input() selected = false;
  @Input() selectedFieldIsSource = false;
  @Input() parentComponent: Component;
  @Input() isOddRow = false;

  getClass(): string {
    let cssClass = 'MappingSelectionSection';
    if (this.selected) {
      cssClass += ' SelectedMappingSelectionSection';
    }
    if (this.isOddRow) {
      cssClass += ' odd';
    }
    return cssClass;
  }

  getSourceTargetLabelText(isSource: boolean, fieldPair: FieldMappingPair): string {
    if (isSource) {
      return (fieldPair.sourceFields.length > 0) ? 'Sources' : 'Source';
    }
    return (fieldPair.targetFields.length > 0) ? 'Targets' : 'Target';
  }

  getFormattedOutputPath(path: string, nameOnly: boolean) {
    if (path == null) {
      return '';
    }
    path = path.replace('.', '/');
    const index: number = path.lastIndexOf('/');
    const fieldName: string = (index == -1) ? path : path.substr(path.lastIndexOf('/') + 1);
    path = (index == -1) ? '' : path.substr(0, path.lastIndexOf('/') + 1);
    return nameOnly ? fieldName : path;
  }

  handleMouseClick(event: MouseEvent) {
    this.selectedCallback(this);
  }
}
