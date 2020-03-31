import { FunctionComponent } from 'react';
import React from 'react';
import { Tooltip, Form, FormGroup } from '@patternfly/react-core';
import { Observable } from 'rxjs';

let atIndex = -1;
let atContainer: Node | undefined;
let candidateIndex = 0;
let candidateSrcElement: any;
let expressionUpdatedSubscription: Observable<any> | null;
let lastUpdatedEvent: any = null;
let markup: any;
let searchFilter = '';
let searchMode = false;
// let mappedFieldCandidates: any[] = [];

let trailerID = '';
let clearText: (
  nodeId?: string,
  startOffset?: number,
  endOffset?: number
) => any;
let getMappingExpression: () => string;
let mappingExprEmpty: () => boolean;
let mappingExprInit: () => void;
let mappingExprInsertText: (
  str: string,
  nodeId?: string | undefined,
  offset?: number | undefined
) => void;
let mappingExprObservable: () => any;
let mappingExprRemoveField: (tokenPosition?: string, offset?: number) => void;

export interface IExpressionContentProps {
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number
  ) => any;
  mappingExpressionEmpty: () => boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (
    str: string,
    nodeId?: string | undefined,
    offset?: number | undefined
  ) => void;
  mappingExpressionObservable: () => any;
  mappingExpressionRemoveField: (
    tokenPosition?: string,
    offset?: number
  ) => void;
  onGetMappingExpression: () => string;
  trailerId: string;
}

function updateExpressionMarkup() {
  if (!markup) {
    return;
  }
  markup.innerHTML =
    getMappingExpression() + `<span id="${trailerID}">&nbsp;</span>`;
}

function moveCaretToEnd() {
  if (!markup) {
    return;
  }
  const trailerNode = markup.querySelector('#' + trailerID);
  markup.focus();
  let range;
  if (window.getSelection()!.rangeCount > 0) {
    range = window.getSelection()!.getRangeAt(0);
  } else {
    range = document.createRange();
    window.getSelection()!.addRange(range);
  }
  range.selectNode(trailerNode.childNodes[0]);
  range.setStart(trailerNode.childNodes[0], 0);
  range.collapse(true);
}

function insertTextAtCaretPosition(key: string) {
  const range = window.getSelection()!.getRangeAt(0);
  const startContainer = range.startContainer;
  const startOffset = range.startOffset;

  if (markup && startContainer === markup) {
    if (startOffset === 0) {
      mappingExprInsertText(key, getCaretPositionNodeId(), 0);
    } else {
      mappingExprInsertText(key);
    }
    return;
  }

  const nodeId = getCaretPositionNodeId();

  if (nodeId === trailerID) {
    mappingExprInsertText(key);
  } else {
    mappingExprInsertText(key, nodeId, startOffset);
  }
}

function removeTokenAtCaretPosition(before: boolean) {
  const selection = window.getSelection();
  if (!selection || !markup) {
    return;
  }
  if (!selection.rangeCount) {
    if (getCaretPositionNodeId() === trailerID) {
      if (before) {
        mappingExprRemoveField();
      }
    }
    return;
  }
  const range = selection.getRangeAt(0);
  const startContainer = range.startContainer;
  const startOffset = range.startOffset;
  if (startContainer === markup) {
    if (startOffset === 0) {
      // head of expression
      if (!before && !mappingExprEmpty()) {
        mappingExprRemoveField(getCaretPositionNodeId(), 0);
      }
      return;
    }
    // end of expression
    if (before && !mappingExprEmpty()) {
      mappingExprRemoveField();
    }
    return;
  }

  if (getCaretPositionNodeId(startContainer) === trailerID) {
    if (before) {
      mappingExprRemoveField();
    }
    return;
  }
  mappingExprRemoveField(
    getCaretPositionNodeId(),
    before ? startOffset - 1 : startOffset
  );
}

function restoreCaretPosition(event: any) {
  if (!markup || !event || !event.node) {
    return;
  }
  markup.focus();
  for (let i = 0; i < markup.childNodes.length; i++) {
    const target = markup.childNodes[i];
    if (target.getAttribute('id') === event.node.getUuid()) {
      const selection = window.getSelection();
      if (selection && selection.rangeCount) {
        const range = selection.getRangeAt(0);
        range.selectNode(target.childNodes[0] ? target.childNodes[0] : target);
        range.setStart(
          target.childNodes[0] ? target.childNodes[0] : target,
          event.offset
        );
        range.collapse(true);
      }
      lastUpdatedEvent = event;
      return;
    }
  }
  moveCaretToEnd();
}

