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
import { FormControl, FormGroup } from '@angular/forms';

import { TransitionMode, TransitionDelimiter, TransitionModel, TransitionDelimiterModel } from '../../models/transition.model';
import { ConfigModel } from '../../models/config.model';
import { LookupTable } from '../../models/lookup-table.model';
import { MappingModel, FieldMappingPair } from '../../models/mapping.model';

import { ModalWindowComponent } from '../modal-window.component';
import { LookupTableComponent } from './lookup-table.component';

@Component({
  selector: 'transition-selection',
  templateUrl: './transition-selection.component.html',
})

export class TransitionSelectionComponent {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;
  @Input() fieldPair: FieldMappingPair;

  selectActionForm: FormGroup;
  modes = TransitionMode;
  delimiters: TransitionDelimiterModel[];

  constructor() {
    TransitionModel.initialize();
    this.delimiters = TransitionModel.delimiterModels;
    this.selectActionForm = new FormGroup({
        selectAction: new FormControl(null)
    });
    this.selectActionForm.controls['selectAction'].setValue('Map', {onlySelf: true});
  }

  /**
   * Validate the user selected mode with the user selected field pairs.
   * @param selectedMode
   */
  validModeTransition(selectedMode: TransitionMode): boolean {
    const mappedSourceFields = this.fieldPair.getMappedFields(true);
    const mappedTargetFields = this.fieldPair.getMappedFields(false);

    if (mappedSourceFields.length > 1 && selectedMode !== TransitionMode.COMBINE) {
      this.cfg.errorService.warn('The selected mapping details action ' + TransitionModel.getActionName(selectedMode) +
                                 ' is not applicable from compound source selections.', null);
      return false;
    } else if (mappedTargetFields.length > 1 && selectedMode !== TransitionMode.SEPARATE) {
      this.cfg.errorService.warn('The selected mapping details action ' + TransitionModel.getActionName(selectedMode) +
                                 ' is not applicable to compound target selections.', null);
      return false;
    }
    return true;
  }

  /**
   * The user has selected a new mapping details action.  Validate it and update any mapped fields.
   * @param event - contains the selected value
   */
  selectionChanged(event: any): void {
    const selectorIsMode: boolean = 'mode' === event.target.attributes.getNamedItem('selector').value;
    const selectedValue: any = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
    if (selectorIsMode) {
      const selectedMode: TransitionMode = parseInt(selectedValue, 10);
      if (this.validModeTransition(selectedMode)) {
        this.fieldPair.transition.mode = selectedMode;
      } else {
        // Bad selected mapping details action.  Reset the UI selection to the item before the user changed it.
        this.selectActionForm.controls['selectAction'].setValue(this.fieldPair.transition.mode.toString(10), {onlySelf: true});
        return;
      }
    } else {
      this.fieldPair.transition.delimiter = parseInt(selectedValue, 10);
    }
    this.cfg.mappingService.updateMappedField(this.fieldPair, false);
  }

  modeIsEnum(): boolean {
    return this.fieldPair.transition.isEnumerationMode();
  }

  getMappedValueCount(): number {
    const tableName: string = this.fieldPair.transition.lookupTableName;
    if (tableName == null) {
      return 0;
    }
    const table: LookupTable = this.cfg.mappings.getTableByName(tableName);
    if (!table || !table.entries) {
      return 0;
    }
    return table.entries.length;
  }

  showLookupTable(): void {
    const mapping: MappingModel = this.cfg.mappings.activeMapping;
    if (!mapping.hasMappedFields(true) || !mapping.hasMappedFields(false)) {
      this.cfg.errorService.warn('Please select source and target fields before mapping values.', null);
      return;
    }
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Finish';
    this.modalWindow.headerText = 'Map Enumeration Values';
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
      c.initialize(this.cfg, this.fieldPair);
    };
    this.modalWindow.nestedComponentType = LookupTableComponent;
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      const c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
      c.saveTable();
      this.cfg.mappingService.saveCurrentMapping();
    };
    this.modalWindow.show();
  }

  modeIsSupported(delimiterModel: TransitionDelimiterModel): boolean {
    if (delimiterModel.delimiter === TransitionDelimiter.NONE) {
      return false;
    } else if (delimiterModel.delimiter === TransitionDelimiter.MULTI_SPACE) {
      return this.fieldPair.transition.isSeparateMode();
    }
    return true;
  }
}
