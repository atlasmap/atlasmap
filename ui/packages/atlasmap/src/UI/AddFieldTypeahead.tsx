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
import React, {
  ChangeEvent,
  FunctionComponent,
  ReactElement,
  useCallback,
} from 'react';
import {
  Select,
  SelectGroup,
  SelectOption,
  SelectProps,
} from '@patternfly/react-core';

import styles from './AddFieldTypeahead.module.css';
import { useToggle } from '../Atlasmap/utils';

export interface IAddFieldTypeaheadField {
  label: string;
  group: string;
  onAdd: () => void;
}

export interface IAddFieldTypeaheadProps {
  fields: IAddFieldTypeaheadField[];
  ariaLabelTypeAhead: string;
  placeholderText: string;
}

export const AddFieldTypeahead: FunctionComponent<IAddFieldTypeaheadProps> = ({
  fields,
  ariaLabelTypeAhead,
  placeholderText,
  ...props
}) => {
  const { state, toggle, toggleOff } = useToggle(false);

  const renderOptions = (fields: IAddFieldTypeaheadField[]) => {
    const groups = fields.reduce<{ [group: string]: ReactElement[] }>(
      (groups, f) => {
        groups[f.group] = [
          ...(groups[f.group] || []),
          <SelectOption
            key={f.label}
            value={{
              ...f,
              toString: () => f.label,
              compareTo: (c) =>
                f.label.localeCompare((c as IAddFieldTypeaheadField).label) ===
                0,
            }}
            data-testid={`add-field-option-${f.label}`}
            className={styles.field}
          >
            {f.label}
          </SelectOption>,
        ];
        return groups;
      },
      {},
    );
    return Object.entries<ReactElement[]>(groups).map(
      ([groupName, elements]) => (
        <SelectGroup label={groupName} key={groupName}>
          {elements}
        </SelectGroup>
      ),
    );
  };

  const filterFields = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      try {
        const searchValueRX = new RegExp(e.target.value, 'i');
        return renderOptions(fields.filter((f) => searchValueRX.test(f.label)));
      } catch (err) {}
      return renderOptions(fields);
    },
    [fields],
  );

  const onSelect: SelectProps['onSelect'] = useCallback(
    (_e, f) => {
      (f as IAddFieldTypeaheadField).onAdd();
      toggleOff();
    },
    [toggleOff],
  );

  return (
    <div {...props}>
      <Select
        variant={'typeahead'}
        typeAheadAriaLabel={ariaLabelTypeAhead}
        onToggle={toggle}
        isOpen={state}
        placeholderText={placeholderText}
        onFilter={filterFields}
        onSelect={onSelect}
        maxHeight={300}
        className={styles.select}
      >
        {renderOptions(fields)}
      </Select>
    </div>
  );
};
