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

import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';
import { DocumentDefinition, DocumentTypes, DocumentType } from '../models/document.definition.model';

import { LineMachineComponent } from './line.machine.component';
import { ModalWindowComponent } from './modal.window.component';
import { TemplateEditComponent } from './template.edit.component';

@Component({
selector: 'dm-toolbar',
template: `
    <div class="dm-toolbar">
        <div class="dm-toolbar-icons" style="float:right;">
            <i class="fa fa-plus link" (click)="toolbarButtonClicked('addMapping');"></i>
            <i [attr.class]="getCSSClass('editTemplate')"  *ngIf="targetSupportsTemplate()"
                (click)="toolbarButtonClicked('editTemplate');"></i>
            <i [attr.class]="getCSSClass('showMappingTable')" (click)="toolbarButtonClicked('showMappingTable');"></i>
            <i *ngIf="cfg.getFirstXmlDoc(false)" [attr.class]="getCSSClass('showNamespaceTable')" 
                (click)="toolbarButtonClicked('showNamespaceTable');"></i>
            <i [attr.class]="getCSSClass('showDetails')" (click)="toolbarButtonClicked('showDetails');"></i>            
            <div dropdown placement="bottom right" style="display:inline; position:relative;">
                <i [attr.class]="getCSSClass('advancedMode')" dropdownToggle (click)="false"></i>
                <!-- <a href dropdownToggle (click)="false">X</a> -->
                <ul *dropdownMenu class="dropdown-menu dropdown-menu-right" role="menu">
                    <li role="menuitem" (click)="toolbarButtonClicked('showTypes');">
                        <div style="float:left">
                            <a class="dropdown-item" href="#">
                                <i class="fa fa-tag"></i>Show Types
                            </a>
                        </div>                        
                        <i class="fa fa-check" *ngIf="cfg.showTypes" style="float:right"></i>
                        <div class="clear"></div>
                    </li>
                    <li role="menuitem" (click)="toolbarButtonClicked('showLines');">
                        <div style="float:left">
                            <a class="dropdown-item" href="#">
                                <i class="fa fa-share-alt"></i>Show Lines
                            </a>
                        </div>                        
                        <i class="fa fa-check" *ngIf="cfg.showLinesAlways" style="float:right"></i>
                        <div class="clear"></div>
                    </li>
                    <li role="menuitem" (click)="toolbarButtonClicked('showMappedFields');">
                        <div style="float:left">
                            <a class="dropdown-item" href="#">
                                <i class="fa fa-chain"></i>Show Mapped Fields
                            </a>
                        </div>                        
                        <i class="fa fa-check" *ngIf="cfg.showMappedFields" style="float:right"></i>
                        <div class="clear"></div>
                    </li>
                    <li role="menuitem" (click)="toolbarButtonClicked('showUnmappedFields');">
                        <div style="float:left">
                            <a class="dropdown-item" href="#">
                                <i class="fa fa-chain-broken"></i>Show Unmapped Fields
                            </a>
                        </div>                        
                        <i class="fa fa-check" *ngIf="cfg.showUnmappedFields" style="float:right"></i>
                        <div class="clear"></div>
                    </li>
                </ul>
            </div>
        </div>
        <div style="clear:both; height:0px;"></div>
    </div>
`
})

export class ToolbarComponent {
    @Input() cfg: ConfigModel;
    @Input() lineMachine: LineMachineComponent;
    @Input() modalWindow: ModalWindowComponent;

    private getCSSClass(action: string) {
        if ("showDetails" == action) {
            return "fa fa-exchange link" + (this.cfg.mappings.activeMapping ? " selected" : "");
        } else if ("showLines" == action) {
            return  "fa fa-share-alt link" + (this.cfg.showLinesAlways ? " selected" : "");
        } else if ("advancedMode" == action) {
            var clz: string = "fa fa-cog link "
            if (this.cfg.showLinesAlways || this.cfg.showTypes 
                || !this.cfg.showMappedFields || !this.cfg.showUnmappedFields) {
                clz += "selected";
            }
            return clz;
        } else if ("showMappingTable" == action) {
            return "fa fa-table link" + (this.cfg.showMappingTable ? " selected" : "");
        } else if ("showNamespaceTable" == action) {
            return "fa fa-code link" + (this.cfg.showNamespaceTable ? " selected" : "");
        } else if ("editTemplate" == action) {
            return "fa fa-file-text-o link" + (this.cfg.mappings.templateExists() ? " selected" : "");
        }
    }

    public targetSupportsTemplate(): boolean {
        var targetDoc: DocumentDefinition = this.cfg.targetDocs[0];
        return targetDoc.initCfg.type.isXML() || targetDoc.initCfg.type.isJSON();
    }

    public toolbarButtonClicked(action: string): void {
        if ("showDetails" == action) {
            if (this.cfg.mappings.activeMapping == null) {
                this.cfg.mappingService.addNewMapping(null);
                this.cfg.mappings.activeMapping.brandNewMapping = true;
            } else {
                this.cfg.mappingService.deselectMapping();
            }
        } else if ("editTemplate" == action) {
            this.editTemplate();
        } else if ("showLines" == action) {
            this.cfg.showLinesAlways = !this.cfg.showLinesAlways;
            this.lineMachine.redrawLinesForMappings();
        } else if ("showTypes" == action) {
            this.cfg.showTypes = !this.cfg.showTypes;
        } else if ("showMappedFields" == action) {
            this.cfg.showMappedFields = !this.cfg.showMappedFields;
        } else if ("showUnmappedFields" == action) {
            this.cfg.showUnmappedFields = !this.cfg.showUnmappedFields;
        } else if ("addMapping" == action) {
            this.cfg.mappingService.addNewMapping(null);
        } else if ("showMappingTable" == action) {
            this.cfg.showMappingTable = !this.cfg.showMappingTable;
            if (!this.cfg.showMappingTable) {
                setTimeout(() => {
                    this.lineMachine.redrawLinesForMappings();
                }, 10);
            } else {
                this.cfg.showNamespaceTable = false;
            }
        } else if ("showNamespaceTable" == action) {
            this.cfg.showNamespaceTable = !this.cfg.showNamespaceTable;
            if (!this.cfg.showNamespaceTable) {
                setTimeout(() => {
                    this.lineMachine.redrawLinesForMappings();
                }, 10);
            } else {
                this.cfg.showMappingTable = false;
            }
        }        
    }

    private editTemplate(): void {
        var self: ToolbarComponent = this;
        this.modalWindow.reset();
        this.modalWindow.confirmButtonText = "Save";
        this.modalWindow.parentComponent = this;
        this.modalWindow.headerText = this.cfg.mappings.templateExists() ? "Edit Template" : "Add Template";
        this.modalWindow.nestedComponentInitializedCallback = (mw: ModalWindowComponent) => {
            var templateComponent: TemplateEditComponent = mw.nestedComponent as TemplateEditComponent;
            templateComponent.templateText = this.cfg.mappings.templateText;
        };
        this.modalWindow.nestedComponentType = TemplateEditComponent;
        this.modalWindow.okButtonHandler = (mw: ModalWindowComponent) => {
            var templateComponent: TemplateEditComponent = mw.nestedComponent as TemplateEditComponent;
            this.cfg.mappings.templateText = templateComponent.templateText;
            self.cfg.mappingService.saveCurrentMapping();
        };
        this.modalWindow.show();
    }
}

