import { constantTypes } from "@atlasmap/core";

import React, { useCallback, ReactElement, useState } from "react";

import { ConstantDialog, IConstant } from "../../UI";
import { useToggle } from "../../Atlasmap/utils";
import { IAtlasmapDocument } from "../../Views";

type ConstantCallback = (constant: IConstant) => void;

export function useConstantDialog(
  title: string,
): [
  ReactElement,
  (
    cb: ConstantCallback,
    constants: IAtlasmapDocument | null,
    constant?: IConstant,
  ) => void,
] {
  const [onConstantCb, setOnConstantCb] = useState<ConstantCallback | null>(
    null,
  );
  const [initialConstant, setInitialConstant] = useState<IConstant | null>({
    value: "",
    valueType: constantTypes[0][0],
  });
  const [constants, setConstants] = useState<IAtlasmapDocument | null>(null);
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
  function onValidation(value: string): boolean {
    if (constants) {
      // Ensure constant value is unique
      const fields = constants.fields.filter(
        (fieldOrGroup) => fieldOrGroup.name === value,
      );
      return value === initialConstant?.value || fields.length === 0;
    }
    return true;
  }
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
      onValidation={onValidation}
      {...(initialConstant || {})}
    />
  );
  const onOpenConstantDialog = useCallback(
    (
      callback: ConstantCallback,
      constants: IAtlasmapDocument | null,
      constant?: IConstant,
    ) => {
      // we use a closure to set the state here else React will think that callback
      // is the function to retrieve the state and will call it immediately.
      setOnConstantCb(() => callback);
      if (constant) {
        setInitialConstant(constant);
      }
      if (constants) {
        setConstants(constants);
      }
      toggleOn();
    },
    [toggleOn],
  );
  return [dialog, onOpenConstantDialog];
}
