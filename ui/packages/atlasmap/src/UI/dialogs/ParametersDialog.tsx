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
  Button,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  InputGroup,
  Switch,
  TextInput,
} from '@patternfly/react-core';
import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from './ConfirmationDialog';
import { PlusIcon, TrashIcon } from '@patternfly/react-icons';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';
import { IParameter } from '@atlasmap/core';

export interface IParametersDialogProps {
  title: string;
  readonly parameters: IParameter[];
  readonly initialParameters: IParameter[];
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (parameters: IParameter[]) => void;
}

export const ParametersDialog: FunctionComponent<IParametersDialogProps> = ({
  title,
  parameters: totalAvailableParameters = [],
  initialParameters,
  isOpen,
  onCancel,
  onConfirm,
}) => {
  // Defined parameters are the current working set of user-selected parameters.
  const [definedParameters, setDefinedParameters] = useState<IParameter[]>([]);

  // Available parameters are: (total-available-parameters - defined-parameters).
  const availableParameters: IParameter[] = totalAvailableParameters.filter(
    (param) => !definedParameters.map((p) => p.name).includes(param.name),
  );

  const reset = useCallback(() => {
    if (initialParameters) {
      setDefinedParameters(initialParameters);
    } else {
      setDefinedParameters([]);
    }
  }, [initialParameters]);

  // Callback to actually set the list of user-selected (defined) parameters.
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
    '--pf-c-form--m-horizontal--md__group--GridTemplateColumns': '340px 1fr',
    '--pf-c-form--GridGap': '0.5rem',
  } as React.CSSProperties;
  const formLabelParamRequired = {
    '--pf-c-form__label--FontSize': 'large',
    '--pf-c-form--m-horizontal--md__group--GridTemplateColumns': '140px 1fr',
    marginBottom: '1.0rem',
  } as React.CSSProperties;
  const formLabelTopPadding = {
    '--pf-c-form--m-horizontal__group-label--md--GridColumnWidth': '380px',
    '--pf-c-form--m-horizontal__group-label--md--PaddingTop': '0rem',
  } as React.CSSProperties;

  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={title}
      onCancel={handleOnCancel}
      onConfirm={definedParameters?.length > 0 ? handleOnConfirm : undefined}
      isOpen={isOpen}
    >
      <Form isHorizontal style={formLabelColumnWidth}>
        {definedParameters.map((parameter, index) => (
          <FormGroup
            fieldId={`${index}-parameter`}
            key={index}
            style={
              !parameter.required ? formLabelTopPadding : formLabelParamRequired
            }
            label={
              parameter.required ? (
                <span>{parameter.label}</span>
              ) : (
                <FormSelect
                  id="selected-paramater"
                  value="0"
                  label={parameter.label}
                  style={{ display: 'flex', marginLeft: 'auto' }}
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
                <span
                  style={{
                    paddingTop: 5,
                    display: 'inline',
                    marginLeft: 'auto',
                    float: 'right',
                  }}
                >
                  <Switch
                    id={parameter.name}
                    name={parameter.name}
                    isChecked={parameter.value === 'true'}
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
                  data-testid={parameter.name + '-parameter-form-select'}
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
                  data-testid={parameter.name + '-parameter-text-input'}
                />
              )}
              {!parameter.required && (
                <Button
                  variant="plain"
                  aria-label="Action"
                  onClick={(_event) => handleRemoveParameter(parameter)}
                  style={{ display: 'inline', marginLeft: '0', float: 'right' }}
                >
                  <TrashIcon />
                </Button>
              )}
            </InputGroup>
          </FormGroup>
        ))}
        <span>
          <Button
            style={{
              display: 'inline',
              float: 'left',
              marginTop: '1.0rem',
              width: '40',
            }}
            isDisabled={
              !availableParameters || availableParameters.length === 0
            }
            onClick={handleAddParameter}
            variant="link"
            icon={<PlusIcon />}
          >
            Add parameter
          </Button>
        </span>
      </Form>
    </ConfirmationDialog>
  );
};
