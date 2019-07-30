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
  TransitionMode, FieldActionArgument,
  FieldAction, FieldActionArgumentValue
} from '../models/transition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { DocumentDefinition, NamespaceModel } from '../models/document-definition.model';
import { LookupTable, LookupTableEntry } from '../models/lookup-table.model';
import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { ErrorInfo, ErrorLevel } from '../models/error.model';
import { TransitionModel, TransitionDelimiter } from '../models/transition.model';
import { ExpressionModel } from '../models/expression.model';

export class MappingSerializer {

  static serializeMappings(cfg: ConfigModel): any {
    const mappingDefinition: MappingDefinition = cfg.mappings;
    let jsonMappings: any[] = [];
    for (const mapping of mappingDefinition.mappings) {
      try {
        let fieldMappingsForThisMapping: any[] = [];
        let jsonMapping: any;

        for (const fieldMappingPair of mapping.fieldMappings) {
          fieldMappingsForThisMapping.push(MappingSerializer.serializeFieldMapping(cfg, fieldMappingPair, mapping.uuid));
        }

        if (mapping.isCollectionMode()) {
          let collectionType: string = null;
          for (const field of mapping.getAllFields()) {
            collectionType = field.getCollectionType();
            if (collectionType != null) {
              break;
            }
          }
          jsonMapping = {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Collection',
            'mappingType': 'COLLECTION',  /* @deprecated */
            'collectionType': collectionType,
            'mappings': { 'mapping': fieldMappingsForThisMapping },
          };
          fieldMappingsForThisMapping = [jsonMapping];
        }

        jsonMappings = jsonMappings.concat(fieldMappingsForThisMapping);
      } catch (e) {
        const input: any = { 'mapping': mapping, 'mapping def': mappingDefinition };
        cfg.errorService.error('Caught exception while attempting to serialize mapping, skipping. ', { 'input': input, 'error': e });
      }
    }

    const serializedLookupTables: any[] = MappingSerializer.serializeLookupTables(cfg.mappings);
    const constantDescriptions: any[] = MappingSerializer.serializeConstants(cfg.constantDoc);
    const propertyDescriptions: any[] = MappingSerializer.serializeProperties(cfg.propertyDoc);
    const serializedDataSources: any = MappingSerializer.serializeDocuments(cfg.sourceDocs.concat(cfg.targetDocs), mappingDefinition);

    const payload: any = {
      'AtlasMapping': {
        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.AtlasMapping',
        'dataSource': serializedDataSources,
        'mappings': { 'mapping': jsonMappings },
        'name': cfg.mappings.name,
        'lookupTables': { 'lookupTable': serializedLookupTables },
        'constants': { 'constant': constantDescriptions },
        'properties': { 'property': propertyDescriptions },
      },
    };
    return payload;
  }

  private static createInputFieldGroup(fieldMappingPair: FieldMappingPair, field: any[]): any {
    const actions = [];

    if (fieldMappingPair.transition.isCombineMode()) {
      if (fieldMappingPair.transition.enableExpression) {
        actions[0] = {
          'Expression' : {
            'expression' : fieldMappingPair.transition.expression.toText()
          }
        };
      } else {
        let delimiter = fieldMappingPair.transition.getActualDelimiter();

        if (fieldMappingPair.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          delimiter = fieldMappingPair.transition.userDelimiter;
        }
        actions[0] = {
          'Concatenate' : {
            'delimiter' : delimiter
          }
        };
      }
    }
    const inputFieldGroup: any = {
        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.FieldGroup',
        actions,
        field
    };
    return inputFieldGroup;
  }

