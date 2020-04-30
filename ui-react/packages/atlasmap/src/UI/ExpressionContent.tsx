import { FunctionComponent, useEffect, useCallback } from "react";
import React from "react";
import { Tooltip, Form, FormGroup } from "@patternfly/react-core";
import { Subscription, Observable } from "rxjs";
import { ExpressionFieldSearch } from "./ExpressionFieldSearch";
import { ITextNode, IExpressionUpdatedEvent } from "../../src/Atlasmap";

let atIndex = -1;
let atContainer: Node | undefined;
let expressionUpdatedSubscription: Subscription | null;
let mappedFieldCandidates: string[][] = [];
let markup: HTMLDivElement | null = null;
let searchFilter = "";
let searchMode = false;
let trailerHTML = "";
let trailerID = "";
let getMappingExpression: () => string;
let mappingExprInit: () => void;
let mappingExprObservable: () => Observable<IExpressionUpdatedEvent> | null;

export interface IExpressionContentProps {
  executeFieldSearch: (searchFilter: string, isSource: boolean) => string[][];
  mappingExpressionAddField: (
    selectedField: string,
    newTextNode: ITextNode,
    atIndex: number,
    isTrailer: boolean,
  ) => void;
  mappingExpressionClearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number,
  ) => ITextNode;
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
  const trailerNode = markup.querySelector("#" + trailerID);
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
    if (expressionUpdatedSubscription) {
      expressionUpdatedSubscription.unsubscribe();
    }
    expressionUpdatedSubscription = mappingExprObs.subscribe(
      (updatedEvent: IExpressionUpdatedEvent) => {
        updateExpressionMarkup();
        restoreCaretPosition(updatedEvent);
      },
    );
  }
  updateExpressionMarkup();
  moveCaretToEnd();
}

export const ExpressionContent: FunctionComponent<IExpressionContentProps> = ({
  executeFieldSearch,
  mappingExpressionAddField,
  mappingExpressionClearText,
  isMappingExpressionEmpty,
  mappingExpressionInit,
  mappingExpressionInsertText,
  mappingExpressionObservable,
  mappingExpressionRemoveField,
  mappingExpression,
  trailerId,
}) => {
  let selectedField: string;

  let addFieldToExpression: (
    selectedField: string,
    newTextNode: ITextNode,
    atIndex: number,
    isTrailer: boolean,
  ) => void;
  let clearText: (
    nodeId?: string,
    startOffset?: number,
    endOffset?: number,
  ) => ITextNode;
  let fieldSearch: (searchFilter: string, isSource: boolean) => string[][];

  function insertTextAtCaretPosition(key: string) {
    const range = window.getSelection()!.getRangeAt(0);
    const startContainer = range.startContainer;
    const startOffset = range.startOffset;

    if (markup && startContainer === markup) {
      if (startOffset === 0) {
        mappingExpressionInsertText(key, getCaretPositionNodeId(), 0);
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
      selection.focusNode &&
      selection.focusNode.nodeType === selection.focusNode.TEXT_NODE &&
      selection.focusNode.textContent &&
      selection.focusOffset === selection.focusNode.textContent.length
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

  function fieldCandidateIndex(fieldStr: string): number {
    for (let i = 0; i < mappedFieldCandidates.length; i++) {
      if (mappedFieldCandidates[i][0] === fieldStr) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Handle key down events.
   *
   * @param event - expression keyboard event
   */
  function onKeyDown(event: React.KeyboardEvent<HTMLDivElement>): void {
    if ("Backspace" === event.key) {
      // TODO handle cursor position
      event.preventDefault();
      removeTokenAtCaretPosition(true);
      if (searchMode) {
        updateSearchMode();
      }
    } else if ("Delete" === event.key) {
      event.preventDefault();
      removeTokenAtCaretPosition(false);
      if (searchMode) {
        updateSearchMode();
      }
    }
  }

  function onKeyPress(event: React.KeyboardEvent<HTMLDivElement>) {
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
      searchMode = event.key === "@" ? true : false;
      if (searchMode) {
        atContainer = window.getSelection()!.getRangeAt(0).startContainer;
        atIndex = window.getSelection()!.getRangeAt(0).startOffset;
        searchFilter = "";
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
    const pasted = event.clipboardData.getData("text/plain");
    // TODO handle cursor position... for now just append to the end
    mappingExpressionInsertText(pasted);
  }

  /**
   * The user has selected a field from the search select menu.
   *
   * @param value
   */
  function insertSelectedField(index: number): void {
    if (index >= mappedFieldCandidates.length) {
      return;
    }
    selectedField = mappedFieldCandidates[index][1];
    if (!selectedField) {
      return;
    }
    const newTextNode = clearAtText(getCaretPositionNodeId(atContainer));
    if (newTextNode === null) {
      return;
    }
    const isTrailer = getCaretPositionNodeId(atContainer) === trailerID;
    addFieldToExpression(selectedField, newTextNode, atIndex, isTrailer);
    clearSearchMode(false);
    markup!.focus();
  }

  /**
   * Clear user input from the selected range offset within the TextNode at
   * the specified node ID.  The input will become a FieldNode so we don't
   * need the text.  Return the new UUID position indicator.
   */
  function clearAtText(nodeId: string): ITextNode | null {
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
    if (!startContainer) {
      const selection = window.getSelection();
      if (!selection || selection.rangeCount === 0) {
        return trailerID;
      }
      startContainer = selection!.getRangeAt(0).startContainer;
    }
    return startContainer.parentElement!.getAttribute("id")!;
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
    searchFilter = "";
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

  const initMappingExpression = useCallback(() => {
    initializeMappingExpression();
  }, []);

  addFieldToExpression = mappingExpressionAddField;
  clearText = mappingExpressionClearText;
  fieldSearch = executeFieldSearch;
  getMappingExpression = () => mappingExpression || "";
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
    initMappingExpression();
    return () => uninitializeMappingExpression();
  }, [initMappingExpression, mappingExpression]);

  return (
    <>
      <Form>
        <FormGroup fieldId="expressionContent">
          <Tooltip
            content={"Enter text or '@' for source fields menu."}
            enableFlip={true}
            entryDelay={2000}
            position={"left"}
          >
            <div
              id="expressionMarkup"
              key="expressionMarkup-div"
              aria-label="Expression Content"
              contentEditable
              className="ExpressionFieldSearch"
              suppressContentEditableWarning={true}
              onChange={onChange}
              onKeyDown={onKeyDown}
              onKeyPress={onKeyPress}
              onPaste={onPaste}
              ref={(el) => (markup = el)}
              tabIndex={-1}
            />
          </Tooltip>
        </FormGroup>
      </Form>
      <div>
        {mappedFieldCandidates.length > 0 && (
          <span>
            <ExpressionFieldSearch
              clearSearchMode={clearSearchMode}
              fieldCandidateIndex={fieldCandidateIndex}
              insertSelectedField={insertSelectedField}
              mappedFieldCandidates={mappedFieldCandidates}
            />
          </span>
        )}
      </div>
    </>
  );
};
