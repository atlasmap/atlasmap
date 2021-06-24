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
import { ErrorInfo, ErrorScope, ErrorType } from '../models/error.model';
import { LookupTable, LookupTableEntry } from '../models/lookup-table.model';

import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { MappingDefinition } from '../models/mapping-definition.model';
import { MappingModel } from '../models/mapping.model';
import { TransitionMode } from '../models/transition.model';

const EnumerationUnspecified = '[ None ]';

/**
 * Lookup table structure.
 */
export class LookupTableData {
  sourceEnumValue: string;
  targetEnumValues: string[];
  selectedTargetEnumValue: string;
}

/**
 * Static lookup table utility methods.
 */
export class LookupTableUtil {
  static populateMappingLookupTable(
    mappingDefinition: MappingDefinition,
    m: MappingModel
  ): void {
    if (
      !(
        m.transition.mode === TransitionMode.ENUM &&
        !m.transition.lookupTableName &&
        m.getFields(true).length === 1 &&
        m.getFields(false).length === 1
      )
    ) {
      return;
    }
    let inputIdentifier: string | undefined;
    let outputIdentifier: string | undefined;

    const inputField: Field = m.getFields(true)[0];
    if (inputField) {
      inputIdentifier = inputField.name + '-' + inputField.docDef.id;
    }
    const outputField: Field = m.getFields(true)[0];
    if (outputField) {
      outputIdentifier = outputField.name + '-' + outputField.docDef.id;
    }
    if (inputIdentifier && outputIdentifier) {
      let table: LookupTable = mappingDefinition.getTableBySourceTarget(
        inputIdentifier,
        outputIdentifier
      );
      if (!table) {
        table = new LookupTable();
        table.sourceIdentifier = inputIdentifier;
        table.targetIdentifier = outputIdentifier;
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
      const m: MappingModel = LookupTableUtil.getFirstMappingForLookupTable(
        mappingDefinition,
        t.name
      );
      if (m && m.transition.lookupTableName) {
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

  private static getFirstMappingForLookupTable(
    mappingDefinition: MappingDefinition,
    lookupTableName: string
  ): MappingModel {
    // TODO: check this non null operator
    return mappingDefinition.mappings.find(
      (m) => m.transition.lookupTableName === lookupTableName
    )!;
  }

  private static errorNoTable(cfg: ConfigModel, mapping: MappingModel) {
    cfg.errorService.addError(
      new ErrorInfo({
        message:
          'Could not find enumeration lookup table ' +
          mapping.transition?.lookupTableName +
          ' for mapping.',
        scope: ErrorScope.MAPPING,
        type: ErrorType.INTERNAL,
        mapping: mapping,
      })
    );
  }

  static getEnumerationValues(
    cfg: ConfigModel,
    mapping: MappingModel
  ): LookupTableData[] {
    if (!cfg || !cfg.mappings || !mapping) {
      return [];
    }
    const targetField: Field = mapping.getFields(false)[0];
    const targetValues: string[] = [];
    targetValues.push('[ None ]');
    for (const e of targetField.enumValues) {
      targetValues.push(e.name);
    }

    const table = cfg.mappings.getTableByName(
      mapping.transition?.lookupTableName!
    );
    if (!table) {
      LookupTableUtil.errorNoTable(cfg, mapping);
      return [];
    }

    const enumVals: LookupTableData[] = [];
    const sourceField: Field = mapping.getFields(true)[0];
    for (const sourceEnum of sourceField.enumValues) {
      const tableData: LookupTableData = new LookupTableData();
      tableData.sourceEnumValue = sourceEnum.name;
      tableData.targetEnumValues = targetValues;
      const selected: LookupTableEntry | null = table.getEntryForSource(
        tableData.sourceEnumValue,
        false
      );
      tableData.selectedTargetEnumValue =
        selected == null ? EnumerationUnspecified : selected.targetValue;
      enumVals.push(tableData);
    }
    return enumVals;
  }

  static updateEnumerationValues(
    cfg: ConfigModel,
    mapping: MappingModel,
    enumerationValues: LookupTableData[]
  ) {
    if (!cfg || !cfg.mappings || !mapping) {
      return;
    }
    const table = cfg.mappings.getTableByName(
      mapping.transition?.lookupTableName!
    );
    if (!table) {
      LookupTableUtil.errorNoTable(cfg, mapping);
      return;
    }
    table.lookupEntry = [];
    for (const enumValue of enumerationValues) {
      if (enumValue.selectedTargetEnumValue === EnumerationUnspecified) {
        continue;
      }
      const lte: LookupTableEntry = new LookupTableEntry();
      lte.sourceValue = enumValue.sourceEnumValue;
      lte.targetValue = enumValue.selectedTargetEnumValue;
      table.lookupEntry.push(lte);
    }
  }
}
