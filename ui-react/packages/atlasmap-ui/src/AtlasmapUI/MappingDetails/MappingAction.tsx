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
  value: string;
  actions: { name: string; value: string }[];
  args?: { name: string; value: string }[];
  onChange: (value: string) => void;
  onRemoveTransformation: () => void;
}

export const MappingAction: FunctionComponent<IMappingActionProps> = ({
  value,
  actions,
  args = [],
  onChange,
  onRemoveTransformation,
}) => {
  const id = `field-action-${value}`;
  return (
    <>
      <div className={css(styles.spaced)}>
        <InputGroup>
          <FormSelect value={value} id={id} onChange={onChange}>
            {actions.map((a, idx) => (
              <FormSelectOption label={a.name} value={a.value} key={idx}/>
            ))}
          </FormSelect>
          <Button variant={'control'} onClick={onRemoveTransformation}>
            <CloseIcon />
          </Button>
        </InputGroup>
      </div>
      {args.map((a, idx) => (
        <div className={css(styles.spaced)} key={idx}>
          <InputGroup>
            <InputGroupText>{a.name}</InputGroupText>
            <TextInput id={a.name} value={a.value} />
          </InputGroup>
        </div>
      ))}
    </>
  );
};
