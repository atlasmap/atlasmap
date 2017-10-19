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

import { Component, Input, ViewChildren, Injectable, QueryList, ViewChild, OnInit } from '@angular/core';

import { ConfigModel } from '../../models/config.model';
import { Field } from '../../models/field.model';
import { MappingModel, FieldMappingPair, MappedField } from '../../models/mapping.model';
import { TransitionModel, TransitionMode, TransitionDelimiter } from '../../models/transition.model';
import { DocumentDefinition } from '../../models/document.definition.model';
import { ErrorInfo } from '../../models/error.model';

import { MappingManagementService } from '../../services/mapping.management.service';
import { DocumentManagementService } from '../../services/document.management.service';

import { DataMapperAppComponent } from '../data.mapper.app.component';

import { ModalWindowComponent } from '../modal.window.component';
import { CollapsableHeaderComponent } from '../collapsable.header.component';

import { MappingFieldDetailComponent } from './mapping.field.detail.component';
import { MappingFieldActionComponent } from './mapping.field.action.component';
import { TransitionSelectionComponent } from './transition.selection.component';
import { MappingSelectionComponent } from './mapping.selection.component';

@Component({
    selector: 'simple-mapping',
    template: `
        <div class="mappingFieldContainer" *ngIf="fieldPair">
            <div *ngFor="let mappedField of fieldPair.getMappedFields(isSource)" class="MappingFieldSection">
                <!-- header label / trash icon -->
                <div style="float:left;"><label>{{ getTopFieldTypeLabel() }}</label></div>
                <div style="float:right; margin-right:5px;">
                    <i class="fa fa-trash link" aria-hidden="true"
                        (click)="removeMappedField(mappedField)"></i>
                </div>
                <div class="clear"></div>

                <mapping-field-detail [fieldPair]="fieldPair" [cfg]="cfg" [isSource]="isSource" [mappedField]="mappedField"></mapping-field-detail>
                <mapping-field-action [mappedField]="mappedField" [cfg]="cfg" [isSource]="isSource"
                    [fieldPair]="fieldPair"></mapping-field-action>
            </div>
            <!-- add button -->
            <div class="linkContainer" *ngIf="isAddButtonVisible()">
                <a (click)="addClicked()" class="small-primary">{{ getAddButtonLabel() }}</a>
            </div>
        </div>
    `
})

export class SimpleMappingComponent {
    @Input() cfg: ConfigModel;
    @Input() isSource: boolean = false;
    @Input() fieldPair: FieldMappingPair;

    public isAddButtonVisible(): boolean {
        if (this.isSource && this.fieldPair.transition.isCombineMode()) {
            return true;
        } else if (!this.isSource && this.fieldPair.transition.isSeparateMode()) {
            return true;
        }
        return false;
    }

    public getTopFieldTypeLabel(): string {
        return this.isSource ? "Source" : "Target";
    }

    public getAddButtonLabel(): string {
        return this.isSource ? "Add Source" : "Add Target"
    }

    public addClicked(): void {
        this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource);
        this.cfg.mappingService.updateMappedField(this.fieldPair);
    }

    public removePair(): void {
        this.cfg.mappingService.removeMappedPair(this.fieldPair);
    }

    public removeMappedField(mappedField: MappedField): void {
        this.fieldPair.removeMappedField(mappedField, this.isSource);
        if (this.fieldPair.getMappedFields(this.isSource).length == 0) {
            this.fieldPair.addField(DocumentDefinition.getNoneField(), this.isSource);
        }
        this.cfg.mappingService.updateMappedField(this.fieldPair);
    }
}

