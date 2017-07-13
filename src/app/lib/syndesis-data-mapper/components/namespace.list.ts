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

import { ConfigModel } from '../models/config.model';
import { DocumentDefinition, NamespaceModel } from '../models/document.definition.model';
import { NamespaceEditComponent } from './namespace.edit.component';
import { ModalWindowComponent } from './modal.window.component';
import { DataMapperUtil } from '../common/data.mapper.util';

@Component({
    selector: 'namespace-list',
    template: `
        <div class="dataMapperItemList namespaceList">
            <div class="card-pf">
                <div class="card-pf-heading">
                    <h2 class="card-pf-title">
                        <div class="name">
                            <i class="fa fa-table"></i>
                            <label>Namespaces for {{ cfg.getFirstXmlDoc(false).initCfg.shortIdentifier }}</label>
                        </div>
                        <i (click)="toggleSearch()" [attr.class]="getSearchIconCSSClass()"></i>
                        <i (click)="addEditNamespace(null, $event)" class="fa fa-plus link"></i>
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
                            <label class="alias">Alias</label>
                            <label class="uri">Uri</label>
                            <label class="locationUri">Location URI</label>
                            <div class="clear"></div>
                        </div>
                        <div class="clear"></div>
                    </div>
                </div>
                <div [attr.class]="getItemsCSSClass()">
                    <div [attr.class]="getRowsCSSClass()">
                        <div *ngFor="let namespace of getNamespaces(); let index=index;"
                            [attr.class]="getNamespaceCSSClass(namespace, index)" (click)="selectNamespace(namespace)">
                            <label class="alias">{{namespace.isTarget ? 'Target (tns)' : namespace.alias}}</label>
                            <label class="uri">{{namespace.uri}}</label>
                            <label class="locationUri">{{namespace.locationUri}}</label>
                            <div class="actions" style="float:right">
                                <i class="fa fa-edit link" aria-hidden="true" (click)="addEditNamespace(namespace, $event);"></i>
                                <i class="fa fa-trash link" aria-hidden="true" (click)="removeNamespace(namespace, $event);"></i>
                            </div>
                            <div class="clear"></div>
                        </div>
                    </div>
                    <div class="noSearchResults" *ngIf="searchResultsVisible()">
                        <label>No search results.</label>
                        <div class="clear"></div>
                    </div>
                </div>
                <div class="card-pf-heading itemCount">{{ getNamespaces().length }} namespaces</div>
                <div class="clear"></div>
            </div>
        </div>
    `
})

export class NamespaceListComponent {
    @Input() cfg: ConfigModel;
    @Input() modalWindow: ModalWindowComponent;

    public searchMode: boolean = false;
    private searchFilter: string = "";
    private selectedNamespace: NamespaceModel = null;
    private searchResults: NamespaceModel[] = [];

    public getNamespaceCSSClass(namespace: NamespaceModel, index: number): string {
        var cssClass: string = "item itemRow ";
        cssClass += (index % 2 == 1) ? " even" : "";
        if (namespace == this.selectedNamespace) {
            cssClass += " active";
        }
        return cssClass;
    }

    public searchResultsVisible(): boolean {
        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            return false;
        }
        return (this.searchResults.length == 0);
    }

    public selectNamespace(ns: NamespaceModel): void {
        this.selectedNamespace = (this.selectedNamespace == ns) ? null : ns;
    }

    public getItemsCSSClass(): string {
        return "items namespaces" + (this.searchMode ? " searchShown" : "");
    }

    public getRowTitleCSSClass(): string {
        return this.searchMode ? "rowTitles searchShown" : "rowTitles"
    }

    public getRowsCSSClass(): string {
        return this.searchMode ? "rows searchShown" : "rows"
    }

    public getNamespaces(): NamespaceModel[] {
        return this.searchMode ? this.searchResults : this.cfg.getFirstXmlDoc(false).namespaces;
    }

    public addEditNamespace(ns: NamespaceModel, event: any): void {
        event.stopPropagation();
        var isEditMode = (ns != null);
        if (!isEditMode) {
            ns = new NamespaceModel();
            ns.createdByUser = true;
        }
        var self: NamespaceListComponent = this;
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Save";
        this.modalWindow.parentComponent = this;
        this.modalWindow.headerText = (ns == null) ? "Add Namespace" : "Edit Namespace";
        this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
            var namespaceComponent: NamespaceEditComponent = mw.nestedComponent as NamespaceEditComponent;
            namespaceComponent.initialize(ns, this.cfg.getFirstXmlDoc(false).namespaces);
        };
        this.modalWindow.nestedComponentType = NamespaceEditComponent;
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            var namespaceComponent: NamespaceEditComponent = mw.nestedComponent as NamespaceEditComponent;
            var newNamespace: NamespaceModel = namespaceComponent.namespace;
            if (isEditMode) {
                ns.copyFrom(newNamespace);
            } else {
                this.cfg.getFirstXmlDoc(false).namespaces.push(newNamespace);
            }
            this.search(this.searchFilter);
            self.cfg.mappingService.saveCurrentMapping();
        };
        this.modalWindow.show();
    }

    private removeNamespace(ns: NamespaceModel, event: any): void {
        event.stopPropagation();
        event.stopPropagation();
        var self: NamespaceListComponent = this;
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Remove";
        this.modalWindow.parentComponent = this;
        this.modalWindow.headerText = "Remove Namespace?"
        this.modalWindow.message = "Are you sure you want to remove '" + ns.alias + "' ?";
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            DataMapperUtil.removeItemFromArray(ns, this.cfg.getFirstXmlDoc(false).namespaces);
            this.selectedNamespace = null;
            this.search(this.searchFilter);
        };
        this.modalWindow.show();
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
        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            this.searchResults = [].concat(this.cfg.getFirstXmlDoc(false).namespaces);
            return;
        }

        this.searchFilter = searchFilter;
        this.searchResults = [];
        for (let ns of this.cfg.getFirstXmlDoc(false).namespaces) {
            if (this.namespaceMatchesSearch(ns)) {
                this.searchResults.push(ns);
            } else if (this.selectedNamespace != null) {
                this.selectNamespace = null;
            }
        }
    }

    public namespaceMatchesSearch(ns: NamespaceModel): boolean {
        if (!this.searchMode || this.searchFilter == null || this.searchFilter == "") {
            return true;
        }
        var filter: string = this.searchFilter.toLowerCase();
        if (ns.isTarget && ("tns".includes(filter) || "target".includes(filter))) {
            return true;
        }
        if (ns.alias != null && ns.alias.toLowerCase().includes(filter)) {
            return true;
        }
        if (ns.uri != null && ns.uri.toLowerCase().includes(filter)) {
            return true;
        }
        if (ns.locationUri != null && ns.locationUri.toLowerCase().includes(filter)) {
            return true;
        }
        return false;
    }


}
