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

import { MappingModel, MappedField } from './mapping.model';
import { LookupTable } from '../models/lookup-table.model';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { FieldAction, FieldActionArgumentValue, FieldActionDefinition } from './field-action.model';
import { TransitionMode } from './transition.model';
import { DocumentDefinition } from '../models/document-definition.model';

import { DataMapperUtil } from '../common/data-mapper-util';

export class MappingDefinition {
  name: string = null;
  mappings: MappingModel[] = [];
  activeMapping: MappingModel = null;
  parsedDocs: DocumentDefinition[] = [];
  templateText: string = null;

  private tables: LookupTable[] = [];
  private tablesBySourceTargetKey: { [key: string]: LookupTable; } = {};
  private tablesByName: { [key: string]: LookupTable; } = {};

  constructor() {
    this.name = 'UI.' + Math.floor((Math.random() * 1000000) + 1).toString();
  }

  templateExists(): boolean {
    return ((this.templateText != null) && (this.templateText !== ''));
  }

  addTable(table: LookupTable): void {
    this.tablesBySourceTargetKey[table.getInputOutputKey()] = table;
    this.tablesByName[table.name] = table;
    this.tables.push(table);
  }

  getTableByName(name: string): LookupTable {
    return this.tablesByName[name];
  }

  detectTableIdentifiers() {
    for (const t of this.getTables()) {
      if (t.sourceIdentifier && t.targetIdentifier) {
        continue;
      }
      let tableChanged = false;
      const m: MappingModel = this.getFirstMappingForLookupTable(t.name);
      if (m != null && m.transition.lookupTableName != null) {
        if (!t.sourceIdentifier) {
          const inputField: Field = m.getFields(true)[0];
          if (inputField) {
            t.sourceIdentifier = inputField.classIdentifier;
            tableChanged = true;
          }
        }
        if (!t.targetIdentifier) {
          const outputField: Field = m.getFields(false)[0];
          if (outputField) {
            t.targetIdentifier = outputField.classIdentifier;
            tableChanged = true;
          }
        }
      }
    }
    for (const m of this.mappings) {
      this.initializeMappingLookupTable(m);
    }
  }

  getTableBySourceTarget(sourceIdentifier: string, targetIdentifier: string): LookupTable {
    const key: string = sourceIdentifier + ':' + targetIdentifier;
    return this.tablesBySourceTargetKey[key];
  }

  getTables(): LookupTable[] {
    const tables: LookupTable[] = [];
    for (const key in this.tablesByName) {
      if (!this.tablesByName.hasOwnProperty(key)) {
        continue;
      }
      const table: LookupTable = this.tablesByName[key];
      tables.push(table);
    }
    return tables;
  }

  removeTableByName(name: string) {
    if (name) {
      const table = this.tablesByName[name];
      const iokey = table.getInputOutputKey();
      if (this.tablesByName[name]) {
        delete this.tables[this.tables.indexOf(table)];
        delete this.tablesByName[name];
        delete this.tablesBySourceTargetKey[iokey];
      }
    }
  }

  getFirstMappingForLookupTable(lookupTableName: string): MappingModel {
    for (const m of this.mappings) {
      if (m.transition.lookupTableName === lookupTableName) {
        return m;
      }
    }
    return null;
  }

  removeStaleMappings(cfg: ConfigModel): void {
    let index = 0;
    let sourceFieldPaths: string[] = [];
    for (const doc of cfg.getDocs(true)) {
      sourceFieldPaths = sourceFieldPaths.concat(Field.getFieldPaths(doc.getAllFields()));
    }
    let targetSourcePaths: string[] = [];
    for (const doc of cfg.getDocs(false)) {
      targetSourcePaths = targetSourcePaths.concat(Field.getFieldPaths(doc.getAllFields()));
    }
    while (index < this.mappings.length) {
      const mapping: MappingModel = this.mappings[index];
      const mappingIsStale: boolean = this.isMappingStale(mapping, sourceFieldPaths, targetSourcePaths);
      if (mappingIsStale) {
        this.mappings.splice(index, 1);
      } else {
        index++;
      }
    }
  }

  isMappingStale(mapping: MappingModel, sourceFieldPaths: string[], targetSourcePaths: string[]): boolean {
    for (const field of mapping.getFields(true)) {
      if (sourceFieldPaths.indexOf(field.path) === -1) {
        return true;
      }
    }
    for (const field of mapping.getFields(false)) {
      if (targetSourcePaths.indexOf(field.path) === -1) {
        return true;
      }
    }
    return false;
  }

