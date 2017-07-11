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

import { MappingModel, MappedField, FieldMappingPair } from './mapping.model';
import { LookupTable } from '../models/lookup.table.model';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { TransitionModel, TransitionMode, FieldAction, FieldActionConfig } from './transition.model';
import { DocumentDefinition } from '../models/document.definition.model';

import { DataMapperUtil } from '../common/data.mapper.util';

export class MappingDefinition {
    public name: string = null;
    public mappings: MappingModel[] = [];
    public activeMapping: MappingModel = null;
    public parsedDocs: DocumentDefinition[] = [];
    public templateText: string = null;

    private tables: LookupTable[] = [];
    private tablesBySourceTargetKey: { [key:string]:LookupTable; } = {};
    private tablesByName: { [key:string]:LookupTable; } = {};

    public constructor() {
        this.name = "UI." + Math.floor((Math.random() * 1000000) + 1).toString();
    }

    public templateExists(): boolean {
        return ((this.templateText != null) && (this.templateText != ""));
    }

    public addTable(table: LookupTable): void {
        this.tablesBySourceTargetKey[table.getInputOutputKey()] = table;
        this.tablesByName[table.name] = table;
        this.tables.push(table);
    }

    public getTableByName(name: string): LookupTable {
        return this.tablesByName[name];
    }

    public detectTableIdentifiers() {
        for (let t of this.getTables()) {
            if (t.sourceIdentifier && t.targetIdentifier) {
                continue;
            }
            var tableChanged: boolean = false;
            var m: MappingModel = this.getFirstMappingForLookupTable(t.name);
            if (m != null) {
                for (let fieldPair of m.fieldMappings) {
                    if (fieldPair.transition.lookupTableName == null) {
                        continue;
                    }
                    if (!t.sourceIdentifier) {
                        var inputField: Field = fieldPair.getFields(true)[0];
                        if (inputField) {
                            t.sourceIdentifier = inputField.classIdentifier;
                            tableChanged = true;
                        }
                    }
                    if (!t.targetIdentifier) {
                        var outputField: Field = fieldPair.getFields(false)[0];
                        if (outputField) {
                            t.targetIdentifier = outputField.classIdentifier;
                            tableChanged = true;
                        }
                    }
                }
            }
            if (tableChanged) {
                console.log("Detected lookup table source/target id: " + t.toString());
            }
        }
        for (let m of this.mappings) {
            this.initializeMappingLookupTable(m);
        }
    }

    public getTableBySourceTarget(sourceIdentifier:string, targetIdentifier:string): LookupTable {
        var key: string = sourceIdentifier + ":" + targetIdentifier;
        return this.tablesBySourceTargetKey[key];
    }

    public getTables(): LookupTable[] {
        var tables: LookupTable[] = [];
        for (let key in this.tablesByName) {
            var table: LookupTable = this.tablesByName[key];
            tables.push(table);
        }
        return tables;
    }

    public getFirstMappingForLookupTable(lookupTableName: string): MappingModel {
        for (let m of this.mappings) {
            for (let fieldPair of m.fieldMappings) {
                if (fieldPair.transition.lookupTableName == lookupTableName) {
                    return m;
                }
            }
        }
        return null;
    }

    public removeStaleMappings(cfg: ConfigModel): void {
        console.log("Removing stale mappings. Current Mappings: " + this.mappings.length + ".", this.mappings);
        var index = 0;
        var sourceFieldPaths: string[] = [];
        for (let doc of cfg.getDocs(true)) {
            sourceFieldPaths = sourceFieldPaths.concat(Field.getFieldPaths(doc.getAllFields()));
        }
        var targetSourcePaths: string[] = [];
        for (let doc of cfg.getDocs(false)) {
            targetSourcePaths = targetSourcePaths.concat(Field.getFieldPaths(doc.getAllFields()));
        }
        while (index < this.mappings.length) {
            var mapping: MappingModel = this.mappings[index];
            console.log("Checking if mapping is stale: " + mapping.uuid, mapping);
            var mappingIsStale: boolean = this.isMappingStale(mapping, sourceFieldPaths, targetSourcePaths);
            console.log("stale:" + mappingIsStale);
            if (mappingIsStale) {
                console.log("Removing stale mapping.", { "mapping": mapping,
                    "sourceDocs": cfg.sourceDocs, "targetDocs": cfg.targetDocs });
                this.mappings.splice(index, 1);
            } else {
                index++;
            }
        }
        console.log("Finished removing stale mappings.");
    }

