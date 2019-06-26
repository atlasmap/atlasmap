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
import { MappingModel, MappedField } from '../../models/mapping.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import { Field } from '../../models/field.model';

@Component({
  selector: 'simple-mapping',
  templateUrl: './simple-mapping.component.html',
})

export class SimpleMappingComponent {
  @Input() cfg: ConfigModel;
  @Input() isSource = false;
  @Input() mapping: MappingModel;

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
      this.cfg.mappingService.resequenceMappedField(this.mapping, droppedMappedField,
        insertBeforeMappedField.index);

      // Update indexing in any conditional mapping expressions.
      if (this.mapping.transition && this.mapping.transition.enableExpression) {
        this.mapping.transition.expression.updateFieldReference(this.mapping);
        this.cfg.mappingService.notifyMappingUpdated();
      }
    }
    this.cfg.currentDraggedField = null;
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
    this.mapping.removeMappedField(mappedField, this.isSource);
    if (this.mapping.getMappedFields(this.isSource).length === 0) {
      this.mapping.addField(DocumentDefinition.getNoneField(), this.isSource, true);
    }
    this.cfg.mappingService.updateMappedField(this.mapping, this.isSource, true);
  }
}
