import { Button, Modal } from "@patternfly/react-core";
import React, { FunctionComponent } from "react";

export type ConfirmCallback = () => void;
export type CancelCallback = () => void;

export interface IConfirmationDialogProps {
  title: string;
  description?: string;
  dataTestid?: string;
  onConfirm?: ConfirmCallback;
  onCancel: CancelCallback;
  isOpen: boolean;
}

export const ConfirmationDialog: FunctionComponent<IConfirmationDialogProps> =
  ({
    title,
    description,
    dataTestid,
    onCancel,
    onConfirm,
    isOpen,
    children,
  }) => {
    return (
      <Modal
        variant="small"
        title={title}
        description={description}
        data-testid={dataTestid}
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
      >
        {children}
      </Modal>
    );
  };
