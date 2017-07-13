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

import { TransitionModel, TransitionMode, TransitionDelimiter, FieldActionArgument,
    FieldAction, FieldActionConfig, FieldActionArgumentValue } from '../models/transition.model';
import { MappingModel, FieldMappingPair, MappedField, MappedFieldParsingData } from '../models/mapping.model';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping.definition.model';
import { DocumentDefinition, NamespaceModel } from '../models/document.definition.model';
import { LookupTable, LookupTableEntry } from '../models/lookup.table.model';
import { ConfigModel } from '../models/config.model';

export class MappingSerializer {

    public static serializeMappings(cfg: ConfigModel): any {
        var mappingDefinition: MappingDefinition = cfg.mappings;
        var jsonMappings: any[] = [];
        var tables: LookupTable[] = [];
        for (let m of mappingDefinition.mappings) {
            try {
                var fieldMappingsForThisMapping: any[] = [];
                var jsonMapping: any;

                for (let fieldPair of m.fieldMappings) {
                    var serializedInputFields: any[] = MappingSerializer.serializeFields(fieldPair, true);
                    var serializedOutputFields: any[] = MappingSerializer.serializeFields(fieldPair, false);

                    jsonMapping = {
                        "jsonType" : "io.atlasmap.v2.Mapping",
                        "mappingType" : "MAP",
                        "inputField": serializedInputFields,
                        "outputField": serializedOutputFields,
                    };

                    if (fieldPair.transition.isSeparateMode()) {
                        jsonMapping["mappingType"] = "SEPARATE";
                        jsonMapping["strategy"] = fieldPair.transition.getSerializedDelimeter();
                    } else if (fieldPair.transition.isCombineMode()) {
                        jsonMapping["mappingType"] = "COMBINE";
                        jsonMapping["strategy"] = fieldPair.transition.getSerializedDelimeter();
                    } else if (fieldPair.transition.isEnumerationMode()) {
                        jsonMapping["mappingType"] = "LOOKUP";
                        jsonMapping["lookupTableName"] = fieldPair.transition.lookupTableName;
                    }
                    fieldMappingsForThisMapping.push(jsonMapping);
                }

                if (m.isCollectionMode()) {
                    var collectionType: string = null;
                    for (var field of m.getAllFields()) {
                        collectionType = field.getCollectionType();
                        if (collectionType != null) {
                            break;
                        }
                    }
                    jsonMapping = {
                        "jsonType": ConfigModel.mappingServicesPackagePrefix + ".Collection",
                        "mappingType" : "COLLECTION",
                        "collectionType": collectionType,
                        "mappings": { "mapping": fieldMappingsForThisMapping },
                    };
                    fieldMappingsForThisMapping = [jsonMapping];
                }

                jsonMappings = jsonMappings.concat(fieldMappingsForThisMapping);
            } catch (e) {
                var input: any = { "mapping": m, "mapping def": mappingDefinition };
                console.error("Caught exception while attempting to serialize mapping, skipping. ", { "input": input, "error": e})
            }
        }

        var serializedLookupTables: any[] = MappingSerializer.serializeLookupTables(cfg.mappings);
        var propertyDescriptions: any[] = MappingSerializer.serializeProperties(cfg.propertyDoc);
        var serializedDataSources: any = MappingSerializer.serializeDocuments(cfg.sourceDocs.concat(cfg.targetDocs), mappingDefinition);

        var payload: any = {
            "AtlasMapping": {
                "jsonType": ConfigModel.mappingServicesPackagePrefix + ".AtlasMapping",
                "dataSource": serializedDataSources,
                "mappings": { "mapping": jsonMappings },
                "name": cfg.mappings.name,
                "lookupTables": { "lookupTable": serializedLookupTables },
                "properties": { "property": propertyDescriptions }
            }
        };
        return payload;
    }

    private static serializeDocuments(docs: DocumentDefinition[], mappingDefinition: MappingDefinition): any[] {
        var serializedDocs: any[] = [];
        for (let doc of docs) {
            var docType: string = doc.isSource ? "SOURCE" : "TARGET";
            var serializedDoc: any = {
                "jsonType" : "io.atlasmap.v2.DataSource",
                "id": doc.initCfg.shortIdentifier,
                "uri": doc.uri,
                "dataSourceType": docType
            };
            if (doc.initCfg.type.isXML()) {
                serializedDoc["jsonType"] = "io.atlasmap.xml.v2.XmlDataSource";
                var namespaces: any[] = [];
                for (let ns of doc.namespaces) {
                    namespaces.push({
                        "alias": ns.alias,
                        "uri": ns.uri,
                        "locationUri": ns.locationUri,
                        "targetNamespace": ns.isTarget
                    });
                }
                if (!doc.isSource) {
                    serializedDoc["template"] = mappingDefinition.templateText;
                }
                serializedDoc["xmlNamespaces"] = { "xmlNamespace": namespaces };
            } else if (doc.initCfg.type.isJSON()) {
                if (!doc.isSource) {
                    serializedDoc["template"] = mappingDefinition.templateText;
                }
                serializedDoc["jsonType"] = "io.atlasmap.json.v2.JsonDataSource";
            }

            serializedDocs.push(serializedDoc);
        }
        return serializedDocs;
    }

