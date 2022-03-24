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
  ATLAS_MAPPING_JSON_TYPE,
  COLLECTION_JSON_TYPE,
  CONSTANT_FIELD_JSON_TYPE,
  FIELD_GROUP_JSON_TYPE,
  IAtlasMappingContainer,
  ICollection,
  IConstant,
  IFieldGroup,
  IJsonDataSource,
  ILookupEntry,
  ILookupTable,
  IMapping,
  IProperty,
  IPropertyField,
  MAPPING_JSON_TYPE,
  MappingType,
  PROPERTY_FIELD_JSON_TYPE,
} from '../contracts/mapping';
import {
  DATA_SOURCE_JSON_TYPE,
  DataSourceType,
  DocumentType,
  FIELD_PATH_SEPARATOR,
  FieldType,
  IDataSource,
  IField,
  IFieldAction,
} from '../contracts/common';
import {
  DocumentDefinition,
  NamespaceModel,
} from '../models/document-definition.model';
import {
  ErrorInfo,
  ErrorLevel,
  ErrorScope,
  ErrorType,
} from '../models/error.model';
import {
  FieldAction,
  FieldActionArgument,
  FieldActionArgumentValue,
} from '../models/field-action.model';
import {
  IXmlDataSource,
  IXmlField,
  IXmlNamespace,
  XML_DATA_SOURCE_JSON_TYPE,
  XML_ENUM_FIELD_JSON_TYPE,
  XML_MODEL_PACKAGE_PREFIX,
} from '../contracts/documents/xml';
import {
  JSON_DATA_SOURCE_JSON_TYPE,
  JSON_ENUM_FIELD_JSON_TYPE,
} from '../contracts/documents/json';
import { LookupTable, LookupTableEntry } from '../models/lookup-table.model';
import { MappedField, MappingModel } from '../models/mapping.model';
import { TransitionMode, TransitionModel } from '../models/transition.model';

import { ConfigModel } from '../models/config.model';
import { ExpressionModel } from '../models/expression.model';
import { Field } from '../models/field.model';
import { IAudits } from '../contracts/mapping-preview';
import { ICsvField } from '../contracts/documents/csv';
import { IJavaField } from '../contracts/documents/java';
import { JAVA_ENUM_FIELD_JSON_TYPE } from '../contracts/documents/java';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingUtil } from './mapping-util';
import { Multiplicity } from '../contracts/field-action';

export class MappingSerializer {
  static serializeMappings(
    cfg: ConfigModel,
    ignoreValue: boolean = true
  ): IAtlasMappingContainer {
    // TODO: check this non null operator
    const mappingDefinition: MappingDefinition = cfg.mappings!;
    let jsonMappings: IMapping[] = [];
    for (const mapping of mappingDefinition.mappings.filter((m) =>
      m.isFullyMapped()
    )) {
      try {
        const serializedFieldMapping = MappingSerializer.serializeFieldMapping(
          cfg,
          mapping,
          mapping.uuid,
          ignoreValue
        );
        if (serializedFieldMapping) {
          jsonMappings = jsonMappings.concat(serializedFieldMapping);
        }
      } catch (e) {
        const input: any = {
          mapping: mapping,
          'mapping def': mappingDefinition,
        };
        cfg.errorService.addError(
          new ErrorInfo({
            message:
              'Caught exception while attempting to serialize mapping, skipping. ',
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: { input: input, error: e },
          })
        );
      }
    }

    const serializedLookupTables = MappingSerializer.serializeLookupTables(
      cfg.mappings!
    );
    const constantDescriptions: IConstant[] =
      MappingSerializer.serializeConstants(cfg.constantDoc);
    const sourcePropertyDescriptions: IProperty[] =
      MappingSerializer.serializeProperties(cfg.sourcePropertyDoc);
    const targetPropertyDescriptions: IProperty[] =
      MappingSerializer.serializeProperties(cfg.targetPropertyDoc);
    const serializedDataSources: IDataSource[] =
      MappingSerializer.serializeDocuments(
        cfg.sourceDocs.concat(cfg.targetDocs),
        mappingDefinition
      );

    const payload: IAtlasMappingContainer = {
      AtlasMapping: {
        jsonType: ATLAS_MAPPING_JSON_TYPE,
        dataSource: serializedDataSources,
        mappings: { mapping: jsonMappings },
        name: cfg.mappings!.name ? cfg.mappings!.name : undefined,
        version: cfg.mappings!.version!,
        lookupTables: { lookupTable: serializedLookupTables },
        constants: { constant: constantDescriptions },
        properties: {
          property: sourcePropertyDescriptions.concat(
            targetPropertyDescriptions
          ),
        },
      },
    };
    return payload;
  }

