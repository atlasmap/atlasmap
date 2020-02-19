import { Modal, Button } from '@patternfly/react-core';
import React, {
  ReactChild,
  ReactPortal,
  useCallback,
  useRef,
  useState,
} from 'react';
import { createPortal } from 'react-dom';

export type Callback = () => void;

export interface IUseConfirmationDialogArgs {
  title: string;
  content: ReactChild;
  modalContainer: HTMLElement;
}

export function useConfirmationDialog({
  title,
  content,
  modalContainer,
}: IUseConfirmationDialogArgs): [
  ReactPortal,
  (onConfirm?: Callback, onCancel?: Callback) => void
] {
  const onConfirm = useRef<Callback | undefined>();
  const onCancel = useRef<Callback | undefined>();
  const [isOpen, setIsOpen] = useState(false);
  const openModal = (onConfirmCb?: Callback, onCancelCb?: Callback) => {
    onConfirm.current = onConfirmCb;
    onCancel.current = onCancelCb;
    setIsOpen(true);
  };
  const closeModal = () => setIsOpen(false);
  const handleConfirm = useCallback(() => {
    onConfirm.current && onConfirm.current();
    closeModal();
  }, [onConfirm]);
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
        <Button key={'confirm'} variant={'primary'} onClick={handleConfirm}>
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
