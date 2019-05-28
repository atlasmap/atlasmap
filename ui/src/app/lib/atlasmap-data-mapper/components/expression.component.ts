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
import { ExpressionModel, FieldNode, ExpressionUpdatedEvent } from '../models/expression.model';
import { Field } from '../models/field.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'expression',
  templateUrl: 'expression.component.html'
})

export class ExpressionComponent implements OnInit, OnDestroy {

  static readonly trailerId = 'expression-trailer';

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
    if (!this.getExpression()) {
      this.mapping.transition.expression = new ExpressionModel(this.mapping);
      this.getExpression().generateInitialExpression();
    }
    this.getExpression().updateFieldReference(this.mapping);
    this.expressionUpdatedSubscription = this.getExpression().expressionUpdated$.subscribe((updatedEvent) => {
      this.updateExpressionMarkup();
      this.restoreCaretPosition(updatedEvent);
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
    if ('Enter' === event.key) {
      event.preventDefault();
    } else if ('Backspace' === event.key) {
      // TODO handle cursor position
      event.preventDefault();
      this.getExpression().removeLastToken(removedMfield => {
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
      const lastNode = this.getExpression().getLastNode();
      if (lastNode && lastNode.toText().length <= this.atIndex) {
        this.clearSearchMode();
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

    if (this.searchMode) {
      if (event.key.match(/[a-z0-9]/i)) {
        this.searchFilter += event.key;
        this.mappedFieldCandidates = this.executeSearch(this.searchFilter);
      }
    } else {
      this.searchMode = (event.key === '@') ? true : false;
      const lastNode = this.getExpression().getLastNode();
      if (lastNode) {
        this.atIndex = lastNode.toText().length - 1;
      } else {
        this.atIndex = 0;
      }
    }

    this.insertTextAtCaretPosition(event.key);
  }

  @HostListener('cut', ['$event'])
  onCut(event: ClipboardEvent) {
    // TODO remove only selected area
    this.getExpression().clear();
  }

  @HostListener('paste', ['$event'])
  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasted = event.clipboardData.getData('text/plain')
      || window['clipboardData'].getData('Text');
    // TODO handle cursor position... for now just append to the end
    this.getExpression().insertText(pasted);
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
      // TODO handle drop position - for now this appends a field ref to the end of expression
      const mappedField = currentFieldMapping.getMappedFieldForField(droppedField, true);
      this.addConditionExpressionNode(mappedField);

    // Pulling an unmapped field into a transition expression evaluation implies a compound selection.
    } else {
      // TODO handle drop position - for now this appends a field ref to the
      // end of expression in FieldMappingPair#updateTransition()
      this.configModel.mappingService.fieldSelected(droppedField, true);
    }
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
    const mappedField = currentFieldMapping.getMappedFieldForField(selectedField, true);
    this.getExpression().clearToEnd(this.atIndex + 1);

    // If the selected field was not part of the original mapping then add it now.
    if (mappedField === null) {
      this.configModel.mappingService.fieldSelected(selectedField, true);
      this.configModel.mappingService.updateMappedField(currentFieldMapping, true, false);
    } else {
      this.addConditionExpressionNode(mappedField);
    }
    this.clearSearchMode();
    this.markup.nativeElement.focus();
  }

  /**
   * Clear elements associated with mapped-field searching.
   */
  private clearSearchMode(): void {
    this.atIndex = 0;
    this.searchMode = false;
    this.searchFilter = '';
    this.mappedFieldCandidates = [];
  }

  private addConditionExpressionNode(mappedField: MappedField): void {
    this.getExpression().addNode(new FieldNode(mappedField));
  }

  private updateExpressionMarkup() {
    this.markup.nativeElement.innerHTML = this.getExpression().toHTML()
      + `<span id="${ExpressionComponent.trailerId}">&nbsp;</span>`;
  }

  private moveCaretToEnd() {
    const trailerNode = this.markup.nativeElement.querySelector('#' + ExpressionComponent.trailerId);
    this.markup.nativeElement.focus();
    const range = window.getSelection().getRangeAt(0);
    range.selectNode(trailerNode);
    range.collapse(false);
  }

  private nodeIndexOf(list: NodeList, node: any) {
    for (let i = 0; i < list.length; i++) {
      if (list[i] === node) {
        return i;
      }
    }
  }

  private getExpression(): ExpressionModel {
    return this.mapping.transition.expression;
  }

  private insertTextAtCaretPosition(key: string) {
    const range = window.getSelection().getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;
    if (startContainer === this.markup.nativeElement) {
      if (startOffset === 0) {
        this.getExpression().insertText(key, this.getExpression().nodes[0].getUuid(), 0);
      } else {
        this.getExpression().insertText(key);
      }
      return;
    }

    const nodeId = startContainer.parentElement.getAttribute('id');
    if (nodeId === ExpressionComponent.trailerId) {
      this.getExpression().insertText(key);
    } else {
      this.getExpression().insertText(key, nodeId, startOffset);
    }
  }

  private restoreCaretPosition(event: ExpressionUpdatedEvent) {
    this.markup.nativeElement.focus();
    if (!event) {
      this.moveCaretToEnd();
      return;
    }
    for (let i = 0; i < this.markup.nativeElement.childNodes.length; i++) {
      const target = this.markup.nativeElement.childNodes[i];
      if (target.getAttribute('id') === event.node.getUuid()) {
        const range = window.getSelection().getRangeAt(0);
        range.selectNode(target.childNodes[0]);
        range.setStart(target.childNodes[0], event.offset);
        range.collapse(true);
        return;
      }
    }
    this.moveCaretToEnd();
  }

}