  static serializeFieldMapping(
    cfg: ConfigModel, fieldMappingPair: FieldMappingPair,
    id: string, ignoreValue: boolean = true): any {
    let inputFieldGroup = {};
    const outputFields = {};
    const jsonMappingType = ConfigModel.mappingServicesPackagePrefix + '.Mapping';
    const serializedInputFields: any[] = MappingSerializer.serializeFields(fieldMappingPair, true, cfg, ignoreValue);
    const serializedOutputFields: any[] = MappingSerializer.serializeFields(fieldMappingPair, false, cfg, ignoreValue);
    let jsonMapping = {};

    if (fieldMappingPair.transition.isCombineMode()) {
      inputFieldGroup = MappingSerializer.createInputFieldGroup(fieldMappingPair, serializedInputFields);

      jsonMapping = {
       'jsonType': jsonMappingType,
       'id': id,
       inputFieldGroup,
       'outputField': serializedOutputFields,
      };
    } else if (fieldMappingPair.transition.isSeparateMode()) {
        jsonMapping = {
         'jsonType': jsonMappingType,
         'id': id,
         'inputField' : serializedInputFields,
         'outputField': serializedOutputFields,
        };
    }  else {
      jsonMapping = {
       'jsonType': jsonMappingType,
       'id': id,
       'inputField' : serializedInputFields,
       'outputField': serializedOutputFields,
     };
    }

    if (fieldMappingPair.transition.isEnumerationMode()) {
      jsonMapping['mappingType'] = 'LOOKUP';  /* @deprecated */
      jsonMapping['lookupTableName'] = fieldMappingPair.transition.lookupTableName;
    }
    return jsonMapping;
  }

  private static serializeDocuments(docs: DocumentDefinition[], mappingDefinition: MappingDefinition): any[] {
    const serializedDocs: any[] = [];
    for (const doc of docs) {
      const docType: string = doc.isSource ? 'SOURCE' : 'TARGET';
      const serializedDoc: any = {
        'jsonType': 'io.atlasmap.v2.DataSource',
        'id': doc.id,
        'uri': doc.uri,
        'dataSourceType': docType,
      };
      if (doc.characterEncoding != null) {
        serializedDoc['characterEncoding'] = doc.characterEncoding;
      }
      if (doc.locale != null) {
        serializedDoc['locale'] = doc.locale;
      }
      if (doc.type === DocumentType.XML) {
        serializedDoc['jsonType'] = 'io.atlasmap.xml.v2.XmlDataSource';
        const namespaces: any[] = [];
        for (const ns of doc.namespaces) {
          namespaces.push({
            'alias': ns.alias,
            'uri': ns.uri,
            'locationUri': ns.locationUri,
            'targetNamespace': ns.isTarget,
          });
        }
        if (!doc.isSource) {
          serializedDoc['template'] = mappingDefinition.templateText;
        }
        serializedDoc['xmlNamespaces'] = { 'xmlNamespace': namespaces };
      } else if (doc.type === DocumentType.JSON) {
        if (!doc.isSource) {
          serializedDoc['template'] = mappingDefinition.templateText;
        }
        serializedDoc['jsonType'] = 'io.atlasmap.json.v2.JsonDataSource';
      }

      serializedDocs.push(serializedDoc);
    }
    return serializedDocs;
  }

  private static serializeConstants(docDef: DocumentDefinition): any[] {
    const constantDescriptions: any[] = [];
    for (const field of docDef.fields) {
      // Use the constant value for the name.
      constantDescriptions.push({
        'name': field.value, 'value': field.value, 'fieldType': field.type
      });
    }
    return constantDescriptions;
  }

  private static serializeProperties(docDef: DocumentDefinition): any[] {
    const propertyDescriptions: any[] = [];
    for (const field of docDef.fields) {
      propertyDescriptions.push({
        'name': field.name,
        'value': field.value, 'fieldType': field.type
      });
    }
    return propertyDescriptions;
  }

