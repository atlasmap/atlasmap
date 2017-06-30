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

import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions, Response, HttpModule } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/observable/forkJoin';
import { Subject } from 'rxjs/Subject';

import { ConfigModel } from '../models/config.model';
import { Field, EnumValue } from '../models/field.model';
import { DocumentDefinition, NamespaceModel } from '../models/document.definition.model';
import { ErrorHandlerService } from './error.handler.service';

import { DataMapperUtil } from '../common/data.mapper.util';

@Injectable()
export class DocumentManagementService {
    public cfg: ConfigModel;

    private headers: Headers = new Headers();

    constructor(private http: Http) {
        this.headers.append("Content-Type", "application/json");
    }

    public initialize(): void {
        this.cfg.mappingService.mappingUpdated$.subscribe(mappingDefinition => {
            for (var d of this.cfg.getAllDocs()) {
                if (d.initCfg.initialized) {
                    d.updateFromMappings(this.cfg.mappings, this.cfg);
                }
            }
        });
    }

    public fetchClassPath(): Observable<string> {
        return new Observable<string>((observer:any) => {
            var startTime: number = Date.now();
            var requestBody = {
                "MavenClasspathRequest": {
                    "jsonType": ConfigModel.javaServicesPackagePrefix + ".MavenClasspathRequest",
                    "pomXmlData": this.cfg.initCfg.pomPayload,
                    "executeTimeout": this.cfg.initCfg.classPathFetchTimeoutInMilliseconds
                }
            }
            var url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + "mavenclasspath";
            DataMapperUtil.debugLogJSON(requestBody, "Classpath Service Request", this.cfg.debugClassPathJSON, url);
            this.http.post(url, requestBody, { headers: this.headers }).toPromise()
                .then((res: Response) => {
                    let body: any = res.json();
                    DataMapperUtil.debugLogJSON(body, "Classpath Service Response", this.cfg.debugClassPathJSON, url);
                    var classPath: string = body.MavenClasspathResponse.classpath;
                    console.log("Finished fetching class path '" + classPath + "' in "
                        + (Date.now() - startTime) + "ms.");
                    observer.next(classPath);
                    observer.complete();
                })
                .catch((error: any) => {
                    observer.error(error);
                    observer.complete();
                }
            );
        });
    }

    public fetchDocument(docDef: DocumentDefinition, classPath:string): Observable<DocumentDefinition> {
        return new Observable<DocumentDefinition>((observer:any) => {
            var startTime: number = Date.now();
            var payload: any = this.createDocumentFetchRequest(docDef, classPath);
            var url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + "class";
            if (docDef.initCfg.type.isXML()) {
                url = this.cfg.initCfg.baseXMLInspectionServiceUrl + "inspect";                
            }
            DataMapperUtil.debugLogJSON(payload, "Document Service Request", this.cfg.debugDocumentJSON, url);
            this.http.post(url, payload, { headers: this.headers }).toPromise()
                .then((res: Response) => {
                    DataMapperUtil.debugLogJSON(res.json(), "Document Service Response", this.cfg.debugDocumentJSON, url);
                    if (docDef.initCfg.type.isJava()) {
                        this.extractJavaDocumentDefinitionData(res, docDef);
                    } else {
                        this.extractXMLDocumentDefinitionData(res, docDef);
                    }
                    console.log("Finished fetching and parsing document '" + docDef.name + "' in "
                        + (Date.now() - startTime) + "ms.");
                    observer.next(docDef);
                    observer.complete();
                })
                .catch((error: any) => {
                    observer.error(error);
                    docDef.initCfg.errorOccurred = true;
                    observer.next(docDef);
                    observer.complete();
                }
            );
        });
    }

