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
  Tooltip,
  ValidatedOptions,
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
  onValidation: (name: string, scope: string) => boolean;
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
  onValidation,
}) => {
  const [name, setName] = useState(initialName);
  const [valueType, setValueType] = useState(initialValueType);
  const [scope, setScope] = useState(initialScope);
  const [isPropertyValid, setPropertyValid] = useState(true);
  const [isPropertyNameValid, setPropertyNameValid] = useState(
    ValidatedOptions.default,
  );
  const [isNameAndScopeUnique, setNameAndScopeUnique] = useState(true);

  const reset = useCallback(() => {
    setName(initialName);
    setValueType(initialValueType);
    setScope(initialScope);
    setPropertyValid(true);
    setPropertyNameValid(ValidatedOptions.default);
    setNameAndScopeUnique(true);
  }, [initialName, initialValueType, initialScope]);

  const handleOnConfirm = useCallback(() => {
    onConfirm({ name, valueType, scope });
    reset();
  }, [name, onConfirm, reset, valueType, scope]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  function handleOnNameChange(name: string) {
    validateProperty(name, scope);
    setName(name);
  }

  function handleOnScopeChange(scope: string) {
    if (validateProperty(name, scope)) {
      setScope(scope);
    }
  }

  function validateProperty(name: string, scope: string): boolean {
    if (!name || name.length === 0) {
      setPropertyNameValid(ValidatedOptions.default);
      return false;
    }
    const nameRegex = /^[a-zA-Z0-9_@]+$/;
    if (!nameRegex.test(name)) {
      setPropertyNameValid(ValidatedOptions.error);
      return false;
    }
    setPropertyNameValid(ValidatedOptions.success);
    const isValid = onValidation(name, scope);
    setNameAndScopeUnique(isValid);
    setPropertyValid(name.length > 0 && isValid);
    return isValid;
  }

  // make sure to resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={isPropertyValid ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        <FormGroup label={"Name"} fieldId={"name"} isRequired={true}>
          {!isNameAndScopeUnique ? (
            <Tooltip
              content={
                <div>A property with this name and scope already exists</div>
              }
              entryDelay={750}
              exitDelay={100}
            >
              <TextInput
                value={name}
                onChange={handleOnNameChange}
                id={"name"}
                autoFocus={true}
                isRequired={true}
                data-testid={"property-name-text-input-tooltip"}
                style={{ color: "red" }}
              />
            </Tooltip>
          ) : (
            <TextInput
              value={name}
              onChange={handleOnNameChange}
              id={"name"}
              autoFocus={true}
              isRequired={true}
              data-testid={"property-name-text-input"}
              validated={isPropertyNameValid}
            />
          )}
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
          {!isNameAndScopeUnique ? (
            <Tooltip
              content={
                <div>A property with this name and scope already exists</div>
              }
              entryDelay={750}
              exitDelay={100}
            >
              <FormSelect
                value={scope}
                aria-label={"Select property scope"}
                onChange={handleOnScopeChange}
                data-testid={"property-scope-form-select"}
                style={{ color: "red" }}
              >
                {scopeOptions.map(({ label, value }, idx) => (
                  <FormSelectOption key={idx} label={label} value={value} />
                ))}
              </FormSelect>
            </Tooltip>
          ) : (
            <FormSelect
              value={scope}
              aria-label={"Select property scope"}
              onChange={handleOnScopeChange}
              data-testid={"property-scope-form-select"}
            >
              {scopeOptions.map(({ label, value }, idx) => (
                <FormSelectOption key={idx} label={label} value={value} />
              ))}
            </FormSelect>
          )}
        </FormGroup>
      </Form>
    </ConfirmationDialog>
  );
};
