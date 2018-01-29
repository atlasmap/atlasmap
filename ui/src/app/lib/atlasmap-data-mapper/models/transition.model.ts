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
    name: string = null;
    type = 'STRING';
    serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
    name: string = null;
    value: string = null;
}

export class FieldAction {
    static combineActionConfig: FieldActionConfig = null;
    static separateActionConfig: FieldActionConfig = null;

    isSeparateOrCombineMode = false;
    name: string;
    config: FieldActionConfig = null;
    argumentValues: FieldActionArgumentValue[] = [];

    getArgumentValue(argumentName: string): FieldActionArgumentValue {
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

    setArgumentValue(argumentName: string, value: string): void {
        this.getArgumentValue(argumentName).value = value;
    }

    static createSeparateCombineFieldAction(separateMode: boolean, value: string) {
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
    name: string;
    arguments: FieldActionArgument[] = [];
    method: string;
    sourceType = 'undefined';
    targetType = 'undefined';
    serviceObject: any = new Object();

    /**
     * Return true if the target field pair is numeric, string or the field pair types
     * match the current field action types.
     * @param fieldPair
     */
    appliesToField(fieldPair: FieldMappingPair): boolean {

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

    populateFieldAction(action: FieldAction): void {
        action.name = this.name;
        action.config = this;

        // Use the parsed values if present, otherwise set to '0'.
        if (action.argumentValues == null || action.argumentValues.length == 0) {
           action.argumentValues = [];
           for (const arg of this.arguments) {
               action.setArgumentValue(arg.name, '');
           }
        }
    }

    getArgumentForName(name: string): FieldActionArgument {
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
    delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
    serializedValue: string = null;
    prettyName: string = null;

    constructor(delimiter: TransitionDelimiter, serializedValue: string, prettyName: string) {
        this.delimiter = delimiter;
        this.serializedValue = serializedValue;
        this.prettyName = prettyName;
    }
}

export class TransitionModel {
    static delimiterModels: TransitionDelimiterModel[] = [];
    static actionConfigs: FieldActionConfig[] = [];

    mode: TransitionMode = TransitionMode.MAP;
    delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
    lookupTableName: string = null;

    constructor() {
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

    static getActionConfigForName(actionName: string): FieldActionConfig {
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

    static getTransitionDelimiterPrettyName(delimiter: TransitionDelimiter): string {
        for (const m of TransitionModel.delimiterModels) {
            if (m.delimiter == delimiter) {
                return m.prettyName;
            }
        }
        return null;
    }

    getSerializedDelimeter(): string {
        for (const m of TransitionModel.delimiterModels) {
            if (m.delimiter == this.delimiter) {
                return m.serializedValue;
            }
        }
        return null;
    }

    setSerializedDelimeterFromSerializedValue(value: string): void {
        for (const m of TransitionModel.delimiterModels) {
            if (m.serializedValue == value) {
                this.delimiter = m.delimiter;
            }
        }
    }

    getPrettyName() {
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

    isSeparateMode(): boolean {
        return this.mode == TransitionMode.SEPARATE;
    }

    isMapMode(): boolean {
        return this.mode == TransitionMode.MAP;
    }

    isCombineMode(): boolean {
        return this.mode == TransitionMode.COMBINE;
    }

    isEnumerationMode(): boolean {
        return this.mode == TransitionMode.ENUM;
    }
}
