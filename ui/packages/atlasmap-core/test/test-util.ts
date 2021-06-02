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

import { ConfigModel } from '../src/models/config.model';
import { DocumentDefinition } from '../src/models/document-definition.model';
import { Field } from '../src/models/field.model';
import { InspectionType } from '../src/common/config.types';
import { MappingDefinition } from '../src/models/mapping-definition.model';
import { MappingModel } from '../src/models/mapping.model';

export class TestUtils {
  /**
   * Deep diff of JSON objects.  Return true if they match, false otherwise.
   */
  static isEqualJSON = function (eObj: any, gObj: any): boolean {
    const expected = Object.keys(eObj);
    const generated = Object.keys(gObj);

    if (expected.length !== generated.length) {
      console.error(
        'JSON object key length error.  Expected: ' +
          expected.length +
          ', Generated: ' +
          generated.length
      );
      return false;
    }

    for (let objKey of generated) {
      if (eObj[objKey] !== gObj[objKey]) {
        if (
          typeof eObj[objKey] === 'object' &&
          typeof gObj[objKey] === 'object'
        ) {
          if (!TestUtils.isEqualJSON(eObj[objKey], gObj[objKey])) {
            console.error(`${objKey} is different`);
            console.info('Expected: ', eObj[objKey]);
            console.error('Generated: ', gObj[objKey]);
            return false;
          }
        }
      }
    }
    return true;
  };

  static createMockDocs(cfg: ConfigModel) {
    const srcDoc = new DocumentDefinition();
    srcDoc.id = 'SourceJson';
    srcDoc.name = 'json source document';
    srcDoc.uri = 'atlas:json:SourceJson';
    srcDoc.inspectionType = InspectionType.SCHEMA;
    srcDoc.inspectionSource = 'dummy schema';
    srcDoc.isSource = true;
    const srcF = new Field();
    srcF.docDef = srcDoc;
    srcF.name = 'sourceField';
    srcF.path = '/sourceField';
    srcF.type = 'STRING';
    srcDoc.addField(srcF);
    const srcF2 = new Field();
    srcF2.docDef = srcDoc;
    srcF2.name = 'sourceField2';
    srcF2.path = '/sourceField2';
    srcF2.type = 'STRING';
    srcDoc.addField(srcF2);
    const srcF3 = new Field();
    srcF3.docDef = srcDoc;
    srcF3.name = 'sourceField3';
    srcF3.path = '/sourceField3';
    srcF3.type = 'STRING';
    srcDoc.addField(srcF3);

    const tgtDoc = new DocumentDefinition();
    tgtDoc.id = 'TargetJson';
    tgtDoc.name = 'json target document';
    tgtDoc.uri = 'atlas:json:TargetJson';
    tgtDoc.inspectionType = InspectionType.SCHEMA;
    tgtDoc.inspectionSource = 'dummy schema';
    tgtDoc.isSource = false;
    const tgtF = new Field();
    tgtF.docDef = tgtDoc;
    tgtF.name = 'targetField';
    tgtF.path = '/targetField';
    tgtF.type = 'STRING';
    tgtDoc.addField(tgtF);
    const tgtF2 = new Field();
    tgtF2.docDef = tgtDoc;
    tgtF2.name = 'targetField2';
    tgtF2.path = '/targetField2';
    tgtF2.type = 'STRING';
    tgtDoc.addField(tgtF2);

    cfg.sourceDocs.push(srcDoc);
    cfg.targetDocs.push(tgtDoc);
  }

  static createMockMappings(cfg: ConfigModel) {
    TestUtils.createMockDocs(cfg);
    const srcDoc = cfg.sourceDocs[0];
    const tgtDoc = cfg.targetDocs[0];
    cfg.mappings = new MappingDefinition();
    const mapping = new MappingModel();
    mapping.cfg = cfg;
    mapping.addField(srcDoc.getField('/sourceField')!, true);
    mapping.addField(tgtDoc.getField('/targetField')!, true);
    cfg.mappings.mappings.push(mapping);
    const mapping2 = new MappingModel();
    mapping2.cfg = cfg;
    mapping2.addField(srcDoc.getField('/sourceField')!, true);
    mapping2.addField(srcDoc.getField('/sourceField2')!, true);
    mapping2.addField(tgtDoc.getField('/targetField2')!, true);
    cfg.mappings.mappings.push(mapping2);
  }
}
