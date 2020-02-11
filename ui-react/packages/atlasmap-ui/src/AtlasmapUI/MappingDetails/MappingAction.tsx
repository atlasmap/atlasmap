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
});

export interface IMappingActionProps {
  associatedFieldActionName: string;
  actionsOptions: { name: string; value: string }[];
  args?: { label: string; name: string; value: string }[];
  isMultiplicityAction: boolean;
  onArgValueChange: (val: string, event: any) => void;
  onActionChange: (value: string) => void;
  onRemoveTransformation: () => void;
}

export const MappingAction: FunctionComponent<IMappingActionProps> = ({
  associatedFieldActionName,
  actionsOptions,
  args = [],
  isMultiplicityAction,
  onArgValueChange,
  onActionChange,
  onRemoveTransformation,
}) => {
  const id = `user-field-action-${associatedFieldActionName}`;
  return (
    <>
      <div className={css(styles.spaced)}>
        <InputGroup>
          <FormSelect value={associatedFieldActionName} id={id} onChange={onActionChange}>
            {actionsOptions.map((a, idx) => (
              <FormSelectOption label={a.name} value={a.value} key={idx}/>
            ))}
          </FormSelect>
          {!isMultiplicityAction && (
            <Button variant={'control'} onClick={onRemoveTransformation}>
              <CloseIcon />
            </Button>
          )}
        </InputGroup>
      </div>
      {args.map((a, idx) => (
        <div className={css(styles.spaced)} key={idx}>
          <InputGroup>
            <InputGroupText>{a.label}</InputGroupText>
            <TextInput
              id={a.name}
              type="text"
              defaultValue={a.value}
              name={a.name}
              value={a.value}
              onChange={onArgValueChange}
            />
          </InputGroup>
        </div>
      ))}
    </>
  );
};
