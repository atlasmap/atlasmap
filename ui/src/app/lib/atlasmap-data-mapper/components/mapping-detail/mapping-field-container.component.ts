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

import { ConfigModel } from '../../models/config.model';
import { DataMapperUtil } from '../../common/data-mapper-util';
import { MappingModel, MappedField } from '../../models/mapping.model';
import { TransitionMode } from '../../models/transition.model';
import { Observable } from 'rxjs';
import { Field } from '../../models/field.model';
import { ModalWindowComponent } from '../modal/modal-window.component';

@Component({
  selector: 'mapping-field-container',
  templateUrl: './mapping-field-container.component.html',
})

export class MappingFieldContainerComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() isSource = false;
  @Input() mapping: MappingModel;
  @Input() modalWindow: ModalWindowComponent;

  inputId: String;

  private isDragDropTarget = false;
  private elem = null;
  private start = 0;
  private diff = 0;
  private searchFilter = '';
  dataSource: Observable<any>;

  constructor() {
    this.dataSource = Observable.create((observer: any) => {
      observer.next(this.cfg.mappingService.executeFieldSearch(this.cfg, this.searchFilter, this.isSource));
    });
  }

  ngOnInit() {
    this.inputId = 'input-' + this.isSource ? 'source' : 'target';
  }

  itemIsDocument(model: any): boolean {
    return (!model.field);
  }
  getDisplayName(model: any): string {
    return model.displayName;
  }

  isPartialComponent(): boolean {
    return true;
  }

  getPanelIconCSSClass(model: any): string {
    return (model.field) ? '' : (this.isSource ? 'fa fa-hdd-o' : 'fa fa-download');
  }

  handleMouseOver(evt1: MouseEvent): void {
    if (this.elem != null) {
      evt1.stopPropagation();
      evt1.preventDefault();
      let end = 0;

      if (evt1['pageY']) {
        end = evt1['pageY'];
      } else if (evt1['clientY']) {
        end = evt1['clientY'];
      }
      this.diff = end - this.start;
      this.elem.style['top'] = this.diff + 'px';
    }
  }

  startDrag(event: any, draggedMappedField: MappedField): void {
    event = event || window.event;
    event.dataTransfer.setData('text', '');  // firefox bug
    this.cfg.currentDraggedField = draggedMappedField;
    /* this code does correctly constrain the drag movement to the vertical area of
     * the mapping details section. It couldn't correctly identify the on-drop target
    event = event || window.event;
    event.stopPropagation();
    event.preventDefault();
    this.elem = event.currentTarget;

    if (event['pageY']) {
        this.start = event['pageY'];
    } else if (event['clientY']) {
        this.start = event['clientY'];
    }

    this.elem.style.position = 'relative';
    this.elem.onmouseup = (evt: any) => {
      // See endDrag()
      evt.stopPropagation();
      evt.preventDefault();
    };
    */
  }

  dragEnterLeave(event: any, entering: boolean): void {
    this.isDragDropTarget = entering;
  }

  allowDrop(event: any): void {
    if (event.preventDefault) {
      event.preventDefault();
    }
    if (event.stopPropagation) {
      event.stopPropagation();
    }
    this.isDragDropTarget = true;
  }

  endDrag(event: any, insertBeforeMappedField: MappedField): void {
    this.isDragDropTarget = false;

    const droppedMappedField: MappedField = this.cfg.currentDraggedField;
    if (droppedMappedField == null) {
      return;
    }

    if (insertBeforeMappedField != null) {
      this.cfg.mappingService.moveMappedFieldTo(this.mapping, droppedMappedField,
        this.mapping.getIndexForMappedField(insertBeforeMappedField));

      // Update indexing in any conditional mapping expressions.
      if (this.mapping.transition && this.mapping.transition.enableExpression) {
        this.mapping.transition.expression.updateFieldReference(this.mapping);
        this.cfg.mappingService.notifyMappingUpdated();
      }
    }
    this.cfg.currentDraggedField = null;
  }

  displayTransitionSelection(): boolean {
    return (this.isSource && !this.mapping.transition.enableExpression &&
      (this.mapping.transition.isOneToManyMode() || this.mapping.transition.isManyToOneMode()
      || this.mapping.transition.isEnumerationMode()));
  }

  displayFieldSearchBox(): boolean {

    const mappedFields = this.mapping.getMappedFields(this.isSource);
    if (mappedFields.length === 0) {
      return true;
    } else if (mappedFields[0].field.isInCollection()) {
      return false;
    }

    if (this.mapping.transition.mode === TransitionMode.ONE_TO_ONE) {
      return true;
    }
    if (this.isSource) {
      if (this.mapping.transition.mode === TransitionMode.MANY_TO_ONE) {
        return !mappedFields[0].field.isInCollection();
      }
    } else {
      if (this.mapping.transition.mode === TransitionMode.ONE_TO_MANY) {
        return !mappedFields[0].field.isInCollection();
      }
    }
    return false;
  }

  updateSearchFilter(value: string) {
    this.searchFilter = value;
  }

  getSearchPlaceholder(): string {
    return 'Begin typing to search for more ' + (this.isSource ? 'sources' : 'targets');
  }

  selectionChanged(event: any): void {
    if (event.item['field']) {
      this.cfg.mappingService.fieldSelected(event.item['field'], true);
    }
    this.searchFilter = '';
  }

  isAddButtonVisible(): boolean {
    if (this.isSource && this.mapping.transition.isManyToOneMode()) {
      return true;
    } else if (!this.isSource && this.mapping.transition.isOneToManyMode()) {
      return true;
    }
    return false;
  }

  getTopFieldTypeLabel(): string {
    return this.isSource ? 'Source' : 'Target';
  }

  getAddButtonLabel(): string {
    return this.isSource ? 'Add Source' : 'Add Target';
  }

  removeMappedField(mappedField: MappedField): void {
    this.mapping.removeMappedField(mappedField);
    this.cfg.mappingService.updateMappedField(this.mapping);
  }
}
