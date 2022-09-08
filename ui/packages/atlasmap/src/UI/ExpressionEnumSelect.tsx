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
import { EnumValue, useToggle } from '../impl/utils';
import {
  Select,
  SelectOption,
  SelectOptionObject,
} from '@patternfly/react-core';

import { FunctionComponent } from 'react';
import React from 'react';
import styles from './ExpressionEnumSelect.module.css';

export interface IExpressionEnumSelectProps {
  selectedNodeId: string;
  enumCandidates: EnumValue[];
  clearEnumSelect: () => void;
  onEnumSelect: (selectedNodeId: string, selectedIndex: number) => void;
}
let selectValue = '';

export const ExpressionEnumSelect: FunctionComponent<
  IExpressionEnumSelectProps
> = ({ selectedNodeId, enumCandidates, clearEnumSelect, onEnumSelect }) => {
  const id = `expr-enum-select-${selectValue}`;
  const { state, toggle, toggleOff } = useToggle(true, onToggleEnumSelect);

  function onToggleEnumSelect(toggled: boolean): any {
    if (!toggled) {
      enumCandidates = [];
      clearEnumSelect();
    }
  }

  function selectionChanged(
    event: any,
    value: string | SelectOptionObject,
    _isPlaceholder?: boolean | undefined,
  ): void {
    selectValue = value as string;
    onEnumSelect(selectedNodeId, event.currentTarget.id.split('-').pop());
    onToggleEnumSelect(false);
    toggleOff();
  }

  function createSelectOption(selectField: string, idx: number): any {
    if (selectField[1].length === 0) {
      return (
        <SelectOption
          isDisabled={true}
          label={selectField}
          value={selectField}
          key={idx}
          className={styles.document}
        />
      );
    } else {
      return (
        <SelectOption
          label={selectField}
          value={selectField}
          key={idx}
          className={styles.field}
        />
      );
    }
  }

  return (
    <div
      aria-label="Expression Enumeration"
      className="enumSelectMenu"
      data-testid={'expression-enumeration-select'}
    >
      <Select
        onToggle={toggle}
        isOpen={state}
        value={selectValue}
        id={id}
        onSelect={selectionChanged}
        data-testid={id}
      >
        {enumCandidates.map((s, idx: number) =>
          createSelectOption(s.name, idx),
        )}
      </Select>
    </div>
  );
};
