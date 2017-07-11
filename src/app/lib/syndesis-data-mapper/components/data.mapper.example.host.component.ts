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

import { Component, ViewChild } from '@angular/core';

import { DocumentDefinition, DocumentTypes } from '../models/document.definition.model';
import { MappingDefinition } from '../models/mapping.definition.model';
import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';

import { ErrorHandlerService } from '../services/error.handler.service';
import { DocumentManagementService } from '../services/document.management.service';
import { MappingManagementService } from '../services/mapping.management.service';
import { InitializationService } from '../services/initialization.service';
import { ValidationService } from '../services/validation.service';

import { DataMapperAppComponent } from './data.mapper.app.component';

@Component({
    selector: 'data-mapper-example-host',
    template: `
        <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    `,
    providers: [MappingManagementService, ErrorHandlerService, DocumentManagementService]
})

export class DataMapperAppExampleHostComponent {

    @ViewChild('dataMapperComponent')
    public dataMapperComponent: DataMapperAppComponent;

    public cfg: ConfigModel = null;

    constructor(private initializationService: InitializationService) {
        console.log("Host component being created.");
        // initialize config information before initializing services
        var c: ConfigModel = initializationService.cfg

        //store references to our services in our config model

        //initialize base urls for our service calls
        c.initCfg.baseJavaInspectionServiceUrl = "http://localhost:8585/v2/atlas/java/";
        c.initCfg.baseXMLInspectionServiceUrl = "http://localhost:8585/v2/atlas/xml/";
        c.initCfg.baseJSONInspectionServiceUrl = "http://localhost:8585/v2/atlas/json/";        
        c.initCfg.baseMappingServiceUrl = "http://localhost:8585/v2/atlas/";
        c.initCfg.baseValidationServiceUrl = "http://localhost:8585/v2/atlas/";
        c.initCfg.baseFieldMappingServiceUrl = "http://localhost:8585/v2/atlas/"

        //initialize data for our class path service call
        //note that quotes, newlines, and tabs are escaped
        c.initCfg.pomPayload = InitializationService.createExamplePom();
        c.initCfg.classPathFetchTimeoutInMilliseconds = 30000;
        // if classPath is specified, maven call to resolve pom will be skipped
        c.initCfg.classPath = null;

        //specify source/target documents          
           
        c.addJavaDocument("io.atlasmap.java.test.SourceOrder", true);                
        
        c.addJavaDocument("io.atlasmap.java.test.SourceContact", true);
        c.addJavaDocument("io.atlasmap.java.test.SourceAddress", true);
        c.addJavaDocument("io.atlasmap.java.test.TestListOrders", true);
        //c.addJavaDocument("io.atlasmap.java.test.TestListOrders", false);
        c.addJavaDocument("io.atlasmap.java.test.TargetOrderArray", true);
        c.addJavaDocument("io.atlasmap.java.test.SourceFlatPrimitiveClass", true);        
        
        c.addJavaDocument("io.atlasmap.java.test.TargetTestClass", true);
        c.addJavaDocument("io.atlasmap.java.test.TargetTestClass", false);
        

        var sampleXML: string = `<data>
                <intField a='1'>32000</intField><longField>12421</longField>
                <stringField>abc</stringField><booleanField>true</booleanField>
                <doubleField b='2'>12.0</doubleField><shortField>1000</shortField>
                <floatField>234.5f</floatField><charField>A</charField>
                <outer><inner><value>val</value></inner></outer>
            </data>
        `;

        sampleXML = `
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

        //c.addXMLDocument("XMLSampleInstanceTarget", sampleXML, false, false);

        sampleXML = `<?xml version="1.0" encoding="UTF-8" ?>
            <foo>bar</foo>
        `;

        sampleXML = "<foo>bar</foo>";        

        sampleXML = `
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

        c.addXMLDocument("XMLSampleInstanceSource", sampleXML, true, false);
        

        sampleXML = `
            <foo><bar><jason>somevalue</jason></bar></foo>
        `;        

        sampleXML = `            
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

        sampleXML = `
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

        sampleXML = `
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

        sampleXML = `
            <schema xmlns="http://www.w3.org/2001/XMLSchema" targetNamespace="http://example.com/"
                xmlns:tns="http://example.com/">                
                <element name="aGlobalElement" type="tns:aGlobalType"/>
                <simpleType name="aGlobalType"><restriction base="string"/></simpleType>
            </schema>
        `;

        //c.addXMLDocument("XMLSampleSchemaSource1", sampleXML, false, true);

        sampleXML = `
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

        c.addXMLDocument("XMLSampleSchemaSource", sampleXML, false, true);

        sampleXML = `
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

        var sampleJSON: string = `{
                "id": "0001",
                "type": "donut",
                "name": "Cake",
                "ppu": 0.55,
                "batters": {
                    "batter": [
                        { "id": "1001", "type": "Regular" },
                        { "id": "1002", "type": "Chocolate" },
                        { "id": "1003", "type": "Blueberry" },
                        { "id": "1004", "type": "Devil's Food" }
                    ]
                },
                "topping": [
                    { "id": "5001", "type": "None" },
                    { "id": "5002", "type": "Glazed" },
                    { "id": "5005", "type": "Sugar" },
                    { "id": "5007", "type": "Powdered Sugar" },
                    { "id": "5006", "type": "Chocolate with Sprinkles" },
                    { "id": "5003", "type": "Chocolate" },
                    { "id": "5004", "type": "Maple" }
                ]
            }
        `;

        c.addJSONDocument("JSONSampleSource", sampleJSON, true);
        //c.addJSONDocument("JSONSampleTarget", sampleJSON, false);
        
        console.log("Example config.", c);

        //initialize system
        this.cfg = c;
        c.initializationService.initialize();

        //save the mappings when the ui calls us back asking for save
        c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
            //NOTE: the mapping definition being saved is currently stored in "this.cfg.mappings" until further notice.

            console.log("Host component saving mappings.");
            console.log("Mappings to save.", this.cfg.mappings);

            //turn this on to print out example json
            var makeExampleJSON: boolean = false;
            if (makeExampleJSON) {
                var jsonObject: any = c.mappingService.serializeMappingsToJSON();
                var jsonVersion = JSON.stringify(jsonObject);
                var jsonPretty = JSON.stringify(JSON.parse(jsonVersion),null,2);
                console.log("Mappings as JSON: " + jsonPretty);
            }

            //This is an example callout to save the mapping to the mock java service
            c.mappingService.saveMappingToService();

            //After you've sucessfully saved you *MUST* call this (don't call on error)
            c.mappingService.handleMappingSaveSuccess(saveHandler);
        });
    }
}
