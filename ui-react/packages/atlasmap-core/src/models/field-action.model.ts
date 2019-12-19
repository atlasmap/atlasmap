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

export class FieldActionArgument {
  name: string;
  type = 'STRING';
  values: string[] | null = null;
  serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
  name: string;
  value: string;
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

  populateFieldAction(action: FieldAction): void {
    action.name = this.name;
    action.definition = this;

    // Use the parsed values if present, otherwise set to '0'.
    if (action.argumentValues == null || action.argumentValues.length === 0) {
      action.argumentValues = [];
      for (const arg of this.arguments) {

        // Default the input field to 0 for numerics
        if (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(arg.type) !== -1) {
          action.setArgumentValue(arg.name!, '0'); // TODO: check this non null operator
        } else {
          action.setArgumentValue(arg.name!, ''); // TODO: check this non null operator
        }
      }
    }
  }

  getArgumentForName(name: string): FieldActionArgument {
    // TODO: check this non null operator
    return this.arguments.find(argument => argument.name === name)!;
  }

}

export class FieldAction {
  name: string;
  definition: FieldActionDefinition;
  argumentValues: FieldActionArgumentValue[] = [];

  static create(definition: FieldActionDefinition): FieldAction {
    const instance = new FieldAction();
    instance.definition = definition;
    instance.name = definition.name;
    return instance;
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

