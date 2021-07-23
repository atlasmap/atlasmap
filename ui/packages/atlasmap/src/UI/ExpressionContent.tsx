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
import {
  Button,
  Form,
  FormGroup,
  InputGroup,
  TextInput,
  Tooltip,
} from '@patternfly/react-core';
import { EnumValue, useToggle } from '../impl/utils';
import { Observable, Subscription } from 'rxjs';
import React, {
  FunctionComponent,
  KeyboardEvent,
  MouseEvent,
  useCallback,
  useEffect,
} from 'react';

import { ExpressionEnumSelect } from './ExpressionEnumSelect';
import { ExpressionFieldSearch } from './ExpressionFieldSearch';
import { IExpressionNode } from '@atlasmap/core';
import { css } from '@patternfly/react-styles';
import styles from '@patternfly/react-styles/css/components/FormControl/form-control';

let atIndex = -1;
let atContainer: Node | undefined;
let enumCandidates: EnumValue[] = [];
let expressionUpdatedSubscription: Subscription | null;
let lastUpdatedEvent: IExpressionUpdatedEvent | null = null;
let mappedFieldCandidates: string[][] = [];
let markup: HTMLDivElement | null = null;
let searchFilter = '';
let searchMode = false;
let selectedNodeId: string = '';
let trailerHTML = '';
let trailerID = '';
let getMappingExpression: () => string;
let mappingExprInit: () => void;
let mappingExprObservable: () => Observable<IExpressionUpdatedEvent> | null;

interface IExpressionUpdatedEvent {
  node: IExpressionNode;
  offset: number;
}

export interface IExpressionContentProps {
  executeFieldSearch: (searchFilter: string, isSource: boolean) => string[][];
  getFieldEnums: (nodeId: string) => EnumValue[];
  mappingExpressionAddField: (
    selectedDocId: string,
    selectedField: string,
    newTextNode: IExpressionNode,
    atIndex: number,
    isTrailer: boolean,
  ) => void;
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number,
  ) => IExpressionNode | null;
  isMappingExpressionEmpty: boolean;
  mappingExpressionInit: () => void;
  mappingExpressionInsertText: (
    str: string,
    nodeId?: string | undefined,
    offset?: number | undefined,
  ) => void;
  mappingExpressionObservable: () => Observable<IExpressionUpdatedEvent> | null;
  mappingExpressionRemoveField: (
    tokenPosition?: string,
    offset?: number,
    removeNext?: boolean,
  ) => void;
  mappingExpression?: string;
  trailerId: string;
  disabled: boolean;
  onToggle: () => void;
  setSelectedEnumValue: (
    selectedEnum: string,
    selectedEnumValueIndex: number,
  ) => void;
}

function updateExpressionMarkup(reset?: boolean) {
  if (!markup) {
    return;
  }
  if (reset) {
    markup.innerHTML = trailerHTML;
  } else {
    const currentExpression = getMappingExpression();
    markup.innerHTML = currentExpression + trailerHTML;
  }
}

