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

import { Component, Input, ViewChildren, ElementRef, QueryList, ViewChild, OnInit } from '@angular/core';

import { DocumentType } from '../common/config.types';
import { ConfigModel } from '../models/config.model';
import { Field } from '../models/field.model';
import { DocumentDefinition } from '../models/document-definition.model';

import { DocumentFieldDetailComponent } from './document-field-detail.component';
import { PropertyFieldEditComponent } from './property-field-edit.component';
import { ConstantFieldEditComponent } from './constant-field-edit.component';
import { FieldEditComponent } from './field-edit.component';

import { LineMachineComponent } from './line-machine.component';
import { ModalWindowComponent } from './modal-window.component';

@Component({
  selector: 'document-definition',
  templateUrl: './document-definition.component.html',
})

export class DocumentDefinitionComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() isSource = false;
  @Input() lineMachine: LineMachineComponent;
  @Input() modalWindow: ModalWindowComponent;

  @ViewChild('documentDefinitionElement') documentDefinitionElement: ElementRef;
  @ViewChildren('fieldDetail') fieldComponents: QueryList<DocumentFieldDetailComponent>;
  @ViewChildren('docDetail') docElements: QueryList<ElementRef>;

  private searchMode = false;
  private searchFilter = '';
  private scrollTop = 0;
  private searchResultsExist = false;

  private sourcesTargetsLabel: string;
  private documents: DocumentDefinition[];

  ngOnInit(): void {
    if (this.isSource) {
      this.sourcesTargetsLabel = (this.cfg.sourceDocs.length > 1) ? 'Sources' : 'Source';
    } else {
      this.sourcesTargetsLabel = (this.cfg.targetDocs.length > 1) ? 'Targets' : 'Target';
    }
    this.documents = this.cfg.getDocs(this.isSource);
  }

  getDocDefElementPosition(docDef: DocumentDefinition): any {
    for (const c of this.docElements.toArray()) {
      if (c.nativeElement.id == docDef.name) {
        const documentElementAbsPosition: any = this.getElementPositionForElement(c.nativeElement, false, true);
        const myElement: any = this.documentDefinitionElement.nativeElement;
        const myAbsPosition: any = this.getElementPositionForElement(myElement, false, false);
        return {
          'x': (documentElementAbsPosition.x - myAbsPosition.x),
          'y': (documentElementAbsPosition.y - myAbsPosition.y)
        };
      }
    }
    return null;
  }

  getFieldDetailComponent(field: Field): DocumentFieldDetailComponent {
    for (const c of this.fieldComponents.toArray()) {
      const returnedComponent: DocumentFieldDetailComponent = c.getFieldDetailComponent(field);
      if (returnedComponent != null) {
        return returnedComponent;
      }
    }
    return null;
  }

  getElementPosition(): any {
    return this.getElementPositionForElement(this.documentDefinitionElement.nativeElement, true, false);
  }

  getElementPositionForElement(el: any, addScrollTop: boolean, subtractScrollTop: boolean): any {
    let x = 0;
    let y = 0;

    while (el != null) {
      x += el.offsetLeft;
      y += el.offsetTop;
      el = el.offsetParent;
    }
    if (addScrollTop) {
      y += this.scrollTop;
    }
    if (subtractScrollTop) {
      y -= this.scrollTop;
    }
    return { 'x': x, 'y': y };
  }

  getFieldDetailComponentPosition(field: Field): any {
    const c: DocumentFieldDetailComponent = this.getFieldDetailComponent(field);
    if (c == null) {
      return null;
    }
    const fieldElementAbsPosition: any = c.getElementPosition();
    const myAbsPosition: any = this.getElementPosition();
    return { 'x': (fieldElementAbsPosition.x - myAbsPosition.x), 'y': (fieldElementAbsPosition.y - myAbsPosition.y) };
  }

  getSearchIconCSSClass(): string {
    const cssClass = 'fa fa-search searchBoxIcon link';
    return this.searchMode ? (cssClass + ' selectedIcon') : cssClass;
  }

  getFieldCount(): number {
    let count = 0;
    for (const docDef of this.cfg.getDocs(this.isSource)) {
      if (docDef && docDef.allFields) {
        count += docDef.allFields.length;
      }
    }
    return count;
  }
  handleScroll(event: any) {
    this.scrollTop = event.target.scrollTop;
    this.lineMachine.redrawLinesForMappings();
  }

  toggleSearch(): void {
    this.searchMode = !this.searchMode;
    this.search(this.searchMode ? this.searchFilter : '');
  }

  addField(docDef: DocumentDefinition, event: any): void {
    event.stopPropagation();
    const self = this;
    this.modalWindow.reset();
    this.modalWindow.confirmButtonText = 'Save';
    const isProperty = docDef.type == DocumentType.PROPERTY;
    const isConstant = docDef.type == DocumentType.CONSTANT;
    this.modalWindow.headerText = isProperty ? 'Create Property' : (isConstant ? 'Create Constant' : 'Create Field');
    this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
      if (isProperty) {
        const propertyComponent = mw.nestedComponent as PropertyFieldEditComponent;
        propertyComponent.initialize(null);
      } else if (isConstant) {
        const constantComponent = mw.nestedComponent as ConstantFieldEditComponent;
        constantComponent.initialize(null);
      } else {
        const fieldComponent = mw.nestedComponent as FieldEditComponent;
        fieldComponent.isSource = this.isSource;
        fieldComponent.initialize(null, docDef, true);
      }
    };
    this.modalWindow.nestedComponentType = isProperty ? PropertyFieldEditComponent
      : (isConstant ? ConstantFieldEditComponent : FieldEditComponent);
    this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
      if (isProperty) {
        const propertyComponent = mw.nestedComponent as PropertyFieldEditComponent;
        docDef.addField(propertyComponent.getField());
      } else if (isConstant) {
        const constantComponent = mw.nestedComponent as ConstantFieldEditComponent;
        docDef.addField(constantComponent.getField());
      } else {
        const fieldComponent = mw.nestedComponent as FieldEditComponent;
        docDef.addField(fieldComponent.getField());
      }
      self.cfg.mappingService.saveCurrentMapping();
    };
    this.modalWindow.show();
  }

  isDocNameVisible(docDef: DocumentDefinition): boolean {
    if (this.searchMode && !docDef.visibleInCurrentDocumentSearch) {
      return false;
    }
    return true;
  }

  toggleFieldVisibility(docDef: DocumentDefinition): void {
    docDef.showFields = !docDef.showFields;
    setTimeout(() => {
      this.lineMachine.redrawLinesForMappings();
    }, 10);
  }

  isAddFieldAvailable(docDef: DocumentDefinition): boolean {
    return docDef.isPropertyOrConstant
      || (!docDef.isSource && docDef.type == DocumentType.JSON)
      || (!docDef.isSource && docDef.type == DocumentType.XML);
  }

  private search(searchFilter: string): void {
    this.searchResultsExist = false;
    const searchIsEmpty: boolean = (searchFilter == null) || ('' == searchFilter);
    const defaultVisibility: boolean = searchIsEmpty ? true : false;
    for (const docDef of this.cfg.getDocs(this.isSource)) {
      docDef.visibleInCurrentDocumentSearch = defaultVisibility;
      for (const field of docDef.getAllFields()) {
        field.visibleInCurrentDocumentSearch = defaultVisibility;
      }
      if (!searchIsEmpty) {
        for (const field of docDef.getAllFields()) {
          field.visibleInCurrentDocumentSearch = field.name.toLowerCase().includes(searchFilter.toLowerCase());
          this.searchResultsExist = this.searchResultsExist || field.visibleInCurrentDocumentSearch;
          if (field.visibleInCurrentDocumentSearch) {
            docDef.visibleInCurrentDocumentSearch = true;
            let parentField = field.parentField;
            while (parentField != null) {
              parentField.visibleInCurrentDocumentSearch = true;
              parentField.collapsed = false;
              parentField = parentField.parentField;
            }
          }
        }
      }
    }
  }
}
