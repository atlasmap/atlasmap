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
} from "./ConfirmationDialog";
import {
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  TextInput,
  Tooltip,
} from "@patternfly/react-core";

import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from "react";

interface ValueTypeOption {
  label: string;
  value: string;
}

export interface IConstant {
  value: string;
  valueType: string;
}

export interface IConstantDialogProps {
  title: string;
  value?: string;
  valueType?: string;
  valueTypeOptions: ValueTypeOption[];
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (constant: IConstant) => void;
  onValidation: (value: string) => boolean;
}
export const ConstantDialog: FunctionComponent<IConstantDialogProps> = ({
  title,
  value: initialValue = "",
  valueType: initialValueType = "",
  valueTypeOptions,
  isOpen,
  onCancel,
  onConfirm,
  onValidation,
}) => {
  const [value, setValue] = useState(initialValue);
  const [valueType, setValueType] = useState(initialValueType);
  const [isConstantValid, setConstantValid] = useState(true);
  const [isValueUnique, setValueUnique] = useState(true);

  const reset = useCallback(() => {
    setValue(initialValue);
    setValueType(initialValueType);
    setConstantValid(true);
    setValueUnique(true);
  }, [initialValue, initialValueType]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ value: value, valueType });
    reset();
  }, [onConfirm, reset, value, valueType]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  function handleOnValueChange(value: string) {
    validateConstant(value);
    setValue(value);
  }

  function validateConstant(value: string) {
    const isValid = onValidation(value);
    setValueUnique(isValid);
    setConstantValid(value.length > 0 && isValid);
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
        <FormGroup label={"Value"} fieldId={"constvalue"} isRequired={true}>
          {!isValueUnique ? (
            <Tooltip
              content={<div>A constant with this value already exists</div>}
              entryDelay={750}
              exitDelay={100}
            >
              <TextInput
                value={value}
                onChange={handleOnValueChange}
                id={"constvalue"}
                autoFocus={true}
                isRequired={true}
                data-testid={"constant-value-text-input"}
                style={{ color: "red" }}
              />
            </Tooltip>
          ) : (
            <TextInput
              value={value}
              onChange={handleOnValueChange}
              id={"constvalue"}
              autoFocus={true}
              isRequired={true}
              data-testid={"constant-value-text-input"}
            />
          )}
        </FormGroup>
        <FormGroup label={"Value type"} fieldId={"valueType"}>
          <FormSelect
            value={valueType}
            aria-label={"Select value type"}
            onChange={setValueType}
            data-testid={"constant-type-form-select"}
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
