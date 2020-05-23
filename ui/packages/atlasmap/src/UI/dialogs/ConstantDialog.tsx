import React, {
  FunctionComponent,
  useState,
  useEffect,
  useCallback,
} from "react";

import {
  Form,
  FormGroup,
  TextInput,
  FormSelect,
  FormSelectOption,
} from "@patternfly/react-core";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";

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
}
export const ConstantDialog: FunctionComponent<IConstantDialogProps> = ({
  title,
  value: initialValue = "",
  valueType: initialValueType = "",
  valueTypeOptions,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [value, setValue] = useState(initialValue);
  const [valueType, setValueType] = useState(initialValueType);

  const reset = useCallback(() => {
    setValue(initialValue);
    setValueType(initialValueType);
  }, [initialValue, initialValueType]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ value: value, valueType });
    reset();
  }, [onConfirm, reset, value, valueType]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  // make sure to resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={value.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={"Name"} fieldId={"name"} isRequired={true}>
          <TextInput
            value={value}
            onChange={setValue}
            id={"name"}
            autoFocus={true}
            isRequired={true}
          />
        </FormGroup>
        <FormGroup label={"Value type"} fieldId={"valueType"}>
          <FormSelect
            value={valueType}
            aria-label={"Select value type"}
            onChange={setValueType}
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
