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
  CONSTANT_FIELD_JSON_TYPE,
  IPropertyField,
  PROPERTY_FIELD_JSON_TYPE,
} from '../contracts/mapping';
import {
  DocumentDefinition,
  PaddingField,
} from '../models/document-definition.model';
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import { MappedField, MappingModel } from '../models/mapping.model';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { IField } from '../contracts/common';

/**
 * Static routines for handling mappings.
 */
export class MappingUtil {
  static updateMappingsFromDocuments(cfg: ConfigModel): void {
    // TODO: check this non null operator
    for (const mapping of cfg.mappings!.mappings) {
      MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, true);
      MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, false);
    }
    for (const doc of cfg.getAllDocs()) {
      if (doc.id == null) {
        doc.id =
          'DOC.' +
          doc.name +
          '.' +
          Math.floor(Math.random() * 1000000 + 1).toString();
      }
    }
  }

  static updateMappedFieldsFromDocuments(
    mapping: MappingModel,
    cfg: ConfigModel,
    isSource: boolean
  ): void {
    let mappedFields: MappedField[] = mapping.getMappedFields(isSource);
    let mappedFieldIndex = -1;

    for (const mappedField of mappedFields) {
      let doc: DocumentDefinition | null = null;
      mappedFieldIndex += 1;

      if (
        mappedField.field instanceof PaddingField ||
        mappedField.mappingField === undefined
      ) {
        continue;
      }
      if (MappingUtil.isPropertyField(mappedField.mappingField)) {
        doc = isSource ? cfg.sourcePropertyDoc : cfg.targetPropertyDoc;
        // Preserve property Document ID from parsed data
        if (mappedField.mappingField.docId) {
          doc.id = mappedField.mappingField.docId;
        }
      } else if (MappingUtil.isConstantField(mappedField.mappingField)) {
        doc = cfg.constantDoc;
        // Preserve constant Document ID from parsed data
        if (mappedField.mappingField.docId) {
          doc.id = mappedField.mappingField.docId;
        }
      } else {
        if (mappedField.mappingField.docId == null) {
          cfg.errorService.addError(
            new ErrorInfo({
              message: `Could not find doc ID for mapped field ${mappedField.mappingField.name}`,
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
            })
          );
          continue;
        }
        doc = cfg.getDocForIdentifier(
          mappedField.mappingField.docId!,
          isSource
        );
        if (doc == null) {
          if (mappedField.mappingField.name != null) {
            cfg.errorService.addError(
              new ErrorInfo({
                message: `Could not find document for mapped field '${mappedField.mappingField.name}' \
with ID ${mappedField.mappingField.docId}`,
                level: ErrorLevel.ERROR,
                scope: ErrorScope.APPLICATION,
                type: ErrorType.INTERNAL,
              })
            );
          }
          continue;
        }

        doc.id = mappedField.mappingField.docId;
      }

      if (!mappedField.mappingField.path) {
        continue;
      }
      mappedField.field = doc.getField(mappedField.mappingField.path);

      if (mappedField.field == null) {
        // Check for collection instance.
        if (mappedField.mappingField.path.indexOf('<0>') >= 0) {
          mappedField.field = doc.getField(
            mappedField.mappingField.path!.replace('<0>', '<>')
          );
        } else if (
          MappingUtil.isConstantField(mappedField.mappingField) &&
          mappedField.mappingField.value &&
          mappedField.mappingField.fieldType
        ) {
          let constantField = cfg.constantDoc.getField(
            mappedField.mappingField.value
          );
          if (!constantField) {
            constantField = new Field();
          }
          constantField.value = mappedField.mappingField.value;
          constantField.type = mappedField.mappingField.fieldType;
          constantField.displayName = constantField.value;
          constantField.name = constantField.value;
          constantField.path = constantField.value;
          constantField.userCreated = true;
          mappedField.field = constantField;
          doc.addField(constantField);
        } else if (
          MappingUtil.isPropertyField(mappedField.mappingField) &&
          mappedField.mappingField.fieldType &&
          mappedField.mappingField.name &&
          mappedField.mappingField.path
        ) {
          const propMappingField = mappedField.mappingField as IPropertyField;
          let propertyField = doc.getField(propMappingField.path!);

          if (!propertyField) {
            propertyField = new Field();
          }
          let fieldName = propMappingField.name;
          propertyField.type = propMappingField.fieldType!;
          if (propMappingField.scope) {
            propertyField.scope = propMappingField.scope;
          }
          propertyField.displayName = fieldName!;
          propertyField.name = fieldName!;
          propertyField.path = propMappingField.path!;
          propertyField.userCreated = true;
          mappedField.field = propertyField;
          doc.addField(propertyField);
        } else {
          cfg.errorService.addError(
            new ErrorInfo({
              message: `Could not find field from document '${doc.name}' for mapped field '${mappedField.mappingField.name}'`,
              level: ErrorLevel.ERROR,
              scope: ErrorScope.APPLICATION,
              type: ErrorType.INTERNAL,
              object: { mappedField: mappedField, doc: doc },
            })
          );
          return;
        }
      }

      const zeroBasedIndex = +mappedField.mappingField.index!; // TODO: check this non null operator
      mappedFields = mapping.getMappedFields(isSource);
      if (zeroBasedIndex <= mappedFieldIndex) {
        mappedFields[mappedFieldIndex] = mappedField;
      } else {
        cfg.mappingService.addPlaceholders(
          zeroBasedIndex - mappedFieldIndex,
          mapping,
          mappedFieldIndex,
          isSource
        );
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
      sourceFieldPaths = sourceFieldPaths.concat(
        Field.getFieldPaths(doc.getAllFields())
      );
    }
    let targetSourcePaths: string[] = [];
    for (const doc of cfg.getDocs(false)) {
      targetSourcePaths = targetSourcePaths.concat(
        Field.getFieldPaths(doc.getAllFields())
      );
    }
    // TODO: check these non null operator
    while (index < cfg.mappings!.mappings.length) {
      const mapping: MappingModel = cfg.mappings!.mappings[index];
      const mappingIsStale: boolean = this.isMappingStale(
        mapping,
        sourceFieldPaths,
        targetSourcePaths
      );
      if (mappingIsStale) {
        cfg.mappings!.mappings.splice(index, 1);
      } else {
        index++;
      }
    }
  }

  private static isMappingStale(
    mapping: MappingModel,
    sourceFieldPaths: string[],
    targetSourcePaths: string[]
  ): boolean {
    for (const field of mapping.getFields(true)) {
      if (
        !(field instanceof PaddingField) &&
        sourceFieldPaths.indexOf(field.path) === -1
      ) {
        return true;
      }
    }
    for (const field of mapping.getFields(false)) {
      if (
        !(field instanceof PaddingField) &&
        targetSourcePaths.indexOf(field.path) === -1
      ) {
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
        cfg.errorService.addError(
          new ErrorInfo({
            message: `Could not find document with identifier '${parsedDoc.id}' for namespace override.`,
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: {
              identifier: parsedDoc.id,
              parsedDoc: parsedDoc,
              docs: docs,
            },
          })
        );
        continue;
      }

      doc.namespaces = [...parsedDoc.namespaces];
    }
  }

  private static getDocById(
    documentId: string,
    docs: DocumentDefinition[]
  ): DocumentDefinition | null {
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

  static activeMapping(cfg: ConfigModel): boolean {
    return !!cfg?.mappings?.activeMapping;
  }

  /**
   * Return true if the specified mapped field array has any established field actions,
   * false otherwise.
   *
   * @param fields
   */
  static hasFieldAction(fields: MappedField[]): boolean {
    for (let field of fields) {
      if (field.actions.length > 0) {
        return true;
      }
    }
    return false;
  }

  static hasMappedCollection(
    mapping: MappingModel,
    isSource: boolean
  ): boolean | null {
    const mappedFields = mapping.getMappedFields(isSource);
    return (
      mapping.isFullyMapped() &&
      mappedFields[0].field &&
      mappedFields[0].field.isInCollection()
    );
  }

  static isPropertyField(field: IField) {
    return field?.jsonType === PROPERTY_FIELD_JSON_TYPE;
  }

  static isConstantField(field: IField) {
    return field?.jsonType === CONSTANT_FIELD_JSON_TYPE;
  }
}
