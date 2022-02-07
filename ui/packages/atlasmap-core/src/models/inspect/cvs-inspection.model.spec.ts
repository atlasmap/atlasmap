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

import { ConfigModel, DocumentDefinition } from '..';
import { CsvInspectionModel } from './csv-inspection.model';

describe('CsvInspectionModel', () => {
  test('Duplicate document URI search parameters', () => {
    const cfg = new ConfigModel();
    const doc = new DocumentDefinition();
    const cvs = new CsvInspectionModel(cfg, doc);

    expect(cvs.doc.uri).toBeUndefined();

    const response = {
      CsvDocument: {
        fields: {
          field: {},
        },
      },
    };

    doc.uri = 'base:';
    doc.inspectionParameters = {
      a: '1',
      b: '2',
    };
    cvs.parseResponse(response);
    expect(cvs.doc.uri).toBe('base:?a=1&b=2');

    doc.inspectionParameters = {
      c: '3',
      d: '4',
    };

    cvs.parseResponse(response);
    expect(cvs.doc.uri).toBe('base:?c=3&d=4');
  });
});
