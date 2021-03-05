import React, { FunctionComponent } from "react";

import { Button, Modal } from "@patternfly/react-core";

export type ConfirmCallback = () => void;
export type CancelCallback = () => void;

export interface IConfirmationDialogProps {
  title: string;
  description?: string;
  onConfirm?: ConfirmCallback;
  onCancel: CancelCallback;
  isOpen: boolean;
}

export const ConfirmationDialog: FunctionComponent<IConfirmationDialogProps> = ({
  title,
  description,
  onCancel,
  onConfirm,
  isOpen,
  children,
}) => {
  return (
    <Modal
      isSmall
      title={title}
      description={description}
      isOpen={isOpen}
      onClose={onCancel}
      actions={[
        <Button
          key={"confirm"}
          variant={"primary"}
          onClick={onConfirm}
          aria-label="Confirm"
          data-testid={"confirmation-dialog-confirm-button"}
          isDisabled={!onConfirm}
        >
          Confirm
        </Button>,
        <Button
          key={"cancel"}
          variant={"link"}
          onClick={onCancel}
          aria-label="Cancel"
          data-testid={"confirmation-dialog-cancel-button"}
        >
          Cancel
        </Button>,
      ]}
      isFooterLeftAligned={true}
    >
      {children}
    </Modal>
  );
};
