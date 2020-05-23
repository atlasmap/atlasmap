import React, { useRef, ReactElement, ReactChild } from "react";

import { TextContent } from "@patternfly/react-core";

import { ConfirmationDialog, useToggle } from "../../UI";

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
