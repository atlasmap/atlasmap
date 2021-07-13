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
  ConfirmationDialog,
  LookupTableData,
  LookupTableDialog,
} from '../../UI';
import React, { ReactElement, useCallback, useState } from 'react';
import { getEnumerationValues, updateEnumerationValues } from '../utils';

import { useToggle } from '../utils';

type LookupTableCallback = () => void;

/**
 * Enumeration mapping occurs through a "lookup" table.
 */
export function useEditMappingEnumerationDialog(): [
  ReactElement,
  (cb: LookupTableCallback) => void,
] {
  const { state, toggleOn, toggleOff } = useToggle(false);

  const [enumerationValues, setEnumerationValues] = useState<
    LookupTableData[] | null
  >([]);

  const getEnumValues = () => {
    setEnumerationValues(getEnumerationValues());
  };

  const onConfirm = useCallback(() => {
    updateEnumerationValues(enumerationValues!);
    toggleOff();
  }, [enumerationValues, toggleOff]);

  const dialog = (
    <ConfirmationDialog
      title={'Map Enumeration Values'}
      description={'Map enumeration source values to target values.'}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      isOpen={state}
    >
      {state &&
        enumerationValues &&
        enumerationValues.map((value, idx) => (
          <div key={idx}>
            <LookupTableDialog
              enumerationValue={value}
              sourceKey={idx}
              isOpen={state}
            />
          </div>
        ))}
    </ConfirmationDialog>
  );

  const onOpenDialog = useCallback(() => {
    getEnumValues();
    toggleOn();
  }, [toggleOn]);

  return [dialog, onOpenDialog];
}
