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

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { MappingModel, FieldMappingPair, MappedField } from '../../models/mapping.model';
import { TransitionModel, TransitionMode, TransitionDelimiter } from '../../models/transition.model';
import { DocumentDefinition } from '../../models/document.definition.model';


@Component({
    selector: 'mapping-list-field',
    template: `
        <template #tolTemplate>
            <div class="fieldDetailTooltip" *ngIf="displayParentObject()">
                <label class="parentObjectName">
                    <i [attr.class]="isSource ? 'fa fa-hdd-o' : 'fa fa-download'"></i>
                    {{ getParentObjectName() }}
                </label>
                <label>{{ getFieldPath() }}</label>
                <label *ngIf="displayParentObject() && mappedField.field.type">({{ mappedField.field.type }})</label>
                <div class="clear"></div>
            </div>
        </template>

        <label class="fieldPath" [tooltip]="tolTemplate" placement="right" [isDisabled]="!displayParentObject()">
            {{ getFieldPath() }}
            <i class="fa fa-bolt" *ngIf="mappedField.actions.length != 0"></i>
        </label>
    `
})

export class MappingListFieldComponent {
    @Input() mappedField: MappedField;
    @Input() isSource: boolean;
    @Input() cfg: ConfigModel;

    public getSourceIconCSSClass(): string {
        return this.isSource ? 'fa fa-hdd-o' : 'fa fa-download';
    }

    public getFieldPath(): string {
        if (this.mappedField == null || this.mappedField.field == null
            || (this.mappedField.field == DocumentDefinition.getNoneField())) {
            return "[None]";
        }
        return this.mappedField.field.getFieldLabel(true);
    }

    public displayParentObject(): boolean {
        if (this.mappedField == null || this.mappedField.field == null 
            || this.mappedField.field.docDef == null
            || (this.mappedField.field == DocumentDefinition.getNoneField())) {
            return false;
        }
        return true;
    }   

    public getParentObjectName() {
        if (this.mappedField == null || this.mappedField.field == null || this.mappedField.field.docDef == null) {
            return "";
        }
        return this.mappedField.field.docDef.getName(true);
    }
}

@Component({
    selector: 'mapping-list',
    template: `
        <div class="dataMapperItemList mappingList">
            <div class="card-pf">
                <div class="card-pf-heading">
                    <h2 class="card-pf-title">
                        <div class="name">
                        <i class="fa fa-table"></i>
                            <label>Mappings</label>
                        </div>
                        <i (click)="toggleSearch()" [attr.class]="getSearchIconCSSClass()"></i>
                        <div class="clear"></div>
                    </h2>
                    <div class="searchHeaderWrapper">
                        <div *ngIf="searchMode" class="searchBox">
                            <input type="text" #searchFilterBox id="search-filter-box" [(ngModel)]="searchFilter"
                                (keyup)="search(searchFilterBox.value)" placeholder="Search" />
                            <i class="fa fa-close searchBoxCloseIcon link" (click)="clearSearch()"></i>
                            <div class="clear"></div>
                        </div>
                        <div [attr.class]="getRowTitleCSSClass()">
                            <label class="sources"><i class="fa fa-hdd-o"></i>Sources</label>
                            <label class="targets"><i class="fa fa-download"></i>Targets</label>
                            <label class="type"><i class="fa fa-sliders"></i>Type</label>
                            <div class="clear"></div>
                        </div>
                        <div class="clear"></div>
                    </div>
                </div>
                <div [attr.class]="getItemsCSSClass()">                    
                    <div [attr.class]="getMappingRowsCSSClass()">
                        <div *ngFor="let mapping of getMappings(); let index=index;"
                            [attr.class]="getMappingCSSClass(mapping, index)" (click)="selectMapping(mapping)">
                            <div *ngFor="let fieldPair of mapping.fieldMappings" class="itemRow">
                                <div class="sourceFieldNames fieldNames">
                                    <mapping-list-field *ngFor="let mappedField of getMappedFields(fieldPair, true)"
                                        [mappedField]="mappedField" [isSource]="true" [cfg]="cfg"></mapping-list-field>
                                    <div class="clear"></div>
                                </div>
                                <div class="targetFieldNames fieldNames">
                                    <mapping-list-field *ngFor="let mappedField of getMappedFields(fieldPair, false)"
                                        [mappedField]="mappedField" [isSource]="false" [cfg]="cfg"></mapping-list-field>
                                    <div class="clear"></div>
                                </div>
                                <div class="transition">
                                    <label>{{ fieldPair.transition.getPrettyName() }}</label>
                                    <div class="clear"></div>
                                </div>
                                <div class="error">
                                    <span class="pficon pficon-error-circle-o"
                                    *ngIf="mapping.validationErrors.length"></span>
                                </div>
                                <div class="clear"></div>
                            </div>
                        </div>
                    </div>
                    <div class="noSearchResults" *ngIf="searchResultsVisible()">
                        <label>No search results.</label>
                        <div class="clear"></div>
                    </div>  
                </div>
                <div class="card-pf-heading itemCount">{{ cfg.mappings.mappings.length }} mappings</div>
                <div class="clear"></div>
            </div>
        </div>
    `
})

