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
import {
  ConfirmationDialog,
  IConfirmationDialogProps,
} from './ConfirmationDialog';
import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useState,
} from 'react';

import { TextInput } from '@patternfly/react-core';

export interface IDocumentName {
  id: string;
  name: string;
  isSource: boolean;
}
export interface IChangeDocumentNameDialogProps {
  id?: string;
  name?: string;
  isSource?: boolean;
  isOpen: IConfirmationDialogProps['isOpen'];
  onCancel: IConfirmationDialogProps['onCancel'];
  onConfirm: (updatedDocNameInfo: IDocumentName) => void;
}
export const ChangeDocumentNameDialog: FunctionComponent<
  IChangeDocumentNameDialogProps
> = ({ id, name: initialName = '', isSource, isOpen, onCancel, onConfirm }) => {
  const [documentName, setDocumentName] = useState(initialName);

  const reset = useCallback(() => {
    setDocumentName(initialName);
  }, [initialName]);

  const handleOnConfirm = useCallback(() => {
    if (id && isSource !== undefined) {
      onConfirm({ id: id, name: documentName, isSource: isSource });
    }
    reset();
  }, [documentName, id, isSource, onConfirm, reset]);

  const handleOnCancel = useCallback(() => {
    onCancel();
    reset();
  }, [onCancel, reset]);

  function handleOnNameChange(name: string) {
    setDocumentName(name);
  }

  // resync the internal state to the values passed in as props
  useEffect(reset, [reset]);

  return (
    <ConfirmationDialog
      title={'Change selected document name?'}
      onCancel={handleOnCancel}
      onConfirm={handleOnConfirm}
      isOpen={isOpen}
    >
      <TextInput
        value={documentName}
        onChange={(value) => handleOnNameChange(value)}
        id={id}
        name={documentName}
        data-testid={id + '-parameter-text-input'}
      />
    </ConfirmationDialog>
  );
};