    public isMappingStale(mapping: MappingModel, sourceFieldPaths: string[], targetSourcePaths: string[]): boolean {
        for (var field of mapping.getFields(true)) {
            if (sourceFieldPaths.indexOf(field.path) == -1) {
                return true;
            }
        }
        for (var field of mapping.getFields(false)) {
            if (targetSourcePaths.indexOf(field.path) == -1) {
                return true;
            }
        }
        return false;
    }

    public initializeMappingLookupTable(m: MappingModel): void {
        console.log("Checking mapping for lookup table initialization: " + m.toString());
        for (let fieldPair of m.fieldMappings) {
            if (!(fieldPair.transition.mode == TransitionMode.ENUM
                && fieldPair.transition.lookupTableName == null
                && fieldPair.getFields(true).length == 1
                && fieldPair.getFields(false).length == 1)) {
                    console.log("Not looking for lookuptable for mapping field pair.", fieldPair);
                return;
            }
            console.log("Looking for lookup table for field pair.", fieldPair);
            var inputClassIdentifier: string = null;
            var outputClassIdentifier: string = null;

            var inputField: Field = fieldPair.getFields(true)[0];
            if (inputField) {
                inputClassIdentifier = inputField.classIdentifier;
            }
            var outputField: Field = fieldPair.getFields(true)[0];
            if (outputField) {
                outputClassIdentifier = outputField.classIdentifier;
            }
            if (inputClassIdentifier && outputClassIdentifier) {
                var table: LookupTable = this.getTableBySourceTarget(inputClassIdentifier, outputClassIdentifier);
                if (table == null) {
                    table = new LookupTable();
                    table.sourceIdentifier = inputClassIdentifier;
                    table.targetIdentifier = outputClassIdentifier;
                    this.addTable(table);
                    fieldPair.transition.lookupTableName = table.name;
                    console.log("Created lookup table for mapping.", m);
                } else {
                    fieldPair.transition.lookupTableName = table.name;
                    console.log("Initialized lookup table for mapping.", m)
                }
            }
        }
    }

    public updateMappingsFromDocuments(cfg: ConfigModel): void {
        console.log("Updating mapping fields from documents.");
        var sourceDocMap: any = {};
        for (let doc of cfg.getDocs(true)) {
            sourceDocMap[doc.uri] = doc;
        } 
        var targetDocMap: any = {};
        for (let doc of cfg.getDocs(false)) {
            targetDocMap[doc.uri] = doc;
        } 
        for (let mapping of this.mappings) {
            for (let fieldPair of mapping.fieldMappings) {
                this.updateMappedFieldsFromDocuments(fieldPair, cfg, sourceDocMap, true);
                this.updateMappedFieldsFromDocuments(fieldPair, cfg, targetDocMap, false);                        
            }
        }
        for (let doc of cfg.getAllDocs()) {
            if (doc.initCfg.shortIdentifier == null) {
                doc.initCfg.shortIdentifier = "DOC." + doc.name + "." + Math.floor((Math.random() * 1000000) + 1).toString();
            }
        }        
    }

    public updateDocumentNamespacesFromMappings(cfg: ConfigModel): void {
        console.log("Updating document namespaces from mappings");
        var docs: DocumentDefinition[] = cfg.getDocs(false);
        for (let parsedDoc of this.parsedDocs) {
            if (parsedDoc.isSource) {
                console.log("Skipping doc namespace update, we do not support source doc namespace override.", parsedDoc);
                continue;
            }
            if (parsedDoc.namespaces.length == 0) {
                console.log("Skipping doc namespace update, no namespaces from mappings were parsed.", parsedDoc);
                continue;
            }

            var doc = DocumentDefinition.getDocumentByIdentifier(parsedDoc.initCfg.documentIdentifier, docs);
            if (doc == null) {
                console.error("Could not find document with identifier '" + parsedDoc.initCfg.documentIdentifier 
                    + "' for namespace override.", 
                    { "identifier": parsedDoc.initCfg.documentIdentifier, "parsedDoc": parsedDoc, "docs": docs });
                continue;
            }

            console.log("Updating doc's namespaces.", { "doc": doc, 
                "oldNamespaces": doc.namespaces, "newNamespaces": parsedDoc.namespaces });
            doc.namespaces = [].concat(parsedDoc.namespaces);
        }
    }