    private static serializeProperties(docDef: DocumentDefinition): any[] {
        var propertyDescriptions: any[] = [];
        for (let field of docDef.fields) {
            propertyDescriptions.push({ "name": field.name,
                "value": field.value, "fieldType": field.type });
        }
        return propertyDescriptions;
    }

    private static serializeLookupTables(mappingDefinition: MappingDefinition): any[] {
        var tables: LookupTable[] = mappingDefinition.getTables();

        if (!tables || !tables.length) {
            return [];
        }

        var serializedTables: any[] = [];
        for (let t of tables) {
            var lookupEntries: any[] = [];
            for (let e of t.entries) {
                var serialized: any = {
                    "sourceValue": e.sourceValue,
                    "sourceType": e.sourceType,
                    "targetValue": e.targetValue,
                    "targetType": e.targetType
                };
                lookupEntries.push(serialized);
            }

            var serialized: any = {
                "lookupEntry": lookupEntries,
                "name": t.name
            }
            serializedTables.push(serialized);
        }
        return serializedTables;
    }

    private static serializeFields(fieldPair: FieldMappingPair, isSource: boolean): any[] {
        var fields: MappedField[] = fieldPair.getMappedFields(isSource);
        var fieldsJson: any[] = [];
        for (let mappedField of fields) {
            var field: Field = mappedField.field;
            if (DocumentDefinition.getNoneField().path == field.path) {
                //do not include "none" options from drop downs in mapping
                continue;
            }

            var serializedField: any = {
                "jsonType": field.serviceObject.jsonType,
                "name": field.name,
                "path": field.path,
                "fieldType": field.type,
                "value": field.value,
                "docId": field.docDef.initCfg.shortIdentifier
            };
            if (field.docDef.initCfg.type.isXML() || field.docDef.initCfg.type.isJSON()) {
                serializedField["userCreated"] = field.userCreated;
            }
            if (field.isProperty()) {
                serializedField["jsonType"] = ConfigModel.mappingServicesPackagePrefix + ".PropertyField";
                serializedField["name"] = field.path;
            }
            else if (field.isConstant()) {
                serializedField["jsonType"] = ConfigModel.mappingServicesPackagePrefix + ".ConstantField";
                delete(serializedField["name"]);
            } else {
                delete(serializedField["value"]);
            }

            var includeIndexes: boolean = fieldPair.transition.isSeparateMode() && !isSource;
            includeIndexes = includeIndexes || (fieldPair.transition.isCombineMode() && isSource);
            if (includeIndexes) {
                var index: string = mappedField.getSeparateOrCombineIndex();
                index = (index == null) ? "1" : index;
                serializedField["index"] = (parseInt(index) - 1);
            }

            if (mappedField.actions.length) {
                var actions: any[] = [];
                for (let action of mappedField.actions) {
                    if (action.isSeparateOrCombineMode) {
                        continue;
                    }

                    var actionArguments: any = {};
                    for (let argValue of action.argumentValues) {
                        actionArguments[argValue.name] = argValue.value;
                        var argumentConfig: FieldActionArgument = action.config.getArgumentForName(argValue.name);
                        if (argumentConfig == null) {
                            console.error("Cannot find action argument with name: " + argValue.name, action);
                            continue;
                        }
                        if (argumentConfig.type == "INTEGER") {
                            actionArguments[argValue.name] = parseInt(argValue.value);
                        }
                    }

                    actionArguments = (Object.keys(actionArguments).length == 0) ? null : actionArguments;

                    var actionJson: any = {};
                    actionJson[action.config.name] = actionArguments;
                    actions.push(actionJson);
                }
                if (actions.length > 0) {
                    serializedField["actions"] = actions;
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
        mappingDefinition.mappings = mappingDefinition.mappings.concat(MappingSerializer.deserializeMappings(json));
        for (let lookupTable of MappingSerializer.deserializeLookupTables(json)) {
            mappingDefinition.addTable(lookupTable);
        }
        for (let field of MappingSerializer.deserializeProperties(json)) {
            cfg.propertyDoc.addField(field);
        }
    }

    public static deserializeDocs(json: any, mappingDefinition: MappingDefinition): DocumentDefinition[] {
        var docs: DocumentDefinition[] = [];
        for (let docRef of json.AtlasMapping.dataSource) {
            var doc: DocumentDefinition = new DocumentDefinition();
            doc.isSource = (docRef.dataSourceType == "SOURCE");
            doc.initCfg.documentIdentifier = docRef.uri;
            doc.initCfg.shortIdentifier = docRef.id;
            if (docRef.xmlNamespaces && docRef.xmlNamespaces.xmlNamespace) {
                for (let svcNS of docRef.xmlNamespaces.xmlNamespace) {
                    var ns: NamespaceModel = new NamespaceModel();
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

    public static deserializeMappings(json: any): MappingModel[] {
        var mappings: MappingModel[] = [];
        var docRefs: any = {};
        for (let docRef of json.AtlasMapping.dataSource) {
            docRefs[docRef.id] = docRef.uri;
        }
        for (let fieldMapping of json.AtlasMapping.mappings.mapping) {
            var m: MappingModel = new MappingModel();
            m.fieldMappings = [];

            var isCollectionMapping = (fieldMapping.jsonType == ConfigModel.mappingServicesPackagePrefix + ".Collection");
            if (isCollectionMapping) {
                for (let innerFieldMapping of fieldMapping.mappings.mapping) {
                    m.fieldMappings.push(MappingSerializer.deserializeFieldMapping(innerFieldMapping, docRefs));
                }
            } else {
                m.fieldMappings.push(MappingSerializer.deserializeFieldMapping(fieldMapping, docRefs));
            }

            mappings.push(m);
        }
        return mappings;
    }

    public static deserializeFieldMapping(fieldMapping: any, docRefs: any): FieldMappingPair {
        var fieldPair: FieldMappingPair = new FieldMappingPair();
        fieldPair.sourceFields = [];
        fieldPair.targetFields = [];

        var isSeparateMapping = (fieldMapping.mappingType == "SEPARATE");
        var isLookupMapping = (fieldMapping.mappingType == "LOOKUP");
        var isCombineMapping = (fieldMapping.mappingType == "COMBINE");
        for (let field of fieldMapping.inputField) {
            MappingSerializer.addFieldIfDoesntExist(fieldPair, field, true, docRefs);
        }
        for (let field of fieldMapping.outputField) {
            MappingSerializer.addFieldIfDoesntExist(fieldPair, field, false, docRefs);
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
        var fields: Field[] = [];
        if (!jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.properties
            || !jsonMapping.AtlasMapping.properties.property) {
            return fields;
        }
        for (let f of jsonMapping.AtlasMapping.properties.property) {
            var field: Field = new Field();
            field.name = f.name;
            field.value = f.value;
            field.type = f.fieldType;
            fields.push(field);
        }
        return fields;
    }

    public static deserializeLookupTables(jsonMapping: any): LookupTable[] {
        var tables: LookupTable[] = [];
        if (!jsonMapping.AtlasMapping || !jsonMapping.AtlasMapping.lookupTables
            || !jsonMapping.AtlasMapping.lookupTables.lookupTable) {
            return tables;
        }
        for (let t of jsonMapping.AtlasMapping.lookupTables.lookupTable) {
            var table: LookupTable = new LookupTable();
            table.name = t.name;
            for (let entry of t.lookupEntry) {
                var parsedEntry: LookupTableEntry = new LookupTableEntry();
                parsedEntry.sourceValue = entry.sourceValue;
                parsedEntry.sourceType = entry.sourceType;
                parsedEntry.targetValue = entry.targetValue;
                parsedEntry.targetType = entry.targetType;
                table.entries.push(parsedEntry);
            }
            tables.push(table);
        }
        return tables;
    }

    private static addFieldIfDoesntExist(fieldPair: FieldMappingPair, field: any,
        isSource: boolean, docRefs: any): MappedField {
        var mappedField: MappedField = new MappedField();

        mappedField.parsedData.parsedValueType = field.fieldType;
        mappedField.parsedData.parsedIndex = "1";
        if (field.index != null) {
            mappedField.parsedData.parsedIndex = (field.index + 1).toString();
        }
        if (field.jsonType == (ConfigModel.mappingServicesPackagePrefix + ".PropertyField")) {
            mappedField.parsedData.parsedName = field.name;
            mappedField.parsedData.parsedPath = field.name;
            mappedField.parsedData.fieldIsProperty = true;
        } else if (field.jsonType == (ConfigModel.mappingServicesPackagePrefix + ".ConstantField")) {
            mappedField.parsedData.fieldIsConstant = true;
            mappedField.parsedData.parsedValue = field.value;
            mappedField.parsedData.parsedPath = field.path;
        } else {
            if (field.docId == null) {
                console.error("Parsed mapping field does not have document id, dropping.", field);
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
                console.error("Could not find document URI for parsed mapped field.",
                    { "fieldJSON": field, "knownDocs": docRefs });
                return null;
            }
            if (field.actions) {
                for (let action of field.actions) {
                    for (let actionName in action) {
                        var parsedAction: FieldAction = new FieldAction();
                        parsedAction.name = actionName;
                        var actionParams: any = action[actionName];
                        if (actionParams) {
                            for (let paramName in actionParams) {
                                var parsedArgumentValue: FieldActionArgumentValue = new FieldActionArgumentValue();
                                parsedArgumentValue.name = paramName;
                                var value = actionParams[paramName];
                                value = value == null ? null : value.toString()
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