/**
 * Update the candidate source element and reset the focus.
 *
 * @param sibling
 */
function updateCandidate(sibling: any): void {
  if (candidateSrcElement && sibling) {
    candidateSrcElement.style.backgroundColor = 'white';
    sibling.focus();
    candidateSrcElement = sibling;
    candidateSrcElement.style.backgroundColor = 'lightblue';
  }
}

/**
 * Handle key down events.
 *
 * @param event - expression keyboard event
 */
function onKeyDown(event: any): void {
  if ('Enter' === event.key) {
    event.preventDefault();
    if (candidateSrcElement) {
      selectionChanged(event, candidateIndex);
    }
  } else if ('Backspace' === event.key) {
    // TODO handle cursor position
    event.preventDefault();
    removeTokenAtCaretPosition(true);
    if (searchMode) {
      updateSearchMode();
    }
  } else if ('Delete' === event.key) {
    event.preventDefault();
    removeTokenAtCaretPosition(false);
    if (searchMode) {
      updateSearchMode();
    }
  } else if ('ArrowDown' === event.key) {
    event.preventDefault();
    updateCandidate(candidateSrcElement.nextElementSibling);
  } else if ('ArrowUp' === event.key) {
    event.preventDefault();
    updateCandidate(candidateSrcElement.previousElementSibling);
  } else if ('Tab' === event.key) {
    if (!candidateSrcElement && event.srcElement) {
      candidateSrcElement =
        event.srcElement.nextElementSibling.firstElementChild;
      candidateIndex = 0;
      candidateSrcElement.style.backgroundColor = 'lightblue';
    } else if (candidateSrcElement && candidateSrcElement.nextElementSibling) {
      event.preventDefault();
      updateCandidate(candidateSrcElement!.nextElementSibling);
    }
  }
}

function onKeyPress(event: any) {
  if (event.ctrlKey || event.metaKey || event.altKey) {
    return;
  }
  if (event.key.length > 1) {
    return;
  }

  event.preventDefault();

  if (searchMode) {
    if (event.key.match(/[a-z0-9]/i)) {
      searchFilter += event.key;
      // this.mappedFieldCandidates = this.configModel.mappingService.executeFieldSearch(this.configModel, this.searchFilter, true);
    }
  } else {
    searchMode = event.key === '@' ? true : false;
    if (searchMode) {
      atContainer = window.getSelection()!.getRangeAt(0).startContainer;
      atIndex = window.getSelection()!.getRangeAt(0).startOffset;
      searchFilter = '';
      // mappedFieldCandidates = this.configModel.mappingService.executeFieldSearch(this.configModel, this.searchFilter, true);
    }
  }
  insertTextAtCaretPosition(event.key);
}

function onChange(_event: React.FormEvent<HTMLDivElement>) {
  if (mappingExprEmpty()) {
    mappingExprInit();
  }
  if (expressionUpdatedSubscription) {
    expressionUpdatedSubscription = null;
  }
  expressionUpdatedSubscription = mappingExprObservable().subscribe(
    (updatedEvent: any) => {
      updateExpressionMarkup();
      restoreCaretPosition(updatedEvent);
    }
  );
  updateExpressionMarkup();
}

function onCut(_event: React.ClipboardEvent<HTMLDivElement>) {
  // TODO remove only selected area
  clearText();
}

function onPaste(event: React.ClipboardEvent<HTMLDivElement>) {
  if (!event || !event.clipboardData) {
    return;
  }
  event.preventDefault();
  const pasted = event.clipboardData.getData('text/plain');
  // TODO handle cursor position... for now just append to the end
  mappingExprInsertText(pasted);
}

function onDragEnd(_event: React.DragEvent<HTMLDivElement>): void {
  /*
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

  // Pulling an unmapped field into an expression evaluation implies a compound selection.
  } else {

    if (textNode) {

      // If the selected field was not part of the original mapping then add it now.
      this.configModel.mappingService.fieldSelected(droppedField, true, textNode.getUuid(), 0);
    } else {
      this.configModel.mappingService.fieldSelected(droppedField, true);
    }
  }
  */
  markup.focus();
}

/**
 * The user has selected a field from the type-ahead pull-down.
 *
 * @param event
 */