  static serializeFieldMapping(
    cfg: ConfigModel,
    mapping: MappingModel,
    id: string,
    ignoreValue: boolean = true
  ): IMapping {
    const serializedInputFields = MappingSerializer.serializeFields(
      mapping,
      true,
      cfg,
      ignoreValue
    );
    const serializedOutputFields = MappingSerializer.serializeFields(
      mapping,
      false,
      cfg,
      ignoreValue
    );
    let jsonMapping: IMapping;
    const mappingExpression = cfg.expressionService.getMappingExpressionStr(
      false,
      mapping
    );

    if (
      mapping.transition.isManyToOneMode() ||
      mapping.transition.isForEachMode() ||
      mapping.transition.isExpressionMode()
    ) {
      const actions = [];
      if (mapping.transition.transitionFieldAction) {
        actions[0] = this.serializeAction(
          mapping.transition.transitionFieldAction,
          cfg
        );
      }
      const field = serializedInputFields;
      const inputFieldGroup: IFieldGroup = {
        jsonType: FIELD_GROUP_JSON_TYPE,
        actions,
        field,
      };
      if (mappingExpression.length > 0) {
        jsonMapping = {
          jsonType: MAPPING_JSON_TYPE,
          id: id,
          expression: mappingExpression,
          inputFieldGroup,
          outputField: serializedOutputFields,
        };
      } else {
        jsonMapping = {
          jsonType: MAPPING_JSON_TYPE,
          id: id,
          inputFieldGroup,
          outputField: serializedOutputFields,
        };
      }
    } else {
      if (
        mapping.transition.isOneToManyMode() &&
        mapping.transition.transitionFieldAction
      ) {
        const mappingAction = this.serializeAction(
          mapping.transition.transitionFieldAction,
          cfg
        );
        if (!serializedInputFields[0].actions) {
          serializedInputFields[0].actions = [];
        }
        if (mappingAction) {
          serializedInputFields[0].actions.unshift(mappingAction);
        }
      }
      if (mappingExpression.length > 0) {
        if (serializedInputFields[0].jsonType?.includes('FieldGroup')) {
          let serializedInputFieldGroup =
            serializedInputFields[0] as IFieldGroup;
          for (let i = 1; i < serializedInputFields.length; i++) {
            if (serializedInputFields[i].jsonType?.includes('FieldGroup')) {
              const serializedFieldGroup0 =
                serializedInputFields[0] as IFieldGroup;
              const serializedFieldGroupi = serializedInputFields[
                i
              ] as IFieldGroup;
              if (
                serializedFieldGroupi.field &&
                serializedFieldGroup0.field &&
                serializedFieldGroupi.field[0].path ===
                  serializedFieldGroup0.field[0].path
              ) {
                serializedInputFieldGroup.field?.push(
                  serializedFieldGroupi.field[0]
                );
              }
            }
            // TODO - Support input fields from different complex parent fields
            // in the same complex conditional expression.
          }

          jsonMapping = {
            jsonType: MAPPING_JSON_TYPE,
            id: id,
            expression: mappingExpression,
            inputFieldGroup: serializedInputFieldGroup,
            outputField: serializedOutputFields,
          };
        } else {
          jsonMapping = {
            jsonType: MAPPING_JSON_TYPE,
            id: id,
            expression: mappingExpression,
            inputField: serializedInputFields,
            outputField: serializedOutputFields,
          };
        }
      } else {
        jsonMapping = {
          jsonType: MAPPING_JSON_TYPE,
          id: id,
          inputField: serializedInputFields,
          outputField: serializedOutputFields,
        };
      }
    }

    if (mapping.transition.isEnumerationMode()) {
      jsonMapping.mappingType = MappingType.LOOKUP; /* @deprecated */
      if (mapping.transition.lookupTableName) {
        jsonMapping.lookupTableName = mapping.transition.lookupTableName;
      }
    }
    return jsonMapping;
  }

  static extractCheckVersion(json: IAtlasMappingContainer, cfg: ConfigModel) {
    const currentVersion = cfg.initializationService.getUIVersion();
    const mappingVersion = this.deserializeAtlasMappingVersion(json);
    const mappingVersionComps = mappingVersion.split('.');
    const currentVersionComps = currentVersion.split('.');
    if (
      (mappingVersion.length > 0 &&
        (currentVersionComps.length < 2 || mappingVersionComps.length < 2)) ||
      +currentVersionComps[0] < +mappingVersionComps[0] ||
      +currentVersionComps[1] < +mappingVersionComps[1]
    ) {
      cfg.errorService.addError(
        new ErrorInfo({
          message: `Mappings file version mismatch.  Expected ${currentVersion}, detected ${mappingVersion}`,
          level: ErrorLevel.ERROR,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.USER,
        })
      );
    }
    cfg.mappings!.version = currentVersion;
  }

