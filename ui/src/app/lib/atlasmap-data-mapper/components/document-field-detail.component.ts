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

import { Component, Input, ElementRef, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';

import { LineMachineComponent } from './line-machine.component';
import { ModalWindowComponent } from './modal-window.component';

import { PropertyFieldEditComponent } from './property-field-edit.component';
import { ConstantFieldEditComponent } from './constant-field-edit.component';
import { FieldEditComponent } from './field-edit.component';

@Component({
  selector: 'document-field-detail',
  templateUrl: './document-field-detail.component.html',
})

export class DocumentFieldDetailComponent {
  @Input() cfg: ConfigModel;
  @Input() field: Field;
  @Input() lineMachine: LineMachineComponent;
  @Input() modalWindow: ModalWindowComponent;

  @ViewChild('fieldDetailElement') fieldDetailElement: ElementRef;
  @ViewChildren('fieldDetail') fieldComponents: QueryList<DocumentFieldDetailComponent>;

  private isDragDropTarget = false;

  constructor(private sanitizer: DomSanitizer) { }

  startDrag(event: any): void {

    if (!this.field.isTerminal()) {
      // ignore drag event, it's coming from a child field who's already set on the drag event
      return;
    }

    this.cfg.currentDraggedField = null;

    // event's data transfer store isn't available during dragenter/dragleave/dragover, so we need
    // to store this info in a global somewhere since those methods depend on knowing if the
    // dragged field is source/target
    event = event || window.event;
    event.dataTransfer.setData('text', '');  // firefox bug
    this.cfg.currentDraggedField = this.field;
  }

  dragEnterLeave(event: any, entering: boolean): void {
    if (!this.field.isTerminal() || (this.field.isSource() === this.cfg.currentDraggedField.isSource())) {
      this.isDragDropTarget = false;
      return;
    }
    this.isDragDropTarget = entering;
  }

  allowDrop(event: any): void {
    if (!this.field.isTerminal() || (this.field.isSource() === this.cfg.currentDraggedField.isSource())) {
      this.isDragDropTarget = false;
      return;
    }
    if (event.preventDefault) {
      event.preventDefault();
    }
    if (event.stopPropagation) {
      event.stopPropagation();
    }
    this.isDragDropTarget = true;
  }

  endDrag(event: MouseEvent): void {
    this.isDragDropTarget = false;
    if (!this.field.isTerminal() || (this.field.isSource() === this.cfg.currentDraggedField.isSource())) {
      return;
    }

    const droppedField: Field = this.cfg.currentDraggedField;
    if (droppedField == null) {
      return;
    }

    if (!this.cfg.currentDraggedField.selected) {
      this.cfg.mappingService.fieldSelected(this.cfg.currentDraggedField, event.ctrlKey || event.metaKey);
    }
    this.cfg.mappingService.fieldSelected(this.field, false);
  }

  getFieldTypeIcon(): string {
    if (this.field.enumeration) {
      return 'fa fa-file-text-o';
    }
    if (this.field.isCollection) {
      return 'fa fa-list-ul';
    }
    if (this.field.docDef.type === DocumentType.XML) {
      return this.field.isAttribute ? 'fa fa-at' : 'fa fa-code';
    }
    return 'fa fa-file-o';
  }

  fieldShouldBeVisible(): boolean {
    const partOfMapping: boolean = this.field.partOfMapping;
    return partOfMapping ? this.cfg.showMappedFields : this.cfg.showUnmappedFields;
  }

  getTransformationClass(): string {
    if (!this.field.partOfMapping || !this.field.partOfTransformation) {
      return 'partOfMappingIcon partOfMappingIconHidden';
    }
    return 'partOfMappingIcon fa fa-bolt';
  }

  getMappingClass(): string {
    if (!this.field.partOfMapping) {
      return 'partOfMappingIcon partOfMappingIconHidden';
    }
    let clz = 'fa fa-circle';
    if (!this.field.isTerminal() && this.field.hasUnmappedChildren) {
      clz = 'fa fa-adjust';
    }
    return 'partOfMappingIcon ' + clz;
  }

  getCssClass(): string {
    let cssClass = 'fieldDetail';
    if (this.selected) {
      cssClass += ' selectedField';
    }
    if (!this.field.isTerminal()) {
      cssClass += ' parentField';
    }
    if (!this.field.isSource()) {
      cssClass += ' outputField';
    }
    if (this.isDragDropTarget) {
      cssClass += ' dragHover';
    }
    return cssClass;
  }

  getElementPosition(): any {
    let x = 0;
    let y = 0;

    let el: any = this.fieldDetailElement.nativeElement;
    while (el != null) {
      x += el.offsetLeft;
      y += el.offsetTop;
      el = el.offsetParent;
    }
    return { 'x': x, 'y': y };
  }

  handleMouseOver(event: MouseEvent): void {
    if (this.field.isTerminal() && this.lineMachine != null) {
      this.lineMachine.handleDocumentFieldMouseOver(this, event, this.field.isSource());
    }
  }

  getParentToggleClass() {
    return 'arrow fa fa-angle-' + (this.field.collapsed ? 'right' : 'down');
  }

  /**
   * Semantic support for a mouse click.
   * * M1 - the field is selected
   * * Ctrl/Cmd-M1 - compound-select
   *
   * @param event
   */
  handleMouseClick(event: MouseEvent): void {
    this.cfg.mappingService.fieldSelected(this.field, event.ctrlKey || event.metaKey);
    if (this.lineMachine != null) {
      setTimeout(() => {
        this.lineMachine.redrawLinesForMappings();
      }, 1);
    }
  }

  getFieldDetailComponent(field: Field): DocumentFieldDetailComponent {
    if (this.field === field) {
      return this;
    }

    // Matching name and doc definition is a match
    if ((this.field.path === field.path) && (this.field.docDef === field.docDef)) {
      this.field = field;
      return this;
    }
    for (const c of this.fieldComponents.toArray()) {
      const returnedComponent: DocumentFieldDetailComponent = c.getFieldDetailComponent(field);
      if (returnedComponent != null) {
        return returnedComponent;
      }
    }
    return null;
  }

  editField(event: any): void {
    event.stopPropagation();
    const self: DocumentFieldDetailComponent = this;
    const oldPath: string = this.field.path;
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Save';
    const isProperty: boolean = this.field.isProperty();
    const isConstant: boolean = this.field.isConstant();
    this.modalWindow.headerText = isProperty ? 'Edit Property' : (isConstant ? 'Edit Constant' : 'Edit Field');
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      if (isProperty) {
        const propertyComponent: PropertyFieldEditComponent = mw.nestedComponent as PropertyFieldEditComponent;
        propertyComponent.initialize(self.field, this.field.docDef, this.modalWindow);
      } else if (isConstant) {
        const constantComponent: ConstantFieldEditComponent = mw.nestedComponent as ConstantFieldEditComponent;
        constantComponent.initialize(self.field, this.field.docDef, this.modalWindow);
      } else {
        const fieldComponent: FieldEditComponent = mw.nestedComponent as FieldEditComponent;
        fieldComponent.isSource = self.field.isSource();
        fieldComponent.initialize(self.field, this.field.docDef, false);
      }
    };
    this.modalWindow.nestedComponentType = isProperty ? PropertyFieldEditComponent
      : (isConstant ? ConstantFieldEditComponent : FieldEditComponent);
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      let newField: Field = null;
      if (isProperty) {
        const propertyComponent: PropertyFieldEditComponent = mw.nestedComponent as PropertyFieldEditComponent;
        newField = propertyComponent.getField();
      } else if (isConstant) {
        const constantComponent: ConstantFieldEditComponent = mw.nestedComponent as ConstantFieldEditComponent;
        newField = constantComponent.getField();
      } else {
        const fieldComponent: FieldEditComponent = mw.nestedComponent as FieldEditComponent;
        newField = fieldComponent.getField();
      }
      self.field.copyFrom(newField);

      self.field.docDef.updateField(self.field, oldPath);

      self.cfg.mappingService.saveCurrentMapping();
    };
    this.modalWindow.show();
  }

  removeField(event: any): void {
    event.stopPropagation();
    const self: DocumentFieldDetailComponent = this;
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Remove';
    if (this.field.isPropertyOrConstant()) {
      this.modalWindow.headerText = this.field.isProperty() ? 'Remove Property?' : 'Remove Constant?';
    } else {
      this.modalWindow.headerText = 'Remove field?';
    }
    this.modalWindow.message = 'Are you sure you want to remove \'' + this.field.displayName + '\'?';
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      self.cfg.mappings.removeFieldFromAllMappings(self.field);
      self.field.docDef.removeField(self.field);
      self.cfg.mappingService.saveCurrentMapping();
    };
    this.modalWindow.show();
  }

  getSpacerWidth(): SafeStyle {
    const width: string = (this.field.fieldDepth * 30).toString();
    return this.sanitizer.bypassSecurityTrustStyle('display:inline; margin-left:' + width + 'px');
  }

  get selected(): boolean {
    if (this.cfg.mappings && this.cfg.mappings.activeMapping) {
      return this.cfg.mappings.activeMapping.getFields(this.field.isSource()).includes(this.field);
    }
    return false;
  }

}