    private updateMappedFieldsFromDocuments(fieldPair: FieldMappingPair, cfg: ConfigModel, docMap: any, isSource: boolean): void {
        var mappedFields: MappedField[] = fieldPair.getMappedFields(isSource);
        for (let mappedField of mappedFields) {        
            var doc: DocumentDefinition = null;
            if (mappedField.parsedData.fieldIsProperty) {
                doc = cfg.propertyDoc;
            } else if (mappedField.parsedData.fieldIsConstant) {
                doc = cfg.constantDoc;
            } else {
                doc = docMap[mappedField.parsedData.parsedDocURI] as DocumentDefinition;
                if (doc == null) {
                    console.error("Could not find doc for mapped field.", mappedField);
                    continue;
                }

                if (mappedField.parsedData.parsedDocID == null) {
                    console.error("Could not find doc ID for mapped field.", mappedField);
                    continue;
                }
                doc.initCfg.shortIdentifier = mappedField.parsedData.parsedDocID;
            }
            mappedField.field = null;
            if (!mappedField.parsedData.userCreated) {   
                mappedField.field = doc.getField(mappedField.parsedData.parsedPath);
            }
            if (mappedField.field == null) {
                if (mappedField.parsedData.fieldIsConstant) {
                    var constantField: Field = new Field();
                    constantField.value = mappedField.parsedData.parsedValue;
                    constantField.type = mappedField.parsedData.parsedValueType;
                    constantField.displayName = constantField.value;
                    constantField.name = constantField.value;
                    constantField.path = constantField.value;
                    mappedField.field = constantField;
                    doc.addField(constantField);
                } else if (mappedField.parsedData.userCreated) {
                    var path: string = mappedField.parsedData.parsedPath;
                    
                    mappedField.field = new Field();
                    mappedField.field.serviceObject.jsonType = "io.atlasmap.xml.v2.XmlField";        
                    mappedField.field.path = path;
                    mappedField.field.type = mappedField.parsedData.parsedValueType;
                    mappedField.field.userCreated = true;
                    
                    var lastSeparator: number = path.lastIndexOf("/");

                    var parentPath: string = (lastSeparator > 0) ? path.substring(0, lastSeparator) : null;
                    var fieldName: string = (lastSeparator == -1) ? path : path.substring(lastSeparator + 1);
                    var namespaceAlias: string = null;
                    if (fieldName.indexOf(":") != -1) {
                        namespaceAlias = fieldName.split(":")[0];
                        fieldName = fieldName.split(":")[1];
                    }

                    mappedField.field.name = fieldName;                    
                    mappedField.field.displayName = fieldName;                    
                    mappedField.field.isAttribute = (fieldName.indexOf("@") != -1);
                    mappedField.field.namespaceAlias = namespaceAlias;
                    
                    if (parentPath != null) {
                        mappedField.field.parentField = doc.getField(parentPath);
                    }
                    if (mappedField.field.parentField == null) {
                        mappedField.field.parentField = DocumentDefinition.getNoneField();
                    }

                    doc.addField(mappedField.field);
                } else {
                    console.error("Could not find field from doc for mapped field.", 
                        { "mappedField": mappedField, "doc": doc });
                    mappedField.field = DocumentDefinition.getNoneField();
                    return;
                }
            }
            if (mappedField.parsedData.parsedActions.length > 0) {
                for (let action of mappedField.parsedData.parsedActions) {
                    var actionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(action.name);
                    if (actionConfig == null) {
                        console.error("Could not find field action config for action name '" + action.name + "'");
                        continue;
                    }
                    actionConfig.populateFieldAction(action);
                    mappedField.actions.push(action);
                }
            }
            
            var isSeparate: boolean = fieldPair.transition.isSeparateMode();
            var isCombine: boolean = fieldPair.transition.isCombineMode();            
            var index: string = mappedField.parsedData.parsedIndex;
            mappedField.updateSeparateOrCombineIndex(isSeparate, isCombine, index, isSource); 
        }
    }

    public getAllMappings(includeActiveMapping: boolean): MappingModel[] {
        var mappings: MappingModel[] = [].concat(this.mappings);
        if (includeActiveMapping) {
            if (this.activeMapping == null) {
                return mappings;
            }
            for (let mapping of mappings) {
                if (mapping == this.activeMapping) {
                    return mappings;
                }
            }
            mappings.push(this.activeMapping);
        }
        return mappings;
    }

    public findMappingsForField(field: Field): MappingModel[] {
        var mappingsForField: MappingModel[] = [];
        for (let m of this.mappings) {
            if (m.isFieldMapped(field, field.isSource())) {
                mappingsForField.push(m);            
            }
        }
        return mappingsForField;
    }

    public removeMapping(m: MappingModel): boolean {
        return DataMapperUtil.removeItemFromArray(m, this.mappings);
    }

    public removeFieldFromAllMappings(field: Field): void {
        for (let mapping of this.getAllMappings(true)) {
            for (let fieldPair of mapping.fieldMappings) {
                var mappedField: MappedField = fieldPair.getMappedFieldForField(field, field.isSource());
                if (mappedField != null) {
                    mappedField.field = DocumentDefinition.getNoneField();
                }
            }
        }
    }
}
