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

import { Component, Input, OnInit, OnDestroy } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { MappingModel, MappedField } from '../../models/mapping.model';
import { ErrorInfo, ErrorScope, ErrorType } from '../../models/error.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'mapping-list',
  templateUrl: './mapping-list.component.html',
})

export class MappingListComponent implements OnInit, OnDestroy {
  @Input() cfg: ConfigModel;

  searchMode = false;
  searchFilter = '';
  validationErrors = new Map<MappingModel, ErrorInfo[]>();
  private searchResults: MappingModel[] = [];
  private errorSubscription: Subscription;

  ngOnInit() {
    this.cfg.mappingService.notifyMappingUpdated();
    this.storeErrors(this.cfg.errorService.getErrors());
    this.errorSubscription = this.cfg.errorService.subscribe(errors => {
      this.storeErrors(errors);
    });
  }

  ngOnDestroy() {
    if (this.errorSubscription) {
      this.errorSubscription.unsubscribe();
    }
  }

  getItemsCSSClass(): string {
    return 'items mappings' + (this.searchMode ? ' searchShown' : '');
  }

  searchResultsVisible(): boolean {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      return false;
    }
    return (this.searchResults.length === 0);
  }

  getMappingCSSClass(mapping: MappingModel, index: number): string {
    let cssClass = 'item ';
    cssClass += (index % 2 === 1) ? ' even' : '';
    if (mapping === this.cfg.mappings.activeMapping) {
      cssClass += ' active';
    }
    return cssClass;
  }

  selectMapping(mapping: MappingModel): void {
    this.cfg.mappingService.selectMapping(mapping);
  }

  getRowTitleCSSClass(): string {
    return this.searchMode ? 'rowTitles searchShown' : 'rowTitles';
  }

  getMappingRowsCSSClass(): string {
    return this.searchMode ? 'rows searchShown' : 'rows';
  }

  getMappings(): MappingModel[] {
    return this.searchMode ? this.searchResults : [].concat(this.cfg.mappings.getAllMappings(true));
  }

  getMappedFields(mapping: MappingModel, isSource: boolean): MappedField[] {
    return mapping.getUserMappedFields(isSource);
  }

  toggleSearch(): void {
    this.searchMode = !this.searchMode;
    this.search(this.searchFilter);
  }

  getSearchIconCSSClass(): string {
    const cssClass = 'fa fa-search searchBoxIcon link';
    return this.searchMode ? (cssClass + ' selectedIcon') : cssClass;
  }

  fieldPairMatchesSearch(mapping: MappingModel): boolean {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      return true;
    }
    const filter: string = this.searchFilter.toLowerCase();
    const transitionName: string = mapping.transition.getPrettyName();
    if (transitionName != null && transitionName.toLowerCase().includes(filter)) {
      return true;
    }
    for (const mappedField of mapping.getAllMappedFields()) {
      const field: Field = mappedField.field;
      if (field == null || field.path == null) {
        continue;
      }
      if (field.path.toLowerCase().includes(filter)) {
        return true;
      }
    }
    return false;
  }

  isActiveMapping(mapping: MappingModel): boolean {
    return this.cfg.mappings.activeMapping === mapping;
  }

  private search(searchFilter: string): void {
    this.searchFilter = searchFilter;

    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      this.searchResults = [].concat(this.cfg.mappings.getAllMappings(true));
      return;
    }

    this.searchResults = [];
    for (const mapping of this.cfg.mappings.getAllMappings(true)) {
      if (this.fieldPairMatchesSearch(mapping)) {
        this.searchResults.push(mapping);
        break;
      }
    }
  }

  private storeErrors(errors: ErrorInfo[]) {
    this.validationErrors = new Map<MappingModel, ErrorInfo[]>();
    errors.forEach(e => {
      if (e.scope !== ErrorScope.MAPPING || e.type !== ErrorType.VALIDATION || !e.mapping) {
        return;
      }
      if (!this.validationErrors.has(e.mapping)) {
        this.validationErrors.set(e.mapping, []);
      }
      this.validationErrors.get(e.mapping).push(e);
    });
  }
}