  initializeMappingLookupTable(m: MappingModel): void {
    if (!(m.transition.mode === TransitionMode.ENUM
      && m.transition.lookupTableName == null
      && m.getFields(true).length === 1
      && m.getFields(false).length === 1)) {
      return;
    }
    let inputClassIdentifier: string = null;
    let outputClassIdentifier: string = null;

    const inputField: Field = m.getFields(true)[0];
    if (inputField) {
      inputClassIdentifier = inputField.classIdentifier;
    }
    const outputField: Field = m.getFields(true)[0];
    if (outputField) {
      outputClassIdentifier = outputField.classIdentifier;
    }
    if (inputClassIdentifier && outputClassIdentifier) {
      let table: LookupTable = this.getTableBySourceTarget(inputClassIdentifier, outputClassIdentifier);
      if (table == null) {
        table = new LookupTable();
        table.sourceIdentifier = inputClassIdentifier;
        table.targetIdentifier = outputClassIdentifier;
        this.addTable(table);
        m.transition.lookupTableName = table.name;
      } else {
        m.transition.lookupTableName = table.name;
      }
    }
  }

  /**
   * Return a document map for either the sources or targets panel contents.
   *
   * @param cfg
   * @param isSource
   */
  private getDocMap(cfg: ConfigModel, isSource: boolean): any {
    const docMap: any = {};
    for (const doc of cfg.getDocs(isSource)) {
      docMap[doc.uri] = doc;
    }
    return docMap;
  }

  updateMappingsFromDocuments(cfg: ConfigModel): void {
    const sourceDocMap: any = this.getDocMap(cfg, true);
    const targetDocMap: any = this.getDocMap(cfg, false);

    for (const mapping of this.mappings) {
      this.updateMappedFieldsFromDocuments(mapping, cfg, sourceDocMap, true);
      this.updateMappedFieldsFromDocuments(mapping, cfg, targetDocMap, false);
    }
    for (const doc of cfg.getAllDocs()) {
      if (doc.id == null) {
        doc.id = 'DOC.' + doc.name + '.' + Math.floor((Math.random() * 1000000) + 1).toString();
      }
    }
  }

  updateDocumentNamespacesFromMappings(cfg: ConfigModel): void {
    const docs: DocumentDefinition[] = cfg.getDocs(false);
    for (const parsedDoc of this.parsedDocs) {
      if (parsedDoc.isSource) {
        continue;
      }
      if (parsedDoc.namespaces.length === 0) {
        continue;
      }

      const doc = DocumentDefinition.getDocumentByIdentifier(parsedDoc.id, docs);
      if (doc == null) {
        cfg.errorService.error('Could not find document with identifier \'' + parsedDoc.id
          + '\' for namespace override.',
          { 'identifier': parsedDoc.id, 'parsedDoc': parsedDoc, 'docs': docs });
        continue;
      }

      doc.namespaces = [].concat(parsedDoc.namespaces);
    }
  }

  getAllMappings(includeActiveMapping: boolean): MappingModel[] {
    const mappings: MappingModel[] = [].concat(this.mappings);
    if (includeActiveMapping) {
      if (this.activeMapping == null) {
        return mappings;
      }
      for (const mapping of mappings) {
        if (mapping === this.activeMapping) {
          return mappings;
        }
      }
      mappings.push(this.activeMapping);
    }
    return mappings;
  }

  findMappingsForField(field: Field): MappingModel[] {
    const mappingsForField: MappingModel[] = [];
    for (const m of this.mappings) {
      if (m.isFieldMapped(field)) {
        mappingsForField.push(m);
      }
    }
    return mappingsForField;
  }

  removeMapping(m: MappingModel): boolean {
    return DataMapperUtil.removeItemFromArray(m, this.mappings);
  }

  removeFieldFromAllMappings(field: Field): void {
    for (const mapping of this.getAllMappings(true)) {
      const mappedField: MappedField = mapping.getMappedFieldForField(field);
      if (mappedField != null) {
        mapping.removeMappedField(mappedField, field.isSource());
      }
    }
  }

  /**
   * Remove any mappings referencing the specified document ID.
   *
   * @param docId - Specified document ID
   */
  removeDocumentReferenceFromAllMappings(docId: string) {
    for (const mapping of this.getAllMappings(true)) {
      for (const mappedField of mapping.getAllFields()) {
        if (mappedField.docDef.id === docId) {
          this.removeFieldFromAllMappings(mappedField);
          this.removeMapping(mapping);
        }
      }
    }
  }

