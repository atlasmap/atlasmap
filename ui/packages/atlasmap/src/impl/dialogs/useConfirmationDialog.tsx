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
import React, { ReactChild, ReactElement, useRef } from 'react';

import { ConfirmationDialog } from '../../UI';
import { TextContent } from '@patternfly/react-core';
import { useToggle } from '../utils';

export function useConfirmationDialog(
  title: string,
  content: ReactChild,
): [ReactElement, (cb: () => void) => void] {
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirmCb = useRef<(() => void) | null>(null);
  const onConfirm = () => {
    if (onConfirmCb.current) {
      onConfirmCb.current();
    }
    toggleOff();
  };
  const openDialog = (confirmCb: () => void) => {
    onConfirmCb.current = confirmCb;
    toggleOn();
  };
  return [
    <ConfirmationDialog
      key={'confirmation-dialog-' + title}
      title={title}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      isOpen={state}
    >
      <TextContent>{content}</TextContent>
    </ConfirmationDialog>,
    openDialog,
  ];
}
