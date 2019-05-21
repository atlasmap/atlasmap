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
import { Component, ViewChild, Input, HostListener, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingModel, FieldMappingPair, MappedField } from '../models/mapping.model';
import { ExpressionModel, FieldNode } from '../models/expression.model';
import { Field } from '../models/field.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'expression',
  templateUrl: 'expression.component.html'
})

export class ExpressionComponent implements OnInit, OnDestroy {

  @Input()
  configModel: ConfigModel;

  @ViewChild('expressionMarkupRef')
  markup: ElementRef;

  @ViewChild('expressionSearch')
  expressionSearch: ElementRef;

  mappedFieldCandidates = [];

  private atIndex = 0;
  private searchFilter = '';
  private searchMode = false;
  private mapping: FieldMappingPair;
  private expressionUpdatedSubscription: Subscription;

  ngOnInit() {
    this.mapping = this.configModel.mappings.activeMapping.getCurrentFieldMapping();
    if (!this.mapping.transition.expression) {
      this.mapping.transition.expression = new ExpressionModel(this.mapping);
    }
    this.mapping.transition.expression.updateFieldReference(this.mapping);
    this.expressionUpdatedSubscription = this.mapping.transition.expression.expressionUpdated$.subscribe(() => {
      this.updateExpressionMarkup();
      this.moveCaretToEnd();
    });
    this.updateExpressionMarkup();
    this.moveCaretToEnd();
  }

  ngOnDestroy() {
    if (this.expressionUpdatedSubscription) {
      this.expressionUpdatedSubscription.unsubscribe();
    }
  }

  @HostListener('keydown', ['$event'])
  onKeydown(event: KeyboardEvent) {
    const expression = this.mapping.transition.expression;
    if ('Enter' === event.key) {
      event.preventDefault();
    } else if ('Backspace' === event.key) {
      // TODO handle cursor position
      event.preventDefault();
      expression.removeLastToken(removedMfield => {
        this.mapping.removeMappedField(removedMfield, true);
        this.configModel.mappingService.updateMappedField(this.mapping, true, true);
      });
      if (this.searchMode) {
        if (this.searchFilter.length === 0) {
          this.mappedFieldCandidates = [];
        } else {
          this.searchFilter = this.searchFilter.substr(0, this.searchFilter.length - 1);
        }
      }
    } else if ('Delete' === event.key) {
      // TODO
    }
  }

  @HostListener('keypress', ['$event'])
  onKeypress(event: KeyboardEvent) {
    if (event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    if (event.key.length > 1) {
      return;
    }

    event.preventDefault();

    if (this.searchMode && event.key.match(/[a-z0-9]/i)) {
      this.searchFilter += event.key;
      this.mappedFieldCandidates = this.executeSearch(this.searchFilter);
    } else {
      this.searchMode = (event.key === '@') ? true : false;
      const lastNode = this.mapping.transition.expression.getLastNode();
      if (lastNode) {
        this.atIndex = lastNode.toText().length;
      } else {
        this.atIndex = 0;
      }
    }

    const range = window.getSelection().getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;
    const endContainer = range.endContainer;
    const endOffset = range.endOffset;
    // TODO handle cursor position... for now just append to the end
    this.mapping.transition.expression.addText(event.key);
  }

  @HostListener('cut', ['$event'])
  onCut(event: ClipboardEvent) {
    // TODO remove only selected area
    this.mapping.transition.expression.clear();
  }

  @HostListener('paste', ['$event'])
  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasted = event.clipboardData.getData('text/plain')
      || window['clipboardData'].getData('Text');
    // TODO handle cursor position... for now just append to the end
    this.mapping.transition.expression.addText(pasted);
  }

  @HostListener('dragover', ['$event'])
  allowDrop(event: any): void {
    if (event.preventDefault) {
      event.preventDefault();
    }
    if (event.stopPropagation) {
      event.stopPropagation();
    }
  }

