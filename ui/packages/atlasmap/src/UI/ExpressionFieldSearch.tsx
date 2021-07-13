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
  Select,
  SelectOption,
  SelectOptionObject,
} from '@patternfly/react-core';
import { FunctionComponent } from 'react';
import React from 'react';
import styles from './ExpressionFieldSearch.module.css';
import { useToggle } from '../impl/utils';

export interface IExpressionFieldSearchProps {
  clearSearchMode: (clearAtSign: boolean) => void;
  fieldCandidateIndex: (fieldStr: string) => number;
  insertSelectedField: (index: number) => void;
  mappedFieldCandidates: string[][];
}
let selectValue = '';

export const ExpressionFieldSearch: FunctionComponent<IExpressionFieldSearchProps> =
  ({
    clearSearchMode,
    fieldCandidateIndex,
    insertSelectedField,
    mappedFieldCandidates,
  }) => {
    function onToggleFieldSearch(toggled: boolean): any {
      if (!toggled) {
        mappedFieldCandidates = [];
        clearSearchMode(true);
        candidateSrcElement = null;
        candidateIndex = 0;
      }
    }

    const id = `expr-field-search-${selectValue}`;
    let candidateIndex = 0;
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
        candidateIndex = fieldCandidateIndex(candidateSrcElement);
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
        event.preventDefault();
        if (candidateSrcElement) {
          insertSelectedField(candidateIndex);
        }
      } else if ('ArrowDown' === event.key) {
        event.preventDefault();
        trackSelection(event);
        if (candidateSrcElement) {
          updateCandidate(candidateSrcElement.nextElementSibling);
        }
      } else if ('ArrowUp' === event.key) {
        event.preventDefault();
        trackSelection(event);
        if (candidateSrcElement) {
          updateCandidate(candidateSrcElement.previousElementSibling);
        }
      } else if ('Tab' === event.key) {
        if (!candidateSrcElement && event.srcElement) {
          candidateSrcElement =
            event.srcElement.nextElementSibling.firstElementChild;
          candidateIndex = 0;
          candidateSrcElement.style.backgroundColor = 'lightblue';
        } else if (
          candidateSrcElement &&
          candidateSrcElement.nextElementSibling
        ) {
          event.preventDefault();
          updateCandidate(candidateSrcElement!.nextElementSibling);
        }
      }
    }

    function selectionChanged(
      _event: any,
      value: string | SelectOptionObject,
      _isPlaceholder?: boolean | undefined,
    ): void {
      selectValue = value as string;
      const fieldIndex = fieldCandidateIndex(selectValue);
      if (fieldIndex >= 0) {
        insertSelectedField(fieldIndex);
      }
      onToggleFieldSearch(false);
      toggleOff();
    }

    function createSelectOption(selectField: string[], idx: number): any {
      // Use the display name for documents and field path for fields.
      if (selectField[1].length === 0) {
        return (
          <SelectOption
            label={selectField[0]}
            value={selectField[0]}
            key={idx}
            className={styles.document}
          />
        );
      } else {
        return (
          <SelectOption
            label={selectField[1]}
            value={selectField[1]}
            key={idx}
            keyHandler={onKeyHandler}
            className={styles.field}
          />
        );
      }
    }

    return (
      <div
        aria-label="Expression Field Search"
        className={styles.searchMenu}
        data-testid={'expression-field-search'}
      >
        <Select
          onToggle={toggle}
          isOpen={state}
          value={selectValue}
          id={id}
          onKeyDown={onKeyDown}
          onSelect={selectionChanged}
          data-testid={id}
        >
          {mappedFieldCandidates.map((s, idx: number) =>
            createSelectOption(s, idx),
          )}
        </Select>
      </div>
    );
  };
