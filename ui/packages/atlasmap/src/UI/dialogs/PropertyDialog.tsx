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

interface ValueLabelOption {
  label: string;
  value: string;
}

export interface IProperty {
  name: string;
  valueType: string;
  scope: string;
}

export interface IPropertyDialogProps {
  title: string;
  name?: string;
  valueType?: string;
  valueTypeOptions: ValueLabelOption[];
  scope?: string;
  scopeOptions: ValueLabelOption[];
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (property: IProperty) => void;
}
export const PropertyDialog: FunctionComponent<IPropertyDialogProps> = ({
  title,
  name: initialName = "",
  valueType: initialValueType = "",
  valueTypeOptions,
  scope: initialScope = "",
  scopeOptions,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [name, setName] = useState(initialName);
  const [valueType, setValueType] = useState(initialValueType);
  const [scope, setScope] = useState(initialScope);

  const reset = useCallback(() => {
    setName(initialName);
    setValueType(initialValueType);
    setScope(initialScope);
  }, [initialName, initialValueType, initialScope]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ name, valueType, scope });
    reset();
  }, [name, onConfirm, reset, valueType, scope]);

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
      onConfirm={name.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={"Name"} fieldId={"name"} isRequired={true}>
          <TextInput
            value={name}
            onChange={setName}
            id={"name"}
            autoFocus={true}
            isRequired={true}
            data-testid={"property-name-text-input"}
          />
        </FormGroup>
        <FormGroup label={"Value type"} fieldId={"valueType"}>
          <FormSelect
            value={valueType}
            aria-label={"Select value type"}
            onChange={setValueType}
            data-testid={"property-type-form-select"}
          >
            {valueTypeOptions.map(({ label, value }, idx) => (
              <FormSelectOption key={idx} value={value} label={label} />
            ))}
          </FormSelect>
        </FormGroup>
        <FormGroup label={"Scope"} fieldId={"scope"}>
          <FormSelect
            value={scope}
            aria-label={"Select property scope"}
            onChange={setScope}
            data-testid={"property-scope-form-select"}
          >
            {scopeOptions.map(({ label, value }, idx) => (
              <FormSelectOption key={idx} label={label} value={value} />
            ))}
          </FormSelect>
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
