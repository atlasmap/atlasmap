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
import { DocumentType, InspectionType } from '../common/config.types';
import { MappingDefinition } from './mapping-definition.model';
import { DataMapperUtil } from '../common/data-mapper-util';
import { DocumentInitializationModel } from './config.model';

export class NamespaceModel {
  private static unqualifiedNamespace: NamespaceModel;

  alias: string;
  uri: string;
  locationUri: string;
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
    return (this.isTarget ? 'Target' : this.alias)
      + ' [' + (this.uri == null ? 'NO URI' : this.uri) + ']';
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
    this.type = '';
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
  description: string;
  uri: string;
  inspectionType: InspectionType;
  inspectionSource: string;
  inspectionResult: string;
  isSource: boolean;
  isPropertyOrConstant: boolean;
  selectedRoot: string;

  classPath: string;
  initialized = false;
  errorOccurred = false;
  pathSeparator = '/';
  fields: Field[] = [];
  allFields: Field[] = [];
  terminalFields: Field[] = [];
  complexFieldsByClassIdentifier: { [key: string]: Field; } = {};
  enumFieldsByClassIdentifier: { [key: string]: Field; } = {};
  fieldsByPath: { [key: string]: Field; } = {};
  fieldPaths: string[] = [];
  showFields = true;
  visibleInCurrentDocumentSearch = true;
  namespaces: NamespaceModel[] = [];
  characterEncoding: string;
  locale: string;

