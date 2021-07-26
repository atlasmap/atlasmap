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
  InspectionType,
} from '../contracts/common';

import { CommonUtil } from '../utils/common-util';
import { DocumentInitializationModel } from './config.model';
import { Field } from './field.model';
import { MappingDefinition } from './mapping-definition.model';

export class NamespaceModel {
  private static unqualifiedNamespace: NamespaceModel;

  alias: string;
  uri: string;
  locationUri: string | undefined;
  createdByUser = false;
  isTarget = false;

  static getUnqualifiedNamespace(): NamespaceModel {
    if (NamespaceModel.unqualifiedNamespace == null) {
      const ns: NamespaceModel = new NamespaceModel();
      ns.alias = 'Unqualified';
      NamespaceModel.unqualifiedNamespace = ns;
    }
    return NamespaceModel.unqualifiedNamespace;
  }

  getPrettyLabel(): string {
    if (this === NamespaceModel.getUnqualifiedNamespace()) {
      return this.alias;
    }
    return (
      (this.isTarget ? 'Target' : this.alias) +
      ' [' +
      (this.uri == null ? 'NO URI' : this.uri) +
      ']'
    );
  }

  copy(): NamespaceModel {
    const copy: NamespaceModel = new NamespaceModel();
    Object.assign(copy, this);
    return copy;
  }

  copyFrom(that: NamespaceModel): void {
    Object.assign(this, that);
  }
}

export class PaddingField extends Field {
  constructor(private _isSource: boolean) {
    super();
    this.name = '<padding field>';
    this.classIdentifier = '<padding field>';
    this.type = FieldType.NONE;
    this.displayName = '<padding field>';
    this.path = '';
  }

  isSource(): boolean {
    return this._isSource;
  }
}

export class DocumentDefinition {
  private static padField: Field;

  LEFT_BRACKET = '\x5b';
  RIGHT_BRACKET = '\x5d';

  initModel: DocumentInitializationModel;
  id: string;
  _type: DocumentType;
  name: string;
  description: string | undefined;
  uri: string;
  inspectionType: InspectionType;
  inspectionParameters: { [key: string]: string };
  inspectionSource: string;
  inspectionResult: string;
  isSource: boolean;
  isPropertyOrConstant: boolean;
  selectedRoot: string;
  classPath: string;
  initialized = false;
  errorOccurred = false;
  fields: Field[] = [];
  allFields: Field[] = [];
  terminalFields: Field[] = [];
  complexFieldsByClassIdentifier: { [key: string]: Field } = {};
  enumFieldsByClassIdentifier: { [key: string]: Field } = {};
  fieldsByPath: { [key: string]: Field } = {};
  fieldPaths: string[] = [];
  showFields = true;
  visibleInCurrentDocumentSearch = true;
  namespaces: NamespaceModel[] = [];
  characterEncoding: string;
  locale: string;

  set type(type: DocumentType) {
    this._type = type;
    this.isPropertyOrConstant =
      type === DocumentType.CONSTANT || type === DocumentType.PROPERTY;
  }

  get type(): DocumentType {
    return this._type;
  }

  getComplexField(classIdentifier: string): Field {
    return this.complexFieldsByClassIdentifier[classIdentifier];
  }

  getEnumField(classIdentifier: string): Field {
    return this.enumFieldsByClassIdentifier[classIdentifier];
  }

  getAllFields(): Field[] {
    return [...this.allFields];
  }

  /**
   * Return true if the specified field name already exists in the specified document definition,
   * false otherwise.
   *
   * @param targetField
   * @param targetFieldDocDefType
   */
  fieldExists(
    targetField: Field,
    targetFieldDocDefType: DocumentType
  ): boolean {
    for (const field of this.getAllFields()) {
      if (
        field.name === targetField.name &&
        field.docDef.type === targetFieldDocDefType
      ) {
        return true;
      }
    }
    return false;
  }

  isFieldsExist(fields: Field[]): boolean {
    if (fields == null || fields.length === 0) {
      return true;
    }
    const foundFields: Field[] = this.getFields(Field.getFieldPaths(fields));
    return foundFields != null && fields.length === foundFields.length;
  }

  getFields(fieldPaths: string[]): Field[] {
    const fields: Field[] = [];
    for (const fieldPath of fieldPaths) {
      const field = this.getField(fieldPath);
      if (field != null) {
        fields.push(field);
      }
    }
    return fields;
  }

  getName(showTypes: boolean): string {
    let name = this.name;
    if (showTypes && !this.isPropertyOrConstant) {
      const type = this.type;
      if (type) {
        name += ' (' + type + ')';
      }
    }
    return name;
  }

