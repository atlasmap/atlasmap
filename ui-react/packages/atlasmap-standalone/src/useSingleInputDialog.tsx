import { Modal, Button, TextInput } from '@patternfly/react-core';
import React, { ReactChild, ReactPortal, useCallback, useState } from 'react';
import { createPortal } from 'react-dom';

export type Callback = (closeDialog: () => void) => void;

export interface IUseSingleInputDialogArgs {
  title: string;
  content: ReactChild;
  placeholder: string;
  state: {
    inputValue: ''
  };
  onConfirm: Callback;
  onCancel: Callback;
}

export function useSingleInputDialog({
  title,
  content,
  placeholder,
  state,
  onConfirm,
  onCancel,
}: IUseSingleInputDialogArgs): [ReactPortal, () => void] {
  const [isOpen, setIsOpen] = useState(false);
  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);
  const handleTextInputChange = (value: string) => {
    console.log('>>>>>>>>>>>>>>>>>>>>>> handleTextInputChange - value is ' + value);
    // setState({ value });
  };

  const handleConfirm = useCallback(() => onConfirm(closeModal), [onConfirm]);
  const handleCancel = useCallback(() => onCancel(closeModal), [onCancel]);
  const { inputValue } = state;

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <TextInput key={'text-input'} value={inputValue} type="text"
         onChange={handleTextInputChange} aria-label={title} placeholder={placeholder}/>,
        <Button key={'confirm'} variant={'primary'} onClick={handleConfirm}>
          Confirm
        </Button>,
        <Button key={'cancel'} variant={'link'} onClick={handleCancel}>
          Cancel
        </Button>
      ]}
      isFooterLeftAligned={true}
    >
      {content}
    </Modal>,
    document.getElementById('modals')!
  );

  return [modal, openModal];
}

