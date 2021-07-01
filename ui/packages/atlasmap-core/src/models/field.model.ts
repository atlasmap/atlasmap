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
import {
  DocumentType,
  FIELD_PATH_SEPARATOR,
  FieldType,
  IField,
} from '../contracts/common';
import { DocumentDefinition } from './document-definition.model';

export class EnumValue {
  name: string;
  ordinal: number;
}

export class Field {
  private static uuidCounter = 0;

  name: string;
  classIdentifier: string;
  displayName: string;
  path: string;
  type: FieldType;
  scope: string | undefined;
  value: string;
  column: number;
  // The field properties read from document inspection result.
  documentField: IField = { jsonType: '' };
  parentField: Field;
  partOfMapping = false;
  partOfTransformation = false;
  visibleInCurrentDocumentSearch = true;
  enumeration = false;
  enumIndexValue: number;
  enumValues: EnumValue[] = [];
  children: Field[] = [];
  fieldDepth = 0;
  uuid: string;
  collapsed = true;
  hasUnmappedChildren = false;
  isCollection = false;
  isArray = false;
  isAttribute = false;
  isPrimitive = false;
  userCreated = false;
  docDef: DocumentDefinition;
  namespaceAlias: string | null;

  static fieldHasUnmappedChild(field: Field): boolean {
    if (field == null) {
      return false;
    }
    if (field.isTerminal()) {
      return field.partOfMapping === false;
    }
    for (const childField of field.children) {
      if (
        childField.hasUnmappedChildren ||
        Field.fieldHasUnmappedChild(childField)
      ) {
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
    // TODO: check this non null operator
    return fields.find((field) => fieldPath === field.path)!;
  }

  static alphabetizeFields(fields: Field[]): void {
    const fieldsByPath: { [key: string]: Field } = {};
    const fieldPaths: string[] = [];
    for (const field of fields) {
      let fieldKey = field.path;
      // Discard duplicate field keys, field names are repeatable.
      if (fieldsByPath[fieldKey] != null) {
        continue;
      }
      fieldsByPath[fieldKey] = field;
      fieldPaths.push(fieldKey);
    }
    fieldPaths.sort();
    fields.length = 0;
    for (const path of fieldPaths) {
      fields.push(fieldsByPath[path]);
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

  /**
   * Expand all fields above the current field.
   */
  expandToRoot() {
    let parent: Field = this;
    while (parent != null) {
      parent.collapsed = false;
      if (parent.isPropertyOrConstant()) {
        if (parent.docDef) {
          parent.docDef.showFields = true;
        }
      }
      parent = parent.parentField;
    }
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
    return this.type === 'COMPLEX';
  }

  isStringField(): boolean {
    return this.type === 'STRING';
  }

  isTerminal(): boolean {
    if (this.enumeration) {
      return true;
    }
    if (this.isCollection && !this.isPrimitive) {
      return false;
    }
    return this.type !== 'COMPLEX';
  }

  copy(): Field {
    const copy: Field = new Field();
    Object.assign(copy, this);

    // make these pointers to the same object, not copies
    copy.documentField = this.documentField;
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
    this.documentField = that.documentField;
    this.parentField = that.parentField;
    this.docDef = that.docDef;

    this.children = [];
    for (const childField of that.children) {
      this.children.push(childField.copy());
    }
  }

  // @ts-ignore
  getCollectionParentField(): Field {
    let parent: Field = this;
    while (parent != null) {
      if (parent.isCollection) {
        return parent;
      }
      parent = parent.parentField;
    }
  }

  isInCollection(): boolean {
    return this.getCollectionParentField() != null;
  }

  getCollectionCount(): number {
    let count = 0;
    let field: Field = this;
    while (field != null) {
      if (field.isCollection) {
        count++;
      }
      field = field.parentField;
    }
    return count;
  }

  isSource(): boolean {
    return this.docDef != null && this.docDef.isSource;
  }

  getCollectionType(): string | null {
    return this.isCollection ? (this.isArray ? 'ARRAY' : 'LIST') : null;
  }

  getFieldLabel(showTypes: boolean, includePath: boolean): string {
    let fieldPath = '';
    if (includePath) {
      fieldPath = this.path;
    } else {
      const pathComps = this.path.split(FIELD_PATH_SEPARATOR);
      // Check for a leaf path attribute field starting with '@'
      if (
        this.isAttribute &&
        pathComps.length > 0 &&
        pathComps[pathComps.length - 1].startsWith('@')
      ) {
        fieldPath = this.path.split('@')[1];
      } else {
        fieldPath = this.getNameWithNamespace();
      }
    }
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
    return this.docDef == null ? false : this.docDef.isPropertyOrConstant;
  }

  isProperty(): boolean {
    return this.docDef == null
      ? false
      : this.docDef.type === DocumentType.PROPERTY;
  }

  isConstant(): boolean {
    return this.docDef == null
      ? false
      : this.docDef.type === DocumentType.CONSTANT;
  }
}
