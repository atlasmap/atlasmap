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
    public type: string = "STRING";
    public serviceObject: any = new Object();
}

export class FieldActionArgumentValue {
    public name: string = null;
    public value: string = null;
}

export class FieldAction {
    public isSeparateOrCombineMode: boolean = false;
    public name: string;
    public config: FieldActionConfig = null;
    public argumentValues: FieldActionArgumentValue[] = [];
    public static combineActionConfig: FieldActionConfig = null;
    public static separateActionConfig: FieldActionConfig = null;

    public getArgumentValue(argumentName: string): FieldActionArgumentValue {
        for (let argValue of this.argumentValues) {
            if (argValue.name == argumentName) {
                return argValue;
            }
        }
        var argValue: FieldActionArgumentValue = new FieldActionArgumentValue();
        argValue.name = argumentName;
        argValue.value = "0";
        this.argumentValues.push(argValue);
        return argValue;
    }

    public static createSeparateCombineFieldAction(separateMode: boolean, value: string) {
        if (FieldAction.combineActionConfig == null) {
            var argument: FieldActionArgument = new FieldActionArgument();
            argument.name = "Index";
            argument.type = "NUMBER";
            FieldAction.combineActionConfig = new FieldActionConfig();
            FieldAction.combineActionConfig.name = "Combine";
            FieldAction.combineActionConfig.arguments.push(argument);
            FieldAction.separateActionConfig = new FieldActionConfig();
            FieldAction.separateActionConfig.name = "Separate";
            FieldAction.separateActionConfig.arguments.push(argument);
        }

        var fieldAction: FieldAction = new FieldAction(); 
        FieldAction.combineActionConfig.populateFieldAction(fieldAction);
        if (separateMode) {
            FieldAction.separateActionConfig.populateFieldAction(fieldAction);
        }
        fieldAction.isSeparateOrCombineMode = true;

        var argumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
        argumentValue.name = "Index";
        argumentValue.value = (value == null) ? "1" : value;
        fieldAction.argumentValues.push(argumentValue);
        return fieldAction;
    }
}

export class FieldActionConfig {
    public name: string;
    public arguments: FieldActionArgument[] = [];
    public method: string;
    public sourceType: string = "STRING";
    public targetType: string = "STRING";
    public serviceObject: any = new Object();    

    public appliesToField(field: Field, fieldPair: FieldMappingPair): boolean {
        var type: string = (field == null) ? null : field.type;
        if (type == null) {
            return false;
        }
        
        if (this.sourceType == "STRING" && fieldPair.transition.isMapMode()
            && fieldPair.hasMappedField(true)) {
            var sourceField: Field = fieldPair.getFields(true)[0];
            var sourceFieldIsString: boolean = (["STRING", "CHAR"].indexOf(sourceField.type) != -1);
            if (!sourceFieldIsString) {
                return false;
            }
        }        
        
        if (this.targetType == "STRING") {
            var fieldTypeIsString: boolean = (["STRING", "CHAR"].indexOf(type) != -1);
            return fieldTypeIsString;
        }

        var typeIsNumber: boolean = (["LONG", "INTEGER", "FLOAT", "DOUBLE"].indexOf(type) != -1);
        return typeIsNumber;
    }

    public populateFieldAction(action: FieldAction): void {
        action.name = this.name;
        action.config = this;        
    }

    public getArgumentForName(name: string): FieldActionArgument {
        for (let argument of this.arguments) {
            if (argument.name == name) {
                return argument;
            }
        }
        return null;
    }
}

export enum TransitionMode { MAP, SEPARATE, ENUM, COMBINE }
export enum TransitionDelimiter { COLON, COMMA, DASH, NONE, PERIOD, SEMICOLON, SPACE, UNDERSCORE }

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
    public mode: TransitionMode = TransitionMode.MAP;
    public delimiter: TransitionDelimiter = TransitionDelimiter.SPACE;
    public lookupTableName: string = null;

    public static delimiterModels: TransitionDelimiterModel[] = [];
    public static actionConfigs: FieldActionConfig[] = [];

    public constructor() { 
        if (TransitionModel.delimiterModels.length == 0) {
            var models: TransitionDelimiterModel[] = [];
            models.push(new TransitionDelimiterModel(TransitionDelimiter.COLON, "COLON", "Colon"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.COMMA, "COMMA", "Comma"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.DASH, "DASH", "Dash"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.NONE, "NONE", "None"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.PERIOD, "PERIOD", "Period"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.SEMICOLON, "SEMICOLON", "Semicolon"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.SPACE, "SPACE", "Space"));
            models.push(new TransitionDelimiterModel(TransitionDelimiter.UNDERSCORE, "UNDERSCORE", "Underscore"));
            TransitionModel.delimiterModels = models;
        }
    }

    public static getActionConfigForName(actionName: string): FieldActionConfig {
        if (actionName == null) {
            return null;
        }
        for (let actionConfig of TransitionModel.actionConfigs) {
            if (actionName == actionConfig.name) {
                return actionConfig;
            }
        }
        return null;
    }

    public static getTransitionDelimiterPrettyName(delimiter: TransitionDelimiter) : string {
        for (let m of TransitionModel.delimiterModels) {
            if (m.delimiter == delimiter) {
                return m.prettyName;
            }
        }        
        return null;
    }

    public getSerializedDelimeter(): string {
        for (let m of TransitionModel.delimiterModels) {
            if (m.delimiter == this.delimiter) {
                return m.serializedValue;
            }
        }        
        return null;
    }

    public setSerializedDelimeterFromSerializedValue(value: string): void {
        for (let m of TransitionModel.delimiterModels) {
            if (m.serializedValue == value) {
                this.delimiter = m.delimiter;
            }
        }        
    }

    public getPrettyName() {
        var delimiterDesc: string = TransitionModel.getTransitionDelimiterPrettyName(this.delimiter);
        if (this.mode == TransitionMode.SEPARATE) {
            return "Separate (" + delimiterDesc + ")";
        } else if (this.mode == TransitionMode.COMBINE) {
            return "Combine (" + delimiterDesc + ")";
        } else if (this.mode == TransitionMode.ENUM) {
            return "Enum (table: " + this.lookupTableName + ")";
        }
        return "Map";
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
