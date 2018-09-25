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

import { Injectable, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';

import { DocumentType, InspectionType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { Field, EnumValue } from '../models/field.model';
import { DocumentDefinition, NamespaceModel } from '../models/document-definition.model';

import { DataMapperUtil } from '../common/data-mapper-util';
import { Subscription } from 'rxjs';

@Injectable()
export class DocumentManagementService implements OnDestroy {
  cfg: ConfigModel;

  private headers = new HttpHeaders({'Content-Type': 'application/json'});
  private mappingUpdatedSubscription: Subscription;

  static generateMockInstanceXMLDoc(): string {
    // here we have a bunch of examples we can use.
    let mockDoc = `<data>
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

    mockDoc = '<foo>bar</foo>';

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
            <ns:XmlOE xmlns:ns="http://atlasmap.io/xml/test/v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://atlasmap.io/xml/test/v2 atlas-xml-test-model-v2.xsd ">
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

  static generateMockSchemaXMLDoc(): string {
    let mockDoc = `
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

    mockDoc = `
        <d:SchemaSet xmlns:d="http://atlasmap.io/xml/schemaset/v2" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
          <xsd:schema targetNamespace="http://syndesis.io/v1/swagger-connector-template/request" elementFormDefault="qualified">
            <xsd:element name="request">
              <xsd:complexType>
                <xsd:sequence>
                  <xsd:element name="body">
                    <xsd:complexType>
                      <xsd:sequence>
                        <xsd:element ref="Pet" />
                      </xsd:sequence>
                    </xsd:complexType>
                  </xsd:element>
                </xsd:sequence>
              </xsd:complexType>
            </xsd:element>
          </xsd:schema>
          <d:AdditionalSchemas>
            <xsd:schema>
              <xsd:element name="Pet">
                <xsd:complexType>
                  <xsd:sequence>
                    <xsd:element name="id" type="xsd:decimal" />
                    <xsd:element name="Category">
                      <xsd:complexType>
                        <xsd:sequence>
                          <xsd:element name="id" type="xsd:decimal" />
                          <xsd:element name="name" type="xsd:string" />
                        </xsd:sequence>
                      </xsd:complexType>
                    </xsd:element>
                    <xsd:element name="name" type="xsd:string" />
                    <xsd:element name="photoUrl">
                      <xsd:complexType>
                        <xsd:sequence>
                          <xsd:element name="photoUrl" type="xsd:string" maxOccurs="unbounded" minOccurs="0" />
                        </xsd:sequence>
                      </xsd:complexType>
                    </xsd:element>
                    <xsd:element name="tag">
                      <xsd:complexType>
                        <xsd:sequence>
                          <xsd:element name="Tag" maxOccurs="unbounded" minOccurs="0">
                            <xsd:complexType>
                              <xsd:sequence>
                                <xsd:element name="id" type="xsd:decimal" />
                                <xsd:element name="name" type="xsd:string" />
                              </xsd:sequence>
                            </xsd:complexType>
                          </xsd:element>
                        </xsd:sequence>
                      </xsd:complexType>
                    </xsd:element>
                    <xsd:element name="status" type="xsd:string" />
                  </xsd:sequence>
                </xsd:complexType>
              </xsd:element>
            </xsd:schema>
          </d:AdditionalSchemas>
        </d:SchemaSet>
        `;

    return mockDoc;
  }

  /**
   * Use the JSON utility to translate the specified buffer into a JSON buffer - then replace any
   * non-ascii character encodings with unicode escape sequences.
   *
   * @param buffer
   */
  private static sanitizeJSON(buffer: string): string {
    let jsonBuffer = JSON.stringify(buffer);
    jsonBuffer = jsonBuffer.replace(/[\u007F-\uFFFF]/g, function(chr) {
      return '\\u' + ('0000' + chr.charCodeAt(0).toString(16)).substr(-4);
    });
    return jsonBuffer;
  }

  /**
   * Restrict JSON parsing to the document management service.
   *
   * @param buffer
   */
  static getMappingsInfo(buffer: string): any {
    return JSON.parse(buffer);
  }

  /**
   * Capture the specified user mappings into a general catalog JSON buffer (exportMappings).
   * @param buffer
   */
  static generateExportMappings(buffer: string): string {

    if (buffer === null || buffer.length === 0) {
      return '';
    }
    const metaStr = `   "exportMappings":
    {
       \"value\": ` + this.sanitizeJSON(buffer) + `
    },\n`;

    return metaStr;
  }

  /**
   * Capture the specified user JSON or XML document buffer into a general catalog JSON buffer.
   *
   * @param buffer
   */
  static generateExportBlockData(buffer: string): string {

    if (buffer === null || buffer.length === 0) {
        return '';
    }
    const metaStr = `
          {
             \"value\": ` + this.sanitizeJSON(buffer) + `
          }`;
    return metaStr;
  }

  /**
   * Capture the specified user document definition meta data into a general catalog JSON buffer.
   * @param docDef
   */
  static generateExportMetaStr(docDef: DocumentDefinition): string {
    const metaStr = `
       {
          \"name\": \"` + docDef.name + `\",
          \"documentType\": \"` + docDef.type + `\",
          \"inspectionType\": \"` + docDef.inspectionType + `\",
          \"isSource\": \"` + docDef.isSource + `\"
       }`;
    return metaStr;
  }

  static generateMockJSONDoc(): string {
    return DocumentManagementService.generateMockJSONInstanceDoc();
  }

  static generateMockJSONInstanceDoc(): string {
    const mockDoc = `   {
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

  static generateMockJSONSchemaDoc(): string {
    const mockDoc = `
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
                    "primitiveArrays": {
                        "type": "object",
                        "properties": {
                            "stringArray": {
                                "type": "array",
                                "items": { "type": "string" }
                            },
                            "booleanArray": {
                                "type": "array",
                                "items": { "type": "boolean" }
                            },
                            "numberArray": {
                                "type": "array",
                                "items": { "type": "number" }
                            }
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

  static generateMockJavaDoc(): string {
    const mockDoc = `
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

  constructor(private http: HttpClient) {}

  initialize(): void {
    this.mappingUpdatedSubscription
      = this.cfg.mappingService.mappingUpdated$.subscribe(mappingDefinition => {
      for (const d of this.cfg.getAllDocs()) {
        if (d.initialized) {
          d.updateFromMappings(this.cfg.mappings);
        }
      }
    });
  }

  ngOnDestroy() {
    this.mappingUpdatedSubscription.unsubscribe();
  }

  fetchClassPath(): Observable<string> {
    return new Observable<string>((observer: any) => {
      const requestBody = {
        'MavenClasspathRequest': {
          'jsonType': ConfigModel.javaServicesPackagePrefix + '.MavenClasspathRequest',
          'pomXmlData': this.cfg.initCfg.pomPayload,
          'executeTimeout': this.cfg.initCfg.classPathFetchTimeoutInMilliseconds,
        },
      };
      const url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + 'mavenclasspath';
      DataMapperUtil.debugLogJSON(requestBody, 'Classpath Service Request', this.cfg.initCfg.debugClassPathServiceCalls, url);
      this.http.post(url, requestBody, { headers: this.headers }).toPromise()
        .then((body: any) => {
          DataMapperUtil.debugLogJSON(body, 'Classpath Service Response', this.cfg.initCfg.debugClassPathServiceCalls, url);
          const classPath: string = body.MavenClasspathResponse.classpath;
          observer.next(classPath);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          observer.complete();
        },
      );
    });
  }

  fetchDocument(docDef: DocumentDefinition, classPath: string): Observable<DocumentDefinition> {
    return new Observable<DocumentDefinition>((observer: any) => {
      if (docDef.inspectionResult) {
        const responseJson: any = JSON.parse(docDef.inspectionResult);
        this.parseDocumentResponse(responseJson, docDef);
        observer.next(docDef);
        observer.complete();
        return;
      }

      const payload: any = this.createDocumentFetchRequest(docDef, classPath);
      let url: string = this.cfg.initCfg.baseJavaInspectionServiceUrl + 'class';
      if ((docDef.type === DocumentType.XML) || (docDef.type === DocumentType.XSD)) {
        url = this.cfg.initCfg.baseXMLInspectionServiceUrl + 'inspect';
      } else if (docDef.type === DocumentType.JSON) {
        url = this.cfg.initCfg.baseJSONInspectionServiceUrl + 'inspect';
      }
      DataMapperUtil.debugLogJSON(payload, 'Document Service Request', this.cfg.initCfg.debugDocumentServiceCalls, url);
      this.http.post(url, payload, { headers: this.headers }).toPromise()
        .then((responseJson: any) => {
          DataMapperUtil.debugLogJSON(responseJson, 'Document Service Response', this.cfg.initCfg.debugDocumentServiceCalls, url);
          this.parseDocumentResponse(responseJson, docDef);
          observer.next(docDef);
          observer.complete();
        })
        .catch((error: any) => {
          observer.error(error);
          docDef.errorOccurred = true;
          observer.next(docDef);
          observer.complete();
        },
      );
    });
  }

/**
 * Read the selected file and parse it with the format defined by the specified inspection type.  Call the
 * initialization service to update the sources/ targets in both the runtime and the UI.  The runtime will
 * parse/ validate the file.
 *
 * @param selectedFile
 * @param inspectionType
 * @param isSource
 */
  async processDocument(selectedFile: any, inspectionType: InspectionType, isSource: boolean) {

      let fileText = '';
      const reader = new FileReader();

      // Wait for the async read of the selected document to be completed.
      try {
        fileText = await DataMapperUtil.readFile(selectedFile, reader);
      } catch (error) {
        this.cfg.errorService.mappingError('Unable to import the specified schema document.', error);
        return;
      }

      this.cfg.errorService.clearValidationErrors();

      const schemaFile = selectedFile.name.split('.')[0];
      const schemaFileSuffix: string = selectedFile.name.split('.')[1].toUpperCase();

      // Derive the format if not already defined.
      if (inspectionType === InspectionType.UNKNOWN) {

        if (schemaFileSuffix === DocumentType.XSD) {
          inspectionType = InspectionType.SCHEMA;
        } else if ((fileText.search('SchemaSet') === -1) || (fileText.search('\"\$schema\"') === -1)) {
          inspectionType = InspectionType.INSTANCE;
        } else {
          inspectionType = InspectionType.SCHEMA;
        }
      }
      switch (schemaFileSuffix) {

      case DocumentType.JSON:
        this.cfg.initializationService.initializeUserDoc(fileText, schemaFile, DocumentType.JSON,
          inspectionType, isSource);
        break;

      case 'java':
        this.cfg.initializationService.initializeUserDoc(fileText, schemaFile, DocumentType.JAVA,
          inspectionType, isSource);
        break;

      case DocumentType.XML:
      case DocumentType.XSD:
        this.cfg.initializationService.initializeUserDoc(fileText, schemaFile, schemaFileSuffix,
          inspectionType, isSource);
        break;

      default:
        this.handleError('Unrecognized document suffix (' + schemaFileSuffix + ')', null);
      }
  }

  private createDocumentFetchRequest(docDef: DocumentDefinition, classPath: string): any {
    if ((docDef.type === DocumentType.XML) || (docDef.type === DocumentType.XSD)) {
      return {
        'XmlInspectionRequest': {
          'jsonType': 'io.atlasmap.xml.v2.XmlInspectionRequest',
          'type': docDef.inspectionType,
          'xmlData': docDef.inspectionSource,
        },
      };
    }
    if (docDef.type === DocumentType.JSON) {
      return {
        'JsonInspectionRequest': {
          'jsonType': 'io.atlasmap.json.v2.JsonInspectionRequest',
          'type': docDef.inspectionType,
          'jsonData': docDef.inspectionSource,
        },
      };
    }
    const className: string = docDef.inspectionSource;
    const payload: any = {
      'ClassInspectionRequest': {
        'jsonType': ConfigModel.javaServicesPackagePrefix + '.ClassInspectionRequest',
        'classpath': classPath,
        'className': className,
        'disablePrivateOnlyFields': this.cfg.initCfg.disablePrivateOnlyFields,
        'disableProtectedOnlyFields': this.cfg.initCfg.disableProtectedOnlyFields,
        'disablePublicOnlyFields': this.cfg.initCfg.disablePublicOnlyFields,
        'disablePublicGetterSetterFields': this.cfg.initCfg.disablePublicGetterSetterFields,
      },
    };
    if (this.cfg.initCfg.fieldNameBlacklist && this.cfg.initCfg.fieldNameBlacklist.length) {
      payload['ClassInspectionRequest']['fieldNameBlacklist'] = { 'string': this.cfg.initCfg.fieldNameBlacklist };
    }
    if (this.cfg.initCfg.classNameBlacklist && this.cfg.initCfg.classNameBlacklist.length) {
      payload['ClassInspectionRequest']['classNameBlacklist'] = { 'string': this.cfg.initCfg.classNameBlacklist };
    }
    return payload;
  }

  private parseDocumentResponse(responseJson: any, docDef: DocumentDefinition): void {
    if (docDef.type === DocumentType.JAVA) {
      if (typeof responseJson.ClassInspectionResponse !== 'undefined') {
        this.extractJavaDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.javaClass !== 'undefined')
        || (typeof responseJson.JavaClass !== 'undefined')) {
        this.extractJavaDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown Java inspection result format', responseJson);
      }
    } else if (docDef.type === DocumentType.JSON) {
      if (typeof responseJson.JsonInspectionResponse !== 'undefined') {
        this.extractJSONDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.jsonDocument !== 'undefined')
        || (typeof responseJson.JsonDocument !== 'undefined')) {
        this.extractJSONDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown JSON inspection result format', responseJson);
      }
    } else {
      if (typeof responseJson.XmlInspectionResponse !== 'undefined') {
        this.extractXMLDocumentDefinitionFromInspectionResponse(responseJson, docDef);
      } else if ((typeof responseJson.xmlDocument !== 'undefined')
        || (typeof responseJson.XmlDocument !== 'undefined')) {
        this.extractXMLDocumentDefinition(responseJson, docDef);
      } else {
        this.handleError('Unknown XML inspection result format', responseJson);
      }
    }
    docDef.initializeFromFields(ConfigModel.getConfig().initCfg.debugDocumentParsing);
  }

  private extractJSONDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.JsonInspectionResponse;
    if (body.errorMessage) {
      this.handleError('Could not load JSON document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }

    this.extractJSONDocumentDefinition(body, docDef);
  }

  private extractJSONDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    let jsonDocument: any;
    if (typeof body.jsonDocument !== 'undefined') {
      jsonDocument = body.jsonDocument;
    } else {
      jsonDocument = body.JsonDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.description;
    }

    docDef.characterEncoding = jsonDocument.characterEncoding;
    docDef.locale = jsonDocument.locale;

    for (const field of jsonDocument.fields.field) {
      this.parseJSONFieldFromDocument(field, null, docDef);
    }
  }

  private extractXMLDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.XmlInspectionResponse;
    if (body.errorMessage) {
      this.handleError('Could not load XML document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }

    this.extractXMLDocumentDefinition(body, docDef);
  }

  private extractXMLDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    let xmlDocument: any;
    if (typeof body.xmlDocument !== 'undefined') {
      xmlDocument = body.xmlDocument;
    } else {
      xmlDocument = body.XmlDocument;
    }

    if (!docDef.description) {
      docDef.description = docDef.id;
    }
    if (!docDef.name) {
      docDef.name = docDef.description;
    }

    docDef.characterEncoding = xmlDocument.characterEncoding;
    docDef.locale = xmlDocument.locale;

    if (xmlDocument.xmlNamespaces && xmlDocument.xmlNamespaces.xmlNamespace
      && xmlDocument.xmlNamespaces.xmlNamespace.length) {
      for (const serviceNS of xmlDocument.xmlNamespaces.xmlNamespace) {
        const ns: NamespaceModel = new NamespaceModel();
        ns.alias = serviceNS.alias;
        ns.uri = serviceNS.uri;
        ns.locationUri = serviceNS.locationUri;
        ns.isTarget = serviceNS.targetNamespace;
        docDef.namespaces.push(ns);
      }
    }

    for (const field of xmlDocument.fields.field) {
      if (!docDef.selectedRoot || this.isSelectedRootElement(field, docDef)) {
        this.parseXMLFieldFromDocument(field, null, docDef);
        break;
      }
    }
  }

  private isSelectedRootElement(field: any, docDef: DocumentDefinition): boolean {
    return docDef.selectedRoot && field && field.name
      && docDef.selectedRoot === (field.name.indexOf(':') !== -1 ? field.name.split(':')[1] : field.name);
  }

  private extractJavaDocumentDefinitionFromInspectionResponse(responseJson: any, docDef: DocumentDefinition): void {
    const body: any = responseJson.ClassInspectionResponse;

    if (body.errorMessage) {
      this.handleError('Could not load Java document, error: ' + body.errorMessage, null);
      docDef.errorOccurred = true;
      return;
    }
    this.extractJavaDocumentDefinition(body, docDef);
  }

  private extractJavaDocumentDefinition(body: any, docDef: DocumentDefinition): void {
    const docIdentifier: string = docDef.id;
    const javaClass = body.JavaClass ? body.JavaClass : body.javaClass;
    if (!javaClass || javaClass.status === 'NOT_FOUND') {
      this.handleError('Could not load JAVA document. Document is not found: ' + docIdentifier, null);
      docDef.errorOccurred = true;
      return;
    }

    if (!docDef.description) {
      docDef.description = javaClass.className;
    }
    if (!docDef.name) {
      docDef.name = javaClass.className;
      // Make doc name the class name rather than fully qualified name
      if (docDef.name && docDef.name.indexOf('.') !== -1) {
        docDef.name = docDef.name.substr(docDef.name.lastIndexOf('.') + 1);
      }
    }
    if (javaClass.uri) {
      docDef.uri = javaClass.uri;
    }

    docDef.characterEncoding = javaClass.characterEncoding;
    docDef.locale = javaClass.locale;

    for (const field of javaClass.javaFields.javaField) {
      this.parseJavaFieldFromDocument(field, null, docDef);
    }
  }

  private parseJSONFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.jsonFields && field.jsonFields.jsonField && field.jsonFields.jsonField.length) {
      for (const childField of field.jsonFields.jsonField) {
        this.parseJSONFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private parseFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): Field {
    if (field != null && field.status === 'NOT_FOUND') {
      this.cfg.errorService.warn('Ignoring unknown field: ' + field.name
        + ' (' + field.className + '), parent class: ' + docDef.name, null);
      return null;
    } else if (field != null && field.status === 'BLACK_LIST') {
      return null;
    }

    const parsedField: Field = new Field();
    parsedField.name = field.name;
    parsedField.type = field.fieldType;
    parsedField.path = field.path;
    parsedField.isPrimitive = field.fieldType !== 'COMPLEX';
    parsedField.serviceObject = field;

    if ('LIST' === field.collectionType || 'ARRAY' === field.collectionType) {
      parsedField.isCollection = true;
      if ('ARRAY' === field.collectionType) {
        parsedField.isArray = true;
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
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    if (field.name.indexOf(':') !== -1) {
      parsedField.namespaceAlias = field.name.split(':')[0];
      parsedField.name = field.name.split(':')[1];
    }

    parsedField.isAttribute = (parsedField.path.indexOf('@') !== -1);

    if (field.xmlFields && field.xmlFields.xmlField && field.xmlFields.xmlField.length) {
      for (const childField of field.xmlFields.xmlField) {
        this.parseXMLFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private parseJavaFieldFromDocument(field: any, parentField: Field, docDef: DocumentDefinition): void {
    const parsedField = this.parseFieldFromDocument(field, parentField, docDef);
    if (parsedField == null) {
      return;
    }

    // java fields have a special primitive property, so override the "!= COMPLEX" math from parseFieldFromDocument()
    parsedField.isPrimitive = field.primitive;
    parsedField.classIdentifier = field.className;
    parsedField.enumeration = field.enumeration;

    if (parsedField.enumeration && field.javaEnumFields && field.javaEnumFields.javaEnumField) {
      for (const enumValue of field.javaEnumFields.javaEnumField) {
        const parsedEnumValue: EnumValue = new EnumValue();
        parsedEnumValue.name = enumValue.name;
        parsedEnumValue.ordinal = enumValue.ordinal;
        parsedField.enumValues.push(parsedEnumValue);
      }
    }

    if (field.javaFields && field.javaFields.javaField && field.javaFields.javaField.length) {
      for (const childField of field.javaFields.javaField) {
        this.parseJavaFieldFromDocument(childField, parsedField, docDef);
      }
    }
  }

  private handleError(message: string, error: any): void {
    this.cfg.errorService.error(message, error);
  }
}
