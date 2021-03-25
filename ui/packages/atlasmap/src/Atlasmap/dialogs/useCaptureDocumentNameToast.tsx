import { ReactElement, useCallback, useRef } from "react";

import { TimedToast } from "../../UI";
import { useToggle } from "../../Atlasmap/utils";
import React from "react";
import { copyToClipboard } from "../utils/ui";
let docId = "";

function useCreateToast(): [ReactElement, (cb: () => void) => void] {
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onExecuteCb = useRef<(() => void) | null>(null);
  const onClose = () => {
    toggleOff();
  };
  const description = "Document ID " + docId + " copied to clipboard.";
  const openToast = (executeCb: () => void) => {
    onExecuteCb.current = executeCb;
    toggleOn();
  };
  let toastElement: ReactElement = (
    <TimedToast
      variant={"info"}
      title={"Capture Document ID"}
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

export function useCaptureDocumentNameToast(): [
  ReactElement,
  (documentId: string) => void,
] {
  const [
    captureDocumentNameToast,
    openCaptureDocumentNameToast,
  ] = useCreateToast();
  const onCaptureDocumentName = useCallback(
    (documentId: string) => {
      docId = documentId;
      openCaptureDocumentNameToast(() => copyToClipboard(documentId));
    },
    [openCaptureDocumentNameToast],
  );
  return [captureDocumentNameToast, onCaptureDocumentName];
}
