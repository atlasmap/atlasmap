import { Modal, Button, TextInput } from '@patternfly/react-core';
import React, {
  FormEvent,
  ReactChild,
  ReactPortal,
  useCallback,
  useRef,
  useState,
} from 'react';
import { createPortal } from 'react-dom';

export type ConfirmCallback = (value: string) => void;
export type CancelCallback = () => void;

export interface IUseSingleInputDialogArgs {
  title: string;
  content: ReactChild;
  placeholder: string;
  modalContainer: HTMLElement;
}

export function useSingleInputDialog({
  title,
  content,
  placeholder,
  modalContainer,
}: IUseSingleInputDialogArgs): [
  ReactPortal,
  (onConfirm?: ConfirmCallback, onCancel?: CancelCallback) => void
] {
  const onConfirm = useRef<ConfirmCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [value, setValue] = useState('');
  const openModal = (
    onConfirmCb?: ConfirmCallback,
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
  const handleConfirm = useCallback(() => {
    isValid && onConfirm.current && onConfirm.current(value);
    closeModal();
  }, [onConfirm, value, isValid]);
  const handleCancel = useCallback(() => {
    onCancel.current && onCancel.current();
    closeModal();
  }, [onCancel]);

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <TextInput
          key={'text-input'}
          value={value}
          placeholder={placeholder}
          type="text"
          onChange={handleTextInputChange}
          aria-label={title}
          isRequired={true}
          isValid={isValid}
        />,
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
      isFooterLeftAligned={true}
    >
      {content}
    </Modal>,
    modalContainer
  );

  return [modal, openModal];
}
