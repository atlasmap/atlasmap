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
import { DocumentDefinition } from './document-definition.model';
import { MappingModel } from './mapping.model';
import { Field } from './field.model';

export class FieldActionArgument {
  name: string = null;
  type = 'STRING';
  values = null;
  serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
  name: string = null;
  value: string = null;
}

export enum Multiplicity {
  ONE_TO_ONE = 'ONE_TO_ONE',
  ONE_TO_MANY = 'ONE_TO_MANY',
  MANY_TO_ONE = 'MANY_TO_ONE',
  ZERO_TO_ONE = 'ZERO_TO_ONE'
}

export class FieldActionDefinition {
  name: string;
  isCustom: boolean;
  arguments: FieldActionArgument[] = [];
  method: string;
  sourceType = 'undefined';
  targetType = 'undefined';
  multiplicity = Multiplicity.ONE_TO_ONE;
  serviceObject: any = new Object();

  /**
   * Return true if the action's source/target types and collection types match the respective source/target
   * field properties for source transformations, or matches the respective target field properties only for
   * a target transformation.
   *
   * Note - source-side only transformations are permitted so the target field may be undefined.
   *
   * @param mapping
   */
  appliesToField(mapping: MappingModel, isSource: boolean): boolean {

    if (mapping == null) {
      return false;
    }
    const selectedSourceField: Field = this.getActualField(mapping, true);
    const selectedTargetField: Field = this.getActualField(mapping, false);

    if (selectedSourceField == null) {
      return false;
    }

    return isSource ? this.appliesToSourceField(mapping, selectedSourceField)
     : this.appliesToTargetField(mapping, selectedTargetField);
  }

  populateFieldAction(action: FieldAction): void {
    action.name = this.name;
    action.config = this;

    // Use the parsed values if present, otherwise set to '0'.
    if (action.argumentValues == null || action.argumentValues.length === 0) {
      action.argumentValues = [];
      for (const arg of this.arguments) {

        // Default the input field to 0 for numerics
        if (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(arg.type) !== -1) {
          action.setArgumentValue(arg.name, '0');
        } else {
          action.setArgumentValue(arg.name, '');
        }
      }
    }
  }

  getArgumentForName(name: string): FieldActionArgument {
    for (const argument of this.arguments) {
      if (argument.name === name) {
        return argument;
      }
    }
    return null;
  }

  /**
   * Return the first non-padding field in either the source or target mappings.
   *
   * @param mapping
   * @param isSource
   */
  private getActualField(mapping: MappingModel, isSource: boolean): Field {
    let targetField: Field = null;
    for (targetField of mapping.getFields(isSource)) {
      if ((targetField.name !== '<padding field>') && (targetField !== DocumentDefinition.getNoneField())) {
        break;
      }
    }
    return targetField;
  }

  /**
   * Return true if the candidate type and selected type are generically a date, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesDate(candidateType: string, selectedType: string): boolean {
    return ((candidateType === 'ANY') ||
      (candidateType === 'ANY_DATE' &&
        ['DATE', 'DATE_TIME', 'DATE_TIME_TZ', 'TIME'].indexOf(selectedType) !== -1));
  }

  /**
   * Return true if the candidate type and selected type are generically numeric, false otherwise.
   *
   * @param candidateType
   * @param selectedType
   */
  private matchesNumeric(candidateType: string, selectedType: string): boolean {
    return ((candidateType === 'ANY') ||
      (candidateType === 'NUMBER' &&
        ['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(selectedType) !== -1));
  }

  /**
   * Check if it could be applied to source field.
   * @param mapping FieldMappingPair
   * @param selectedSourceField selected source field
   */
  private appliesToSourceField(mapping: MappingModel, selectedSourceField: Field): boolean {

    if ([Multiplicity.ONE_TO_MANY, Multiplicity.ZERO_TO_ONE].includes(this.multiplicity)) {
      return false;
    } else if (this.multiplicity === Multiplicity.MANY_TO_ONE) {
      // MANY_TO_ONE field action only applies to collection field or FieldGroup
      if (!selectedSourceField.isInCollection()) {
        return false;
      }
    }

    // Check for matching types - date.
    if (this.matchesDate(this.sourceType, selectedSourceField.type)) {
      return true;
    }

    // Check for matching types - numeric.
    if (this.matchesNumeric(this.sourceType, selectedSourceField.type)) {
      return true;
    }

    // First check if the source types match.
    if ((this.sourceType === 'ANY') || (selectedSourceField.type === this.sourceType)) {
      return true;
    }

    return false;
  }

  /**
   * Check if it could be applied for target field. Target type may not change.
   * @param mapping FieldMappingPair
   * @param selectedTargetField selected target field
   */
  private appliesToTargetField(mapping: MappingModel, selectedTargetField: Field): boolean {
    if (selectedTargetField == null) {
      return false;
    }

    if (this.multiplicity !== Multiplicity.ONE_TO_ONE) {
      return false;
    }

    // Check for matching types - date.
    if (this.matchesDate(this.sourceType, selectedTargetField.type) && this.matchesDate(this.targetType, selectedTargetField.type)) {
      return true;
    }

    // Check for matching types - numeric.
    if (this.matchesNumeric(this.sourceType, selectedTargetField.type) && this.matchesNumeric(this.targetType, selectedTargetField.type)) {
      return true;
    }

    if (this.sourceType !== 'ANY' && this.sourceType !== selectedTargetField.type) {
      return false;
    }

    // All other types must match the selected field types with the candidate field action types.
    return (this.targetType === 'ANY' || selectedTargetField.type === this.targetType);
  }

}

export class FieldAction {
  static combineActionConfig: FieldActionDefinition = null;
  static separateActionConfig: FieldActionDefinition = null;

  isSeparateOrCombineMode = false;
  name: string;
  config: FieldActionDefinition = null;
  argumentValues: FieldActionArgumentValue[] = [];

  static createSeparateCombineFieldAction(separateMode: boolean) {
    if (FieldAction.combineActionConfig == null) {
      FieldAction.combineActionConfig = new FieldActionDefinition();
      FieldAction.combineActionConfig.name = 'Combine';
      FieldAction.separateActionConfig = new FieldActionDefinition();
      FieldAction.separateActionConfig.name = 'Separate';
    }

    const fieldAction: FieldAction = new FieldAction();
    FieldAction.combineActionConfig.populateFieldAction(fieldAction);
    if (separateMode) {
      FieldAction.separateActionConfig.populateFieldAction(fieldAction);
    }
    fieldAction.isSeparateOrCombineMode = true;

    return fieldAction;
  }

  getArgumentValue(argumentName: string): FieldActionArgumentValue {
    for (const argValue of this.argumentValues) {
      if (argValue.name === argumentName) {
        return argValue;
      }
    }
    const newArgValue: FieldActionArgumentValue = new FieldActionArgumentValue();
    newArgValue.name = argumentName;
    newArgValue.value = '0';
    this.argumentValues.push(newArgValue);
    return newArgValue;
  }

  setArgumentValue(argumentName: string, value: string): void {
    this.getArgumentValue(argumentName).value = value;
  }

}

