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
  DocumentDefinition,
  NamespaceModel,
} from '../models/document-definition.model';
import { DocumentType } from '../contracts/common';

describe('DocumentDefinitionModel', () => {
  test('initialize NamespaceModel', () => {
    const nsModel = new NamespaceModel();
    nsModel.uri = 'http://atlasmap.io/xml/test/testNamespace';
    nsModel.alias = 'ns1';
    expect(nsModel.getPrettyLabel()).toBe(
      'ns1 [http://atlasmap.io/xml/test/testNamespace]'
    );
  });

  test('initialize DocumentDefinition', () => {
    const docDef = new DocumentDefinition();
    docDef.name = 'TestDoc';
    docDef.type = DocumentType.JAVA;
    expect(docDef.getName(true)).toEqual('TestDoc (JAVA)');
  });
});