@Component({
    selector: 'collection-mapping',
    template: `
        <div class="collectionSectionContainer">
            <div [attr.class]="'collectionSection ' + getAnimationCSSClass()">
                <!-- collection field pairing detail -->
                <div style="float:left; width:50%; padding-top:10px;" class="mappingFieldContainer">
                    <div *ngFor="let fieldPair of cfg.mappings.activeMapping.fieldMappings">
                        <div class="MappingFieldSection">
                            <!-- header label / trash icon -->
                            <div style="float:left;">
                                <label>Source</label>
                                <i class="fa fa-bolt" style="font-size:12px; vertical-align:baseline;"
                                    *ngIf="fieldPair.hasTransition()"></i>
                            </div>
                            <div style="float:right; margin-right:5px; text-align:right">
                                <i class="fa fa-edit link" aria-hidden="true"
                                    (click)="editPair(fieldPair)"></i>
                                <i class="fa fa-trash link" aria-hidden="true" (click)="removePair(fieldPair)"></i>
                            </div>
                            <div class="clear"></div>

                            <mapping-field-detail *ngFor="let mappedField of fieldPair.getMappedFields(true)"
                                [mappedField]="mappedField" [fieldPair]="fieldPair" [cfg]="cfg" [isSource]="true"></mapping-field-detail>
                            <div style="float:left;"><label>Target</label></div>
                            <div class="clear"></div>
                            <mapping-field-detail *ngFor="let mappedField of fieldPair.getMappedFields(false)"
                                [mappedField]="mappedField" [fieldPair]="fieldPair" [cfg]="cfg" [isSource]="false"></mapping-field-detail>
                        </div>
                    </div>
                    <!-- add button -->
                    <div class="linkContainer">
                        <a (click)="addClicked()" class="small-primary">Add Mapping</a>
                    </div>
                </div>
                <div style="float:left; width:50%; margin:0; padding:0" *ngIf="fieldPairForEditing">
                    <div class="card-pf-title">
                        <div style="float:left">Edit Details</div>
                        <div style="float:right;">
                            <i class="fa fa-close link" aria-hidden="true" (click)="exitEditMode()"></i>
                        </div>
                        <div class="clear"></div>
                    </div>
                    <mapping-pair-detail [cfg]="cfg" [fieldPair]="fieldPairForEditing"
                        [modalWindow]="modalWindow"></mapping-pair-detail>
                </div>
                <div class="clear"></div>
            </div>
        </div>
    `
})

export class CollectionMappingComponent {
    @Input() cfg: ConfigModel;
    public fieldPairForEditing: FieldMappingPair = null;
    private animateLeft: boolean = false;
    private animateRight: boolean = false;

    public getAnimationCSSClass(): string {
        if (this.animateLeft) {
            return "dm-swipe-left collectionSectionLeft";
        } else if (this.animateRight) {
            return "dm-swipe-right";
        }
        return "";
    }

    public getFields(fieldPair: FieldMappingPair, isSource: boolean): Field[] {
        var fields: Field[] = fieldPair.getFields(isSource);
        return (fields.length > 0) ? fields : [DocumentDefinition.getNoneField()];
    }

    public addClicked(): void {
        this.cfg.mappingService.addMappedPair();
    }

    public editPair(fieldPair: FieldMappingPair): void {
        this.fieldPairForEditing = fieldPair;
        this.cfg.mappings.activeMapping.currentFieldMapping = fieldPair;
        this.animateLeft = true;
    }

    public exitEditMode(): void {
        this.fieldPairForEditing = null;
        this.animateLeft = false;
        this.animateRight = true;
        this.cfg.mappings.activeMapping.currentFieldMapping = null;
    }

    public removePair(fieldPair: FieldMappingPair): void {
        this.cfg.mappingService.removeMappedPair(fieldPair);
    }
}

@Component({
    selector: 'mapping-pair-detail',
    template: `
        <div>
            <collapsable-header title="Sources" #sourcesHeader class="sources"></collapsable-header>
            <simple-mapping [cfg]="cfg" [isSource]="true" *ngIf="!sourcesHeader.collapsed"
                [fieldPair]="fieldPair"></simple-mapping>
            <collapsable-header title="Action" #actionsHeader></collapsable-header>
            <transition-selector [cfg]="cfg" [modalWindow]="modalWindow"
                [fieldPair]="fieldPair" *ngIf="!actionsHeader.collapsed"></transition-selector>
            <collapsable-header title="Targets" #targetsHeader></collapsable-header>
            <simple-mapping [cfg]="cfg" [isSource]="false" *ngIf="!targetsHeader.collapsed"
                [fieldPair]="fieldPair"></simple-mapping>
        </div>
    `
})

export class MappingPairDetailComponent {
    @Input() cfg: ConfigModel;
    @Input() fieldPair: FieldMappingPair;
    @Input() modalWindow: ModalWindowComponent;

