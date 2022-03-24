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

import { CommonUtil } from '../utils/common-util';
import { DocumentDefinition } from './document-definition.model';
import { Field } from './field.model';
import { LookupTable } from './lookup-table.model';
import { MappingModel } from './mapping.model';
import { TransitionMode } from './transition.model';

export class MappingDefinition {
  name: string | null = null;
  version: string | null = null;
  mappings: MappingModel[] = [];
  activeMapping: MappingModel | null = null;
  parsedDocs: DocumentDefinition[] = [];
  templateText: string | null = null;

  private tables: LookupTable[] = [];
  private tablesBySourceTargetKey: { [key: string]: LookupTable } = {};
  private tablesByName: { [key: string]: LookupTable } = {};

  constructor() {
    // Mapping definition ID must be 0 until https://github.com/atlasmap/atlasmap/issues/1577
    // this.name = 'UI.' + Math.floor((Math.random() * 1000000) + 1).toString();
    this.name = 'UI.0';
  }

  templateExists(): boolean {
    return this.templateText != null && this.templateText !== '';
  }

  addTable(table: LookupTable): void {
    this.tablesBySourceTargetKey[table.getInputOutputKey()] = table;
    this.tablesByName[table.name] = table;
    this.tables.push(table);
  }

  getTableByName(name: string): LookupTable {
    return this.tablesByName[name];
  }

  getTableBySourceTarget(
    sourceIdentifier: string,
    targetIdentifier: string
  ): LookupTable {
    const key: string = sourceIdentifier + ':' + targetIdentifier;
    return this.tablesBySourceTargetKey[key];
  }

  getTables(): LookupTable[] {
    const tables: LookupTable[] = [];
    for (const key in this.tablesByName) {
      if (!this.tablesByName.hasOwnProperty(key)) {
        continue;
      }
      const table: LookupTable = this.tablesByName[key];
      tables.push(table);
    }
    return tables;
  }

  removeTableByName(name: string) {
    if (name) {
      const table = this.tablesByName[name];
      const iokey = table.getInputOutputKey();
      if (this.tablesByName[name]) {
        delete this.tables[this.tables.indexOf(table)];
        delete this.tablesByName[name];
        delete this.tablesBySourceTargetKey[iokey];
      }
    }
  }

  getAllMappings(includeActiveMapping: boolean): MappingModel[] {
    const mappings: MappingModel[] = [...this.mappings];
    if (includeActiveMapping) {
      if (this.activeMapping == null) {
        return mappings;
      }
      for (const mapping of mappings) {
        if (mapping === this.activeMapping) {
          return mappings;
        }
      }
      mappings.push(this.activeMapping);
    }
    return mappings;
  }

  findMappingsForField(field: Field): MappingModel[] {
    const mappingsForField: MappingModel[] = [];
    for (const m of this.mappings) {
      if (m.isFieldMapped(field)) {
        mappingsForField.push(m);
      }
    }
    return mappingsForField;
  }

  removeMapping(m: MappingModel): boolean {
    if (m.transition.mode === TransitionMode.ENUM) {
      this.removeTableByName(m.transition.lookupTableName!);
    }
    return CommonUtil.removeItemFromArray(m, this.mappings);
  }
}
