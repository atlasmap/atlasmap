/*
    Copyright (C) 2019 IBM, Inc.

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
import { MappedField } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { FieldAction, FieldActionArgument, FieldActionArgumentValue } from '../../models/field-action.model';

@Component({
  selector: 'mapping-field-action-argument',
  templateUrl: './mapping-field-action-argument.component.html',
})

export class MappingFieldActionArgumentComponent {
  @Input() action: FieldAction;
  @Input() actionIndex: number;
  @Input() argValIndex: number;
  @Input() argConfig: FieldActionArgument;
  @Input() cfg: ConfigModel;
  @Input() mappedField: MappedField;

  private checkIconEnabled = false;

  getMappedFieldActions(): FieldAction[] {
    return this.mappedField.actions;
  }

  enableCheck(): void {
    this.checkIconEnabled = true;
  }

  /**
   * The user has selected the check icon at the end of the input text widget.
   *
   * @param event
   */
  acceptInput(event: any): void {
    this.actionConfigParamSelectionChanged(event);

  }

  /**
   * Return in a string array the parameter values for the specified field action argument.
   *
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
   * Simply validate that the user isn't attempting a conversion to the original type.
   *
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
   *
   * @param event
   */
  actionConfigParamSelectionChanged(event: any): void {

    // Make sure they've specified something.
    if (!event.target.value || event.target.value.length === 0) {
      this.cfg.errorService.info('You must specify a transformation argument value.', null);
      return;
    }
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
    this.checkIconEnabled = false;
    // this.enableCheck();
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

  /**
   * Translate an internal label to a human legible form.
   *
   * @param argConfigName
   */
  getLabel(argConfigName: string): string {
    return DataMapperUtil.toDisplayable(argConfigName);
  }

}