  getNamespaceForAlias(alias: string): NamespaceModel {
    // TODO: check this non null operator
    return this.namespaces.find((ns) => alias === ns.alias)!;
  }

  getField(fieldPath: string): Field | null {
    if (!fieldPath) {
      return null;
    }
    let field: Field = this.fieldsByPath[fieldPath];
    // if we can't find the field we're looking for, find parent fields and populate their children
    const pathSeparator: string = FIELD_PATH_SEPARATOR;
    let originalPath: string = fieldPath;
    // strip beginning path separator from path
    if (originalPath != null && originalPath.indexOf(pathSeparator) === 0) {
      originalPath = originalPath.substring(1);
    }
    if (field == null && originalPath.indexOf(pathSeparator) !== -1) {
      let currentParentPath = '';
      while (originalPath.indexOf(pathSeparator) !== -1) {
        const currentPathSection: string = originalPath.substr(
          0,
          originalPath.indexOf(pathSeparator)
        );
        currentParentPath += pathSeparator + currentPathSection;
        const parentField: Field = this.fieldsByPath[currentParentPath];
        if (parentField == null) {
          if (originalPath.indexOf(pathSeparator) !== -1) {
            originalPath = originalPath.substr(
              originalPath.indexOf(pathSeparator) + 1
            );
            continue;
          } else {
            break;
          }
          // https://github.com/atlasmap/atlasmap/issues/1128
          // throw new Error('Could not populate parent field with path \''
          //  + currentParentPath + '\' (for: ' + fieldPath + ')');
        }
        this.populateChildren(parentField);
        if (originalPath.indexOf(pathSeparator) !== -1) {
          originalPath = originalPath.substr(
            originalPath.indexOf(pathSeparator) + 1
          );
        }
      }
      field = this.fieldsByPath[fieldPath];
    }
    return field;
  }

  getComplexFields(): Field[] {
    return this.discoverAllComplexFields(this.fields);
  }

  getTerminalFields(): Field[] {
    return [...this.terminalFields];
  }

  initializeFromFields(): void {
    if (this.type === DocumentType.JAVA) {
      this.prepareComplexFields();
    }

    Field.alphabetizeFields(this.fields);

    for (const field of this.fields) {
      this.populateFieldData(field);
      this.allFields.push(field);
      this.populateChildren(field);
    }

    this.fieldPaths.sort();
    this.initialized = true;
  }

  updateField(field: Field, oldPath: string | null): void {
    Field.alphabetizeFields(this.fields);
    if (
      oldPath != null &&
      oldPath.length > 0 &&
      this.fieldsByPath[oldPath] != null
    ) {
      delete this.fieldsByPath[oldPath];
      CommonUtil.removeItemFromArray(oldPath, this.fieldPaths);
    } else {
      CommonUtil.removeItemFromArray(field.path, this.fieldPaths);
    }
    this.populateFieldData(field);
    this.fieldPaths.sort();
  }

  addField(field: Field): void {
    if (!field.parentField || this.isPropertyOrConstant) {
      this.fields.push(field);
      Field.alphabetizeFields(this.fields);
    } else {
      this.populateChildren(field.parentField);
      field.parentField.children.push(field);
      Field.alphabetizeFields(field.parentField.children);
    }
    this.populateFieldData(field);
    this.allFields.push(field);
    this.fieldPaths.sort();
  }

  /**
   * Return true if the passed field is terminal or children are detected, false otherwise.
   * @param field - target field
   */
  populateChildren(field: Field): boolean {
    // populate complex fields
    if (field.isTerminal() || field.children.length > 0) {
      return true;
    }

    let cachedField = this.getComplexField(field.classIdentifier);
    if (cachedField == null) {
      return false;
    }

    // copy cached field contents
    cachedField = cachedField.copy();
    for (let childField of cachedField.children) {
      childField = childField.copy();
      childField.parentField = field;
      this.rewriteFieldPath(childField);
      this.populateFieldData(childField);
      field.children.push(childField);
    }

    if (field.children.length > 0) {
      this.fieldPaths.sort();
      return true;
    } else {
      return false;
    }
  }

  private rewriteFieldPath(field: Field) {
    const parent = field.parentField;
    const pathSegments = field.path.split(FIELD_PATH_SEPARATOR);
    field.path = parent.path + FIELD_PATH_SEPARATOR + pathSegments.slice(-1)[0];
    for (let child of field.children) {
      this.rewriteFieldPath(child);
    }
  }

