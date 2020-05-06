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
import { FieldActionArgument, FieldAction, FieldActionArgumentValue, Multiplicity } from '../models/field-action.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { DocumentDefinition, NamespaceModel } from '../models/document-definition.model';
import { LookupTable, LookupTableEntry } from '../models/lookup-table.model';
import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { ErrorInfo, ErrorLevel, ErrorType, ErrorScope } from '../models/error.model';
import { ExpressionModel } from '../models/expression.model';
import { MappingUtil } from './mapping-util';

export class MappingSerializer {

  static serializeMappings(cfg: ConfigModel): any {
    // TODO: check this non null operator
    const mappingDefinition: MappingDefinition = cfg.mappings!;
    let jsonMappings: any[] = [];
    for (const mapping of mappingDefinition.mappings.filter(m => m.isFullyMapped())) {
      try {
        jsonMappings = jsonMappings.concat(MappingSerializer.serializeFieldMapping(cfg, mapping, mapping.uuid));
      } catch (e) {
        const input: any = { 'mapping': mapping, 'mapping def': mappingDefinition };
        cfg.errorService.addError(new ErrorInfo({message:
          'Caught exception while attempting to serialize mapping, skipping. ',
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: { 'input': input, 'error': e }}));
      }
    }

    // TODO: check this non null operator
    const serializedLookupTables: any[] = MappingSerializer.serializeLookupTables(cfg.mappings!);
    const constantDescriptions: any[] = MappingSerializer.serializeConstants(cfg.constantDoc);
    const propertyDescriptions: any[] = MappingSerializer.serializeProperties(cfg.propertyDoc);
    const serializedDataSources: any = MappingSerializer.serializeDocuments(cfg.sourceDocs.concat(cfg.targetDocs), mappingDefinition);

    const payload: any = {
      'AtlasMapping': {
        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.AtlasMapping',
        'dataSource': serializedDataSources,
        'mappings': { 'mapping': jsonMappings },
        'name': cfg.mappings!.name, // TODO: check this non null operator
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
    let jsonMapping: {[key: string]: any} = {};

    if (mapping.transition.isManyToOneMode()) {
      inputFieldGroup = MappingSerializer.createInputFieldGroup(mapping, serializedInputFields, cfg);

      jsonMapping = {
       'jsonType': jsonMappingType,
       'id': id,
       'expression' : MappingUtil.getMappingExpressionStr(false, mapping),
       inputFieldGroup,
       'outputField': serializedOutputFields,
      };
    } else {
      if (mapping.transition.enableExpression) {
        jsonMapping = {
          'jsonType': jsonMappingType,
          'id': id,
          'expression' : MappingUtil.getMappingExpressionStr(false, mapping),
          'inputField' : serializedInputFields,
          'outputField': serializedOutputFields,
        };
      } else {
        jsonMapping = {
          'jsonType': jsonMappingType,
          'id': id,
          'inputField' : serializedInputFields,
          'outputField': serializedOutputFields,
        };
      }
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
    cfg.mappings.parsedDocs = cfg.mappings.parsedDocs.concat(MappingSerializer.deserializeDocs(json, cfg.mappings)!); // TODO: check this non null operator
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
    const isLookupMapping = (mappingJson.mappingType === 'LOOKUP') || mappingJson.lookupTableName != null;

    if (mappingJson.mappingType && mappingJson.mappingType !== '') {
      this.deserializeFieldMappingFromType(mapping, mappingJson, docRefs, cfg, ignoreValue);
      return mapping;
    }

    let inputField = [];
    if (mappingJson.inputFieldGroup) {

      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      inputField = mappingJson.inputFieldGroup.field;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
      }
      MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, null, true);

      // Check for an InputFieldGroup containing a many-to-one action
      const firstAction = mappingJson.inputFieldGroup.actions[0];
      if (firstAction) {
        // @deprecated Support legacy ADM files that have transformation-action-based expressions.
        if (firstAction.Expression || firstAction['@type'] === 'Expression') {
          mapping.transition.enableExpression = true;
          mapping.transition.expression = new ExpressionModel(mapping, cfg);
          const expr = firstAction.Expression ? firstAction.Expression.expression : firstAction['expression'];
          mapping.transition.expression.insertText(expr);
        } else {
          mapping.transition.mode = TransitionMode.MANY_TO_ONE;
          const parsedAction = this.parseAction(firstAction);
          // TODO: check this non null operator
          parsedAction.definition = cfg.fieldActionService.getActionDefinitionForName(parsedAction.name, Multiplicity.MANY_TO_ONE)!;
          mapping.transition.transitionFieldAction = parsedAction;
        }
      }
    } else {
      inputField = mappingJson.inputField;

      for (const field of inputField) {
        MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
      }

      if (cfg.mappings) {
        MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, null, true);
      }
    }

    if (mappingJson.expression && mappingJson.expression.length > 0) {
      mapping.transition.enableExpression = true;
      mapping.transition.expression = new ExpressionModel(mapping, cfg);
      mapping.transition.expression.insertText(mappingJson.expression);
    }

    for (const field of mappingJson.outputField) {
      MappingSerializer.addFieldIfDoesntExist(mapping, field, false, docRefs, cfg, ignoreValue);
    }
    MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, null, false);

