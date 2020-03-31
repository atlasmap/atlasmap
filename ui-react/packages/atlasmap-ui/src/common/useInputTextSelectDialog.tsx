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
  textValue1: React.MutableRefObject<string>;
  text1ReadOnly: boolean;
  textLabel2: string;
  textValue2: React.MutableRefObject<string>;
  selectLabel: string;
  selectValues: string[][];
  selectDefault: React.MutableRefObject<number>;
  modalContainer: HTMLElement;
}

export function useInputTextSelectDialog({
  title,
  textLabel1,
  textValue1,
  text1ReadOnly,
  textLabel2,
  textValue2,
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
  const [value1, setValue1] = useState(textValue1.current);
  const [value2, setValue2] = useState(textValue2.current);
  const [selectValue, setSelectValue] = useState(
    selectValues[selectDefault.current][0]
  );
  const openModal = (
    onConfirmCb?: ConfirmInputTextCallback,
    onCancelCb?: CancelCallback
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
    setValue1(textValue1.current);
    setValue2(textValue2.current);
    setSelectValue(selectValues[selectDefault.current][0]);
  };

  const closeModal = () => setIsOpen(false);

  const handleTextInputChange = (
    value: string,
    event: FormEvent<HTMLInputElement>
  ) => {
    setValue1(value);
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
      onConfirm.current(value1, value2, selectValue);
    closeModal();
  }, [onConfirm, value1, value2, selectValue, isValid]);

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
          data-testid={'text-select-dialog-confirm-button'}
        >
          Confirm
        </Button>,
        <Button
          key={'cancel'}
          variant={'link'}
          onClick={handleCancel}
          data-testid={'text-select-dialog-cancel-button'}
        >
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
            value={value1}
            type="text"
            onChange={handleTextInputChange}
            aria-label={title}
            isRequired={true}
            isValid={isValid}
            isDisabled={text1ReadOnly}
            data-testid={'value1-text-input'}
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
            data-testid={'value2-text-input'}
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
          data-testid={'type-dropdown-form-select'}
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
