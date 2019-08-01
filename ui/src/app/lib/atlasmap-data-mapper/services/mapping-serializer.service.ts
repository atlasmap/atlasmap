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

import { TransitionMode } from '../models/transition.model';
import { FieldActionArgument, FieldAction, FieldActionArgumentValue } from '../models/field-action.model';
import { MappingModel, MappedField } from '../models/mapping.model';
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
    for (const mapping of mappingDefinition.mappings.filter(m => m.isFullyMapped())) {
      try {
        jsonMappings = jsonMappings.concat(MappingSerializer.serializeFieldMapping(cfg, mapping, mapping.uuid));
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

  static serializeFieldMapping(
    cfg: ConfigModel, mapping: MappingModel,
    id: string, ignoreValue: boolean = true): any {
    let inputFieldGroup = {};
    const jsonMappingType = ConfigModel.mappingServicesPackagePrefix + '.Mapping';
    const serializedInputFields: any[] = MappingSerializer.serializeFields(mapping, true, cfg, ignoreValue);
    const serializedOutputFields: any[] = MappingSerializer.serializeFields(mapping, false, cfg, ignoreValue);
    let jsonMapping = {};

    if (mapping.getMappedFields(true).length > 1) {
      inputFieldGroup = MappingSerializer.createInputFieldGroup(mapping, serializedInputFields);

      jsonMapping = {
       'jsonType': jsonMappingType,
       'id': id,
       inputFieldGroup,
       'outputField': serializedOutputFields,
      };
    } else {
      if (mapping.transition.isManyToOneMode()) {
        let delimiter = mapping.transition.getActualDelimiter();

        if (mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          delimiter = mapping.transition.userDelimiter;
        }
        serializedInputFields[0].actions = [{
          'Concatenate' : {
            'delimiter' : delimiter
          }
        }];
      }
      jsonMapping = {
        'jsonType': jsonMappingType,
        'id': id,
        'inputField' : serializedInputFields,
        'outputField': serializedOutputFields,
      };
    }

    if (mapping.transition.isEnumerationMode()) {
      jsonMapping['mappingType'] = 'LOOKUP';  /* @deprecated */
      jsonMapping['lookupTableName'] = mapping.transition.lookupTableName;
    }
    return jsonMapping;
  }

  static deserializeMappingServiceJSON(json: any, cfg: ConfigModel): void {
    if (!cfg.mappings) {
      cfg.mappings = new MappingDefinition;
    }
    cfg.mappings.name = this.deserializeAtlasMappingName(json);
    cfg.mappings.parsedDocs = cfg.mappings.parsedDocs.concat(MappingSerializer.deserializeDocs(json, cfg.mappings));
    cfg.mappings.mappings = cfg.mappings.mappings.concat(MappingSerializer.deserializeMappings(json, cfg));
    for (const lookupTable of MappingSerializer.deserializeLookupTables(json)) {
      cfg.mappings.addTable(lookupTable);
    }
    for (const field of MappingSerializer.deserializeConstants(json)) {
      cfg.constantDoc.addField(field);
    }
    for (const field of MappingSerializer.deserializeProperties(json)) {
      cfg.propertyDoc.addField(field);
    }
  }

  /**
   * Return the AtlasMap mappings file name from the specified JSON buffer or an empty string.
   *
   * @param json
   */
  static deserializeAtlasMappingName(json: any): string {
    if (json && json.AtlasMapping && json.AtlasMapping.name) {
      return json.AtlasMapping.name;
    } else {
      return '';
    }
  }

  static deserializeFieldMapping(
    mappingJson: any, docRefs: any, cfg: ConfigModel, ignoreValue: boolean = true): MappingModel {
    const mapping = new MappingModel();
    mapping.uuid = mappingJson.id;
    mapping.sourceFields = [];
    mapping.targetFields = [];
    mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    const isLookupMapping = (mappingJson.mappingType === 'LOOKUP');

    if (mappingJson.mappingType && mappingJson.mappingType !== '') {
      this.deserializeFieldMappingFromType(mapping, mappingJson, docRefs, cfg, ignoreValue);
      return mapping;
    }

    let inputField = [];

    if (mappingJson.inputFieldGroup) {
      inputField = mappingJson.inputFieldGroup.field;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
      }
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      cfg.mappings.updateMappedFieldsFromDocuments(mapping, cfg, null, true);

      // Check for an InputFieldGroup containing a concatenate action inferring combine mode.
      const firstAction = mappingJson.inputFieldGroup.actions[0];
      if (firstAction) {
        if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          mapping.transition.enableExpression = true;
          mapping.transition.expression = new ExpressionModel(mapping, cfg);
          const expr = firstAction.Expression ? firstAction.Expression.expression : firstAction['expression'];
          mapping.transition.expression.insertText(expr);
        } else if (firstAction.Concatenate || firstAction['@type'] === 'Concatenate') {
          const concatDelimiter =
            firstAction.Concatenamte ? firstAction.Concatenate.delimiter : firstAction['delimiter'];
          if (concatDelimiter) {
            mapping.transition.mode = TransitionMode.MANY_TO_ONE;
            mapping.transition.delimiter =
              TransitionModel.getTransitionDelimiterFromActual(concatDelimiter);

            if (mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
              mapping.transition.userDelimiter = concatDelimiter;
            }
          }
        }
      }
    } else {
      inputField = mappingJson.inputField;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
      }

      if (cfg.mappings) {
        cfg.mappings.updateMappedFieldsFromDocuments(mapping, cfg, null, true);
      }

      if (inputField[0].actions && inputField[0].actions[0]) {
        // Check for an InputField containing a split action inferring separate mode.
        const firstAction = inputField[0].actions[0];
        if (firstAction.Split || firstAction['@type'] === 'Split') {
          const splitDelimiter = firstAction.Split ? firstAction.Split.delimiter : firstAction['delimiter'];
          if (splitDelimiter) {
            mapping.transition.mode = TransitionMode.ONE_TO_MANY;
            mapping.transition.delimiter =
              TransitionModel.getTransitionDelimiterFromActual(splitDelimiter);
            if (mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
              mapping.transition.userDelimiter = splitDelimiter;
            }
          }
        } else if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          mapping.transition.enableExpression = true;
          mapping.transition.expression = new ExpressionModel(mapping, cfg);
          const expr = firstAction.Expression ? firstAction.Expression.expression : firstAction['expression'];
          mapping.transition.expression.insertText(expr);
        }
      }
    }

    for (const field of mappingJson.outputField) {
      MappingSerializer.addFieldIfDoesntExist(mapping, field, false, docRefs, cfg, ignoreValue);
    }

    if (isLookupMapping) {
      mapping.transition.lookupTableName = mappingJson.lookupTableName;
      mapping.transition.mode = TransitionMode.ENUM;
    }

    return mapping;
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

  private static createInputFieldGroup(mapping: MappingModel, field: any[]): any {
    const actions = [];

    if (mapping.transition.isManyToOneMode()) {
      if (mapping.transition.enableExpression) {
        actions[0] = {
          'Expression' : {
            'expression' : mapping.transition.expression.toText()
          }
        };
      } else {
        let delimiter = mapping.transition.getActualDelimiter();

        if (mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          delimiter = mapping.transition.userDelimiter;
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
  private static processActionArguments(action: FieldAction, cfg: ConfigModel): any {
    let actionArguments: any = {};
    for (const argValue of action.argumentValues) {
      actionArguments[argValue.name] = argValue.value;
      const argumentConfig: FieldActionArgument = action.definition.getArgumentForName(argValue.name);
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
    mapping: MappingModel, isSource: boolean,
    cfg: ConfigModel, ignoreValue: boolean = false): any[] {
    const fields: MappedField[] = mapping.getMappedFields(isSource);
    const fieldsJson: any[] = [];
    for (const mappedField of fields) {
      if (!mappedField.field || mappedField.isPadField()) {
        continue;
      }

      const field: Field = mappedField.field;
      const serializedField: any = {
        'jsonType': field.serviceObject.jsonType,
        'name': field.name,
        'path': field.path,
        'fieldType': field.type,
        'docId': field.docDef.id,
      };

      if (isSource && fields.length === 1 && mapping.transition.enableExpression) {
        serializedField['actions'] = [ {
          'Expression' : {
            'expression' : mapping.transition.expression.toText()
          }
        } ];
      }

      if (mapping.transition.isOneToManyMode() && field.isSource()) {
        let delimiter = mapping.transition.getActualDelimiter();

        if (mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          delimiter = mapping.transition.userDelimiter;
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

      let includeIndexes: boolean = mapping.transition.isOneToManyMode() && !isSource;
      includeIndexes = includeIndexes || (mapping.transition.isManyToOneMode() && isSource);
      if (includeIndexes) {
        serializedField['index'] = mapping.getIndexForMappedField(mappedField) - 1;
      }

      if (mappedField.actions.length) {
        const actions: any[] = [];
        let updatedActions = 0;

        for (const action of mappedField.actions) {
          if (action.isSeparateOrCombineMode || action.definition.serviceObject.name === 'Split') {
            continue;
          }
          updatedActions++;

          // Serialize custom field actions.
          if (action.definition.isCustom) {
              const customActionJson: any = {};
              let customActionBody = {};
              customActionBody['name'] = action.definition.serviceObject.name;
              customActionBody['className'] = action.definition.serviceObject.className;
              customActionBody['methodName'] = action.definition.serviceObject.method;
              // customActionBody['arguments'] = MappingSerializer.processActionArguments(action, cfg);
              customActionBody = (Object.keys(customActionBody).length === 0) ? null : customActionBody;
              customActionJson['CustomAction'] = customActionBody;
              actions.push(customActionJson);
              continue;
          }

          let actionArguments: any = {};
          actionArguments = MappingSerializer.processActionArguments(action, cfg);
          const actionJson: any = {};
          actionJson[action.definition.name] = actionArguments;
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

  private static deserializeDocs(json: any, mappingDefinition: MappingDefinition): DocumentDefinition[] {
    const docs: DocumentDefinition[] = [];
    if (!json || !json.AtlasMapping) {
      return;
    }
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

  private static deserializeMappings(json: any, cfg: ConfigModel): MappingModel[] {
    const mappings: MappingModel[] = [];
    const docRefs: any = {};
    for (const docRef of json.AtlasMapping.dataSource) {
      docRefs[docRef.id] = docRef.uri;
    }
    for (const fieldMapping of json.AtlasMapping.mappings.mapping) {
      // for backward compatibility
      const isCollectionMapping = (fieldMapping.jsonType === ConfigModel.mappingServicesPackagePrefix + '.Collection');
      if (isCollectionMapping) {
        for (const innerFieldMapping of fieldMapping.mappings.mapping) {
          mappings.push(MappingSerializer.deserializeFieldMapping(innerFieldMapping, docRefs, cfg));
        }
      } else {
        mappings.push(MappingSerializer.deserializeFieldMapping(fieldMapping, docRefs, cfg));
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
  private static deserializeFieldMappingFromType(mapping: MappingModel,
          fieldMapping: any, docRefs: any, cfg: ConfigModel, ignoreValue: boolean): void {
    const isSeparateMapping = (fieldMapping.mappingType === 'SEPARATE');
    const isLookupMapping = (fieldMapping.mappingType === 'LOOKUP');
    const isCombineMapping = (fieldMapping.mappingType === 'COMBINE');

    for (const field of fieldMapping.inputField) {
       MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
    }
    for (const field of fieldMapping.outputField) {
       MappingSerializer.addFieldIfDoesntExist(mapping, field, false, docRefs, cfg, ignoreValue);
    }
    if (isSeparateMapping) {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      mapping.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.delimiter);
    } else if (isCombineMapping) {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      mapping.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.delimiter);
    } else if (isLookupMapping) {
      mapping.transition.lookupTableName = fieldMapping.lookupTableName;
      mapping.transition.mode = TransitionMode.ENUM;
    } else {
      mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    }
  }

  private static deserializeConstants(jsonMapping: any): Field[] {
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

  private static deserializeProperties(jsonMapping: any): Field[] {
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

  private static deserializeLookupTables(jsonMapping: any): LookupTable[] {
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

  private static addFieldIfDoesntExist(
    mapping: MappingModel, field: any, isSource: boolean,
    docRefs: any, cfg: ConfigModel, ignoreValue: boolean = true): MappedField {
    const mappedField: MappedField = new MappedField();

    mappedField.parsedData.parsedValueType = field.fieldType;
    mappedField.parsedData.parsedIndex = '0';
    if (field.index != null) {
      mappedField.parsedData.parsedIndex = (field.index).toString();
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
    mapping.addMappedField(mappedField, isSource);
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
