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
import { NamespaceModel } from '../models/document-definition.model';
import { NamespaceEditComponent } from './namespace-edit.component';
import { ModalWindowComponent } from './modal-window.component';
import { DataMapperUtil } from '../common/data-mapper-util';

@Component({
  selector: 'namespace-list',
  templateUrl: './namespace-list.component.html',
})

export class NamespaceListComponent {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;

  searchMode = false;
  private searchFilter = '';
  private selectedNamespace: NamespaceModel = null;
  private searchResults: NamespaceModel[] = [];

  getNamespaceCSSClass(namespace: NamespaceModel, index: number): string {
    let cssClass = 'item itemRow ';
    cssClass += (index % 2 === 1) ? ' even' : '';
    if (namespace === this.selectedNamespace) {
      cssClass += ' active';
    }
    return cssClass;
  }

  searchResultsVisible(): boolean {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      return false;
    }
    return (this.searchResults.length === 0);
  }

  selectNamespace(ns: NamespaceModel): void {
    this.selectedNamespace = (this.selectedNamespace === ns) ? null : ns;
  }

  getItemsCSSClass(): string {
    return 'items namespaces' + (this.searchMode ? ' searchShown' : '');
  }

  getRowTitleCSSClass(): string {
    return this.searchMode ? 'rowTitles searchShown' : 'rowTitles';
  }

  getRowsCSSClass(): string {
    return this.searchMode ? 'rows searchShown' : 'rows';
  }

  getNamespaces(): NamespaceModel[] {
    return this.searchMode ? this.searchResults : this.cfg.getFirstXmlDoc(false).namespaces;
  }

  addEditNamespace(ns: NamespaceModel, event: any): void {
    event.stopPropagation();
    const isEditMode = (ns != null);
    if (!isEditMode) {
      ns = new NamespaceModel();
      ns.createdByUser = true;
    }
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Save';
    this.modalWindow.headerText = (ns == null) ? 'Add Namespace' : 'Edit Namespace';
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const namespaceComponent: NamespaceEditComponent = mw.nestedComponent as NamespaceEditComponent;
      namespaceComponent.initialize(ns, this.cfg.getFirstXmlDoc(false).namespaces);
    };
    this.modalWindow.nestedComponentType = NamespaceEditComponent;
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      const namespaceComponent: NamespaceEditComponent = mw.nestedComponent as NamespaceEditComponent;
      const newNamespace: NamespaceModel = namespaceComponent.namespace;
      if (isEditMode) {
        ns.copyFrom(newNamespace);
      } else {
        this.cfg.getFirstXmlDoc(false).namespaces.push(newNamespace);
      }
      this.search(this.searchFilter);
      this.cfg.mappingService.notifyMappingUpdated();
    };
    this.modalWindow.show();
  }

  toggleSearch(): void {
    this.searchMode = !this.searchMode;
    this.search(this.searchFilter);
  }

  getSearchIconCSSClass(): string {
    const cssClass = 'fa fa-search searchBoxIcon link';
    return this.searchMode ? (cssClass + ' selectedIcon') : cssClass;
  }

  namespaceMatchesSearch(ns: NamespaceModel): boolean {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      return true;
    }
    const filter: string = this.searchFilter.toLowerCase();
    if (ns.isTarget && ('tns'.includes(filter) || 'target'.includes(filter))) {
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

  removeNamespace(ns: NamespaceModel, event: any): void {
    event.stopPropagation();
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Remove';
    this.modalWindow.headerText = 'Remove Namespace?';
    this.modalWindow.message = 'Are you sure you want to remove \'' + ns.alias + '\' ?';
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      DataMapperUtil.removeItemFromArray(ns, this.cfg.getFirstXmlDoc(false).namespaces);
      this.selectedNamespace = null;
      this.search(this.searchFilter);
    };
    this.modalWindow.show();
  }

  private search(searchFilter: string): void {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      this.searchResults = [].concat(this.cfg.getFirstXmlDoc(false).namespaces);
      return;
    }

    this.searchFilter = searchFilter;
    this.searchResults = [];
    for (const ns of this.cfg.getFirstXmlDoc(false).namespaces) {
      if (this.namespaceMatchesSearch(ns)) {
        this.searchResults.push(ns);
      } else if (this.selectedNamespace != null) {
        this.selectNamespace = null;
      }
    }
  }

}
