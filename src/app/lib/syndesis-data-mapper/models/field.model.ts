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

import { DocumentDefinition, NamespaceModel } from './document.definition.model';
import { ConfigModel } from '../models/config.model';

export class EnumValue {
    name: string;
    ordinal: number;
}

export class Field {
    public name: string = null;
    public classIdentifier: string = null;
    public displayName: string;
    public path: string = null;
    public type: string = null;
    public value: string = null;
    public serviceObject: any = new Object();
    public parentField: Field;
    public partOfMapping: boolean = false;
    public partOfTransformation: boolean = false;
    public visibleInCurrentDocumentSearch: boolean = true;
    public selected: boolean = false;
    public enumeration: boolean = false;
    public enumValues: EnumValue[] = [];
    public children: Field[] = [];
    public fieldDepth: number = 0;
    public uuid: string = null;
    public collapsed: boolean = true;
    public hasUnmappedChildren: boolean = false;
    public isCollection: boolean = false;
    public isArray: boolean = false;
    public isAttribute: boolean = false;
    public isPrimitive: boolean = false;
    public userCreated: boolean = false;
    public availableForSelection: boolean = true;
    public selectionExclusionReason: string = null;
    public docDef: DocumentDefinition = null;
    public namespace: NamespaceModel = null;

    private static uuidCounter: number = 0;

    constructor() {
        this.uuid = Field.uuidCounter.toString();
        Field.uuidCounter++;
    }

    public getNameWithNamespace(): string {
        if (!this.docDef || !this.namespace || this.namespace.isTarget) {
            return this.name;
        }
        return this.namespace.alias + ":" + this.name;
    }

    public isTerminal(): boolean {
        if (this.enumeration) {
            return true;
        }
        if (this.isCollection && !this.isPrimitive) {
            return false;
        }
        return (this.type != "COMPLEX");
    }

    public copy(): Field {
        var copy: Field = new Field();
        Object.assign(copy, this);
        copy.serviceObject = this.serviceObject;
        copy.children = [];
        for (let childField of this.children) {
            copy.children.push(childField.copy());
        }
        //console.log("Copied: " + this.name, { "src": this, "target": copy });
        return copy;
    }

    public copyFrom(that:Field ): void {
        Object.assign(this, that);
        this.serviceObject = that.serviceObject;
        this.children = [];
        for (let childField of that.children) {
            this.children.push(childField.copy());
        }
        //console.log("Copied: " + that.name, { "src": that, "target": this });
    }

    public isInCollection(): boolean {
        var parent: Field = this;
        while (parent != null) {
            if (parent.isCollection && (!parent.isPrimitive || parent.isArray)) {
                return true;
            }
            parent = parent.parentField;
        }
    }

    public isSource(): boolean {
        return (this.docDef != null) && this.docDef.isSource;
    }

    public getCollectionType(): string {
        return this.isCollection ? (this.isArray ? "ARRAY" : "LIST") : null;
    }

    public getFieldLabel(includePath: boolean): string {
        var fieldPath = includePath ? this.path : this.getNameWithNamespace();
        if (this != DocumentDefinition.getNoneField() && ConfigModel.getConfig().showTypes && this.type && !this.isPropertyOrConstant()) {
            fieldPath = fieldPath + " (" + this.type + ")";            
        }
        if (this.isProperty() && this.value != null) {
            fieldPath += " = " + this.value;
        }
        return fieldPath;
    }

    public isPropertyOrConstant(): boolean {
        return (this.docDef == null) ? false : this.docDef.initCfg.type.isPropertyOrConstant();
    }

    public isProperty(): boolean {
        return (this.docDef == null) ? false : this.docDef.initCfg.type.isProperty();
    }

    public isConstant(): boolean {
        return (this.docDef == null) ? false : this.docDef.initCfg.type.isConstant();
    }

    public static fieldHasUnmappedChild(field: Field): boolean {
        if (field == null) {
            return false;
        }
        if (field.isTerminal()) {
            return (field.partOfMapping == false);
        }
        for (let childField of field.children) {
            if (childField.hasUnmappedChildren || Field.fieldHasUnmappedChild(childField)) {
                return true;
            }
        }
        return false;
    }

    public static getFieldPaths(fields: Field[]): string[] {
        var paths: string[] = [];
        for (let field of fields) {
            paths.push(field.path);
        }
        return paths;
    }

    public static getFieldNames(fields: Field[]): string[] {
        var paths: string[] = [];
        for (let field of fields) {
            paths.push(field.name);
        }
        return paths;
    }

    public static getField(fieldPath: string, fields: Field[]): Field {
        for (let field of fields) {
            if (fieldPath == field.path) {
                return field;
            }
        }
        return null;
    }    

    public static alphabetizeFields(fields: Field[]): void {
        var fieldsByName: { [key:string]:Field; } = {};
        var fieldNames: string[] = [];
        for (let field of fields) {
            var name: string = field.name;
            var firstCharacter: string = name.charAt(0).toUpperCase();
            name = firstCharacter + name.substring(1);
            field.displayName = name;
            //if field is a dupe, discard it
            if (fieldsByName[name] != null) {
                continue;
            }
            fieldsByName[name] = field;
            fieldNames.push(name);
        }
        fieldNames.sort();
        fields.length = 0;
        for (let name of fieldNames) {
            fields.push(fieldsByName[name]);
        }

        for (let field of fields) {
            if (field.children && field.children.length) {
                this.alphabetizeFields(field.children);
            }
        }
    }
}