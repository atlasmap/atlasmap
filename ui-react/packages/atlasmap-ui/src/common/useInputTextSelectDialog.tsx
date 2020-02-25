import {
  Modal,
  Button,
  TextInput,
  FormSelect,
  FormSelectOption,
  InputGroup,
  InputGroupText,
} from '@patternfly/react-core';
import React, {
  FormEvent,
  ReactPortal,
  useCallback,
  useRef,
  useState,
} from 'react';
import { createPortal } from 'react-dom';
import { css, StyleSheet } from '@patternfly/react-styles';
import { CancelCallback } from './useSingleInputDialog';

export type ConfirmInputTextCallback = (
  value1: string,
  value2: string,
  selectValue: string
) => void;

export interface IUseInputTextSelectDialogArgs {
  title: string;
  textLabel1: string;
  textLabel2: string;
  selectLabel: string;
  selectValues: string[][];
  selectDefault: number;
  modalContainer: HTMLElement;
}

export function useInputTextSelectDialog({
  title,
  textLabel1,
  textLabel2,
  selectLabel,
  selectValues,
  selectDefault,
  modalContainer,
}: IUseInputTextSelectDialogArgs): [
  ReactPortal,
  (onConfirm?: ConfirmInputTextCallback, onCancel?: CancelCallback) => void
] {
  const onConfirm = useRef<ConfirmInputTextCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [value, setValue] = useState('');
  const [value2, setValue2] = useState('');
  const [selectValue, setSelectValue] = useState(
    selectValues[selectDefault][0]
  );
  const openModal = (
    onConfirmCb?: ConfirmInputTextCallback,
    onCancelCb?: CancelCallback
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
  };

  const closeModal = () => setIsOpen(false);

  const handleTextInputChange = (
    value: string,
    event: FormEvent<HTMLInputElement>
  ) => {
    setValue(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleTextInputChange2 = (
    value: string,
    event: FormEvent<HTMLInputElement>
  ) => {
    setValue2(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };

  const handleSelect = (value: string) => {
    setSelectValue(value);
  };

  const handleConfirm = useCallback(() => {
    isValid &&
      onConfirm.current &&
      onConfirm.current(value, value2, selectValue);
    closeModal();
  }, [onConfirm, value, value2, selectValue, isValid]);

  const handleCancel = useCallback(() => {
    onCancel.current && onCancel.current();
    closeModal();
  }, [onCancel]);

  const styles = StyleSheet.create({
    iGroup: {
      marginBottom: '1.0rem',
      marginLeft: '0.5rem',
    },
    iGroupTextLabel: {
      width: 125,
    },
  });

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <Button
          key={'confirm'}
          variant={'primary'}
          onClick={handleConfirm}
          isDisabled={!isValid}
        >
          Confirm
        </Button>,
        <Button key={'cancel'} variant={'link'} onClick={handleCancel}>
          Cancel
        </Button>,
      ]}
      isFooterLeftAligned={false}
    >
      {textLabel1 && (
        <InputGroup className={css(styles.iGroup)}>
          <InputGroupText className={css(styles.iGroupTextLabel)}>
            {textLabel1}
          </InputGroupText>
          <TextInput
            key={'text-input-value1'}
            value={value}
            type="text"
            onChange={handleTextInputChange}
            aria-label={title}
            isRequired={true}
            isValid={isValid}
          />
        </InputGroup>
      )}
      {textLabel2.length > 0 && (
        <InputGroup className={css(styles.iGroup)}>
          <InputGroupText className={css(styles.iGroupTextLabel)}>
            {textLabel2}
          </InputGroupText>
          <TextInput
            key={'text-input-value2'}
            value={value2}
            type="text"
            onChange={handleTextInputChange2}
            aria-label={title}
            isRequired={true}
            isValid={isValid}
          />
        </InputGroup>
      )}
      <InputGroup className={css(styles.iGroup)}>
        <InputGroupText className={css(styles.iGroupTextLabel)}>
          {selectLabel}
        </InputGroupText>
        <FormSelect
          value={selectValue}
          id={selectValue}
          onChange={handleSelect}
        >
          {selectValues.map(
            (selectValue: string[], idx: number | undefined) => (
              <FormSelectOption
                label={selectValue[1]}
                value={selectValue[0]}
                key={idx}
              />
            )
          )}
        </FormSelect>
      </InputGroup>
    </Modal>,
    modalContainer
  );

  return [modal, openModal];
}
