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

import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../../models/mapping.model';
import { FieldAction, FieldActionConfig, TransitionMode, TransitionModel } from '../../models/transition.model';
import { MappingFieldActionComponent } from './mapping-field-action.component';

@Component({
  selector: 'mapping-field-detail',
  templateUrl: './mapping-field-detail.component.html',
})

export class MappingFieldDetailComponent implements OnInit {

  @Input() cfg: ConfigModel;
  @Input() fieldPair: FieldMappingPair;
  @Input() isSource: boolean;
  @Input() mappedField: MappedField;

  dataSource: Observable<any>;
  inputId: string;
  sourceIconCSSClass: string;
  parentObjectName: string;

  private searchFilter = '';

  constructor() {
    this.dataSource = Observable.create((observer: any) => {
      observer.next(this.executeSearch(this.searchFilter));
    });
  }

  ngOnInit() {
    this.updateTemplateValues();
  }

  isTransformCapable() {
    return (!this.mappedField.isPadField() && this.mappedField.field.name.length > 0);
  }

  updateSearchFilter(value: string) {
    this.searchFilter = value;
  }

  /**
   * The add transformation icon has been selected.  Add a field action to the current
   * mapped field.
   */
  addTransformation(): void {
    const actionConfig: FieldActionConfig =
      MappingFieldActionComponent.getFieldActions(this.fieldPair, this.isSource)[0];
    if (actionConfig == null) {
      this.cfg.errorService.info('The selected field has no applicable transformation actions.', null);
      return;
    }
    const action: FieldAction = new FieldAction();
    actionConfig.populateFieldAction(action);
    this.mappedField.actions.push(action);
    this.cfg.mappingService.saveCurrentMapping();
    this.mappedField.incTransformationCount();
    this.cfg.mappingService.notifyMappingUpdated();
  }

  getFieldPath(): string {
    if (this.mappedField == null || this.mappedField.field == null
      || (this.mappedField.isNoneField())) {
      return '[None]';
    }
    return this.mappedField.field.path;
  }

  displayParentObject(): boolean {
    if (this.mappedField != null && this.mappedField.isPadField()) {
      this.parentObjectName = '<padding field>';
      return true;
    }
    if (this.mappedField == null || this.mappedField.field == null
      || this.mappedField.field.name.length === 0
      || this.mappedField.field.docDef == null
      || (this.mappedField.isNoneField())) {
      return false;
    }
    if (this.parentObjectName == null || this.parentObjectName.length === 0) {
      this.updateTemplateValues();
    }
    return true;
  }

  /**
   * The user has hand-edited the index value of a mapped field.  Perform the following:
   *   - Add place-holders for each index value between the updated value and its previous value.
   *   - Re-sequence the field action indices.
   *   - Sort the mapped fields array to get then back into numerical order.
   * @param event
   * @param mappedField
   */
  indexSelectionChanged(event: any, mappedField: MappedField): void {
    const insertionIndex = Number(event.target.value) || 0;
    if (insertionIndex === 0) {
      return;
    }
    const mappedFields = this.fieldPair.getMappedFields(mappedField.isSource());
    const maxIndex = this.fieldPair.getMaxIndex(mappedFields);

    if (insertionIndex > maxIndex) {
      // Add place-holders for each index value between the previous max index and the insertion index.
      mappedField.addPlaceholders(maxIndex, insertionIndex, this.fieldPair);
    }
    this.fieldPair.resequenceFieldActionIndices(mappedFields, mappedField, insertionIndex.toString(10), false);

    // Sort the mapped fields array to get then back into numerical order.
    this.fieldPair.sortFieldActionFields(mappedFields);
    this.cfg.mappingService.saveCurrentMapping();
  }

  selectionChanged(event: any): void {
    this.mappedField.field = event.item['field'];
    this.searchFilter = this.mappedField.field.getFieldLabel(this.cfg.showTypes, false);
    this.fieldPair.updateTransition(this.mappedField.field.isSource(), true, false);
    this.cfg.mappingService.transitionMode(this.fieldPair, this.mappedField.field);
    this.cfg.mappingService.saveCurrentMapping();
    this.updateTemplateValues();
  }

  removeMappedField(mappedField: MappedField): void {
    this.fieldPair.removeMappedField(mappedField, this.isSource);
    if (this.fieldPair.getMappedFields(this.isSource).length === 0) {
      this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource, true);
    }
    this.cfg.mappingService.updateMappedField(this.fieldPair, this.isSource, true);
  }

  getActionIndex(mappedField: MappedField): string {
    return mappedField.actions[0].argumentValues[0].value;
  }

  hasActionIndex(mappedField: MappedField): boolean {
    if (!mappedField.isNoneField() && mappedField.field.name.length > 0 && mappedField.actions != null &&
        mappedField.actions.length > 0 && mappedField.actions[0].argumentValues != null &&
        mappedField.actions[0].argumentValues.length > 0 && mappedField.actions[0].isSeparateOrCombineMode) {
      return true;
    } else { return false; }
  }

  getSearchPlaceholder(): string {
    return 'Begin typing to search for more ' + (this.isSource ? 'sources' : 'targets');
  }

  displaySeparator(): boolean {
    return (this.mappedField.isNoneField() && this.isSource &&
      (this.fieldPair.transition.isSeparateMode() || this.fieldPair.transition.isCombineMode()));
  }

  displayFieldSearchBox(): boolean {

    if (this.mappedField.isPadField()) {
      return false;
    }

    if ((this.fieldPair.transition.mode === TransitionMode.MAP) ||
        (this.mappedField.field.name.length > 0)) {
      return true;
    }

    if (this.isSource) {
      if (this.fieldPair.transition.mode === TransitionMode.COMBINE) {
        return true;
      }
    } else {
      if (this.fieldPair.transition.mode === TransitionMode.SEPARATE) {
        return true;
      }
    }
    return false;
  }

  /**
   * This search is triggered off of the observer created in the constructor.  Note that we display any
   * field whose path matches but we capture only the field leaf name for display.
   *
   * @param filter
   */
  executeSearch(filter: string): any[] {
    const formattedFields: any[] = [];
    let fields: Field[] = [DocumentDefinition.getNoneField()];
    for (const docDef of this.cfg.getDocs(this.isSource)) {
      fields = fields.concat(docDef.getTerminalFields());
    }
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    for (const field of fields) {
      let displayName = (field == null) ? '' : field.getFieldLabel(ConfigModel.getConfig().showTypes, true);

      if (filter == null || filter === '' || displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        if (!activeMapping.isFieldSelectable(field)) {
          continue;
        }
        displayName = field.getFieldLabel(ConfigModel.getConfig().showTypes, false);
        const formattedField: any = { 'field': field, 'displayName': displayName };
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 9) {
        break;
      }
    }
    return formattedFields;
  }

  private updateTemplateValues(): void {
    this.inputId = this.getInputId();
    this.sourceIconCSSClass = this.getSourceIconCSSClass();
    this.parentObjectName = this.getParentObjectName();
  }

  private getInputId(): string {
    return 'input-' + (this.isSource ? 'source' : 'target') + '-' +
      this.mappedField.field.getFieldLabel(ConfigModel.getConfig().showTypes, false);
  }

  private getSourceIconCSSClass(): string {
    return this.isSource ? 'fa fa-hdd-o' : 'fa fa-download';
  }

  private getParentObjectName() {
    if (this.mappedField == null || this.mappedField.field == null || this.mappedField.field.docDef == null) {
      return '';
    }
    return this.mappedField.field.docDef.getName(ConfigModel.getConfig().showTypes);
  }
}
