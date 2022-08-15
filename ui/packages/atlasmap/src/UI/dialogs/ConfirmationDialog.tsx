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
import { Button, Modal } from '@patternfly/react-core';
import React, { FunctionComponent } from 'react';

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

export const ConfirmationDialog: FunctionComponent<
  IConfirmationDialogProps
> = ({
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
          key={'confirm'}
          variant={'primary'}
          onClick={onConfirm}
          aria-label="Confirm"
          data-testid={'confirmation-dialog-confirm-button'}
          isDisabled={!onConfirm}
        >
          Confirm
        </Button>,
        <Button
          key={'cancel'}
          variant={'link'}
          onClick={onCancel}
          aria-label="Cancel"
          data-testid={'confirmation-dialog-cancel-button'}
        >
          Cancel
        </Button>,
      ]}
    >
      {children}
    </Modal>
  );
};
