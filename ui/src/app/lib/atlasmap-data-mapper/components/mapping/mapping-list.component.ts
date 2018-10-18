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
import { DocumentDefinition } from '../../models/document-definition.model';

@Component({
  selector: 'mapping-list',
  templateUrl: './mapping-list.component.html',
})

export class MappingListComponent {
  @Input() cfg: ConfigModel;

  searchMode = false;
  private searchFilter = '';
  private searchResults: MappingModel[] = [];

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

  getMappedFields(fieldPair: FieldMappingPair, isSource: boolean): MappedField[] {
    let fields: MappedField[] = fieldPair.getMappedFields(isSource);
    if (fields.length === 0) {
      const mappedField: MappedField = new MappedField();
      mappedField.field = DocumentDefinition.getNoneField();
      fields.push(mappedField);
    }
    return fields;
  }

  toggleSearch(): void {
    this.searchMode = !this.searchMode;
    this.search(this.searchFilter);
  }

  getSearchIconCSSClass(): string {
    const cssClass = 'fa fa-search searchBoxIcon link';
    return this.searchMode ? (cssClass + ' selectedIcon') : cssClass;
  }

  fieldPairMatchesSearch(fieldPair: FieldMappingPair): boolean {
    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      return true;
    }
    const filter: string = this.searchFilter.toLowerCase();
    const transitionName: string = fieldPair.transition.getPrettyName();
    if (transitionName != null && transitionName.toLowerCase().includes(filter)) {
      return true;
    }
    for (const mappedField of fieldPair.getAllMappedFields()) {
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

  private search(searchFilter: string): void {
    this.searchFilter = searchFilter;

    if (!this.searchMode || this.searchFilter == null || this.searchFilter === '') {
      this.searchResults = [].concat(this.cfg.mappings.getAllMappings(true));
      return;
    }

    this.searchResults = [];
    for (const mapping of this.cfg.mappings.getAllMappings(true)) {
      for (const fieldPair of mapping.fieldMappings) {
        if (this.fieldPairMatchesSearch(fieldPair)) {
          this.searchResults.push(mapping);
          break;
        }
      }
    }
  }

}
