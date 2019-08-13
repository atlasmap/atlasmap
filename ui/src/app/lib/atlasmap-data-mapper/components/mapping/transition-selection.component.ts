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

import { TransitionDelimiter, TransitionModel, TransitionDelimiterModel } from '../../models/transition.model';
import { ConfigModel } from '../../models/config.model';
import { MappingModel } from '../../models/mapping.model';
import { ModalWindowComponent } from '../modal-window.component';
import { LookupTableComponent } from './lookup-table.component';

@Component({
  selector: 'transition-selection',
  templateUrl: './transition-selection.component.html'
})

export class TransitionSelectionComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;
  @Input() mapping: MappingModel;

  delimiters: TransitionDelimiterModel[];

  constructor() {
    TransitionModel.initialize();
    this.delimiters = TransitionModel.delimiterModels;
  }

  ngOnInit(): void {
    const that = this;

    // $(document).ready( function() {
    jQuery(function() {
      // Invoke the combobox so it supersedes the stock select.
      // $('select#separator').combobox({clearIfNoMatch: false});

      $('select#separator').on('change', function() {

        // Check the combobox select.
        const optionSelected = $(this).find('option:selected');
        const selectedValue: any = optionSelected.val();

        if (selectedValue) {
          that.mapping.transition.delimiter = parseInt(selectedValue, 10);
          that.cfg.mappingService.notifyMappingUpdated();
          return;
        }
        const inputValue: any = $(this).val();

        if (inputValue) {
          that.mapping.transition.delimiter = TransitionDelimiter.USER_DEFINED;
          that.mapping.transition.userDelimiter = inputValue;
          that.cfg.mappingService.notifyMappingUpdated();
          return;
        }
      });

      // Replace the user input when focus is lost.
      $('.combobox').on('blur', function() {

        if (that.mapping.transition.delimiter === TransitionDelimiter.USER_DEFINED) {
          $(this).find('option:selected').val(that.mapping.transition.userDelimiter);
          $(this).val(that.mapping.transition.userDelimiter).trigger('input');
        }
      });

      // Check the combobox input text field.
      /* NOTE: re-enable this function for non-standard delimiters with combobox
      $('.combobox').on('input', function() {

        const inputValue: any = $(this).val();

        if (inputValue) {
          if (inputValue.length > 1) {
            that.cfg.errorService.error('The separator delimiter must be one character in length.', null);
            return;
          }
          that.fieldPair.transition.delimiter = TransitionDelimiter.USER_DEFINED;
          that.fieldPair.transition.userDelimiter = inputValue;
          that.cfg.mappingService.updateMappedField(that.fieldPair, false, false);
        }
      });
      */
    });

  }

  modeIsEnum(): boolean {
    return this.mapping.transition.isEnumerationMode();
  }

  showLookupTable(): void {
    const mapping: MappingModel = this.cfg.mappings.activeMapping;
    this.cfg.errorService.clearMappingErrors();

    if (!mapping.isFullyMapped()) {
      this.cfg.errorService.mappingError('Please select source and target fields before mapping values.', null);
      return;
    }
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Finish';
    this.modalWindow.headerText = 'Map Enumeration Values';
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
      c.initialize(this.cfg, this.mapping);
    };
    this.modalWindow.nestedComponentType = LookupTableComponent;
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      const c: LookupTableComponent = mw.nestedComponent as LookupTableComponent;
      c.saveTable();
      this.cfg.mappingService.notifyMappingUpdated();
    };
    this.modalWindow.show();
  }

  modeIsSupported(delimiterModel: TransitionDelimiterModel): boolean {
    if (delimiterModel.delimiter === TransitionDelimiter.NONE) {
      return false;
    } else if (delimiterModel.delimiter === TransitionDelimiter.MULTI_SPACE) {
      return this.mapping.transition.isOneToManyMode();
    }
    return true;
  }

  isUserDelimiter(delimiterModel: TransitionDelimiterModel) {
    return (delimiterModel.delimiter === TransitionDelimiter.USER_DEFINED);
  }
}