    private createDocumentFetchRequest(docDef: DocumentDefinition, classPath:string): any {
        if (docDef.initCfg.type.isXML()) {
            return {
                "XmlInspectionRequest": {
                    "jsonType": "io.atlasmap.xml.v2.XmlInspectionRequest",
                    "type": docDef.initCfg.xmlInspectionType, 
                    "xmlData": docDef.initCfg.xmlData
                }
            };
        }
        var className: string = docDef.initCfg.documentIdentifier;
        var payload: any = {
            "ClassInspectionRequest": {
                "jsonType": ConfigModel.javaServicesPackagePrefix + ".ClassInspectionRequest",
                "classpath": classPath,
                "className": className,
                "disablePrivateOnlyFields": this.cfg.initCfg.disablePrivateOnlyFields,
                "disableProtectedOnlyFields": this.cfg.initCfg.disableProtectedOnlyFields,
                "disablePublicOnlyFields": this.cfg.initCfg.disablePublicOnlyFields,
                "disablePublicGetterSetterFields": this.cfg.initCfg.disablePublicGetterSetterFields
            }
        }
        if (this.cfg.initCfg.fieldNameBlacklist && this.cfg.initCfg.fieldNameBlacklist.length) {
            payload["ClassInspectionRequest"]["fieldNameBlacklist"] = { "string": this.cfg.initCfg.fieldNameBlacklist };
        }
        if (this.cfg.initCfg.classNameBlacklist && this.cfg.initCfg.classNameBlacklist.length) {
            payload["ClassInspectionRequest"]["classNameBlacklist"] = { "string": this.cfg.initCfg.classNameBlacklist };
        }
        return payload;
    }

    private extractXMLDocumentDefinitionData(res: Response, docDef: DocumentDefinition): void {
        var body: any = res.json()
        if (body.XmlInspectionResponse.errorMessage) {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load XML document. Document is not found: " 
                + body.XmlInspectionResponse.errorMessage, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }

        body = body.XmlInspectionResponse.xmlDocument;

        docDef.name = docDef.initCfg.shortIdentifier;
        docDef.fullyQualifiedName = docDef.initCfg.shortIdentifier;
        
        console.log("Loading XML document: " + docDef.name, body);        

        if (body.xmlNamespaces && body.xmlNamespaces.xmlNamespace 
            && body.xmlNamespaces.xmlNamespace.length) {
            for (let serviceNS of body.xmlNamespaces.xmlNamespace) {
                var ns: NamespaceModel = new NamespaceModel();
                ns.alias = serviceNS.alias;
                ns.uri = serviceNS.uri;
                ns.locationUri = serviceNS.locationUri;
                ns.isTarget = serviceNS.targetNamespace;
                docDef.namespaces.push(ns);
            }
        } else {
            var ns: NamespaceModel = new NamespaceModel();
            ns.alias = "tns";
            ns.isTarget = true;
            docDef.namespaces.push(ns);
        }

        var firstNS: NamespaceModel = docDef.namespaces[0];

        for (let field of body.fields.field) {
            var parsedField: Field = this.parseXMLFieldFromDocument(field, docDef, firstNS);
            if (parsedField != null) {
                docDef.fields.push(parsedField);
            }
        }

        docDef.populateFromFields();
        docDef.initCfg.initialized = true;
    }              

