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

import { Component, ViewChildren, QueryList } from '@angular/core';

import { MappingModel } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';

import { ModalWindowComponent } from '../modal-window.component';
import { MappingSelectionSectionComponent } from './mapping-selection-section.component';

@Component({
  selector: 'mapping-selection',
  templateUrl: './mapping-selection.component.html',
})

export class MappingSelectionComponent {
  modalWindow: ModalWindowComponent;
  mappings: MappingModel[];
  selectedField: Field = null;
  cfg: ConfigModel;

  @ViewChildren('mappingSection') sectionComponents: QueryList<MappingSelectionSectionComponent>;

  private selectedMappingComponent: MappingSelectionSectionComponent = null;

  selectionChanged(c: MappingSelectionSectionComponent) {
    const self: MappingSelectionComponent = c.parentComponent as MappingSelectionComponent;
    const oldSelectedItem: MappingSelectionSectionComponent = self.getSelectedMappingComponent();
    oldSelectedItem.selected = false;
    c.selected = true;
    self.selectedMappingComponent = c;
  }

  getFormattedOutputPath(path: string, nameOnly: boolean) {
    path = path.replace('.', '/');
    const index: number = path.lastIndexOf('/');
    const fieldName: string = (index == -1) ? path : path.substr(path.lastIndexOf('/') + 1);
    path = (index == -1) ? '' : path.substr(0, path.lastIndexOf('/') + 1);
    return nameOnly ? fieldName : path;
  }

  addMapping() {
    this.cfg.mappingService.addNewMapping(this.selectedField);
    this.modalWindow.close();
  }

  getSelectedMapping(): MappingModel {
    return this.getSelectedMappingComponent().mapping;
  }

  private getSelectedMappingComponent(): MappingSelectionSectionComponent {
    if (this.selectedMappingComponent == null) {
      for (const c of this.sectionComponents.toArray()) {
        if (c.selected) {
          this.selectedMappingComponent = c;
          break;
        }
      }
    }
    return this.selectedMappingComponent;
  }

}
