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
import { Button, Modal, Radio } from '@patternfly/react-core';
import React, { ReactElement, useRef } from 'react';

import { useToggle } from '../utils';

export function useSpecifyInstanceSchemaDialog(
  defaultSchema: boolean,
): [ReactElement, (cb: (isSchema: boolean) => void) => void] {
  const dialogState: {
    state: boolean;
    toggleOn: () => void;
    toggleOff: () => void;
  } = useToggle(false);
  const isSchema: {
    state: boolean;
    toggleOn: () => void;
    toggleOff: () => void;
  } = useToggle(defaultSchema);
  const onConfirmCb = useRef<((isSchema: boolean) => void) | null>(null);
  const onConfirm = () => {
    if (onConfirmCb.current) {
      onConfirmCb.current(isSchema.state);
    }
    dialogState.toggleOff();
  };
  const openDialog = (confirmCb: (isSchema: boolean) => void) => {
    onConfirmCb.current = confirmCb;
    if (defaultSchema) {
      isSchema.toggleOn();
    } else {
      isSchema.toggleOff();
    }
    dialogState.toggleOn();
  };
  const handleInstanceChange = () => {
    isSchema.toggleOff();
  };
  const handleSchemaChange = () => {
    isSchema.toggleOn();
  };
  return [
    <Modal
      key="specify-instance-schema-dialog"
      variant="small"
      title={'Specify Instance/ Schema'}
      description={
        'Distinguish between instance and schema imported file formats.'
      }
      isOpen={dialogState.state}
      onClose={dialogState.toggleOff}
      actions={[
        <Button
          key={'confirm'}
          variant={'primary'}
          onClick={onConfirm}
          aria-label="Ok"
          data-testid={'specify-instance-schema-dialog-ok-button-test'}
          isDisabled={!onConfirm}
          style={{ display: 'flex', marginLeft: 'auto' }}
        >
          OK
        </Button>,
      ]}
    >
      {
        <React.Fragment>
          <Radio
            isChecked={!isSchema.state}
            name="instance"
            onChange={handleInstanceChange}
            label="Instance"
            id="instance-radio"
            data-testid={'instance-radio-button-test'}
          />
          <Radio
            isChecked={isSchema.state}
            name="schema"
            onChange={handleSchemaChange}
            label="Schema"
            id="schema-radio"
            data-testid={'schema-radio-button-test'}
          />
        </React.Fragment>
      }
    </Modal>,
    openDialog,
  ];
}
