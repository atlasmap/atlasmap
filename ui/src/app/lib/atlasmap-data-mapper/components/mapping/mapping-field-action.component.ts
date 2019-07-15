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
import { DataMapperUtil } from '../../common/data-mapper-util';
import { MappingModel, MappedField } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { FieldAction, FieldActionArgument, FieldActionArgumentValue,
         FieldActionDefinition } from '../../models/field-action.model';

@Component({
  selector: 'mapping-field-action',
  templateUrl: './mapping-field-action.component.html',
})

export class MappingFieldActionComponent {
  @Input() cfg: ConfigModel;
  @Input() mappedField: MappedField;
  @Input() isSource: boolean;
  @Input() mapping: MappingModel;

  getMappedFieldActions(): FieldAction[] {
    return this.mappedField.actions;
  }

  isIndexArg(argVal: string, index: number): boolean {
    return (argVal === 'Index' && index === 0);
  }

  actionsExistForField(): boolean {
    return (this.cfg.fieldActionService.getActionsAppliesToField(this.mapping, this.isSource).length > 0);
  }

  getActionConfigs(): FieldActionDefinition[] {
    return this.cfg.fieldActionService.getActionsAppliesToField(this.mapping, this.isSource);
  }

  /**
   * Remove the specified field action (transformation) from the current mapped field's
   * actions.
   * @param action
   */
  removeAction(action: FieldAction): void {
    this.mappedField.removeAction(action);
    this.cfg.mappingService.saveCurrentMapping();
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * A mapping field action configuration selection has been made.  Note that action field arguments, if any,
   * may be specified by either a text field or pull-down menu.
   * @param event
   */
  configSelectionChanged(event: any) {
    const attributes: any = event.target.selectedOptions.item(0).attributes;
    const selectedActionName: any = attributes.getNamedItem('value').value;
    const selectedActionIndex: any = attributes.getNamedItem('actionIndex').value;
    const action: FieldAction = this.getMappedFieldActions()[selectedActionIndex];
    if (action.name !== selectedActionName) {
      action.argumentValues = [];  // Invalidate the previously selected field action arguments.
      const fieldActionDefinition = this.cfg.fieldActionService.getActionDefinitionForName(selectedActionName);
      fieldActionDefinition.populateFieldAction(action);

      // If the field action configuration predefines argument values then populate the fields with
      // default values.  Needed to support pull-down menus in action argument definitions.
      if (action.argumentValues.values && action.argumentValues.length > 0
        && fieldActionDefinition.arguments[0] && fieldActionDefinition.arguments[0].values
        && fieldActionDefinition.arguments[0].values.length > 0) {
        for (let i = 0; i < action.argumentValues.length; i++) {
          action.argumentValues[i].value = fieldActionDefinition.arguments[i].values[i];
        }
      }
    }
    this.cfg.mappingService.saveCurrentMapping();
  }

  /**
   * Translate an internal label to a human legible form.
   * @param paramName
   */
  getLabel(paramName: string): string {
    return DataMapperUtil.toDisplayable(paramName);
  }

  displayTransformationAction(action): boolean {
    return (action.name !== 'Split');
  }
}
