
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

import { Component, Input, ViewChildren, ElementRef, QueryList,  } from '@angular/core';

import { LookupTable, LookupTableEntry } from '../../models/lookup.table.model';
import { ConfigModel } from '../../models/config.model';
import { Field, EnumValue } from '../../models/field.model';
import { MappingModel, FieldMappingPair } from '../../models/mapping.model';

export class LookupTableData {
    sourceEnumValue: string;
    targetEnumValues: string[];
    selectedTargetEnumValue: string;
}

@Component({
    selector: 'lookup-table',
    template: `
        <div class="LookupTableComponent" *ngIf="data">
            <div class="lookupTableRow" *ngFor="let d of data">
                <label>{{ d.sourceEnumValue }}</label>
                <select #outputSelect [ngModel]="d.selectedTargetEnumValue" [attr.sourceValue]="d.sourceEnumValue">
                    <option *ngFor="let targetEnumValue of d.targetEnumValues" [ngValue]="targetEnumValue"
                        [attr.enumvalue]="targetEnumValue">{{ targetEnumValue }}
                    </option>
                </select>
            </div>
        </div>
    `
})

export class LookupTableComponent {
    public fieldPair: FieldMappingPair;

    table: LookupTable;
    data: LookupTableData[];

    @ViewChildren('outputSelect') outputSelects: QueryList<ElementRef>;

    public initialize(cfg: ConfigModel, fieldPair: FieldMappingPair): void {
        this.fieldPair = fieldPair;

        var targetField: Field = fieldPair.getFields(false)[0];
        var targetValues: string[] = [];
        targetValues.push("[ None ]");
        for (let e of targetField.enumValues) {
            targetValues.push(e.name);
        }

        this.table = cfg.mappings.getTableByName(fieldPair.transition.lookupTableName);
        if (this.table == null) {
            console.error("Could not find enum lookup table for mapping.", fieldPair);
        }

        var d: LookupTableData[] = [];
        var sourceField: Field = fieldPair.getFields(true)[0];
        for (let e of sourceField.enumValues) {
            var tableData: LookupTableData = new LookupTableData();
            tableData.sourceEnumValue = e.name;
            tableData.targetEnumValues = [].concat(targetValues);
            var selected: LookupTableEntry = this.table.getEntryForSource(tableData.sourceEnumValue, false);
            tableData.selectedTargetEnumValue = (selected == null) ? "[ None ]" : selected.targetValue;
            d.push(tableData);
        }
        this.data = d;
    }

    public saveTable(): void {
        this.table.entries = [];
        for (let c of this.outputSelects.toArray()) {
            var selectedOptions: any[] = c.nativeElement.selectedOptions;
            if (selectedOptions && selectedOptions.length) {
                var targetValue: string = selectedOptions[0].label;
                if (targetValue == "[ None ]") {
                    continue;
                }
                var e: LookupTableEntry = new LookupTableEntry();
                e.sourceValue = c.nativeElement.attributes["sourceValue"].value;
                e.targetValue = targetValue;
                this.table.entries.push(e);
            }
        }
        console.log("Saved table.", this.table.toString());
    }
}

