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

import { Component, Input, OnInit } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { MappingModel, MappedField } from '../../models/mapping.model';
import { FieldAction } from '../../models/field-action.model';

@Component({
  selector: 'mapping-field-detail',
  templateUrl: './mapping-field-detail.component.html',
})

export class MappingFieldDetailComponent implements OnInit {

  @Input() cfg: ConfigModel;
  @Input() mapping: MappingModel;
  @Input() isSource: boolean;
  @Input() mappedField: MappedField;

  sourceIconCSSClass: string;
  parentObjectName: string;

  ngOnInit() {
    this.updateTemplateValues();
  }

  isTransformCapable() {
    return (!this.mappedField.isPadField() && this.mappedField.field.name.length > 0);
  }

  /**
   * The add transformation icon has been selected.  Add a field action to the current
   * mapped field.
   */
  addTransformation(): void {
    const actionDefinition = this.cfg.fieldActionService.getActionsAppliesToField(this.mapping, this.isSource)[0];
    if (actionDefinition == null) {
      this.cfg.errorService.info('The selected field has no applicable transformation actions.', null);
      return;
    }
    const action: FieldAction = new FieldAction();
    actionDefinition.populateFieldAction(action);
    this.mappedField.actions.push(action);
    this.cfg.mappingService.saveCurrentMapping();
    this.cfg.mappingService.notifyMappingUpdated();
  }

  getFieldPath(): string {
    if (this.mappedField == null || this.mappedField.field == null) {
      return '[None]';
    }
    return this.mappedField.field.path;
  }

  displayParentObject(): boolean {
    if (this.mappedField != null && this.mappedField.isPadField()) {
      this.parentObjectName = '<padding field>';
      return true;
    }
    if (this.mappedField == null || this.mappedField.field == null
      || this.mappedField.field.name.length === 0
      || this.mappedField.field.docDef == null) {
      return false;
    }
    if (this.parentObjectName == null || this.parentObjectName.length === 0) {
      this.updateTemplateValues();
    }
    return true;
  }

  displayIndex(): boolean {
    return this.mapping.getMappedFields(this.isSource).length > 1 && !this.mapping.transition.enableExpression;
  }

  /**
   * The user has hand-edited the index value of a mapped field.  Perform the following:
   *   - Add place-holders for each index value between the updated value and its previous value.
   *   - Re-sequence the field action indices.
   *   - Sort the mapped fields array to get then back into numerical order.
   * @param event
   * @param mappedField
   */
  indexSelectionChanged(event: any, mappedField: MappedField): void {
    const insertionIndex = Number(event.target.value) || 0;
    if (insertionIndex === 0) {
      return;
    }
    const mappedFields = this.mapping.getMappedFields(mappedField.isSource());
    const targetIndex = mappedFields.length;
    if (insertionIndex > targetIndex) {

      // Add place-holders for each index value between the previous max index and the insertion index.
      this.cfg.mappingService.addPlaceholders(insertionIndex - mappedFields.length,
        this.mapping, targetIndex, mappedField.field.isSource());
    }
    this.cfg.mappingService.moveMappedFieldTo(this.mapping, mappedField, insertionIndex);
    this.cfg.mappingService.saveCurrentMapping();
  }

  removeMappedField(mappedField: MappedField): void {
    this.mapping.removeMappedField(mappedField);
    this.cfg.mappingService.updateMappedField(this.mapping);
  }

  private updateTemplateValues(): void {
    this.sourceIconCSSClass = this.getSourceIconCSSClass();
    this.parentObjectName = this.getParentObjectName();
  }

  private getSourceIconCSSClass(): string {
    return this.isSource ? 'fa fa-hdd-o' : 'fa fa-download';
  }

  private getParentObjectName() {
    if (this.mappedField == null || this.mappedField.field == null || this.mappedField.field.docDef == null) {
      return '';
    }
    return this.mappedField.field.docDef.getName(ConfigModel.getConfig().showTypes);
  }
}
