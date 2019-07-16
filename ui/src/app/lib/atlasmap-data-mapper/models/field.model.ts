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

import { DocumentType } from '../common/config.types';
import { DocumentDefinition } from './document-definition.model';

export class EnumValue {
  name: string;
  ordinal: number;
}

export class Field {
  private static uuidCounter = 0;

  name: string = null;
  classIdentifier: string = null;
  displayName: string;
  path: string = null;
  type: string = null;
  value: string = null;
  serviceObject: any = new Object();
  parentField: Field;
  partOfMapping = false;
  partOfTransformation = false;
  visibleInCurrentDocumentSearch = true;
  enumeration = false;
  enumValues: EnumValue[] = [];
  children: Field[] = [];
  fieldDepth = 0;
  uuid: string = null;
  collapsed = true;
  hasUnmappedChildren = false;
  isCollection = false;
  isArray = false;
  isAttribute = false;
  isPrimitive = false;
  userCreated = false;
  docDef: DocumentDefinition = null;
  namespaceAlias: string = null;

  static fieldHasUnmappedChild(field: Field): boolean {
    if (field == null) {
      return false;
    }
    if (field.isTerminal()) {
      return (field.partOfMapping === false);
    }
    for (const childField of field.children) {
      if (childField.hasUnmappedChildren || Field.fieldHasUnmappedChild(childField)) {
        return true;
      }
    }
    return false;
  }

  static getFieldPaths(fields: Field[]): string[] {
    const paths: string[] = [];
    for (const field of fields) {
      paths.push(field.path);
    }
    return paths;
  }

  static getFieldNames(fields: Field[]): string[] {
    const paths: string[] = [];
    for (const field of fields) {
      paths.push(field.name);
    }
    return paths;
  }

  static getField(fieldPath: string, fields: Field[]): Field {
    for (const field of fields) {
      if (fieldPath === field.path) {
        return field;
      }
    }
    return null;
  }

  static alphabetizeFields(fields: Field[]): void {
    const fieldsByName: { [key: string]: Field; } = {};
    const fieldNames: string[] = [];
    for (const field of fields) {
      // if field is a dupe, discard it
      if (fieldsByName[field.name] != null) {
        continue;
      }
      fieldsByName[field.name] = field;
      fieldNames.push(field.name);
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

  constructor() {
    this.uuid = Field.uuidCounter.toString();
    Field.uuidCounter++;
  }

  getNameWithNamespace(): string {
    if (!this.namespaceAlias) {
      return this.name;
    }
    return this.namespaceAlias + ':' + this.name;
  }

  isParentField(): boolean {
    if (this.isCollection && !this.isPrimitive) {
      return true;
    }
    return (this.type === 'COMPLEX');
  }

  isStringField(): boolean {
    return (this.type === 'STRING');
  }

  isTerminal(): boolean {
    if (this.enumeration) {
      return true;
    }
    if (this.isCollection && !this.isPrimitive) {
      return false;
    }
    return (this.type !== 'COMPLEX');
  }

  copy(): Field {
    const copy: Field = new Field();
    Object.assign(copy, this);

    // make these pointers to the same object, not copies
    copy.serviceObject = this.serviceObject;
    copy.parentField = this.parentField;
    copy.docDef = this.docDef;

    copy.children = [];
    for (const childField of this.children) {
      copy.children.push(childField.copy());
    }
    // console.log("Copied: " + this.name, { "src": this, "target": copy });
    return copy;
  }

  copyFrom(that: Field): void {
    Object.assign(this, that);

    // make these pointers to the same object, not copies
    this.serviceObject = that.serviceObject;
    this.parentField = that.parentField;
    this.docDef = that.docDef;

    this.children = [];
    for (const childField of that.children) {
      this.children.push(childField.copy());
    }
  }

  getCollectionParentField(): Field {
    let parent: Field = this;
    while (parent != null) {
      if (parent.isCollection) {
        return parent;
      }
      parent = parent.parentField;
    }
    return null;
  }

  isInCollection(): boolean {
    return (this.getCollectionParentField() != null);
  }

  isSource(): boolean {
    return (this.docDef != null) && this.docDef.isSource;
  }

  getCollectionType(): string {
    return this.isCollection ? (this.isArray ? 'ARRAY' : 'LIST') : null;
  }

  getFieldLabel(showTypes: boolean, includePath: boolean): string {
    let fieldPath = includePath ? this.path : this.getNameWithNamespace();
    if (showTypes && this.type && !this.isPropertyOrConstant()) {
      fieldPath += ' (' + this.type + ')';
    } else if (this.isProperty() && this.value != null) {
      fieldPath += ' = ' + this.value;
      if (showTypes && this.type) {
        fieldPath += ' (' + this.type + ')';
      }
    }
    if (!fieldPath && !this.parentField) {
      fieldPath = '< Document Root >';
    }
    return fieldPath;
  }

  isPropertyOrConstant(): boolean {
    return (this.docDef == null) ? false : this.docDef.isPropertyOrConstant;
  }

  isProperty(): boolean {
    return (this.docDef == null) ? false : this.docDef.type === DocumentType.PROPERTY;
  }

  isConstant(): boolean {
    return (this.docDef == null) ? false : this.docDef.type === DocumentType.CONSTANT;
  }

}
