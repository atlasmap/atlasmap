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

export interface ICustomClass {
  value: string;
  collectionType: string;
}

export interface ICustomClassDialogProps {
  title: string;
  value?: string;
  collectionType?: string;
  collectionTypeOptions: ValueTypeOption[];
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (constant: ICustomClass) => void;
}
export const CustomClassDialog: FunctionComponent<ICustomClassDialogProps> = ({
  title,
  value: initialValue = "",
  collectionType: initialCollectionType = "",
  collectionTypeOptions,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [value, setValue] = useState(initialValue);
  const [collectionType, setCollectionType] = useState(initialCollectionType);

  const reset = useCallback(() => {
    setValue(initialValue);
    setCollectionType(initialCollectionType);
  }, [initialValue, initialCollectionType]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ value: value, collectionType: collectionType });
    reset();
  }, [onConfirm, reset, value, collectionType]);

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
        <FormGroup
          label={"Custom class package name"}
          fieldId={"name"}
          isRequired={true}
        >
          <TextInput
            value={value}
            onChange={setValue}
            id={"name"}
            autoFocus={true}
            isRequired={true}
            placeholder={"com.package.class"}
          />
        </FormGroup>
        <FormGroup label={"Collection type"} fieldId={"valueType"}>
          <FormSelect
            value={collectionType}
            aria-label={"Select value type"}
            onChange={setCollectionType}
          >
            {collectionTypeOptions.map(({ label, value }, idx) => (
              <FormSelectOption key={idx} value={value} label={label} />
            ))}
          </FormSelect>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
