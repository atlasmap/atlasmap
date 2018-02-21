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

import { Component, Input, ViewChildren, QueryList } from '@angular/core';

import { MappingModel, FieldMappingPair } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';

import { ModalWindowComponent } from '../modal.window.component';

@Component({
  selector: 'mapping-selection-section',
  template: `
        <div [attr.class]="getClass()" (click)="handleMouseClick($event)">
            <div class="numberWrapper"><div class="number">{{ outputNumber + 1 }}</div></div>
            <div class="pathContainer">
                <div class="fieldPair" *ngFor="let fieldPair of mapping.fieldMappings">
                    <div class="sourceTargetSection sourcePaths">
                        <label>{{ getSourceTargetLabelText(true, fieldPair) }}</label>
                        <div class="paths" *ngFor="let sourceField of fieldPair.sourceFields">
                            <div class="path">
                                <div class="pathName">
                                    <i class="fa fa-hdd-o" aria-hidden="true"></i>
                                    {{ getFormattedOutputPath(sourceField.field.path, false) }}
                                </div>
                                <div class="fieldName">{{ getFormattedOutputPath(sourceField.field.path, true) }}</div>
                                <div class="clear"></div>
                            </div>
                        </div>
                    </div>
                    <div class="sourceTargetSection targetPaths">
                        <label>{{ getSourceTargetLabelText(false, fieldPair) }}</label>
                        <div class="paths" *ngFor="let targetField of fieldPair.targetFields">
                            <div class="path">
                                <div class="pathName">
                                    <i class="fa fa-download" aria-hidden="true"></i>
                                    {{ getFormattedOutputPath(targetField.field.path, false) }}
                                </div>
                                <div class="fieldName">{{ getFormattedOutputPath(targetField.field.path, true) }}</div>
                                <div class="clear"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="clear"></div>
        </div>
    `,
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

@Component({
  selector: 'mapping-selection',
  template: `
        <div class="MappingSelectionComponent" *ngIf="mappings">
            <div class="header">
                <div class="sourceTargetHeader">{{ selectedField.isSource() ? 'Source' : 'Target' }}</div>
                <div class="pathHeader">
                    <div class="pathName">{{ getFormattedOutputPath(selectedField.path, false) }}</div>
                    <div class="fieldName">{{ getFormattedOutputPath(selectedField.path, true) }}</div>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
                <button class="btn btn-primary addButton" (click)="addMapping()">
                    <i class="fa fa-plus"></i>Add New Mapping
                </button>
            </div>
            <div class="MappingSelectionOptions">
                <mapping-selection-section *ngFor="let mapping of mappings; let i = index; let odd=odd; let even=even;"
                    [mapping]="mapping" [outputNumber]="i" [selected]="i == 0" [selectedCallback]="selectionChanged"
                    [selectedFieldIsSource]="selectedField.isSource()" [parentComponent]="this"
                    [isOddRow]="odd" #mappingSection>
                </mapping-selection-section>
            </div>
        </div>
    `,
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