export class MappingListComponent {
    @Input() cfg:ConfigModel;

    public searchMode: boolean = false;
    private searchFilter: string = "";
    private searchResults: MappingModel[] = [];

    public getItemsCSSClass(): string {
        return "items mappings" + (this.searchMode ? " searchShown" : "");
    }

    public searchResultsVisible(): boolean {
        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            return false;
        }
        return (this.searchResults.length == 0);
    }

    public getMappingCSSClass(mapping: MappingModel, index: number): string {
        var cssClass: string = "item ";
        cssClass += (index % 2 == 1) ? " even" : "";
        if (mapping == this.cfg.mappings.activeMapping) {
            cssClass += " active";
        }
        return cssClass;
    }

    public selectMapping(mapping: MappingModel): void {
        if (this.cfg.mappings.activeMapping == mapping) {
            this.cfg.mappingService.deselectMapping();
        } else {
            this.cfg.mappingService.selectMapping(mapping);
        }
    }

    public getRowTitleCSSClass(): string {
        return this.searchMode ? "rowTitles searchShown" : "rowTitles"
    }

    public getMappingRowsCSSClass(): string {
        return this.searchMode ? "rows searchShown" : "rows"
    }

    public getMappings(): MappingModel[] {
        return this.searchMode ? this.searchResults : [].concat(this.cfg.mappings.getAllMappings(true));
    }    

    public getMappedFields(fieldPair: FieldMappingPair, isSource: boolean): MappedField[] {
        var fields: MappedField[] = fieldPair.getMappedFields(isSource);
        fields = MappedField.sortMappedFieldsByPath(fields, false);
        if (fields.length == 0) {
            var mappedField: MappedField = new MappedField();
            mappedField.field = DocumentDefinition.getNoneField();
            fields.push(mappedField);
        }
        return fields;
    }

    public toggleSearch(): void  {
        this.searchMode = !this.searchMode;
        this.search(this.searchFilter);
    }

    public getSearchIconCSSClass(): string {
        var cssClass: string = "fa fa-search searchBoxIcon link";
        return this.searchMode ? (cssClass + " selectedIcon") : cssClass;
    }

    private clearSearch(): void  {
        this.searchFilter = "";
        this.search(this.searchFilter);
    }

    private search(searchFilter: string): void {
        this.searchFilter = searchFilter;

        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            this.searchResults = [].concat(this.cfg.mappings.getAllMappings(true));
            return;
        }

        this.searchResults = [];
        for (let mapping of this.cfg.mappings.getAllMappings(true)) {
            for (let fieldPair of mapping.fieldMappings) {
                if (this.fieldPairMatchesSearch(fieldPair)) {
                    this.searchResults.push(mapping);
                    break;
                }
            }    
        }        
    }

    public fieldPairMatchesSearch(fieldPair: FieldMappingPair): boolean {
        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            return true;
        }
        var filter: string = this.searchFilter.toLowerCase();
        var transitionName: string = fieldPair.transition.getPrettyName();
        if (transitionName != null && transitionName.toLowerCase().includes(filter)) {
            return true;
        }
        for (let mappedField of fieldPair.getAllMappedFields()) {
            let field: Field = mappedField.field;
            if (field == null || field.path == null) {
                continue;
            }
            if (field.path.toLowerCase().includes(filter)) {
                return true;
            }
        }
        return false;
    }
}
