import React, { FunctionComponent } from "react";

import {
  Button,
  FormSelect,
  FormSelectOption,
  InputGroup,
  TextInput,
  InputGroupText,
} from "@patternfly/react-core";
import { TrashIcon } from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

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

const styles = StyleSheet.create({
  wrapper: {
    "& + &": {
      borderTop:
        "var(--pf-global--BorderWidth--md) solid var(--pf-global--BorderColor--300)",
    },
    "&:last-child": {
      borderBottom:
        "var(--pf-global--BorderWidth--sm) solid var(--pf-global--BorderColor--100)",
    },
  },
  wrapperPadded: {
    padding: "1rem",
  },
  spaced: {
    margin: "var(--pf-global--spacer--form-element) 0",
  },
});

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
  noPaddings = false,
}) => {
  const id = `user-field-action-${name}`;
  return (
    <div className={css(styles.wrapper, !noPaddings && styles.wrapperPadded)}>
      <div className={css(styles.spaced)}>
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
      </div>
      {transformationsArguments.map((a, idx) => (
        <div className={css(styles.spaced)} key={idx}>
          <InputGroup>
            <InputGroupText>{a.label}</InputGroupText>
            {a.options ? (
              <FormSelect
                value={a.value}
                id={a.name}
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
                id={a.name}
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
          </InputGroup>
        </div>
      ))}
    </div>
  );
};
