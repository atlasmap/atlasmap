import { FunctionComponent } from "react";
import React from "react";
import {
  Select,
  SelectOption,
  SelectOptionObject,
} from "@patternfly/react-core";
import { StyleSheet, css } from "@patternfly/react-styles";

const styles = StyleSheet.create({
  document: {
    fontWeight: "bold",
  },
  field: {
    fontSize: "small",
    marginLeft: 20,
  },
  searchMenu: {
    margin: "var(--pf-global--spacer--form-element) 0",
    width: "40%",
  },
});

export interface IExpressionFieldSearchProps {
  clearSearchMode: (clearAtSign: boolean) => void;
  fieldCandidateIndex: (fieldStr: string) => number;
  insertSelectedField: (index: number) => void;
  mappedFieldCandidates: string[][];
}
let mouseOverTimeOut: NodeJS.Timeout | null = null;
let selectValue = "";

export const ExpressionFieldSearch: FunctionComponent<IExpressionFieldSearchProps> = ({
  clearSearchMode,
  fieldCandidateIndex,
  insertSelectedField,
  mappedFieldCandidates,
}) => {
  const id = `expr-field-search-${selectValue}`;
  let candidateIndex = 0;
  let candidateSrcElement: any;

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
      candidateSrcElement.style.backgroundColor = "white";
      sibling.focus();
      candidateSrcElement = sibling;
      candidateSrcElement.style.backgroundColor = "lightblue";
    }
  }

  function onKeyHandler(_index: number, _position: string): void {
    // TODO
  }

  function onKeyDown(event: any): void {
    if ("Enter" === event.key) {
      event.preventDefault();
      if (candidateSrcElement) {
        insertSelectedField(candidateIndex);
      }
    } else if ("ArrowDown" === event.key) {
      event.preventDefault();
      trackSelection(event);
      if (candidateSrcElement) {
        updateCandidate(candidateSrcElement.nextElementSibling);
      }
    } else if ("ArrowUp" === event.key) {
      event.preventDefault();
      trackSelection(event);
      if (candidateSrcElement) {
        updateCandidate(candidateSrcElement.previousElementSibling);
      }
    } else if ("Tab" === event.key) {
      if (!candidateSrcElement && event.srcElement) {
        candidateSrcElement =
          event.srcElement.nextElementSibling.firstElementChild;
        candidateIndex = 0;
        candidateSrcElement.style.backgroundColor = "lightblue";
      } else if (
        candidateSrcElement &&
        candidateSrcElement.nextElementSibling
      ) {
        event.preventDefault();
        updateCandidate(candidateSrcElement!.nextElementSibling);
      }
    }
  }

  function onMouseEnter(_event: any): void {
    if (mouseOverTimeOut) {
      clearTimeout(mouseOverTimeOut);
    }
  }

  function onMouseLeave(_event: any): void {
    // mouseOverTimeOut = setTimeout(() => {
    clearSearchMode(true);
    candidateSrcElement = null;
    candidateIndex = 0;
    // }, 750);
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
  }

  function createSelectOption(selectField: string[], idx: number): any {
    if (selectField[1].length === 0) {
      return (
        <SelectOption
          isDisabled={true}
          label={selectField[0]}
          value={selectField[0]}
          key={idx}
          className={css(styles.document)}
        />
      );
    } else {
      return (
        <SelectOption
          label={selectField[0]}
          value={selectField[0]}
          key={idx}
          keyHandler={onKeyHandler}
          className={css(styles.field)}
        />
      );
    }
  }

  return (
    <div
      aria-label="Expression Field Search"
      className={css(styles.searchMenu)}
      data-testid={"expression-field-search"}
    >
      <Select
        isExpanded={true}
        value={selectValue}
        id={id}
        onKeyDown={onKeyDown}
        onSelect={selectionChanged}
        onToggle={() => void 0}
        onMouseEnter={onMouseEnter}
        onMouseLeave={onMouseLeave}
        data-testid={id}
      >
        {mappedFieldCandidates.map((s, idx: number) =>
          createSelectOption(s, idx),
        )}
      </Select>
    </div>
  );
};
