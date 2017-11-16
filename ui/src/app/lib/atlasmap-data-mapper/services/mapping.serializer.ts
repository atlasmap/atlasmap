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

import { TransitionMode, FieldActionArgument,
    FieldAction, FieldActionArgumentValue } from '../models/transition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping.definition.model';
import { DocumentDefinition, NamespaceModel } from '../models/document.definition.model';
import { LookupTable, LookupTableEntry } from '../models/lookup.table.model';
import { ConfigModel } from '../models/config.model';

export class MappingSerializer {

    public static serializeMappings(cfg: ConfigModel): any {
        const mappingDefinition: MappingDefinition = cfg.mappings;
        let jsonMappings: any[] = [];
        for (const mapping of mappingDefinition.mappings) {
            try {
                let fieldMappingsForThisMapping: any[] = [];
                let jsonMapping: any;

                for (const fieldMappingPair of mapping.fieldMappings) {
                    const serializedInputFields: any[] = MappingSerializer.serializeFields(fieldMappingPair, true, cfg);
                    const serializedOutputFields: any[] = MappingSerializer.serializeFields(fieldMappingPair, false, cfg);

                    jsonMapping = {
                        'jsonType' : 'io.atlasmap.v2.Mapping',
                        'mappingType' : 'MAP',
                        'id' : mapping.uuid,
                        'inputField': serializedInputFields,
                        'outputField': serializedOutputFields,
                    };

                    if (fieldMappingPair.transition.isSeparateMode()) {
                        jsonMapping['mappingType'] = 'SEPARATE';
                        jsonMapping['strategy'] = fieldMappingPair.transition.getSerializedDelimeter();
                    } else if (fieldMappingPair.transition.isCombineMode()) {
                        jsonMapping['mappingType'] = 'COMBINE';
                        jsonMapping['strategy'] = fieldMappingPair.transition.getSerializedDelimeter();
                    } else if (fieldMappingPair.transition.isEnumerationMode()) {
                        jsonMapping['mappingType'] = 'LOOKUP';
                        jsonMapping['lookupTableName'] = fieldMappingPair.transition.lookupTableName;
                    }
                    fieldMappingsForThisMapping.push(jsonMapping);
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
                        'mappingType' : 'COLLECTION',
                        'collectionType': collectionType,
                        'mappings': { 'mapping': fieldMappingsForThisMapping },
                    };
                    fieldMappingsForThisMapping = [jsonMapping];
                }

                jsonMappings = jsonMappings.concat(fieldMappingsForThisMapping);
            } catch (e) {
                const input: any = { 'mapping': mapping, 'mapping def': mappingDefinition };
                cfg.errorService.error('Caught exception while attempting to serialize mapping, skipping. ', { 'input': input, 'error': e});
            }
        }

        const serializedLookupTables: any[] = MappingSerializer.serializeLookupTables(cfg.mappings);
        const propertyDescriptions: any[] = MappingSerializer.serializeProperties(cfg.propertyDoc);
        const serializedDataSources: any = MappingSerializer.serializeDocuments(cfg.sourceDocs.concat(cfg.targetDocs), mappingDefinition);

