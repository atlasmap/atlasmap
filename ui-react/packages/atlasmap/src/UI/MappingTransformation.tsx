import React, { FunctionComponent } from "react";

import {
  Button,
  FormSelect,
  FormSelectOption,
  InputGroup,
  TextInput,
} from "@patternfly/react-core";
import { TrashIcon } from "@patternfly/react-icons";
import { css, StyleSheet } from "@patternfly/react-styles";

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
  associatedFieldActionName: string;
  actionsOptions: { name: string; value: string }[];
  actionDelimiters: { displayName: string; delimiterValue: string }[];
  currentActionDelimiter: string;
  args?: { label: string; name: string; value: string }[];
  isMultiplicityAction: boolean;
  onArgValueChange: (val: string, event: any) => void;
  onActionChange: (value: string) => void;
  onActionDelimiterChange: (value: string) => void;
  onRemoveTransformation: () => void;
  noPaddings?: boolean;
}

export const MappingTransformation: FunctionComponent<IMappingTransformationProps> = ({
  associatedFieldActionName,
  actionsOptions,
  actionDelimiters,
  args = [],
  isMultiplicityAction,
  onArgValueChange,
  onActionChange,
  onActionDelimiterChange,
  onRemoveTransformation,
  noPaddings = false,
}) => {
  const id = `user-field-action-${associatedFieldActionName}`;
  return (
    <div className={css(styles.wrapper, !noPaddings && styles.wrapperPadded)}>
      <div className={css(styles.spaced)}>
        <InputGroup style={{ background: "transparent" }}>
          <FormSelect
            value={associatedFieldActionName}
            id={id}
            onChange={onActionChange}
            data-testid={id}
          >
            {actionsOptions.map((a, idx) => (
              <FormSelectOption label={a.name} value={a.value} key={idx} />
            ))}
          </FormSelect>
          {!isMultiplicityAction && (
            <Button
              variant={"plain"}
              onClick={onRemoveTransformation}
              data-testid={`close-transformation-${associatedFieldActionName}-button`}
              aria-label="Remove the transformation"
            >
              <TrashIcon />
            </Button>
          )}
        </InputGroup>
      </div>
      {args.map((a, idx) => (
        <div className={css(styles.spaced)} key={idx}>
          <InputGroup>
            {isMultiplicityAction && a.label === "Delimiter" && (
              <FormSelect
                value={a.value}
                id={a.name}
                onChange={onActionDelimiterChange}
                data-testid={a.name}
              >
                {actionDelimiters.map((delimiter, delimiterIdx) => (
                  <FormSelectOption
                    label={delimiter.displayName}
                    value={delimiter.delimiterValue}
                    key={delimiterIdx}
                  />
                ))}
              </FormSelect>
            )}
            {!(isMultiplicityAction && a.label === "Delimiter") && (
              <TextInput
                id={a.name}
                type="text"
                name={a.name}
                value={a.value}
                onChange={onArgValueChange}
                placeholder={`(${a.label})`}
                data-testid={`insert-transformation-parameter-${a.name}-input-field`}
              />
            )}
          </InputGroup>
        </div>
      ))}
    </div>
  );
};
