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
import { ConfigModel } from './config.model';
import { Field } from './field.model';
import { TransitionModel } from './transition.model';

import { DataMapperUtil } from '../common/data-mapper-util';
import { FieldAction } from './field-action.model';
import { PaddingField } from './document-definition.model';

export class MappedFieldParsingData {
  parsedName: string | null = null;
  parsedPath: string | null = null;
  parsedValue: string | null = null;
  parsedDocID: string | null = null;
  parsedDocURI: string | null = null;
  parsedIndex: string | null = null;
  fieldIsProperty = false;
  fieldIsConstant = false;
  parsedValueType: string | null = null;
  parsedActions: FieldAction[] = [];
  userCreated = false;
}

export class MappedField {
  parsedData: MappedFieldParsingData = new MappedFieldParsingData();
  field: Field | null;
  actions: FieldAction[] = [];

  static sortMappedFieldsByPath(mappedFields: MappedField[]): MappedField[] {
    if (mappedFields == null || mappedFields.length === 0) {
      return [];
    }
    const fieldsByPath: { [key: string]: MappedField; } = {};
    const fieldPaths: string[] = [];
    for (const mappedField of mappedFields) {
      if (mappedField == null || mappedField.field == null) {
        continue;
      }
      const path: string = mappedField.field.path;
      fieldsByPath[path] = mappedField;
      fieldPaths.push(path);
    }
    fieldPaths.sort();
    const result: MappedField[] = [];
    for (const name of fieldPaths) {
      result.push(fieldsByPath[name]);
    }
    return result;
  }

  isPadField(): boolean {
    return this.field instanceof PaddingField;
  }

  isSource(): boolean {
    return this.field ? this.field.isSource() : false;
  }

  removeAction(action: FieldAction): void {
    DataMapperUtil.removeItemFromArray(action, this.actions);
  }

}

export class MappingModel {
  cfg: ConfigModel;
  uuid: string;

  sourceFields: MappedField[] = [];
  targetFields: MappedField[] = [];
  transition: TransitionModel = new TransitionModel();

  constructor() {
    this.uuid = 'mapping.' + Math.floor((Math.random() * 1000000) + 1).toString();
    this.cfg = ConfigModel.getConfig();
  }

  getFirstCollectionField(isSource: boolean): Field | null {
    for (const f of isSource ? this.sourceFields : this.targetFields) {
      if (f.field && f.field.isInCollection()) {
        return f.field;
      }
    }
    return null;
  }

