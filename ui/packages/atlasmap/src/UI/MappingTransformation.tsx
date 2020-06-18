import React, { FunctionComponent } from "react";

import {
  Button,
  FormSelect,
  FormSelectOption,
  InputGroup,
  TextInput,
  FormGroup,
} from "@patternfly/react-core";
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

export const MappingTransformation: FunctionComponent<IMappingTransformationProps> = ({
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
        return (
          <FormGroup fieldId={argId} label={a.label} key={idx}>
            {a.options ? (
              <FormSelect
                value={a.value}
                id={argId}
                isDisabled={disableTransformation}
                onChange={(value) =>
                  onTransformationArgumentChange(a.name, value)
                }
                data-testid={a.name}
              >
                {a.options.map((option, optIndx) => (
                  <FormSelectOption
                    label={option.name}
                    value={option.value}
                    key={optIndx}
                  />
                ))}
              </FormSelect>
            ) : (
              <TextInput
                id={argId}
                type="text"
                name={a.name}
                isDisabled={disableTransformation}
                defaultValue={
                  a.value
                } /* uncontrolled component because the state will be updated slowly after some API call */
                onChange={(value) =>
                  onTransformationArgumentChange(a.name, value)
                }
                data-testid={`insert-transformation-parameter-${a.name}-input-field`}
              />
            )}
          </FormGroup>
        );
      })}
    </>
  );
};
