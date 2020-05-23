import React, { useCallback, ReactElement, useState } from "react";

import { useToggle, NamespaceDialog, INamespace } from "../../UI";

type NamespaceCallback = (namespace: INamespace) => void;

export function useNamespaceDialog(
  title: string,
): [ReactElement, (cb: NamespaceCallback, namespace?: INamespace) => void] {
  const [onNamespaceCb, setOnNamespaceCb] = useState<NamespaceCallback | null>(
    null,
  );
  const [initialNamespace, setInitialNamespace] = useState<INamespace | null>(
    null,
  );
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (namespace: INamespace) => {
      if (onNamespaceCb) {
        onNamespaceCb(namespace);
        toggleOff();
      }
    },
    [onNamespaceCb, toggleOff],
  );
  const dialog = (
    <NamespaceDialog
      title={title}
      isOpen={state}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialNamespace || {})}
    />
  );
  const onOpenNamespaceDialog = useCallback(
    (callback: NamespaceCallback, namespace?: INamespace) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnNamespaceCb(() => callback);
      if (namespace) {
        setInitialNamespace(namespace);
      }
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenNamespaceDialog];
}
