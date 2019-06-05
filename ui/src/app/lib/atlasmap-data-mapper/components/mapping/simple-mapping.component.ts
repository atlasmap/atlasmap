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
import { FieldMappingPair, MappedField } from '../../models/mapping.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import { Field } from '../../models/field.model';

@Component({
  selector: 'simple-mapping',
  templateUrl: './simple-mapping.component.html',
})

export class SimpleMappingComponent {
  @Input() cfg: ConfigModel;
  @Input() isSource = false;
  @Input() fieldPair: FieldMappingPair;

  private isDragDropTarget = false;
  private elem = null;
  private start = 0;
  private diff = 0;

  isPartialComponent(): boolean {
    return true;
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

    if (insertBeforeMappedField != null && insertBeforeMappedField.actions[0] != null) {
      this.cfg.mappingService.resequenceMappedField(this.fieldPair, droppedMappedField,
        insertBeforeMappedField.getFieldIndex());

      // Update indexing in any conditional mapping expressions.
      if (this.fieldPair.transition && this.fieldPair.transition.enableExpression) {
        this.fieldPair.transition.expression.updateFieldReference(this.fieldPair);
        this.cfg.mappingService.notifyMappingUpdated();
      }
    }
    this.cfg.currentDraggedField = null;
  }

  isAddButtonVisible(): boolean {
    if (this.isSource && this.fieldPair.transition.isCombineMode()) {
      return true;
    } else if (!this.isSource && this.fieldPair.transition.isSeparateMode()) {
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

  removePair(): void {
    this.cfg.mappingService.removeMappedPair(this.fieldPair);
  }

  removeMappedField(mappedField: MappedField): void {
    this.fieldPair.removeMappedField(mappedField, this.isSource);
    if (this.fieldPair.getMappedFields(this.isSource).length === 0) {
      this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource, true);
    }
    this.cfg.mappingService.updateMappedField(this.fieldPair, this.isSource, true);
  }
}
