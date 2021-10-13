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
  IConfirmationDialogProps,
} from './ConfirmationDialog';
import {
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  TextInput,
  Tooltip,
  ValidatedOptions,
} from '@patternfly/react-core';

import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

interface ValueTypeOption {
  label: string;
  value: string;
}

export interface IConstant {
  name: string;
  value: string;
  valueType: string;
}

export interface IConstantDialogProps {
  title: string;
  name?: string;
  value?: string;
  valueType?: string;
  valueTypeOptions: ValueTypeOption[];
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (constant: IConstant) => void;
  onValidation: (name: string, value: string) => boolean;
}
export const ConstantDialog: FunctionComponent<IConstantDialogProps> = ({
  title,
  name: initialName = '',
  value: initialValue = '',
  valueType: initialValueType = '',
  valueTypeOptions,
  isOpen,
  onCancel,
  onConfirm,
  onValidation,
}) => {
  const [name, setName] = useState(initialName);
  const [value, setValue] = useState(initialValue);
  const [valueType, setValueType] = useState(initialValueType);
  const [isConstantNameUnique, setConstantNameUnique] = useState(true);
  const [isConstantValid, setConstantValid] = useState(false);
  const [isConstantNameValid, setConstantNameValid] = useState(
    ValidatedOptions.default,
  );

  const reset = useCallback(() => {
    setName(initialName);
    setValue(initialValue);
    setValueType(initialValueType);
    setConstantValid(false);
    setConstantNameValid(ValidatedOptions.default);
    setConstantNameUnique(true);
  }, [initialName, initialValue, initialValueType]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ name, value: value, valueType });
    reset();
  }, [onConfirm, reset, name, value, valueType]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  function handleOnNameChange(name: string) {
    validateConstant(name, value);
    setName(name);
  }

  function handleOnValueChange(value: string) {
    validateConstant(name, value);
    setValue(value);
  }

  function validateConstant(name: string, value: string): boolean {
    if (!name || name.length === 0) {
      setConstantNameValid(ValidatedOptions.default);
      setConstantValid(false);
      return false;
    }
    const nameRegex = /^[a-zA-Z0-9_@-]+$/;
    if (!nameRegex.test(name)) {
      setConstantNameValid(ValidatedOptions.error);
      return false;
    }
    setConstantNameValid(ValidatedOptions.success);
    const isValid = onValidation(name, value);
    setConstantValid(value.length > 0 && isValid);
    setConstantNameUnique(isValid);
    return isValid;
  }

  // make sure to resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={isConstantValid ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={'Name'} fieldId={'name'} isRequired={true}>
          {!isConstantNameUnique ? (
            <Tooltip
              content={<div>A constant with this name already exists</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <TextInput
                value={name}
                onChange={handleOnNameChange}
                id={'name'}
                autoFocus={true}
                isRequired={true}
                data-testid={'constant-name-text-input-tooltip'}
                style={{ color: 'red' }}
              />
            </Tooltip>
          ) : (
            <TextInput
              value={name}
              onChange={handleOnNameChange}
              id={'name'}
              autoFocus={true}
              isRequired={true}
              data-testid={'constant-name-text-input'}
              validated={isConstantNameValid}
            />
          )}
        </FormGroup>
        <FormGroup label={'Value'} fieldId={'constvalue'} isRequired={true}>
          <TextInput
            value={value}
            onChange={handleOnValueChange}
            id={'constvalue'}
            autoFocus={true}
            isRequired={true}
            data-testid={'constant-value-text-input'}
          />
        </FormGroup>
        <FormGroup label={'Value type'} fieldId={'valueType'}>
          <FormSelect
            value={valueType}
            aria-label={'Select value type'}
            onChange={setValueType}
            data-testid={'constant-type-form-select'}
          >
            {valueTypeOptions.map(({ label, value }, idx) => (
              <FormSelectOption key={idx} value={value} label={label} />
            ))}
          </FormSelect>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
