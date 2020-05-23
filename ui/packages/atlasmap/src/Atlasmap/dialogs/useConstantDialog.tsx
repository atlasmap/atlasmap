import { constantTypes } from "@atlasmap/core";

import React, { useCallback, ReactElement, useState } from "react";

import { useToggle, ConstantDialog, IConstant } from "../../UI";

type ConstantCallback = (constant: IConstant) => void;

export function useConstantDialog(
  title: string,
): [ReactElement, (cb: ConstantCallback, constant?: IConstant) => void] {
  const [onConstantCb, setOnConstantCb] = useState<ConstantCallback | null>(
    null,
  );
  const [initialConstant, setInitialConstant] = useState<IConstant | null>(
    null,
  );
  const { state, toggleOn, toggleOff } = useToggle(false);
  const onConfirm = useCallback(
    (constant: IConstant) => {
      if (onConstantCb) {
        onConstantCb(constant);
        toggleOff();
      }
    },
    [onConstantCb, toggleOff],
  );
  const dialog = (
    <ConstantDialog
      title={title}
      isOpen={state}
      valueTypeOptions={constantTypes.map(([value, label]) => ({
        value,
        label,
      }))}
      onCancel={toggleOff}
      onConfirm={onConfirm}
      {...(initialConstant || {})}
    />
  );
  const onOpenConstantDialog = useCallback(
    (callback: ConstantCallback, constant?: IConstant) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnConstantCb(() => callback);
      if (constant) {
        setInitialConstant(constant);
      }
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenConstantDialog];
}
