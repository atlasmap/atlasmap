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
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Label,
} from '@patternfly/react-core';
import React, { FunctionComponent, useState } from 'react';

import { IConfirmationDialogProps } from './ConfirmationDialog';
import styles from './LookupTableDialog.module.css';

export type LookupTableData = {
  sourceEnumValue: string;
  targetEnumValues: string[];
  selectedTargetEnumValue: string;
};
export interface ILookupTableDialogProps {
  enumerationValue: LookupTableData;
  sourceKey: number;
  isOpen: IConfirmationDialogProps['isOpen'];
}

export const LookupTableDialog: FunctionComponent<ILookupTableDialogProps> = ({
  enumerationValue,
  sourceKey,
  isOpen,
}) => {
  const [targetEnum, setTargetEnum] = useState(
    enumerationValue.selectedTargetEnumValue,
  );

  const onChangeTargetEnum = (
    enumValue: string,
    _event: React.FormEvent<HTMLSelectElement>,
  ) => {
    setTargetEnum(enumValue);
    enumerationValue.selectedTargetEnumValue = enumValue;
  };

  return (
    <Form>
      <FormGroup className={styles.iGroup} fieldId={'lookup-table-row'}>
        <Label className={styles.iSelectLabel}>
          {enumerationValue.sourceEnumValue}
        </Label>
        <FormSelect
          className={styles.iSelectBody}
          value={targetEnum}
          aria-label={'enum-map'}
          autoFocus={true}
          onChange={onChangeTargetEnum}
          data-testid={'enum-map-select'}
          key={`${targetEnum}-${sourceKey}`}
        >
          {isOpen &&
            enumerationValue.targetEnumValues &&
            enumerationValue.targetEnumValues.map((value, idx) => (
              <FormSelectOption
                key={`tgtenum-${idx}`}
                value={value}
                label={value}
              />
            ))}
        </FormSelect>
      </FormGroup>
    </Form>
  );
};
