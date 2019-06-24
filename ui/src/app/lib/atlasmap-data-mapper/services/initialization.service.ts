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
import { inflate } from 'pako';
import { Subject, Observable } from 'rxjs';
import { NGXLogger } from 'ngx-logger';

import { DocumentType, InspectionType } from '../common/config.types';
import { DataMapperUtil } from '../common/data-mapper-util';
import { DocumentInitializationModel, ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingDefinition } from '../models/mapping-definition.model';

import { ErrorHandlerService } from './error-handler.service';
import { DocumentManagementService } from '../services/document-management.service';
import { MappingManagementService } from '../services/mapping-management.service';
import { MappingSerializer } from '../services/mapping-serializer.service';

import { TransitionModel, FieldActionConfig } from '../models/transition.model';

@Injectable()
export class InitializationService {
  cfg: ConfigModel = ConfigModel.getConfig();

  systemInitializedSource = new Subject<void>();
  systemInitialized$: Observable<void> = this.systemInitializedSource.asObservable();

  initializationStatusChangedSource = new Subject<void>();
  initializationStatusChanged$: Observable<void> = this.initializationStatusChangedSource.asObservable();

  static createExamplePom(): string {
    const pom = `
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

                <modelVersion>4.0.0</modelVersion>
                <groupId>foo.bar</groupId>
                <artifactId>test.model</artifactId>
                <version>1.10.0</version>
                <packaging>jar</packaging>
                <name>Test :: Model</name>

                <dependencies>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-annotations</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-databind</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-core</artifactId>
                        <version>2.8.5</version>
                    </dependency>
                </dependencies>
            </project>
        `;

    // pom = pom.replace(/\"/g, "\\\"");
    /*xs
    pom = pom.replace(/\n/g, "\\n");
    pom = pom.replace(/\t/g, "\\t");
    */
    return pom;
  }