  set type(type: DocumentType) {
    this._type = type;
    this.isPropertyOrConstant = type === DocumentType.CONSTANT || type === DocumentType.PROPERTY;
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
  fieldExists(targetField: Field, targetFieldDocDefType: DocumentType): boolean {

    for (const field of this.getAllFields()) {
      if (field.name === targetField.name && field.docDef.type === targetFieldDocDefType) {
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
    return (foundFields != null) && (fields.length === foundFields.length);
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
    return this.namespaces.find(ns => alias === ns.alias)!;
  }

  getField(fieldPath: string): Field | null {
    if (!fieldPath) {
      return null;
    }
    let field: Field = this.fieldsByPath[fieldPath];
    // if we can't find the field we're looking for, find parent fields and populate their children
    const pathSeparator: string = this.pathSeparator;
    let originalPath: string = fieldPath;
    // strip beginning path separator from path
    if (originalPath != null && originalPath.indexOf(pathSeparator) === 0) {
      originalPath = originalPath.substring(1);
    }
    if (field == null && (originalPath.indexOf(pathSeparator) !== -1)) {
      let currentParentPath = '';
      while (originalPath.indexOf(pathSeparator) !== -1) {
        const currentPathSection: string = originalPath.substr(0, originalPath.indexOf(pathSeparator));
        currentParentPath += pathSeparator + currentPathSection;
        const parentField: Field = this.fieldsByPath[currentParentPath];
        if (parentField == null) {
          if (originalPath.indexOf(pathSeparator) !== -1) {
            originalPath = originalPath.substr(originalPath.indexOf(pathSeparator) + 1);
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
          originalPath = originalPath.substr(originalPath.indexOf(pathSeparator) + 1);
        }
      }
      field = this.fieldsByPath[fieldPath];
    }
    return field;
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
      this.populateFieldParentPaths(field, null, 0);
      this.populateFieldData(field);
    }

    this.fieldPaths.sort();
    this.initialized = true;
  }

  updateField(field: Field, oldPath: string): void {
    Field.alphabetizeFields(this.fields);
    if (!field.parentField || this.isPropertyOrConstant) {
      this.populateFieldParentPaths(field, null, 0);
    } else {
      const pathSeparator: string = this.pathSeparator;
      this.populateFieldParentPaths(field, field.parentField.path + pathSeparator,
        field.parentField.fieldDepth + 1);
    }
    if (oldPath != null && this.fieldsByPath[oldPath] != null) {
      delete (this.fieldsByPath[oldPath]);
    }
    DataMapperUtil.removeItemFromArray(field.path, this.fieldPaths);
    this.populateFieldData(field);
    this.fieldPaths.sort();
  }

  addField(field: Field): void {
    if (!field.parentField || this.isPropertyOrConstant) {
      this.fields.push(field);
      Field.alphabetizeFields(this.fields);
      this.populateFieldParentPaths(field, null, 0);
    } else {
      this.populateChildren(field.parentField);
      field.parentField.children.push(field);
      Field.alphabetizeFields(field.parentField.children);
      const pathSeparator: string = this.pathSeparator;
      this.populateFieldParentPaths(field, field.parentField.path + pathSeparator,
        field.parentField.fieldDepth + 1);
    }
    this.populateFieldData(field);
    this.fieldPaths.sort();
  }

  /**
   * Return true if the passed field is terminal or children are detected, false otherwise.
   * @param field - target field
   */
  populateChildren(field: Field): boolean {

    // populate complex fields
    if (field.isTerminal() || (field.children.length > 0)) {
      return true;
    }

    let cachedField = this.getComplexField(field.classIdentifier);
    if (cachedField == null) {
      return false;
    }

    // copy cached field children
    cachedField = cachedField.copy();
    const pathSeparator: string = this.pathSeparator;
    for (let childField of cachedField.children) {
      childField = childField.copy();
      childField.parentField = field;
      this.populateFieldParentPaths(childField, field.path + pathSeparator, field.fieldDepth + 1);
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

  /**
   * Remove the specified field from this document definition.
   *
   * @param field
   */
  removeField(field: Field): void {
    if (field == null) {
      return;
    }
    DataMapperUtil.removeItemFromArray(field, this.fields);
    DataMapperUtil.removeItemFromArray(field, this.allFields);
    DataMapperUtil.removeItemFromArray(field, this.terminalFields);
    DataMapperUtil.removeItemFromArray(field.path, this.fieldPaths);
    delete (this.fieldsByPath[field.path]);
    if (field.parentField != null) {
      DataMapperUtil.removeItemFromArray(field, field.parentField.children);
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
        const partOfTransformation = mapping.getMappedFieldForField(field)!.actions.length > 0;
        while (parentField != null) {
          parentField.partOfMapping = true;
          parentField.partOfTransformation = parentField.partOfTransformation || partOfTransformation;
          parentField = parentField.parentField;
        }
      }
    }
    for (const field of this.allFields) {
      field.hasUnmappedChildren = Field.fieldHasUnmappedChild(field);
    }
  }

  private populateFieldParentPaths(field: Field, parentPath: string | null, depth: number): void {
    if (parentPath == null) {
      parentPath = this.pathSeparator;
    }
    field.path = parentPath + field.getNameWithNamespace();
    if (field.isCollection) {
      field.path += field.isArray ? (this.LEFT_BRACKET + this.RIGHT_BRACKET) : '<>';
    }
    if (field.isAttribute) {
      field.path = parentPath += '@' + field.name;
    }
    if (field.serviceObject) {
      field.serviceObject.path = field.path;
    }
    field.fieldDepth = depth;
    const pathSeparator: string = this.pathSeparator;
    for (const childField of field.children) {
      childField.parentField = field;
      this.populateFieldParentPaths(childField, field.path + pathSeparator, depth + 1);
    }
  }

  private populateFieldData(field: Field): void {
    field.docDef = this;
    this.fieldPaths.push(field.path);
    this.allFields.push(field);
    this.fieldsByPath[field.path] = field;
    if (field.enumeration) {
      this.enumFieldsByClassIdentifier[field.classIdentifier] = field;
    }
    if (field.isTerminal()) {
      this.terminalFields.push(field);
    } else {
      for (const childField of field.children) {
        this.populateFieldData(childField);
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
      // alphebatize complex field's childrein
      Field.alphabetizeFields(cachedField.children);
    }
  }

  private discoverComplexFields(fields: Field[]): void {
    for (const field of fields) {
      if (field.type !== 'COMPLEX') {
        continue;
      }
      if (field.serviceObject.status === 'SUPPORTED') {
        this.complexFieldsByClassIdentifier[field.classIdentifier] = field.copy();
      }
      if (field.children) {
        this.discoverComplexFields(field.children);
      }
    }
  }

}
