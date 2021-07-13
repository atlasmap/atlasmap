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
import { ChangeDocumentNameDialog, IDocumentName } from '../../UI';
import React, { ReactElement, useCallback, useState } from 'react';

import { useToggle } from '../utils';

type ChangeDocumentNameCallback = (documentNameInfo: IDocumentName) => void;

export function useChangeDocumentNameDialog(): [
  ReactElement,
  (cb: ChangeDocumentNameCallback, documentNameInfo: IDocumentName) => void,
] {
  const [onDocumentNameCb, setOnChangeDocumentNameCb] =
    useState<ChangeDocumentNameCallback | null>(null);
  const [initialDocumentName, setInitialDocumentName] =
    useState<IDocumentName | null>({
      id: '',
      name: '',
      isSource: false,
    });
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (documentNameInfo: IDocumentName) => {
      if (onDocumentNameCb) {
        onDocumentNameCb(documentNameInfo);
        toggleOff();
      }
    },
    [onDocumentNameCb, toggleOff],
  );
  const dialog = (
    <ChangeDocumentNameDialog
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialDocumentName || {})}
    />
  );
  const onOpenChangeDocumentNameDialog = useCallback(
    (callback: ChangeDocumentNameCallback, documentNameInfo: IDocumentName) => {
      setOnChangeDocumentNameCb(() => callback);
      setInitialDocumentName(documentNameInfo);
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenChangeDocumentNameDialog];
}
