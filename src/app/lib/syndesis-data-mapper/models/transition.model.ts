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

export class FieldAction {
    public description: string = "Transformation";
    public isSeparateOrCombineMode: boolean = false;
    public identifier: string;
    public name: string;
    public argumentValues: string[] = [];
    public argumentNames: string[] = [];
}

export class FieldActionConfig {
    public identifier: string;
    public name: string;
    public argumentNames: string[] = [];
    public forString: boolean = true;
    public method: string;
    public serviceObject: any;

    public appliesToField(field: Field): boolean {
        var type: string = (field == null) ? null : field.type;
        if (type == null) {
            return false;
        }
        if (this.forString) {
            var typeIsString: boolean = (["STRING", "CHAR"].indexOf(type) != -1);
            return typeIsString;
        }
        var typeIsNumber: boolean = (["LONG", "INTEGER", "FLOAT", "DOUBLE"].indexOf(type) != -1);
        return typeIsNumber;
    }

    public populateFieldAction(action: FieldAction): void {
        action.name = this.name;
        action.identifier = this.identifier;
        action.argumentNames = [];
        action.argumentValues = [];
        for (let argName of this.argumentNames) {
            action.argumentNames.push(argName);
            action.argumentValues.push("");
        }
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

    public static getActionConfig(action: FieldAction): FieldActionConfig {
        if (action == null) {
            return null;
        }
        for (let actionConfig of TransitionModel.actionConfigs) {
            if (action.identifier == actionConfig.identifier) {
                return actionConfig;
            }
        }
        return null;
    }

    public static getActionConfigForMethod(actionMethod: string): FieldActionConfig {
        if (actionMethod == null) {
            return null;
        }
        for (let actionConfig of TransitionModel.actionConfigs) {
            if (actionMethod == actionConfig.method) {
                return actionConfig;
            }
        }
        return null;
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
