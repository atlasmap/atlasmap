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
import { Button, Modal, TextInput } from '@patternfly/react-core';
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
  (onConfirm?: ConfirmCallback, onCancel?: CancelCallback) => void,
] {
  const onConfirm = useRef<ConfirmCallback | undefined>();
  const onCancel = useRef<CancelCallback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const [isValid, setIsValid] = useState(true);
  const [value, setValue] = useState('');
  const openModal = (
    onConfirmCb?: ConfirmCallback,
    onCancelCb?: CancelCallback,
  ) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
  };
  const closeModal = () => setIsOpen(false);
  const handleTextInputChange = (
    value: string,
    event: FormEvent<HTMLInputElement>,
  ) => {
    setValue(value);
    setIsValid((event.target as HTMLInputElement).reportValidity());
  };
  const handleConfirm = useCallback(() => {
    if (isValid && onConfirm.current) {
      onConfirm.current(value);
    }
    closeModal();
  }, [onConfirm, value, isValid]);
  const handleCancel = useCallback(() => {
    if (onCancel.current) {
      onCancel.current();
    }
    closeModal();
  }, [onCancel]);

  const modal = createPortal(
    <Modal
      variant="small"
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
          validated={isValid ? 'default' : 'error'}
          data-testid={'input-text-field'}
        />,
        <Button
          key={'confirm'}
          variant={'primary'}
          onClick={handleConfirm}
          isDisabled={!isValid}
          aria-label="Confirm"
          data-testid={'single-input-dialog-confirm-button'}
        >
          Confirm
        </Button>,
        <Button
          key={'cancel'}
          variant={'link'}
          onClick={handleCancel}
          aria-label="Cancel"
          data-testid={'single-input-dialog-cancel-button'}
        >
          Cancel
        </Button>,
      ]}
    >
      {content}
    </Modal>,
    modalContainer,
  );

  return [modal, openModal];
}
