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

import { Component, OnInit, ViewChild, ViewEncapsulation, ChangeDetectorRef } from '@angular/core';
import { ConfigModel } from '../models/config.model';

import { ToolbarComponent } from './toolbar.component';
import { DataMapperErrorComponent } from './data.mapper.error.component';
import { LineMachineComponent } from './line.machine.component';
import { ModalWindowComponent } from './modal.window.component';

import { DocumentDefinitionComponent } from './document.definition.component';

import { MappingDetailComponent } from './mapping/mapping.detail.component';

@Component({
    selector: 'data-mapper',
    moduleId: module.id,
    encapsulation: ViewEncapsulation.None,
    templateUrl: './data.mapper.app.component.html',
    styleUrls: ['data.mapper.app.component.css'],
})

export class DataMapperAppComponent implements OnInit {

    @ViewChild('lineMachine') lineMachine: LineMachineComponent;
    @ViewChild('errorPanel') errorPanel: DataMapperErrorComponent;
    @ViewChild('modalWindow') modalWindow: ModalWindowComponent;
    @ViewChild('docDefInput') docDefInput: DocumentDefinitionComponent;
    @ViewChild('docDefOutput') docDefOutput: DocumentDefinitionComponent;
    @ViewChild('mappingDetailComponent') mappingDetailComponent: MappingDetailComponent;
    @ViewChild('toolbarComponent') toolbarComponent: ToolbarComponent;

    public loadingStatus = 'Loading.';

    constructor(public detector: ChangeDetectorRef) {}

    public getConfig(): ConfigModel {
        return ConfigModel.getConfig();
    }

    ngOnInit(): void {
        this.getConfig().initializationService.systemInitialized$.subscribe(() => {
            this.updateFromConfig();
        });

        this.getConfig().initializationService.initializationStatusChanged$.subscribe(() => {
            this.loadingStatus = this.getConfig().initCfg.loadingStatus;
            setTimeout(() => {
                this.detector.detectChanges();
            }, 10);
        });
    }

    public updateFromConfig(): void {
        // update the mapping line drawing after our fields have redrawn themselves
        // without this, the x/y from the field dom elements is messed up / misaligned.
        setTimeout(() => { this.lineMachine.redrawLinesForMappings(); }, 1);
    }
}
