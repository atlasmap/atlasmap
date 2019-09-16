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

import { ChangeDetectorRef, Component, ElementRef, Input, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

import { ConfigModel, AdmRedrawMappingLinesEvent } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';
import { Field } from '../models/field.model';

import { DocumentDefinitionComponent } from './document-definition.component';
import { DocumentFieldDetailComponent } from './document-field-detail.component';
import { Subscription } from 'rxjs';

export class LineModel {
  sourceX: string;
  sourceY: string;
  targetX: string;
  targetY: string;
  stroke = 'url(#line-gradient-dormant)';
  style: SafeStyle;
  targetField: Field;
}

@Component({
  selector: 'line-machine',
  templateUrl: './line-machine.component.html',
})

export class LineMachineComponent implements OnInit, OnDestroy {
  @Input() cfg: ConfigModel;
  @Input() docDefInput: DocumentDefinitionComponent;
  @Input() docDefOutput: DocumentDefinitionComponent;

  lines: LineModel[] = [];
  lineBeingFormed: LineModel;
  drawingLine = false;
  svgStyle: SafeStyle;

  @ViewChild('lineMachineElement') lineMachineElement: ElementRef;

  private yOffset = 3;
  private mappingUpdatedSubscription: Subscription;

  constructor(private sanitizer: DomSanitizer, public detector: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.mappingUpdatedSubscription = this.cfg.mappingService.mappingUpdated$.subscribe(() => {
      this.mappingChanged();
      this.docDefInput.setLineMachine(this);
      this.docDefOutput.setLineMachine(this);
    });
  }

  ngOnDestroy() {
    this.mappingUpdatedSubscription.unsubscribe();
  }

  handleRedrawMappingLinesEvent(event: AdmRedrawMappingLinesEvent): void {
    const lmcInstance: LineMachineComponent = event._lmcInstance;
    lmcInstance.redrawLinesForMappings();
  }

  /**
   * Match the line geometry of a selected line to determine the matching line model array element.  Return
   * the target field from that array element.
   *
   * @param selectedLineAttrs
   */
  private getTargetFieldFromLine(selectedLineAttrs: NamedNodeMap): Field {
    for (const line of this.lines) {
      if ((selectedLineAttrs[1].nodeValue === line.sourceX) &&
          (selectedLineAttrs[2].nodeValue === line.sourceY) &&
          (selectedLineAttrs[3].nodeValue === line.targetX) &&
          (selectedLineAttrs[4].nodeValue === line.targetY)) {
        return line.targetField;
      }
    }
    return null;
  }

  /**
   * The user has selected between the panels.  This is likely a line - verify it and select the fields
   * associated with the line.
   *
   * @param event
   */
  handleLineClick(event) {
    const selectedElement = event.target;
    if (selectedElement !== null && selectedElement.nodeName === 'line') {
      const targetField: Field = this.getTargetFieldFromLine(selectedElement.attributes);
      if (targetField != null) {
        this.cfg.mappingService.fieldSelected(targetField, false);
        setTimeout(() => {
          this.redrawLinesForMappings();
        }, 1);
      }
    }
  }

  addLineFromParams(sourceX: string, sourceY: string, targetX: string, targetY: string, stroke: string, targetField: Field): void {
    const l: LineModel = new LineModel();
    l.sourceX = sourceX;
    l.sourceY = sourceY;
    l.targetX = targetX;
    l.targetY = targetY;
    l.stroke = stroke;
    l.targetField = targetField;
    this.addLine(l);
  }

  addLine(l: LineModel): void {
    this.createLineStyle(l);
    this.lines.push(l);
  }

  setLineBeingFormed(l: LineModel): void {
    if (l != null) {
      this.createLineStyle(l);
    }
    this.lineBeingFormed = l;
  }

  clearLines(): void {
    this.lines = [];
    this.lineBeingFormed = null;
    this.drawingLine = false;
  }

  drawLine(event: MouseEvent): void {
    this.drawCurrentLine(event.offsetX.toString(), event.offsetY.toString());
  }

  drawCurrentLine(x: string, y: string): void {
    if (this.drawingLine && this.lineBeingFormed) {
      this.lineBeingFormed.targetX = x;
      this.lineBeingFormed.targetY = y;
    }
  }

  handleDocumentFieldMouseOver(component: DocumentFieldDetailComponent, event: any, isSource: boolean): void {
    if (!this.drawingLine) {
      return;
    }
    if (isSource) {
      return;
    }
    const targetY = this.docDefOutput.getFieldDetailComponentPosition(component.field).y;
    this.drawCurrentLine('100%', (targetY + this.yOffset).toString());
  }

  mappingChanged(): void {
    this.drawingLine = false;
    this.setLineBeingFormed(null);
    this.redrawLinesForMappings();
  }

  redrawLinesForMappings(): void {
    if (!this.cfg.initCfg.initialized || !this.cfg.mappings) {
      this.clearLines();

      // Clear any scroll deltas from the sources and targets panels.
      if (this.docDefOutput) {
        this.docDefInput.handleScroll(null);
      }
      if (this.docDefOutput) {
        this.docDefOutput.handleScroll(null);
      }
      return;
    }
    if (!this.cfg.mappings.activeMapping) {
      this.setLineBeingFormed(null);
    }
    this.clearLines();
    if (!this.cfg.showMappedFields) {
      return;
    }
    const mappings: MappingModel[] = this.cfg.mappings.mappings;
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    let foundSelectedMapping = false;
    for (const m of mappings) {
      foundSelectedMapping = foundSelectedMapping || (m === activeMapping);
      this.drawLinesForMapping(m);
    }
    if (!foundSelectedMapping && activeMapping) {
      this.drawLinesForMapping(activeMapping);
    }
    setTimeout(() => {
      this.detector.detectChanges();
    }, 10);
  }

  private createLineStyle(l: LineModel): void {
    // angular2 will throw an error if we don't use this sanitizer to signal to angular2 that the css style value is ok.
    l.style = this.sanitizer.bypassSecurityTrustStyle('stroke:' + l.stroke + '; stroke-width:4px;');
  }

  private drawLinesForMapping(m: MappingModel): void {
    const el: any = this.lineMachineElement.nativeElement;
    const lineMachineHeight: number = el.offsetHeight;

    const isSelectedMapping: boolean = (this.cfg.mappings.activeMapping === m);
    const stroke: string = 'url(#line-gradient-' + (isSelectedMapping ? 'active' : 'dormant') + ')';
    if (!m.sourceFields.length || !m.targetFields.length) {
      return;
    }

    for (const mappedInputField of m.sourceFields) {
      const inputField: Field = mappedInputField.field;
      if (!this.checkFieldEligibiltyForLineDrawing(inputField, 'input', m)) {
        continue;
      }

      const inputFieldPos: any = this.getScreenPosForField(inputField, this.docDefInput);
      if (inputFieldPos == null) {
        continue;
      }

      let sourceY: number = inputFieldPos.y;
      sourceY = (sourceY < 55) ? 55 : sourceY;
      sourceY = (sourceY > (lineMachineHeight - 27)) ? (lineMachineHeight - 27) : sourceY;

      for (const mappedOutputField of m.targetFields) {
        const outputField: Field = mappedOutputField.field;
        if (!this.checkFieldEligibiltyForLineDrawing(outputField, 'output', m)) {
          continue;
        }

        const outputFieldPos: any = this.getScreenPosForField(outputField, this.docDefOutput);
        if (outputFieldPos == null) {
          continue;
        }

        let targetY: number = outputFieldPos.y;
        targetY = (targetY < 55) ? 55 : targetY;
        targetY = (targetY > (lineMachineHeight - 27)) ? (lineMachineHeight - 27) : targetY;

        if (isSelectedMapping || (this.cfg.showLinesAlways)) {
          this.addLineFromParams('0', (sourceY + this.yOffset).toString(),
            '100%', (targetY + this.yOffset).toString(), stroke, outputField);
        }
      }
    }
  }

  private getScreenPosForField(field: Field, docDefComponent: DocumentDefinitionComponent): any {
    if (field == null || field.docDef == null) {
      return null;
    }
    if (!field.docDef.showFields) {
      const pos: any = docDefComponent.getDocDefElementPosition(field.docDef);
      if (pos) {
        pos['y'] = pos['y'] + 5;
      }
      return pos;
    }
    let parentField: Field = field;
    while (parentField != null) {
      const fieldPos: any = docDefComponent.getFieldDetailComponentPosition(parentField);
      if (fieldPos != null) {
        return fieldPos;
      }
      parentField = parentField.parentField;
    }
    return null;
  }

  private checkFieldEligibiltyForLineDrawing(field: Field, description: string, m: MappingModel): boolean {
    if (!field) {
      return false;
    }
    if (!field.visibleInCurrentDocumentSearch) {
      return false;
    }
    return true;
  }
}
