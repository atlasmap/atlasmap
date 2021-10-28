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
import { FieldType, InspectionType } from '../src/contracts/common';

import { ConfigModel } from '../src/models/config.model';
import { DocumentDefinition } from '../src/models/document-definition.model';
import { Field } from '../src/models/field.model';
import { MappingDefinition } from '../src/models/mapping-definition.model';
import { MappingModel } from '../src/models/mapping.model';

export class TestUtils {
  /**
   * Deep diff of JSON objects.  Return true if they match, false otherwise.
   */
  static isEqualJSON = function (
    testName: string,
    eObj: any,
    gObj: any
  ): boolean {
    const expected = Object.keys(eObj);
    const generated = Object.keys(gObj);

    if (expected.length !== generated.length) {
      console.error(
        testName +
          ': JSON object key length error.  Expected: ' +
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
          if (!TestUtils.isEqualJSON(testName, eObj[objKey], gObj[objKey])) {
            console.error(testName + `: ${objKey} is different`);
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
    srcF.type = FieldType.STRING;
    srcF.isPrimitive = true;
    srcDoc.addField(srcF);
    const srcF2 = new Field();
    srcF2.docDef = srcDoc;
    srcF2.name = 'sourceField2';
    srcF2.path = '/sourceField2';
    srcF2.type = FieldType.STRING;
    srcF2.isPrimitive = true;
    srcDoc.addField(srcF2);
    const srcF3 = new Field();
    srcF3.docDef = srcDoc;
    srcF3.name = 'sourceField3';
    srcF3.path = '/sourceField3';
    srcF3.type = FieldType.STRING;
    srcF3.isPrimitive = true;
    srcDoc.addField(srcF3);
    const srcCF = new Field();
    srcCF.docDef = srcDoc;
    srcCF.name = 'sourceCollectionField';
    srcCF.path = '/sourceCollectionField<>';
    srcCF.type = FieldType.STRING;
    srcCF.isPrimitive = true;
    srcCF.isCollection = true;
    srcDoc.addField(srcCF);
    const srcCF2 = new Field();
    srcCF2.docDef = srcDoc;
    srcCF2.name = 'sourceCollectionField2';
    srcCF2.path = '/sourceCollectionField2<>';
    srcCF2.type = FieldType.STRING;
    srcCF2.isPrimitive = true;
    srcCF2.isCollection = true;
    srcDoc.addField(srcCF2);

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
    tgtF.type = FieldType.STRING;
    tgtF.isPrimitive = true;
    tgtDoc.addField(tgtF);
    const tgtF2 = new Field();
    tgtF2.docDef = tgtDoc;
    tgtF2.name = 'targetField2';
    tgtF2.path = '/targetField2';
    tgtF2.type = FieldType.STRING;
    tgtF2.isPrimitive = true;
    tgtDoc.addField(tgtF2);
    const tgtF3 = new Field();
    tgtF3.docDef = tgtDoc;
    tgtF3.name = 'targetField3';
    tgtF3.path = '/targetField3';
    tgtF3.type = FieldType.STRING;
    tgtF3.isPrimitive = true;
    tgtDoc.addField(tgtF3);
    const tgtCF = new Field();
    tgtCF.docDef = tgtDoc;
    tgtCF.name = 'targetCollectionField';
    tgtCF.path = '/targetCollectionField<>';
    tgtCF.type = FieldType.STRING;
    tgtCF.isPrimitive = true;
    tgtCF.isCollection = true;
    tgtDoc.addField(tgtCF);
    const tgtCF2 = new Field();
    tgtCF2.docDef = tgtDoc;
    tgtCF2.name = 'targetCollectionField2';
    tgtCF2.path = '/targetCollectionField2<>';
    tgtCF2.type = FieldType.STRING;
    tgtCF2.isPrimitive = true;
    tgtCF2.isCollection = true;
    tgtDoc.addField(tgtCF2);

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
