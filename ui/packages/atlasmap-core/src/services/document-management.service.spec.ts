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
import {
  ConfigModel,
  DocumentInitializationModel,
} from '../models/config.model';
import { DocumentType, FieldType, InspectionType } from '../contracts/common';

import { DocumentDefinition } from '../models/document-definition.model';
import { DocumentManagementService } from '../services/document-management.service';
import { Field } from '../models/field.model';
import { InitializationService } from './initialization.service';
import type { Input } from 'ky/distribution/types/options';
import { MappingDefinition } from '../models/mapping-definition.model';
import { TestUtils } from '../../test/test-util';
import atlasmapInspectionComplexObjectRootedJson from '../../../../test-resources/inspected/atlasmap-inspection-complex-object-rooted.json';
import atlasmapInspectionPoExampleSchemaJson from '../../../../test-resources/inspected/atlasmap-inspection-po-example-schema.json';
import atlasmapInspectionTargetTestClassJson from '../../../../test-resources/inspected/atlasmap-inspection-io.atlasmap.java.test.TargetTestClass.json';
import atlasmapInspectionTwitter4jStatusJson from '../../../../test-resources/inspected/atlasmap-inspection-twitter4j.Status.json';
import fs from 'fs';
import ky from 'ky';

describe('DocumentManagementService', () => {
  let cfg: ConfigModel;
  let service: DocumentManagementService;

  beforeEach(() => {
    const initService = new InitializationService(ky);
    cfg = initService.cfg;
    service = cfg.documentService;
  });

  test('initialize()/uninitialize()', () => {
    TestUtils.createMockMappings(cfg);
    const spyUfm = spyOn<any>(
      DocumentDefinition.prototype,
      'updateFromMappings'
    ).and.stub();
    cfg.sourceDocs[0].initialized = true;
    service.uninitialize();
    const count = spyUfm.calls.count();
    cfg.mappingService.mappingUpdatedSource.next();
    expect(spyUfm.calls.count()).toBe(count);
    service.initialize();
    cfg.mappingService.mappingUpdatedSource.next();
    expect(spyUfm.calls.count()).toBe(count + 1);
    service.uninitialize();
    cfg.mappingService.mappingUpdatedSource.next();
    expect(spyUfm.calls.count()).toBe(count + 1);
  });

  test('inspectDocuments() parse Java inspection', (done) => {
    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.JAVA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionTwitter4jStatusJson
    );
    cfg.addDocument(docDef);
    service.inspectDocuments().subscribe({
      next: (answer: DocumentDefinition) => {
        expect(answer.fields.length).toBe(29);
        const text = answer.getField('/text');
        expect(text).toBeTruthy();
        expect(text?.name).toBe('text');
        expect(text?.type).toBe('STRING');
        expect(text?.children.length).toBe(0);
        const user = answer.getField('/user');
        expect(user).toBeTruthy();
        expect(user?.name).toBe('user');
        expect(user?.type).toBe('COMPLEX');
        expect(user?.classIdentifier).toBe('twitter4j.User');
        expect(user?.children.length).toBe(57);
        const screenName = user?.children?.filter(
          (child: Field) => child?.name === 'screenName'
        );
        if (!screenName) {
          fail('no screenName');
        }
        expect(screenName?.length).toBe(1);
        expect(screenName[0]?.name).toBe('screenName');
        expect(screenName[0]?.path).toBe('/user/screenName');
        expect(screenName[0]?.type).toBe('STRING');
        expect(screenName[0]?.children.length).toBe(0);
        const url = answer.getField('/place/name');
        expect(url?.name).toBe('name');
        expect(url?.path).toBe('/place/name');
        expect(url?.type).toBe('STRING');
        const urlParent = url?.parentField;
        expect(urlParent?.name).toBe('place');
        expect(screenName[0]?.children.length).toBe(0);
        done();
      },
      error: (error) => {
        fail(error);
      },
    });
  });

  test('inspectDocuments() parse Java inspection TargetTestClass', (done) => {
    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.JAVA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionTargetTestClassJson
    );
    cfg.addDocument(docDef);
    service.inspectDocuments().subscribe({
      next: (answer: DocumentDefinition) => {
        const contact = answer.getField('/contact');
        expect(contact).toBeTruthy();
        expect(contact?.type).toBe('COMPLEX');
        expect(contact?.name).toBe('contact');
        expect(contact?.path).toBe('/contact');
        expect(contact?.isCollection).toBeFalsy();
        const contactFirstName = answer.getField('/contact/firstName');
        expect(contactFirstName).toBeTruthy();
        expect(contactFirstName?.type).toBe('STRING');
        expect(contactFirstName?.name).toBe('firstName');
        expect(contactFirstName?.path).toBe('/contact/firstName');
        const list = answer.getField('/contactList<>');
        expect(list).toBeTruthy();
        expect(list?.type).toBe('COMPLEX');
        expect(list?.name).toBe('contactList');
        expect(list?.path).toBe('/contactList<>');
        expect(list?.isCollection).toBeTruthy();
        const listFirstName = answer.getField('/contactList<>/firstName');
        expect(listFirstName).toBeTruthy();
        expect(listFirstName?.type).toBe('STRING');
        expect(listFirstName?.name).toBe('firstName');
        expect(listFirstName?.path).toBe('/contactList<>/firstName');
        const array = answer.getField('/contactArray[]');
        expect(array).toBeTruthy();
        expect(array?.type).toBe('COMPLEX');
        expect(array?.name).toBe('contactArray');
        expect(array?.path).toBe('/contactArray[]');
        expect(array?.isCollection).toBeTruthy();
        const arrayFirstName = answer.getField('/contactArray[]/firstName');
        expect(arrayFirstName).toBeTruthy();
        expect(arrayFirstName?.type).toBe('STRING');
        expect(arrayFirstName?.name).toBe('firstName');
        expect(arrayFirstName?.path).toBe('/contactArray[]/firstName');
        done();
      },
      error: (error) => {
        fail(error);
      },
    });
  });

  test('inspectDocuments() parse JSON inspection', (done) => {
    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.JSON;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionComplexObjectRootedJson
    );
    cfg.addDocument(docDef);
    service.inspectDocuments().subscribe({
      next: (answer: DocumentDefinition) => {
        expect(answer.fields.length).toBe(1);
        expect(answer.fields[0].name).toBe('order');
        const order = answer.getField('/order');
        expect(order?.name).toBe('order');
        expect(order?.type).toBe('COMPLEX');
        expect(order?.children).toBeTruthy();
        expect(order?.children?.length).toBe(3);
        done();
      },
      error: (error) => {
        fail(error);
      },
    });
  });

  test('inspectDocuments() parse XML inspection', (done) => {
    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.XML;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionPoExampleSchemaJson
    );
    cfg.addDocument(docDef);
    service.inspectDocuments().subscribe((answer: DocumentDefinition) => {
      expect(answer.fields.length).toBe(1);
      expect(answer.fields[0].name).toBe('purchaseOrder');
      const purchaseOrder = answer.getField('/tns:purchaseOrder');
      expect(purchaseOrder?.name).toBe('purchaseOrder');
      expect(purchaseOrder?.type).toBe('COMPLEX');
      expect(purchaseOrder?.children).toBeTruthy();
      expect(purchaseOrder?.children?.length).toBe(5);
      done();
    });
  });

  test('inspectDocuments() pick up one XML root element', (done) => {
    const docDef = new DocumentInitializationModel();
    docDef.type = DocumentType.XML;
    docDef.name = 'purchaseOrder';
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionPoExampleSchemaJson
    );
    docDef.selectedRoot = 'purchaseOrder';
    cfg.addDocument(docDef);
    const docDef2 = new DocumentInitializationModel();
    docDef2.type = DocumentType.XML;
    docDef2.name = 'comment';
    docDef2.inspectionType = InspectionType.SCHEMA;
    docDef2.inspectionResult = docDef.inspectionResult;
    docDef2.selectedRoot = 'comment';
    cfg.addDocument(docDef2);
    let count = 0;
    service.inspectDocuments().subscribe((answer) => {
      if (answer.name === 'purchaseOrder') {
        expect(answer.fields.length).toBe(1);
        expect(answer.fields[0].name).toBe('purchaseOrder');
        count++;
      } else if (answer.name === 'comment') {
        expect(answer.fields.length).toBe(1);
        expect(answer.fields[0].name).toBe('comment');
        count++;
      }
      if (count === 2) {
        done();
      }
    });
  });

  test('getLibraryClassNames()', (done) => {
    cfg.initCfg.baseMappingServiceUrl = 'dummyurl';
    spyOn(ky, 'get').and.callFake((_url: Input) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve({
            ArrayList: ['dummy.class.0', 'dummy.class.1'],
          });
        }
      })();
    });
    service
      .getLibraryClassNames()
      .then((values) => {
        expect(values[0]).toBe('dummy.class.0');
        expect(values[1]).toBe('dummy.class.1');
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('importNonJavaDocument()', (done) => {
    cfg.initCfg.baseJSONInspectionServiceUrl = 'json';
    spyOn(ky, 'post').and.callFake((_url: Input) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve({
            JsonInspectionResponse: {
              jsonDocument: {
                fields: {
                  field: [
                    {
                      name: 'dummyField',
                      path: 'dummyField',
                    },
                  ],
                },
              },
            },
          });
        }
      })();
    });
    const buf = fs.readFileSync(
      `${__dirname}/../../../../test-resources/json/schema/mock-json-schema.json`
    );
    const file = new File([new Blob([buf])], 'mock-json-schema.json');
    expect(cfg.sourceDocs.length).toBe(0);
    service
      .importNonJavaDocument(file, true, true, {})
      .then((value) => {
        expect(value).toBeTruthy();
        expect(cfg.sourceDocs.length).toBe(1);
        expect(cfg.sourceDocs[0].type).toBe(DocumentType.JSON);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('importJavaDocument()', (done) => {
    cfg.initCfg.baseJavaInspectionServiceUrl = 'java';
    spyOn(ky, 'post').and.callFake((_url: Input) => {
      return new (class {
        json(): Promise<any> {
          return Promise.resolve({
            ClassInspectionResponse: {
              javaClass: {
                javaFields: {
                  javaField: [
                    {
                      name: 'dummyField',
                      path: 'dummyField',
                    },
                  ],
                },
              },
            },
          });
        }
      })();
    });
    expect(cfg.sourceDocs.length).toBe(0);
    service
      .importJavaDocument('io.atlasmap.test.TestDocumentClass', true)
      .then((value) => {
        expect(value).toBeTruthy();
        expect(cfg.sourceDocs.length).toBe(1);
        expect(cfg.sourceDocs[0].type).toBe(DocumentType.JAVA);
        done();
      })
      .catch((error) => {
        fail(error);
      });
  });

  test('Constant field', () => {
    cfg.mappings = new MappingDefinition();
    expect(cfg.constantDoc.fields.length).toBe(0);
    service.createConstant('testConst', 'testConstVal', 'STRING', false);
    expect(cfg.constantDoc.fields.length).toBe(1);
    const f = cfg.constantDoc.getField('/testConst');
    expect(f).toBeTruthy();
    expect(f!.name).toBe('testConst');
    expect(f!.path).toBe('/testConst');
    expect(f!.type).toBe(FieldType.STRING);
    expect(f!.value).toBe('testConstVal');
    expect(service.getConstantType('testConst')).toBe(FieldType.STRING);
    expect(service.getConstantTypeIndex('testConst')).toBe(0);
    service.editConstant(
      'testConstMod',
      'testConstValMod',
      'STRING',
      'testConst'
    );
    expect(cfg.constantDoc.fields.length).toBe(1);
    const fm = cfg.constantDoc.getField('/testConstMod');
    expect(fm).toBeTruthy();
    expect(fm!.name).toBe('testConstMod');
    expect(fm!.path).toBe('/testConstMod');
    expect(fm!.type).toBe(FieldType.STRING);
    expect(fm!.value).toBe('testConstValMod');
    service.deleteConstant('testConstMod');
    expect(cfg.constantDoc.fields.length).toBe(0);
  });

  test('Property field', () => {
    cfg.mappings = new MappingDefinition();
    expect(cfg.sourcePropertyDoc.fields.length).toBe(0);
    expect(cfg.targetPropertyDoc.fields.length).toBe(0);
    service.createProperty('testProp', 'STRING', 'current', true, false);
    expect(cfg.sourcePropertyDoc.fields.length).toBe(1);
    expect(cfg.targetPropertyDoc.fields.length).toBe(0);
    const f = cfg.sourcePropertyDoc.getField('/current/testProp');
    expect(f).toBeTruthy();
    expect(f!.name).toBe('testProp');
    expect(f!.scope).toBe('current');
    expect(f!.path).toBe('/current/testProp');
    expect(f!.type).toBe(FieldType.STRING);
    expect(service.getPropertyType('testProp', 'current', true)).toBe(
      FieldType.STRING
    );
    expect(service.getPropertyTypeIndex('testProp', 'current', true)).toBe(0);
    service.editProperty(
      'testProp',
      'STRING',
      'current',
      true,
      'testPropMod',
      'currentMod'
    );
    expect(cfg.sourcePropertyDoc.fields.length).toBe(1);
    expect(cfg.targetPropertyDoc.fields.length).toBe(0);
    const fm = cfg.sourcePropertyDoc.getField('/currentMod/testPropMod');
    expect(fm).toBeTruthy();
    expect(fm!.name).toBe('testPropMod');
    expect(fm!.scope).toBe('currentMod');
    expect(fm!.path).toBe('/currentMod/testPropMod');
    expect(fm!.type).toBe(FieldType.STRING);
    service.deleteProperty('testPropMod', 'currentMod', true);
    expect(cfg.sourcePropertyDoc.fields.length).toBe(0);
    expect(cfg.targetPropertyDoc.fields.length).toBe(0);
  });
});