    private extractJavaDocumentDefinitionData(res: Response, docDef: DocumentDefinition): void {
        let body: any = res.json().ClassInspectionResponse;

        docDef.name = body.javaClass.className;
        //Make doc name the class name rather than fully qualified name
        if (docDef.name && docDef.name.indexOf(".") != -1) {
            docDef.name = docDef.name.substr(docDef.name.lastIndexOf(".") + 1);
        }

        docDef.fullyQualifiedName = body.javaClass.className;
        if (docDef.name == null) {
            console.error("Document's className is empty.", body.javaClass);
        }
        console.log("Loading JAVA document: " + docDef.name, body);
        docDef.uri = body.javaClass.uri;

        if (body.javaClass.status == "NOT_FOUND") {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load JAVA document. Document is not found: " + docIdentifier, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }

        for (let field of body.javaClass.javaFields.javaField) {
            var parsedField: Field = this.parseJavaFieldFromDocument(field, docDef);
            if (parsedField != null) {
                docDef.fields.push(parsedField);
            }
        }

        docDef.populateFromFields();
        docDef.initCfg.initialized = true;
    } 

    private parseXMLFieldFromDocument(field: any, docDef: DocumentDefinition, firstNS: NamespaceModel): Field {
        if (field != null && field.status == "NOT_FOUND") {
            console.error("Filtering missing field: " + field.name
                + " (" + field.className + "), parent class: " + docDef.name);
            return null;
        } else if (field != null && field.status == "BLACK_LIST") {
            console.log("Filtering black listed field: " + field.name
                + " (" + field.className + "), parent class: " + docDef.name);
            return null;
        }

        var parsedField: Field = new Field();
        parsedField.name = field.name;
        parsedField.path = field.path;
        parsedField.isAttribute = (parsedField.path.indexOf("@") != -1);
        parsedField.type = field.type;
        parsedField.namespace = firstNS;
        if (field.collectionType && ("LIST" == field.collectionType || "ARRAY" == field.collectionType)) {
            parsedField.isCollection = true;
            if ("ARRAY" == field.collectionType) {
                parsedField.isArray = true;
                parsedField.type = "ARRAY[" + parsedField.type + "]";
            } else {
                parsedField.type = "LIST<" + parsedField.type + ">";
            }
        }
        
        parsedField.isPrimitive = field.type != "COMPLEX";
        //TODO: parsedField.classIdentifier = field.className;
        parsedField.serviceObject = field;        

        if (field.xmlFields && field.xmlFields.xmlField && field.xmlFields.xmlField.length) {
            for (let childField of field.xmlFields.xmlField) {
                var parsedChild: Field = this.parseXMLFieldFromDocument(childField, docDef, firstNS);
                if (parsedChild != null) {
                    parsedField.children.push(parsedChild);
                }
            }
        }

        return parsedField;
    }                  

    private parseJavaFieldFromDocument(field: any, docDef: DocumentDefinition): Field {
        if (field != null && field.status == "NOT_FOUND") {
            console.error("Filtering missing field: " + field.name
                + " (" + field.className + "), parent class: " + docDef.name);
            return null;
        } else if (field != null && field.status == "BLACK_LIST") {
            console.log("Filtering black listed field: " + field.name
                + " (" + field.className + "), parent class: " + docDef.name);
            return null;
        }

        var parsedField: Field = new Field();
        parsedField.name = field.name;
        parsedField.type = field.fieldType;
        if ("LIST" == field.collectionType || "ARRAY" == field.collectionType) {
            parsedField.isCollection = true;
            if ("ARRAY" == field.collectionType) {
                parsedField.isArray = true;
                parsedField.type = "ARRAY[" + parsedField.type + "]";
            } else {
                parsedField.type = "LIST<" + parsedField.type + ">";
            }
        }
        
        parsedField.isPrimitive = field.primitive;
        parsedField.classIdentifier = field.className;
        parsedField.enumeration = field.enumeration;
        parsedField.serviceObject = field;

        if (parsedField.enumeration && field.javaEnumFields && field.javaEnumFields.javaEnumField) {
            for (let enumValue of field.javaEnumFields.javaEnumField) {
                var parsedEnumValue: EnumValue = new EnumValue();
                parsedEnumValue.name = enumValue.name;
                parsedEnumValue.ordinal = enumValue.ordinal;
                parsedField.enumValues.push(parsedEnumValue);
            }
            if (this.cfg.debugDocumentParsing) {
                console.log("parsed enums for field " + parsedField.classIdentifier, parsedField);
            }
        }

        if (field.javaFields && field.javaFields.javaField && field.javaFields.javaField.length) {
            for (let childField of field.javaFields.javaField) {
                var parsedChild: Field = this.parseJavaFieldFromDocument(childField, docDef);
                if (parsedChild != null) {
                    parsedField.children.push(parsedChild);
                }
            }
        }

        return parsedField;
    }

    private handleError(message:string, error: any): void {
        this.cfg.errorService.error(message, error);
    }
}
