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
import { Position, editor } from 'monaco-editor';
import {
  Select,
  SelectOption,
  SelectOptionObject,
} from '@patternfly/react-core';
import { FunctionComponent } from 'react';
import React from 'react';
import styles from './ExpressionFieldSearch.module.css';
import { useToggle } from '../impl/utils';

export interface IExpressionFieldSearchProps {
  condExprEditor: editor.IStandaloneCodeEditor | undefined;
  clearSearchMode: () => void;
  insertSelectedField: (docId: string, fieldStr: string) => void;
  insertSelectedNonField: (elementStr: string) => void;
  searchCandidates: string[][];
}
let selectedField = '';

export const ExpressionFieldSearch: FunctionComponent<
  IExpressionFieldSearchProps
> = ({
  condExprEditor,
  clearSearchMode,
  insertSelectedField,
  insertSelectedNonField,
  searchCandidates,
}) => {
  function onToggleFieldSearch(toggled: boolean): any {
    if (!toggled) {
      searchCandidates = [];
      clearSearchMode();
      candidateSrcElement = null;
    }
  }

  const id = `expr-field-search-${selectedField}`;
  let candidateSrcElement: any;
  const { state, toggle, toggleOff } = useToggle(true, onToggleFieldSearch);

  /**
   * Track a candidate selection from either a mouse hover or arrow
   * key navigation.
   *
   * @param event
   * @param index
   */
  function trackSelection(event: any): void {
    if (event.srcElement) {
      candidateSrcElement = event.srcElement;
    }
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

  function onKeyHandler(
    _index: number,
    _innerIndex: number,
    _position: string,
  ): void {
    // TODO
  }

  function onKeyDown(event: any): void {
    if ('Enter' === event.key) {
      if (candidateSrcElement) {
        // insertSelectedField(candidateIndex);
      }
    } else if ('ArrowDown' === event.key) {
      trackSelection(event);
      if (candidateSrcElement) {
        updateCandidate(candidateSrcElement.nextElementSibling);
      }
    } else if ('ArrowUp' === event.key) {
      trackSelection(event);
      if (candidateSrcElement) {
        updateCandidate(candidateSrcElement.previousElementSibling);
      }
    } else if ('Tab' === event.key) {
      if (!candidateSrcElement && event.srcElement) {
        candidateSrcElement =
          event.srcElement.nextElementSibling.firstElementChild;
        candidateSrcElement.style.backgroundColor = 'lightblue';
      } else if (
        candidateSrcElement &&
        candidateSrcElement.nextElementSibling
      ) {
        updateCandidate(candidateSrcElement!.nextElementSibling);
      }
    }
  }

  function selectionChanged(
    event: any,
    value: string | SelectOptionObject,
    isPlaceholder?: boolean | undefined,
  ): void {
    if (!isPlaceholder) {
      const labelId = event.target.getAttribute('label');
      selectedField = value as string;
      if (labelId[0] === '>') {
        selectedField = selectedField.concat('()');
        insertSelectedNonField(selectedField);
      } else if (labelId[0] === ' ') {
        insertSelectedNonField(selectedField);
      } else {
        insertSelectedField(labelId, selectedField);
      }
    }
    onToggleFieldSearch(false);
    toggleOff();
  }

  function createSelectOption(selectField: string[], idx: number): any {
    // Check for document fields.
    if (selectField[1][0] === '/') {
      return (
        <SelectOption
          label={selectField[0]}
          value={selectField[1]}
          key={idx}
          keyHandler={onKeyHandler}
          index={idx}
          className={styles.field}
        />
      );
      // Check for field action functions.
    } else if (selectField[1][0] === '*') {
      return (
        <SelectOption
          label={selectField[0]}
          value={selectField[1].substr(1, selectField[1].length)}
          key={idx}
          keyHandler={onKeyHandler}
          index={idx}
          className={styles.field}
        />
      );
      // Use the display name for documents and field path for fields.
    } else {
      return (
        <SelectOption
          label={selectField[0]}
          value={selectField[0]}
          key={idx}
          index={idx}
          className={styles.document}
          isPlaceholder={true}
        />
      );
    }
  }

  condExprEditor?.setPosition(new Position(1, 1));

  return (
    <div
      aria-label="Expression Field Search"
      className={styles.searchMenu}
      data-testid={'expression-field-search'}
    >
      {searchCandidates.length > 0 && (
        <span>
          <Select
            onToggle={toggle}
            isOpen={state}
            value={selectedField}
            id={id}
            onKeyDown={onKeyDown}
            onSelect={selectionChanged}
            data-testid={id}
            defaultValue={searchCandidates[0]}
          >
            {searchCandidates.map((s, idx: number) =>
              createSelectOption(s, idx),
            )}
          </Select>
        </span>
      )}
    </div>
  );
};
