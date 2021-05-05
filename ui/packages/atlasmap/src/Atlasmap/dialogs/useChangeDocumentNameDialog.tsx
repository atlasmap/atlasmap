import { ChangeDocumentNameDialog, IDocumentName } from "../../UI";
import React, { ReactElement, useCallback, useState } from "react";

import { useToggle } from "../../Atlasmap/utils";

type ChangeDocumentNameCallback = (documentNameInfo: IDocumentName) => void;

export function useChangeDocumentNameDialog(): [
  ReactElement,
  (cb: ChangeDocumentNameCallback, documentNameInfo: IDocumentName) => void,
] {
  const [onDocumentNameCb, setOnChangeDocumentNameCb] =
    useState<ChangeDocumentNameCallback | null>(null);
  const [initialDocumentName, setInitialDocumentName] =
    useState<IDocumentName | null>({
      id: "",
      name: "",
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
