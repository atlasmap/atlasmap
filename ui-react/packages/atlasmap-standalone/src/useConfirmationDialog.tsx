import { Modal, Button } from '@patternfly/react-core';
import React, { ReactChild, ReactPortal, useCallback, useState } from 'react';
import { createPortal } from 'react-dom';

export type Callback = (closeDialog: () => void) => void;

export interface IUseConfirmationDialogArgs {
  title: string;
  content: ReactChild;
  onConfirm: Callback;
  onCancel: Callback;
}

export function useConfirmationDialog({
  title,
  content,
  onConfirm,
  onCancel
}: IUseConfirmationDialogArgs): [ReactPortal, () => void] {
  const [isOpen, setIsOpen] = useState(false);
  const openModal = () => setIsOpen(true);
  const closeModal = () => setIsOpen(false);
  const handleConfirm = useCallback(() => onConfirm(closeModal), [onConfirm]);
  const handleCancel = useCallback(() => onCancel(closeModal), [onCancel]);

  const modal = createPortal(
    <Modal
      isSmall
      title={title}
      isOpen={isOpen}
      onClose={closeModal}
      actions={[
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