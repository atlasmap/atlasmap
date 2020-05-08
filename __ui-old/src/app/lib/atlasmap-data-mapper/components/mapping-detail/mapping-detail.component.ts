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

import { Component, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { MappingModel } from '../../models/mapping.model';

import { CollapsableHeaderComponent } from '../mapping-detail/collapsable-header.component';

import { ModalWindowComponent } from '../modal/modal-window.component';
import { MappingSelectionComponent } from '../mapping-detail/mapping-selection.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'mapping-detail',
  templateUrl: './mapping-detail.component.html',
})

export class MappingDetailComponent implements OnInit, OnDestroy {
  @Input() cfg: ConfigModel;
  @Input() modalWindow: ModalWindowComponent;

  @ViewChild('sourcesHeader')
  sourcesHeader: CollapsableHeaderComponent;

  private mappingSelectionRequiredSubscription: Subscription;

  ngOnInit(): void {
    this.mappingSelectionRequiredSubscription = this.cfg.mappingService.mappingSelectionRequired$.subscribe((field: Field) => {
      this.selectMapping(field);
    });
  }

  ngOnDestroy(): void {
    this.mappingSelectionRequiredSubscription.unsubscribe();
  }

  getTitle(): string {
    return this.cfg.mappings.activeMapping.isLookupMode() ? 'Lookup Mapping' : 'Mapping Details';
  }

  removeMapping(event: MouseEvent): void {
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Remove';
    this.modalWindow.headerText = 'Remove Mapping?';
    this.modalWindow.message = 'Are you sure you want to remove the current mapping?';
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      const activeMapping = this.cfg.mappings.activeMapping;
      if (activeMapping.isLookupMode) {
        this.cfg.mappings.removeTableByName(activeMapping.transition.lookupTableName);
      }
      this.cfg.mappingService.removeMapping(activeMapping);
      this.cfg.showMappingDetailTray = false;
    };
    this.modalWindow.show();
  }

  private selectMapping(field: Field): void {
    const mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);
    const self: MappingDetailComponent = this;
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Select';
    this.modalWindow.headerText = 'Select Mapping';
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      const c: MappingSelectionComponent = mw.nestedComponent as MappingSelectionComponent;
      c.selectedField = field;
      c.cfg = self.cfg;
      c.mappings = mappingsForField;
      c.modalWindow = this.modalWindow;
    };
    this.modalWindow.nestedComponentType = MappingSelectionComponent;
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      this.cfg.errorService.clearValidationErrors();
      const c: MappingSelectionComponent = mw.nestedComponent as MappingSelectionComponent;
      const mapping: MappingModel = c.getSelectedMapping();
      self.cfg.mappingService.selectMapping(mapping);
    };
    this.modalWindow.cancelButtonHandler = (mw: ModalWindowComponent) => {
      self.cfg.mappingService.selectMapping(null);
    };
    this.modalWindow.show();
  }
}
