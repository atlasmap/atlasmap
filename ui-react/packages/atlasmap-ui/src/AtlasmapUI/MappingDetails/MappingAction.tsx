import {
  Button,
  FormSelect,
  FormSelectOption,
  InputGroup,
  InputGroupText,
  TextInput,
} from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';
import { CloseIcon } from '@patternfly/react-icons';
import { css, StyleSheet } from '@patternfly/react-styles';

const styles = StyleSheet.create({
  spaced: {
    margin: 'var(--pf-global--spacer--form-element) 0',
  },
  smallBold: {
    fontSize: 'small',
    fontWeight: 'bold',
  },
});

export interface IMappingActionProps {
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
}

export const MappingAction: FunctionComponent<IMappingActionProps> = ({
  associatedFieldActionName,
  actionsOptions,
  actionDelimiters,
  args = [],
  isMultiplicityAction,
  onArgValueChange,
  onActionChange,
  onActionDelimiterChange,
  onRemoveTransformation,
}) => {
  const id = `user-field-action-${associatedFieldActionName}`;
  return (
    <>
      <div className={css(styles.spaced)}>
        <InputGroup>
          <FormSelect
            className={css(styles.smallBold)}
            value={associatedFieldActionName}
            id={id}
            onChange={onActionChange}
          >
            {actionsOptions.map((a, idx) => (
              <FormSelectOption label={a.name} value={a.value} key={idx} />
            ))}
          </FormSelect>
          {!isMultiplicityAction && (
            <Button
              className={css(styles.smallBold)}
              variant={'control'}
              onClick={onRemoveTransformation}
            >
              <CloseIcon />
            </Button>
          )}
        </InputGroup>
      </div>
      {args.map((a, idx) => (
        <div className={css(styles.spaced)} key={idx}>
          <InputGroup>
            <InputGroupText className={css(styles.smallBold)}>
              {a.label}
            </InputGroupText>
            {isMultiplicityAction && a.label === 'Delimiter' && (
              <FormSelect
                className={css(styles.smallBold)}
                value={a.value}
                id={a.name}
                onChange={onActionDelimiterChange}
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
            {!(isMultiplicityAction && a.label === 'Delimiter') && (
              <TextInput
                className={css(styles.smallBold)}
                id={a.name}
                type="text"
                name={a.name}
                value={a.value}
                onChange={onArgValueChange}
              />
            )}
          </InputGroup>
        </div>
      ))}
    </>
  );
};
