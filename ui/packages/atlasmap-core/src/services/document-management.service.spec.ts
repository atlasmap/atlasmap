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
import { DocumentType, InspectionType } from '../common/config.types';

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { DocumentManagementService } from '../services/document-management.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { Field } from '../models/field.model';
import atlasmapInspectionComplexObjectRootedJson from '../../../../test-resources/inspected/atlasmap-inspection-complex-object-rooted.json';
import atlasmapInspectionPoExampleSchemaJson from '../../../../test-resources/inspected/atlasmap-inspection-po-example-schema.json';
import atlasmapInspectionTwitter4jStatusJson from '../../../../test-resources/inspected/atlasmap-inspection-twitter4j.Status.json';
import ky from 'ky/umd';
import log from 'loglevel';

describe('DocumentManagementService', () => {
  const api = ky.create({ headers: { 'ATLASMAP-XSRF-TOKEN': 'awesome' } });
  const service = new DocumentManagementService(api);

  beforeEach(() => {
    service.cfg = new ConfigModel();
    service.cfg.errorService = new ErrorHandlerService();
    service.cfg.logger = log.getLogger('config');
  });

  test('parse Java inspection', () => {
    const docDef = new DocumentDefinition();
    docDef.type = DocumentType.JAVA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionTwitter4jStatusJson
    );
    service.fetchDocument(docDef).subscribe((answer: DocumentDefinition) => {
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
    });
  });

  test('parse JSON inspection', () => {
    const docDef = new DocumentDefinition();
    docDef.type = DocumentType.JSON;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionComplexObjectRootedJson
    );
    service.fetchDocument(docDef).subscribe((answer: DocumentDefinition) => {
      expect(answer.fields.length).toBe(1);
      expect(answer.fields[0].name).toBe('order');
      const order = answer.getField('/order');
      expect(order?.name).toBe('order');
      expect(order?.type).toBe('COMPLEX');
      expect(order?.children).toBeTruthy();
      expect(order?.children?.length).toBe(3);
    });
  });

  test('parse XML inspection', () => {
    const docDef = new DocumentDefinition();
    docDef.type = DocumentType.XML;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionPoExampleSchemaJson
    );
    service.fetchDocument(docDef).subscribe((answer: DocumentDefinition) => {
      expect(answer.fields.length).toBe(1);
      expect(answer.fields[0].name).toBe('purchaseOrder');
      const purchaseOrder = answer.getField('/tns:purchaseOrder');
      expect(purchaseOrder?.name).toBe('purchaseOrder');
      expect(purchaseOrder?.type).toBe('COMPLEX');
      expect(purchaseOrder?.children).toBeTruthy();
      expect(purchaseOrder?.children?.length).toBe(5);
    });
  });

  test('pick up one XML root element', () => {
    const docDef = new DocumentDefinition();
    docDef.type = DocumentType.XML;
    docDef.inspectionType = InspectionType.SCHEMA;
    docDef.inspectionResult = JSON.stringify(
      atlasmapInspectionPoExampleSchemaJson
    );
    docDef.selectedRoot = 'purchaseOrder';
    service.fetchDocument(docDef).subscribe((answer: DocumentDefinition) => {
      expect(answer.fields.length).toBe(1);
      expect(answer.fields[0].name).toBe('purchaseOrder');
    });

    const docDef2 = new DocumentDefinition();
    docDef2.type = DocumentType.XML;
    docDef2.inspectionType = InspectionType.SCHEMA;
    docDef2.inspectionResult = docDef.inspectionResult;
    docDef2.selectedRoot = 'comment';
    service.fetchDocument(docDef2).subscribe((answer) => {
      expect(answer.fields.length).toBe(1);
      expect(answer.fields[0].name).toBe('comment');
    });
  });
});