  private static serializeLookupTables(mappingDefinition: MappingDefinition): any[] {
    const tables: LookupTable[] = mappingDefinition.getTables();

    if (!tables || !tables.length) {
      return [];
    }

    const serializedTables: any[] = [];
    for (const table of tables) {
      const lookupEntries: any[] = [];
      for (const entry of table.entries) {
        const serializedEntry: any = {
          'sourceValue': entry.sourceValue,
          'sourceType': entry.sourceType,
          'targetValue': entry.targetValue,
          'targetType': entry.targetType,
        };
        lookupEntries.push(serializedEntry);
      }

      const serializedTable: any = {
        'lookupEntry': lookupEntries,
        'name': table.name,
      };
      serializedTables.push(serializedTable);
    }
    return serializedTables;
  }

  /**
   * Serialize field action arguments.
   *
   * @param action
   * @param cfg
   */
  private static processActionArguments(action: any, cfg: ConfigModel): any {
    let actionArguments: any = {};
    for (const argValue of action.argumentValues) {
      actionArguments[argValue.name] = argValue.value;
      const argumentConfig: FieldActionArgument = action.config.getArgumentForName(argValue.name);
      if (argumentConfig == null) {
        cfg.errorService.error('Cannot find action argument with name: ' + argValue.name, action);
         continue;
      }
      if (argumentConfig.type === 'INTEGER') {
        actionArguments[argValue.name] = parseInt(argValue.value, 10);
      }
    }
    actionArguments = (Object.keys(actionArguments).length === 0) ? null : actionArguments;
    return actionArguments;
  }

  private static serializeFields(
    fieldPair: FieldMappingPair, isSource: boolean,
    cfg: ConfigModel, ignoreValue: boolean = false): any[] {
    const fields: MappedField[] = fieldPair.getMappedFields(isSource);
    const fieldsJson: any[] = [];
    for (const mappedField of fields) {
      const field: Field = mappedField.field;
      if (DocumentDefinition.getNoneField().path === field.path) {
        // do not include "none" options from drop downs in mapping
        continue;
      }

      const serializedField: any = {
        'jsonType': field.serviceObject.jsonType,
        'name': field.name,
        'path': field.path,
        'fieldType': field.type,
        'docId': field.docDef.id,
      };

      if (isSource && fields.length === 1 && fieldPair.transition.enableExpression) {
        serializedField['actions'] = [ {
          'Expression' : {
            'expression' : fieldPair.transition.expression.toText()
          }
        } ];
      }

      if (fieldPair.transition.isSeparateMode() && field.isSource()) {
        let delimiter = fieldPair.transition.getActualDelimiter();

        if (fieldPair.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          delimiter = fieldPair.transition.userDelimiter;
        }
        serializedField['actions'] = [ {
          'Split' : {
            'delimiter' : delimiter
          }
        } ];
      }

      if (!ignoreValue || field.isPropertyOrConstant()) {
        serializedField['value'] = field.value;
      }
      if (field.docDef.type === DocumentType.XML || field.docDef.type === DocumentType.JSON) {
        serializedField['userCreated'] = field.userCreated;
      } else if (field.docDef.type === DocumentType.JAVA && !field.isPrimitive) {
        serializedField['className'] = field.classIdentifier;
      }
      if (field.isProperty()) {
        serializedField['jsonType'] = ConfigModel.mappingServicesPackagePrefix + '.PropertyField';
        serializedField['name'] = field.path ? field.path : field.name;
      } else if (field.isConstant()) {
        serializedField['jsonType'] = ConfigModel.mappingServicesPackagePrefix + '.ConstantField';
        delete (serializedField['name']);
      } else if (field.enumeration) {
        serializedField['jsonType'] = 'io.atlasmap.java.v2.JavaEnumField';
      }

      let includeIndexes: boolean = fieldPair.transition.isSeparateMode() && !isSource;
      includeIndexes = includeIndexes || (fieldPair.transition.isCombineMode() && isSource);
      if (includeIndexes) {
        let index: string = mappedField.getSeparateOrCombineIndex();
        index = (index == null) ? '1' : index;
        serializedField['index'] = (parseInt(index, 10) - 1);
      }

      if (mappedField.actions.length) {
        const actions: any[] = [];
        let updatedActions = 0;

        for (const action of mappedField.actions) {
          if (action.isSeparateOrCombineMode || action.config.serviceObject.name === 'Split') {
            continue;
          }
          updatedActions++;

          // Serialize custom field actions.
          if (action.config.isCustom) {
              const customActionJson: any = {};
              let customActionBody = {};
              customActionBody['name'] = action.config.serviceObject.name;
              customActionBody['className'] = action.config.serviceObject.className;
              customActionBody['methodName'] = action.config.serviceObject.method;
              customActionBody = (Object.keys(customActionBody).length === 0) ? null : customActionBody;
              customActionJson['CustomAction'] = customActionBody;
              actions.push(customActionJson);
              continue;
          }

          let actionArguments: any = {};
          actionArguments = MappingSerializer.processActionArguments(action, cfg);
          const actionJson: any = {};
          actionJson[action.config.name] = actionArguments;
          actions.push(actionJson);
        }
        if (updatedActions > 0) {
          serializedField['actions'] = actions;
        }
      }

      fieldsJson.push(serializedField);
    }

    return fieldsJson;
  }

