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
    public sourceType = 'undefined';
    public targetType = 'undefined';
    public serviceObject: any = new Object();

    /**
     * Return true if the target field pair is numeric, string or the field pair types
     * match the current field action types.
     * @param fieldPair
     */
    public appliesToField(fieldPair: FieldMappingPair): boolean {

        if (fieldPair == null) {
            return false;
        }
        const sourceField: Field = fieldPair.getFields(true)[0];
        const targetField: Field = fieldPair.getFields(false)[0];

        if ((sourceField == null) || (targetField == null)) {
            return false;
        }

        // Check for target string types.
        if (this.targetType == 'STRING') {
            return (['STRING', 'CHAR'].indexOf(targetField.type) != -1);
        }

        // Check for numeric target types.
        if (this.targetType == 'NUMBER') {
            return (['LONG', 'INTEGER', 'FLOAT', 'DOUBLE', 'SHORT', 'BYTE', 'DECIMAL', 'NUMBER'].indexOf(targetField.type) != -1);
        }

        // All other types must match the mapped field types with the field action types.
        return ((sourceField.type == this.sourceType) && (targetField.type == this.targetType));
    }

    public populateFieldAction(action: FieldAction): void {
        action.name = this.name;
        action.config = this;
        
        // Use the parsed values if present, otherwise set to '0'.
        if (action.argumentValues == null || action.argumentValues.length == 0) {
           action.argumentValues = [];
           for (const arg of this.arguments) {
               action.setArgumentValue(arg.name, '0');
           }
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
