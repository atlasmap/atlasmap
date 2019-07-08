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
import { Component, ViewChild, Input, HostListener, ElementRef, OnInit, OnDestroy, OnChanges } from '@angular/core';
import { ConfigModel } from '../models/config.model';
import { DocumentDefinition } from '../models/document-definition.model';
import { MappingModel, MappedField } from '../models/mapping.model';
import { ExpressionModel, FieldNode, ExpressionUpdatedEvent, TextNode } from '../models/expression.model';
import { Field } from '../models/field.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'expression',
  templateUrl: 'expression.component.html'
})

export class ExpressionComponent implements OnInit, OnDestroy, OnChanges {

  static readonly trailerId = 'expression-trailer';

  @Input()
  configModel: ConfigModel;

  @Input()
  mapping: MappingModel;

  @ViewChild('expressionMarkupRef')
  markup: ElementRef;

  @ViewChild('expressionSearch')
  expressionSearch: ElementRef;

  mappedFieldCandidates = [];
  tooltiptext = '';

  // Need both the range object of the user text input and the index at the time the user typed '@'.
  private atIndex = 0;
  private atContainer = null;

  private mouseOverTimeOut = null;
  private searchFilter = '';
  private searchMode = false;
  private expressionUpdatedSubscription: Subscription;

  ngOnInit() {
    if (!this.getExpression()) {
      this.mapping.transition.expression = new ExpressionModel(this.mapping, this.configModel);
      this.getExpression().generateInitialExpression();
    } else {
      this.getExpression().setConfigModel(this.configModel);
    }

    this.getExpression().updateFieldReference(this.mapping);
    this.expressionUpdatedSubscription = this.getExpression().expressionUpdated$.subscribe((updatedEvent) => {
      this.updateExpressionMarkup();
      this.restoreCaretPosition(updatedEvent);
    });
    this.updateExpressionMarkup();
    this.moveCaretToEnd();
  }

  ngOnChanges() {
    this.mapping = this.configModel.mappings.activeMapping;
    if (!this.getExpression()) {
      this.mapping.transition.expression = new ExpressionModel(this.mapping, this.configModel);
      this.getExpression().generateInitialExpression();
    }
    if (this.expressionUpdatedSubscription) {
      this.expressionUpdatedSubscription.unsubscribe();
    }
    this.expressionUpdatedSubscription = this.getExpression().expressionUpdated$.subscribe((updatedEvent) => {
      this.updateExpressionMarkup();
      this.restoreCaretPosition(updatedEvent);

      // Only validate for inserted or appended text nodes.
      if ((!updatedEvent && this.getExpression().getLastNode() instanceof TextNode) ||
          (updatedEvent && updatedEvent.node instanceof TextNode)) {
        this.configModel.mappingService.validateMappings();
      }
    });
    this.updateExpressionMarkup();
    this.moveCaretToEnd();
  }

  ngOnDestroy() {
    if (this.expressionUpdatedSubscription) {
      this.expressionUpdatedSubscription.unsubscribe();
    }
  }

  @HostListener ('click', ['$event'])
  onClick($event) {
    if ($event.target.className.includes('expressionMarkup') && this.mouseOverTimeOut) {
      this.tooltiptext = '';
      clearTimeout(this.mouseOverTimeOut);
      this.mouseOverTimeOut = null;
    }
  }

  @HostListener ('mouseover', ['$event'])
  onMouseOver($event) {
    if ($event.target.className.includes('expressionMarkup')) {
      this.tooltiptext = 'Enter source fields for expr: e.g. IF (ISEMPTY(fieldA), fieldB, fieldC)';
      const self = this;

      this.mouseOverTimeOut = setTimeout(function() {
        self.tooltiptext = '';
        this.mouseOverTimeOut = null;
      }, 8000);
    }
  }

  @HostListener ('mouseleave', ['$event'])
  onMouseLeave($event) {
    if ($event.target.className.includes('expressionMarkup') && this.mouseOverTimeOut) {
      clearTimeout(this.mouseOverTimeOut);
      this.mouseOverTimeOut = null;
    }
  }

