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
import { Observable } from 'rxjs/Observable';

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { DocumentDefinition } from '../../models/document-definition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../../models/mapping.model';

@Component({
  selector: 'mapping-field-detail',
  templateUrl: './mapping-field-detail.component.html',
})

export class MappingFieldDetailComponent implements OnInit {

  @Input() cfg: ConfigModel;
  @Input() fieldPair: FieldMappingPair;
  @Input() isSource: boolean;
  @Input() mappedField: MappedField;

  dataSource: Observable<any>;
  inputId: string;
  sourceIconCSSClass: string;
  parentObjectName: string;

  constructor() {
    this.dataSource = Observable.create((observer: any) => {
      observer.next(this.executeSearch(observer.outerValue));
    });
  }

  ngOnInit() {
    this.updateTemplateValues();
  }

  getFieldPath(): string {
    if (this.mappedField == null || this.mappedField.field == null
      || (this.mappedField.field == DocumentDefinition.getNoneField())) {
      return '[None]';
    }
    return this.mappedField.field.path;
  }

  displayParentObject(): boolean {
    if (this.mappedField == null || this.mappedField.field == null
      || this.mappedField.field.docDef == null
      || (this.mappedField.field == DocumentDefinition.getNoneField())) {
      return false;
    }
    return true;
  }

  selectionChanged(event: any): void {
    this.mappedField.field = event.item['field'];
    this.cfg.mappingService.updateMappedField(this.fieldPair, this.mappedField.field.isSource());
    this.updateTemplateValues();
  }

  executeSearch(filter: string): any[] {
    const formattedFields: any[] = [];
    let fields: Field[] = [DocumentDefinition.getNoneField()];
    for (const docDef of this.cfg.getDocs(this.isSource)) {
      fields = fields.concat(docDef.getTerminalFields());
    }
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    for (const field of fields) {
      const displayName = (field == null) ? '' : field.getFieldLabel(ConfigModel.getConfig().showTypes, true);
      const formattedField: any = { 'field': field, 'displayName': displayName };
      if (filter == null || filter == ''
        || formattedField['displayName'].toLowerCase().indexOf(filter.toLowerCase()) != -1) {
        if (!activeMapping.isFieldSelectable(field)) {
          continue;
        }
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 9) {
        break;
      }
    }
    return formattedFields;
  }

  private updateTemplateValues(): void {
    this.inputId = this.getInputId();
    this.sourceIconCSSClass = this.getSourceIconCSSClass();
    this.parentObjectName = this.getParentObjectName();
  }

  private getInputId(): string {
    return 'input-' + (this.isSource ? 'source' : 'target') + '-' +
      this.mappedField.field.getFieldLabel(ConfigModel.getConfig().showTypes, false);
  }

  private getSourceIconCSSClass(): string {
    return this.isSource ? 'fa fa-hdd-o' : 'fa fa-download';
  }

  private getParentObjectName() {
    if (this.mappedField == null || this.mappedField.field == null || this.mappedField.field.docDef == null) {
      return '';
    }
    return this.mappedField.field.docDef.getName(ConfigModel.getConfig().showTypes);
  }

}
