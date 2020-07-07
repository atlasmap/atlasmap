import React, {
  FunctionComponent,
  useState,
  useCallback,
  useEffect,
} from "react";

import {
  Button,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Split,
  SplitItem,
  TextInput,
} from "@patternfly/react-core";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";
import { PlusIcon, TimesIcon } from "@patternfly/react-icons";

export interface IParameter {
  name: string;
  value: string;
  options?: IParameterOption[];
  hidden?: boolean;
  required?: boolean;
}

export interface IParameterOption {
  label: string;
  value: string;
}

interface AddParameterProps {
  parameters: IParameter[];
  onSelect: (parameter: IParameter) => void;
}

const SelectParameter: FunctionComponent<AddParameterProps> = ({
  parameters = [],
  onSelect,
}) => {
  const [selectedParameter, setSelectedParameter] = useState("");
  const initialSelectedParameter = "Select...";
  const handleAddParameter = useCallback(() => {
    if (selectedParameter != null) {
      onSelect(parameters[parseInt(selectedParameter)]);
      setSelectedParameter("");
    }
  }, [parameters, selectedParameter, onSelect]);

  return (
    <>
      {parameters.length > 0 && (
        <FormGroup
          fieldId="select-parameter"
          label="Customize additional parameters"
        >
          <Split>
            <SplitItem>
              <FormSelect
                id="selected-paramater"
                value={selectedParameter}
                onChange={setSelectedParameter}
              >
                <FormSelectOption
                  key="initValue"
                  value=""
                  label={initialSelectedParameter}
                />
                {parameters.map((parameter, index) => (
                  <FormSelectOption
                    key={index}
                    value={index}
                    label={parameter.name}
                  />
                ))}
              </FormSelect>
            </SplitItem>
            <SplitItem>
              <Button
                isDisabled={selectedParameter === ""}
                onClick={handleAddParameter}
                variant="link"
                icon={<PlusIcon />}
              >
                Add parameter
              </Button>
            </SplitItem>
          </Split>
        </FormGroup>
      )}
    </>
  );
};

export interface IParametersDialogProps {
  title: string;
  parameters?: IParameter[];
  isOpen: IConfirmationDialogProps["isOpen"];
  onCancel: IConfirmationDialogProps["onCancel"];
  onConfirm: (parameters: IParameter[]) => void;
}

export const ParametersDialog: FunctionComponent<IParametersDialogProps> = ({
  title,
  parameters: initialParameters = [],
  isOpen,
  onCancel,
  onConfirm,
}) => {
  const [definedParameters, setDefinedParameters] = useState<IParameter[]>([]);
  const availableParameters: IParameter[] = initialParameters.filter(
    (p) => !definedParameters.includes(p),
  );

  const reset = useCallback(() => {
    setDefinedParameters(initialParameters.filter((p) => p.required));
  }, [initialParameters]);

  const handleOnConfirm = useCallback(() => {
    onConfirm(definedParameters);
  }, [definedParameters, onConfirm]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  const handleAddParameter = useCallback(
    (parameter: IParameter) => {
      setDefinedParameters(definedParameters.concat(parameter));
    },
    [definedParameters],
  );

  const handleRemoveParameter = useCallback(
    (parameter: IParameter) => {
      setDefinedParameters(definedParameters.filter((p) => p !== parameter));
    },
    [definedParameters],
  );

  const handleOnChangeParameter = useCallback(
    (index, value) => {
      const parameters = [...definedParameters];
      const parameter = { ...parameters[index], value: value };
      parameters[index] = parameter;
      setDefinedParameters(parameters);
    },
    [definedParameters],
  );

  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={definedParameters.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form>
        {definedParameters.map((parameter, index) => (
          <FormGroup
            key={index}
            label={parameter.name}
            fieldId={parameter.name}
          >
            <Split>
              <SplitItem>
                {parameter.options && parameter.options.length > 0 ? (
                  <FormSelect
                    value={parameter.value}
                    onChange={(value) => handleOnChangeParameter(index, value)}
                    id={parameter.name}
                    name={parameter.name}
                    data-testid={parameter.name + "-parameter-form-select"}
                  >
                    {parameter.options.map(({ label, value }, idx) => (
                      <FormSelectOption key={idx} value={value} label={label} />
                    ))}
                  </FormSelect>
                ) : (
                  <TextInput
                    value={parameter.value}
                    onChange={(value) => handleOnChangeParameter(index, value)}
                    id={parameter.name}
                    name={parameter.name}
                    data-testid={parameter.name + "-parameter-text-input"}
                  />
                )}
              </SplitItem>
              {!parameter.required && (
                <SplitItem>
                  <Button
                    variant="plain"
                    aria-label="Action"
                    onClick={(_event) => handleRemoveParameter(parameter)}
                  >
                    <TimesIcon />
                  </Button>
                </SplitItem>
              )}
            </Split>
          </FormGroup>
        ))}

        <SelectParameter
          parameters={availableParameters}
          onSelect={handleAddParameter}
        />
      </Form>
    </ConfirmationDialog>
  );
};
