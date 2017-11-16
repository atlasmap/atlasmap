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

import { DocumentDefinition } from './document.definition.model';
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
    public partOfMapping = false;
    public partOfTransformation = false;
    public visibleInCurrentDocumentSearch = true;
    public selected = false;
    public enumeration = false;
    public enumValues: EnumValue[] = [];
    public children: Field[] = [];
    public fieldDepth = 0;
    public uuid: string = null;
    public collapsed = true;
    public hasUnmappedChildren = false;
    public isCollection = false;
    public isArray = false;
    public isAttribute = false;
    public isPrimitive = false;
    public userCreated = false;
    public docDef: DocumentDefinition = null;
    public namespaceAlias: string = null;

    private static uuidCounter = 0;

    constructor() {
        this.uuid = Field.uuidCounter.toString();
        Field.uuidCounter++;
    }

    public getNameWithNamespace(): string {
        if (!this.docDef || !this.namespaceAlias) {
            return this.name;
        }
        return this.namespaceAlias + ':' + this.name;
    }

    public isParentField(): boolean {
        if (this.isCollection && !this.isPrimitive) {
            return true;
        }
        return (this.type == 'COMPLEX');
    }

    public isStringField(): boolean {
        return (this.type == 'STRING');
    }

    public isTerminal(): boolean {
        if (this.enumeration) {
            return true;
        }
        if (this.isCollection && !this.isPrimitive) {
            return false;
        }
        return (this.type != 'COMPLEX');
    }

    public copy(): Field {
        const copy: Field = new Field();
        Object.assign(copy, this);

        //make these pointers to the same object, not copies
        copy.serviceObject = this.serviceObject;
        copy.parentField = this.parentField;
        copy.docDef = this.docDef;

        copy.children = [];
        for (const childField of this.children) {
            copy.children.push(childField.copy());
        }
        //console.log("Copied: " + this.name, { "src": this, "target": copy });
        return copy;
    }

    public copyFrom(that: Field): void {
        Object.assign(this, that);

        //make these pointers to the same object, not copies
        this.serviceObject = that.serviceObject;
        this.parentField = that.parentField;
        this.docDef = that.docDef;

        this.children = [];
        for (const childField of that.children) {
            this.children.push(childField.copy());
        }
        //console.log("Copied: " + that.name, { "src": that, "target": this });
    }

    public getCollectionParentField(): Field {
        let parent: Field = this;
        while (parent != null) {
            if (parent.isCollection) {
                return parent;
            }
            parent = parent.parentField;
        }
        return null;
    }

    public isInCollection(): boolean {
        return (this.getCollectionParentField() != null);
    }

    public isSource(): boolean {
        return (this.docDef != null) && this.docDef.isSource;
    }

    public getCollectionType(): string {
        return this.isCollection ? (this.isArray ? 'ARRAY' : 'LIST') : null;
    }

    public getFieldLabel(includePath: boolean): string {
        let fieldPath = includePath ? this.path : this.getNameWithNamespace();
        if (this != DocumentDefinition.getNoneField() && ConfigModel.getConfig().showTypes && this.type && !this.isPropertyOrConstant()) {
            fieldPath = fieldPath + ' (' + this.type + ')';
        }
        if (this.isProperty() && this.value != null) {
            fieldPath += ' = ' + this.value;
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
        for (const childField of field.children) {
            if (childField.hasUnmappedChildren || Field.fieldHasUnmappedChild(childField)) {
                return true;
            }
        }
        return false;
    }

    public static getFieldPaths(fields: Field[]): string[] {
        const paths: string[] = [];
        for (const field of fields) {
            paths.push(field.path);
        }
        return paths;
    }

    public static getFieldNames(fields: Field[]): string[] {
        const paths: string[] = [];
        for (const field of fields) {
            paths.push(field.name);
        }
        return paths;
    }

    public static getField(fieldPath: string, fields: Field[]): Field {
        for (const field of fields) {
            if (fieldPath == field.path) {
                return field;
            }
        }
        return null;
    }

    public static alphabetizeFields(fields: Field[]): void {
        const fieldsByName: { [key: string]: Field; } = {};
        const fieldNames: string[] = [];
        for (const field of fields) {
            let name: string = field.name;
            const firstCharacter: string = name.charAt(0).toUpperCase();
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
        for (const name of fieldNames) {
            fields.push(fieldsByName[name]);
        }

        for (const field of fields) {
            if (field.children && field.children.length) {
                this.alphabetizeFields(field.children);
            }
        }
    }
}