    if (isLookupMapping) {
      mapping.transition.lookupTableName = mappingJson.lookupTableName;
      mapping.transition.mode = TransitionMode.ENUM;
    }

    return mapping;
  }

  static deserializeAudits(audits: any, errorType: ErrorType): ErrorInfo[] {
    const errors: ErrorInfo[] = [];
    if (!audits && !audits.audit) {
      return errors;
    }
    for (const audit of audits.audit) {
      const msg = audit.status + '[' + audit.path + ']: ' + audit.message;
      errors.push(new ErrorInfo({message: msg, level: audit.status, scope: ErrorScope.MAPPING,
        type: errorType, object: audit.audit?.value}));
    }
    return errors;
  }

  private static createInputFieldGroup(mapping: MappingModel, field: any[], cfg: ConfigModel): any {
    const actions = [];

    if (mapping.transition.isManyToOneMode() &&
      mapping.transition.transitionFieldAction) {
      actions[0] = this.serializeAction(mapping.transition.transitionFieldAction, cfg);
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
    const actionArguments: any = {};
    for (const argValue of action.argumentValues) {
      if (action.definition.isCustom && ['methodName', 'className', 'name'].includes(argValue.name)) {
        continue;
      }
      actionArguments[argValue.name] = argValue.value;
      const argumentConfig: FieldActionArgument = action.definition.getArgumentForName(argValue.name);
      if (argumentConfig == null) {
        cfg.errorService.addError(new ErrorInfo({message: `Cannot find action argument ${argValue.name}: ${argValue.value}`,
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: action}));
         continue;
      }
      if (argumentConfig.type === 'INTEGER') {
        actionArguments[argValue.name] = parseInt(argValue.value, 10);
      }
    }
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
        // TODO: check this non null operator
        serializedField['index'] = mapping.getIndexForMappedField(mappedField)! - 1;
      }

      this.serializeActions( cfg, mappedField, serializedField );
      fieldsJson.push(serializedField);
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
  private static serializeActions( cfg: ConfigModel, mappedField: MappedField, serializedField: any ): void {
    if ( mappedField.actions.length ) {
      const actions: any[] = [];

      for ( const action of mappedField.actions ) {
        const actionJson = this.serializeAction( action, cfg );
        if ( actionJson ) {
          actions.push( actionJson );
        }
      }
      if ( actions.length > 0 ) {
        serializedField['actions'] = actions;
      }
    }
  }

  private static serializeAction(action: FieldAction, cfg: ConfigModel): any {
    let actionJson: any = MappingSerializer.processActionArguments(action, cfg);
    if (action.definition.isCustom) {
      actionJson = [];  // ref https://github.com/atlasmap/atlasmap/issues/1757
      actionJson['@type'] = 'CustomAction';
      actionJson['name'] = action.definition.serviceObject.name;
      actionJson['className'] = action.definition.serviceObject.className;
      actionJson['methodName'] = action.definition.serviceObject.method;
    } else {
      actionJson['@type'] = action.definition.name;
    }
    return actionJson;
  }

  private static deserializeDocs(json: any, mappingDefinition: MappingDefinition): DocumentDefinition[] | null {
    const docs: DocumentDefinition[] = [];
    if (!json || !json.AtlasMapping) {
      return null;
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

    if (!json || !json.AtlasMapping) {
      return mappings;
    }
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
    if (fieldMapping.mappingType === 'SEPARATE') {
      mapping.transition.mode = TransitionMode.ONE_TO_MANY;
      mapping.transition.transitionFieldAction
        = FieldAction.create(cfg.fieldActionService.getActionDefinitionForName('Split', Multiplicity.ONE_TO_MANY)!); // TODO: check this non null operator
      mapping.transition.transitionFieldAction.setArgumentValue('delimiter', fieldMapping.delimiter);
    } else if (fieldMapping.mappingType === 'LOOKUP') {
      mapping.transition.mode = TransitionMode.ENUM;
      mapping.transition.lookupTableName = fieldMapping.lookupTableName;
    } else if (fieldMapping.mappingType === 'COMBINE') {
      mapping.transition.mode = TransitionMode.MANY_TO_ONE;
      mapping.transition.transitionFieldAction
        = FieldAction.create(cfg.fieldActionService.getActionDefinitionForName('Concatenate', Multiplicity.MANY_TO_ONE)!); // TODO: check this non null operator
      mapping.transition.transitionFieldAction.setArgumentValue('delimiter', fieldMapping.delimiter);
    } else {
      mapping.transition.mode = TransitionMode.ONE_TO_ONE;
    }

    for (const field of fieldMapping.inputField) {
       MappingSerializer.addFieldIfDoesntExist(mapping, field, true, docRefs, cfg, ignoreValue);
    }
    for (const field of fieldMapping.outputField) {
       MappingSerializer.addFieldIfDoesntExist(mapping, field, false, docRefs, cfg, ignoreValue);
    }
    MappingUtil.updateMappedFieldsFromDocuments(mapping, cfg, null, true);
  }

  private static deserializeConstants(jsonMapping: any): Field[] {
    const fields: Field[] = [];
    if (!jsonMapping || !jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.constants
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
    if (!jsonMapping || !jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.properties
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
    if (!jsonMapping || !jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.lookupTables
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

  /**
   * Walk the list of field actions found in the parsed data and restore them to the live mapping.
   *
   * @param field
   * @param mappedField
   * @param mapping
   * @param cfg
   * @param isSource
   */
  private static deserializeFieldActions( field: any, mappedField: MappedField, mapping: MappingModel,
                                          cfg: ConfigModel, isSource: boolean ): void {
    if (field.Expression) {
      const expr = field.Expression.expression;
      mapping.transition.expression.insertText(expr);
    }
    for ( const action of field.actions ) {
      const parsedAction = this.parseAction( action );
      parsedAction.definition = cfg.fieldActionService.getActionDefinitionForName( parsedAction.name)!; // TODO: check this non null operator

      // @deprecated Support old-style transformation-action-based expressions.
      if ( isSource && ( action.Expression || action['@type'] === 'Expression' ) ) {
        mapping.transition.enableExpression = true;
        mapping.transition.expression = new ExpressionModel( mapping, cfg );
        const expr = action.Expression ? action.Expression.expression : action['expression'];
        mapping.transition.expression.insertText( expr );
      } else if ( isSource && parsedAction.definition && [Multiplicity.ONE_TO_MANY, Multiplicity.MANY_TO_ONE]
        .includes( parsedAction.definition.multiplicity ) ) {
        if ( mapping.transition.transitionFieldAction ) {
            cfg.logger!.warn( `Duplicated multiplicity transformations were detected: \
              ${mapping.transition.transitionFieldAction.name} is being overwritten by ${parsedAction.name} ...` );
        }
        mapping.transition.transitionFieldAction = parsedAction;
      } else {
        mappedField.parsedData.parsedActions.push( parsedAction );
      }
    }
  }

  private static addFieldIfDoesntExist(
    mapping: MappingModel, field: any, isSource: boolean,
    docRefs: any, cfg: ConfigModel, ignoreValue: boolean = true): MappedField | null {
    const mappedField: MappedField = new MappedField();

    mappedField.parsedData.parsedValueType = field.fieldType;
    mappedField.parsedData.parsedIndex = '0';
    if (field.index != null) {
      mappedField.parsedData.parsedIndex = (field.index).toString();
    }
    if (field.jsonType === (ConfigModel.mappingServicesPackagePrefix + '.PropertyField')) {
      mappedField.parsedData.parsedName = field.name;
      mappedField.parsedData.parsedPath = field.name;
      mappedField.parsedData.parsedValue = field.value;
      mappedField.parsedData.fieldIsProperty = true;
    } else if (field.jsonType === (ConfigModel.mappingServicesPackagePrefix + '.ConstantField')) {
      mappedField.parsedData.fieldIsConstant = true;
      mappedField.parsedData.parsedValue = field.value;
      mappedField.parsedData.parsedPath = field.path;
    } else {
      if (field.docId == null) {
        cfg.errorService.addError(new ErrorInfo({
          message: 'Parsed mapping field does not have document id, dropping.',
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL, object: field}));
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
        cfg.errorService.addError(new ErrorInfo({
          message: 'Could not find document URI for parsed mapped field.',
          level: ErrorLevel.ERROR, scope: ErrorScope.APPLICATION, type: ErrorType.INTERNAL,
          object: { 'fieldJSON': field, 'knownDocs': docRefs }}));
        return null;
      }
    }
    mapping.addMappedField(mappedField, isSource);
    if (field.actions) {
      this.deserializeFieldActions( field, mappedField, mapping, cfg, isSource );
    }
    return mappedField;
  }

  private static parseAction(action: any): FieldAction {
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
  private static parseOldAction(action: any): FieldAction | null {
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
          const parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
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

  private static parseNewAction(action: any): FieldAction {
    const parsedAction: FieldAction = new FieldAction();
    parsedAction.name = action['@type'];
    for (const [key, value] of Object.entries(action)) {
      if ('@type' === key) {
        continue;
      }
      const parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
      parsedArgumentValue.name = key;
      const valueString = value == null ? null : (value as any).toString();
      parsedArgumentValue.value = valueString;
      parsedAction.argumentValues.push(parsedArgumentValue);
    }
    return parsedAction;
  }
}
