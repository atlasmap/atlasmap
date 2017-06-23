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

import { Component, Input, OnInit, ElementRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { DomSanitizer, SafeResourceUrl, SafeUrl, SafeStyle} from '@angular/platform-browser';

import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';
import { Field } from '../models/field.model';

import { MappingManagementService } from '../services/mapping.management.service';
import { DocumentManagementService } from '../services/document.management.service';

import { DocumentDefinitionComponent } from './document.definition.component';
import { DocumentFieldDetailComponent } from './document.field.detail.component';

export class LineModel {
    public sourceX: string;
    public sourceY: string;
    public targetX: string;
    public targetY: string;
    public stroke: string = "url(#line-gradient-dormant)";
    public style: SafeStyle;
}

@Component({
    selector: 'line-machine',
    template: `
        <div class="LineMachineComponent" #lineMachineElement on-mousemove="drawLine($event)" style="height:100%; margin-top:6%;">
            <svg style="width:100%; height:100%;">
                <defs>
                    <linearGradient id='line-gradient-active' gradientUnits="userSpaceOnUse">
                        <stop stop-color='#0088ce'/>
                        <stop offset='100%' stop-color='#0088ce'/> <!-- was #bee1f4 -->
                    </linearGradient>
                    <linearGradient id='line-gradient-dormant' gradientUnits="userSpaceOnUse">
                        <stop stop-color='#8b8d8f'/>
                        <stop offset='100%' stop-color='#8b8d8f'/> <!-- was #EEEEEE -->
                    </linearGradient>
                </defs>
                <svg:line *ngFor="let l of lines"
                    [attr.x1]="l.sourceX" [attr.y1]="l.sourceY"
                    [attr.x2]="l.targetX" [attr.y2]="l.targetY"
                    shape-rendering="optimizeQuality"
                    [attr.style]="l.style"></svg:line>
                <svg:line *ngIf="lineBeingFormed && lineBeingFormed.targetY"
                    [attr.x1]="lineBeingFormed.sourceX" [attr.y1]="lineBeingFormed.sourceY"
                    [attr.x2]="lineBeingFormed.targetX" [attr.y2]="lineBeingFormed.targetY"
                    shape-rendering="optimizeQuality"
                    [attr.style]="lineBeingFormed.style"></svg:line>
            </svg>
        </div>
    `
})

export class LineMachineComponent {
    @Input() cfg: ConfigModel;
    @Input() docDefInput: DocumentDefinitionComponent;
    @Input() docDefOutput: DocumentDefinitionComponent;

    public lines: LineModel[] = [];
    public lineBeingFormed: LineModel;
    public drawingLine: boolean = false;
    public svgStyle: SafeStyle;
    private yOffset = 3;

    @ViewChild('lineMachineElement') lineMachineElement: ElementRef;

    constructor(private sanitizer: DomSanitizer, public detector: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.cfg.mappingService.mappingUpdated$.subscribe(() => {
            this.mappingChanged();
        });
    }

    public addLineFromParams(sourceX: string, sourceY: string, targetX: string, targetY: string, stroke: string): void {
        var l: LineModel = new LineModel();
        l.sourceX = sourceX;
        l.sourceY = sourceY;
        l.targetX = targetX;
        l.targetY = targetY;
        l.stroke = stroke;
        this.addLine(l);
    }

    public addLine(l: LineModel): void {
        //console.log("Add line", l);
        this.createLineStyle(l);
        this.lines.push(l);
    }

    private createLineStyle(l: LineModel): void {
        //angular2 will throw an error if we don't use this sanitizer to signal to angular2 that the css style value is ok.
        l.style = this.sanitizer.bypassSecurityTrustStyle("stroke:" + l.stroke + "; stroke-width:4px;");
    }

    public setLineBeingFormed(l: LineModel): void {
        if (l != null) {
            this.createLineStyle(l);
        }
        this.lineBeingFormed = l;
    }

    public clearLines(): void {
        this.lines = [];
    }

    public drawLine(event: MouseEvent): void {
        this.drawCurrentLine(event.offsetX.toString(), event.offsetY.toString());
    }

    public drawCurrentLine(x: string, y: string): void {
        if (this.drawingLine && this.lineBeingFormed) {
            this.lineBeingFormed.targetX = x;
            this.lineBeingFormed.targetY = y;
        }
    }

    public handleDocumentFieldMouseOver(component: DocumentFieldDetailComponent, event: MouseEvent, isSource: boolean): void {
        if (!this.drawingLine) {
            return;
        }
        if (isSource) {
            return;
        }
        //console.log("Drawing current line from mouse over.");
        var targetY = this.docDefOutput.getFieldDetailComponentPosition(component.field).y;
        this.drawCurrentLine("100%", (targetY + this.yOffset).toString());
    }

    public mappingChanged(): void {
        var mappingIsNew: boolean = false;
        if (!mappingIsNew) {
            //console.log("Mapping is not new, active line drawing turned off.");
            this.drawingLine = false;
            this.setLineBeingFormed(null);
        } else {
        /*
            var mapping: MappingModel = this.cfg.mappings.activeMapping;
            var inputPaths: string[] = mapping.getMappedFieldPaths(true);
            var outputPaths: string[] = mapping.getMappedFieldPaths(false);
            var inputSelected: boolean = (inputPaths.length == 1);
            var outputSelected: boolean = (outputPaths.length == 1);
            if ((inputSelected && !outputSelected) || (!inputSelected && outputSelected) ) {
                //console.log("active line drawing turned on");
                var l: LineModel = new LineModel();
                var pos: any = null;
                if (inputSelected) {
                    var fieldPathToFind: string = inputPaths[0];
                    pos = this.docDefInput.getFieldDetailComponentPosition(fieldPathToFind);
                    l.sourceX = "0";
                } else {
                    var fieldPathToFind: string = outputPaths[0];
                    pos = this.docDefOutput.getFieldDetailComponentPosition(fieldPathToFind);
                    l.sourceX = "100%";
                }
                if (pos != null) {
                    l.sourceY = (pos.y + this.yOffset).toString();
                    this.setLineBeingFormed(l);
                    this.drawingLine = true;
                }
            }
        */
        }
        this.redrawLinesForMappings();
    }

    public redrawLinesForMappings(): void {
        if (!this.cfg.initCfg.initialized) {
            //console.log("Not drawing lines, system is not yet initialized.");
            return;
        }
        //console.log("Drawing lines");
        if (!this.cfg.mappings.activeMapping) {
            //console.log("No active mapping for line drawing.");
            this.setLineBeingFormed(null);
        }
        this.clearLines();
        var mappings: MappingModel[] = this.cfg.mappings.mappings;
        var activeMapping: MappingModel = this.cfg.mappings.activeMapping;
        var foundSelectedMapping: boolean = false;
        for (let m of mappings) {
            //console.log("Drawing line for mapping.", m);
            foundSelectedMapping = foundSelectedMapping || (m == activeMapping);
            this.drawLinesForMapping(m);
        }
        if (!foundSelectedMapping && activeMapping) {
            this.drawLinesForMapping(activeMapping);
        }
        setTimeout(() => {
            this.detector.detectChanges();
        }, 10);
    }

    private drawLinesForMapping(m: MappingModel): void {
        var el: any = this.lineMachineElement.nativeElement;
        var lineMachineHeight: number = el.offsetHeight;

        var isSelectedMapping: boolean = (this.cfg.mappings.activeMapping == m);
        var stroke: string = "url(#line-gradient-" + (isSelectedMapping ? "active" : "dormant") + ")";
        for (let fieldPair of m.fieldMappings) {
            if (!fieldPair.sourceFields.length || !fieldPair.targetFields.length) {
                //console.log("Not drawing lines for mapping, source or target fields are empty.", fieldPair);
                return;
            }

            for (let mappedInputField of fieldPair.sourceFields) {
                var inputField: Field = mappedInputField.field;
                if (!this.checkFieldEligibiltyForLineDrawing(inputField, "input", m)) {
                    continue;
                }

                var inputFieldPos: any = this.docDefInput.getFieldDetailComponentPosition(inputField);
                if (inputFieldPos == null) {
                    //console.log("Cant find screen position for input field, not drawing line: " + inputField.path);
                    continue;
                }
                var sourceY: number = inputFieldPos.y;

                if ((sourceY < 16) || (sourceY > (lineMachineHeight - 40))) {
                    //console.log("Not drawing line, input line coords are out of bounds.", sourceY);
                    continue;
                }

                for (let mappedOutputField of fieldPair.targetFields) {
                    var outputField: Field = mappedOutputField.field;
                    if (!this.checkFieldEligibiltyForLineDrawing(outputField, "output", m)) {
                        continue;
                    }

                    var outputFieldPos: any = this.docDefOutput.getFieldDetailComponentPosition(outputField);
                    if (outputFieldPos == null) {
                        //console.log("Cant find screen position for output field, not drawing line: " + outputField.path);
                        continue;
                    }
                    var targetY: number = outputFieldPos.y;
                    if ((targetY < 16) || (targetY > (lineMachineHeight - 40))) {
                        //console.log("Not drawing line, output line coords are out of bounds.", targetY);
                        continue;
                    }

                    if (isSelectedMapping || (this.cfg.showLinesAlways)) {
                        this.addLineFromParams("0", (sourceY + this.yOffset).toString(),
                            "100%", (targetY + this.yOffset).toString(), stroke);
                    }
                }
            }
        }
    }

    private checkFieldEligibiltyForLineDrawing(field: Field, description: string, m: MappingModel): boolean {
        if (!field) {
            //console.error("Not drawing line, " + description + " field can't be found: " + field.path, m);
            return false;
        }
        if (!field.visibleInCurrentDocumentSearch) {
            //console.log("Not drawing line, " + description + " field isn't visible: " + field.path, m);
            return false;
        }
        var parentField: Field = field.parentField;
        while (parentField != null) {
            if (parentField.collapsed) {
                //console.log("Not drawing line, " + description + " field's parent is collapsed: "  + field.path, m);
                return false;
            }
            parentField = parentField.parentField;
        }
        return true;
    }
}