        const payload: any = {
            'AtlasMapping': {
                'jsonType': ConfigModel.mappingServicesPackagePrefix + '.AtlasMapping',
                'dataSource': serializedDataSources,
                'mappings': { 'mapping': jsonMappings },
                'name': cfg.mappings.name,
                'lookupTables': { 'lookupTable': serializedLookupTables },
                'properties': { 'property': propertyDescriptions },
            },
        };
        return payload;
    }

    private static serializeDocuments(docs: DocumentDefinition[], mappingDefinition: MappingDefinition): any[] {
        const serializedDocs: any[] = [];
        for (const doc of docs) {
            const docType: string = doc.isSource ? 'SOURCE' : 'TARGET';
            const serializedDoc: any = {
                'jsonType' : 'io.atlasmap.v2.DataSource',
                'id': doc.initCfg.shortIdentifier,
                'uri': doc.uri,
                'dataSourceType': docType,
            };
            if (doc.characterEncoding != null) {
                serializedDoc['characterEncoding'] = doc.characterEncoding;
            }
            if (doc.locale != null) {
                serializedDoc['locale'] = doc.locale;
            }
            if (doc.initCfg.type.isXML()) {
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
            } else if (doc.initCfg.type.isJSON()) {
                if (!doc.isSource) {
                    serializedDoc['template'] = mappingDefinition.templateText;
                }
                serializedDoc['jsonType'] = 'io.atlasmap.json.v2.JsonDataSource';
            }

            serializedDocs.push(serializedDoc);
        }
        return serializedDocs;
    }

    private static serializeProperties(docDef: DocumentDefinition): any[] {
        const propertyDescriptions: any[] = [];
        for (const field of docDef.fields) {
            propertyDescriptions.push({ 'name': field.name,
                                        'value': field.value, 'fieldType': field.type });
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

    private static serializeFields(fieldPair: FieldMappingPair, isSource: boolean, cfg: ConfigModel): any[] {
        const fields: MappedField[] = fieldPair.getMappedFields(isSource);
        const fieldsJson: any[] = [];
        for (const mappedField of fields) {
            const field: Field = mappedField.field;
            if (DocumentDefinition.getNoneField().path == field.path) {
                //do not include "none" options from drop downs in mapping
                continue;
            }

            const serializedField: any = {
                'jsonType': field.serviceObject.jsonType,
                'name': field.name,
                'path': field.path,
                'fieldType': field.type,
                'value': field.value,
                'docId': field.docDef.initCfg.shortIdentifier,
            };
            if (field.docDef.initCfg.type.isXML() || field.docDef.initCfg.type.isJSON()) {
                serializedField['userCreated'] = field.userCreated;
            }
            if (field.isProperty()) {
                serializedField['jsonType'] = ConfigModel.mappingServicesPackagePrefix + '.PropertyField';
                serializedField['name'] = field.path;
            } else if (field.isConstant()) {
                serializedField['jsonType'] = ConfigModel.mappingServicesPackagePrefix + '.ConstantField';
                delete(serializedField['name']);
            } else if (field.enumeration) {
                serializedField['jsonType'] = 'io.atlasmap.java.v2.JavaEnumField';
            } else {
                delete(serializedField['value']);
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
                for (const action of mappedField.actions) {
                    if (action.isSeparateOrCombineMode) {
                        continue;
                    }

                    let actionArguments: any = {};
                    for (const argValue of action.argumentValues) {
                        actionArguments[argValue.name] = argValue.value;
                        const argumentConfig: FieldActionArgument = action.config.getArgumentForName(argValue.name);
                        if (argumentConfig == null) {
                            cfg.errorService.error('Cannot find action argument with name: ' + argValue.name, action);
                            continue;
                        }
                        if (argumentConfig.type == 'INTEGER') {
                            actionArguments[argValue.name] = parseInt(argValue.value, 10);
                        }
                    }

                    actionArguments = (Object.keys(actionArguments).length == 0) ? null : actionArguments;

                    const actionJson: any = {};
                    actionJson[action.config.name] = actionArguments;
                    actions.push(actionJson);
                }
                if (actions.length > 0) {
                    serializedField['actions'] = actions;
                }
            }

            fieldsJson.push(serializedField);
        }

        return fieldsJson;
    }

    public static deserializeMappingServiceJSON(json: any, mappingDefinition: MappingDefinition, cfg: ConfigModel): void {
        if (json && json.AtlasMapping && json.AtlasMapping.name) {
            mappingDefinition.name = json.AtlasMapping.name;
        }
        mappingDefinition.parsedDocs = mappingDefinition.parsedDocs.concat(MappingSerializer.deserializeDocs(json, mappingDefinition));
        mappingDefinition.mappings = mappingDefinition.mappings.concat(MappingSerializer.deserializeMappings(json, cfg));
        for (const lookupTable of MappingSerializer.deserializeLookupTables(json)) {
            mappingDefinition.addTable(lookupTable);
        }
        for (const field of MappingSerializer.deserializeProperties(json)) {
            cfg.propertyDoc.addField(field);
        }
    }

    public static deserializeDocs(json: any, mappingDefinition: MappingDefinition): DocumentDefinition[] {
        const docs: DocumentDefinition[] = [];
        for (const docRef of json.AtlasMapping.dataSource) {
            const doc: DocumentDefinition = new DocumentDefinition();
            doc.isSource = (docRef.dataSourceType == 'SOURCE');
            doc.initCfg.documentIdentifier = docRef.uri;
            doc.initCfg.shortIdentifier = docRef.id;
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

    public static deserializeMappings(json: any, cfg: ConfigModel): MappingModel[] {
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

            const isCollectionMapping = (fieldMapping.jsonType == ConfigModel.mappingServicesPackagePrefix + '.Collection');
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

    public static deserializeFieldMapping(fieldMapping: any, docRefs: any, cfg: ConfigModel): FieldMappingPair {
        const fieldPair: FieldMappingPair = new FieldMappingPair();
        fieldPair.sourceFields = [];
        fieldPair.targetFields = [];

        const isSeparateMapping = (fieldMapping.mappingType == 'SEPARATE');
        const isLookupMapping = (fieldMapping.mappingType == 'LOOKUP');
        const isCombineMapping = (fieldMapping.mappingType == 'COMBINE');
        for (const field of fieldMapping.inputField) {
            MappingSerializer.addFieldIfDoesntExist(fieldPair, field, true, docRefs, cfg);
        }
        for (const field of fieldMapping.outputField) {
            MappingSerializer.addFieldIfDoesntExist(fieldPair, field, false, docRefs, cfg);
        }
        if (isSeparateMapping) {
            fieldPair.transition.mode = TransitionMode.SEPARATE;
            fieldPair.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.strategy);
        } else if (isCombineMapping) {
            fieldPair.transition.mode = TransitionMode.COMBINE;
            fieldPair.transition.setSerializedDelimeterFromSerializedValue(fieldMapping.strategy);
        } else if (isLookupMapping) {
            fieldPair.transition.lookupTableName = fieldMapping.lookupTableName;
            fieldPair.transition.mode = TransitionMode.ENUM;
        } else {
            fieldPair.transition.mode = TransitionMode.MAP;
        }

        return fieldPair;
    }

    public static deserializeProperties(jsonMapping: any): Field[] {
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

    public static deserializeLookupTables(jsonMapping: any): LookupTable[] {
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

    private static addFieldIfDoesntExist(fieldPair: FieldMappingPair, field: any,
                                         isSource: boolean, docRefs: any, cfg: ConfigModel): MappedField {
        const mappedField: MappedField = new MappedField();

        mappedField.parsedData.parsedValueType = field.fieldType;
        mappedField.parsedData.parsedIndex = '1';
        if (field.index != null) {
            mappedField.parsedData.parsedIndex = (field.index + 1).toString();
        }
        if (field.jsonType == (ConfigModel.mappingServicesPackagePrefix + '.PropertyField')) {
            mappedField.parsedData.parsedName = field.name;
            mappedField.parsedData.parsedPath = field.name;
            mappedField.parsedData.fieldIsProperty = true;
        } else if (field.jsonType == (ConfigModel.mappingServicesPackagePrefix + '.ConstantField')) {
            mappedField.parsedData.fieldIsConstant = true;
            mappedField.parsedData.parsedValue = field.value;
            mappedField.parsedData.parsedPath = field.path;
        } else {
            if (field.docId == null) {
                cfg.errorService.error('Parsed mapping field does not have document id, dropping.', field);
                return null;
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
                    for (const actionName in action) {
                        if (!action.hasOwnProperty(actionName)) {
                            continue;
                        }
                        const parsedAction: FieldAction = new FieldAction();
                        parsedAction.name = actionName;
                        const actionParams: any = action[actionName];
                        if (actionParams) {
                            for (const paramName in actionParams) {
                                if (!actionParams.hasOwnProperty(paramName)) {
                                    continue;
                                }
                                const parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
                                parsedArgumentValue.name = paramName;
                                let value = actionParams[paramName];
                                value = value == null ? null : value.toString();
                                parsedArgumentValue.value = value;
                                parsedAction.argumentValues.push(parsedArgumentValue);
                            }
                        }
                        mappedField.parsedData.parsedActions.push(parsedAction);
                    }
                }
            }
        }
        fieldPair.addMappedField(mappedField, isSource);
        return mappedField;
    }
}
