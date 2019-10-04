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

import { TransitionDelimiter, TransitionMode } from '../../models/transition.model';
import { ConfigModel } from '../../models/config.model';
import { MappingModel } from '../../models/mapping.model';
import { ModalWindowComponent } from '../modal-window.component';
import { LookupTableComponent } from './lookup-table.component';
import { FieldAction, FieldActionDefinition, Multiplicity } from '../../models/field-action.model';
import { DataMapperUtil } from '../../common/data-mapper-util';
import { LookupTable } from '../../models/lookup-table.model';
import { ErrorScope, ErrorType, ErrorInfo, ErrorLevel } from '../../models/error.model';

@Component({
  selector: 'transition-selection',
  templateUrl: './transition-selection.component.html'
})

export class TransitionSelectionComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;
  @Input() mapping: MappingModel;
  @Input() action: FieldAction;

  getLabel = DataMapperUtil.toDisplayable;

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

getMappedValueCount(): number {
    const tableName: string = this.mapping.transition.lookupTableName;
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

    if (!mapping.isFullyMapped()) {
      this.cfg.errorService.addError(new ErrorInfo({message: 'Please select source and target fields before mapping values.',
        level: ErrorLevel.ERROR, scope: ErrorScope.MAPPING, type: ErrorType.USER, mapping: mapping}));
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

  getMultiplicityActionConfigs(): FieldActionDefinition[] {
    if (this.mapping.transition.mode === TransitionMode.ONE_TO_MANY) {
      return this.cfg.fieldActionService.getActionsAppliesToField(this.mapping, true, Multiplicity.ONE_TO_MANY);
    } else if (this.mapping.transition.mode === TransitionMode.MANY_TO_ONE) {
      return this.cfg.fieldActionService.getActionsAppliesToField(this.mapping, true, Multiplicity.MANY_TO_ONE);
    } else {
      return [];
    }
  }

  /**
   * A mapping field action configuration selection has been made.  Note that action field arguments, if any,
   * may be specified by either a text field or pull-down menu.
   * @param event
   */
  configSelectionChanged(event: any) {
    const attributes: any = event.target.selectedOptions.item(0).attributes;
    const selectedActionName: any = attributes.getNamedItem('value').value;
    const action: FieldAction = this.action;
    if (action.name !== selectedActionName) {
      action.argumentValues = [];  // Invalidate the previously selected field action arguments.
      const multiplicity = this.mapping.transition.mode === TransitionMode.ONE_TO_MANY
       ? Multiplicity.ONE_TO_MANY : Multiplicity.MANY_TO_ONE;
      const fieldActionDefinition = this.cfg.fieldActionService.getActionDefinitionForName(selectedActionName, multiplicity);
      fieldActionDefinition.populateFieldAction(action);

      // If the field action configuration predefines argument values then populate the fields with
      // default values.  Needed to support pull-down menus in action argument definitions.
      if (action.argumentValues.values && action.argumentValues.length > 0
        && fieldActionDefinition.arguments[0] && fieldActionDefinition.arguments[0].values
        && fieldActionDefinition.arguments[0].values.length > 0) {
        for (let i = 0; i < action.argumentValues.length; i++) {
          action.argumentValues[i].value = fieldActionDefinition.arguments[i].values[i];
        }
      }
    }
    this.cfg.mappingService.notifyMappingUpdated();
  }

  isIndexArg(argVal: string, index: number): boolean {
    return (argVal === 'Index' && index === 0);
  }

}