  @HostListener('drop', ['$event'])
  endDrag(event: MouseEvent): void {

    const droppedField: Field = this.configModel.currentDraggedField;
    const currentFieldMapping = this.configModel.mappings.activeMapping.getCurrentFieldMapping();
    if (droppedField === null || currentFieldMapping === null || !droppedField.isSource) {
      return;
    }

    if (droppedField.partOfMapping) {

      // The selected field is part of a different mapping.
      if (!currentFieldMapping.isFieldMapped(droppedField)) {
        return;
      }

    // Pulling an unmapped field into a transition expression evaluation implies a compound selection.
    } else {
      this.configModel.mappingService.fieldSelected(droppedField, true);
    }
    const mappedField = currentFieldMapping.getMappedFieldForField(droppedField, true);
    // TODO handle drop position - for now just append to the end
    this.addConditionExpressionNode(mappedField);
  }

  private addConditionExpressionNode(mappedField: MappedField): void {
    this.configModel.mappings.activeMapping.getCurrentFieldMapping().transition.expression.addNode(new FieldNode(mappedField));
  }

  private updateExpressionMarkup() {
    this.markup.nativeElement.innerHTML = this.mapping.transition.expression.toHTML();
  }

  private setSelectionRange(startNode: Node, startOffset: number, endNode: Node, endOffset: number) {
    this.markup.nativeElement.focus();
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    range.setStart(startNode, startOffset);
    range.setEnd(endNode, endOffset);
  }

  private moveCaretToEnd() {
    this.markup.nativeElement.focus();
    const lastNodeIndex = this.markup.nativeElement.childNodes.length - 1;
    const lastNode = lastNodeIndex > -1 ? this.markup.nativeElement.childNodes[lastNodeIndex] : this.markup.nativeElement;
    const range = window.getSelection().getRangeAt(0);
    range.selectNode(lastNode);
    range.collapse(false);
  }

  /**
   * Return an array of strings representing display names of active mapping fields based on the
   * specified filter.
   *
   * @param filter
   */
  executeSearch(filter: string): any[] {
    const currentFieldMapping = this.configModel.mappings.activeMapping.getCurrentFieldMapping();
    const formattedFields: any[] = [];
    let fields: Field[] = [DocumentDefinition.getNoneField()];
    for (const docDef of this.configModel.getDocs(true)) {
      fields = fields.concat(docDef.getTerminalFields());
    }
    const activeMapping: MappingModel = this.configModel.mappings.activeMapping;
    for (const field of fields) {
      let displayName = (field == null) ? '' : field.getFieldLabel(ConfigModel.getConfig().showTypes, true);

      if (filter == null || filter === '' || displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        if (!activeMapping.isFieldSelectable(field)) {
          continue;
        }
        displayName = field.path;
        const formattedField: any = { 'field': field, 'displayName': displayName };
        formattedFields.push(formattedField);
      }
      if (formattedFields.length > 9) {
        break;
      }
    }
    return formattedFields;
  }

  /**
   * The user has selected a field from the type-ahead pull-down.
   *
   * @param event
   */
  selectionChanged(event: any, index: number): void {
    const currentFieldMapping = this.configModel.mappings.activeMapping.getCurrentFieldMapping();
    const selectedField = this.mappedFieldCandidates[index].field;
    let mappedField = currentFieldMapping.getMappedFieldForField(selectedField, true);
    this.mapping.transition.expression.clearToEnd(this.atIndex);

    // If the selected field was not part of the original mapping then add it now.
    if (mappedField === null) {
      mappedField = currentFieldMapping.addField(selectedField, true, false);
      this.configModel.mappingService.updateMappedField(currentFieldMapping, true, false);
    }
    this.addConditionExpressionNode(mappedField);
    this.atIndex = 0;
    this.mappedFieldCandidates = [];
    this.searchFilter = '';
    this.markup.nativeElement.focus();
  }

}