  @HostListener('keydown', ['$event'])
  onKeydown(event: KeyboardEvent) {
    if ('Enter' === event.key) {
      event.preventDefault();
    } else if ('Backspace' === event.key) {
      // TODO handle cursor position
      event.preventDefault();
      this.removeTokenAtCaretPosition(true);
      if (this.searchMode) {
        this.updateSearchMode();
      }

    } else if ('Delete' === event.key) {
      event.preventDefault();
      this.removeTokenAtCaretPosition(false);
      if (this.searchMode) {
        this.updateSearchMode();
      }
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
      if (this.searchMode) {
        this.atContainer = window.getSelection().getRangeAt(0).startContainer;
        this.atIndex = window.getSelection().getRangeAt(0).startOffset;
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
    const activeMapping = this.configModel.mappings.activeMapping;
    if (droppedField === null || activeMapping === null || !droppedField.isSource) {
      return;
    }
    const caretPositionNodeId = event.target['id'];
    const textNode = this.getExpression().getNode(caretPositionNodeId);

    if (droppedField.partOfMapping && activeMapping.isFieldMapped(droppedField)) {

      // Since the dropped field is already part of the mapping, just add a new node.
      const mappedField = activeMapping.getMappedFieldForField(droppedField);
      if (textNode) {
        this.addConditionalExpressionNode(mappedField, textNode.getUuid(), 0);
      } else {
        this.addConditionalExpressionNode(mappedField, null, 0);
      }

    // Pulling an unmapped field into a transition expression evaluation implies a compound selection.
    } else {

      if (textNode) {

        // If the selected field was not part of the original mapping then add it now.
        this.configModel.mappingService.fieldSelected(droppedField, true, textNode.getUuid(), 0);
      } else {
        this.configModel.mappingService.fieldSelected(droppedField, true);
      }
    }
    this.markup.nativeElement.focus();
  }

  /**
   * Return an array of strings representing display names of active mapping fields based on the
   * specified filter.
   *
   * @param filter
   */
  executeSearch(filter: string): any[] {
    const activeMapping = this.configModel.mappings.activeMapping;
    const formattedFields: any[] = [];
    let fields: Field[] = [];
    for (const docDef of this.configModel.getDocs(true)) {
      fields = fields.concat(docDef.getTerminalFields());
    }
    for (const field of fields) {
      let displayName = (field == null) ? '' : field.getFieldLabel(ConfigModel.getConfig().showTypes, true);

      if (filter == null || filter === '' || displayName.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
        if (!this.configModel.mappingService.isFieldSelectable(activeMapping, field)) {
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
    const activeMapping = this.configModel.mappings.activeMapping;
    const selectedField = this.mappedFieldCandidates[index].field;
    const mappedField = activeMapping.getMappedFieldForField(selectedField);

    const newTextNode = this.clearAtText(this.getCaretPositionNodeId(this.atContainer));
    if (newTextNode === null) {
      return;
    }
    // If the selected field was not part of the original mapping then add it now.
    const isTrailer = this.getCaretPositionNodeId(this.atContainer) === ExpressionComponent.trailerId;
    if (mappedField === null) {
      this.configModel.mappingService.fieldSelected(selectedField, true, newTextNode.getUuid(),
        isTrailer ? newTextNode.toText().length : this.atIndex);
    } else {
      this.addConditionalExpressionNode(mappedField, newTextNode.getUuid(),
        isTrailer ? newTextNode.str.length : this.atIndex);
    }
    this.clearSearchMode();
    this.markup.nativeElement.focus();
  }

  /**
   * Clear user input from the selected range offset within the TextNode at the specified
   * node ID.  The input will become a FieldNode so we don't need the text.  Return the new
   * UUID position indicator.
   */
  private clearAtText(nodeId: string): TextNode {
    const startOffset = this.atIndex;
    const endOffset = startOffset + this.searchFilter.length + 1;
    let updatedTextNode = null;

    if (nodeId === ExpressionComponent.trailerId) {
      updatedTextNode = this.getExpression().clearText();
    } else {
      updatedTextNode = this.getExpression().clearText(nodeId, startOffset, endOffset);
    }
    return updatedTextNode;
  }

  /**
   * Return the UUID string representing the caret position as defined by the
   * user-specified range.  If no range is specified then return the current
   * caret position.
   *
   * @param range
   */
  private getCaretPositionNodeId(startContainer?: Node): string {
    if (!startContainer) {
      const selection = window.getSelection();
      if (selection.rangeCount === 0) {
          return ExpressionComponent.trailerId;
      }
      startContainer = selection.getRangeAt(0).startContainer;
    }
    return startContainer.parentElement.getAttribute('id');
  }

  /**
   * Clear elements associated with mapped-field searching.
   */
  private clearSearchMode(): void {
    this.atIndex = 0;
    this.atContainer = null;
    this.searchMode = false;
    this.searchFilter = '';
    this.mappedFieldCandidates = [];
  }

  private addConditionalExpressionNode(mappedField: MappedField, nodeId: string, offset: number): void {
    this.getExpression().insertNodes([new FieldNode(mappedField)], nodeId, offset);
  }

  private updateExpressionMarkup() {
    this.markup.nativeElement.innerHTML = this.getExpression().toHTML()
      + `<span id="${ExpressionComponent.trailerId}">&nbsp;</span>`;
  }

  private moveCaretToEnd() {
    const trailerNode = this.markup.nativeElement.querySelector('#' + ExpressionComponent.trailerId);
    this.markup.nativeElement.focus();
    let range;
    if (window.getSelection().rangeCount > 0) {
      range = window.getSelection().getRangeAt(0);
    } else {
      range = document.createRange();
      window.getSelection().addRange(range);
    }
    range.selectNode(trailerNode.childNodes[0]);
    range.setStart(trailerNode.childNodes[0], 0);
    range.collapse(true);
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

    const nodeId = this.getCaretPositionNodeId();
    if (nodeId === ExpressionComponent.trailerId) {
      this.getExpression().insertText(key);
    } else {
      this.getExpression().insertText(key, nodeId, startOffset);
    }
  }

  private removeTokenAtCaretPosition(before: boolean) {
    const selection = window.getSelection();
    if (!selection.rangeCount) {
      if (this.getCaretPositionNodeId() === ExpressionComponent.trailerId) {
        if (before) {
          this.getExpression().removeToken(this.reflectRemovedField);
        }
      }
      return;
    }
    const range = selection.getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;
    if (startContainer === this.markup.nativeElement) {
      if (startOffset === 0) {
        // head of expression
        if (!before && this.getExpression().nodes.length > 0) {
          this.getExpression().removeToken(this.reflectRemovedField,
            this.getExpression().nodes[0].getUuid(), 0);
        }
        return;
      }
      // end of expression
      if (before && this.getExpression().nodes.length > 0) {
        this.getExpression().removeToken(this.reflectRemovedField);
      }
      return;
    }

    if (this.getCaretPositionNodeId(startContainer) === ExpressionComponent.trailerId) {
      if (before) {
        this.getExpression().removeToken(this.reflectRemovedField);
      }
      return;
    }
    this.getExpression().removeToken(this.reflectRemovedField, this.getCaretPositionNodeId(),
      before ? startOffset - 1 : startOffset);
  }

  private restoreCaretPosition(event: ExpressionUpdatedEvent) {
    this.markup.nativeElement.focus();
    if (!event || !event.node) {
      this.moveCaretToEnd();
      return;
    }
    for (let i = 0; i < this.markup.nativeElement.childNodes.length; i++) {
      const target = this.markup.nativeElement.childNodes[i];
      if (target.getAttribute('id') === event.node.getUuid()) {
        const selection = window.getSelection();
        if (selection.rangeCount) {
          const range = selection.getRangeAt(0);
          range.selectNode(target.childNodes[0] ? target.childNodes[0] : target);
          range.setStart(target.childNodes[0] ? target.childNodes[0] : target, event.offset);
          range.collapse(true);
        }
        return;
      }
    }
    this.moveCaretToEnd();
  }

  private reflectRemovedField = (removed: MappedField) => {
    this.mapping.removeMappedField(removed, true);
    this.configModel.mappingService.updateMappedField(this.mapping, true, true);
  }

  private updateSearchMode(): void {
    const selectionRange = window.getSelection().getRangeAt(0);
    if (selectionRange.startContainer.nodeValue.indexOf('@') === -1) {
      this.clearSearchMode();
      return;
    }
    if (this.searchFilter.length === 0) {
      this.mappedFieldCandidates = [];
    } else {
      this.searchFilter = this.searchFilter.substr(0, this.searchFilter.length - 1);
    }
  }
}
