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
  Checkbox,
  FormGroup,
  FormSelect,
  FormSelectOption,
  InputGroup,
  Split,
  SplitItem,
  TextInput,
} from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

import { TrashIcon } from "@patternfly/react-icons";

export interface ITransformationSelectOption {
  name: string;
  value: string;
}

export interface ITransformationArgument {
  label: string;
  name: string;
  value: string;
  options?: ITransformationSelectOption[];
}

export interface IMappingTransformationProps {
  name: string;
  transformationsOptions: ITransformationSelectOption[];
  transformationsArguments?: ITransformationArgument[];
  disableTransformation: boolean;
  onTransformationArgumentChange: (name: string, value: string) => void;
  onTransformationChange: (value: string) => void;
  onRemoveTransformation?: () => void;
  noPaddings?: boolean;
}

export const MappingTransformation: FunctionComponent<IMappingTransformationProps> =
  ({
    name,
    transformationsOptions,
    transformationsArguments = [],
    disableTransformation,
    onTransformationArgumentChange,
    onTransformationChange,
    onRemoveTransformation,
  }) => {
    const id = `user-field-action-${name}`;
    return (
      <>
        <FormGroup fieldId={`${id}-transformation`}>
          <InputGroup style={{ background: "transparent" }}>
            <FormSelect
              value={name}
              id={id}
              isDisabled={disableTransformation}
              onChange={onTransformationChange}
              data-testid={id}
            >
              {transformationsOptions.map((a, idx) => (
                <FormSelectOption label={a.name} value={a.value} key={idx} />
              ))}
            </FormSelect>
            {onRemoveTransformation && (
              <Button
                variant={"plain"}
                onClick={onRemoveTransformation}
                data-testid={`close-transformation-${name}-button`}
                aria-label="Remove the transformation"
              >
                <TrashIcon />
              </Button>
            )}
          </InputGroup>
        </FormGroup>
        {transformationsArguments.map((a, idx) => {
          const argId = `${id}-transformation-${idx}`;
          const udOption = a.options?.find(
            (option) => option.name === "User defined",
          );
          // If user-defined option, then this must be a delimiter argument.
          // Replace user-defined option value with arg value, since arg options
          // are always reset with static values
          if (
            a.options &&
            udOption &&
            !a.options.find((option) => option.value === a.value)
          ) {
            udOption.value = a.value;
          }
          return a.name !== "delimitingEmptyValues" ? (
            <FormGroup fieldId={argId} label={a.label} key={idx}>
              {a.options ? (
                <Split>
                  <SplitItem>
                    <FormSelect
                      label={a.label}
                      value={a.value}
                      id={argId}
                      isDisabled={disableTransformation}
                      onChange={(value) =>
                        onTransformationArgumentChange(a.name, value)
                      }
                      data-testid={a.name}
                    >
                      {a.options.map((option, optIndx) => {
                        return (
                          <FormSelectOption
                            label={option.name}
                            value={option.value}
                            key={optIndx}
                          />
                        );
                      })}
                    </FormSelect>
                  </SplitItem>
                  {a.value === udOption?.value && (
                    <SplitItem>
                      <TextInput
                        id="userDefined"
                        type="text"
                        name="userDefined"
                        defaultValue={a.value}
                        value={a.value}
                        onChange={(value) =>
                          onTransformationArgumentChange(a.name, value)
                        }
                        data-testid={`userDefined`}
                        autoFocus
                      />
                    </SplitItem>
                  )}
                </Split>
              ) : (
                <TextInput
                  id={argId}
                  type="text"
                  name={a.name}
                  isDisabled={disableTransformation}
                  defaultValue={
                    a.value
                  } /* uncontrolled component because the state will be updated slowly after some API call */
                  value={a.value}
                  onChange={(value) =>
                    onTransformationArgumentChange(a.name, value)
                  }
                  data-testid={`insert-transformation-parameter-${a.name}-input-field`}
                />
              )}
            </FormGroup>
          ) : (
            <Checkbox
              id={argId}
              key={argId}
              label="Delimit empty values"
              aria-label="Delimit empty values"
              isChecked={a.value === "true"}
              onChange={(value) =>
                onTransformationArgumentChange(a.name, value.toString())
              }
            />
          );
        })}
      </>
    );
  };
