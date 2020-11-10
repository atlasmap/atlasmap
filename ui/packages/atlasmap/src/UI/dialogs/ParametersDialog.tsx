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
  InputGroup,
  Switch,
  TextInput,
} from "@patternfly/react-core";

import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from "./ConfirmationDialog";
import { PlusIcon, TrashIcon } from "@patternfly/react-icons";

export interface IParameter {
  name: string;
  label: string;
  value: string;
  boolean?: boolean;
  options?: IParameterOption[];
  hidden?: boolean;
  required?: boolean;
}

export interface IParameterOption {
  label: string;
  value: string;
}

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
    (param) => !definedParameters.map((p) => p.name).includes(param.name),
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

  const handleAddParameter = useCallback(() => {
    setDefinedParameters(
      definedParameters.concat({ ...availableParameters[0] }),
    );
  }, [definedParameters, availableParameters]);

  const handleRemoveParameter = useCallback(
    (parameter: IParameter) => {
      setDefinedParameters(definedParameters.filter((p) => p !== parameter));
    },
    [definedParameters],
  );

  const handleChangeParameterValue = useCallback(
    (index, value) => {
      const parameters = [...definedParameters];
      const parameter = { ...parameters[index], value: value };
      parameters[index] = parameter;
      setDefinedParameters(parameters);
    },
    [definedParameters],
  );

  const handleChangeParameter = useCallback(
    (index, availableParameterIndex) => {
      if (availableParameterIndex === 0) {
        return; //nothing changed
      }
      const parameters = [...definedParameters];
      const parameter = { ...availableParameters[availableParameterIndex - 1] };
      parameters[index] = parameter;
      setDefinedParameters(parameters);
    },
    [definedParameters, availableParameters],
  );

  const formLabelColumnWidth = {
    "--pf-c-form--m-horizontal--md__group--GridTemplateColumns": "260px 1fr",
  } as React.CSSProperties;
  const formLabelTopPadding = {
    "--pf-c-form__label--PaddingTop": "0",
  } as React.CSSProperties;

  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={definedParameters.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form isHorizontal style={formLabelColumnWidth}>
        {definedParameters.map((parameter, index) => (
          <FormGroup
            fieldId={`${index}-parameter`}
            key={index}
            style={!parameter.required ? formLabelTopPadding : {}}
            label={
              parameter.required ? (
                <b>{parameter.name}</b>
              ) : (
                <FormSelect
                  id="selected-paramater"
                  value="0"
                  label={parameter.name}
                  onChange={(availableParameterIndex) =>
                    handleChangeParameter(index, availableParameterIndex)
                  }
                >
                  {[parameter]
                    .concat(availableParameters)
                    .map((parameter, index) => (
                      <FormSelectOption
                        key={index}
                        value={index}
                        label={parameter.label}
                      />
                    ))}
                </FormSelect>
              )
            }
          >
            <InputGroup>
              {parameter.boolean ? (
                <span style={{ paddingTop: 5 }}>
                  <Switch
                    id={parameter.name}
                    name={parameter.name}
                    isChecked={parameter.value === "true"}
                    onChange={(checked) => {
                      handleChangeParameterValue(index, String(checked));
                    }}
                  />
                </span>
              ) : parameter.options && parameter.options.length > 0 ? (
                <FormSelect
                  value={parameter.value}
                  onChange={(value) => handleChangeParameterValue(index, value)}
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
                  onChange={(value) => handleChangeParameterValue(index, value)}
                  id={parameter.name}
                  name={parameter.name}
                  data-testid={parameter.name + "-parameter-text-input"}
                />
              )}
              {!parameter.required && (
                <Button
                  variant="plain"
                  aria-label="Action"
                  onClick={(_event) => handleRemoveParameter(parameter)}
                >
                  <TrashIcon />
                </Button>
              )}
            </InputGroup>
          </FormGroup>
        ))}
        <Button
          isDisabled={availableParameters.length === 0}
          onClick={handleAddParameter}
          variant="link"
          icon={<PlusIcon />}
        >
          Add parameter
        </Button>
      </Form>
    </ConfirmationDialog>
  );
};
