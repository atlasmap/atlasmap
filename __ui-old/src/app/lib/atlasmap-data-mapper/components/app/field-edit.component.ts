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

import { Component, ViewChild, ElementRef } from '@angular/core';

import { DocumentDefinition, NamespaceModel } from '../../models/document-definition.model';
import { Field } from '../../models/field.model';
import { DocumentType } from '../../common/config.types';
import { ConfigModel } from '../../models/config.model';
import { Observable } from 'rxjs';
import { ModalWindowValidator } from '../modal/modal-window.component';

@Component({
  selector: 'field-edit',
  templateUrl: './field-edit.component.html',
})

export class FieldEditComponent implements ModalWindowValidator {
  cfg: ConfigModel = ConfigModel.getConfig();
  field: Field = new Field();
  parentField: Field;
  parentFieldName: String = null;
  isSource = false;
  fieldType: any = 'element';
  valueType: any = 'STRING';
  namespaceAlias = '';
  editMode = false;
  namespaces: NamespaceModel[] = [];
  docDef: DocumentDefinition = null;
  dataSource: Observable<any>;
  isXML = false;
  @ViewChild('value') private focusEl: ElementRef;

  constructor() {
    this.dataSource = Observable.create((observer: any) => {
      observer.next(this.executeSearch(observer.outerValue));
    });
  }

  initialize(field: Field, docDef: DocumentDefinition, isAdd: boolean): void {
    this.docDef = docDef;
    this.editMode = !isAdd;
    this.field = field == null ? new Field() : field.copy();
    this.valueType = (this.field.type == null) ? 'STRING' : this.field.type;
    this.parentField = this.field.parentField;

    if (this.docDef.type === DocumentType.XML) {
      this.isXML = true;
      this.fieldType = this.field.isAttribute ? 'attribute' : 'element';
      this.parentField = (this.field.parentField == null) ? docDef.fields[0] : this.field.parentField;
      const unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
      this.namespaceAlias = unqualifiedNS.alias;
      if (this.field.namespaceAlias) {
        this.namespaceAlias = this.field.namespaceAlias;
      }
      if (isAdd) { // on add, inherit namespace from parent field
        this.namespaceAlias = this.parentField.namespaceAlias == null ? unqualifiedNS.alias : this.parentField.namespaceAlias;
      }

      this.namespaces = [unqualifiedNS].concat(this.docDef.namespaces);

      // if the field references a namespace that doesn't exist, add a fake namespace option for the
      // user to select if they desire to leave that bad namespace alias in place
      let namespaceFound = false;
      for (const ns of this.namespaces) {
        if (ns.alias === this.namespaceAlias) {
          namespaceFound = true;
          break;
        }
      }
      if (!namespaceFound) {
        const fakeNamespace: NamespaceModel = new NamespaceModel();
        fakeNamespace.alias = this.namespaceAlias;
        this.namespaces.push(fakeNamespace);
      }
    }
    this.parentFieldName = this.parentField.name;
  }

  handleOnBlur(event: any): void {
    this.parentFieldName = this.parentField.name;
  }

  parentSelectionChanged(event: any): void {
    const oldParentField: Field = this.parentField;
    this.parentField = event.item['field'];
    this.parentField = (this.parentField == null) ? oldParentField : this.parentField;
    this.parentFieldName = this.parentField.name;

    // change namespace dropdown selecte option to match parent fields' namespace automatically
    const unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
    this.namespaceAlias = this.parentField.namespaceAlias == null ? unqualifiedNS.alias : this.parentField.namespaceAlias;
  }

  fieldTypeSelectionChanged(event: any): void {
    this.fieldType = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
  }

  valueTypeSelectionChanged(event: any): void {
    this.valueType = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
  }

  namespaceSelectionChanged(event: any): void {
    this.namespaceAlias = event.target.selectedOptions.item(0).attributes.getNamedItem('value').value;
  }

  executeSearch(filter: string): any[] {
    const formattedFields: any[] = [];

    for (const field of this.docDef.getAllFields()) {
      if (!field.isParentField()) {
        continue;
      }
      const displayName = (field == null) ? '' : field.getFieldLabel(ConfigModel.getConfig().showTypes, true);
      const formattedField: any = { 'field': field, 'displayName': displayName };
      if (filter == null || filter === ''
        || formattedField['displayName'].toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 9) {
        break;
      }
    }
    return formattedFields;
  }

  getField(): Field {
    this.field.displayName = this.field.name;
    this.field.parentField = this.parentField;
    this.field.type = this.valueType;
    this.field.userCreated = true;
    this.field.serviceObject.jsonType = 'io.atlasmap.json.v2.JsonField';
    if (this.docDef.type === DocumentType.XML) {
      this.field.isAttribute = (this.fieldType === 'attribute');
      this.field.namespaceAlias = this.namespaceAlias;
      const unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
      if (this.namespaceAlias === unqualifiedNS.alias) {
        this.field.namespaceAlias = null;
      }
      this.field.serviceObject.jsonType = 'io.atlasmap.xml.v2.XmlField';
    }
    return this.field;
  }

  isDataValid(): boolean {
    return ConfigModel.getConfig().errorService.isRequiredFieldValid(this.field.name, 'Name');
  }

  getInitialFocusElement(): ElementRef {
    return this.focusEl;
  }

}
