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

import { Component, Input } from '@angular/core';

import { TransitionModel, TransitionMode, TransitionDelimiter } from '../../models/transition.model';
import { ConfigModel } from '../../models/config.model';
import { Field, EnumValue } from '../../models/field.model';
import { LookupTable, LookupTableEntry } from '../../models/lookup.table.model';
import { MappingModel, FieldMappingPair } from '../../models/mapping.model';

import { ModalWindowComponent } from '../modal.window.component';
import { LookupTableComponent } from './lookup.table.component';

@Component({
    selector: 'transition-selector',
    template: `
        <div class="mappingFieldContainer TransitionSelector" *ngIf="cfg.mappings.activeMapping">
            <div class="MappingFieldSection">
                <div *ngIf="modeIsEnum()" class="enumSection">
                    <label>{{ getMappedValueCount() }} values mapped</label>
                    <i class="fa fa-edit link" (click)="showLookupTable()"></i>
                </div>
                <div *ngIf="!modeIsEnum()">
                    <label>Action</label>
                    <select (change)="selectionChanged($event);" selector="mode"
                        [ngModel]="fieldPair.transition.mode">
                        <option value="{{modes.COMBINE}}">Combine</option>
                        <option value="{{modes.MAP}}">Map</option>
                        <option value="{{modes.SEPARATE}}">Separate</option>
                    </select>
                    <div class="clear"></div>
                </div>
                <div *ngIf="fieldPair.transition.isSeparateMode() || fieldPair.transition.isCombineMode()" style="margin-top:10px;">
                    <label>Separator:</label>
                    <select (change)="selectionChanged($event);" selector="separator"
                        [ngModel]="fieldPair.transition.delimiter">
                        <option value="{{delimeters.COLON}}">Colon</option>
                        <option value="{{delimeters.COMMA}}">Comma</option>
                        <option value="{{delimeters.MULTISPACE}}">Multispace</option>
                        <option value="{{delimeters.SPACE}}">Space</option>
                    </select>
                </div>
            </div>
        </div>
    `
})

export class TransitionSelectionComponent {
    @Input() cfg: ConfigModel;
    @Input() modalWindow: ModalWindowComponent;
    @Input() fieldPair: FieldMappingPair;

    private modes: any = TransitionMode;
    private delimeters: any = TransitionDelimiter;

    private modeIsEnum(): boolean {
        return this.fieldPair.transition.isEnumerationMode();
    }

    private modeIsCombine(): boolean {
        return this.fieldPair.transition.isCombineMode();
    }

    private getMappedValueCount(): number {
        var tableName: string = this.fieldPair.transition.lookupTableName;
        if (tableName == null) {
            return 0;
        }
        var table: LookupTable = this.cfg.mappings.getTableByName(tableName);
        if (!table || !table.entries) {
            return 0;
        }
        return table.entries.length;
    }

    selectionChanged(event: any): void {
        var selectorIsMode: boolean = "mode" == event.target.attributes.getNamedItem("selector").value
        var selectedValue: any = event.target.selectedOptions.item(0).attributes.getNamedItem("value").value;
        if (selectorIsMode) {
            this.fieldPair.transition.mode = parseInt(selectedValue);
        } else {
            this.fieldPair.transition.delimiter = parseInt(selectedValue);
        }
        this.cfg.mappingService.updateMappedField(this.fieldPair);
    }

    private showLookupTable(): void {
        var mapping: MappingModel = this.cfg.mappings.activeMapping;
        if (!mapping.hasMappedFields(true) || !mapping.hasMappedFields(false)) {
            this.cfg.errorService.warn("Please select source and target fields before mapping values.", null);
            return;
        }
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Finish";
        this.modalWindow.parentComponent = this;
        this.modalWindow.headerText = "Map Enumeration Values";
        this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
            var self: TransitionSelectionComponent = mw.parentComponent as TransitionSelectionComponent;
            var c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
            c.initialize(self.cfg, this.fieldPair);
        };
        this.modalWindow.nestedComponentType = LookupTableComponent;
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            var self: TransitionSelectionComponent = mw.parentComponent as TransitionSelectionComponent;
            var c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
            c.saveTable();
            self.cfg.mappingService.saveCurrentMapping();
        };
        this.modalWindow.show();
    }
}