function selectionChanged(_event: any, _index: number): void {
  const newTextNode = clearAtText(getCaretPositionNodeId(atContainer));
  if (newTextNode === null) {
    return;
  }
  // If the selected field was not part of the original mapping then add
  // it to the active mapping.
  /*
  const isTrailer = getCaretPositionNodeId(atContainer) === trailerID;
  const selectedField = mappedFieldCandidates[index].field;
  const activeMapping = this.configModel.mappings.activeMapping;
  const mappedField = activeMapping.getMappedFieldForField(selectedField);

  if (mappedField === null) {
    this.configModel.mappingService.fieldSelected(selectedField, true, newTextNode.getUuid(),
      isTrailer ? newTextNode.toText().length : atIndex);
  } else {
    this.addConditionalExpressionNode(mappedField, newTextNode.getUuid(),
      isTrailer ? newTextNode.str.length : atIndex);
  }
  */
  clearSearchMode();
  markup.focus();
  candidateSrcElement = null;
  candidateIndex = 0;
}

/**
 * Clear user input from the selected range offset within the TextNode at
 * the specified node ID.  The input will become a FieldNode so we don't
 * need the text.  Return the new UUID position indicator.
 */
function clearAtText(nodeId: string): any {
  if (atIndex === -1) {
    return;
  }
  const startOffset = atIndex;
  const endOffset = startOffset + searchFilter.length + 1;
  let updatedTextNode = null;

  if (nodeId === trailerID) {
    updatedTextNode = clearText();
  } else {
    updatedTextNode = clearText(nodeId, startOffset, endOffset);
  }
  return updatedTextNode;
}

/**
 * Return the UUID string representing the caret position as defined
 * by the user-specified starting container.  If no container is
 * specified then return the current caret position node ID value.
 *
 * @param startContainer
 */
function getCaretPositionNodeId(startContainer?: Node): string {
  if (!startContainer) {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) {
      return trailerID;
    }
    startContainer = selection!.getRangeAt(0).startContainer;
  }
  return startContainer.parentElement!.getAttribute('id')!;
}

/**
 * Clear elements associated with mapped-field searching.
 */
function clearSearchMode(): void {
  atIndex = -1;
  atContainer = undefined;
  searchMode = false;
  searchFilter = '';
  // mappedFieldCandidates = [];
}

function updateSearchMode(): void {
  if (searchFilter.length === 0) {
    // mappedFieldCandidates = [];
    searchMode = false;
  } else {
    searchFilter = searchFilter.substr(0, searchFilter.length - 1);
    // mappedFieldCandidates = this.configModel.mappingService.executeFieldSearch(this.configModel, this.searchFilter, true);
  }
}

function initializeMappingExpression() {
  if (lastUpdatedEvent) {
    restoreCaretPosition(lastUpdatedEvent);
    lastUpdatedEvent = null;
    return;
  }
  mappingExprInit();
  const mappingExprObs = mappingExprObservable();
  if (mappingExprObs) {
    expressionUpdatedSubscription = mappingExprObs.subscribe(
      (updatedEvent: any) => {
        updateExpressionMarkup();
        restoreCaretPosition(updatedEvent);
      }
    );
  }
  updateExpressionMarkup();
}

export const ExpressionContent: FunctionComponent<IExpressionContentProps> = ({
  mappingExpressionClearText,
  mappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  onGetMappingExpression,
  trailerId,
}) => {
  clearText = mappingExpressionClearText;
  getMappingExpression = onGetMappingExpression;
  mappingExprEmpty = mappingExpressionEmpty;
  mappingExprInit = mappingExpressionInit;
  mappingExprInsertText = mappingExpressionInsertText;
  mappingExprObservable = mappingExpressionObservable;
  mappingExprRemoveField = mappingExpressionRemoveField;
  trailerID = trailerId;
  initializeMappingExpression();
  return (
    <Form>
      <FormGroup fieldId="expressionContent">
        <Tooltip
          content={"Enter text or '@' for source fields dropdown."}
          enableFlip={true}
          entryDelay={2000}
          position={'left'}
        >
          <div
            aria-label="Expression Content"
            contentEditable
            onChange={onChange}
            onCut={onCut}
            onDragEnd={onDragEnd}
            onKeyDown={onKeyDown}
            onKeyPress={onKeyPress}
            onPaste={onPaste}
            ref={el => (markup = el)}
          />
        </Tooltip>
      </FormGroup>
    </Form>
  );
};
