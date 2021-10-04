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
import { ReactElement, useCallback, useRef } from 'react';

import React from 'react';
import { TimedToast } from '../../UI';
import { copyToClipboard } from '../utils/ui';
import { useToggle } from '../utils';

let docId = '';

function useCreateToast(): [ReactElement, (cb: () => void) => void] {
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onExecuteCb = useRef<(() => void) | null>(null);
  const onClose = () => {
    toggleOff();
  };
  const description = 'Document ID ' + docId + ' copied to clipboard.';
  const openToast = (executeCb: () => void) => {
    onExecuteCb.current = executeCb;
    toggleOn();
  };
  let toastElement: ReactElement = (
    <TimedToast
      variant={'info'}
      title={'Capture Document ID'}
      key={docId}
      onClose={onClose}
      onTimeout={onClose}
    >
      {description}
    </TimedToast>
  );

  if (state) {
    if (onExecuteCb.current) {
      onExecuteCb.current();
    }
  } else {
    toastElement = <span></span>;
  }
  return [toastElement, openToast];
}

export function useCaptureDocumentIDToast(): [
  ReactElement,
  (documentId: string) => void,
] {
  const [captureDocumentIDToast, openCaptureDocumentIDToast] = useCreateToast();
  const onCaptureDocumentID = useCallback(
    (documentId: string) => {
      docId = documentId;
      openCaptureDocumentIDToast(() => copyToClipboard(documentId));
    },
    [openCaptureDocumentIDToast],
  );
  return [captureDocumentIDToast, onCaptureDocumentID];
}
