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

import { Component } from '@angular/core';

import { DocumentDefinition, NamespaceModel } from '../models/document.definition.model';
import { Field } from '../models/field.model';
import { ConfigModel } from '../models/config.model';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'field-edit',
    template: `
        <!-- our template for type ahead -->
        <template #typeaheadTemplate let-model="item" let-index="index">
            <h5 style="font-style:italic;">{{ model['field'].docDef.name }}</h5>
            <h5>{{ model['field'].path }}</h5>
        </template>

        <div class="PropertyEditFieldComponent">
            <div class="form-group">
                <label>Parent</label>
                <input type="text" [ngModel]="parentField.getFieldLabel(false)" [typeahead]="dataSource" 
                    typeaheadWaitMs="200" (typeaheadOnSelect)="parentSelectionChanged($event)" 
                    typeaheadOptionField="displayName" [typeaheadItemTemplate]="typeaheadTemplate">
            </div>            
            <div class="form-group">
                <label>Name</label>
                <input name="value" type="text" [(ngModel)]="field.name"/>
            </div>
            <div class="form-group">
                <label>Namespace</label>
                <select (change)="namespaceSelectionChanged($event);" [ngModel]="namespaceAlias">                        
                    <option *ngFor="let ns of cfg.getFirstXmlDoc(false).namespaces" value="{{ns.alias}}" [selected]="namespaceAlias == ns.alias">
                        {{ ns.getPrettyLabel() }}
                    </option>
                </select>
            </div>
            <div class="form-group">
                <label>Field Type</label>
                <select (change)="fieldTypeSelectionChanged($event);" [ngModel]="fieldType">                        
                    <option value="element">Element</option>
                    <option value="attribute">Attribute</option>
                </select>
            </div>
            <div class="form-group">
                <label>Value Type</label>
                <select (change)="valueTypeSelectionChanged($event);" [ngModel]="valueType">
                    <option value="BOOLEAN">Boolean</option>
                    <option value="BYTE">Byte</option>
                    <option value="BYTE_ARRAY">ByteArray</option>
                    <option value="CHAR">Char</option>
                    <option value="COMPLEX">Complex</option>
                    <option value="DECIMAL">Decimal</option>
                    <option value="DOUBLE">Double</option>
                    <option value="FLOAT">Float</option>
                    <option value="INTEGER">Integer</option>
                    <option value="LONG">Long</option>
                    <option value="SHORT">Short</option>
                    <option value="STRING">String</option>
                    <option value="TIME">Time</option>
                    <option value="DATE">Date</option>
                    <option value="DATE_TIME">DateTime</option>
                    <option value="DATE_TZ">DateTZ</option>
                    <option value="TIME_TZ">TimeTZ</option>
                    <option value="DATE_TIME_TZ">DateTimeTZ</option>
                    <option value="UNSIGNED_BYTE">Unsigned Byte</option>
                    <option value="UNSIGNED_INTEGER">Unsigned Integer</option>
                    <option value="UNSIGNED_LONG">Unsigned Long</option>
                    <option value="UNSIGNED_SHORT">Unsigned Short</option>
                </select>
            </div>            
        </div>
    `
})

export class FieldEditComponent {
    public cfg: ConfigModel = ConfigModel.getConfig();
    public field: Field = new Field();
    public parentField: Field = DocumentDefinition.getNoneField();
    public isSource: boolean = false;
    public fieldType: any = "element";
    public valueType: any = "STRING";
    public namespaceAlias: string = null;

    public dataSource: Observable<any>;

    public constructor() {
        this.dataSource = Observable.create((observer: any) => {
            observer.next(this.executeSearch(observer.outerValue));
        });
    }

    public initialize(field: Field): void {
        this.field = field == null ? new Field() : field;
        if (this.field.namespace) {
            this.namespaceAlias = this.field.namespace.alias;
        }
        if (this.namespaceAlias == null && this.cfg.getFirstXmlDoc(false).namespaces.length) {
            this.namespaceAlias = this.cfg.getFirstXmlDoc(false).namespaces[0].alias;
        }
        this.fieldType = this.field.isAttribute ? "attribute" : "element";
        this.valueType = (this.field.type == null) ? "STRING" : this.field.type;
        this.parentField = (this.field.parentField == null) ? DocumentDefinition.getNoneField() : this.field.parentField;
    }

    public parentSelectionChanged(event: any): void {
        this.parentField = event.item["field"];
    }   

    public fieldTypeSelectionChanged(event: MouseEvent): void {
        var eventTarget: any = event.target; //extract this to avoid compiler error about 'selectedOptions' not existing.
        this.fieldType = eventTarget.selectedOptions.item(0).attributes.getNamedItem("value").value;
    } 

    public valueTypeSelectionChanged(event: MouseEvent): void {
        var eventTarget: any = event.target; //extract this to avoid compiler error about 'selectedOptions' not existing.
        this.valueType = eventTarget.selectedOptions.item(0).attributes.getNamedItem("value").value;
    }

    public namespaceSelectionChanged(event: MouseEvent): void {
        var eventTarget: any = event.target; //extract this to avoid compiler error about 'selectedOptions' not existing.
        this.namespaceAlias = eventTarget.selectedOptions.item(0).attributes.getNamedItem("value").value;
    }

    public executeSearch(filter: string): any[] {
        var formattedFields: any[] = [];
        var fields: Field[] = [DocumentDefinition.getNoneField()];
        for (let docDef of ConfigModel.getConfig().getDocs(this.isSource)) {
            fields = fields.concat(docDef.getAllFields());
        }
        for (let field of fields) {
            if (!field.availableForSelection) {
                continue;
            }
            var displayName = (field == null) ? "" : field.getFieldLabel(true);
            var formattedField: any = { "field": field, "displayName": displayName };
            if (filter == null || filter == ""
                || formattedField["displayName"].toLowerCase().indexOf(filter.toLowerCase()) != -1) {
                formattedFields.push(formattedField);
            }
            if (formattedFields.length > 9) {
                break;
            }
        }
        return formattedFields;
    }

    public getField(): Field {
        this.field.displayName = this.field.name;
        this.field.isAttribute = (this.fieldType == "attribute");
        this.field.parentField = this.parentField;
        this.field.type = this.valueType;
        this.field.userCreated = true;
        this.field.namespace = this.cfg.targetDocs[0].getNamespaceForAlias(this.namespaceAlias);
        this.field.serviceObject.jsonType = "io.atlasmap.xml.v2.XmlField";
        return this.field;
    }
}