  static deserializeMappingServiceJSON(json: any, mappingDefinition: MappingDefinition, cfg: ConfigModel): void {

    // ref https://github.com/atlasmap/atlasmap/issues/1142
    if (!cfg.mappings) {
      cfg.mappings = mappingDefinition ? mappingDefinition : new MappingDefinition;
    }
    if (json && json.AtlasMapping && json.AtlasMapping.name) {
      mappingDefinition.name = json.AtlasMapping.name;
    }
    mappingDefinition.parsedDocs = mappingDefinition.parsedDocs.concat(MappingSerializer.deserializeDocs(json, mappingDefinition));
    mappingDefinition.mappings = mappingDefinition.mappings.concat(MappingSerializer.deserializeMappings(json, cfg));
    for (const lookupTable of MappingSerializer.deserializeLookupTables(json)) {
      mappingDefinition.addTable(lookupTable);
    }
    for (const field of MappingSerializer.deserializeConstants(json)) {
      cfg.constantDoc.addField(field);
    }
    for (const field of MappingSerializer.deserializeProperties(json)) {
      cfg.propertyDoc.addField(field);
    }
  }

  static deserializeDocs(json: any, mappingDefinition: MappingDefinition): DocumentDefinition[] {
    const docs: DocumentDefinition[] = [];
    for (const docRef of json.AtlasMapping.dataSource) {
      const doc: DocumentDefinition = new DocumentDefinition();
      doc.isSource = (docRef.dataSourceType === 'SOURCE');
      doc.uri = docRef.uri;
      doc.id = docRef.id;
      if (docRef.xmlNamespaces && docRef.xmlNamespaces.xmlNamespace) {
        for (const svcNS of docRef.xmlNamespaces.xmlNamespace) {
          const ns: NamespaceModel = new NamespaceModel();
          ns.alias = svcNS.alias;
          ns.uri = svcNS.uri;
          ns.locationUri = svcNS.locationUri;
          ns.isTarget = svcNS.targetNamespace;
          ns.createdByUser = true;
          doc.namespaces.push(ns);
        }
      }
      if (docRef.template) {
        mappingDefinition.templateText = docRef.template;
      }
      docs.push(doc);
    }
    return docs;
  }

