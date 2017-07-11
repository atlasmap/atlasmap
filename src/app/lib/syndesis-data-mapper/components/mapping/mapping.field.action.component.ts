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

import { Field } from '../../models/field.model';
import { MappingModel, FieldMappingPair, MappedField } from '../../models/mapping.model';
import { ConfigModel } from '../../models/config.model';
import { TransitionModel, FieldAction, FieldActionConfig } from '../../models/transition.model';

@Component({
    selector: 'mapping-field-action',
    template: `
        <div class="mappingFieldAction">
            <div class="actionContainer" *ngFor="let action of getMappedFieldActions(); let actionIndex = index">
                <div class="form-group">
                    <label style="float:left;">{{ getActionDescription(action) }}</label>
                    <div style="float:right; margin-right:5px;" *ngIf="!action.isSeparateOrCombineMode">
                        <i class="fa fa-trash link" aria-hidden="true" (click)="removeAction(action)"></i>
                    </div>
                    <div class="clear"></div>

                    <select (change)="configSelectionChanged($event);"
                        [ngModel]="action.name" *ngIf="!action.isSeparateOrCombineMode">
                        <option *ngFor="let actionConfig of getActionConfigs()"
                            [attr.actionIndex]="actionIndex"
                            [attr.value]="actionConfig.name">{{ actionConfig.name }}</option>
                    </select>
                    
                    <div class="clear"></div>
                </div>
                <div class="form-group argument" *ngFor="let argConfig of action.config.arguments; let i = index">
                    <label style="">{{ argConfig.name }}</label>
                    <input type="text" [(ngModel)]="action.getArgumentValue(argConfig.name).value" (change)="selectionChanged($event)"/>
                    <div class="clear"></div>
                </div>
            </div>
            <div *ngIf="actionsExistForField() && !isSource" class="linkContainer">
                <a (click)="addTransformation()" class="small-primary">Add Transformation</a>
            </div>
        </div>
    `
})

export class MappingFieldActionComponent {
    @Input() cfg: ConfigModel;
    @Input() mappedField: MappedField;
    @Input() isSource: boolean;
    @Input() fieldPair: FieldMappingPair;

    public getMappedFieldActions(): FieldAction[] {
        return this.mappedField.actions;
    }

    public getActionDescription(fieldAction: FieldAction): string {
        if (fieldAction.isSeparateOrCombineMode) {
            return fieldAction.config.name;
        }
        return "Transformation";
    }

    public actionsExistForField(): boolean {
        return (this.getActionConfigs().length > 0);
    }

    public getActionConfigs(): FieldActionConfig[] {
        var configs: FieldActionConfig[] = [];
        for (let config of TransitionModel.actionConfigs) {
            if (config.appliesToField(this.mappedField.field, this.fieldPair)) {
                configs.push(config);
            }
        }
        return configs;
    }

    public removeAction(action: FieldAction): void {
        this.mappedField.removeAction(action);
    }

    public selectionChanged(event: MouseEvent):void {
        this.cfg.mappingService.saveCurrentMapping();
    }

    public addTransformation(): void {
        var actionConfig: FieldActionConfig = this.getActionConfigs()[0];
        var action: FieldAction = new FieldAction();
        actionConfig.populateFieldAction(action);
        this.getMappedFieldActions().push(action);
        this.cfg.mappingService.saveCurrentMapping();
    }

    configSelectionChanged(event: MouseEvent) {
        var eventTarget: any = event.target; //extract this to avoid compiler error about 'selectedOptions' not existing.
        var attributes: any = eventTarget.selectedOptions.item(0).attributes;
        var selectedActionName: any = attributes.getNamedItem("value").value;
        var selectedActionIndex: any = attributes.getNamedItem("actionIndex").value;
        var action: FieldAction = this.getMappedFieldActions()[selectedActionIndex];
        if (action.name != selectedActionName) {
            var fieldActionConfig: FieldActionConfig = TransitionModel.getActionConfigForName(selectedActionName);
            fieldActionConfig.populateFieldAction(action);
        }
        this.cfg.mappingService.saveCurrentMapping();
    }
}