  updateMappedFieldsFromDocuments(mapping: MappingModel, cfg: ConfigModel, docMap: any, isSource: boolean): void {
    const mappedFields: MappedField[] = mapping.getMappedFields(isSource);

    for (const mappedField of mappedFields) {
      let doc: DocumentDefinition = null;

      if (mappedField.parsedData.fieldIsProperty) {
        doc = cfg.propertyDoc;
      } else if (mappedField.parsedData.fieldIsConstant) {
        doc = cfg.constantDoc;
      } else {
        if (docMap === null) {
          docMap = this.getDocMap(cfg, isSource);
        }
        doc = docMap[mappedField.parsedData.parsedDocURI] as DocumentDefinition;
        if (doc == null) {
          if (mappedField.parsedData.parsedName != null) {
            cfg.errorService.error('Could not find document for mapped field \'' + mappedField.parsedData.parsedName +
              '\' at URI ' + mappedField.parsedData.parsedDocURI, null);
          }
          continue;
        }

        if (mappedField.parsedData.parsedDocID == null) {
          cfg.errorService.error('Could not find doc ID for mapped field ' + mappedField.parsedData.parsedName, null);
          continue;
        }
        doc.id = mappedField.parsedData.parsedDocID;
      }
      mappedField.field = null;
      if (!mappedField.parsedData.userCreated) {
        mappedField.field = doc.getField(mappedField.parsedData.parsedPath);
      }
      if (mappedField.field == null) {
        if (mappedField.parsedData.fieldIsConstant || mappedField.parsedData.fieldIsProperty) {
          const constantField: Field = new Field();
          constantField.value = mappedField.parsedData.parsedValue;
          constantField.type = mappedField.parsedData.parsedValueType;
          constantField.displayName = constantField.value;
          constantField.name = constantField.value;
          constantField.path = constantField.value;
          constantField.userCreated = true;
          mappedField.field = constantField;
          doc.addField(constantField);
        } else if (mappedField.parsedData.userCreated) {
          const path: string = mappedField.parsedData.parsedPath;

          mappedField.field = new Field();
          mappedField.field.serviceObject.jsonType = 'io.atlasmap.xml.v2.XmlField';
          mappedField.field.path = path;
          mappedField.field.type = mappedField.parsedData.parsedValueType;
          mappedField.field.userCreated = true;

          const lastSeparator: number = path.lastIndexOf('/');

          const parentPath: string = (lastSeparator > 0) ? path.substring(0, lastSeparator) : null;
          let fieldName: string = (lastSeparator === -1) ? path : path.substring(lastSeparator + 1);
          let namespaceAlias: string = null;
          if (fieldName.indexOf(':') !== -1) {
            namespaceAlias = fieldName.split(':')[0];
            fieldName = fieldName.split(':')[1];
          }

          mappedField.field.name = fieldName;
          mappedField.field.displayName = fieldName;
          mappedField.field.isAttribute = (fieldName.indexOf('@') !== -1);
          mappedField.field.namespaceAlias = namespaceAlias;

          if (parentPath != null) {
            mappedField.field.parentField = doc.getField(parentPath);
          }

          doc.addField(mappedField.field);
        } else {
          cfg.errorService.error('Could not find field from document for mapped field \'' + mappedField.parsedData.parsedName + '\'',
            { 'mappedField': mappedField, 'doc': doc });
          return;
        }
      }

      // Process field actions.
      mappedField.actions = [];
      if (mappedField.parsedData.parsedActions.length > 0) {

        for (const action of mappedField.parsedData.parsedActions) {

          const actionDefinition = cfg.fieldActionService.getActionDefinitionForName(action.name);
          if (actionDefinition == null) {
            cfg.errorService.error('Could not find field action definition for action \'' + action.name + '\'', null);
            continue;
          }
          actionDefinition.populateFieldAction(action);
          mappedField.actions.push(action);
        }
      }

      const isSeparate: boolean = mapping.transition.isOneToManyMode();
      const isCombine: boolean = mapping.transition.isManyToOneMode();
      mappedField.index = +mappedField.parsedData.parsedIndex;
      mappedField.updateSeparateOrCombineFieldAction(isSeparate, isCombine, isSource, false, false);
    }
  }

}