    @ViewChild('sourcesHeader')
    public sourcesHeader: CollapsableHeaderComponent;
    @ViewChild('actionsHeader')
    public actionsHeader: CollapsableHeaderComponent;
    @ViewChild('targetsHeader')
    public targetsHeader: CollapsableHeaderComponent;
}

@Component({
    selector: 'mapping-detail',
    template: `
        <div class='fieldMappingDetail' *ngIf="cfg.mappings.activeMapping && cfg.showMappingDetailTray">
            <div class="card-pf">
                <div class="card-pf-heading">
                    <h2 class="card-pf-title">
                        <div style="float:left;">{{ getTitle() }}</div>
                        <div style="float:right; text-align:right;">
                            <i class="fa fa-trash link" aria-hidden="true" (click)="removeMapping($event)"></i>
                        </div>
                        <div style="clear:both; height:0px;"></div>
                    </h2>
                </div>
                <div class="fieldMappingDetail-body">
                    <div class="alert alert-danger" *ngFor="let error of cfg.mappings.activeMapping.getValidationErrors()">
                        <a class="close" (click)="cfg.mappings.activeMapping.removeError(error.identifier)">
                            <i class="fa fa-close"></i>
                        </a>
                        <span class="pficon pficon-error-circle-o"></span>
                        <label>{{ error.message }}</label>
                    </div>
                    <div class="alert alert-warning" *ngFor="let warn of cfg.mappings.activeMapping.getValidationWarnings()">
                        <a class="close" (click)="cfg.mappings.activeMapping.removeError(warn.identifier)">
                            <i class="fa fa-close"></i>
                        </a>
                        <span class="pficon pficon-warning-triangle-o"></span>
                        <label>{{ warn.message }}</label>
                    </div>
                    <div *ngIf="!isMappingCollection()">
                        <mapping-pair-detail *ngFor="let fieldPair of cfg.mappings.activeMapping.fieldMappings"
                            [cfg]="cfg" [fieldPair]="fieldPair" [modalWindow]="modalWindow"></mapping-pair-detail>
                    </div>
                    <collection-mapping [cfg]="cfg" *ngIf="isMappingCollection()"></collection-mapping>
                </div>
            </div>
        </div>
    `
})

export class MappingDetailComponent implements OnInit {
    @Input() cfg: ConfigModel;
    @Input() modalWindow: ModalWindowComponent;

    ngOnInit(): void {
        this.cfg.mappingService.mappingSelectionRequired$.subscribe((field: Field) => {
            this.selectMapping(field);
        });
    }

    private getTitle(): string {
        if (this.cfg.mappings.activeMapping.isLookupMode()) {
            return "Lookup Mapping";
        }
        return this.isMappingCollection() ? "Repeating Mapping" : "Mapping Details";
    }

    public isMappingCollection(): boolean {
        return this.cfg.mappings.activeMapping.isCollectionMode();
    }

    private removeMapping(event: MouseEvent): void {
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Remove";
        this.modalWindow.headerText = "Remove Mapping?";
        this.modalWindow.message = "Are you sure you want to remove the current mapping?";
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            this.cfg.mappingService.removeMapping(this.cfg.mappings.activeMapping);
            this.cfg.showMappingDetailTray = false;
        };
        this.modalWindow.show();
    }

    private selectMapping(field:Field): void {
        var mappingsForField: MappingModel[] = this.cfg.mappings.findMappingsForField(field);
        var self: MappingDetailComponent = this;
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Select";
        this.modalWindow.headerText = "Select Mapping";
        this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
            var c: MappingSelectionComponent = mw.nestedComponent as MappingSelectionComponent;
            c.selectedField = field;
            c.cfg = self.cfg;
            c.mappings = mappingsForField;
            c.modalWindow = this.modalWindow;
        };
        this.modalWindow.nestedComponentType = MappingSelectionComponent;
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            var c: MappingSelectionComponent = mw.nestedComponent as MappingSelectionComponent;
            var mapping: MappingModel = c.getSelectedMapping();
            self.cfg.mappingService.selectMapping(mapping);
        };
        this.modalWindow.cancelButtonHandler = (mw: ModalWindowComponent) => {
            self.cfg.mappingService.selectMapping(null);
        };
        this.modalWindow.show();
    }
}