  isLookupMode(): boolean {
    for (const f of this.sourceFields.concat(this.targetFields)) {
      if (f.field && f.field.enumeration) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add the specified field to this field mapping.
   *
   * @param field - field to add to the mapping
   * @param first - if true add the field to the beginning of the array, last otherwise.
   */
  addField(field: Field, first: boolean): MappedField {
    const mappedFields = this.getMappedFields(field.isSource());
    if (mappedFields.length == 1) {
      const mappedField: MappedField = mappedFields[0];
      if (!mappedField.field) {
        mappedField.field = field;
        return mappedField;
      }
    }
    const mappedField: MappedField = new MappedField();
    mappedField.field = field;
    if (first) {
      mappedFields.unshift(mappedField);
    } else {
      mappedFields.push(mappedField);
    }
    return mappedField;
  }

  /**
   * Remove the specified field from this field mapping.
   *
   * @param field
   */
  removeField(field: Field) {
    const mappedFields = this.getMappedFields(field.isSource());
    DataMapperUtil.removeItemFromArray(mappedFields.find(mf => mf.field === field), mappedFields);
  }

  /**
   * Return the number of user-defined (non-padding) fields in this mapping.
   *
   * @param field
   */
  getUserFieldCount(field: Field): number {
    const mappedFields = this.getMappedFields(field.isSource());
    let userFieldCount = 0;

    for (const mappedField of mappedFields) {
      if (!mappedField.isPadField()) {
        userFieldCount++;
      }
    }
    return userFieldCount;
  }

  hasMappedField(isSource: boolean) {
    return isSource ? this.sourceFields.length > 0 : this.targetFields.length > 0;
  }

  isEmpty() {
    return this.sourceFields.length === 0 && this.targetFields.length === 0;
  }

  isFullyMapped(): boolean {
    return this.sourceFields.length > 0 && this.targetFields.length > 0;
  }

  addMappedField(mappedField: MappedField, isSource: boolean): void {
    this.getMappedFields(isSource).push(mappedField);
  }

  removeMappedField(mappedField: MappedField): void {
    if (!mappedField || !mappedField.field) {
      return;
    }
    DataMapperUtil.removeItemFromArray(mappedField, this.getMappedFields(mappedField.field!.isSource()));
    this.cfg.mappingService.notifyMappingUpdated();
  }

  getMappedFieldForField(field: Field): MappedField | null {
    if (!field) {
      return null;
    }

    for (const mappedField of this.getMappedFields(field.isSource())) {
      if (mappedField.field === field) {
        return mappedField;
      }
    }
    return null;
  }

  getMappedFieldByName(fieldPath: string, isSource: boolean): MappedField | null {
    if (!fieldPath) {
      return null;
    }
    const mappedFields = this.getMappedFields(isSource);
    for (let i=0; i<mappedFields.length; i++) {
      if (mappedFields[i].parsedData.parsedPath === fieldPath) {
        return mappedFields[i];
      }
    }
    return null;
  }

  getMappedFieldForIndex(index: string, isSource: boolean): MappedField | null {
    if (!index || index.length === 0) {
      return null;
    }
    const mappedFields = this.getMappedFields(isSource);
    if (+index - 1 > mappedFields.length - 1) {
      return null;
    }
    return mappedFields[+index - 1];
  }

  getIndexForMappedField(mappedField: MappedField): number | null {
    if (!mappedField || !mappedField.field) {
      return null;
    }
    return this.getMappedFields(mappedField.field.isSource()).indexOf(mappedField) + 1;
  }

  /**
   * Return an array of user mapped fields for the specified panel in this field pair instance.  No
   * data-mapper generated padding fields will be included.
   *
   * @param isSource - true source panel, false target panel
   */
  getUserMappedFields(isSource: boolean): MappedField[] {
    const workingFields = isSource ? this.sourceFields : this.targetFields;
    const resultFields: MappedField[] = [new MappedField()];

    for (const mappedField of workingFields) {
      if (!mappedField.isPadField()) {
        resultFields.push(mappedField);
      }
    }
    resultFields.shift();
    return resultFields;
  }

  getMappedFields(isSource: boolean): MappedField[] {
    return isSource ? this.sourceFields : this.targetFields;
  }

  getLastMappedField(isSource: boolean): MappedField | null {
    const fields: MappedField[] = this.getMappedFields(isSource);
    if ((fields != null) && (fields.length > 0)) {
      return fields[fields.length - 1];
    }
    return null;
  }

  getFields(isSource: boolean): Field[] {
    const fields: Field[] = [];
    for (const mappedField of this.getMappedFields(isSource)) {
      if (mappedField.field != null) {
        fields.push(mappedField.field);
      }
    }
    return fields;
  }

  getFieldNames(isSource: boolean): string[] {
    const fields: Field[] = this.getFields(isSource);
    Field.alphabetizeFields(fields);
    const names: string[] = [];
    for (const field of fields) {
      names.push(field.name);
    }
    return names;
  }

  getFieldPaths(isSource: boolean): string[] {
    const fields: Field[] = this.getFields(isSource);
    Field.alphabetizeFields(fields);
    const paths: string[] = [];
    for (const field of fields) {
      paths.push(field.path);
    }
    return paths;
  }

  hasFieldActions(): boolean {
    for (const mappedField of this.getAllMappedFields()) {
      if (mappedField.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  getAllFields(): Field[] {
    return this.getFields(true).concat(this.getFields(false));
  }

  getAllMappedFields(): MappedField[] {
    return this.getMappedFields(true).concat(this.getMappedFields(false));
  }

  isFieldMapped(field: Field): boolean {
    return this.getMappedFieldForField(field) != null;
  }

  hasTransformation(): boolean {
    const mappedFields: MappedField[] = this.getAllMappedFields();
    for (const mappedField of mappedFields) {
      if (mappedField.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Walk all target field mappings and return one of corresponding source field name
   * if the specified field is already the target of a previous mapping, null otherwise.
   *
   * @param field
   */
  public getMappedTarget(field: Field): string | null {
    // TODO: check this non null operator
    const mappings: MappingModel[] = this.cfg.mappings!.mappings;

    if (field.isSource()) {
      return null;
    }
    for (const m of mappings) {
      if (m.targetFields.length === 0) {
        continue;
      }

      for (const mappedOutputField of m.targetFields) {
        // TODO: check this non null operator
        if (mappedOutputField.field?.docDef === field.docDef
          && mappedOutputField.field!.path === field.path) {
          if (m.isFieldMapped(field)) {
            return m.sourceFields[0].field!.name;
          }
        }
      }
    }
    return null;
  }

}