  static async deserializeMappingServiceJSON(
    json: IAtlasMappingContainer,
    cfg: ConfigModel
  ) {
    // Process constants and properties before mappings.
    for (const field of MappingSerializer.deserializeConstants(json)) {
      cfg.constantDoc.addField(field);
    }
    for (const field of MappingSerializer.deserializeProperties(
      cfg,
      json,
      true
    )) {
      cfg.sourcePropertyDoc.addField(field);
    }
    for (const field of MappingSerializer.deserializeProperties(
      cfg,
      json,
      false
    )) {
      cfg.targetPropertyDoc.addField(field);
    }
    if (!cfg.mappings) {
      cfg.mappings = new MappingDefinition();
    }
    cfg.mappings.name = this.deserializeAtlasMappingName(json);
    this.extractCheckVersion(json, cfg);
    cfg.mappings.parsedDocs = cfg.mappings.parsedDocs.concat(
      MappingSerializer.deserializeDocs(json, cfg.mappings)!
    ); // TODO: check this non null operator
    cfg.mappings.mappings = cfg.mappings.mappings.concat(
      MappingSerializer.deserializeMappings(json, cfg)
    );
    for (const lookupTable of MappingSerializer.deserializeLookupTables(json)) {
      cfg.mappings.addTable(lookupTable);
    }
  }

  /**
   * Return the AtlasMap mappings file name from the specified JSON buffer or an empty string.
   *
   * @param json
   */
  static deserializeAtlasMappingName(json: IAtlasMappingContainer): string {
    if (json?.AtlasMapping?.name) {
      return json.AtlasMapping.name;
    } else {
      return '';
    }
  }

  /**
   * Return the AtlasMap mappings version from the specified JSON buffer or an empty
   * string.
   *
   * @param json JSON buffer
   * @returns version string or empty string
   */
  static deserializeAtlasMappingVersion(json: IAtlasMappingContainer): string {
    if (json?.AtlasMapping?.version) {
      return json.AtlasMapping.version;
    } else {
      return '';
    }
  }

  static addInputFieldGroupFields(
    inputField: IField[],
    mapping: MappingModel,
    cfg: ConfigModel
  ) {
    for (const field of inputField) {
      if (field.fieldType === 'COMPLEX') {
        MappingSerializer.addInputFieldGroupFields(
          (field as IFieldGroup).field!,
          mapping,
          cfg
        );
      } else {
        MappingSerializer.deserializeMappedField(mapping, field, true, cfg);
      }
    }
  }

  static deserializeFieldMapping(
    mappingJson: IMapping,
    cfg: ConfigModel
  ): MappingModel {
    const mapping = new MappingModel();
    mapping.uuid = mappingJson.id;
    mapping.sourceFields = [];
    mapping.targetFields = [];
    mapping.referenceFields = [];
    mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    const isLookupMapping =
      mappingJson.mappingType === 'LOOKUP' ||
      mappingJson.lookupTableName != null;

    if (
      mappingJson.mappingType &&
      mappingJson.mappingType !== MappingType.NONE
    ) {
      this.deserializeFieldMappingFromType(mapping, mappingJson, cfg);
      return mapping;
    }

    if (mappingJson.inputFieldGroup) {
      MappingSerializer.deserializeInputFieldGroup(mappingJson, mapping, cfg);
    } else {
      const inputField = mappingJson.inputField;

      if (inputField) {
        for (const field of inputField) {
          MappingSerializer.deserializeMappedField(mapping, field, true, cfg);
        }
      }
      if (
        mappingJson.outputField?.length &&
        mappingJson.outputField.length > 1
      ) {
        mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      }
      if (cfg.mappings) {
        MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, true);
      }
    }

    if (mappingJson.expression && mappingJson.expression.length > 0) {
      mapping.transition.enableExpression = true;
      mapping.transition.mode = TransitionMode.EXPRESSION;
      mapping.transition.expression = new ExpressionModel(mapping, cfg);
      mapping.transition.expression.insertText(mappingJson.expression);
    }

