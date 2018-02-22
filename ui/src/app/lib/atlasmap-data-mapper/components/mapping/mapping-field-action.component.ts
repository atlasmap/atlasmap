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
import { FieldMappingPair, MappedField } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { TransitionModel, FieldAction, FieldActionConfig } from '../../models/transition.model';

@Component({
  selector: 'mapping-field-action',
  templateUrl: './mapping-field-action.component.html',
})

export class MappingFieldActionComponent {
  @Input() cfg: ConfigModel;
  @Input() mappedField: MappedField;
  @Input() isSource: boolean;
  @Input() fieldPair: FieldMappingPair;

  getMappedFieldActions(): FieldAction[] {
    return this.mappedField.actions;
  }

  getActionDescription(fieldAction: FieldAction): string {
    if (fieldAction.isSeparateOrCombineMode) {
      return fieldAction.config.name;
    }
    return 'Transformation';
  }

  actionsExistForField(): boolean {
    return (this.getActionConfigs().length > 0);
  }

  getActionConfigs(): FieldActionConfig[] {
    const configs: FieldActionConfig[] = [];

    // Start with the complete list of field actions.
    for (const config of TransitionModel.actionConfigs) {

      // Filter down to those field actions that apply to the selected field pair.
      if (config.appliesToField(this.fieldPair)) {
        configs.push(config);
      }
    }
    return configs;
  }

  removeAction(action: FieldAction): void {
    this.mappedField.removeAction(action);
  }

  selectionChanged(event: MouseEvent): void {
    this.cfg.mappingService.saveCurrentMapping();
  }

  addTransformation(): void {
    const actionConfig: FieldActionConfig = this.getActionConfigs()[0];
    const action: FieldAction = new FieldAction();
    actionConfig.populateFieldAction(action);
    this.getMappedFieldActions().push(action);
    this.cfg.mappingService.saveCurrentMapping();
  }

  configSelectionChanged(event: any) {
    const attributes: any = event.target.selectedOptions.item(0).attributes;
    const selectedActionName: any = attributes.getNamedItem('value').value;
    const selectedActionIndex: any = attributes.getNamedItem('actionIndex').value;
    const action: FieldAction = this.getMappedFieldActions()[selectedActionIndex];
    if (action.name != selectedActionName) {
      action.argumentValues = [];  // Invalidate the previously selected field action arguments.
      const fieldActionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(selectedActionName);
      fieldActionConfig.populateFieldAction(action);
    }
    this.cfg.mappingService.saveCurrentMapping();
  }
}