  /**
   * Clear all fields in this document.
   */
  clearFields(): void {
    this.initialized = false;
    this.fields = [];
    this.allFields = [];
    this.terminalFields = [];
    this.fieldPaths = [];
    this.namespaces = [];
    this.fieldsByPath = {};
    this.complexFieldsByClassIdentifier = {};
    this.enumFieldsByClassIdentifier = {};
  }

  getFieldIndex(field: Field, fields: Field[]): number {
    for (let i = 0; i < fields.length; i++) {
      if (fields[i].path === field.path) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Remove the specified field from this document definition.
   *
   * @param field
   */
  removeField(field: Field): void {
    if (field == null) {
      return;
    }
    let targetIndex = this.getFieldIndex(field, this.fields);
    if (targetIndex > -1) {
      this.fields.splice(targetIndex, 1);
    }
    targetIndex = this.getFieldIndex(field, this.allFields);
    if (targetIndex > -1) {
      this.allFields.splice(targetIndex, 1);
    }
    targetIndex = this.getFieldIndex(field, this.terminalFields);
    if (targetIndex > -1) {
      this.terminalFields.splice(targetIndex, 1);
    }
    let oldFieldPath = field.path;
    CommonUtil.removeItemFromArray(oldFieldPath, this.fieldPaths);
    delete this.fieldsByPath[oldFieldPath];
    if (field.parentField != null) {
      CommonUtil.removeItemFromArray(field, field.parentField.children);
    }
  }

  updateFromMappings(mappingDefinition: MappingDefinition): void {
    if (mappingDefinition === null) {
      return;
    }

    for (const field of this.allFields) {
      field.partOfMapping = false;
      field.hasUnmappedChildren = false;
      field.partOfTransformation = false;
    }

    // FIXME: some of this work is happening N times for N source/target docs, should only happen once.
    for (const mapping of mappingDefinition.getAllMappings(true)) {
      for (const field of mapping.getAllFields()) {
        let parentField = field;
        // TODO: check this non null operator
        const partOfTransformation =
          mapping.getMappedFieldForField(field)!.actions.length > 0;
        while (parentField != null) {
          parentField.partOfMapping = true;
          parentField.partOfTransformation =
            parentField.partOfTransformation || partOfTransformation;
          parentField = parentField.parentField;
        }
      }
    }
    for (const field of this.allFields) {
      field.hasUnmappedChildren = Field.fieldHasUnmappedChild(field);
    }
  }

  private populateFieldData(field: Field): void {
    field.docDef = this;
    let newFieldKey = field.path;
    this.fieldPaths.push(newFieldKey);
    this.fieldsByPath[newFieldKey] = field;

    if (field.enumeration) {
      this.enumFieldsByClassIdentifier[field.classIdentifier] = field;
    }
    if (field.isTerminal()) {
      this.terminalFields.push(field);
    } else {
      for (const childField of field.children) {
        this.populateFieldData(childField);
        this.allFields.push(childField);
      }
    }
  }

  private prepareComplexFields(): void {
    const fields: Field[] = this.fields;

    // build complex field cache
    this.discoverComplexFields(fields);

    for (const key in this.complexFieldsByClassIdentifier) {
      if (!this.complexFieldsByClassIdentifier.hasOwnProperty(key)) {
        continue;
      }
      const cachedField: Field = this.complexFieldsByClassIdentifier[key];
      // remove children more than one level deep in cached fields
      for (const childField of cachedField.children) {
        childField.children = [];
      }
      // alphabetize complex field's childrein
      Field.alphabetizeFields(cachedField.children);
    }
  }

  private discoverAllComplexFields(fields: Field[]): Field[] {
    let complexFields: Field[] = [];

    for (const field of fields) {
      if (
        field.type === 'COMPLEX' &&
        (field.documentField.status === 'SUPPORTED' ||
          field.documentField.status === 'CACHED')
      ) {
        complexFields.push(field.copy());
      }
      if (field.children) {
        complexFields = complexFields.concat(
          this.discoverAllComplexFields(field.children)
        );
      }
    }
    return complexFields;
  }

  private discoverComplexFields(fields: Field[]): void {
    for (const field of fields) {
      if (field.type !== 'COMPLEX') {
        continue;
      }
      if (field.documentField.status === 'SUPPORTED') {
        this.complexFieldsByClassIdentifier[field.classIdentifier] =
          field.copy();
      }
      if (field.children) {
        this.discoverComplexFields(field.children);
      }
    }
  }
}
