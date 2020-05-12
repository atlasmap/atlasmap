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

import { PaddingField, DocumentDefinition } from '../models/document-definition.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { Multiplicity } from '../models/field-action.model';
import { ErrorType, ErrorScope, ErrorInfo, ErrorLevel } from '../models/error.model';

/**
 * Static routines for handling mappings.
 */
export class MappingUtil {

  static updateMappingsFromDocuments(cfg: ConfigModel): void {
    const sourceDocMap = cfg.getDocUriMap(cfg, true);
    const targetDocMap = cfg.getDocUriMap(cfg, false);

    // TODO: check this non null operator
    for (const mapping of cfg.mappings!.mappings) {
      MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, sourceDocMap, true);
      MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, targetDocMap, false);
    }
    for (const doc of cfg.getAllDocs()) {
      if (doc.id == null) {
        doc.id = 'DOC.' + doc.name + '.' + Math.floor((Math.random() * 1000000) + 1).toString();
      }
    }
  }

  static updateMappedFieldsFromDocuments(mapping: MappingModel, cfg: ConfigModel, docMap: any, isSource: boolean): void {
    let mappedFields: MappedField[] = mapping.getMappedFields(isSource);
    let mappedFieldIndex = -1;

    for (const mappedField of mappedFields) {
      let doc: DocumentDefinition | null = null;
      mappedFieldIndex += 1;

      if (mappedField.parsedData.fieldIsProperty) {
        doc = cfg.propertyDoc;
      } else if (mappedField.parsedData.fieldIsConstant) {
        doc = cfg.constantDoc;
      } else {
        if (docMap === null) {
          docMap = cfg.getDocUriMap(cfg, isSource);
        }
        // TODO: check this non null operator
        doc = docMap[mappedField.parsedData.parsedDocURI!] as DocumentDefinition;
        if (doc == null) {
          if (mappedField.parsedData.parsedName != null) {
            cfg.errorService.addError(new ErrorInfo({
              message: `Could not find document for mapped field \'${mappedField.parsedData.parsedName}\' \
at URI ${mappedField.parsedData.parsedDocURI}`,
              level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
          }
          continue;
        }

        if (mappedField.parsedData.parsedDocID == null) {
          cfg.errorService.addError(new ErrorInfo({message: `Could not find doc ID for mapped field ${mappedField.parsedData.parsedName}`,
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
          continue;
        }
        doc.id = mappedField.parsedData.parsedDocID;
      }
      mappedField.field = null;
      if (!mappedField.parsedData.userCreated) {
        // TODO: check this non null operator
        mappedField.field = doc.getField(mappedField.parsedData.parsedPath!);
      }
      if (mappedField.field == null) {
        if (mappedField.parsedData.fieldIsConstant &&
          mappedField.parsedData.parsedValue &&
          mappedField.parsedData.parsedValueType) {
          let constantField =
            cfg.constantDoc.getField(mappedField.parsedData.parsedValue);
          if (!constantField) {
            constantField = new Field();
          }
          constantField.value = mappedField.parsedData.parsedValue;
          constantField.type = mappedField.parsedData.parsedValueType;
          constantField.displayName = constantField.value;
          constantField.name = constantField.value;
          constantField.path = constantField.value;
          constantField.userCreated = true;
          mappedField.field = constantField;
          doc.addField(constantField);
        } else if (mappedField.parsedData.fieldIsProperty &&
          mappedField.parsedData.parsedValue &&
          mappedField.parsedData.parsedValueType &&
          mappedField.parsedData.parsedName &&
          mappedField.parsedData.parsedPath) {
          let propertyField =
            cfg.propertyDoc.getField(mappedField.parsedData.parsedName);
          if (!propertyField) {
            propertyField = new Field();
          }
          const lastSeparator: number =
            mappedField.parsedData.parsedName.lastIndexOf('/');
          let fieldName = (lastSeparator === -1)
            ? mappedField.parsedData.parsedName
            : mappedField.parsedData.parsedName.substring(lastSeparator + 1);
          propertyField.value = mappedField.parsedData.parsedValue;
          propertyField.type = mappedField.parsedData.parsedValueType;
          propertyField.displayName = fieldName;
          propertyField.name = fieldName;
          propertyField.path = mappedField.parsedData.parsedPath;
          propertyField.userCreated = true;
          mappedField.field = propertyField;
          doc.addField(propertyField);
        } else if (mappedField.parsedData.userCreated || mappedField.parsedData.parsedPath) {
          const path: string = mappedField.parsedData.parsedPath!; // TODO: check this non null operator

          mappedField.field = new Field();
          mappedField.field.serviceObject.jsonType = 'io.atlasmap.xml.v2.XmlField';
          mappedField.field.path = path;
          mappedField.field.type = mappedField.parsedData.parsedValueType!; // TODO: check this non null operator
          mappedField.field.userCreated = true;

          const lastSeparator: number = path.lastIndexOf('/');

          const parentPath = (lastSeparator > 0) ? path.substring(0, lastSeparator) : null;
          let fieldName = (lastSeparator === -1) ? path : path.substring(lastSeparator + 1);
          let namespaceAlias: string | null = null;
          if (fieldName.indexOf(':') !== -1) {
            namespaceAlias = fieldName.split(':')[0];
            fieldName = fieldName.split(':')[1];
          }

          mappedField.field.name = fieldName;
          mappedField.field.displayName = fieldName;
          mappedField.field.isAttribute = (fieldName.indexOf('@') !== -1);
          mappedField.field.namespaceAlias = namespaceAlias;

          if (parentPath != null) {
            // TODO: check this non null operator
            mappedField.field.parentField = doc.getField(parentPath)!;
          }

          doc.addField(mappedField.field);
        } else {
          cfg.errorService.addError(new ErrorInfo({
            message: `Could not find field from document for mapped field \'${mappedField.parsedData.parsedName}\'`,
            level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL,
            object: { 'mappedField': mappedField, 'doc': doc }}));
          return;
        }
      }

      // Process field actions.
      mappedField.actions = [];
      if (mappedField.parsedData.parsedActions.length > 0) {

        for (const action of mappedField.parsedData.parsedActions) {
          let actionDefinition = null;
          if (action.name === 'CustomAction') {
            actionDefinition = cfg.fieldActionService.getActionDefinitionForName(action.argumentValues[0].value, Multiplicity.ONE_TO_ONE);
          } else {
            actionDefinition = cfg.fieldActionService.getActionDefinitionForName(action.name, Multiplicity.ONE_TO_ONE);
          }
          if (actionDefinition == null) {
            cfg.errorService.addError(new ErrorInfo({message: `Could not find field action definition for action \'${action.name}\'`,
              level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL}));
            continue;
          }
          actionDefinition.populateFieldAction(action);
          mappedField.actions.push(action);
        }
      }

      const zeroBasedIndex = +mappedField.parsedData.parsedIndex!; // TODO: check this non null operator
      mappedFields = mapping.getMappedFields(isSource);
      if (zeroBasedIndex <= mappedFieldIndex) {
        mappedFields[zeroBasedIndex] = mappedField;
      } else {
        cfg.mappingService.addPlaceholders(zeroBasedIndex - mappedFieldIndex, mapping, mappedFieldIndex, isSource);
      }
    }
  }

  /**
   * Check all mappings in the current context and remove if it refers to un-existing fields.
   *
   * @param cfg ConfigModel
   */
  static removeStaleMappings(cfg: ConfigModel): void {
    let index = 0;
    let sourceFieldPaths: string[] = [];
    for (const doc of cfg.getDocs(true)) {
      sourceFieldPaths = sourceFieldPaths.concat(Field.getFieldPaths(doc.getAllFields()));
    }
    let targetSourcePaths: string[] = [];
    for (const doc of cfg.getDocs(false)) {
      targetSourcePaths = targetSourcePaths.concat(Field.getFieldPaths(doc.getAllFields()));
    }
    // TODO: check these non null operator
    while (index < cfg.mappings!.mappings.length) {
      const mapping: MappingModel = cfg.mappings!.mappings[index];
      const mappingIsStale: boolean = this.isMappingStale(mapping, sourceFieldPaths, targetSourcePaths);
      if (mappingIsStale) {
        cfg.mappings!.mappings.splice(index, 1);
      } else {
        index++;
      }
    }
  }

  private static isMappingStale(mapping: MappingModel, sourceFieldPaths: string[], targetSourcePaths: string[]): boolean {

    for (const field of mapping.getFields(true)) {
      if (!(field instanceof PaddingField) && sourceFieldPaths.indexOf(field.path) === -1) {
        return true;
      }
    }
    for (const field of mapping.getFields(false)) {
      if (!(field instanceof PaddingField) && targetSourcePaths.indexOf(field.path) === -1) {
        return true;
      }
    }

    return false;
  }

  static updateDocumentNamespacesFromMappings(cfg: ConfigModel): void {
    const docs: DocumentDefinition[] = cfg.getDocs(false);

    // TODO: check this non null operator
    for (const parsedDoc of cfg.mappings!.parsedDocs) {
      if (!parsedDoc) {
        continue;
      }
      if (parsedDoc.isSource) {
        continue;
      }
      if (parsedDoc.namespaces.length === 0) {
        continue;
      }

      const doc = this.getDocById(parsedDoc.id, docs);
      if (doc == null) {
        cfg.errorService.addError(new ErrorInfo({
          message: `Could not find document with identifier \'${parsedDoc.id}\' for namespace override.`,
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL,
          object: { 'identifier': parsedDoc.id, 'parsedDoc': parsedDoc, 'docs': docs }}));
        continue;
      }

      doc.namespaces = [...parsedDoc.namespaces];
    }
  }

  private static getDocById(documentId: string, docs: DocumentDefinition[]): DocumentDefinition | null {
    if (documentId == null || docs == null || !docs.length) {
      return null;
    }
    for (const doc of docs) {
      if (doc.id === documentId) {
        return doc;
      }
    }
    return null;

  }

  static activeMapping(): boolean {
    const cfg = ConfigModel.getConfig();
    return !!(cfg?.mappings?.activeMapping);
  }

  /**
   * Return a string, in either text or HTML form, representing the
   * expression mapping of either the optionally specified mapping or
   * the active mapping if it exists, empty string otherwise.
   *
   * @param asHTML
   * @param mapping
   */
  static getMappingExpressionStr(asHTML: boolean, mapping?: any): string {
    const cfg = ConfigModel.getConfig();
    if (!mapping && !this.activeMapping()) {
      return '';
    }
    if (!mapping) {
      mapping = cfg.mappings?.activeMapping;
    }
    if (!(mapping.transition.expression)) {
      return '';
    }

    if ((mapping.transition.expression) && (mapping.transition.enableExpression)) {
      return (asHTML)
        ? mapping.transition.expression.expressionHTML
        : mapping.transition.expression.toText(true);
    }
    return '';
  }

}