  static createExampleMappingsJSON(): any {
    const json: any = {
      'AtlasMapping': {
        'jsonType': ConfigModel.mappingServicesPackagePrefix + '.AtlasMapping',
        'dataSource': [
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'twitter4j.Status',
            'uri': 'atlas:java?className=twitter4j.Status',
            'dataSourceType': 'SOURCE'
          },
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'SomeJsonSource',
            'uri': 'atlas:json:SomeJsonSource',
            'dataSourceType': 'SOURCE'
          },
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'SomeXmlSource',
            'uri': 'atlas:xml:SomeXmlSource',
            'dataSourceType': 'SOURCE'
          },
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'salesforce.Contact',
            'uri': 'atlas:java?className=org.apache.camel.salesforce.dto.Contact',
            'dataSourceType': 'TARGET'
          },
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'SomeJsonTarget',
            'uri': 'atlas:json:SomeJsonTarget',
            'dataSourceType': 'TARGET'
          },
          {
            'jsonType': ConfigModel.mappingServicesPackagePrefix + '.DataSource',
            'id': 'SomeXmlTarget',
            'uri': 'atlas:xml:SomeXmlTarget',
            'dataSourceType': 'TARGET'
          },
        ],
        'mappings': {
          'mapping': [
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'inputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'twitter4j.Status',
                  'status': 'SUPPORTED',
                  'modifiers': { 'modifier': [] },
                  'name': 'text',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getText',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/Text',
                  'fieldActions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'salesforce.Contact',
                  'status': 'SUPPORTED',
                  'modifiers': { 'modifier': ['PRIVATE'] },
                  'name': 'Description',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getDescription',
                  'setMethod': 'setDescription',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/Description',
                  'fieldActions': null,
                },
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'mappingType': 'SEPARATE',
              'inputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'twitter4j.Status',
                  'status': 'SUPPORTED',
                  'modifiers': { 'modifier': [] },
                  'name': 'name',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getName',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/User/Name',
                  'actions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'salesforce.Contact',
                  'status': 'SUPPORTED',
                  'modifiers': { 'modifier': ['PRIVATE'] },
                  'name': 'FirstName',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getFirstName',
                  'setMethod': 'setFirstName',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/FirstName',
                  'index': 0,
                  'actions': null,
                },
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'salesforce.Contact',
                  'status': 'SUPPORTED',
                  'modifiers': {
                    'modifier': ['PRIVATE']
                  },
                  'name': 'LastName',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getLastName',
                  'setMethod': 'setLastName',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/LastName',
                  'index': 1,
                  'actions': null,
                },
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'inputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'twitter4j.Status',
                  'status': 'SUPPORTED',
                  'modifiers': { 'modifier': [] },
                  'name': 'screenName',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getScreenName',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/User/ScreenName',
                  'fieldActions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.javaServicesPackagePrefix + '.JavaField',
                  'docId': 'salesforce.Contact',
                  'status': 'SUPPORTED',
                  'modifiers': {
                    'modifier': ['PRIVATE']
                  },
                  'name': 'Title',
                  'className': 'java.lang.String',
                  'type': 'STRING',
                  'getMethod': 'getTitle',
                  'setMethod': 'setTitle',
                  'primitive': true,
                  'array': false,
                  'synthetic': false,
                  'path': '/Title',
                  'fieldActions': null,
                },
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'mappingType': 'COMBINE',
              'inputField': [
                {
                  'jsonType': ConfigModel.jsonServicesPackagePrefix + '.JsonField',
                  'docId': 'SomeJsonSource',
                  'status': 'SUPPORTED',
                  'name': 'js0',
                  'type': 'STRING',
                  'path': '/js0',
                  'index': 0,
                  'actions': null,
                },
                {
                  'jsonType': ConfigModel.jsonServicesPackagePrefix + '.JsonField',
                  'docId': 'SomeJsonSource',
                  'status': 'SUPPORTED',
                  'name': 'js1',
                  'type': 'STRING',
                  'path': '/js1',
                  'index': 1,
                  'actions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.xmlServicesPackagePrefix + '.XmlField',
                  'docId': 'SomeXmlTarget',
                  'status': 'SUPPORTED',
                  'name': 'xt0',
                  'type': 'STRING',
                  'path': '/xt0',
                  'actions': [
                    {
                      'Uppercase': {}
                    }
                  ],
                }
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'mappingType': 'SEPARATE',
              'inputField': [
                {
                  'jsonType': ConfigModel.xmlServicesPackagePrefix + '.XmlField',
                  'docId': 'SomeXmlSource',
                  'status': 'SUPPORTED',
                  'name': 'xs0',
                  'type': 'STRING',
                  'path': '/xs0',
                  'actions': [
                    {
                      'Append': {
                        'string': 'xxx'
                      }
                    }
                  ]
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.jsonServicesPackagePrefix + '.JsonField',
                  'docId': 'SomeJsonTarget',
                  'status': 'SUPPORTED',
                  'name': 'jt0',
                  'type': 'STRING',
                  'path': '/jt0',
                  'index': 0,
                  'actions': null,
                },
                {
                  'jsonType': ConfigModel.jsonServicesPackagePrefix + '.JsonField',
                  'docId': 'SomeJsonTarget',
                  'status': 'SUPPORTED',
                  'name': 'jt1',
                  'type': 'STRING',
                  'path': '/jt1',
                  'index': 1,
                  'actions': null,
                },
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'inputField': [
                {
                  'jsonType': ConfigModel.mappingServicesPackagePrefix + '.ConstantField',
                  'status': 'SUPPORTED',
                  'name': 'constantName',
                  'type': 'STRING',
                  'actions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.xmlServicesPackagePrefix + '.XmlField',
                  'docId': 'SomeXmlTarget',
                  'status': 'SUPPORTED',
                  'name': 'xt1',
                  'type': 'STRING',
                  'path': '/xt1',
                  'actions': null,
                }
              ],
            },
            {
              'jsonType': ConfigModel.mappingServicesPackagePrefix + '.Mapping',
              'inputField': [
                {
                  'jsonType': ConfigModel.mappingServicesPackagePrefix + '.PropertyField',
                  'status': 'SUPPORTED',
                  'name': 'propertyName',
                  'type': 'STRING',
                  'actions': null,
                },
              ],
              'outputField': [
                {
                  'jsonType': ConfigModel.xmlServicesPackagePrefix + '.XmlField',
                  'docId': 'SomeXmlTarget',
                  'status': 'SUPPORTED',
                  'name': 'xt2',
                  'type': 'STRING',
                  'path': '/xt2',
                  'actions': null,
                }
              ],
            },
          ],
        },
        'name': 'UI.867332',
        'lookupTables': { 'lookupTable': [
          {
            'name': 'dummyTable',
            'lookupEntry': [
              {
                'sourceValue': 'Arizona',
                'sourceType': 'String',
                'targetValue': 'AZ',
                'targetType': 'String'
              }
            ]
          }
        ]},
        'constants': { 'constant': [
          {
            'name': 'constantName',
            'value': 'constantValue',
            'fieldType': 'String'
          }
        ]},
        'properties': { 'property': [
          {
            'name': 'propertyName',
            'value': 'propertyValue',
            'fieldType': 'String'
          }
        ]},
      },
    };
    return json;
  }

  constructor(
    private documentService: DocumentManagementService,
    private mappingService: MappingManagementService,
    private errorService: ErrorHandlerService,
    private logger: NGXLogger) {
    this.resetConfig();

    this.cfg.documentService.initialize();
  }

  resetConfig(): void {
    this.cfg = new ConfigModel();
    this.cfg.documentService = this.documentService;
    this.cfg.documentService.cfg = this.cfg;
    this.cfg.mappingService = this.mappingService;
    this.cfg.mappingService.cfg = this.cfg;
    this.cfg.errorService = this.errorService;
    this.cfg.errorService.cfg = this.cfg;
    this.cfg.initializationService = this;
    this.cfg.logger = this.logger;
    ConfigModel.setConfig(this.cfg);
  }

  /**
   * Initialize a user-import schema or schema-instance document.
   *
   * @param docBody
   * @param docName
   * @param docType
   * @param inspectionType
   * @param isSource
   */
  async initializeUserDoc(docBody: any, docName: string, docType: DocumentType, inspectionType: InspectionType, isSource: boolean) {
    let docdef: DocumentDefinition = null;
    const javaArchive = (docType === DocumentType.JAVA_ARCHIVE);
    if (this.cfg.mappingService == null) {
      this.cfg.errorService.warn('Mapping service is not configured, validation service will not be used.', null);
    } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
      this.cfg.errorService.warn('Mapping service URL is not configured, validation service will not be used.', null);
    }

    // Clear out the existing document if importing the same name.
    if (docdef = this.cfg.getDocForIdentifier(docName, isSource)) {
      if (isSource) {
        DataMapperUtil.removeItemFromArray(docdef, this.cfg.sourceDocs);
      } else {
        DataMapperUtil.removeItemFromArray(docdef, this.cfg.targetDocs);
      }
    }

    if (!javaArchive) {
      if (docType === DocumentType.JAVA) {
        docdef = this.addJavaDocument(docName, isSource);
      } else {
        docdef = this.addNonJavaDocument(docName, docType, inspectionType, docBody, isSource);
      }
      docdef.name = docName;
      docdef.updateFromMappings(this.cfg.mappings);
    }

    this.cfg.documentService.fetchClassPath().toPromise()
      .then((classPath: string) => {
        this.cfg.initCfg.classPath = classPath;

        // Push the user-defined java archive file to the runtime service.
        if (javaArchive) {
          this.cfg.documentService.setLibraryToService(docBody);
        } else {
          this.cfg.documentService.fetchDocument(docdef, this.cfg.initCfg.classPath).toPromise()
          .then(async(doc: DocumentDefinition) => {

            if (doc.fields.length === 0) {
              if (isSource) {
                DataMapperUtil.removeItemFromArray(docdef, this.cfg.sourceDocs);
              } else {
                DataMapperUtil.removeItemFromArray(docdef, this.cfg.targetDocs);
              }
              await this.fetchFieldActions();
            }
            this.cfg.mappingService.notifyMappingUpdated();
            this.updateStatus();
          })
          .catch((error: any) => {
            if (error.status === 0) {
              this.handleError('Unable to fetch document ' + docName + ' from the runtime service.', error);
            } else {
              this.handleError('Could not load document \'' + docdef.id + '\': ' + error.status + ' ' + error.statusText, error);
            }
          });
        }
      })
      .catch((error: any) => {
        if (error.status === 0) {
          this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
        } else {
          this.handleError('Could not load Maven class path: ' + error.status + ' ' + error.statusText, error);
        }
      });
  }

  async initialize(): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      this.cfg.fieldActionMetadata = null;
      this.cfg.errorService.clearMappingErrors();
      this.cfg.errorService.clearValidationErrors();

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.warn('Mapping service is not configured, validation service will not be used.', null);
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.warn('Mapping service URL is not configured, validation service will not be used.', null);
      }

      if (this.cfg.initCfg.discardNonMockSources) {
        this.cfg.clearDocs();
      }

      if (this.cfg.initCfg.addMockJSONMappings) {
        const mappingDefinition: MappingDefinition = new MappingDefinition();
        const mappingJSON: any = InitializationService.createExampleMappingsJSON();
        MappingSerializer.deserializeMappingServiceJSON(mappingJSON, mappingDefinition, this.cfg);
        this.cfg.mappings = mappingDefinition;
      }

      if (this.cfg.initCfg.addMockJavaSources || this.cfg.initCfg.addMockJavaSingleSource) {
        this.addJavaDocument('twitter4j.Status', true);
        if (this.cfg.initCfg.addMockJavaSources) {
          this.addJavaDocument('io.atlasmap.java.test.TargetTestClass', true);
          this.addJavaDocument('io.atlasmap.java.test.SourceContact', true);
          this.addJavaDocument('io.atlasmap.java.test.SourceAddress', true);
          this.addJavaDocument('io.atlasmap.java.test.TestListOrders', true);
          this.addJavaDocument('io.atlasmap.java.test.TargetOrderArray', true);
          this.addJavaDocument('io.atlasmap.java.test.SourceFlatPrimitiveClass', true);
          this.addJavaDocument('io.atlasmap.java.test.SourceOrder', true);
          this.addJavaDocument('io.atlasmap.java.test.DateTimeClass', true);
          this.addJavaDocument('io.atlasmap.java.test.SourceCollectionsClass', true);
          this.addJavaDocument('io.atlasmap.java.test.BaseOrder$SomeStaticClass', true);
        }
      }

      if (this.cfg.initCfg.addMockJavaCachedSource) {
        const docDef: DocumentDefinition = this.addJavaDocument('io.atlasmap.java.test.Name', true);
        docDef.inspectionResult = DocumentManagementService.generateMockJavaDoc();
      }

      if (this.cfg.initCfg.addMockXMLInstanceSources) {
        this.addNonJavaDocument('XMLInstanceSource', DocumentType.XML, InspectionType.INSTANCE,
          DocumentManagementService.generateMockInstanceXMLDoc(), true);
      }

      if (this.cfg.initCfg.addMockXMLSchemaSources) {
        this.addNonJavaDocument('XMLSchemaSource', DocumentType.XML, InspectionType.SCHEMA,
          DocumentManagementService.generateMockSchemaXMLDoc(), true);
      }

      if (this.cfg.initCfg.addMockJSONSources || this.cfg.initCfg.addMockJSONInstanceSources) {
        this.addNonJavaDocument('JSONInstanceSource', DocumentType.JSON, InspectionType.INSTANCE,
          DocumentManagementService.generateMockJSONInstanceDoc(), true);
      }

      if (this.cfg.initCfg.addMockJSONSchemaSources) {
        this.addNonJavaDocument('JSONSchemaSource', DocumentType.JSON, InspectionType.SCHEMA,
          DocumentManagementService.generateMockJSONSchemaDoc(), true);
      }

      if (this.cfg.initCfg.addMockJavaTarget) {
        this.addJavaDocument('io.atlasmap.java.test.TargetTestClass', false);
        this.addJavaDocument('io.atlasmap.java.test.SourceContact', false);
        this.addJavaDocument('io.atlasmap.java.test.SourceAddress', false);
        this.addJavaDocument('io.atlasmap.java.test.TestListOrders', false);
        this.addJavaDocument('io.atlasmap.java.test.TargetOrderArray', false);
        this.addJavaDocument('io.atlasmap.java.test.SourceFlatPrimitiveClass', false);
        this.addJavaDocument('io.atlasmap.java.test.SourceOrder', false);
        this.addJavaDocument('io.atlasmap.java.test.DateTimeClass', false);
        this.addJavaDocument('io.atlasmap.java.test.TargetCollectionsClass', false);
      }

      if (this.cfg.initCfg.addMockJavaCachedTarget) {
        const docDef: DocumentDefinition = this.addJavaDocument('io.atlasmap.java.test.Name', false);
        docDef.inspectionResult = DocumentManagementService.generateMockJavaDoc();
      }

      if (this.cfg.initCfg.addMockXMLInstanceTarget) {
        this.addNonJavaDocument('XMLInstanceTarget', DocumentType.XML, InspectionType.INSTANCE,
          DocumentManagementService.generateMockInstanceXMLDoc(), false);
      }

      if (this.cfg.initCfg.addMockXMLSchemaTarget) {
        this.addNonJavaDocument('XMLSchemaTarget', DocumentType.XML, InspectionType.SCHEMA,
          DocumentManagementService.generateMockSchemaXMLDoc(), false);
      }

      if (this.cfg.initCfg.addMockJSONTarget || this.cfg.initCfg.addMockJSONInstanceTarget) {
        this.addNonJavaDocument('JSONInstanceTarget', DocumentType.JSON, InspectionType.INSTANCE,
          DocumentManagementService.generateMockJSONInstanceDoc(), false);
      }

      if (this.cfg.initCfg.addMockJSONSchemaTarget) {
        this.addNonJavaDocument('JSONSchemaTarget', DocumentType.JSON, InspectionType.SCHEMA,
          DocumentManagementService.generateMockJSONSchemaDoc(), false);
      }

      // load field actions
      await this.fetchFieldActions();

      // load documents
      if (!this.cfg.isClassPathResolutionNeeded()) {
        this.fetchDocuments();
      } else {
        this.updateLoadingStatus('Loading Maven class path.');
        // fetch class path
        this.cfg.documentService.fetchClassPath().toPromise()
          .then((classPath: string) => {
            this.cfg.initCfg.classPath = classPath;
            this.fetchDocuments();
            this.updateStatus();
          })
          .catch((error: any) => {
            if (error.status === 0) {
              this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
            } else {
              this.handleError('Could not load Maven class path: ' + error.status + ' ' + error.statusText, error);
            }
            resolve(false);
          });
      }

      // Fetch adm-catalog-files.gz if it exists.
      this.cfg.mappingService.getCurrentMappingCatalog().subscribe( async(catalog: Uint8Array) => {

        // If catalog is null then no compressed mappings catalog is available on the server.
        if (catalog === null) {
          if (this.cfg.mappings === null) {
            this.cfg.mappings = new MappingDefinition();
          }
          this.updateStatus();
          resolve(true);
          return;
        }

        await this.processMappingsCatalogFiles(catalog);

        // load mappings
        if (this.cfg.mappings == null) {
          this.cfg.mappings = new MappingDefinition();
          if (this.cfg.mappingFiles.length > 0) {
            await this.initMappings(this.cfg.mappingFiles);
          } else {
            this.cfg.mappingService.findMappingFiles('UI').toPromise()
              .then(async(files: string[]) => {
                await this.initMappings(files);
              },
              (error: any) => {
                if (error.status === 0) {
                  this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
                  resolve(false);
                }
              }
            );
          }
        }
        this.updateStatus();
        resolve(true);
      });
      resolve(true);
    });
  }

  /**
   * Fetch and initialize user mappings from the runtime service.
   *
   * @param mappingFiles
   */
  async initMappings(mappingFiles: string[]): Promise<boolean> {
    return new Promise<boolean>( async(resolve, reject) => {
      await this.fetchMappings(mappingFiles);
      this.cfg.mappingService.notifyMappingUpdated();
      resolve(true);
    });
  }

  processMappingsDocuments(mappingsSchemaAggregate: string): any {
    let mInfo: any = null;
    try {
      mInfo = DocumentManagementService.getMappingsInfo(mappingsSchemaAggregate);
    } catch (error) {
      this.cfg.errorService.mappingError('Unable to process mapping information from the data mappings file. ' +
        '\n' + error.message, error);
      return null;
    }

    this.cfg.errorService.clearValidationErrors();

    let metaFragment: any = null;
    let fragData = '';
    let fragIndex = 0;

    // Reinitialize the model documents.
    for (metaFragment of mInfo.exportMeta) {
      fragData = mInfo.exportBlockData[fragIndex].value;
      this.initializeUserDoc(fragData, metaFragment.name, metaFragment.documentType,
        metaFragment.inspectionType, (metaFragment.isSource === 'true'));
      fragIndex++;
    }
    this.cfg.mappingService.notifyMappingUpdated();
    return mInfo;
  }

  /**
   * Update .../target/mappings/atlasmapping-UI.nnnnnn.json in the runtime service.
   *
   * @param mInfo
   */
  async updateMappings(mInfo: any): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      try {
        this.cfg.mappingService.setMappingToService(mInfo.exportMappings.value).toPromise()
          .then(async(result: boolean) => {
            resolve(true);
        }).catch((error: any) => {
          if (error.status === 0) {
            this.cfg.errorService.mappingError(
              'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
          } else {
            this.cfg.errorService.mappingError(
              'Unable to update the mappings file to the AtlasMap design runtime service.  ' +
                 error.status + ' ' + error.statusText, error);
          }
          resolve(false);
        });
      } catch (error) {
        this.cfg.errorService.mappingError('Unable to decompress the aggregate mappings catalog buffer.\n', error);
        resolve(false);
      }
    });
  }

  /**
   * Update the GZIP catalog file in the runtime service.
   *
   * @param compressedCatalog
   */
  async updateCatalog(compressedCatalog: Uint8Array): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
        // Update .../target/mappings/adm-catalog-files.gz
        const fileContent: Blob = new Blob([compressedCatalog], {type: 'application/octet-stream'});
        this.cfg.mappingService.setBinaryFileToService(fileContent, this.cfg.initCfg.baseMappingServiceUrl + 'mapping/GZ/0').toPromise()
          .then(async(result: boolean) => {
          resolve(true);
        }).catch((error: any) => {
          if (error.status === 0) {
            this.cfg.errorService.mappingError(
              'Fatal network error: Unable to connect to the AtlasMap design runtime service.', error);
          } else {
            this.cfg.errorService.mappingError(
              'Unable to update the catalog mappings file to the AtlasMap design runtime service.  ' +
                error.status + ' ' + error.statusText, error);
            resolve(false);
          }
        });
    });
  }

  /**
   * The compressed binary content (gzip) from either an imported ADM catalog file or from
   * the DM runtime catalog is presented to update the canvas.
   *
   * @param compressedCatalog - gzip binary buffer
   */
  async processMappingsCatalogFiles(compressedCatalog: Uint8Array): Promise<boolean> {
    return new Promise<boolean>(async(resolve, reject) => {
      try {

        // Inflate the compressed content.
        const decompress = inflate(compressedCatalog);
        const mappingsDocuments =
          new Uint8Array(decompress).reduce((data, byte) => data + String.fromCharCode(byte), '');
        const mInfo = this.processMappingsDocuments(mappingsDocuments);

        // Reinitialize the model mappings.
        if (mInfo && mInfo.exportMappings) {
          const catalogMappingsName = MappingSerializer.deserializeAtlasMappingName(
            DocumentManagementService.getMappingsInfo(mInfo.exportMappings.value));

            // If the live UI mappings name does not match the UI mappings name extracted from the
            // catalog file then use the mappings from the catalog file.  Otherwise use the live
            // UI file.
            this.cfg.mappingService.findMappingFiles('UI').toPromise()
              .then( async(files: string[]) => {
              if (catalogMappingsName !== files[0]) {
                await this.updateMappings(mInfo);
              }
              await this.updateCatalog(compressedCatalog);
              resolve(true);
            },
            (error: any) => {
              if (error.status === 0) {
                this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
              }
              resolve(false);
            }
          );
        }
        await this.updateCatalog(compressedCatalog);
        resolve(true);
      } catch (error) {
        this.cfg.errorService.mappingError('Unable to decompress the aggregate mappings catalog buffer.\n', error);
        resolve(false);
      }
    });
  }

  addJavaDocument(className: string, isSource: boolean): DocumentDefinition {
    const model: DocumentInitializationModel = new DocumentInitializationModel();
    model.id = className;
    model.type = DocumentType.JAVA;
    model.inspectionType = InspectionType.JAVA_CLASS;
    model.inspectionSource = className;
    model.isSource = isSource;
    return this.cfg.addDocument(model);
  }

  private addNonJavaDocument(
    name: string, documentType: DocumentType, inspectionType: InspectionType,
    inspectionSource: string, isSource: boolean): DocumentDefinition {
    const model: DocumentInitializationModel = new DocumentInitializationModel();
    model.id = name;
    model.type = documentType;
    model.inspectionType = inspectionType;
    model.inspectionSource = inspectionSource;
    model.isSource = isSource;
    return this.cfg.addDocument(model);
  }

  private fetchDocuments(): void {
    this.updateLoadingStatus('Loading source/target documents.');
    for (const docDef of this.cfg.getAllDocs()) {
      if (docDef === this.cfg.propertyDoc || docDef === this.cfg.constantDoc) {
        docDef.initialized = true;
        continue;
      }

      const docName: string = docDef.name;

      if (docDef.type === DocumentType.JAVA_ARCHIVE && this.cfg.initCfg.baseJavaInspectionServiceUrl == null) {
        this.cfg.errorService.warn('Java inspection service is not configured. Document will not be loaded: ' + docName, docDef);
        docDef.initialized = true;
        this.updateStatus();
        continue;
      } else if (docDef.type === DocumentType.XML && this.cfg.initCfg.baseXMLInspectionServiceUrl == null) {
        this.cfg.errorService.warn('XML inspection service is not configured. Document will not be loaded: ' + docName, docDef);
        docDef.initialized = true;
        this.updateStatus();
        continue;
      } else if (docDef.type === DocumentType.JSON && this.cfg.initCfg.baseJSONInspectionServiceUrl == null) {
        this.cfg.errorService.warn('JSON inspection service is not configured. Document will not be loaded: ' + docName, docDef);
        docDef.initialized = true;
        this.updateStatus();
        continue;
      }

      this.cfg.documentService.fetchDocument(docDef, this.cfg.initCfg.classPath).toPromise()
        .then((doc: DocumentDefinition) => {
          this.updateStatus();
        })
        .catch((error: any) => {
          if (error.status === 0) {
            this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
          } else {
            const errDetail = error.status ? error.status + ':' + error.statusText : error.toString();
            this.handleError(`Could not load Document \'${docDef.name}\'(${docDef.id}): ${errDetail}`, error);
          }
        });
    }
  }

  /**
   * Fetch mapping files and initialize user mappings in the canvas.
   *
   * @param mappingFiles
   */
  async fetchMappings(mappingFiles: string[]): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (mappingFiles == null || mappingFiles.length === 0) {
        resolve(false);
      }

      this.cfg.mappingService.fetchMappings(mappingFiles, this.cfg.mappings).toPromise()
        .then((result: boolean) => {
        this.cfg.initCfg.mappingInitialized = true;
        this.updateStatus();
        resolve(true);
      }).catch((error: any) => {
        if (error.status === 0) {
          this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
        } else {
          this.handleError('Could not load mapping definitions.', error);
        }
        resolve(false);
      });
    });
  }

  async fetchFieldActions(): Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      if (this.cfg.fieldActionMetadata) {
        const actionConfigs: FieldActionConfig[] = [];
        for (const actionDetail of this.cfg.fieldActionMetadata.ActionDetails.actionDetail) {
          const fieldActionConfig = MappingManagementService.extractFieldActionConfig(actionDetail);
          actionConfigs.push(fieldActionConfig);
        }
        MappingManagementService.sortFieldActionConfigs(actionConfigs);
        TransitionModel.actionConfigs = actionConfigs;
        this.cfg.initCfg.fieldActionsInitialized = true;
        resolve(true);
        return;
      }

      if (this.cfg.mappingService == null) {
        this.cfg.errorService.warn('Mapping service is not provided. Field Actions will not be used.', null);
        this.cfg.initCfg.fieldActionsInitialized = true;
        resolve(true);
        return;
      } else if (this.cfg.initCfg.baseMappingServiceUrl == null) {
        this.cfg.errorService.warn('Mapping service URL is not provided. Field Actions will not be used.', null);
        this.cfg.initCfg.fieldActionsInitialized = true;
        resolve(true);
        return;
      }

      // Fetch the field actions from the runtime service.
      this.cfg.mappingService.fetchFieldActions().toPromise()
        .then((fetchedActionConfigs: FieldActionConfig[]) => {
          TransitionModel.actionConfigs = fetchedActionConfigs;
          this.cfg.initCfg.fieldActionsInitialized = true;
          resolve(true);
        }).catch((error: any) => {
          if (error.status === 0) {
            this.handleError('Fatal network error: Could not connect to AtlasMap design runtime service.', error);
          } else {
            this.handleError('Could not load field action configs: ' + error.status + ' ' + error.statusText, error);
          }
          resolve(false);
        });
    });
  }

  private updateStatus(): void {
    const documentCount: number = this.cfg.getAllDocs().length;
    let finishedDocCount = 0;
    for (const docDef of this.cfg.getAllDocs()) {
      if (docDef.initialized || docDef.errorOccurred) {
        finishedDocCount++;
      }
    }

    if ((documentCount === finishedDocCount) && this.cfg.initCfg.fieldActionsInitialized) {
      if (this.cfg.mappings) {
        this.cfg.mappings.detectTableIdentifiers();
        this.cfg.mappings.updateDocumentNamespacesFromMappings(this.cfg);
        this.cfg.mappings.updateMappingsFromDocuments(this.cfg);
        for (const d of this.cfg.getAllDocs()) {
          d.updateFromMappings(this.cfg.mappings);
        }
        this.cfg.mappings.removeStaleMappings(this.cfg);
      }
      this.updateLoadingStatus('Initialization complete.');
      this.cfg.initCfg.initialized = true;
      this.systemInitializedSource.next();
    }
  }

  private handleError(message: string, error: any) {
    message = 'Data Mapper UI Initialization Error: ' + message;
    this.cfg.errorService.error(message, error);
    this.updateLoadingStatus(message);
    this.cfg.initCfg.initializationErrorOccurred = true;
    this.cfg.initCfg.initialized = true;
    this.systemInitializedSource.next();
  }

  updateLoadingStatus(status: string): void {
    this.cfg.initCfg.loadingStatus = status;
    this.initializationStatusChangedSource.next();
  }

}
