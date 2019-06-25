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
   * Return in a string array the parameter values for the specified field action argument.
   * @param argConfig
   */
  getActionConfigParamValues(argConfig: FieldActionArgument): String[] {
    const acpv: String[] = [];
    for (const argument of argConfig.values) {
      acpv.push(argument);
    }
    return acpv;
  }

  /**
   * Remove the specified field action (transformation) from the current mapped field's
   * actions.
   * @param action
   */
  removeAction(action: FieldAction): void {
    this.mappedField.removeAction(action);
    this.cfg.mappingService.saveCurrentMapping();
    this.mappedField.reduceTransformationCount();
    this.cfg.mappingService.notifyMappingUpdated();
  }

  /**
   * Simply validate that the user isn't attempting a conversion to the original type.
   * @param acp
   */
  validateActionConfigParamSelection(acp: FieldActionArgumentValue[]): void {
    this.cfg.errorService.clearMappingErrors();
    if (acp != null && acp.length === 2) {
      if (acp[0].value === acp[1].value) {
        this.cfg.errorService.mappingError('Please select differing \'from\' and \'to\' units in your conversion transformation.', null);
      }
    }
  }

  /**
   * A mapping field action parameter selection has been made either from a pull-down menu
   * or from user input to a text field.
   * @param event
   */
  actionConfigParamSelectionChanged(event: any): void {
    this.mappedField.parsedData.userCreated = true;

    // Identify the pull-down
    if (event.target.selectedOptions != null) {
      const attributes: any = event.target.selectedOptions.item(0).attributes;
      const selectedArgValName: any = attributes.getNamedItem('value').value;
      const argValIndex: any = attributes.getNamedItem('argValIndex').value;
      const actionIndex: any = attributes.getNamedItem('actionIndex').value;
      const action: FieldAction = this.mappedField.actions[actionIndex];
      action.argumentValues[argValIndex].value = selectedArgValName;
      this.validateActionConfigParamSelection(action.argumentValues);
    }
    this.cfg.mappingService.saveCurrentMapping();
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
      const fieldActionConfig = this.cfg.fieldActionService.getActionDefinitionForName(selectedActionName);
      fieldActionConfig.populateFieldAction(action);

      // If the field action configuration predefines argument values then populate the fields with
      // default values.  Needed to support pull-down menus in action argument definitions.
      if (action.argumentValues.length > 0 && fieldActionConfig.arguments[0].values.length > 0) {
        for (let i = 0; i < action.argumentValues.length; i++) {
          action.argumentValues[i].value = fieldActionConfig.arguments[i].values[i];
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
    return this.toDisplayable(paramName);
  }

  private toDisplayable(camelCaseString: string): string {
    if (typeof camelCaseString === 'undefined' || !camelCaseString || camelCaseString.indexOf(' ') >= 0) {
      return camelCaseString;
    }
    let displayableString: string = camelCaseString.charAt(0).toUpperCase();
    for (let index = 1; index < camelCaseString.length; index++) {
      const chr: string = camelCaseString.charAt(index);
      if (chr !== chr.toLowerCase()) {
        displayableString += ' ';
      }
      displayableString += chr;
    }
    return displayableString;
  }

  /**
   * Return a string representing the default value for the field action argument pull-down.  If a mapped
   * field already exists for this component then use that to determine the displayed valued in the
   * pull-down; otherwise use the sequential configuration value based on the argument value index.
   *
   * @param argConfig - argument configuration used if no mapped field exists
   * @param actionIndex - used when multiple actions are specified
   * @param argValIndex - index into the argument values for any one specific action.
   */
  getActionConfigParamVDefault(argConfig: FieldActionArgument, actionIndex: number, argValIndex: number): String {
    const action: FieldAction = this.getMappedFieldActions()[actionIndex];
    if (action != null && action.argumentValues.length > 0) {
      return action.argumentValues[argValIndex].value;
    } else {
      return argConfig.values[argValIndex];
    }
  }

  displayTransformationAction(action): boolean {
    return (action.name !== 'Split');
  }
}
