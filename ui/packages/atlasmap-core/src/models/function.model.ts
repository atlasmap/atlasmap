import { FieldAction } from './field-action.model';
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

export class FunctionArgument {
  name: string;
  type = 'STRING';
  values: string[] | null = null;
  serviceObject: any = {};
}

export class FunctionArgumentValue {
  label: string;
  name: string;
  value: string;
}

export class FunctionDefinition {
  name: string;
  isCustom: boolean;
  arguments: FunctionArgument[] = [];
  method: string;
  serviceObject: any = {};

  populateFunction(func: Function): void {
    func.name = this.name;
    func.definition = this;

    // Use the parsed values if present, otherwise set to '0'.
    if (func.argumentValues == null || func.argumentValues.length === 0) {
      func.argumentValues = [];
      for (const arg of this.arguments) {
        // Default the input field to 0 for numerics
        if (
          [
            'LONG',
            'INTEGER',
            'FLOAT',
            'DOUBLE',
            'SHORT',
            'BYTE',
            'DECIMAL',
            'NUMBER',
          ].indexOf(arg.type) !== -1
        ) {
          func.setArgumentValue(arg.name!, '0'); // TODO: check this non null operator
        } else {
          func.setArgumentValue(arg.name!, ''); // TODO: check this non null operator
        }
      }
    }
  }

  getArgumentForName(name: string): FunctionArgument {
    // TODO: check this non null operator
    return this.arguments.find((argument) => argument.name === name)!;
  }
}

export class Function {
  name: string;
  definition: FunctionDefinition;
  argumentValues: FunctionArgumentValue[] = [];

  static create(definition: FunctionDefinition): Function {
    const instance = new Function();
    instance.definition = definition;
    instance.name = definition.name;
    return instance;
  }

  getArgumentValue(argumentName: string): FunctionArgumentValue {
    for (const argValue of this.argumentValues) {
      if (argValue.name === argumentName) {
        return argValue;
      }
    }
    const newArgValue: FunctionArgumentValue = new FunctionArgumentValue();
    newArgValue.name = argumentName;
    newArgValue.value = '0';
    this.argumentValues.push(newArgValue);
    return newArgValue;
  }

  setArgumentValue(argumentName: string, value: string): void {
    this.getArgumentValue(argumentName).value = value;
  }
}
