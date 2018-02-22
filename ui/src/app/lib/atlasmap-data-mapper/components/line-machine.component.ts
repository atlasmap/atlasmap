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

import { ChangeDetectorRef, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

import { ConfigModel } from '../models/config.model';
import { MappingModel } from '../models/mapping.model';
import { Field } from '../models/field.model';

import { DocumentDefinitionComponent } from './document-definition.component';
import { DocumentFieldDetailComponent } from './document-field-detail.component';

export class LineModel {
  sourceX: string;
  sourceY: string;
  targetX: string;
  targetY: string;
  stroke = 'url(#line-gradient-dormant)';
  style: SafeStyle;
}

@Component({
  selector: 'line-machine',
  templateUrl: './line-machine.component.html',
})

export class LineMachineComponent implements OnInit {
  @Input() cfg: ConfigModel;
  @Input() docDefInput: DocumentDefinitionComponent;
  @Input() docDefOutput: DocumentDefinitionComponent;

  lines: LineModel[] = [];
  lineBeingFormed: LineModel;
  drawingLine = false;
  svgStyle: SafeStyle;

  @ViewChild('lineMachineElement') lineMachineElement: ElementRef;

  private yOffset = 3;

  constructor(private sanitizer: DomSanitizer, public detector: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.cfg.mappingService.mappingUpdated$.subscribe(() => {
      this.mappingChanged();
    });
  }

  addLineFromParams(sourceX: string, sourceY: string, targetX: string, targetY: string, stroke: string): void {
    const l: LineModel = new LineModel();
    l.sourceX = sourceX;
    l.sourceY = sourceY;
    l.targetX = targetX;
    l.targetY = targetY;
    l.stroke = stroke;
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
    const mappingIsNew = false;
    if (!mappingIsNew) {
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

  redrawLinesForMappings(): void {
    if (!this.cfg.initCfg.initialized) {
      return;
    }
    if (!this.cfg.mappings.activeMapping) {
      this.setLineBeingFormed(null);
    }
    this.clearLines();
    const mappings: MappingModel[] = this.cfg.mappings.mappings;
    const activeMapping: MappingModel = this.cfg.mappings.activeMapping;
    let foundSelectedMapping = false;
    for (const m of mappings) {
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

  private createLineStyle(l: LineModel): void {
    //angular2 will throw an error if we don't use this sanitizer to signal to angular2 that the css style value is ok.
    l.style = this.sanitizer.bypassSecurityTrustStyle('stroke:' + l.stroke + '; stroke-width:4px;');
  }

  private drawLinesForMapping(m: MappingModel): void {
    const el: any = this.lineMachineElement.nativeElement;
    const lineMachineHeight: number = el.offsetHeight;

    const isSelectedMapping: boolean = (this.cfg.mappings.activeMapping == m);
    const stroke: string = 'url(#line-gradient-' + (isSelectedMapping ? 'active' : 'dormant') + ')';
    for (const fieldPair of m.fieldMappings) {
      if (!fieldPair.sourceFields.length || !fieldPair.targetFields.length) {
        return;
      }

      for (const mappedInputField of fieldPair.sourceFields) {
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

        for (const mappedOutputField of fieldPair.targetFields) {
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
              '100%', (targetY + this.yOffset).toString(), stroke);
          }
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