    for (const field of mappingJson.outputField) {
      MappingSerializer.deserializeMappedField(mapping, field, false, cfg);
    }
    MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, false);

    if (isLookupMapping) {
      mapping.transition.lookupTableName = mappingJson.lookupTableName!;
      mapping.transition.mode = TransitionMode.ENUM;
    }

    return mapping;
  }

  private static deserializeInputFieldGroup(
    mappingJson: IMapping,
    mapping: MappingModel,
    cfg: ConfigModel
  ) {
    if (!mappingJson.inputFieldGroup) {
      return;
    }

    if (
      mappingJson.expression &&
      mappingJson.inputFieldGroup!.fieldType === FieldType.COMPLEX
    ) {
      mapping.transition.expression.hasComplexField = true;
    }
    mapping.transition.mode = TransitionMode.MANY_TO_ONE;

    MappingSerializer.addInputFieldGroupFields(
      mappingJson.inputFieldGroup.field!,
      mapping,
      cfg
    );
    MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, true);

    if (
      mappingJson.inputFieldGroup.actions &&
      mappingJson.inputFieldGroup.actions[0]?.delimiter
    ) {
      mapping.transition.delimiter = TransitionModel.delimiterToModel(
        mappingJson.inputFieldGroup.actions[0]?.delimiter
      )?.delimiter;
      // Check for an InputFieldGroup containing a many-to-one action
      const firstAction = mappingJson.inputFieldGroup.actions[0];
      if (firstAction) {
        // @deprecated Support legacy ADM files that have transformation-action-based expressions.
        if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          mapping.transition.enableExpression = true;
          mapping.transition.mode = TransitionMode.EXPRESSION;
          mapping.transition.expression = new ExpressionModel(mapping, cfg);
          const expr = firstAction.Expression
            ? firstAction.Expression.expression
            : firstAction['expression'];
          mapping.transition.expression.insertText(expr);
        } else {
          mapping.transition.mode = TransitionMode.MANY_TO_ONE;
          const parsedAction = this.parseAction(firstAction);
          // TODO: check this non null operator
          parsedAction.definition =
            cfg.fieldActionService.getActionDefinitionForName(
              parsedAction.name,
              Multiplicity.MANY_TO_ONE
            )!;
          mapping.transition.transitionFieldAction = parsedAction;
        }
      }
    }
  }

  static deserializeAudits(audits: IAudits, errorType: ErrorType): ErrorInfo[] {
    const errors: ErrorInfo[] = [];
    if (!audits?.audit) {
      return errors;
    }
    for (const audit of audits.audit) {
      const msg = audit.status + '[' + audit.path + ']: ' + audit.message;
      errors.push(
        new ErrorInfo({
          message: msg,
          level: ErrorLevel[audit.status],
          scope: ErrorScope.MAPPING,
          type: errorType,
          object: audit.value,
        })
      );
    }
    return errors;
  }

  private static createInputFieldGroup(
    field: IField[],
    isComplex: boolean,
    docId?: string,
    path?: string
  ): IFieldGroup {
    const inputFieldGroup: IFieldGroup = {
      jsonType: FIELD_GROUP_JSON_TYPE,
      actions: [],
      docId: docId,
      path: path,
      field,
    };
    if (isComplex) {
      inputFieldGroup.fieldType = FieldType.COMPLEX;
    }
    return inputFieldGroup;
  }

  private static serializeDocuments(
    docs: DocumentDefinition[],
    mappingDefinition: MappingDefinition
  ): IDataSource[] {
    const serializedDocs: IDataSource[] = [];
    for (const doc of docs) {
      let serializedDoc: IDataSource = {
        jsonType: DATA_SOURCE_JSON_TYPE,
        id: doc.id,
        name: doc.name,
        description: doc.description,
        uri: doc.uri,
        dataSourceType: doc.isSource
          ? DataSourceType.SOURCE
          : DataSourceType.TARGET,
      };
      if (doc.characterEncoding != null) {
        serializedDoc.characterEncoding = doc.characterEncoding;
      }
      if (doc.locale != null) {
        serializedDoc.locale = doc.locale;
      }
      if (doc.type === DocumentType.XML || doc.type === DocumentType.XSD) {
        const xmlDoc = serializedDoc as IXmlDataSource;
        xmlDoc.jsonType = XML_DATA_SOURCE_JSON_TYPE;
        const namespaces: IXmlNamespace[] = [];
        for (const ns of doc.namespaces) {
          namespaces.push({
            alias: ns.alias,
            uri: ns.uri,
            locationUri: ns.locationUri,
            targetNamespace: ns.isTarget,
          });
        }
        if (!doc.isSource && mappingDefinition.templateText) {
          xmlDoc.template = mappingDefinition.templateText;
        }
        xmlDoc.xmlNamespaces = { xmlNamespace: namespaces };
      } else if (doc.type === DocumentType.JSON) {
        const jsonDoc = serializedDoc as IJsonDataSource;
        if (!doc.isSource && mappingDefinition.templateText) {
          jsonDoc.template = mappingDefinition.templateText;
        }
        jsonDoc.jsonType = JSON_DATA_SOURCE_JSON_TYPE;
      }

      serializedDocs.push(serializedDoc);
    }
    return serializedDocs;
  }

  private static serializeConstants(docDef: DocumentDefinition): IConstant[] {
    const constantDescriptions: IConstant[] = [];
    for (const field of docDef.fields) {
      // Use the constant value for the name.
      constantDescriptions.push({
        name: field.name,
        value: field.value,
        fieldType: field.type,
      });
    }
    return constantDescriptions;
  }

  private static serializeProperties(docDef: DocumentDefinition): IProperty[] {
    const propertyDescriptions: IProperty[] = [];
    for (const field of docDef.fields) {
      propertyDescriptions.push({
        name: field.name,
        fieldType: field.type,
        scope: field.scope,
        dataSourceType: docDef.isSource
          ? DataSourceType.SOURCE
          : DataSourceType.TARGET,
      });
    }
    return propertyDescriptions;
  }

  private static serializeLookupTables(
    mappingDefinition: MappingDefinition
  ): ILookupTable[] {
    const serializedTables: ILookupTable[] = [];
    const tables = mappingDefinition.getTables();
    if (!tables || !tables.length) {
      return serializedTables;
    }

    for (const table of tables) {
      const lookupEntries: ILookupEntry[] = [];
      for (const entry of table.lookupEntry) {
        const serializedEntry: ILookupEntry = {
          sourceValue: entry.sourceValue,
          sourceType: entry.sourceType,
          targetValue: entry.targetValue,
          targetType: entry.targetType,
        };
        lookupEntries.push(serializedEntry);
      }

      const serializedTable: ILookupTable = {
        lookupEntry: lookupEntries,
        name: table.name,
      };
      serializedTables.push(serializedTable);
    }
    return serializedTables;
  }

  /**
   * Generate serialized meta-data representing a direct-reference instance collection preview.
   *
   * @param cfg
   * @param mapping
   * @param field
   * @param serializedField
   * @param fieldsJson
   */
  private static processCollectionPreview(
    field: Field,
    serializedField: IField,
    fieldsJson: IField[]
  ) {
    serializedField.path = field.path.replace('<>', '<0>');
    const collectionInstanceInputFieldGroup =
      MappingSerializer.createInputFieldGroup(
        [serializedField],
        true,
        field.docDef.id,
        field.path
      );
    fieldsJson.push(collectionInstanceInputFieldGroup);
  }

  /**
   * Serialize field action arguments.
   *
   * @param action
   * @param cfg
   */
  private static processActionArguments(
    action: FieldAction,
    cfg: ConfigModel
  ): { [key: string]: string } {
    const actionArguments: { [key: string]: any } = {};
    if (action === null) {
      return actionArguments;
    }
    for (const argValue of action.argumentValues) {
      if (
        action.definition?.isCustom &&
        ['methodName', 'className', 'name'].includes(argValue.name)
      ) {
        continue;
      }
      actionArguments[argValue.name] = argValue.value;
      const argumentConfig: FieldActionArgument =
        action.definition!.getArgumentForName(argValue.name);
      if (argumentConfig == null) {
        cfg.errorService.addError(
          new ErrorInfo({
            message: `Cannot find action argument ${argValue.name}: ${argValue.value}`,
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
            object: action,
          })
        );
        continue;
      }
      if (argumentConfig.type === 'INTEGER') {
        actionArguments[argValue.name] = parseInt(argValue.value, 10);
      }
    }
    return actionArguments;
  }

  private static serializeFields(
    mapping: MappingModel,
    isSource: boolean,
    cfg: ConfigModel,
    ignoreValue: boolean = false
  ): IField[] {
    let collectionInputFieldGroup = null;
    let collectionInstanceInputFieldGroup = null;
    const fields: MappedField[] = mapping.getMappedFields(isSource);
    const fieldsJson: IField[] = [];

    for (const mappedField of fields) {
      if (!mappedField.field || mappedField.isPadField()) {
        continue;
      }

      const field: Field = mappedField.field;
      const serializedField: IField = {
        jsonType: field.documentField.jsonType,
        name: field.name,
        path: field.path,
        fieldType: field.type,
        docId: field.docDef.id,
      };

      // The 'attribute' field only applies to XML.
      if (field.documentField.jsonType?.includes(XML_MODEL_PACKAGE_PREFIX)) {
        (serializedField as IXmlField).attribute = field.isAttribute;
      }

      // Only capture a value for preview mode and constants.
      if (!ignoreValue || field.isConstant()) {
        if (field.value) {
          serializedField.value = field.value;
        } else {
          serializedField.value = '';
        }
      }

      if (
        field.docDef.type === DocumentType.XML ||
        field.docDef.type === DocumentType.XSD
      ) {
        (serializedField as IXmlField).userCreated = field.userCreated;
      } else if (
        field.docDef.type === DocumentType.JAVA &&
        !field.isPrimitive
      ) {
        (serializedField as IJavaField).className = field.classIdentifier;
      }

      if (field.isProperty()) {
        serializedField.jsonType = PROPERTY_FIELD_JSON_TYPE;
        serializedField.name = field.name;
        (serializedField as IPropertyField).scope = field.scope;
        serializedField.path = cfg.documentService.getPropertyPath(
          field.scope,
          field.name
        );
      } else if (field.isConstant()) {
        serializedField.jsonType = CONSTANT_FIELD_JSON_TYPE;
        serializedField.name = field.name;
      } else if (field.enumeration) {
        if (field.docDef.type === DocumentType.JSON) {
          serializedField.jsonType = JSON_ENUM_FIELD_JSON_TYPE;
        } else if (
          field.docDef.type === DocumentType.XML ||
          field.docDef.type === DocumentType.XSD
        ) {
          serializedField.jsonType = XML_ENUM_FIELD_JSON_TYPE;
        } else {
          serializedField.jsonType = JAVA_ENUM_FIELD_JSON_TYPE;
        }
      }

      let includeIndexes: boolean =
        mapping.getMappedFields(isSource).length > 1;
      includeIndexes =
        includeIndexes || (mapping.transition.isExpressionMode() && isSource);
      if (includeIndexes) {
        // TODO: check this non null operator
        serializedField.index =
          mapping.getIndexForMappedField(mappedField)! - 1;
      }
      if (field.docDef.type === DocumentType.CSV) {
        (serializedField as ICsvField).column = field.column;
      }

      this.serializeActions(cfg, mappedField, serializedField);

      // Check for collection field references.
      if (isSource && field.isInCollection()) {
        const collectionParentField = field.getCollectionParentField();

        if (
          !mapping.referenceFieldExists(
            collectionParentField.docDef.id,
            collectionParentField.path
          )
        ) {
          if (!ignoreValue) {
            MappingSerializer.processCollectionPreview(
              field,
              serializedField,
              fieldsJson
            );
          } else {
            fieldsJson.push(serializedField);
            collectionInputFieldGroup = null;
          }
        } else {
          // Establish/add to the inner reference field group.
          if (collectionInstanceInputFieldGroup === null) {
            collectionInstanceInputFieldGroup =
              MappingSerializer.createInputFieldGroup(
                [serializedField],
                true,
                collectionParentField.docDef.id,
                collectionParentField.path
              );
          } else {
            (collectionInstanceInputFieldGroup as IFieldGroup)!.field!.push(
              serializedField
            );
            continue;
          }
          collectionInstanceInputFieldGroup.fieldType =
            collectionParentField.documentField.fieldType;

          // Preview-mode uses element/ item instance <0>.
          if (!ignoreValue) {
            collectionInstanceInputFieldGroup.path =
              collectionParentField.path.replace('<>', '<0>');

            // Establish one outer input field group for the preview collection.
            if (collectionInputFieldGroup === null) {
              collectionInputFieldGroup =
                MappingSerializer.createInputFieldGroup(
                  [collectionInstanceInputFieldGroup],
                  true,
                  collectionParentField.docDef.id,
                  collectionParentField.path
                );
              collectionInstanceInputFieldGroup.fieldType =
                collectionParentField.documentField.fieldType;
              fieldsJson.push(collectionInputFieldGroup);
            }
          } else {
            fieldsJson.push(collectionInstanceInputFieldGroup);
          }
        }

        // Non-aggregate field reference.
      } else {
        fieldsJson.push(serializedField);
        collectionInputFieldGroup = null;
      }
    }
    return fieldsJson;
  }

  /**
   * Walk the list of actions associated with the specified mapped field and serialize them into JSON.
   *
   * @param cfg
   * @param mappedField
   * @param serializedField
   */
  private static serializeActions(
    cfg: ConfigModel,
    mappedField: MappedField,
    serializedField: IField
  ): void {
    if (mappedField.actions.length) {
      const actions: IFieldAction[] = [];

      for (const action of mappedField.actions) {
        const actionJson = this.serializeAction(action, cfg);
        if (actionJson) {
          actions.push(actionJson);
        }
      }
      if (actions.length > 0) {
        serializedField.actions = actions;
      }
    }
  }

  private static serializeAction(
    action: FieldAction,
    cfg: ConfigModel
  ): { [key: string]: any } {
    let actionJson = MappingSerializer.processActionArguments(action, cfg);
    actionJson['@type'] = action.definition!.name;
    return actionJson;
  }

  private static deserializeDocs(
    json: IAtlasMappingContainer,
    mappingDefinition: MappingDefinition
  ): DocumentDefinition[] | null {
    const docs: DocumentDefinition[] = [];
    if (!json || !json.AtlasMapping || !json.AtlasMapping.dataSource) {
      return null;
    }
    for (const docRef of json.AtlasMapping.dataSource) {
      const doc: DocumentDefinition = new DocumentDefinition();
      doc.isSource = docRef.dataSourceType === 'SOURCE';
      doc.uri = docRef.uri;
      doc.id = docRef.id;
      doc.name = docRef.name ? docRef.name : '';
      doc.description = docRef.description;
      const xmlDocRef = docRef as IXmlDataSource;
      if (xmlDocRef.xmlNamespaces && xmlDocRef.xmlNamespaces.xmlNamespace) {
        for (const svcNS of xmlDocRef.xmlNamespaces.xmlNamespace) {
          const ns: NamespaceModel = new NamespaceModel();
          ns.alias = svcNS.alias;
          ns.uri = svcNS.uri;
          ns.locationUri = svcNS.locationUri;
          ns.isTarget = svcNS.targetNamespace ? svcNS.targetNamespace : false;
          ns.createdByUser = true;
          doc.namespaces.push(ns);
        }
      }
      if (xmlDocRef.template) {
        mappingDefinition.templateText = xmlDocRef.template;
      }
      docs.push(doc);
    }
    return docs;
  }

  private static deserializeMappings(
    json: IAtlasMappingContainer,
    cfg: ConfigModel
  ): MappingModel[] {
    const mappings: MappingModel[] = [];

    if (!json.AtlasMapping.mappings?.mapping) {
      return mappings;
    }
    for (const fieldMapping of json.AtlasMapping.mappings.mapping) {
      // for backward compatibility
      const isCollectionMapping =
        fieldMapping.jsonType === COLLECTION_JSON_TYPE;
      if (isCollectionMapping) {
        const collection = fieldMapping as ICollection;
        for (const innerFieldMapping of collection.mappings.mapping) {
          mappings.push(
            MappingSerializer.deserializeFieldMapping(
              innerFieldMapping as IMapping,
              cfg
            )
          );
        }
      } else {
        mappings.push(
          MappingSerializer.deserializeFieldMapping(
            fieldMapping as IMapping,
            cfg
          )
        );
      }
    }
    return mappings;
  }

  /**
   * @deprecated Deserialize a field mapping based on its mapping type.  This is deprecated in favor of
   * concatenate/ split actions.
   *
   * @param mapping
   * @param fieldMapping
   * @param docRefs
   * @param cfg
   * @param ignoreValue
   */
  private static deserializeFieldMappingFromType(
    mapping: MappingModel,
    fieldMapping: IMapping,
    cfg: ConfigModel
  ): void {
    if (fieldMapping.mappingType === 'SEPARATE') {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      mapping.transition.transitionFieldAction = FieldAction.create(
        cfg.fieldActionService.getActionDefinitionForName(
          'Split',
          Multiplicity.ONE_TO_MANY
        )!
      ); // TODO: check this non null operator
      mapping.transition.transitionFieldAction.setArgumentValue(
        'delimiter',
        fieldMapping.delimiter!
      );
    } else if (fieldMapping.mappingType === 'LOOKUP') {
      mapping.transition.mode = TransitionMode.ENUM;
      mapping.transition.lookupTableName = fieldMapping.lookupTableName!;
    } else if (fieldMapping.mappingType === 'COMBINE') {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      mapping.transition.transitionFieldAction = FieldAction.create(
        cfg.fieldActionService.getActionDefinitionForName(
          'Concatenate',
          Multiplicity.MANY_TO_ONE
        )!
      ); // TODO: check this non null operator
      mapping.transition.transitionFieldAction.setArgumentValue(
        'delimiter',
        fieldMapping.delimiter!
      );
    } else {
      mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    }

    for (const field of fieldMapping.inputField!) {
      MappingSerializer.deserializeMappedField(mapping, field, true, cfg);
    }
    for (const field of fieldMapping.outputField) {
      MappingSerializer.deserializeMappedField(mapping, field, false, cfg);
    }
    MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, true);
  }

  private static deserializeConstants(
    jsonMapping: IAtlasMappingContainer
  ): Field[] {
    const fields: Field[] = [];
    if (!jsonMapping?.AtlasMapping?.constants?.constant) {
      return fields;
    }
    for (const constant of jsonMapping.AtlasMapping.constants.constant) {
      const field: Field = new Field();
      field.name = constant.name;
      field.path = FIELD_PATH_SEPARATOR + field.name;
      field.value = constant.value;
      field.type = constant.fieldType;
      field.userCreated = true;
      field.isAttribute = false;
      fields.push(field);
    }
    return fields;
  }

  private static deserializeProperties(
    cfg: ConfigModel,
    jsonMapping: IAtlasMappingContainer,
    isSource: boolean
  ): Field[] {
    const fields: Field[] = [];
    if (!jsonMapping?.AtlasMapping?.properties?.property) {
      return fields;
    }

    // Source and target properties are mixed in the 'property' JSON array.
    for (const property of jsonMapping.AtlasMapping.properties.property) {
      if (
        (isSource && property.dataSourceType === DataSourceType.TARGET) ||
        (!isSource && property.dataSourceType !== DataSourceType.TARGET)
      ) {
        continue;
      }
      const field: Field = new Field();
      field.name = property.name;
      field.type = property.fieldType;
      field.scope = property.scope;
      field.path = cfg.documentService.getPropertyPath(field.scope, field.name);
      field.userCreated = true;
      field.isAttribute = false;
      fields.push(field);
    }
    return fields;
  }

  private static deserializeLookupTables(
    jsonMapping: IAtlasMappingContainer
  ): LookupTable[] {
    const tables: LookupTable[] = [];
    if (
      !jsonMapping ||
      !jsonMapping.AtlasMapping ||
      !jsonMapping.AtlasMapping.lookupTables ||
      !jsonMapping.AtlasMapping.lookupTables.lookupTable
    ) {
      return tables;
    }
    for (const table of jsonMapping.AtlasMapping.lookupTables.lookupTable) {
      const parsedTable: LookupTable = new LookupTable();
      parsedTable.name = table.name;
      for (const entry of table.lookupEntry) {
        const parsedEntry: LookupTableEntry = new LookupTableEntry();
        parsedEntry.sourceValue = entry.sourceValue;
        parsedEntry.sourceType = entry.sourceType;
        parsedEntry.targetValue = entry.targetValue;
        parsedEntry.targetType = entry.targetType;
        parsedTable.lookupEntry.push(parsedEntry);
      }
      tables.push(parsedTable);
    }
    return tables;
  }

  /**
   * Walk the list of field actions found in the parsed data and restore them to the live mapping.
   *
   * @param field
   * @param mappedField
   * @param mapping
   * @param cfg
   * @param isSource
   */
  private static deserializeFieldActions(
    field: IField,
    mappedField: MappedField,
    mapping: MappingModel,
    cfg: ConfigModel,
    isSource: boolean
  ): void {
    if (!field.actions) {
      return;
    }

    for (const action of field.actions) {
      const parsedAction = this.parseAction(action);
      if (action.name === 'CustomAction') {
        parsedAction.definition =
          cfg.fieldActionService.getActionDefinitionForName(
            action.argumentValues[0].value,
            Multiplicity.ONE_TO_ONE
          );
      } else {
        parsedAction.definition =
          cfg.fieldActionService.getActionDefinitionForName(parsedAction.name)!;
      }
      if (parsedAction.definition == null) {
        cfg.errorService.addError(
          new ErrorInfo({
            message: `Could not find field action definition for action '${action.name}'`,
            level: ErrorLevel.ERROR,
            scope: ErrorScope.APPLICATION,
            type: ErrorType.INTERNAL,
          })
        );
        continue;
      }
      parsedAction.definition.populateFieldAction(parsedAction);

      /** @deprecated Support old-style transformation-action-based expressions. */
      if (isSource && (action.Expression || action['@type'] === 'Expression')) {
        mapping.transition.enableExpression = true;
        mapping.transition.expression = new ExpressionModel(mapping, cfg);
        const expr = action.Expression
          ? action.Expression.expression
          : action['expression'];
        mapping.transition.expression.insertText(expr);
      } else if (
        isSource &&
        parsedAction.definition &&
        [Multiplicity.ONE_TO_MANY, Multiplicity.MANY_TO_ONE].includes(
          parsedAction.definition.multiplicity
        )
      ) {
        if (mapping.transition.transitionFieldAction) {
          cfg.logger!
            .warn(`Duplicated multiplicity transformations were detected: \
              ${mapping.transition.transitionFieldAction.name} is being overwritten by ${parsedAction.name} ...`);
        }
        mapping.transition.transitionFieldAction = parsedAction;
      } else {
        mappedField.actions.push(parsedAction);
      }
    }
  }

  private static deserializeMappedField(
    mapping: MappingModel,
    field: IField,
    isSource: boolean,
    cfg: ConfigModel
  ): MappedField | null {
    if (MappingUtil.isConstantField(field)) {
      if (field.docId) {
        cfg.constantDoc.id = field.docId;
      } else {
        field.docId = cfg.constantDoc.id;
      }
    } else if (MappingUtil.isPropertyField(field)) {
      const doc = isSource ? cfg.sourcePropertyDoc : cfg.targetPropertyDoc;
      if (field.docId) {
        doc.id = field.docId;
      } else {
        field.docId = doc.id;
      }
      field.path = cfg.documentService.getPropertyPath(
        (field as IPropertyField).scope,
        field.name!
      );
    } else if (!field.docId) {
      cfg.errorService.addError(
        new ErrorInfo({
          message: 'Parsed mapping field does not have document id, dropping.',
          level: ErrorLevel.ERROR,
          scope: ErrorScope.APPLICATION,
          type: ErrorType.INTERNAL,
          object: field,
        })
      );
      return null;
    }
    const mappedField: MappedField = new MappedField();
    mappedField.mappingField = field;
    mapping.addMappedField(mappedField, isSource);
    if (field.actions) {
      this.deserializeFieldActions(field, mappedField, mapping, cfg, isSource);
    }
    return mappedField;
  }

  private static parseAction(action: IFieldAction): FieldAction {
    if (action['@type']) {
      return MappingSerializer.parseNewAction(action);
    } else {
      // TODO: check this non null operator
      return MappingSerializer.parseOldAction(action)!;
    }
  }

  /**
   * @deprecated actionName: {param:...} style has been deprecated. Use {`@type`: actionName} style action description.
   */
  private static parseOldAction(action: IFieldAction): FieldAction | null {
    for (const actionName of Object.keys(action)) {
      if (!action.hasOwnProperty(actionName)) {
        return null;
      }
      const parsedAction: FieldAction = new FieldAction();
      parsedAction.name = actionName;
      const actionParams: any = action[actionName];
      if (actionParams) {
        for (const paramName of Object.keys(actionParams)) {
          if (!actionParams.hasOwnProperty(paramName)) {
            return null;
          }
          const parsedArgumentValue: FieldActionArgumentValue =
            new FieldActionArgumentValue();
          parsedArgumentValue.name = paramName;
          let value = actionParams[paramName];
          value = value == null ? null : value.toString();
          parsedArgumentValue.value = value;
          parsedAction.argumentValues.push(parsedArgumentValue);
        }
      }
      return parsedAction;
    }
    return null;
  }

  private static parseNewAction(action: IFieldAction): FieldAction {
    const parsedAction: FieldAction = new FieldAction();
    parsedAction.name = action['@type']!;
    for (const [key, value] of Object.entries(action)) {
      if ('@type' === key) {
        continue;
      }
      const parsedArgumentValue: FieldActionArgumentValue =
        new FieldActionArgumentValue();
      parsedArgumentValue.name = key;
      const valueString = value == null ? null : (value as any).toString();
      parsedArgumentValue.value = valueString;
      parsedAction.argumentValues.push(parsedArgumentValue);
    }
    return parsedAction;
  }
}
