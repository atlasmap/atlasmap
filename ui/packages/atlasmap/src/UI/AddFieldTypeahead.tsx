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
  FormSelect,
  FormSelectOption,
  FormSelectOptionGroup,
} from '@patternfly/react-core';
import React, { FunctionComponent, ReactElement, useCallback } from 'react';

import styles from './AddFieldTypeahead.module.css';

export interface IAddFieldTypeaheadField {
  label: string;
  group: string;
  onAdd: () => void;
}

export interface IAddFieldTypeaheadProps {
  fields: IAddFieldTypeaheadField[];
  ariaLabelTypeAhead: string;
  placeholderText: string;
  isSource: boolean;
}

export const AddFieldTypeahead: FunctionComponent<IAddFieldTypeaheadProps> = ({
  fields,
  ariaLabelTypeAhead,
  placeholderText,
  isSource,
}) => {
  const renderOptions = (
    fields: IAddFieldTypeaheadField[],
    placeholderField: IAddFieldTypeaheadField,
  ) => {
    fields.unshift(placeholderField);
    const groups = fields.reduce<{ [group: string]: ReactElement[] }>(
      (groups, f) => {
        const optValue = f.group + '\\' + f.label;
        groups[f.group] = [
          ...(groups[f.group] || []),
          <FormSelectOption
            isPlaceholder={f.group.length === 0}
            isDisabled={false}
            label={f.label}
            key={
              optValue + (isSource ? '-source' : '-target') + fields.indexOf(f)
            }
            value={optValue}
            data-testid={`add-field-option-${optValue}`}
            className={styles.field}
          >
            {f.label}
          </FormSelectOption>,
        ];
        return groups;
      },
      {},
    );
    return Object.entries<ReactElement[]>(groups).map(
      ([groupName, elements]) => (
        <FormSelectOptionGroup label={groupName} key={groupName}>
          {elements}
        </FormSelectOptionGroup>
      ),
    );
  };

  const onChange = useCallback(
    (value: string, _e: React.FormEvent<HTMLSelectElement>) => {
      const valueComps = value.split('\\');
      const selField = fields.find(
        (f) => f.label === valueComps[1] && f.group === valueComps[0],
      );
      if (selField) {
        (selField as IAddFieldTypeaheadField).onAdd();
      }
    },
    [fields],
  );

  const placeholderField: IAddFieldTypeaheadField = {
    label: placeholderText,
    group: '',
    onAdd: () => void {},
  };

  return (
    <div>
      <FormSelect
        aria-label={ariaLabelTypeAhead}
        className={styles.select}
        data-testid={'mapping-details-add-field'}
        id={ariaLabelTypeAhead}
        onChange={onChange}
        placeholder={placeholderText}
      >
        {renderOptions(fields, placeholderField)}
      </FormSelect>
    </div>
  );
};
