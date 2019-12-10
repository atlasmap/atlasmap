import { Modal, Button, TextInput } from '@patternfly/react-core';
import React, { ReactChild, ReactPortal, useCallback, useState } from 'react';
import { createPortal } from 'react-dom';
import { string } from 'prop-types';

export type Callback = ((closeDialog: () => void, value?: string) => void);

export interface IUseSingleInputDialogArgs {
  title: string;
  content: ReactChild;
  placeholder: string;
  onConfirm: Callback
  onCancel: Callback;
}

export function useSingleInputDialog({
  title,
  content,
  placeholder,
  onConfirm,
  onCancel,
}: IUseSingleInputDialogArgs): [ReactPortal, () => void] {
  const [isOpen, setIsOpen] = useState(false);
  const [value, setValue] = useState('');
  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);
  const handleTextInputChange = (value: string) => {
    setValue(value);
  };
  const handleConfirm = useCallback(() => onConfirm(closeModal, value), [onConfirm, value]);
  const handleCancel = useCallback(() => onCancel(closeModal), [onCancel]);

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
        <TextInput key={'text-input'} value={value} type="text"
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