  static deserializeMappings(json: any, cfg: ConfigModel): MappingModel[] {
    const mappings: MappingModel[] = [];
    const docRefs: any = {};
    for (const docRef of json.AtlasMapping.dataSource) {
      docRefs[docRef.id] = docRef.uri;
    }
    for (const fieldMapping of json.AtlasMapping.mappings.mapping) {
      const mappingModel: MappingModel = new MappingModel();
      if (fieldMapping.id) {
        mappingModel.uuid = fieldMapping.id;
      }
      mappingModel.fieldMappings = [];

      const isCollectionMapping = (fieldMapping.jsonType === ConfigModel.mappingServicesPackagePrefix + '.Collection');
      if (isCollectionMapping) {
        for (const innerFieldMapping of fieldMapping.mappings.mapping) {
          mappingModel.fieldMappings.push(MappingSerializer.deserializeFieldMapping(innerFieldMapping, docRefs, cfg));
        }
      } else {
        mappingModel.fieldMappings.push(MappingSerializer.deserializeFieldMapping(fieldMapping, docRefs, cfg));
      }

      mappings.push(mappingModel);
    }
    return mappings;
  }

  /**
   * @deprecated Deserialize a field mapping based on its mapping type.  This is deprecated in favor of
   * concatenate/ split actions.
   *
   * @param fieldPair
   * @param fieldMapping
   * @param docRefs
   * @param cfg
   * @param ignoreValue
   */
  static deserializeFieldMappingFromType(fieldPair: FieldMappingPair,
          fieldMapping: any, docRefs: any, cfg: ConfigModel, ignoreValue: boolean): void {
    const isSeparateMapping = (fieldMapping.mappingType === 'SEPARATE');
    const isLookupMapping = (fieldMapping.mappingType === 'LOOKUP');
    const isCombineMapping = (fieldMapping.mappingType === 'COMBINE');

    for (const field of fieldMapping.inputField) {
       MappingSerializer.addFieldIfDoesntExist(fieldPair, field, true, docRefs, cfg, ignoreValue);
    }
    for (const field of fieldMapping.outputField) {
       MappingSerializer.addFieldIfDoesntExist(fieldPair, field, false, docRefs, cfg, ignoreValue);
    }
    if (isSeparateMapping) {
      fieldPair.transition.mode = TransitionMode.SEPARATE;
      fieldPair.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.delimiter);
    } else if (isCombineMapping) {
      fieldPair.transition.mode = TransitionMode.COMBINE;
      fieldPair.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.delimiter);
    } else if (isLookupMapping) {
      fieldPair.transition.lookupTableName = fieldMapping.lookupTableName;
      fieldPair.transition.mode = TransitionMode.ENUM;
    } else {
      fieldPair.transition.mode = TransitionMode.MAP;
    }
  }

  static deserializeFieldMapping(
    fieldMapping: any, docRefs: any, cfg: ConfigModel, ignoreValue: boolean = true): FieldMappingPair {
    const fieldPair: FieldMappingPair = new FieldMappingPair();
    fieldPair.sourceFields = [];
    fieldPair.targetFields = [];
    fieldPair.transition.mode = TransitionMode.MAP;
    const isLookupMapping = (fieldMapping.mappingType === 'LOOKUP');

    if (fieldMapping.mappingType && fieldMapping.mappingType !== '') {
      this.deserializeFieldMappingFromType(fieldPair, fieldMapping, docRefs, cfg, ignoreValue);
      return fieldPair;
    }

    let inputField = [];

    if (fieldMapping.inputFieldGroup) {
      inputField = fieldMapping.inputFieldGroup.field;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(fieldPair, field, true, docRefs, cfg, ignoreValue);
      }
      fieldPair.transition.mode = TransitionMode.COMBINE;
      cfg.mappings.updateMappedFieldsFromDocuments(fieldPair, cfg, null, true);

      // Check for an InputFieldGroup containing a concatenate action inferring combine mode.
      const firstAction = fieldMapping.inputFieldGroup.actions[0];
      if (firstAction) {
        if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          fieldPair.transition.enableExpression = true;
          fieldPair.transition.expression = new ExpressionModel(fieldPair);
          const expr = firstAction.Expression ? firstAction.Expression.expression : firstAction['expression'];
          fieldPair.transition.expression.insertText(expr);
        } else if (firstAction.Concatenate || firstAction['@type'] === 'Concatenate') {
          const concatDelimiter =
            firstAction.Concatenamte ? firstAction.Concatenate.delimiter : firstAction['delimiter'];
          if (concatDelimiter) {
            fieldPair.transition.mode = TransitionMode.COMBINE;
            fieldPair.transition.delimiter =
              TransitionModel.getTransitionDelimiterFromActual(concatDelimiter);

            if (fieldPair.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
              fieldPair.transition.userDelimiter = concatDelimiter;
            }
          }
        }
      }
    } else {
      inputField = fieldMapping.inputField;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(fieldPair, field, true, docRefs, cfg, ignoreValue);
      }

      if (inputField[0].actions && inputField[0].actions[0]) {
        // Check for an InputField containing a split action inferring separate mode.
        const firstAction = inputField[0].actions[0];
        if (firstAction.Split || firstAction['@type'] === 'Split') {
          const splitDelimiter = firstAction.Split ? firstAction.Split.delimiter : firstAction['delimiter'];
          if (splitDelimiter) {
            fieldPair.transition.mode = TransitionMode.SEPARATE;
            fieldPair.transition.delimiter =
              TransitionModel.getTransitionDelimiterFromActual(splitDelimiter);
            if (fieldPair.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
              fieldPair.transition.userDelimiter = splitDelimiter;
            }
          }
        } else if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          fieldPair.transition.enableExpression = true;
          fieldPair.transition.expression = new ExpressionModel(fieldPair);
          const expr = firstAction.Expression ? firstAction.Expression.expression : firstAction['expression'];
          fieldPair.transition.expression.insertText(expr);
        }
      }
    }

    for (const field of fieldMapping.outputField) {
      MappingSerializer.addFieldIfDoesntExist(fieldPair, field, false, docRefs, cfg, ignoreValue);
    }

    if (isLookupMapping) {
      fieldPair.transition.lookupTableName = fieldMapping.lookupTableName;
      fieldPair.transition.mode = TransitionMode.ENUM;
    }

    return fieldPair;
  }

  static deserializeConstants(jsonMapping: any): Field[] {
    const fields: Field[] = [];
    if (!jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.constants
      || !jsonMapping.AtlasMapping.constants.constant) {
      return fields;
    }
    for (const constant of jsonMapping.AtlasMapping.constants.constant) {
      const field: Field = new Field();
      field.name = constant.name;
      field.value = constant.value;
      field.type = constant.fieldType;
      field.userCreated = true;
      fields.push(field);
    }
    return fields;
  }

  static deserializeProperties(jsonMapping: any): Field[] {
    const fields: Field[] = [];
    if (!jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.properties
      || !jsonMapping.AtlasMapping.properties.property) {
      return fields;
    }
    for (const property of jsonMapping.AtlasMapping.properties.property) {
      const field: Field = new Field();
      field.name = property.name;
      field.value = property.value;
      field.type = property.fieldType;
      field.userCreated = true;
      fields.push(field);
    }
    return fields;
  }

  static deserializeLookupTables(jsonMapping: any): LookupTable[] {
    const tables: LookupTable[] = [];
    if (!jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.lookupTables
      || !jsonMapping.AtlasMapping.lookupTables.lookupTable) {
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
        parsedTable.entries.push(parsedEntry);
      }
      tables.push(parsedTable);
    }
    return tables;
  }

  static deserializeAudits(audits: any): ErrorInfo[] {
    const errors: ErrorInfo[] = [];
    if (!audits) {
      return errors;
    }
    for (const audit of audits.audit) {
      let errorLevel: ErrorLevel;
      if (audit.status === 'ERROR') {
        errorLevel = ErrorLevel.ERROR;
      } else if (audit.status === 'WARN') {
        errorLevel = ErrorLevel.WARN;
      }
      if (errorLevel) {
        const msg = audit.status + '[' + audit.path + ']: ' + audit.message;
        errors.push(new ErrorInfo(msg, errorLevel, audit.value));
      }
    }
    return errors;
  }

  private static addFieldIfDoesntExist(
    fieldPair: FieldMappingPair, field: any, isSource: boolean,
    docRefs: any, cfg: ConfigModel, ignoreValue: boolean = true): MappedField {
    const mappedField: MappedField = new MappedField();

    mappedField.parsedData.parsedValueType = field.fieldType;
    mappedField.parsedData.parsedIndex = '1';
    if (field.index != null) {
      mappedField.parsedData.parsedIndex = (field.index + 1).toString();
    }
    if (field.jsonType === (ConfigModel.mappingServicesPackagePrefix + '.PropertyField')) {
      mappedField.parsedData.parsedName = field.name;
      mappedField.parsedData.parsedPath = field.name;
      mappedField.parsedData.fieldIsProperty = true;
    } else if (field.jsonType === (ConfigModel.mappingServicesPackagePrefix + '.ConstantField')) {
      mappedField.parsedData.fieldIsConstant = true;
      mappedField.parsedData.parsedValue = field.value;
      mappedField.parsedData.parsedPath = field.path;
    } else {
      if (field.docId == null) {
        cfg.errorService.error('Parsed mapping field does not have document id, dropping.', field);
        return null;
      }
      if (!ignoreValue) {
        mappedField.parsedData.parsedValue = field.value;
      }
      mappedField.parsedData.parsedName = field.name;
      mappedField.parsedData.parsedPath = field.path;
      mappedField.parsedData.parsedDocID = field.docId;
      mappedField.parsedData.parsedDocURI = docRefs[field.docId];
      if (field.userCreated) {
        mappedField.parsedData.userCreated = true;
      }
      if (mappedField.parsedData.parsedDocURI == null) {
        cfg.errorService.error('Could not find document URI for parsed mapped field.',
          { 'fieldJSON': field, 'knownDocs': docRefs });
        return null;
      }
      if (field.actions) {
        for (const action of field.actions) {
          if (action['@type']) {
            MappingSerializer.parseNewAction(action, mappedField.parsedData.parsedActions);
          } else {
            MappingSerializer.parseOldAction(action, mappedField.parsedData.parsedActions);
          }
        }
      }
    }
    fieldPair.addMappedField(mappedField, isSource);
    return mappedField;
  }

  /**
   * @deprecated actionName: {param:...} style has been deprecated. Use {`@type`: actionName} style action description.
   */
  private static parseOldAction(action: any, parsedActions: FieldAction[]) {
    for (const actionName of Object.keys(action)) {
      if (!action.hasOwnProperty(actionName)) {
        return;
      }
      const parsedAction: FieldAction = new FieldAction();
      parsedAction.name = actionName;
      const actionParams: any = action[actionName];
      if (actionParams) {
        for (const paramName of Object.keys(actionParams)) {
          if (!actionParams.hasOwnProperty(paramName)) {
            return;
          }
          const parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
          parsedArgumentValue.name = paramName;
          let value = actionParams[paramName];
          value = value == null ? null : value.toString();
          parsedArgumentValue.value = value;
          parsedAction.argumentValues.push(parsedArgumentValue);
        }
      }
      parsedActions.push(parsedAction);
    }
  }

  private static parseNewAction(action: any, parsedActions: FieldAction[]) {
    const parsedAction: FieldAction = new FieldAction();
    parsedAction.name = action['@type'];
    for (const [key, value] of Object.entries(action)) {
      if ('@type' === key) {
        continue;
      }
      const parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
      parsedArgumentValue.name = key;
      const valueString = value == null ? null : value.toString();
      parsedArgumentValue.value = valueString;
      parsedAction.argumentValues.push(parsedArgumentValue);
    }
    parsedActions.push(parsedAction);
  }
}
