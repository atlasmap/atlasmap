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
            DataMapperUtil.debugLogJSON(requestBody, "Classpath Service Request", this.cfg.initCfg.debugClassPathServiceCalls, url);
            this.http.post(url, requestBody, { headers: this.headers }).toPromise()
                .then((res: Response) => {
                    let body: any = res.json();
                    DataMapperUtil.debugLogJSON(body, "Classpath Service Response", this.cfg.initCfg.debugClassPathServiceCalls, url);
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

            if (docDef.initCfg.inspectionResultContents != null) {
                console.log("Document's inspection response is already specified, parsing: " + docDef.name);
                var responseJson: any = JSON.parse(docDef.initCfg.inspectionResultContents);
                this.parseDocumentResponse(responseJson, docDef);
                console.log("Finished parsing document '" + docDef.name + "' in "
                        + (Date.now() - startTime) + "ms.");
                observer.next(docDef);
                observer.complete();
                return;
            }

            var payload: any = this.createDocumentFetchRequest(docDef, classPath);
            var url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + "class";
            if (docDef.initCfg.type.isXML()) {
                url = this.cfg.initCfg.baseXMLInspectionServiceUrl + "inspect";
            }
            if (docDef.initCfg.type.isJSON()) {
                url = this.cfg.initCfg.baseJSONInspectionServiceUrl + "inspect";
            }
            DataMapperUtil.debugLogJSON(payload, "Document Service Request", this.cfg.initCfg.debugDocumentServiceCalls, url);
            this.http.post(url, payload, { headers: this.headers }).toPromise()
                .then((res: Response) => {
                    var responseJson: any = res.json();
                    DataMapperUtil.debugLogJSON(responseJson, "Document Service Response", this.cfg.initCfg.debugDocumentServiceCalls, url);
                    this.parseDocumentResponse(responseJson, docDef);
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
                    "type": docDef.initCfg.inspectionType,
                    "xmlData": docDef.initCfg.documentContents
                }
            };
        }
        if (docDef.initCfg.type.isJSON()) {
            return {
                "JsonInspectionRequest": {
                    "jsonType": "io.atlasmap.json.v2.JsonInspectionRequest",
                    "type": docDef.initCfg.inspectionType,
                    "jsonData": docDef.initCfg.documentContents
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

    private parseDocumentResponse(responseJson: any, docDef: DocumentDefinition): void {
        docDef.name = docDef.initCfg.shortIdentifier;
        docDef.fullyQualifiedName = docDef.initCfg.shortIdentifier;
        if (docDef.initCfg.type.isJava()) {
            if (typeof responseJson.ClassInspectionResponse != "undefined") {
                this.extractJavaDocumentDefinitionFromInspectionResponse(responseJson, docDef);
            } else if ((typeof responseJson.javaClass != "undefined")
                    || (typeof responseJson.JavaClass != "undefined")) {
                this.extractJavaDocumentDefinition(responseJson, docDef);
            } else {
                this.handleError("Unknown Java inspection result format", responseJson);
            }
        } else if (docDef.initCfg.type.isJSON()) {
            if (typeof responseJson.JsonInspectionResponse != "undefined") {
                this.extractJSONDocumentDefinitionFromInspectionResponse(responseJson, docDef);
            } else if ((typeof responseJson.jsonDocument != "undefined")
                    || (typeof responseJson.JsonDocument != "undefined")) {
                this.extractJSONDocumentDefinition(responseJson, docDef);
            } else {
                this.handleError("Unknown JSON inspection result format", responseJson);
            }
        } else {
            if (typeof responseJson.XmlInspectionResponse != "undefined") {
                this.extractXMLDocumentDefinitionFromInspectionResponse(responseJson, docDef);
            } else if ((typeof responseJson.xmlDocument != "undefined")
                    || (typeof responseJson.XmlDocument != "undefined")) {
                this.extractXMLDocumentDefinition(responseJson, docDef);
            } else {
                this.handleError("Unknown XML inspection result format", responseJson);
            }
        }
        docDef.initializeFromFields();
    }

    private extractJSONDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
        var body: any = responseJson.JsonInspectionResponse;
        if (body.errorMessage) {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load JSON document, error: " + body.errorMessage, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }

        this.extractJSONDocumentDefinition(body, docDef);
    }

    private extractJSONDocumentDefinition(body: any, docDef: DocumentDefinition): void {
        var jsonDocument: any;
        if (typeof body.jsonDocument != "undefined") {
            jsonDocument = body.jsonDocument;
        } else {
            jsonDocument = body.JsonDocument;
        }
        
        docDef.characterEncoding = jsonDocument.characterEncoding;
        docDef.locale = jsonDocument.locale;

        for (let field of jsonDocument.fields.field) {
            this.parseJSONFieldFromDocument(field, null, docDef);
        }
    }

    private extractXMLDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
        var body: any = responseJson.XmlInspectionResponse;
        if (body.errorMessage) {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load XML document, error: " + body.errorMessage, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }

        this.extractXMLDocumentDefinition(body, docDef);
    }

    private extractXMLDocumentDefinition(body: any, docDef: DocumentDefinition): void {
        var xmlDocument: any;
        if (typeof body.xmlDocument != "undefined") {
            xmlDocument = body.xmlDocument;
        } else {
            xmlDocument = body.XmlDocument;
        }
        docDef.characterEncoding = xmlDocument.characterEncoding;
        docDef.locale = xmlDocument.locale;


        if (xmlDocument.xmlNamespaces && xmlDocument.xmlNamespaces.xmlNamespace
            && xmlDocument.xmlNamespaces.xmlNamespace.length) {
            for (let serviceNS of xmlDocument.xmlNamespaces.xmlNamespace) {
                var ns: NamespaceModel = new NamespaceModel();
                ns.alias = serviceNS.alias;
                ns.uri = serviceNS.uri;
                ns.locationUri = serviceNS.locationUri;
                ns.isTarget = serviceNS.targetNamespace;
                docDef.namespaces.push(ns);
            }
        }

        for (let field of xmlDocument.fields.field) {
            this.parseXMLFieldFromDocument(field, null, docDef);
        }
    }

    private extractJavaDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
        var body: any = responseJson.ClassInspectionResponse;

        if (body.errorMessage) {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load Java document, error: " + body.errorMessage, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }
        this.extractJavaDocumentDefinition(body, docDef);
    }

    private extractJavaDocumentDefinition(body: any, docDef: DocumentDefinition): void {
        var javaClass: any;
        if (typeof body.javaClass != "undefined") {
            javaClass = body.javaClass;
        } else {
            javaClass = body.JavaClass;
        }
        
        if (javaClass.status == "NOT_FOUND") {
            var docIdentifier: string = docDef.initCfg.documentIdentifier;
            this.handleError("Could not load JAVA document. Document is not found: " + docIdentifier, null);
            docDef.initCfg.errorOccurred = true;
            return;
        }

        docDef.name = javaClass.className;
        //Make doc name the class name rather than fully qualified name
        if (docDef.name && docDef.name.indexOf(".") != -1) {
            docDef.name = docDef.name.substr(docDef.name.lastIndexOf(".") + 1);
        }

        docDef.fullyQualifiedName = javaClass.className;
        if (docDef.name == null) {
            console.error("Document's className is empty.", javaClass);
        }
        docDef.uri = javaClass.uri;

        docDef.characterEncoding = javaClass.characterEncoding;
        docDef.locale = javaClass.locale;

        for (let field of javaClass.javaFields.javaField) {
            this.parseJavaFieldFromDocument(field, null, docDef);
        }
    }

    private parseJSONFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): void {
        var parsedField = this.parseFieldFromDocument(field, parentField, docDef);
        if (parsedField == null) {
            return;
        }

        if (field.jsonFields && field.jsonFields.jsonField && field.jsonFields.jsonField.length) {
            for (let childField of field.jsonFields.jsonField) {
                this.parseJSONFieldFromDocument(childField, parsedField, docDef);
            }
        }
    }

    private parseFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): Field {
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
        parsedField.path = field.path;
        parsedField.isPrimitive = field.fieldType != "COMPLEX";
        parsedField.serviceObject = field;

        if ("LIST" == field.collectionType || "ARRAY" == field.collectionType) {
            parsedField.isCollection = true;
            if ("ARRAY" == field.collectionType) {
                parsedField.isArray = true;
                parsedField.type = "ARRAY[" + parsedField.type + "]";
            } else {
                parsedField.type = "LIST<" + parsedField.type + ">";
            }
        }

        if (parentField != null) {
            parentField.children.push(parsedField);
        } else {
            docDef.fields.push(parsedField);
        }

        return parsedField;
    }

    private parseXMLFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): void {
        var parsedField = this.parseFieldFromDocument(field, parentField, docDef);
        if (parsedField == null) {
            return;
        }

        if (field.name.indexOf(":") != -1) {
            parsedField.namespaceAlias = field.name.split(":")[0];
            parsedField.name = field.name.split(":")[1];
        }

        parsedField.isAttribute = (parsedField.path.indexOf("@") != -1);

        if (field.xmlFields && field.xmlFields.xmlField && field.xmlFields.xmlField.length) {
            for (let childField of field.xmlFields.xmlField) {
                this.parseXMLFieldFromDocument(childField, parsedField, docDef);
            }
        }
    }

    private parseJavaFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): void {
        var parsedField = this.parseFieldFromDocument(field, parentField, docDef);
        if (parsedField == null) {
            return;
        }

        //java fields have a special primitive property, so override the "!= COMPLEX" math from parseFieldFromDocument()
        parsedField.isPrimitive = field.primitive;
        parsedField.classIdentifier = field.className;
        parsedField.enumeration = field.enumeration;

        if (parsedField.enumeration && field.javaEnumFields && field.javaEnumFields.javaEnumField) {
            for (let enumValue of field.javaEnumFields.javaEnumField) {
                var parsedEnumValue: EnumValue = new EnumValue();
                parsedEnumValue.name = enumValue.name;
                parsedEnumValue.ordinal = enumValue.ordinal;
                parsedField.enumValues.push(parsedEnumValue);
            }
            if (this.cfg.initCfg.debugDocumentParsing) {
                console.log("parsed enums for field " + parsedField.classIdentifier, parsedField);
            }
        }

        if (field.javaFields && field.javaFields.javaField && field.javaFields.javaField.length) {
            for (let childField of field.javaFields.javaField) {
                this.parseJavaFieldFromDocument(childField, parsedField, docDef);
            }
        }
    }

    public static generateMockInstanceXMLDoc(): string {
        // here we have a bunch of examples we can use.
        var mockDoc: string = `<data>
                <intField a='1'>32000</intField><longField>12421</longField>
                <stringField>abc</stringField><booleanField>true</booleanField>
                <doubleField b='2'>12.0</doubleField><shortField>1000</shortField>
                <floatField>234.5f</floatField><charField>A</charField>
                <outer><inner><value>val</value></inner></outer>
            </data>
        `;

        mockDoc = `<?xml version="1.0" encoding="UTF-8" ?>
            <foo>bar</foo>
        `;

        mockDoc = "<foo>bar</foo>";

        mockDoc = `
            <XMLOrder>
            <orderId>orderId</orderId>
            <Address>
                <addressLine1>addressLine1</addressLine1>
                <addressLine2>addressLine2</addressLine2>
                <city>city</city>
                <state>state</state>
                <zipCode>zipCode</zipCode>
            </Address>
            <Contact>
                <firstName>firstName</firstName>
                <lastName>lastName</lastName>
                <phoneNumber>phoneNumber</phoneNumber>
                <zipCode>zipCode</zipCode>
            </Contact>
            </XMLOrder>
        `;

        mockDoc = `
            <foo><bar><jason>somevalue</jason></bar></foo>
        `;

        mockDoc = `
            <orders totalCost="12525.00" xmlns="http://www.example.com/x/"
                xmlns:y="http://www.example.com/y/"
                xmlns:q="http://www.example.com/q/">
                <order>
                <id y:custId="a">12312</id>
                    <id y:custId="b">4423423</id>
                    </order>
                <q:order><id y:custId="x">12312</id></q:order>
                <order><id y:custId="c">54554555</id></order>
                <q:order><id y:custId="a">12312</id></q:order>
            </orders>
        `;

        mockDoc = `
            <ns:XmlFPE targetNamespace="http://atlasmap.io/xml/test/v2"
                xmlns:ns="http://atlasmap.io/xml/test/v2"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://atlasmap.io/xml/test/v2 atlas-xml-test-model-v2.xsd ">
                <booleanField>false</booleanField>
                <byteField>99</byteField>
                <charField>a</charField>
                <doubleField>50000000.0</doubleField>
                <floatField>40000000.0</floatField>
                <intField>2</intField>
                <longField>30000</longField>
                <shortField>1</shortField>
            </ns:XmlFPE>
        `;

        mockDoc = `
            <ns:XmlOE xmlns:ns="http://atlasmap.io/xml/test/v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://atlasmap.io/xml/test/v2 atlas-xml-test-model-v2.xsd ">
            <ns:orderId>ns:orderId</ns:orderId>
            <ns:Address>
                <ns:addressLine1>ns:addressLine1</ns:addressLine1>
                <ns:addressLine2>ns:addressLine2</ns:addressLine2>
                <ns:city>ns:city</ns:city>
                <ns:state>ns:state</ns:state>
                <ns:zipCode>ns:zipCode</ns:zipCode>
            </ns:Address>
            <ns:Contact>
                <ns:firstName>ns:firstName</ns:firstName>
                <ns:lastName>ns:lastName</ns:lastName>
                <ns:phoneNumber>ns:phoneNumber</ns:phoneNumber>
                <ns:zipCode>ns:zipCode</ns:zipCode>
            </ns:Contact>
            </ns:XmlOE>
        `;

        return mockDoc;
    }

    public static generateMockSchemaXMLDoc(): string {
        var mockDoc: string = `
            <xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified"
                     xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="data">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element type="xs:short" name="intField"/>
                            <xs:element type="xs:short" name="longField"/>
                            <xs:element type="xs:string" name="stringField"/>
                            <xs:element type="xs:string" name="booleanField"/>
                            <xs:element type="xs:float" name="doubleField"/>
                            <xs:element type="xs:short" name="shortField"/>
                            <xs:element type="xs:string" name="floatField"/>
                            <xs:element type="xs:string" name="charField"/>
                        </xs:sequence>
                        <xs:attribute name="intAttr" type="xs:int" use="optional" default="1"/>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
        `;

        mockDoc = `
            <schema xmlns="http://www.w3.org/2001/XMLSchema" targetNamespace="http://example.com/"
                xmlns:tns="http://example.com/">
                <element name="aGlobalElement" type="tns:aGlobalType"/>
                <simpleType name="aGlobalType"><restriction base="string"/></simpleType>
            </schema>
        `;

        mockDoc = `
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="shiporder">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="orderperson" type="xs:string"/>
                            <xs:element name="shipto">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="name" type="xs:string"/>
                                        <xs:element name="address" type="xs:string"/>
                                        <xs:element name="city" type="xs:string"/>
                                        <xs:element name="country" type="xs:string"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                            <xs:element name="item" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="title" type="xs:string"/>
                                        <xs:element name="note" type="xs:string" minOccurs="0"/>
                                        <xs:element name="quantity" type="xs:positiveInteger"/>
                                        <xs:element name="price" type="xs:decimal"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                        <xs:attribute name="orderid" type="xs:string" use="required" fixed="2"/>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
        `;

        mockDoc = `
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                <xs:element name="shiporder">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="shipto">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="name" type="xs:string"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                            <xs:element name="item" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="title" type="xs:string"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:schema>
        `;

        return mockDoc;
    }

    public static generateMockJSONDoc(): string {
        return DocumentManagementService.generateMockJSONInstanceDoc();
    }

    public static generateMockJSONInstanceDoc(): string {
        var mockDoc: string = `   {
                "order": {
                    "address": {
                        "street": "123 any st",
                        "city": "Austin",
                        "state": "TX",
                        "zip": "78626"
                    },
                    "contact": {
                        "firstName": "james",
                        "lastName": "smith",
                        "phone": "512-123-1234"
                    },
                    "orderId": "123"
                },
                "primitives": {
                    "stringPrimitive": "some value",
                    "booleanPrimitive": true,
                    "numberPrimitive": 24
                },
                "addressList": [
                    { "street": "123 any st", "city": "Austin", "state": "TX", "zip": "78626" },
                    { "street": "123 any st", "city": "Austin", "state": "TX", "zip": "78626" },
                    { "street": "123 any st", "city": "Austin", "state": "TX", "zip": "78626" },
                    { "street": "123 any st", "city": "Austin", "state": "TX", "zip": "78626" }
                ]
            }
        `;

        return mockDoc;
    }

    public static generateMockJSONSchemaDoc(): string {
        var mockDoc: string = `
            {
                "$schema": "http://json-schema.org/schema#",
                "description": "Order",
                "type": "object",
                "properties": {
                    "order": {
                        "type": "object",
                        "properties": {
                            "address": {
                                "type": "object",
                                "properties": {
                                    "street": { "type": "string" },
                                    "city": { "type": "string" },
                                    "state": { "type": "string" },
                                    "zip": { "type": "string" }
                                }
                            },
                            "contact": {
                                "type": "object",
                                "properties": {
                                    "firstName": { "type": "string" },
                                    "lastName": { "type": "string" },
                                    "phone": { "type": "string" }
                                }
                            },
                            "orderId": { "type": "string" }
                        }
                    },
                    "primitives": {
                        "type": "object",
                        "properties": {
                            "stringPrimitive": { "type": "string" },
                            "booleanPrimitive": { "type": "boolean" },
                            "numberPrimitive": { "type": "number" }
                        }
                    },
                    "addressList": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "street": { "type": "string" },
                                "city": { "type": "string" },
                                "state": { "type": "string" },
                                "zip": { "type": "string" }
                            }
                        }
                    }
                }
            }
        `;

        return mockDoc;
    }

    public static generateMockJavaDoc(): string {
        var mockDoc: string = `
            {
              "JavaClass": {
                "jsonType": "io.atlasmap.java.v2.JavaClass",
                "modifiers": { "modifier": [ "PUBLIC" ] },
                "className": "io.atlasmap.java.test.Name",
                "primitive": false,
                "synthetic": false,
                "javaEnumFields": { "javaEnumField": [] },
                "javaFields": {
                  "javaField": [
                    {
                      "jsonType": "io.atlasmap.java.v2.JavaField",
                      "path": "firstName",
                      "status": "SUPPORTED",
                      "fieldType": "STRING",
                      "modifiers": { "modifier": [ "PRIVATE" ] },
                      "name": "firstName",
                      "className": "java.lang.String",
                      "getMethod": "getFirstName",
                      "setMethod": "setFirstName",
                      "primitive": true,
                      "synthetic": false
                    },
                    {
                      "jsonType": "io.atlasmap.java.v2.JavaField",
                      "path": "lastName",
                      "status": "SUPPORTED",
                      "fieldType": "STRING",
                      "modifiers": { "modifier": [ "PRIVATE" ] },
                      "name": "lastName",
                      "className": "java.lang.String",
                      "getMethod": "getLastName",
                      "setMethod": "setLastName",
                      "primitive": true,
                      "synthetic": false
                    }
                  ]
                },
                "packageName": "io.atlasmap.java.test",
                "annotation": false,
                "annonymous": false,
                "enumeration": false,
                "localClass": false,
                "memberClass": false,
                "uri": "atlas:java?className=io.atlasmap.java.test.Name",
                "interface": false
              }
            }
        `;
        return mockDoc;
    }

    private handleError(message:string, error: any): void {
        this.cfg.errorService.error(message, error);
    }
}
