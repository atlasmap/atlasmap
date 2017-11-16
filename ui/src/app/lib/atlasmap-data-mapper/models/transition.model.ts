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

import { Field } from './field.model';
import { FieldMappingPair } from './mapping.model';

export class FieldActionArgument {
    public name: string = null;
    public type = 'STRING';
    public serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
    public name: string = null;
    public value: string = null;
}

export class FieldAction {
    public static combineActionConfig: FieldActionConfig = null;
    public static separateActionConfig: FieldActionConfig = null;

    public isSeparateOrCombineMode = false;
    public name: string;
    public config: FieldActionConfig = null;
    public argumentValues: FieldActionArgumentValue[] = [];

    public getArgumentValue(argumentName: string): FieldActionArgumentValue {
        for (const argValue of this.argumentValues) {
            if (argValue.name == argumentName) {
                return argValue;
            }
        }
        const newArgValue: FieldActionArgumentValue = new FieldActionArgumentValue();
        newArgValue.name = argumentName;
        newArgValue.value = '0';
        this.argumentValues.push(newArgValue);
        return newArgValue;
    }

    public setArgumentValue(argumentName: string, value: string): void {
        this.getArgumentValue(argumentName).value = value;
    }

    public static createSeparateCombineFieldAction(separateMode: boolean, value: string) {
        if (FieldAction.combineActionConfig == null) {
            const argument: FieldActionArgument = new FieldActionArgument();
            argument.name = 'Index';
            argument.type = 'NUMBER';
            FieldAction.combineActionConfig = new FieldActionConfig();
            FieldAction.combineActionConfig.name = 'Combine';
            FieldAction.combineActionConfig.arguments.push(argument);
            FieldAction.separateActionConfig = new FieldActionConfig();
            FieldAction.separateActionConfig.name = 'Separate';
            FieldAction.separateActionConfig.arguments.push(argument);
        }

        const fieldAction: FieldAction = new FieldAction();
        FieldAction.combineActionConfig.populateFieldAction(fieldAction);
        if (separateMode) {
            FieldAction.separateActionConfig.populateFieldAction(fieldAction);
        }
        fieldAction.isSeparateOrCombineMode = true;

        fieldAction.setArgumentValue('Index', (value == null) ? '1' : value);
        return fieldAction;
    }
}

export class FieldActionConfig {
    public name: string;
    public arguments: FieldActionArgument[] = [];
    public method: string;
    public sourceType = 'STRING';
    public targetType = 'STRING';
    public serviceObject: any = new Object();

    public appliesToField(field: Field, fieldPair: FieldMappingPair): boolean {
        const type: string = (field == null) ? null : field.type;
        if (type == null) {
            return false;
        }

        if (this.sourceType == 'STRING' && fieldPair.transition.isMapMode()
            && fieldPair.hasMappedField(true)) {
            const sourceField: Field = fieldPair.getFields(true)[0];
            const sourceFieldIsString: boolean = (['STRING', 'CHAR'].indexOf(sourceField.type) != -1);
            if (!sourceFieldIsString) {
                return false;
            }
        }

        if (this.targetType == 'STRING') {
            const fieldTypeIsString: boolean = (['STRING', 'CHAR'].indexOf(type) != -1);
            return fieldTypeIsString;
        }

        const typeIsNumber: boolean = (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE'].indexOf(type) != -1);
        return typeIsNumber;
    }

    public populateFieldAction(action: FieldAction): void {
        action.name = this.name;
        action.config = this;
        action.argumentValues = [];
        for (const arg of this.arguments) {
            action.setArgumentValue(arg.name, '0');
        }
    }

    public getArgumentForName(name: string): FieldActionArgument {
        for (const argument of this.arguments) {
            if (argument.name == name) {
                return argument;
            }
        }
        return null;
    }
}

export enum TransitionMode { MAP, SEPARATE, ENUM, COMBINE }
export enum TransitionDelimiter { NONE, COLON, COMMA, MULTISPACE, SPACE }

export class TransitionDelimiterModel {
    public delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
    public serializedValue: string = null;
    public prettyName: string = null;

    public constructor(delimiter: TransitionDelimiter, serializedValue: string, prettyName: string) {
        this.delimiter = delimiter;
        this.serializedValue = serializedValue;
        this.prettyName = prettyName;
    }
}

export class TransitionModel {
    public static delimiterModels: TransitionDelimiterModel[] = [];
    public static actionConfigs: FieldActionConfig[] = [];

    public mode: TransitionMode = TransitionMode.MAP;
    public delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
    public lookupTableName: string = null;

    public constructor() {
        if (TransitionModel.delimiterModels.length == 0) {
            const models: TransitionDelimiterModel[] = [];
            models.push(new TransitionDelimiterModel(TransitionDelimiter.NONE, null, '[None]'));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.COLON, 'Colon', 'Colon'));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.COMMA, 'Comma', 'Comma'));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.MULTISPACE, 'MultiSpace', 'Multispace'));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.SPACE, 'Space', 'Space'));
            TransitionModel.delimiterModels = models;
        }
    }

    public static getActionConfigForName(actionName: string): FieldActionConfig {
        if (actionName == null) {
            return null;
        }
        for (const actionConfig of TransitionModel.actionConfigs) {
            if (actionName == actionConfig.name) {
                return actionConfig;
            }
        }
        return null;
    }

    public static getTransitionDelimiterPrettyName(delimiter: TransitionDelimiter): string {
        for (const m of TransitionModel.delimiterModels) {
            if (m.delimiter == delimiter) {
                return m.prettyName;
            }
        }
        return null;
    }

    public getSerializedDelimeter(): string {
        for (const m of TransitionModel.delimiterModels) {
            if (m.delimiter == this.delimiter) {
                return m.serializedValue;
            }
        }
        return null;
    }

    public setSerializedDelimeterFromSerializedValue(value: string): void {
        for (const m of TransitionModel.delimiterModels) {
            if (m.serializedValue == value) {
                this.delimiter = m.delimiter;
            }
        }
    }

    public getPrettyName() {
        const delimiterDesc: string = TransitionModel.getTransitionDelimiterPrettyName(this.delimiter);
        if (this.mode == TransitionMode.SEPARATE) {
            return 'Separate (' + delimiterDesc + ')';
        } else if (this.mode == TransitionMode.COMBINE) {
            return 'Combine (' + delimiterDesc + ')';
        } else if (this.mode == TransitionMode.ENUM) {
            return 'Enum (table: ' + this.lookupTableName + ')';
        }
        return 'Map';
    }

    public isSeparateMode(): boolean {
        return this.mode == TransitionMode.SEPARATE;
    }

    public isMapMode(): boolean {
        return this.mode == TransitionMode.MAP;
    }

    public isCombineMode(): boolean {
        return this.mode == TransitionMode.COMBINE;
    }

    public isEnumerationMode(): boolean {
        return this.mode == TransitionMode.ENUM;
    }
}
