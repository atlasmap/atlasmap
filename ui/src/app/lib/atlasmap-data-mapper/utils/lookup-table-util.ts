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

import { MappingModel } from '../models/mapping.model';
import { TransitionMode } from '../models/transition.model';
import { Field } from '../models/field.model';
import { LookupTable } from '../models/lookup-table.model';
import { MappingDefinition } from '../models/mapping-definition.model';

/**
 * Static routines for handling LookupTable.
 */
export class LookupTableUtil {

  static populateMappingLookupTable(mappingDefinition: MappingDefinition, m: MappingModel): void {
    if (!(m.transition.mode === TransitionMode.ENUM
      && m.transition.lookupTableName == null
      && m.getFields(true).length === 1
      && m.getFields(false).length === 1)) {
      return;
    }
    let inputClassIdentifier: string = null;
    let outputClassIdentifier: string = null;

    const inputField: Field = m.getFields(true)[0];
    if (inputField) {
      inputClassIdentifier = inputField.classIdentifier;
    }
    const outputField: Field = m.getFields(true)[0];
    if (outputField) {
      outputClassIdentifier = outputField.classIdentifier;
    }
    if (inputClassIdentifier && outputClassIdentifier) {
      let table: LookupTable = mappingDefinition.getTableBySourceTarget(inputClassIdentifier, outputClassIdentifier);
      if (table == null) {
        table = new LookupTable();
        table.sourceIdentifier = inputClassIdentifier;
        table.targetIdentifier = outputClassIdentifier;
        mappingDefinition.addTable(table);
        m.transition.lookupTableName = table.name;
      } else {
        m.transition.lookupTableName = table.name;
      }
    }
  }

  static updateLookupTables(mappingDefinition: MappingDefinition) {
    for (const t of mappingDefinition.getTables()) {
      if (t.sourceIdentifier && t.targetIdentifier) {
        continue;
      }
      const m: MappingModel = LookupTableUtil.getFirstMappingForLookupTable(mappingDefinition, t.name);
      if (m != null && m.transition.lookupTableName != null) {
        if (!t.sourceIdentifier) {
          const inputField: Field = m.getFields(true)[0];
          if (inputField) {
            t.sourceIdentifier = inputField.classIdentifier;
          }
        }
        if (!t.targetIdentifier) {
          const outputField: Field = m.getFields(false)[0];
          if (outputField) {
            t.targetIdentifier = outputField.classIdentifier;
          }
        }
      }
    }
    for (const m of mappingDefinition.mappings) {
      LookupTableUtil.populateMappingLookupTable(mappingDefinition, m);
    }
  }

  private static getFirstMappingForLookupTable(mappingDefinition: MappingDefinition, lookupTableName: string): MappingModel {
    for (const m of mappingDefinition.mappings) {
      if (m.transition.lookupTableName === lookupTableName) {
        return m;
      }
    }
    return null;
  }

}