function moveCaretToEnd() {
  if (!markup || trailerID.length === 0) {
    return;
  }
  const trailerNode = markup.querySelector('#' + trailerID);
  if (!trailerNode) {
    return;
  }
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

function restoreCaretPosition(event: IExpressionUpdatedEvent) {
  if (!markup || !event || !event.node) {
    return;
  }

  for (let i = 0; i < markup.childNodes.length; i++) {
    const target: any = markup.childNodes[i];
    if (target.id === event.node.uuid) {
      const selection = window.getSelection();
      if (selection && selection.rangeCount) {
        const range = selection.getRangeAt(0);
        const actualNode = target.childNodes[0] ? target.childNodes[0] : target;
        range.selectNode(actualNode);
        if (event.offset <= actualNode.length) {
          range.setStart(actualNode, event.offset);
        }
        range.collapse(true);
      }
      return;
    }
  }
  moveCaretToEnd();
}

export function initializeMappingExpression() {
  mappingExprInit();
  const mappingExprObs = mappingExprObservable();
  if (mappingExprObs) {
    expressionUpdatedSubscription = mappingExprObs.subscribe(
      (updatedEvent: IExpressionUpdatedEvent) => {
        lastUpdatedEvent = updatedEvent;
      },
    );
  }
  updateExpressionMarkup();
  if (!lastUpdatedEvent) {
    moveCaretToEnd();
  } else {
    restoreCaretPosition(lastUpdatedEvent);
    lastUpdatedEvent = null;
  }
}

export const ExpressionContent: FunctionComponent<IExpressionContentProps> = ({
  executeFieldSearch,
  getFieldEnums,
  mappingExpressionAddField,
  mappingExpressionClearText,
  isMappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  mappingExpression,
  trailerId,
  disabled,
  onToggle,
  setSelectedEnumValue,
}) => {
  let addFieldToExpression: (
    selectedDocId: string,
    selectedField: string,
    newTextNode: IExpressionNode,
    atIndex: number,
    isTrailer: boolean,
  ) => void;
  let clearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number,
  ) => IExpressionNode | null;
  let fieldSearch: (searchFilter: string, isSource: boolean) => string[][];
  let getEnums: (enumFieldName: string) => EnumValue[];
  let setSelEnumValue: (
    selectedEnumNodeId: string,
    selectedEnumValueIndex: number,
  ) => void;

  const {
    state: showEnumSelect,
    toggleOn: toggleEnumSelOn,
    toggleOff: toggleEnumSelOff,
  } = useToggle(false);

  /**
   * An enumeration value has been selected for the specified selected field node ID.
   *
   * @param selectedEnumNodeId - selected field node ID
   * @param selectedEnumValueIndex - selected enumeration index
   */
  function onEnumSelect(
    selectedEnumNodeId: string,
    selectedEnumValueIndex: number,
  ) {
    setSelEnumValue(selectedEnumNodeId, selectedEnumValueIndex);
    clearEnumSelect();
  }

  function insertTextAtCaretPosition(key: string) {
    const range = window.getSelection()!.getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;

    // On initial caret positioning before the first field node, the markup
    // and start container are the same.
    if (markup && startContainer === markup) {
      if (startOffset === 0) {
        const initialElement = startContainer.childNodes[0] as Element;
        mappingExpressionInsertText(key, initialElement.getAttribute('id')!, 0);
      } else {
        mappingExpressionInsertText(key);
      }
      return;
    }
    const nodeId = getCaretPositionNodeId();

    if (nodeId === trailerID) {
      mappingExpressionInsertText(key);
    } else {
      mappingExpressionInsertText(key, nodeId, startOffset);
    }
  }

  function removeTokenAtCaretPosition(before: boolean) {
    const selection = window.getSelection();
    let removeNext: boolean = false;
    if (!selection || !markup) {
      return;
    }
    if (!selection.rangeCount) {
      if (getCaretPositionNodeId() === trailerID) {
        if (before) {
          mappingExpressionRemoveField();
          moveCaretToEnd();
        }
      }
      return;
    }
    // The window selection node will be the text node if the cursor lies
    // at the boundary between the text node and a field node.  In that
    // case remove the next node.
    removeNext =
      !before &&
      selection.focusNode &&
      selection.focusNode.nodeType === selection.focusNode.TEXT_NODE &&
      selection.focusOffset === selection.focusNode.textContent?.length
        ? true
        : false;
    const range = selection.getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;

    if (startContainer === markup) {
      if (startOffset === 0) {
        // head of expression
        if (!before && !isMappingExpressionEmpty) {
          mappingExpressionRemoveField(getCaretPositionNodeId(), 0);
        }
        return;
      }
      // end of expression
      if (before && !isMappingExpressionEmpty) {
        mappingExpressionRemoveField();
      }
      return;
    }

    if (getCaretPositionNodeId(startContainer) === trailerID) {
      if (before) {
        mappingExpressionRemoveField();
      }
      moveCaretToEnd();
      return;
    }
    mappingExpressionRemoveField(
      getCaretPositionNodeId(),
      before ? startOffset - 1 : startOffset,
      removeNext,
    );
    if (getCaretPositionNodeId() === trailerID) {
      moveCaretToEnd();
    }
  }

  /**
   * Handle key down events.
   *
   * @param event - expression keyboard event
   */
  function onKeyDown(event: KeyboardEvent<HTMLDivElement>): void {
    if ('Backspace' === event.key) {
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
    }
  }

  /**
   * A mouse click has occurred within the expression box.
   *
   * @param event - mouse event
   */
  function onExprClick(event: MouseEvent<HTMLElement>) {
    selectedNodeId = getCaretPositionNodeId();

    // Check for clicking on an enumeration field node.
    enumCandidates = getEnums(selectedNodeId);
    if (enumCandidates.length > 0) {
      event.preventDefault();
      toggleEnumSelOn();
    }
  }

  function onKeyPress(event: KeyboardEvent<HTMLDivElement>) {
    if (event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    if (event.key.length > 1) {
      return;
    }
    event.preventDefault();

    if (isMappingExpressionEmpty) {
      initializeMappingExpression();
    }
    if (searchMode) {
      if (event.key.match(/[a-z0-9]/i)) {
        searchFilter += event.key;
        mappedFieldCandidates = fieldSearch(searchFilter, true);
      }
    } else {
      searchMode = event.key === '@' ? true : false;
      if (searchMode) {
        atContainer = window.getSelection()!.getRangeAt(0).startContainer;
        atIndex = window.getSelection()!.getRangeAt(0).startOffset;
        searchFilter = '';
        mappedFieldCandidates = fieldSearch(searchFilter, true);
      }
    }
    insertTextAtCaretPosition(event.key);
  }

  function onChange(_event: React.FormEvent<HTMLDivElement>) {
    if (isMappingExpressionEmpty) {
      initMappingExpression();
    }
    if (expressionUpdatedSubscription) {
      expressionUpdatedSubscription.unsubscribe();
    }
    expressionUpdatedSubscription = mappingExprObservable()!.subscribe(
      (updatedEvent: IExpressionUpdatedEvent) => {
        updateExpressionMarkup();
        restoreCaretPosition(updatedEvent);
      },
    );
    updateExpressionMarkup();
  }

  function onPaste(event: React.ClipboardEvent<HTMLDivElement>) {
    if (!event || !event.clipboardData) {
      return;
    }
    event.preventDefault();
    const pasted = event.clipboardData.getData('text/plain');
    // TODO handle cursor position... for now just append to the end
    mappingExpressionInsertText(pasted);
  }

  /**
   * The user has selected a field from the search select menu.  Extract
   * the field name and the scope if it is present.
   *
   * @param selectedDocId
   * @param selectedField
   * @returns
   */
  function insertSelectedField(
    selectedDocId: string,
    selectedField: string,
  ): void {
    const newTextNode = clearAtText(getCaretPositionNodeId(atContainer));
    if (newTextNode === null) {
      return;
    }
    const isTrailer = getCaretPositionNodeId(atContainer) === trailerID;
    addFieldToExpression(
      selectedDocId,
      selectedField,
      newTextNode,
      atIndex,
      isTrailer,
    );
    clearSearchMode(false);
    markup!.focus();
  }

  /**
   * Clear user input from the selected range offset within the TextNode at
   * the specified node ID.  The input will become a FieldNode so we don't
   * need the text.  Return the new UUID position indicator.
   */
  function clearAtText(nodeId: string): IExpressionNode | null {
    if (atIndex === -1) {
      return null;
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
    const selection = window.getSelection();
    if (!startContainer) {
      if (!selection || selection.rangeCount === 0) {
        return trailerID;
      }
      startContainer = selection!.getRangeAt(0).startContainer;
    }
    if (startContainer.nodeType === selection?.focusNode?.TEXT_NODE) {
      return startContainer.parentElement!.getAttribute('id')!;
    } else {
      return (startContainer.firstChild! as HTMLElement)?.getAttribute('id')!;
    }
  }

  /**
   * Clear elements associated with mapped-field searching.
   */
  function clearSearchMode(clearAtSign: boolean): void {
    if (clearAtSign) {
      clearAtText(getCaretPositionNodeId(atContainer));
    }
    atIndex = -1;
    atContainer = undefined;
    searchMode = false;
    searchFilter = '';
    mappedFieldCandidates = [];
  }

  function updateSearchMode(): void {
    if (searchFilter.length === 0) {
      mappedFieldCandidates = [];
      searchMode = false;
    } else {
      searchFilter = searchFilter.substr(0, searchFilter.length - 1);
      mappedFieldCandidates = fieldSearch(searchFilter, true);
    }
  }

  function clearEnumSelect() {
    selectedNodeId = '';
    enumCandidates = [];
    toggleEnumSelOff();
  }

  const initMappingExpression = useCallback(() => {
    initializeMappingExpression();
  }, []);

  addFieldToExpression = mappingExpressionAddField;
  clearText = mappingExpressionClearText;
  fieldSearch = executeFieldSearch;
  getEnums = getFieldEnums;
  setSelEnumValue = setSelectedEnumValue;

  getMappingExpression = () => mappingExpression || '';
  mappingExprInit = mappingExpressionInit;
  mappingExprObservable = mappingExpressionObservable;
  trailerID = trailerId;
  trailerHTML = `<span id="${trailerID}">&nbsp;</span>`;

  const uninitializeMappingExpression = () => {
    if (expressionUpdatedSubscription) {
      expressionUpdatedSubscription.unsubscribe();
    }
  };

  useEffect(() => {
    if (mappingExpression !== undefined) {
      initMappingExpression();
      return () => uninitializeMappingExpression();
    }
    return;
  }, [mappingExpression, initMappingExpression]);

  return (
    <>
      <Form>
        <FormGroup fieldId="expressionContent">
          <InputGroup>
            <Tooltip
              content={'Enable/ Disable conditional mapping expression.'}
              enableFlip={true}
              entryDelay={750}
              exitDelay={100}
              position={'left'}
            >
              <Button
                variant={'control'}
                aria-label="Enable/ Disable conditional mapping expression"
                tabIndex={-1}
                onClick={onToggle}
                data-testid={
                  'enable-disable-conditional-mapping-expression-button'
                }
                isDisabled={disabled}
              >
                <i>
                  f
                  <small style={{ position: 'relative', bottom: -3 }}>
                    (x)
                  </small>
                </i>
              </Button>
            </Tooltip>
            {!disabled && mappingExpression !== undefined ? (
              <Tooltip
                content={"Enter text or '@' for source fields menu."}
                enableFlip={true}
                entryDelay={750}
                exitDelay={100}
                position={'left'}
              >
                <div
                  id="expressionMarkup"
                  key="expressionMarkup-div"
                  aria-label="Expression Content"
                  contentEditable
                  className={css(styles.formControl, 'ExpressionFieldSearch')}
                  suppressContentEditableWarning={true}
                  onChange={onChange}
                  onKeyDown={onKeyDown}
                  onKeyPress={onKeyPress}
                  onPaste={onPaste}
                  onClick={onExprClick}
                  ref={(el) => (markup = el)}
                  tabIndex={-1}
                  style={{ paddingLeft: 8 }}
                />
              </Tooltip>
            ) : (
              <TextInput
                isDisabled={true}
                aria-label={'Expression content'}
              /> /* this to render a disabled field */
            )}
          </InputGroup>
        </FormGroup>
      </Form>
      <div>
        {mappedFieldCandidates.length > 0 && (
          <span>
            <ExpressionFieldSearch
              clearSearchMode={clearSearchMode}
              insertSelectedField={insertSelectedField}
              mappedFieldCandidates={mappedFieldCandidates}
            />
          </span>
        )}
        {showEnumSelect && (
          <span>
            <ExpressionEnumSelect
              selectedNodeId={selectedNodeId}
              enumCandidates={enumCandidates!}
              clearEnumSelect={clearEnumSelect}
              onEnumSelect={onEnumSelect}
            />
          </span>
        )}
      </div>
    </>
  );
};
