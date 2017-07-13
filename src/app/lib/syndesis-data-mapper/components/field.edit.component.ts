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
import { ModalWindowValidator } from './modal.window.component';
import { DataMapperUtil } from '../common/data.mapper.util';

@Component({
    selector: 'field-edit',
    template: `
        <!-- our template for type ahead -->
        <template #typeaheadTemplate let-model="item" let-index="index">
            <h5 style="font-style:italic;" *ngIf="model['field'].docDef">{{ model['field'].docDef.name }}</h5>
            <h5>{{ model['field'].path }}</h5>
        </template>

        <div class="DataMapperEditComponent">
            <div class="form-group">
                <label>Parent</label>
                <input type="text" [(ngModel)]="parentFieldName" [typeahead]="dataSource"
                    typeaheadWaitMs="200" (typeaheadOnSelect)="parentSelectionChanged($event)" (blur)="handleOnBlur($event)"
                    typeaheadOptionField="displayName" [typeaheadItemTemplate]="typeaheadTemplate" disabled="{{editMode}}">
            </div>
            <div class="form-group">
                <label>Name</label>
                <input name="value" type="text" [(ngModel)]="field.name"/>
            </div>
            <div class="form-group" *ngIf="docDef.initCfg.type.isXML()">
                <label>Namespace</label>
                <select (change)="namespaceSelectionChanged($event);" [ngModel]="namespaceAlias">
                    <option *ngFor="let ns of namespaces" value="{{ns.alias}}" [selected]="namespaceAlias == ns.alias">
                        {{ ns.getPrettyLabel() }}
                    </option>
                </select>
            </div>
            <div class="form-group" *ngIf="docDef.initCfg.type.isXML()">
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

export class FieldEditComponent implements ModalWindowValidator {
    public cfg: ConfigModel = ConfigModel.getConfig();
    public field: Field = new Field();
    public parentField: Field = DocumentDefinition.getNoneField();
    public parentFieldName: String = null;
    public isSource: boolean = false;
    public fieldType: any = "element";
    public valueType: any = "STRING";
    public namespaceAlias: string = "";
    public editMode: boolean = false;
    public namespaces: NamespaceModel[] = [];
    public docDef: DocumentDefinition = null;

    public dataSource: Observable<any>;

    public constructor() {
        this.dataSource = Observable.create((observer: any) => {
            observer.next(this.executeSearch(observer.outerValue));
        });
    }

    public initialize(field: Field, docDef: DocumentDefinition, isAdd: boolean): void {
        this.docDef = docDef;
        this.editMode = !isAdd;
        this.field = field == null ? new Field() : field.copy();
        this.valueType = (this.field.type == null) ? "STRING" : this.field.type;
        this.parentField = (this.field.parentField == null) ? DocumentDefinition.getNoneField() : this.field.parentField;

        if (this.docDef.initCfg.type.isXML()) {
            this.fieldType = this.field.isAttribute ? "attribute" : "element";
            this.parentField = (this.field.parentField == null) ? docDef.fields[0] : this.field.parentField;
            var unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
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
            var namespaceFound: boolean = false;
            for (let ns of this.namespaces) {
                if (ns.alias == this.namespaceAlias) {
                    namespaceFound = true;
                    break;
                }
            }
            if (!namespaceFound) {
                var fakeNamespace: NamespaceModel = new NamespaceModel();
                fakeNamespace.alias = this.namespaceAlias;
                this.namespaces.push(fakeNamespace);
            }
        }
        this.parentFieldName = this.parentField.name;
    }

    public handleOnBlur(event: any): void {
        this.parentFieldName = this.parentField.name;
    }

    public parentSelectionChanged(event: any): void {
        var oldParentField: Field = this.parentField;
        this.parentField = event.item["field"];
        this.parentField = (this.parentField == null) ? oldParentField : this.parentField;
        this.parentFieldName = this.parentField.name;

        // change namespace dropdown selecte option to match parent fields' namespace automatically
        var unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
        this.namespaceAlias = this.parentField.namespaceAlias == null ? unqualifiedNS.alias : this.parentField.namespaceAlias;
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

        if (this.docDef.initCfg.type.isJSON()) {
            var noneField: Field = DocumentDefinition.getNoneField();
            formattedFields.push({ "field": noneField, "displayName": noneField.getFieldLabel(true) });
        }

        for (let field of this.docDef.getAllFields()) {
            if (!field.isParentField()) {
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
        this.field.parentField = this.parentField;
        this.field.type = this.valueType;
        this.field.userCreated = true;
        this.field.serviceObject.jsonType = "io.atlasmap.json.v2.JsonField";
        if (this.docDef.initCfg.type.isXML()) {
            this.field.isAttribute = (this.fieldType == "attribute");
            this.field.namespaceAlias = this.namespaceAlias;
            var unqualifiedNS: NamespaceModel = NamespaceModel.getUnqualifiedNamespace();
            if (this.namespaceAlias == unqualifiedNS.alias) {
                this.field.namespaceAlias = null;
            }
            this.field.serviceObject.jsonType = "io.atlasmap.xml.v2.XmlField";
        }
        return this.field;
    }

    isDataValid(): boolean {
        return DataMapperUtil.isRequiredFieldValid(this.field.name, "Name");
    }
}
